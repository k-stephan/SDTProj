package com.macys.sdt.framework.runner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.analytics.Analytics;
import com.macys.sdt.framework.utils.analytics.DigitalAnalytics;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class holds all run config data statically
 */
public class RunConfig {

    private static final Logger logger = LoggerFactory.getLogger(RunConfig.class);

    /**
     * True if executing through sauce labs. Checks for valid sauce labs info in "sauce_user" and "sauce_key" env variables
     */
    public static boolean useSauceLabs;
    /**
     * Name of Sauce Connect tunnel to use
     */
    public static String tunnelIdentifier = getEnvOrExParam("tunnel_identifier");

    /**
     * True if executing through TestObject. Checks for valid TestObject info in "testobject_api_key" and "testobject_device" env variables
     */
    public static boolean useTestObject;
    /**
     * True if using chrome device emulation
     */
    public static boolean useChromeEmulation;
    /**
     * True if using appium to connect to a mobile device
     */
    public static boolean useAppium = booleanParam("use_appium");
    /**
     * To set desired / compatible appium version for running appium tests
     */
    public static String appiumVersion = getEnvOrExParam("appium_version");
    /**
     * True if testing a mobile application
     */
    public static boolean appTest;
    /**
     * True if using header file
     */
    public static boolean useHeaders = booleanParam("use_headers");
    /**
     * Map of all headers and their values
     */
    public static HashMap<String, String> headers = new HashMap<>();
    /**
     * Contains OS to use when executing on sauce labs or os version for app as given in "remote_os" env variable
     * <p>
     * Options: windows 7|8|8.1|10, OSX 10.10|10.11,
     * for iOS and android, use the version of OS you want to run against - 9.2, 9.3 for iOS, 5.1, 6.0 for Android.
     * You can use any version number that you want as long as you have an emulator or device with that OS version.
     * </p>
     */
    public static String remoteOS = getEnvOrExParam("remote_os");
    /**
     * Browser to use as given in "browser" env variable. Default chrome.
     */
    public static String browser = getEnvVar("browser");
    /**
     * Version of browser to use as given in "browser_version" env variable
     */
    public static String browserVersion = getEnvOrExParam("browser_version");
    /**
     * Wait timeout as given in "timeout" env variable. Default 95 seconds (125 seconds for safari)
     */
    public static int timeout;
    /**
     * Device in use as given by "device" env variable
     */
    public static String device = getEnvOrExParam("device");
    /**
     * Whether the proxy is disabled
     */
    public static boolean useProxy = false;
    /**
     * The Sauce Labs username to use
     */
    public static String sauceUser = getEnvOrExParam("sauce_user");
    /**
     * The Sauce Labs API key for the user
     */
    public static String sauceKey = getEnvOrExParam("sauce_key");

    /**
     * The TestObject API key for the device
     */
    public static String testobjectAPIKey = getEnvOrExParam("testobject_api_key");

    /**
     * The TestObject Device
     */
    public static String testobjectDevice = getEnvOrExParam("testobject_device");

