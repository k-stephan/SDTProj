package com.macys.sdt.framework.runner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.analytics.Analytics;
import com.macys.sdt.framework.utils.analytics.DATagCollector;
import com.macys.sdt.framework.utils.analytics.DigitalAnalytics;
import cucumber.api.cli.Main;
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
import java.util.stream.Collectors;

import static com.macys.sdt.framework.utils.StepUtils.ie;
import static com.macys.sdt.framework.utils.StepUtils.stopPageLoad;
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
     * True if executing through sauce labs. Checks for valid sauce labs info in "sauce_user" and "sauce_key" env variables
     */
    public static boolean useSauceLabs;

    /**
     * Name of Sauce Connect tunnel to use
     */
    public static String tunnelIdentifier = getEnvOrExParam("tunnel_identifier");

    /**
     * True if using chrome device emulation
     */
    public static boolean useChromeEmulation;

    /**
     * True if using appium to connect to a mobile device
     */
    public static boolean useAppium = booleanParam("use_appium");

    /**
     * True if testing a mobile application
     */
    public static boolean appTest;


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
     * Workspace path as given in "WORKSPACE" env variable
     */
    public static String workspace = getEnvVar("WORKSPACE");

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
     * Browser to use as given in "browser" env variable. Default chrome.
     */
    public static String browser = getEnvVar("browser");

    /**
     * Version of browser to use as given in "browser_version" env variable
     */
    public static String browserVersion = getEnvOrExParam("browser_version");

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
     * Time the tests were started
     */
    public static long startTime = System.currentTimeMillis();

    /**
     * Current run status. 0 is good, anything else is bad
     */
    public static int runStatus = 0;

    /**
     * Wait timeout as given in "timeout" env variable. Default 95 seconds (125 seconds for safari)
     */
    public static int timeout;

    /**
     * Device in use as given by "device" env variable
     */
    public static String device = getEnvOrExParam("device");

    /**
     * List containing URL's that have been visited
     */
    public static ArrayList<String> URLStack = new ArrayList<>();

    /**
     * Path to project currently being run optionally given by "sdt_project" env variable
     */
    public static String project = getEnvVar("sdt_project");

    /**
     * Path to active project files on file system
     */
    public static String projectDir = null;

    /**
     * The current URL of the browser
     */
    public static String currentURL = "";

    /**
     * Whether the proxy is disabled
     */
    public static boolean disableProxy = true;

    /**
     * Whether we're running in debug mode
     */
    public static boolean debugMode = booleanParam("debug");

    /**
     * The Sauce Labs username to use
     */
    public static String sauceUser = getEnvOrExParam("sauce_user");

    /**
     * The Sauce Labs API key for the user
     */
    public static String sauceKey = getEnvOrExParam("sauce_key");

    public static String browsermobServerHarTs = System.currentTimeMillis() + "";

    /**
     * Location of app for app testing (appium)
     */
    protected static String appLocation = getEnvOrExParam("app_location");

    private static String repoJar = getEnvOrExParam("repo_jar");

    /**
     * webdriver instance
     */
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

        System.out.println("Using project: " + project + "\nIf this does not match your project, please" +
                " add an environment variable \"sdt_project\" (previously called \"project\")" +
                " with project name in format \"<domain>.<project>\"");
        if (repoJar != null) {
            projectDir = project.replace(".", "/");
            Utils.extractResources(new File(repoJar), workspace, projectDir);
        } else {
            projectDir = project.replace(".", "/") + "/src/main/java/com/macys/sdt/projects/" + project.replace(".", "/");
        }

        ArrayList<String> featureScenarios = getFeatureScenarios();
        if (featureScenarios == null) {
            throw new Exception("Error getting scenarios");
        }

        // add any tags
        String tags = getEnvOrExParam("tags");
        if (tags != null) {
            featureScenarios.add("--tags");
            featureScenarios.add(tags);
        }

        // attempt to use workspace as relative path to feature file (if needed)
        if (workspace != null) {
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
        }

        featureScenarios.add("--glue");
        featureScenarios.add("com.macys.sdt.shared");
        featureScenarios.add("--plugin");
        featureScenarios.add("com.macys.sdt.framework.utils.SDTFormatter");
        featureScenarios.add("--plugin");
        featureScenarios.add("html:logs");

        System.out.println("-->Testing " + url + " using " +
                (useAppium ? device + " running " + (StepUtils.iOS() ? "iOS " : "Android ") + remoteOS : browser + " " + browserVersion)
                + (useSauceLabs ? " on Sauce Labs" : ""));

        driver = getWebDriver();

        new AuthenticationDialog();

        try {
            Thread cucumberThread = new Thread(() -> {
                int status = 1;
                try {
                    status = Main.run(featureScenarios.toArray(new String[featureScenarios.size()]),
                            Thread.currentThread().getContextClassLoader());
                } catch (IOException e) {
                    System.err.println("ERROR : IOException in cucumber run");
                } finally {
                    runStatus = status;
                }
            });
            cucumberThread.start();
            if (!appTest) {
                PageHangWatchDog.init(cucumberThread);
            }
            cucumberThread.join();

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

    /**
     * get and set environment variables
     */
    public static void getEnvVars() {
        if (workspace == null) {
            workspace = ".";
        }
        workspace = workspace.replace('\\', '/');
        workspace = workspace.endsWith("/") ? workspace : workspace + "/";

        scenarios = scenarios.replace('\\', '/');

        if (!url.matches("^https?://.*"))   {
            url = "http://" + url;
        }

        if (repoJar != null) {
            if (!(new File(repoJar).exists())) {
                if (!(new File(workspace + repoJar).exists())) {
                    System.err.println("-->Warning: Could not find given repo jar. Attempting to run without.");
                    repoJar = null;
                } else {
                    repoJar = workspace + repoJar;
                }
            }
        }
        Utils.createDirectory(logs = workspace + "logs/", true);
        Utils.createDirectory(temp = workspace + "temp/", true);

        if (remoteOS == null) {
            System.out.println("INFO : Remote OS not specified. Using default (Windows 7)");
            remoteOS = "Windows 7";
        }

        String analyticsClass = getEnvOrExParam("analytics");
        if (analyticsClass != null) {
            disableProxy = false;
            if (analyticsClass.equals("da")) {
                analytics = new DigitalAnalytics();
            }
            if (analytics != null) {
                System.out.print("INFO : including Analytics: " + analytics.getClass().getSimpleName());
            }
        }

        System.out.println("\n\n");

        // tag_collection
        tagCollection = booleanParam("tag_collection");
        if (tagCollection) {
            System.out.println("INFO : tag_collection is enabled");
        }

        // batch mode run on QA environment. Batch mode causes all products to be available
        if (batchMode) {
            System.out.println("INFO : batch_mode is enabled");
        }

        // use saucelabs when valid "sauce_user" and "sauce_key" is provided
        useSauceLabs = sauceUser != null && sauceKey != null;

        // use chrome emulation when it is mobile device and use of Appium is not mentioned
        useChromeEmulation = StepUtils.mobileDevice() && !useAppium;

        // Test is appTest when use of Appium is mentioned and app_location is given
        appTest = useAppium && (appLocation != null);

        // close browser at exist unless debugMode is on or test is appTest
        closeBrowserAtExit = !debugMode && !appTest;

        if (url == null && !appTest) {
            Assert.fail("\"website\" variable required to test a website");
        }
        if (browser == null && !appTest) {
            System.out.println("INFO : No browser given, using default (chrome)");
            browser = "chrome";
        }
        if (browserVersion == null) {
            browserVersion = WebDriverConfigurator.defaultBrowserVersion();
        }
        // close the test browser at scenario exit
        String envVal = getEnvOrExParam("timeout");
        if (envVal != null) {
            timeout = Integer.parseInt(envVal);
        } else {
            timeout = StepUtils.safari() ? 130 : 95;
        }

        // get project from environment variables
        if (project == null) {
            getProject();
        }
    }

    /**
     * Retrieves project info either from "sdt_project" or "scenarios" env val if possible
     */
    private static void getProject() {
        String projectPath = scenarios.replace("/", ".");
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(projectPath.split("\\.")));
        int index = parts.lastIndexOf("SDT");
        if (index == -1) {
            index = parts.indexOf("features");
            if (index < 2) {
                Assert.fail("Unable to determine project by given environment variables. Please" +
                        "add an environment variable \"sdt_project\" " +
                        "with project name in format \"<domain>.<project>\"");
            }
            project = parts.get(index - 2) + "." + parts.get(index - 1);  // domain.project
        } else {
            project = parts.get(index + 1) + "." + parts.get(index + 2);  // domain.project
        }
        String[] check = project.split("\\.");
        if (!(check.length == 2)) {
            Assert.fail("Project info is malformed. Please make sure it is in the format \"<domain>.<project>\"");
        }
    }

    /**
     * Resets the driver
     *
     * @param quit whether to close the driver
     */
    public static void resetDriver(boolean quit) {
        try {
            if (quit) {
                if (appTest) {
                    driver.quit();
                } else {
//                    driver.close();
                    driver.quit();
                    System.out.println("driver quit");
                    if (ie()) { // workaround for IE browser closing
                        driver.quit();
                    }
                }
            }
            driver = null;
            System.out.println("INFO : webdriver set to null");
        } catch (Exception e) {
            System.err.println("ERROR : error in resetDriver : " + e.getMessage());
            driver = null;
        }
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
        return val != null ? val : getExParam(name);
    }

    /**
     * Retrieves an environment variable
     *
     * @param name name of parameter to retrieve
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
        String workSpace = getEnvOrExParam("WORKSPACE");
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
                String json = Utils.gherkinToJson(false, path);
                try {
                    featureScenarios = new Gson().fromJson(json, ArrayList.class);
                } catch (JsonSyntaxException jex) {
                    System.err.println("--> Failed to parse : " + path);
                    System.err.println("--> json :\n\n" + json);
                    System.err.println();
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

    /**
     * It will initiate the close process for execution and quit the driver
     */
    private static void close() {
        if (browser.equals("none")) {
            return;
        }
        if (tagCollection) {
            DATagCollector.close();
        }

        if (driver != null) {
            System.out.println("Closing driver...");
            if (useSauceLabs || closeBrowserAtExit) {
                driverQuit();
            }
        }
    }

    /**
     * quit the webdriver
     */
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
            if (!useSauceLabs) {
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
        currentURL = curUrl;

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
                if (browser.equals("ie") && booleanParam("require_authentication")) {
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
            filePath = "shared/resources/framework/authentication_popup/windows_authentication_ie.exe";
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
            filePath = "shared/resources/framework/authentication_popup/windows_authentication_chrome.exe";
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

    public static Timeouts timeouts() {
        return Timeouts.instance();
    }

    // protected methods
    // windows authentication dialog login
    protected static class AuthenticationDialog extends Thread {
        private static ServerSocket socketMutex;

        public AuthenticationDialog() {
            String osName = System.getProperty("os.name").toLowerCase();
            if (getEnvOrExParam("require_authentication") == null) {
                System.out.println("AuthenticationDialog not required");
                return;
            }
            if (!(Utils.isWindows() && browser.equals("firefox")) &&
                    !(Utils.isWindows() && browser.equals("chrome")) &&
                    !((Utils.isOSX() || (remoteOS != null && remoteOS.contains("OS X"))) && browser.equals("safari"))) {
                System.out.println("AuthenticationDialog not required : "
                        + getEnvOrExParam("require_authentication")
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
                filePath = "shared/resources/framework/authentication_popup/windows_authentication_firefox.exe";
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
                filePath = "shared/resources/framework/authentication_popup/windows_authentication_chrome.exe";
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

    /**
     * Watchdog for Web Page
     *
     */
    public static class PageHangWatchDog extends Thread {
        private final static long TIMEOUT = (StepUtils.safari() || StepUtils.ie() ? 130 : 95) * 1000;
        private final static int MAX_FAILURES = 5;
        private static Thread cucumberThread;
        private static PageHangWatchDog hangWatchDog;
        private static int failCount;
        private static boolean pause;
        private String currentUrl;
        private long ts;

        private PageHangWatchDog() {
            System.err.println("--> Start: PageHangWatchDog: " + new Date());
            this.reset(getWebDriver().getCurrentUrl());
            this.setDaemon(true);
            this.start();
        }

        public static void init(Thread t) {
            if (hangWatchDog == null) {
                hangWatchDog = new PageHangWatchDog();
                cucumberThread = t;
            }
        }

        public static void resetWatchDog() {
            hangWatchDog.reset(null);
        }

        public static void pause(boolean pause) {
            PageHangWatchDog.pause = pause;
            if (!pause) {
                failCount = 0;
            }
        }

        private void reset(String url) {
            this.ts = System.currentTimeMillis();
            if (url != null) {
                this.currentUrl = url;
                failCount = 0;
            }
        }

        public void run() {
            while (cucumberThread.isAlive()) {
                try {
                    if (!driverInitialized()) {
                        continue;
                    } else if (pause) {
                        // if we've been waiting a while, send any browser command to prevent
                        // dropping the sauce labs connection
                        if (System.currentTimeMillis() - this.ts > TIMEOUT) {
                            getWebDriver().getCurrentUrl();
                            this.reset(this.currentUrl);
                        }
                        continue;
                    }
                    String url = currentURL;
                    //System.err.println("Watchdog tick:\n>old url: " + this.currentUrl + "\n>new url: " + url);
                    if (url.contains("about:blank")) {
                        continue;
                    }
                    if (url.equals(this.currentUrl)) {
                        if (System.currentTimeMillis() - this.ts > TIMEOUT) {
                            System.err.println("--> PageHangWatchDog: timeout at " + this.currentUrl +
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
                                System.err.println("PageHangWatchDog timeout! Pushing things along...");
                                cucumberThread.interrupt();
                                this.reset(null);
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
