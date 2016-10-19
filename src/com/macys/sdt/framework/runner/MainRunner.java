package com.macys.sdt.framework.runner;

import com.google.gson.Gson;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.analytics.Analytics;
import com.macys.sdt.framework.utils.analytics.DATagCollector;
import com.macys.sdt.framework.utils.analytics.DigitalAnalytics;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import net.lightbody.bmp.BrowserMobProxy;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.macys.sdt.framework.utils.StepUtils.*;
import static java.lang.Runtime.getRuntime;

/**
 * This class handles the configuration and running of cucumber scenarios and features
 */
public class MainRunner {
    /**
     * BrowserMob proxy server
     */
    public static BrowserMobProxy browsermobServer = null;
    /**
     * Will be true if executing through sauce labs. Checks for valid sauce labs info in "sauce_user" and "sauce_key" env variables
     */
    public static boolean useSaucelabs;
    /**
     * Name of Sauce Connect tunnel to use
     */
    public static String tunnelIdentifier = getExParams("tunnel_identifier");
    /**
     * Will be true if using chrome device emulation
     */
    public static boolean useChromeEmulation;
    /**
     * Whether or not to use appium as set in "use_appium" env variable
     */
    public static boolean useAppium = booleanExParam("use_appium");

    public static boolean appTest;
    /**
     * Contains OS to use when executing on sauce labs or os version for app as given in "remote_os" env variable
     * <p>
     * Options: windows 7|8|8.1|10, OSX 10.10|10.11,
     * for iOS and android, use the version of OS you want to run against - 9.2, 9.3 for iOS, 5.1, 6.0 for Android.
     * You can use any version number that you want as long as you have an emulator or device with that OS version.
     * </p>
     */
    public static String remoteOS;
    /**
     * Workspace path as given in "WORKSPACE" env variable
     */
    public static String workspace;
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
    public static String scenarios = getExParams("scenarios");
    /**
     * Browser to use as given in "browser" env variable. Default firefox.
     */
    public static String browser = "firefox";
    /**
     * Version of browser to use as given in "browser_version" env variable
     */
    public static String browserVersion = null;
    /**
     * Analytics object
     */
    public static Analytics analytics;
    /**
     * Whether to close browser after testing is complete. False if "DEBUG" env variable is present
     */
    public static Boolean closeBrowserAtExit = true;
    /**
     * Whether to collect coremetrics tags or not as given in "tag_collection" env variable
     */
    public static Boolean tagCollection = false;
    /**
     * Whether to run on QA env in batch hmode in "batch_mode" env variable
     */
    public static Boolean batchMode = false;
    /**
     * URL to start at and use as a base as given in "website" env variable
     */
    public static String url = "http://www.macys.com";
    /**
     * Domain - MCOM or BCOM, only needed when resolving website with IP
     */
    public static String brand;
    /**
     * Time the tests were started
     */
    public static long startTime = System.currentTimeMillis();
    /**
     * Current run status. 0 is good, anything else is bad
     */
    public static int runStatus = 0;
    /**
     * Wait timeout as given in "timeout" env variable. Default 90 seconds (120 seconds for safari)
     */
    public static int timeout;
    /**
     * Device in use as given by "device" env variable
     */
    public static String device = getExParams("device");
    /**
     * List containing URL's that have been visited
     */
    public static ArrayList<String> URLStack = new ArrayList<>();
    /**
     * Path to project currently being run
     */
    public static String project = null;

    public static String projectDir = null;
    /**
     * The current URL
     */
    public static String currentURL = "";
    /**
     * Whether the proxy is disabled
     */
    public static boolean disableProxy = true;
    /**
     * Whether we're running in debug mode
     */
    public static boolean debugMode = booleanExParam("debug");
    /**
     * The Sauce Labs username to use
     */
    public static String sauceUser;
    /**
     * The Sauce Labs API key for the user
     */
    public static String sauceKey;