    /**
     * Workspace path as given in "WORKSPACE" env variable
     */
    public static String workspace = getEnvOrExParam("WORKSPACE");
    /**
     * Path to logging folder
     */
    public static String logs;
    /**
     * Path to "temp" directory
     */
    public static String temp;
    /**
     * Map of current cucumber features
     */
    public static HashMap<String, Map> features = new HashMap<>();
    /**
     * Path to feature file to execute from
     */
    public static String scenarios = getEnvVar("scenarios");
    /**
     * Analytics object
     */
    public static Analytics analytics;
    /**
     * Whether to close browser or not after testing is complete. False if "DEBUG" env variable is true
     */
    public static Boolean closeBrowserAtExit = true;
    /**
     * Whether to collect coremetrics tags or not as given in "tag_collection" env variable
     */
    public static Boolean tagCollection = false;
    /**
     * Whether to run on QA env in batch mode as given in "batch_mode" env variable
     */
    public static Boolean batchMode = booleanParam("batch_mode");
    /**
     * URL to start at and use as a base as given in "website" env variable
     */
    public static String url = getEnvVar("website");
    /**
     * Domain - MCOM or BCOM, only needed when resolving website with IP
     */
    public static String brand = getEnvOrExParam("brand");
    /**
     * Path to project currently being run optionally given by "sdt_project" env variable
     */
    public static String project;
    /**
     * Path to active project files on file system
     */
    public static List<String> projectResourceDirs = new ArrayList<>();
    /**
     * Whether we're running in debug mode
     */
    public static boolean debugMode = booleanParam("debug");
    /**
     * True if current run is a dry run
     */
    public static boolean dryRun = false;
    /**
     * Name of jar being run if running from jar
     */
    public static String repoJar = getEnvOrExParam("repo_jar");
    /**
     * Path to shared resources
     */
    public static String sharedResourceDir = repoJar != null ?
            "com/macys/sdt/shared/resources" : "shared/resources/src/main/resources";
    /**
     * List of all projects who's steps this test relies on
     */
    public static List<String> includedProjects;
    /**
     * Location of app for app testing (appium)
     */
    protected static String appLocation = getEnvOrExParam("app_location");

    // don't allow objects of this type to be initialized, static access only
    private RunConfig() {
    }

    /**
     * Retrieves project info either from "sdt_project" or "scenarios" env val if possible
     */
    static void getProject() {
        project = getEnvVar("sdt_project");
        if (project == null) {
            project = getProjectFromFilePath();
        }

        String[] check = project.split("\\.");
        if (check.length != 2) {
            Assert.fail("Project info is malformed. Please make sure it is in the format \"<domain>.<project>\"");
        }

        logger.info("Using project: " + project + "\nIf this does not match your project," +
                " add an env variable \"sdt_project\" with value \"<domain>.<project>\"");
        try {
            if (repoJar != null) {
                Utils.extractResources(new File(repoJar), workspace, project.replace(".", "/"));
            }
        } catch (IOException e) {
            logger.error("Failed to extract resources from jar");
        }
        includedProjects = getDependencies(project);
        // old, proprietary resource location
        projectResourceDirs.add(getProjectResourceDir(project));
        for (String prj : includedProjects) {
            projectResourceDirs.add(getProjectResourceDir(prj));
        }
    }

    /**
     * Gets the resources directory for the given project
     * <p>
     * Can use the following dirs:<br>
     * <code>Maven standard: <br>
     * [domain]/[project]/src/main/resources</code><br>
     * <code>SDT Proprietary: <br>
     * [domain]/[project]/src/main/java/com/macys/sdt/projects/[domain]/[project]/resources</code><br>
     * <code>EE resource dir: <br>
     * [domain]/[project]/resources</code><br>
     * </p>
     */
    private static String getProjectResourceDir(String prj) {
        String prjResDir = prj.replace(".", "/") + "/src/main/java/com/macys/sdt/projects/" + prj.replace(".", "/") + "/resources/";
        if (!new File(prjResDir).exists()) {
            // maven standard resource location
            prjResDir = prj.replace(".", "/") + "/src/main/resources";
            if (!new File(prjResDir).exists()) {
                // location for EE runs
                prjResDir = prj.replace(".", "/") + "/resources";
            }
        }

        return prjResDir;
    }

