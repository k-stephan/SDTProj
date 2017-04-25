package com.macys.sdt.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.macys.sdt.framework.runner.RunConfig;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.parser.Parser;
import gherkin.util.FixJava;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.macys.sdt.framework.utils.StepUtils.macys;

/**
 * This is a generic utility class for interacting with files and cucumber
 */
@SuppressWarnings("deprecation")
public class Utils {

    public static final Logger logger = LoggerFactory.getLogger(Utils.class);
    // use these to redirect unneeded error output
    private static boolean resourcesExtracted = false;

    /**
     * Executes a command on the command line (cmd for windows, else bash)
     *
     * @param cmd command to run
     * @return result of command
     */
    public static String executeCMD(String cmd) {
        long ts = System.currentTimeMillis();
        Process p = null;
        if (!isWindows()) {
            cmd = cmd.replaceAll("\"", "\\\\\"");
        } else {
            cmd = "cmd.exe /c \"" + cmd + "\"";
        }
        logger.info("command : " + cmd);
        try {
            if (isWindows()) {
                p = Runtime.getRuntime().exec(cmd);
            } else {
                String[] cmds = new String[]{"bash", "-c", cmd};
                p = Runtime.getRuntime().exec(cmds);
            }
            return captureOutput(p);
        } catch (Throwable e1) {
            logger.warn("issue to execute command : " + e1.getMessage());
        } finally {
            if (p != null) {
                p.destroy();
            }
            logger.info("--> " + (System.currentTimeMillis() - ts) + " : " + cmd);
        }

        return null;
    }