    public static String browsermobServerHarTs = System.currentTimeMillis() + "";

    private static WebDriver driver = null;
    private static long ieAuthenticationTs = System.currentTimeMillis() - 10000; // set authentication checking interval out of range
    //satish-macys:4fc927f7-c0bd-4f1d-859b-ed3aea2bcc40

    /**
     * Main method to run tests
     *
     * @param argv run args. Ignored, use environment variables for all config
     * @throws Throwable if an exception or error gets here, we're done
     */
    public static void main(String[] argv) throws Throwable {
        getEnvVars();

        ArrayList<String> featureScenarios = getFeatureScenarios();
        if (featureScenarios == null) {
            throw new Exception("Error getting scenarios");
        }

        // add any tags
        String tags = getExParams("tags");
        if (tags != null) {
            tags = tags.trim();
            if (!tags.isEmpty()) {
                featureScenarios.add("--tags");
                featureScenarios.add(tags);
            }
        }

        if (project == null) {
            String projectPath = featureScenarios.get(0).replace("/", ".").replace("\\", ".");
            String[] parts = projectPath.split(Pattern.quote("."));
            int sdtIndex = 0;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("SDT")) {
                    sdtIndex = i;
                    break;
                }
            }
            if (sdtIndex != parts.length) {
                project = parts[sdtIndex + 1] + "." +    // domain
                        parts[sdtIndex + 2];             // project
            }
        }
        if (project != null) {
            System.out.println("-->Current project: " + project);
        }
        System.out.println("-->Running with parameters:\n" + featureScenarios);

        // attempt to use workspace as relative path to feature file (if needed)
        if (workspace != null && !workspace.isEmpty()) {
            for (int i = 0; i < featureScenarios.size(); i++) {
                String value = featureScenarios.get(i);
                if (value.equals("--tags")) {
                    break;
                }
                // remove windows drive to avoid incorrect matches on ":"
                String drive = "";
                if (value.matches("[A-Z]:.*?")) {
                    drive = value.substring(0, 2);
                    value = value.substring(2);
                }
                // remove any line number args
                value = value.split(":")[0];
                value = drive + value;
                // make sure file exists
                File featureFile = new File(value);
                if (!(featureFile.exists() || featureFile.getAbsoluteFile().exists())) {
                    featureScenarios.set(i, workspace + "/" + featureScenarios.get(i));
                }
            }
        }

        if (project != null) {
            featureScenarios.add("--glue");
            featureScenarios.add("com.macys.sdt.projects." + project);
            projectDir = project.replace(".", "/") + "/src/main/java/com/macys/sdt/projects/" + project;
        }

        featureScenarios.add("--glue");
        featureScenarios.add("com.macys.sdt.shared");
        featureScenarios.add("--plugin");
        featureScenarios.add("com.macys.sdt.framework.utils.SDTFormatter");
        featureScenarios.add("--plugin");
        featureScenarios.add("html:logs");

        System.out.println("-->Testing " + url + " using " +
                (useAppium ? device + " running " + (StepUtils.iOS() ? "iOS " : "Android ") + remoteOS : browser + " " + browserVersion)
                + (useSaucelabs ? " on Sauce Labs" : ""));

        driver = getWebDriver();

        if (!appTest) {
            PageHangWatchDog.init();
        }

        new AuthenticationDialog();

        try {
            runStatus = cucumber.api.cli.Main.run(featureScenarios.toArray(new String[featureScenarios.size()]),
                    Thread.currentThread().getContextClassLoader());
        } catch (Throwable e) {
            e.printStackTrace();
            runStatus = 1;
        } finally {
            close();
            if (argv != null) {
                System.exit(runStatus);
            }
        }
    }

    private static void getEnvVars() {
        workspace = getExParams("WORKSPACE");
        if (workspace == null) {
            workspace = ".";
        }
        workspace = workspace.replace('\\', '/');
        workspace = workspace.endsWith("/") ? workspace : workspace + "/";
        Utils.createDirectory(logs = workspace + "logs/", true);
        Utils.createDirectory(temp = workspace + "temp/", true);

        url = getExParams("website");
        brand = getExParams("brand");
        remoteOS = getExParams("remote_os");
        if (remoteOS == null) {
            System.out.println("Remote OS not specified.  Using default: Windows 7");
            remoteOS = "Windows 7";
        }
        browser = getExParams("browser") != null ? getExParams("browser") : browser;
        browserVersion = getExParams("browser_version") != null ? getExParams("browser_version") : WebDriverConfigurator.defaultBrowserVersion();
        timeout = StepUtils.safari() ? 120 : 90;

        String analyticsClass = getExParams("analytics");
        if (analyticsClass != null) {
            disableProxy = false;
            if (analyticsClass.equals("da")) {
                analytics = new DigitalAnalytics();
            }
            if (analytics != null) {
                System.out.print(" including Analytics:" + analytics.getClass().getSimpleName());
            }
        }

        System.out.println("\n\n");
        closeBrowserAtExit = !debugMode && !appTest;

        // tag_collection
        tagCollection = booleanExParam("tag_collection");
        if (tagCollection) {
            System.out.println("tag_collection is enabled");
        }

        // batch mode run on QA environment. once QA env has batch_mode, it will enable all products available
        batchMode = booleanExParam("batch_mode");
        if (batchMode) {
            System.out.println("batch_mode is enabled");
        }

        // use sauce labs
        if (getExParams("saucelabs") != null) {
            Assert.fail("This parameter is deprecated, please use sauce_user and sauce_key instead");
        }
        sauceUser = getExParams("sauce_user");
        sauceKey = getExParams("sauce_key");
        useSaucelabs = sauceUser != null && sauceKey != null;

        useChromeEmulation = StepUtils.mobileDevice() && !useAppium;

        String appLoc = getExParams("app_location");
        appTest = useAppium && (appLoc != null && !appLoc.isEmpty());

        // close the test browser at scenario exit
        String envVal = getExParams("timeout");
        if (envVal != null) {
            timeout = Integer.parseInt(envVal);
        }

        // set a project
        envVal = getExParams("project");
        if (envVal != null) {
            project = envVal;
        }
    }

    /**
     * Resets the driver
     *
     * @param quit whether to close the driver
     */
    public static void resetDriver(boolean quit) {
        if (quit) {
            if (appTest) {
                driver.quit();
            } else {
                driver.close();
                driver.quit();
                if (ie()) { // workaround for IE browser closing
                    driver.quit();
                }
            }
        }
        driver = null;
    }

    /**
     * Checks if the web driver exists
     *
     * @return true if a valid web driver is active
     */
    public static Boolean driverInitialized() {
        return driver != null;
    }

    /**
     * Gets the active Appium driver if the driver is an appium driver, otherwise null
     * <p>
     * The Appium driver can be used for most app specific interactions.
     * For iOS/Android specific actions, use getIOSDriver and getAndroidDriver.
     * </p>
     *
     * @return the active Appium driver
     */
    public static AppiumDriver getAppiumDriver() {
        return driver instanceof AppiumDriver ? (AppiumDriver) driver : null;
    }

    /**
     * Gets the active iOS driver if the driver is an iOS driver, otherwise null
     *
     * @return the active iOS driver
     */
    public static IOSDriver getIOSDriver() {
        return driver instanceof IOSDriver ? (IOSDriver) driver : null;
    }

    /**
     * Gets the active android driver if the driver is an android driver, otherwise null
     *
     * @return the active Android driver
     */
    public static AndroidDriver getAndroidDriver() {
        return driver instanceof AndroidDriver ? (AndroidDriver) driver : null;
    }

    /**
     * Gets the current webDriver instance or tries to create one
     *
     * @return current webDriver instance
     */
    public static synchronized WebDriver getWebDriver() {
        if (URLStack.size() == 0) {
            URLStack.add(url);
        }

        if (driver != null) {
            if (!URLStack.get(URLStack.size() - 1).equals(currentURL)) {
                URLStack.add(currentURL);
            }
            return driver;
        }

        for (int i = 0; i < 2; i++) {
            if (disableProxy) {
                // System.out.println("DEBUG stack trace: " +
                //        Utils.listToString(Utils.getCallFromFunction("getWebDriver"), "\n\t ", null));
                driver = WebDriverConfigurator.initDriver(null);
            } else {
                driver = WebDriverConfigurator.initDriverWithProxy();
            }

            try {
                if (!useAppium) {
                    if (browser.equals("safari")) {
                        Dimension dimension = new Dimension(1280, 1024);
                        driver.manage().window().setSize(dimension);
                    } else {
                        driver.manage().window().maximize();
                    }
                    String windowSize = driver.manage().window().getSize().toString();
                    System.out.println("Init driver: browser window size = " + windowSize);
                }
                return driver;
            } catch (Exception ex) {
                System.err.println("-->Failed initialized driver:retry" + i + ":" + ex.getMessage());
                Utils.threadSleep(2000, null);
            }
        }
        System.err.println("Cannot initialize driver: exiting test...");

        System.out.println("Quit the driver " + driver);
        if (driver != null) {
            driverQuit();
        }
        System.exit(-1);
        // return is unreachable but IDE doesn't realize, return non-null
        // to get rid of invalid lint errors
        return new ChromeDriver();
    }

    /**
     * Retrieves an environment variable OR ex_param
     *
     * @param name name of parameter to retrieve
     * @return value of parameter or null if not found
     */
    public static String getExParams(String name) {
        String value = System.getenv(name);
        if (value != null) {
            return value.trim();
        }
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
     * Matches an ex_param against t|true and converts it to a boolean
     *
     * @param name name of parameter
     * @return true if parameter exists and matches "t|true"
     */
    public static boolean booleanExParam(String name) {
        String param = getExParams(name);
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
        System.out.println("-> Parsing env scenarios:" + scenarios);
        String delimit = ".feature";
        int i = 0, end = scenarios.indexOf(delimit);
        while (i < scenarios.length()) {
            end = scenarios.indexOf(' ', end + 1);
            if (end == -1) {
                end = scenarios.length();
            }
            String scenarioPath = scenarios.substring(i, end).trim();
            System.out.println("->" + scenarioPath);
            scenarioList.add(scenarioPath);
            i = end;
        }

        Collections.sort(scenarioList);
        ArrayList<Map> featureScenarios = null;
        String workSpace = getExParams("WORKSPACE");
        if (workSpace == null) {
            workSpace = "";
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
                    System.out.println("File not found: " + path);
                    path = workSpace + "/" + path;
                }
                featureScenarios = new Gson().fromJson(Utils.gherkinToJson(false, path), ArrayList.class);
            }
            findScenario(featureScenarios, path, line);
        }

        // condense any duplicate feature files
        HashMap<String, ArrayList<String>> featureLines = new HashMap<>();
        for (String scenario : scenarioList) {
            int lineIndex = scenario.lastIndexOf(':');
            if (lineIndex == -1) {
                continue;
            }
            String scenarioPath = scenario.substring(0, lineIndex).trim();
            String lineNum = scenario.substring(lineIndex + 1);
            ArrayList<String> lines = featureLines.get(scenarioPath);
            if (lines == null) {
                lines = new ArrayList<>();
                featureLines.put(scenarioPath, lines);
            }
            lines.add(lineNum);
        }

        scenarioList.removeAll(scenarioList.stream()
                .filter(str -> str.contains(":"))
                .collect(Collectors.toList()));

        scenarioList.addAll(featureLines.keySet().stream()
                .map((key) -> key + ":" + StringUtils.join(featureLines.get(key), ":"))
                .collect(Collectors.toList()));

        return scenarioList;
    }

    /**
     * Closes a firefox alert if present
     */
    public static void closeAlert() {
        if (driver != null) {
            try {
                driver.switchTo().alert().accept();
            } catch (org.openqa.selenium.NoAlertPresentException e) {
                // there wasn't an alert
            }
        }
    }

    private static boolean findScenario(ArrayList<Map> featureScenarios, String scenarioPath, int line) {
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
                    return true;
                }
                hScenario.put(l, element);
            }
        }
        System.out.println("Cannot find scenario with line:" + line);
        int closest = 0;
        for (Integer l : hScenario.keySet()) {
            int dist = Math.abs(line - l);
            if (dist < line - closest) {
                closest = l;
            }
        }
        if (closest > 0) {
            features.put(scenarioPath + ":" + line, hScenario.get(closest));
            System.out.println("Load closest scenario with line:" + closest);
        }

        return false;
    }

    private static void close() {
        if (browser.equals("none")) {
            return;
        }
        if (tagCollection) {
            DATagCollector.close();
        }

        if (useSaucelabs) {
            if (driver instanceof RemoteWebDriver) {
                System.out.println("Link to your job: https://saucelabs.com/jobs/" + ((RemoteWebDriver) driver).getSessionId());
            }
            driverQuit();
        } else if (closeBrowserAtExit) {
            System.out.println("Closing driver...");
            if (driver != null) {
                driverQuit();
            }
        }
    }

    private static void driverQuit() {
        try {
            driver.quit();
            if (ie()) {
                try {
                    driver.quit();
                } catch (Exception | Error e) {
                    // nothing we can do if this doesn't work
                }
            }
        } catch (Exception e) {
            // skip error message on saucelab remote driver
            if (!useSaucelabs) {
                System.err.println("Error closing driver. You may need to clean up execution machine. error: " + e);
            }
        }
        driver = null;
    }

    /**
     * Get the current URL from browser and/or URL param
     *
     * @return the current url the browser is on
     */
    public static String getCurrentUrl() {
        if (!driverInitialized()) {
            return url;
        }

        if (appTest) {
            return getAndroidDriver() != null ? getAndroidDriver().currentActivity() : "";
        }

        String curUrl = driver.getCurrentUrl();
        if (curUrl.matches(".*?data.*?")) {
            return url;
        }

        return curUrl;
    }

    /**
     * Get the current URL from browser and/or URL param
     */
    private static String getInternalCurrentUrl() {
        if (StepUtils.ie()) {
            // IE windows authentication popup disappears when driver.getCurrentUrl() executed
            // so need to hook the function and wait for 10 seconds to look for the IE window authentication popup
            // and repeat every 1 hour
            long cs = System.currentTimeMillis();
            // check first 10 seconds only
            if (cs - ieAuthenticationTs < 10000) {
                if (browser.equals("ie") && booleanExParam("require_authentication")) {
                    // check IE window authentication popup
                    int exitValue = runIEMethod();
                    // IE authentication popup login successfully, no more checking for an hour
                    if (exitValue == 0) {
                        ieAuthenticationTs -= 10000;
                    }
                }
            } else {
                // after that check every hour
                if (cs - ieAuthenticationTs > 3600000) {
                    ieAuthenticationTs = cs;
                }
            }
        }
        return getCurrentUrl();
    }

    /**
     * Initialize IE authentication
     */
    public static void authenticationIeInit() {
        ieAuthenticationTs = System.currentTimeMillis();
    }

    public static int runIEMethod() {
        Process p;
        String filePath = "src/com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_ie.exe";
        if (!new File("src").exists()) {
            filePath = "com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_ie.exe";
        }

        try {
            p = getRuntime().exec(filePath);
            p.waitFor();  // wait for process to complete
            return (p.exitValue());
        } catch (Exception e) {
            // ignore all errors
        }
        return 1;
    }

    public static int runChromeMethod() {
        Process p;
        String filePath = "src/com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_chrome.exe";
        if (!new File("src").exists()) {
            filePath = "com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_chrome.exe";
        }

        try {
            p = getRuntime().exec(filePath);
            p.waitFor();  // wait for process to complete
            return (p.exitValue());
        } catch (Exception e) {
            // ignore all errors
        }
        return 1;
    }

    // protected methods
    // windows authentication dialog login
    protected static class AuthenticationDialog extends Thread {
        private static ServerSocket socketMutex;

        public AuthenticationDialog() {
            String osName = System.getProperty("os.name").toLowerCase();
            if (getExParams("require_authentication") == null) {
                System.out.println("AuthenticationDialog not required");
                return;
            }
            if (!(Utils.isWindows() && browser.equals("firefox")) &&
                    !(Utils.isWindows() && browser.equals("chrome")) &&
                    !(Utils.isOSX() && browser.equals("safari"))) {
                System.out.println("AuthenticationDialog not required : "
                        + getExParams("require_authentication")
                        + " : " + osName
                        + " : " + browser);
                return;
            }

            this.start();
            new Thread(() -> {
                switch (browser) {
                    case "firefox":
                        runFirefoxBackgroundMethod();
                        break;
                    case "safari":
                        runSafariBackgroundMethod();
                        break;
                    case "chrome":
                        runChromeBackgroundMethod();
                        break;
                }
            }).start();
        }

        protected static void runFirefoxBackgroundMethod() {
            Utils.threadSleep(4000, null);
            if (socketMutex == null) {
                System.out.println("-->Another Authentication monitoring background thread already exist.");
                return;
            }
            System.out.println("-->Firefox Windows Authentication monitoring background thread started");

            Process p;
            String filePath = "src/com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_firefox.exe";
            if (!new File("src").exists()) {
                filePath = "com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_firefox.exe";
            }

            while (true) {
                try {
                    p = getRuntime().exec(filePath);
                    Utils.ProcessWatchDog pd = new Utils.ProcessWatchDog(p, 5000, "runFirefoxBackgroundMethod()");
                    p.waitFor();  // wait for process to complete
                    pd.interrupt();
                } catch (Exception e) {
                    // ignore all errors
                }
                // wait 2 seconds
                Utils.threadSleep(2000, null);
            }
        }

        protected static void runSafariBackgroundMethod() {
            Utils.threadSleep(4000, null);
            if (socketMutex == null) {
                System.err.println("-->Another Authentication monitoring background thread already exist.");
                return;
            }
            System.err.println("-->Authentication monitoring background thread started");

            String fileName;
            fileName = "mac_authentication_safari.app";
            Process p;
            String filePath = "/Applications/" + fileName;
            File f = new File(filePath);
            if (!f.exists()) {
                System.err.println("-->Authentication monitoring program '" + filePath + "' does not exit.");
                return;
            }

            filePath = "open -a " + filePath;

            while (true) {
                try {
                    p = getRuntime().exec(filePath);
                    Utils.ProcessWatchDog pd = new Utils.ProcessWatchDog(p, 20000, "runSafariBackgroundMethod()");
                    p.waitFor();  // wait for process to complete
                    pd.interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                    // ignore all errors
                }
                // wait 10 seconds
                Utils.threadSleep(10000, null);
            }
        }

        protected static void runChromeBackgroundMethod() {
            Utils.threadSleep(4000, null);
            if (socketMutex == null) {
                System.out.println("-->Another Authentication monitoring background thread already exist.");
                return;
            }
            System.out.println("-->Chrome Windows Authentication monitoring background thread started");

            Process p;
            String filePath = "src/com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_chrome.exe";
            if (!new File("src").exists()) {
                filePath = "com/macys/sdt/shared/resources/framework/authentication_popup/windows_authentication_chrome.exe";
            }

            // chrome need workaround for the Chrome Authentication Required popup
            // check the current URL periodically and compare it with original URL
            String curl;
            String orgUrl = url;
            orgUrl = orgUrl.replace("https://", "");
            orgUrl = orgUrl.replace("http://", "");
            orgUrl = orgUrl.replace("www.", "");
            int width = -1;

            while (true) {
                Utils.threadSleep(4000, null);
                curl = getWebDriver().getCurrentUrl();
                // current url is still the same domain, then skip
                if (curl.contains(orgUrl)) {
                    continue;
                }

                // current url is not empty (Chrome default), then skip
                if (!(curl.contains("data:") || curl.contains("xnchegrn"))) {
                    continue;
                }

                // there seems to be Chrome Authentication Required Popup
                try {
                    if (width == -1) {
                        width = driver.manage().window().getSize().width;
                        filePath = filePath + " " + width;
                    }
                    p = getRuntime().exec(filePath);
                    Utils.ProcessWatchDog pd = new Utils.ProcessWatchDog(p, 10000, "runChromeBackgroundMethod()");
                    p.waitFor();  // wait for process to complete
                    pd.interrupt();
                } catch (Exception e) {
                    // ignore all errors
                }
                // wait 10 seconds
                Utils.threadSleep(6000, null);
            }
        }

        public void run() {
            try {
                socketMutex = new ServerSocket(6999);
                socketMutex.accept();
            } catch (IOException e) {
                socketMutex = null;
            }
        }
    }

    public static class PageHangWatchDog extends Thread {
        public static void init() {
            if (hangWatchDog == null) {
                hangWatchDog = new PageHangWatchDog();
            }
        }

        private PageHangWatchDog() {
            System.err.println("--> Start:PageHangWatchDog:" + new Date());
            this.reset(getWebDriver().getCurrentUrl());
            this.start();
        }

        private static PageHangWatchDog hangWatchDog;

        private void reset(String url) {
            this.ts = System.currentTimeMillis();
            if (url != null) {
                this.m_url = url;
                failCount = 0;
            }
        }

        public static boolean timedOut = false;

        private final static long TIMEOUT = (StepUtils.safari() || StepUtils.ie() ? 120 : 95) * 1000;
        private final static int MAX_FAILURES = 5;
        private String m_url;
        private long ts;
        private static int failCount;
        private static boolean pause;

        public static void pause(boolean pause) {
            PageHangWatchDog.pause = pause;
            if (!pause) {
                timedOut = false;
                failCount = 0;
            }
        }

        public void run() {
            while (true) {
                try {
                    if (pause || timedOut || !driverInitialized()) {
                        continue;
                    }
                    String url = currentURL;
                    //System.err.println("Watchdog tick:\n>old url: " + this.m_url + "\n>new url: " + url);
                    if (url.contains("about:blank")) {
                        continue;
                    }
                    if (url.equals(this.m_url)) {
                        if (System.currentTimeMillis() - this.ts > TIMEOUT) {
                            System.err.println("--> PageHangWatchDog: timeout at " + this.m_url +
                                    ", " + (MAX_FAILURES - failCount) + " failures until exit");
                            failCount++;
                            new Thread(() -> {
                                try {
                                    stopPageLoad();
                                } catch (Exception e) {
                                    // sometimes IE fails to run js. Continue running.
                                } finally {
                                    Navigate.browserRefresh();
                                }
                            }).start();

                            this.reset(null);
                            if (failCount > MAX_FAILURES) {
                                timedOut = true;
                                System.err.println("PageHangWatchDog timeout! Test will fail after this step ends");
                            }
                        }
                    } else {
                        this.reset(url);
                    }
                } catch (Throwable ex) {
                    System.err.println("--> Error:PageHangWatchDog:" + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    //System.err.print(pause ? "|" : "~");
                    Utils.threadSleep(5000, this.getClass().getSimpleName());
                }
            }
        }
    }
}