    private static String getProjectFromFilePath() {
        String project;
        String projectPath;
        if (!workspace.equals(".")) {
            projectPath = scenarios.replace(workspace, "").replace("/", ".").replace("\\", ".");
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(projectPath.split("\\.")));
            if (parts.size() >= 2) {
                project = parts.get(0) + "." + parts.get(1);
            } else {
                project = "";
            }
        } else {
            projectPath = scenarios.replace("/", ".").replace("\\", ".");
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(projectPath.split("\\.")));
            int index = parts.lastIndexOf("features");
            if (index == -1) {
                index = parts.indexOf("SDT");
                if (index < 2) {
                    Assert.fail("Unable to determine project by given environment variables. Please" +
                            "add an environment variable \"sdt_project\" " +
                            "with project name in format \"<domain>.<project>\"");
                }
                project = parts.get(index + 1) + "." + parts.get(index + 2);  // domain.project
            } else {
                project = parts.get(index - 2) + "." + parts.get(index - 1);  // domain.project
            }
        }
        return project;
    }

    /**
     * Retrieves a parameter value from "ex_params" environment variable
     *
     * @param name name of the parameter to retrieve
     * @return value of parameter or null if not found
     */
    public static String getExParam(String name) {
        try {
            String exParams = URLDecoder.decode(System.getenv("ex_params"), "utf-8");
            if (exParams != null && !exParams.isEmpty()) {
                StringBuilder sb = new StringBuilder(exParams);
                for (int i = 0, quoteIndex = -1; i < sb.length(); i++) {
                    char c = sb.charAt(i);
                    if (c == '\"' && quoteIndex == -1) {
                        quoteIndex = i;
                    }
                    if (quoteIndex > -1) {
                        for (i = i + 1; i < sb.length(); i++) {
                            c = sb.charAt(i);
                            if (c == '\"') {
                                quoteIndex = -1;
                                break;
                            }
                            if (c == ' ') {
                                sb.setCharAt(i, '|');
                            }
                        }
                    }
                }
                exParams = sb.toString();
                String[] paramList = exParams.split(" ");
                for (String param : paramList) {
                    if (param.startsWith(name)) {
                        return param.split("=")[1].trim().replace('|', ' ').replace("\"", "");
                    }
                }
            }
        } catch (Exception ex) {
            // variable not found or malformed
        }
        return null;
    }

    /**
     * Retrieves an environment variable OR ex_param
     *
     * @param name name of parameter to retrieve
     * @return value of parameter or null if not found
     */
    public static String getEnvOrExParam(String name) {
        String val = getEnvVar(name);
        if (val == null) {
            val = getExParam(name);
        }
        if (val == null) {
            name = name.toLowerCase();
            val = getEnvVar(name);
        }
        return val != null ? val : getExParam(name);
    }

    /**
     * Retrieves an environment variable
     *
     * @param name environment variable name to retrieve its value
     * @return value of parameter or null if not found
     */
    public static String getEnvVar(String name) {
        String value = System.getenv(name);
        value = value == null ? null : value.trim();
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return null;
    }

    /**
     * Matches an ex_param against t|true and converts it to a boolean
     *
     * @param name name of parameter
     * @return true if parameter exists and matches "t|true"
     */
    public static boolean booleanParam(String name) {
        String param = getEnvOrExParam(name);
        return param != null && param.matches("t|true");
    }

    /**
     * Gets a list of all scenarios to be run
     *
     * @return the list of scenarios
     */
    public static ArrayList<String> getFeatureScenarios() {
        ArrayList<String> scenarioList = new ArrayList<>();
        if (scenarios == null) {
            return scenarioList;
        }
        scenarios = scenarios.trim();
        if (!scenarios.contains(project.replace('.', '/'))) {
            scenarios = scenarios.replaceAll("features/", project.replace(".", "/") + "/features/");
        }
        logger.info("Parsing env scenarios : " + scenarios);
        String delimit = ".feature";
        int i = 0, end = scenarios.indexOf(delimit);
        while (i < scenarios.length()) {
            end = scenarios.indexOf(' ', end + 1);
            if (end == -1) {
                end = scenarios.length();
            }
            String scenarioPath = scenarios.substring(i, end).trim();
            logger.info("scenario path : " + scenarioPath);
            scenarioList.add(scenarioPath);
            i = end;
        }

        Collections.sort(scenarioList);
        ArrayList<Map> featureScenarios = null;
        if (workspace == null) {
            workspace = "";
        }
        for (String featureFilePath : scenarioList) {
            String[] featureInfo = featureFilePath.split(".feature:");
            String path = featureInfo[0];
            if (!path.endsWith(".feature")) {
                path += ".feature";
            }
            int line = 0;
            if (featureInfo.length == 2) {
                line = Utils.parseInt(featureInfo[1], 0);
            }
            if (!path.equals("")) {
                File featureFile = new File(path);
                if (!(featureFile.exists() || featureFile.getAbsoluteFile().exists())) {
                    logger.info("File not found: " + path);
                    path = workspace + "/" + path;
                }
                String json = Utils.gherkinToJson(false, path);
                try {
                    featureScenarios = new Gson().fromJson(json, new TypeToken<ArrayList<Map>>() {
                    }.getType());
                } catch (JsonSyntaxException jex) {
                    logger.error("Failed to parse : " + path);
                    logger.error("json :\n\n" + json);
                    throw jex;
                }
            }
            findScenario(featureScenarios, path, line);
        }

        // condense any duplicate feature files
        HashMap<String, ArrayList<String>> featureLines = new HashMap<>();
        // remove windows drive to avoid incorrect matches on ":"
        final String drive = scenarioList.get(0).matches("[A-Z]:.*?") ? scenarioList.get(0).substring(0, 2) : null;
        if (drive != null) {
            for (i = 0; i < scenarioList.size(); i++) {
                String scenario = scenarioList.remove(i);
                scenarioList.add(i, scenario.substring(2));
            }
        }

        for (String scenario : scenarioList) {
            int lineIndex = scenario.lastIndexOf(':');
            if (lineIndex == -1) {
                continue;
            }
            String scenarioPath = scenario.substring(0, lineIndex).trim();
            String lineNum = scenario.substring(lineIndex + 1);
            featureLines.computeIfAbsent(scenarioPath, (key) -> new ArrayList<>());
            ArrayList<String> lines = featureLines.get(scenarioPath);
            lines.add(lineNum);
        }

        scenarioList.removeAll(scenarioList.stream()
                .filter(str -> str.contains(":"))
                .collect(Collectors.toList()));

        scenarioList.addAll(featureLines.keySet().stream()
                .map((key) -> key + ":" + StringUtils.join(featureLines.get(key), ":"))
                .collect(Collectors.toList()));

        if (drive != null) {
            for (i = 0; i < scenarioList.size(); i++) {
                String scenario = scenarioList.remove(i);
                scenarioList.add(i, drive + scenario);
            }
        }

        return scenarioList;
    }

    private static void findScenario(ArrayList<Map> featureScenarios, String scenarioPath, int line) {
        HashMap<Integer, Map> hScenario = new HashMap<>();
        for (Map scenario : featureScenarios) {
            ArrayList<Map> elements = (ArrayList<Map>) scenario.get("elements");
            for (Map element : elements) {
                element.put("uri", scenario.get("uri"));
                int l = Utils.parseInt(element.get("line"), 0);
                if (line == 0 || line == l) {
                    features.put(scenarioPath + ":" + l, element);
                    if (line == 0) {
                        continue;
                    }
                }
                hScenario.put(l, element);
            }
        }
        int closest = 0;
        for (Integer l : hScenario.keySet()) {
            int dist = Math.abs(line - l);
            if (dist < line - closest) {
                closest = l;
            }
        }
        if (closest > 0) {
            features.put(scenarioPath + ":" + line, hScenario.get(closest));
            logger.info("Load closest scenario with line: " + closest);
        }
    }

    public static List<String> getDependencies(String project) {
        ArrayList<String> deps = new ArrayList<>();
        String pom = workspace + project.replace(".", "/") + "/pom.xml";
        try {
            Document doc = Jsoup.parse(Utils.readTextFile(new File(pom)), "", Parser.xmlParser());
            for (Element e : doc.select("dependencies dependency artifactid")) {
                if (e.html().startsWith("sdt-")) {
                    String[] name = e.html().split("-");
                    deps.add(name[1] + "." + name[2]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read pom file: " + e);
        }
        return deps;
    }

    private static void getHeaders() {
        if (!useHeaders) {
            return;
        }
        try {
            File headerFile = Utils.getResourceFile("headers.json");
            if (!headerFile.exists()) {
                return;
            }
            useProxy = true;
            JSONObject headerJSON = new JSONObject(Utils.readTextFile(headerFile));
            for (String key : headerJSON.keySet()) {
                Object o = headerJSON.get(key);
                if (o instanceof String) {
                    headers.put(key, (String) headerJSON.get(key));
                } else {
                    logger.warn("Bad header: " + key);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read header file");
        }
    }

    /**
     * get and set environment variables
     */
    static void getEnvVars(String[] args) {
        if (workspace == null) {
            workspace = System.getProperty("user.dir");
        }
        workspace = workspace.replace('\\', '/');
        workspace = workspace.endsWith("/") ? workspace : workspace + "/";

        if (debugMode) {
            ((ConsoleAppender) org.apache.log4j.Logger.getRootLogger().getAppender("STDOUT")).setThreshold(Level.DEBUG);
            ((FileAppender) org.apache.log4j.Logger.getRootLogger().getAppender("FILE")).setThreshold(Level.TRACE);
        }

        if (scenarios == null) {
            scenarios = "";
        }

        // get cucumber scenarios from args if not in env - cucumber config does this in intellij
        if (scenarios.isEmpty() && args != null && args.length > 0) {
            StringBuilder temp = new StringBuilder("");
            for (String arg : args) {
                File f = new File(arg);
                if (f.exists() || f.getAbsoluteFile().exists()) {
                    temp.append(arg);
                }
            }
            scenarios = temp.toString();
        }
        scenarios = scenarios.replace('\\', '/');

        if (!url.matches("^https?://.*")) {
            url = "http://" + url;
        }

        if (repoJar != null) {
            if (!(new File(repoJar).exists())) {
                if (!(new File(workspace + repoJar).exists())) {
                    logger.warn("Could not find given repo jar. Attempting to run without.");
                    repoJar = null;
                } else {
                    repoJar = workspace + repoJar;
                }
            }
        }
        Utils.createDirectory(logs = workspace + "logs/", true);
        Utils.createDirectory(temp = workspace + "temp/", true);

        if (remoteOS == null) {
            logger.debug("Remote OS not specified. Using default (Windows 7)");
            remoteOS = "Windows 7";
        }

        String analyticsClass = getEnvOrExParam("analytics");
        if (analyticsClass != null) {
            useProxy = true;
            if (analyticsClass.equals("da")) {
                analytics = new DigitalAnalytics();
            }
            if (analytics != null) {
                logger.info("Including Analytics: " + analytics.getClass().getSimpleName());
            }
        }

        // tag_collection
        tagCollection = booleanParam("tag_collection");
        if (tagCollection) {
            logger.info("tag_collection is enabled");
        }

        // batch mode run on QA environment. Batch mode causes all products to be available
        if (batchMode) {
            logger.info("batch_mode is enabled");
        }

        // use saucelabs when valid "sauce_user" and "sauce_key" is provided
        useSauceLabs = sauceUser != null && sauceKey != null;

        // use testobject when valid "testobject_api_key" and "testobject_device" is provided
        useTestObject = testobjectAPIKey != null && testobjectDevice != null;

        // use chrome emulation when it is mobile device and use of Appium is not mentioned
        useChromeEmulation = StepUtils.mobileDevice() && !useAppium;

        // Test is appTest when use of Appium is mentioned and app_location is given
        appTest = useAppium && (appLocation != null);

        // close browser at exist unless debugMode is on or test is appTest
        closeBrowserAtExit = !(debugMode || appTest);

        if (url == null && !appTest) {
            Assert.fail("\"website\" variable required to test a website");
        }

        // having a slash on the end messes up relative navigation & cookie domain
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (browser == null && !appTest) {
            logger.info("No browser given, using default (chrome)");
            browser = "chrome";
        }

        if (browserVersion == null) {
            browserVersion = WebDriverConfigurator.defaultBrowserVersion();
            logger.info("No Browser Version given, using default : " + browserVersion);
        }

        // close the test browser at scenario exit
        String envVal = getEnvOrExParam("timeout");
        if (envVal != null) {
            timeout = Integer.parseInt(envVal);
        } else {
            timeout = StepUtils.safari() ? 130 : 95;
        }

        // get project from environment variables
        getProject();

        // check for headers file
        // needs to be after project is set in order to check project resources
        getHeaders();
    }
}