    /**
     * Retrieves information about the selenium driver
     *
     * @param driverPath path to the driver
     * @return String with driver information
     */
    public static String getSeleniumDriverInfo(File driverPath) {
        String msg = "Cannot capture driver info.";
        String cmd;
        try {
            cmd = driverPath.getCanonicalPath();
        } catch (Exception ex) {
            logger.error("can't capture driver info due to : " + ex.getMessage());
            return msg;
        }
        Process p = null;
        //    	cmd = "cmd.exe /c \"" + cmd + "\"";
        if (!isWindows()) {
            cmd = cmd.replaceAll("\"", "\\\\\"");
        }
        logger.info("command : " + cmd);
        ProcessWatchDog pd = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            pd = new ProcessWatchDog(p, 3000, "getSeleniumDriverInfo()");
            return captureOutput(p).replace('\n', ' ');
        } catch (Throwable e1) {
            logger.error("issue in capturing driver info due to : " + e1.getMessage());
            return msg;
        } finally {
            if (pd != null) {
                pd.interrupt();
            }
            if (p != null) {
                p.destroy();
            }
            //logger.info("-->" + (System.currentTimeMillis() - ts) + ":" + cmd);
        }
    }

    /**
     * Reads a text file to a string
     *
     * @param f file to read
     * @return file contents
     * @throws IOException read errors
     */
    public static String readTextFile(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = Files.newInputStream(f.toPath(), StandardOpenOption.READ)) {
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader lineReader = new BufferedReader(reader);

            String line;
            while ((line = lineReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Creates a directory
     *
     * @param dir   directory name
     * @param clean true for a clean directory
     * @return resulting File
     */
    public static File createDirectory(String dir, boolean clean) {
        return createDirectory(new File(dir), clean);
    }

    /**
     * Creates a directory
     *
     * @param fDir  File to create
     * @param clean whether to delete any existing directory
     * @return directory that was created
     */
    public static File createDirectory(File fDir, boolean clean) {
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                logger.error("Unable to make directory: " + fDir.getName());
            }
        }
        if (clean) {
            try {
                FileUtils.cleanDirectory(fDir);
            } catch (IOException e) {
                logger.error("Error cleaning directory: " + e.getMessage());
            }
        }
        return fDir;
    }

    /**
     * Creates a directory
     *
     * @param dir directory name
     * @return resulting File
     */
    public static File createDirectory(String dir) {
        return createDirectory(dir, false);
    }

    /**
     * Returns the SHA key of a feature
     *
     * @param feature  feature file path
     * @param scenario scenario name
     * @return SHA key
     */
    public static String getScenarioShaKey(String feature, String scenario) {
        String path = (feature + scenario).replaceAll("\\s", "");
        String key = DigestUtils.sha256Hex(path);
        logger.error("...key generation: " + path + " : " + key);
        return key;
    }

    /**
     * Converts json to "pretty" format
     *
     * @param o input json
     * @return formatted JSON as string
     */
    public static String jsonPretty(Object o) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(o);
    }

    /**
     * Writes a binary file
     *
     * @param aBytes    bytes to write
     * @param aFileName File to write to
     * @return true if write succeeded
     */
    public static boolean writeSmallBinaryFile(byte[] aBytes, File aFileName) {
        DataOutputStream os = null;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(aFileName);
            os = new DataOutputStream(fout);
            os.write(aBytes);
            return true;
        } catch (IOException ex) {
            logger.error("Cannot create file : " + aFileName.getPath());
        } finally {
            closeIoOutput(os);
            closeIoOutput(fout);
        }
        return false;
    }

    /**
     * Converts from milliseconds to days/hours/minutes/seconds
     *
     * @param millis milliseconds to convert
     * @return string result of conversion
     */
    public static String toDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) {
            sb.append(days).append(" Days ");
        }
        if (hours > 0) {
            sb.append(hours).append(" Hours ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" Minutes ");
        }
        sb.append(seconds).append(".").append(millis).append(" Seconds");

        return (sb.toString());
    }

    /**
     * Writes a binary file
     *
     * @param aBytes    bytes to write
     * @param aFileName file to write to
     * @param append    whether or not to append to an existing file
     * @return true if write succeeded
     */
    public static boolean writeBinaryFile(byte[] aBytes, File aFileName, boolean append) {
        try {
            if (!append && aFileName.exists()) {
                //				logger.info("writeSmallBinaryFile(): deleting " + aFileName.getCanonicalPath());
                if (!aFileName.delete()) {
                    logger.error("Unable to delete file: " + aFileName.getName());
                }
            }
            File fDir = aFileName.getAbsoluteFile().getParentFile();
            if (!fDir.exists()) {
                if (!fDir.mkdirs()) {
                    logger.error("Unable to create directory: " + fDir.getName());
                    return false;
                }
            }

            Path path = Paths.get(aFileName.getCanonicalPath());
            if (append && aFileName.exists()) {
                Files.write(path, aBytes, StandardOpenOption.APPEND);
            } else {
                Files.write(path, aBytes); // creates, overwrites
            }
            return true;
        } catch (Exception e) {
            logger.error("unable to write file due to : " + e.getMessage());
            return false;
        }
    }

    /**
     * Attempts to convert an object into an int
     *
     * @param number object to convert
     * @param ret    value to return if conversion fails
     * @return result of conversion
     */
    public static int parseInt(Object number, int ret) {
        try {
            if (number == null) {
                return ret;
            }
            if (number instanceof Float) {
                return ((Float) number).intValue();
            }
            if (number instanceof Double) {
                return ((Double) number).intValue();
            }
            return Integer.parseInt(number.toString().replaceAll(",", "").split("\\.")[0]);
        } catch (Exception ex) {
            return ret;
        }
    }

    /**
     * Converts gherkin feature file into json format
     *
     * @param isPretty true for "pretty" formatting
     * @param path     path to write output to
     * @return json string
     */
    @SuppressWarnings("deprecation")
    public static String gherkinToJson(boolean isPretty, String path) {
        // Define Feature file and JSON File path.
        String gherkin = null;
        try {
            gherkin = FixJava.readReader(new InputStreamReader(new FileInputStream(path.trim()), "UTF-8"));
        } catch (FileNotFoundException e) {
            Assert.fail("Feature file not found at " + path);
        } catch (UnsupportedEncodingException | RuntimeException e) {
            logger.error("issue to convert feature file : " + e.getMessage());
        }

        StringBuilder json = new StringBuilder();
        JSONFormatter formatter;
        // pretty or ugly selection, pretty by default
        if (!isPretty) {
            formatter = new JSONFormatter(json);// not pretty
        } else {
            formatter = new JSONPrettyFormatter(json);// pretty
        }

        Parser parser = new Parser(formatter);
        parser.parse(gherkin, path, 0);
        formatter.done();
        formatter.close();
        //		logger.info("json output: n" + json + "'");
        return json.toString();
    }

    /**
     * Sleeps for a given time
     *
     * @param sleepTime time to sleep in millis
     * @param msg       info message to display
     * @return true if sleep interrupted
     */
    public static boolean threadSleep(long sleepTime, String msg) {
        Thread cur = Thread.currentThread();
        try {
            if (msg != null)
                logger.trace("Thread \"" + cur.getName() + "\" sleeping for: " + sleepTime + "ms");
            Thread.sleep(sleepTime);
            if (msg != null)
                logger.trace("Thread \"" + cur.getName() + " is awake");
            return false;
        } catch (InterruptedException e) {
            if (msg != null)
                logger.trace("Thread \"" + cur.getName() + " was interrupted:" + e.getMessage());
            return true;
        }
    }

    /**
     * Checks if the machine is running OSX
     *
     * @return true if running on an OSX machine
     */
    public static boolean isOSX() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Checks if the machine is running windows
     *
     * @return true if running on a windows machine
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Checks if the machine is running windows 8
     *
     * @return true if running on a windows 8 machine
     */
    public static boolean isWindows8() {
        return System.getProperty("os.name").toLowerCase().contains("windows 8");
    }

    /**
     * Checks if the machine is running linux
     *
     * @return true if running on a linux machine
     */
    public static boolean isLinux() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    /**
     * Gets the method that called another
     *
     * @param from method to look for caller of
     * @return call stack which lead to the call you asked for
     */
    public static List<String> getCallFromFunction(String from) {
        return getCallFromFunction(from, 5);
    }

    /**
     * Gets the method that called another
     *
     * @param from method to look for caller of
     * @param size number of callers to list
     * @return call stack which lead to the call you asked for
     */
    public static List<String> getCallFromFunction(String from, int size) {
        StackTraceElement[] stackels = Thread.currentThread().getStackTrace();
        ArrayList<String> displayEls = new ArrayList<>();
        int count = 20;
        for (StackTraceElement stackel : stackels) {
            String trace = stackel.toString();
            if (trace.contains(".getStackTrace(") ||
                    trace.contains(".getCallFromFunction(") ||
                    trace.contains(from)) {
                continue;
            }
            if (trace.startsWith("com.macys.sdt.")) {
                displayEls.add(trace);
            }
            if (displayEls.size() == size) {
                break;
            }
            if (--count <= 0) {
                break;
            }
        }
        return displayEls;
    }

    /**
     * Gets the ID of the current process
     *
     * @return process ID
     */
    public static int getProcessId() {
        try {
            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
            pid_method.setAccessible(true);

            return (Integer) pid_method.invoke(mgmt);
        } catch (Exception e) {
            logger.error("error to retrieve process id : " + e.getMessage());
            logger.debug("retrieve process id error : " + e);
        }
        return -1;
    }

    /**
     * Captures the desktop and writes it to an output stream
     *
     * @param out output stream to write data to
     * @throws Exception write error
     */
    public static void desktopCapture(OutputStream out) throws Exception {
        long ts = System.currentTimeMillis();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        ImageIO.write(image, "png", out);
        logger.info("capture desktop : " + (System.currentTimeMillis() - ts));
    }

    /**
     * Gets a resource file with a given name
     *
     * @param fName file name
     * @return resulting File
     */
    public static File getResourceFile(String fName) {
        File resource;
        String path;
        // project data
        String resourcePath = getResourcePath(fName);
        for (String resourceDir : RunConfig.projectResourceDirs) {
            // default location: <screen size>/<site>/fName
            path = RunConfig.workspace + resourceDir + "/data/" + resourcePath;
            resource = new File(path);
            if (resource.exists() && !resource.isDirectory()) {
                return resource;
            }

            // root of resources dir
            resource = new File(RunConfig.workspace + resourceDir + "/" + fName);
            if (resource.exists() && !resource.isDirectory()) {
                return resource;
            }

            // check for a responsive resource if applicable
            if (path.matches(".*?(/MEW/|/website/).*?")) {
                resource = new File(path.replace("/MEW/", "/responsive/").replace("/website/", "responsive"));
                if (resource.exists() && !resource.isDirectory()) {
                    return resource;
                }
            }

            //fallback to website resources
            resource = new File(path.replace("/MEW/", "/website/").replace("/iOS/", "/website/").replace("/android/", "/website/"));
            if (resource.exists() && !resource.isDirectory()) {
                return resource;
            }
        }

        // shared data
        path = RunConfig.workspace + RunConfig.sharedResourceDir + "/data/" + resourcePath;
        resource = new File(path);
        if (resource.exists() && !resource.isDirectory()) {
            return resource;
        }
        if (!resource.exists()) {
            //fallback to website resources
            resource = new File(path.replace("/MEW/", "/website/").replace("/iOS/", "/website/").replace("/android/", "/website/"));
            if (resource.exists() && !resource.isDirectory()) {
                return resource;
            }
        }

        return resource;
    }

    /**
     * Gets the path to a resource file
     *
     * @param fName file to look for
     * @return file path
     */
    private static String getResourcePath(String fName) {
        String resPath;
        if (RunConfig.appTest) {
            resPath = StepUtils.iOS() ? "iOS/" : "android/";
        } else {
            resPath = StepUtils.MEW() ? "MEW/" : "website/";
        }
        resPath += (StepUtils.macys() ? "mcom/" : (StepUtils.bloomingdales() ? "bcom/" : "other/"));

        return resPath + fName;
    }

    /**
     * Convert List Object To String
     *
     * @param list   List Object of Strings
     * @param token  separator to used for string list
     * @param cleans strings to remove from list
     * @return List of strings separated by token
     */
    public static String listToString(List<String> list, String token, String[] cleans) {
        if (cleans != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                String s = list.get(i);
                boolean clean = false;
                for (String c : cleans) {
                    if (s.contains(c)) {
                        clean = true;
                        break;
                    }
                }
                if (clean) {
                    list.remove(i);
                }
            }
        }
        return String.join(token, list.toArray(new String[list.size()]));
    }

    private static String captureOutput(Process proc) throws Exception {
        ReadStream stdin = new ReadStream("stdin", proc.getInputStream());
        ReadStream stderr = new ReadStream("stdin", proc.getErrorStream());
        proc.waitFor();

        return stdin.getConsole().append("\n+++Error console:\n").append(stderr.getConsole()).toString();

    }

    protected static byte[] readSmallBinaryFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            Path path = Paths.get(file.getCanonicalPath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Could not read file: " + file.getName());
            return null;
        }
    }

    /**
     * Gets List of files inside a given tar file
     *
     * @param tar      tar file
     * @param filepath file path
     * @return list of files
     * @throws IOException throws IOException
     */
    public static ArrayList getTarFileList(File tar, String filepath) throws IOException {
        ArrayList<HashMap> list = new ArrayList<>();
        try (
                FileInputStream fin = new FileInputStream(tar);
                TarArchiveInputStream inputTar = new TarArchiveInputStream(fin)
        ) {
            getCompressFileList(list, inputTar, filepath);
        }

        return list;
    }

    /**
     * Gets List of files inside a given jar file
     *
     * @param jar      jar file
     * @param filepath file path
     * @return list of files
     * @throws IOException throws IOException
     */
    public static ArrayList getJarFileList(File jar, String filepath) throws IOException {
        ArrayList<HashMap> list = new ArrayList<>();
        try (
                FileInputStream fin = new FileInputStream(jar);
                JarArchiveInputStream inputJar = new JarArchiveInputStream(fin)
        ) {
            getCompressFileList(list, inputJar, filepath);
        }

        return list;
    }

    private static void getCompressFileList(ArrayList<HashMap> list, ArchiveInputStream intar, String filepath) throws IOException {
        ArchiveEntry entry;
        while ((entry = intar.getNextEntry()) != null) {
            if (!entry.getName().contains(filepath)) {
                continue;
            }
            HashMap<String, Object> hsf = new HashMap<>();
            hsf.put("name", entry.getName());
            hsf.put("length", entry.getSize());
            if (entry.isDirectory()) {
                hsf.put("directory", entry.isDirectory());
            }
            list.add(hsf);
        }
    }

    private static byte[] getTarFile(File tar, String filepath) throws IOException {
        try (
                TarArchiveInputStream inputTar = new TarArchiveInputStream(new FileInputStream(tar))
        ) {
            TarArchiveEntry entry;
            while ((entry = inputTar.getNextTarEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().startsWith(filepath)) {
                    continue;
                }
                byte[] ret = new byte[(int) entry.getSize()];
                if (inputTar.read(ret, 0, ret.length) == -1) {
                    logger.error("Failed to read file: " + tar.getName());
                }
                return ret;
            }
        }
        return null;
    }

    private static boolean outputTarFile(File tar, String tarFilePath, String outputPath) throws IOException {
        try (
                TarArchiveInputStream inputTar = new TarArchiveInputStream(new FileInputStream(tar))
        ) {
            extractCompressedFile(tarFilePath, inputTar, outputPath);
        }
        return false;
    }

    private static boolean saveDriver(String driver, String rpath) {
        if (Utils.isWindows() && System.getenv("HOME") != null) {
            logger.info(new Date() + " --> Saving driver: " + driver);
            File fcdriver = new File(System.getenv("HOME") + "/" + driver);
            File fdriver = new File(rpath + "/framework/selenium_drivers/" + driver);
            if (fdriver.exists() && (!fcdriver.exists() || fcdriver.length() != fdriver.length())) {
                try {
                    Files.copy(fdriver.toPath(), fcdriver.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.info(new Date() + " --> Saved driver to: " + fcdriver.getPath());
                } catch (IOException e) {
                    logger.warn("issue to save driver : " + e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Extract Resources from the given repoJar
     *
     * @param repoJar   repo Jar File
     * @param workspace workspace path
     * @param project   project name
     * @throws IOException throws IOException
     */
    public static void extractResources(File repoJar, String workspace, String project) throws IOException {
        if (resourcesExtracted) {
            return;
        }
        String resPath = "com/macys/sdt/framework/resources";
        logger.debug(resPath);
        extractJarFile(repoJar, resPath, workspace + "/" + resPath);

        resPath = "com/macys/sdt/shared/resources";
        logger.debug(resPath);
        extractJarFile(repoJar, resPath, workspace + "/" + resPath);
        saveDriver("chromedriver.exe", resPath);
        saveDriver("IEDriverServer.exe", resPath);

        resPath = "com/macys/sdt/projects/";
        logger.debug(resPath);
        extractJarFile(repoJar, resPath + project, workspace + "/" + project, "/resources", "/features", "pom.xml");
        resourcesExtracted = true;
    }

    protected static boolean extractJarFile(File ar, String tarFilePath, String outputPath, String... fileFilters) throws IOException {
        try (
                FileInputStream fin = new FileInputStream(ar);
                JarArchiveInputStream inputTar = new JarArchiveInputStream(fin)
        ) {
            extractCompressedFile(tarFilePath, inputTar, outputPath, fileFilters);
        }
        return false;
    }

    private static String getOutputPath(String tarPath, String outputPath, String path) {
        if (outputPath.isEmpty()) {
            return path;
        }
        return outputPath + "/" + path.replaceAll(tarPath, "");
    }

    private static boolean matchesFilter(String[] filters, String path) {
        if (filters.length == 0) {
            return true;
        }
        for (String filter : filters) {
            if (path.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    private static void extractCompressedFile(String tarFilePath, ArchiveInputStream inputTar, String outputPath, String... fileFilters) throws IOException {
        File outputFile = new File(outputPath);
        createDirectory(outputFile.getAbsoluteFile().getParentFile(), false);

        ArchiveEntry entry;
        while ((entry = inputTar.getNextEntry()) != null) {
            String path = entry.getName();
            if (!path.startsWith(tarFilePath)) {
                continue;
            }
            if (!entry.isDirectory() && !matchesFilter(fileFilters, path)) {
                continue;
            }

            if (entry.isDirectory()) {
                createDirectory(getOutputPath(tarFilePath, outputPath, path), false);
            } else {
                File fOut = new File(getOutputPath(tarFilePath, outputPath, path));
                long ts = System.currentTimeMillis();
                logger.debug("writing " + fOut.getCanonicalPath() + "...");
                if (fOut.exists()) {
                    if (!fOut.delete()) {
                        logger.debug("Unable to delete file: " + fOut.getName() + " before writing");
                        continue;
                    }
                }

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int length;
                while ((length = inputTar.read(buff)) > -1) {
                    bout.write(buff, 0, length);
                }
                logger.debug(System.currentTimeMillis() - ts + " : " + bout.size());
                File ftemp = new File(fOut.getParentFile().getPath() + "/" + System.currentTimeMillis());
                Utils.createDirectory(ftemp.getParent(), false);
                for (int i = 0; i < 100; i++) {
                    if (writeSmallBinaryFile(bout.toByteArray(), ftemp)) {
                        renameFile(ftemp, fOut, 10);
                        break;
                    }
                    logger.debug("--> retry extractCompressedFile:" + i);
                    threadSleep(3000, null);
                }
            }
        }
    }

    private static boolean renameFile(File src, File dest, int retryCount) {
        for (int i = 0; i < retryCount; i++) {
            if (src.renameTo(dest)) {
                return true;
            }
            threadSleep(3000, "--> file rename retry:" + src.getName() + ":" + dest.getName() + ":" + i);
        }

        if (isWindows()) {
            for (int i = 0; i < retryCount; i++) {
                String res = executeCMD("ren \"\"" + src.getPath() + "\"\" \"\"" + dest.getPath() + "\"\"");
                if (res != null && res.trim().isEmpty() && dest.exists()) {
                    logger.error("used CMD");
                    return true;
                }
                threadSleep(3000, "*" + src.getName() + ":" + dest.getName() + ":" + i);
            }
        }

        try {
            FileUtils.copyFile(src, dest);
            if (!src.delete()) {
                logger.error("Failed to delete file: " + src.getAbsolutePath());
            }
            if (dest.exists()) {
                logger.error("!");
                return true;
            }
        } catch (IOException e) {
            logger.warn("issue to rename file : " + e.getMessage());
        }
        return false;
    }

    private static boolean closeIoOutput(OutputStream st) {
        if (st == null) {
            return true;
        }
        try {
            st.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Object toObject(byte[] bytes) {
        try (
                ObjectInputStream oo = new ObjectInputStream(new ByteArrayInputStream(bytes))
        ) {
            return oo.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] toBytes(Object object) {
        try (
                ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream)
        ) {
            objectOutStream.writeObject(object);
            return byteOutStream.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean writeObject(Object obj, File f) {
        return writeSmallBinaryFile(toBytes(obj), f);
    }

    private static Object readObject(File f) {
        return toObject(readSmallBinaryFile(f));
    }

    private static String encodeURL(String url) {
        try {
            return new java.net.URI(null, url, null).toASCIIString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    private static StringBuilder readStringFromInputStream(BufferedReader is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = "";
        while (line != null) {
            line = is.readLine();
            if (line != null) {
                sb.append(line).append("\n");
            }
        }
        return sb;
    }

    public static String httpGet(String url, StringBuilder cookies) throws Exception {
        HttpClient client = new HttpClient();
        HttpConnectionManager manager = client.getHttpConnectionManager();
        manager.getParams().setConnectionTimeout(20 * 1000);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        GetMethod method = new GetMethod(encodeURL(url));
        try {
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            method.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
            if (cookies != null && !cookies.toString().isEmpty()) {
                method.addRequestHeader("Cookie", cookies.toString());
                method.addRequestHeader("Connection", "keep-alive");
                method.addRequestHeader("Cache-Control", "max-age=0");
                method.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            }

            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new Exception("Message code failed: " + method.getStatusLine());
            }
            InputStream in = method.getResponseBodyAsStream();
            StringBuilder responseBody = readStringFromInputStream(new BufferedReader(new InputStreamReader(in)));
            Object resCookie = method.getResponseHeader("Set-Cookie");
            if (cookies != null && resCookie != null) {
                String cookieValue = ((Header) resCookie).getValue();
                cookies.append(cookieValue).append(";");
            }
            return responseBody.toString();
        } finally {
            method.releaseConnection();
        }
    }

    private static void appendCookies(StringBuilder cookies, CloseableHttpResponse response) {
        Object resCookie = response.getHeaders("Set-Cookie");
        if (resCookie != null) {
            try {
                if (resCookie.getClass().toString().contains("[Lorg.apache.http.Header")) {
                    org.apache.http.Header[] headers = (org.apache.http.Header[]) resCookie;
                    if (headers.length > 0) {
                        String cookieValue = headers[0].toString().replaceAll("Set-Cookie: ", "");
                        cookies.append(cookieValue).append(";");
                    }
                }
            } catch (Exception e1) {
                logger.warn("issue to append cookies : " + e1.getMessage());
            }
        }
    }

    protected static int post(CloseableHttpClient client, String url, Map hparams, StringBuilder cookies, StringBuilder result) throws Exception {
        HttpPost post = new HttpPost(encodeURL(url));
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Connection", "keep-alive");
        if (!cookies.toString().isEmpty()) {
            post.setHeader("Cookie", cookies.toString());
        }

        Iterator en = hparams.keySet().iterator();
        List<NameValuePair> urlParameters = new ArrayList<>();
        while (en.hasNext()) {
            String key = en.next().toString();
            urlParameters.add(new BasicNameValuePair(key, hparams.get(key).toString()));
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        if (!url.endsWith("/j_acegi_security_check")) {
            logger.info("post():" + url + "\n-->Params: " + ((url.endsWith("/json") ? "json_data..." : urlParameters)));
        } else {
            logger.info("post():" + url);
        }
        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        if (200 > statusCode || statusCode >= 400) {
            logger.info("-->post().reponse: " + response);
            throw new Exception("Message code failed: " + response.getStatusLine());
        }

        appendCookies(cookies, (CloseableHttpResponse) response);

        result.append(readStringFromInputStream(new BufferedReader(new InputStreamReader(response.getEntity().getContent()))).toString());
        return statusCode;
    }

    /**
     * Convert Json Array to ArrayList
     *
     * @param json JSON Array
     * @return Array List
     */
    public static ArrayList<JSONObject> jsonArrayToList(JSONArray json) {
        ArrayList<JSONObject> items = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            try {
                items.add((JSONObject) json.get(i));
            } catch (JSONException e) {
                logger.error("Unable to convert JSONArray to List<JSONObject>: " + e.getMessage());
            }
        }
        return items;
    }


    /**
     * Gets the URL of execution engine
     *
     * @return String url pointing to EE
     */
    public static String getEEUrl() {
        String ee = "sdt.ee.fds.com";
        if (System.getenv("EE") != null) {
            ee = System.getenv("EE");
        } else if (System.getenv("meta_data") != null) {
            try {
                Map metaData = new Gson().fromJson(System.getenv("meta_data"), Map.class);
                ee = metaData.get("EE").toString();
            } catch (Exception ex) {
                logger.error("Unable to get EE URL : " + ex.getMessage());
            }
        }

        return "http://" + ee;
    }

    /**
     * Method to return SQL Queries
     *
     * @return SQL queries as json object
     */
    public static JSONObject getSqlQueries() {
        File queries = getResourceFile("queries.json");
        return getFileDataInJson(queries);
    }

    /**
     * Method to return file data as JSON object
     *
     * @param file file to extract data
     * @return file data as JSON object
     */
    public static JSONObject getFileDataInJson(File file) {
        JSONObject jsonObject = null;
        try {
            String jsonTxt = Utils.readTextFile(file);
            jsonObject = new JSONObject(jsonTxt);
        } catch (IOException | JSONException e) {
            logger.warn("issue to convert file data to json due to : " + e.getMessage());
        }
        return jsonObject;
    }

    /**
     * Get the return_order.json file data
     *
     * @param options order details which should match a record in return_order.json
     * @return matching object from return_order.json
     */
    public static JSONObject getVirtualReturns(HashMap<String, String> options) {
        try {
            JSONArray orders;
            File returnsFile = getResourceFile("return_orders.json");
            String jsonTxt = Utils.readTextFile(returnsFile);
            JSONObject json = new JSONObject(jsonTxt);
            if (macys()) {
                orders = (JSONArray) json.get("macys");
            } else {
                orders = (JSONArray) json.get("bloomingdales");
            }

            for (int i = 0; i < orders.length(); i++) {
                JSONObject order = orders.getJSONObject(i);
                boolean found = true;
                for (String key : options.keySet()) {
                    try {
                        if (!options.get(key).equals(order.get(key))) {
                            found = false;
                            break;
                        }
                    } catch (JSONException e) {
                        Assert.fail("Data not found in return_orders for key: " + key);
                    }
                }
                if (found) {
                    return order;
                }
            }
            return null;
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Get Order number of given orderType from order_mods_data.json
     *
     * @param orderType order Type
     * @return Order Number
     */
    public static String getOrderNumber(String orderType) {
        String order = null;
        try {
            File orderNum = getResourceFile("order_mods_data.json");
            String javaTxt = Utils.readTextFile(orderNum);
            JSONObject json = new JSONObject(javaTxt);
            if (macys()) {
                order = json.getJSONObject("macys").get(orderType).toString();
            } else {
                order = json.getJSONObject("bloomingdales").get(orderType).toString();
            }
        } catch (Exception e) {
            Assert.fail("Unable to load the file" + e);
        }
        return order;
    }

    /**
     * Method to return Decrypted Password for the given encrypted password
     *
     * @param password Encrypted password
     * @return Decrypted Password
     */
    public static String decryptPassword(String password) {
        String pWord = null;
        try {
            File passwordTxt = getResourceFile("password.json");
            String javaText = Utils.readTextFile(passwordTxt);
            JSONObject json = new JSONObject(javaText);
            pWord = json.getString(password);
        } catch (Exception e) {
            Assert.fail("Unable to find data in file" + e);
        }
        return pWord;
    }

    /**
     * Method to return all contextual media information
     *
     * @return Contextual Media information
     */
    public static JSONObject getContextualizeMedia() {

        File queries = getResourceFile("contextualize_media.json");
        JSONObject jsonObject = null;

        try {
            String jsonTxt = Utils.readTextFile(queries);
            jsonObject = new JSONObject(jsonTxt);
        } catch (IOException | JSONException e) {
            logger.warn("issue retrieving contextual data due to : " + e.getMessage());
        }

        return jsonObject;
    }

    public static String removeFromString(String s, String... args) {
        for (String arg : args) {
            s = s.replace(arg, "");
        }
        return s;
    }

    /**
     * Watchdog for Threads, to monitor it for timeouts
     */
    public static class ThreadWatchDog extends Thread {
        private Thread m_thread;
        private long m_timeout;
        private String m_name;
        private Runnable m_callback;

        /**
         * Creates a watchdog for a Thread to monitor it for timeouts
         *
         * @param th       th Thread
         * @param timeout  timeout in milliseconds
         * @param name     Thread name
         * @param callback Runnable
         */
        public ThreadWatchDog(Thread th, long timeout, String name, Runnable callback) {
            this.m_thread = th;
            this.m_timeout = timeout;
            this.m_name = name + System.currentTimeMillis();
            this.m_callback = callback;
            this.start();
        }

        /**
         * Interrupt the monitored Thread if it is still running
         */
        public void run() {
            if (Utils.threadSleep(this.m_timeout, "--> ThreadWatchDog.start():" + this.m_name + ":" + this.m_timeout)) {
                logger.error("--> ThreadWatchDog.start(): " + this.m_name + " : " + this.m_timeout + " : exit normally.");
                return;
            }
            if (this.m_thread != null && this.m_thread.isAlive()) {
                logger.error("--> ThreadWatchDog.destroy(): " + this.m_name + " : " + this.m_timeout);
                this.m_thread.interrupt();
            }
            if (this.m_callback != null) {
                m_callback.run();
            }
        }
    }

    protected static class ReadStream extends Thread {
        StringBuilder console = new StringBuilder();
        String name;
        InputStream is;

        public ReadStream(String name, InputStream is) {
            this.name = name;
            this.is = is;
            this.start();
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                while (true) {
                    String s = br.readLine();
                    if (s == null) {
                        break;
                    }
                    if (System.getenv("DEBUG") != null) {
                        logger.info(s);
                    }
                    console.append(s).append("\n");
                }
                is.close();
            } catch (Exception ex) {
                logger.warn("Problem reading stream " + name + "... :" + ex.getMessage());
            }
        }

        public StringBuilder getConsole() {
            return this.console;
        }
    }

    protected abstract static class UtilsComparator implements Comparator {
        private Object[] m_params;

        public UtilsComparator(Object[] params) {
            this.m_params = params;
        }

        @Override
        public abstract int compare(Object o1, Object o2);

    }

    public static class ProcessWatchDog extends Thread {
        private Process m_process;
        private long m_timeout;
        private String m_name;

        /**
         * Creates a watchdog for a process to monitor it for timeouts
         *
         * @param p       process to monitor
         * @param timeout timeout in milliseconds
         * @param name    name of the process
         */
        public ProcessWatchDog(Process p, long timeout, String name) {
            this.m_process = p;
            this.m_timeout = timeout;
            this.m_name = name + System.currentTimeMillis();
            this.start();
        }

        /**
         * Kills the monitored process if it is still running
         */
        public void run() {
            Utils.threadSleep(this.m_timeout, null);
            if (this.m_process.isAlive()) {
                logger.info("--> ProcessWatchDog.destroyForcibly():" + this.m_name + ":" + this.m_timeout);
                this.m_process.destroyForcibly();
            }
        }
    }
}
