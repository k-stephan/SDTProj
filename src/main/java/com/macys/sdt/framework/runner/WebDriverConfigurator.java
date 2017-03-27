package com.macys.sdt.framework.runner;

import com.macys.sdt.framework.utils.ProxyFilters;
import com.macys.sdt.framework.utils.ScenarioHelper;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.junit.Assert;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ThreadGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.macys.sdt.framework.runner.MainRunner.browsermobServer;
import static com.macys.sdt.framework.runner.RunConfig.*;

class WebDriverConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(MainRunner.class);

    /**
     * This method initiate specific driver with customized configurations
     *
     * @param capabilities preferred configurations in UI client
     * @return driver
     */
    static WebDriver initDriver(DesiredCapabilities capabilities) {
        if (capabilities == null) {
            capabilities = StepUtils.mobileDevice() ? initDeviceCapabilities() : initBrowserCapabilities();
        }

        WebDriver driver;
        if (useSauceLabs) {
            driver = initSauceLabs(capabilities);

            // print the session id of saucelabs for tracking job on sauceLabs
            if (driver instanceof RemoteWebDriver) {
                logger.info("Link to your saucelabs job: https://saucelabs.com/jobs/" + ((RemoteWebDriver) driver).getSessionId());
            } else {
                logger.info("No RemoteWebDriver instance : " + driver);
            }
        } else if (useAppium) {
            driver = initAppiumDevice(capabilities);
        } else {
            driver = initBrowser(capabilities);
        }

        Assert.assertNotNull("ERROR - SCRIPT : Driver should have been initialized by now", driver);

        if (!remoteOS.equals("Linux") && !appTest) {
            WebDriver.Timeouts to = driver.manage().timeouts();
            to.pageLoadTimeout(timeout, TimeUnit.SECONDS);
            to.setScriptTimeout(timeout, TimeUnit.SECONDS);
        }

        return driver;
    }

    /**
     * initiate browser driver with given capabilities based on browser asked
     *
     * @param capabilities preferred configurations for browser driver
     * @return instance of browser driver with preferred capabilities
     */
    private static WebDriver initBrowser(DesiredCapabilities capabilities) {
        WebDriver driver = null;
        switch (browser.toLowerCase()) {
            case "ie":
            case "internetexplorer":
                return ThreadGuard.protect(new InternetExplorerDriver(capabilities));
            case "chrome":
                return new ChromeDriver(capabilities);
            case "safari":
                int count = 0;
                while (driver == null && count++ < 3)
                    try {
                        driver = new SafariDriver(capabilities);
                    } catch (Exception e) {
                        logger.error("Failed to open safari driver: " + e);
                        logger.error("Retrying: " + count);
                        Utils.threadSleep(5000, null);
                    }
                return driver;
            case "edge":
                return new EdgeDriver(capabilities);
            default:
                boolean marionette = browserVersion.compareTo("48") >= 0;
                capabilities.setCapability("marionette", marionette);
                try {
                    return new FirefoxDriver(capabilities);
                } catch (Exception | Error e) {
                    capabilities.setCapability("marionette", !marionette);
                    return new FirefoxDriver(capabilities);
                }
        }

    }

    /**
     * This method set up capabilities based on browser asked mainly for desktop execution
     *
     * @return desiredCapabilities customized configurations as per browser
     */
    private static DesiredCapabilities initBrowserCapabilities() {
        DesiredCapabilities capabilities;

        switch (browser.toLowerCase()) {
            case "ie":
            case "internetexplorer":
                capabilities = DesiredCapabilities.internetExplorer();
                String path = "shared/resources/framework/selenium_drivers/IEDriverServer.exe";
                File file = new File(path);
                if (!file.exists()) {
                    file = new File(workspace + "com/macys/sdt/" + path);
                    if (!file.exists() && Utils.isWindows()) {
                        file = new File(System.getenv("HOME") + "/IEDriverServer.exe");
                    }
                }
                if (file.exists()) {
                    System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
                } else {
                    logger.warn("Unable to use built-in IE driver, will use machine's IE driver if it exists");
                }
                capabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, true);
                capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
                // changing requireWindowFocus to default value 'false' to avoid window or
                // page freeze issue when the focus is not on the window
                capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
                capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
                return disabledProxyCap(capabilities);
            case "chrome":
                capabilities = DesiredCapabilities.chrome();
                setChromeDriverLocation();
                ChromeOptions chrome = new ChromeOptions();
                chrome.addArguments("test-type");
                chrome.addArguments("--disable-extensions");

                //hide infobar if a session is being controlled by an automated test
                chrome.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

                capabilities.setCapability(ChromeOptions.CAPABILITY, chrome);
                return disabledProxyCap(capabilities);
            case "safari":
                capabilities = DesiredCapabilities.safari();
                capabilities.setCapability("unexpectedAlertBehaviour", "accept");
                return disabledProxyCap(capabilities);
            case "edge":
                logger.warn("Microsoft's Edge Driver is not fully implemented yet. There may be strange or unexpected errors.");
                capabilities = DesiredCapabilities.edge();
                return disabledProxyCap(capabilities);
            default:
                setFirefoxDriverLocation();
                capabilities = DesiredCapabilities.firefox();
                FirefoxProfile firefoxProfile = new FirefoxProfile();
                ArrayList<File> extensions = new ArrayList<>();
                if (tagCollection) {
                    logger.info("tag collection started ...");
                    path = "shared/resources/framework/plugins/firefox/coremetricstools@coremetrics.xpi";
                    file = new File(path);
                    if (!file.exists()) {
                        file = new File("com/macys/sdt/" + path);
                    }
                    extensions.add(file);
                }
                if (disableJS) {
                    firefoxProfile.setPreference("javascript.enabled", false);
                }
                String envExtensions = getEnvOrExParam("firefox_extensions");
                if (envExtensions != null) {
                    String[] extensionSplit = envExtensions.split(";");
                    for (String s : extensionSplit) {
                        File f = new File(s);
                        if (f.exists()) {
                            extensions.add(f);
                        }
                    }
                }
                for (File f : extensions) {
                    try {
                        firefoxProfile.addExtension(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("Cannot load extension");
                    }
                }
                capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

                // latest firefox from 48 use marionette. But in Lorein VM firefox installed (default firefox version) is 46. Hence the conditional branching
                if (browserVersion != null && browserVersion.compareTo("48.0") >= 0) {
                    capabilities.setCapability("marionette", true);
                } else {
                    capabilities.setCapability("marionette", false);
                }

                return disabledProxyCap(capabilities);
        }
    }

    /**
     * This method set chromeDriver from the repo in the running machine for execution
     */
    private static void setChromeDriverLocation() {
        String fileName = Utils.isOSX() ? "chromedriver" : "chromedriver.exe";
        String path = "shared/resources/framework/selenium_drivers/" + fileName;
        File file = new File(workspace + path);
        if (!file.exists()) {
            file = new File(workspace + "com/macys/sdt/" + path);
            if (!file.exists() && Utils.isWindows()) {
                file = new File(System.getenv("HOME") + "/" + fileName);
            }
        }
        if (file.exists()) {
            System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        } else {
            logger.warn("Unable to use built-in chromedriver, will use machine's chromedriver if it exists");
        }

    }

    /**
     * This method set firefox gecko driver from the repo in the running machine for execution
     */
    private static void setFirefoxDriverLocation() {
        String fileName = Utils.isOSX() ? "geckodriver" : "geckodriver.exe";
        String path = "shared/resources/framework/selenium_drivers/" + fileName;
        File file = new File(workspace + path);
        if (!file.exists()) {
            file = new File(workspace + "com/macys/sdt/" + path);
            if (!file.exists() && Utils.isWindows()) {
                file = new File(System.getenv("HOME") + "/" + fileName);
            }
        }
        if (file.exists()) {
            System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
        } else {
            logger.warn("Unable to use built-in firefox geckodriver, will use machine's geckodriver if it exists");
        }
    }

    /**
     * This method set proxy as disables in capability
     *
     * @param capabilities configurations which are already set where disable proxy configurations is to be added
     * @return desiredCapabilities configurations including disable proxy capability
     */
    private static DesiredCapabilities disabledProxyCap(DesiredCapabilities capabilities) {
        if (disableProxy) {
            //			Proxy py = new Proxy();
            //			py.setNoProxy( "DIRECT" );
            capabilities.setCapability(CapabilityType.ForSeleniumServer.AVOIDING_PROXY, true);
            capabilities.setCapability(CapabilityType.ForSeleniumServer.PROXYING_EVERYTHING, false);
            //			desiredCap.setCapability ( CapabilityType.PROXY, py );
        }
        capabilities.setCapability("version", browserVersion);
        return capabilities;
    }

    /**
     * This method setup chrome emulator or device based capabilities
     * based on if useChromeEmulation is set to true or not
     *
     * @return chrome emulator or device based capabilities
     */
    private static DesiredCapabilities initDeviceCapabilities() {
        if (device == null) {
            device = "";
        }

        if (useChromeEmulation) {
            return setupChromeEmulator();
        } else {
            return setupDevice();
        }
    }

    /**
     * This method set up device (appium) based capabilities for ios or android
     *
     * @return desiredCapabilities ios or android device (appium) based configurations
     */
    private static DesiredCapabilities setupDevice() {
        DesiredCapabilities caps;
        if (StepUtils.iOS()) {
            if (useAppium) {
                caps = new DesiredCapabilities(browser, browserVersion, Platform.MAC);
                caps.setCapability("platformName", "iOS");
                caps.setCapability("autoDismissAlerts", true);
            } else {
                caps = DesiredCapabilities.iphone();
            }
            remoteOS = useAppium ? remoteOS : "OS X 10.10";
        } else {
            if (useAppium) {
                caps = new DesiredCapabilities();
                caps.setCapability("platformName", "Android");
            } else {
                caps = DesiredCapabilities.android();
            }
            remoteOS = useAppium ? remoteOS : "Linux";
        }
        caps.setCapability("platformVersion", remoteOS);
        caps.setCapability("deviceName", device);
        caps.setCapability("deviceOrientation", "portrait");
        return caps;
    }

    /**
     * This method set up chrome emulator based capabilities for a number of devices given by MainRunner.device
     *
     * @return desiredCapabilities chrome emulator based configurations for devices asked
     */
    private static DesiredCapabilities setupChromeEmulator() {
        Map<String, String> emulationOptions = new HashMap<>();
        switch (device.toLowerCase()) {
            case "android emulator":
                DesiredCapabilities androidCapabilities = DesiredCapabilities.android();
                androidCapabilities.setBrowserName("chrome");
                androidCapabilities.setPlatform(Platform.ANDROID);
                androidCapabilities.setCapability("newCommandTimeout", 9000);
                return androidCapabilities;
            case "iphone 6":
                emulationOptions.put("deviceName", "Apple iPhone 6");
                return getChromeEmulatorConfig(emulationOptions);
            case "ipad":
                emulationOptions.put("deviceName", "Apple iPad");
                return getChromeEmulatorConfig(emulationOptions);
            case "google nexus 5":
                emulationOptions.put("deviceName", "Google Nexus 5");
                return getChromeEmulatorConfig(emulationOptions);
            // despite being available manually, 6p and 5x don't work through automation yet
            // aliases of nexus 6 for now
            case "google nexus 6p":
            case "google nexus 5x":
            case "google nexus 6":
                emulationOptions.put("deviceName", "Google Nexus 6");
                return getChromeEmulatorConfig(emulationOptions);
            case "google nexus 10":
                emulationOptions.put("deviceName", "Google Nexus 10");
                return getChromeEmulatorConfig(emulationOptions);
            case "samsung galaxy s4":
                emulationOptions.put("deviceName", "Samsung Galaxy S4");
                return getChromeEmulatorConfig(emulationOptions);
            default:
                return DesiredCapabilities.chrome();
        }
    }

    /**
     * This method set up capabilities for Chrome Emulator based on device asked
     *
     * @param emulationOptions device name
     * @return desiredCapabilities preferred configurations for Chrome Emulator
     */
    private static DesiredCapabilities getChromeEmulatorConfig(Map<String, String> emulationOptions) {
        setChromeDriverLocation();
        ChromeOptions chromeOptions = new ChromeOptions();

        // set mobile emulation information like device name
        chromeOptions.setExperimentalOption("mobileEmulation", emulationOptions);

        chromeOptions.addArguments("--disable-extensions");

        //hide infobar session is being controlled by an automated test
        chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        return desiredCapabilities;
    }

    /**
     * This method initiate SauceLabs customized driver with preferred capabilities
     *
     * @param capabilities preferred configurations for driver
     * @return instance of SauceLabs related driver with preferred capabilities
     */
    private static WebDriver initSauceLabs(DesiredCapabilities capabilities) {
        try {
            // remove quoted chars
            remoteOS = remoteOS.replace("\"", "");
            remoteOS = remoteOS.replace("'", "");

            // only browser specific capability (including mew website in chrome emulation)
            if (!useAppium) {
                capabilities.setCapability("platform", remoteOS);
                capabilities.setCapability("version", browserVersion);
            }

            // temp fix until sauce labs updates it: Set the chrome driver version
            if (StepUtils.chrome()) {
                capabilities.setCapability("chromedriverVersion", "2.28");
            }

            // set browser name
            if(browser.equalsIgnoreCase("ie"))
                capabilities.setCapability("browserName", "iexplore");
            else if (browser.equalsIgnoreCase("edge"))
                capabilities.setCapability("browserName", "microsoftedge");
            else
                capabilities.setCapability("browserName", browser);

            capabilities.setCapability("idleTimeout", 300);
            capabilities.setCapability("tags", getEnvOrExParam("tags"));

            // set test name for sauceLabs
            String scenario = ScenarioHelper.scenario != null ? ScenarioHelper.scenario.getName() : "";
            capabilities.setCapability("name", (StepUtils.macys() ? "MCOM" : "BCOM") +
                    " SDT " + (project != null ? project : "") + " : " + scenario);
            capabilities.setCapability("maxDuration", 3600);

            // to use sauce connect
            if (tunnelIdentifier != null) {
                if (tunnelIdentifier.equalsIgnoreCase("parent")) {
                    capabilities.setCapability("tunnel-identifier", "macysParentTunnel");
                    capabilities.setCapability("parentTunnel", "satish-macys");
                    logger.info("Using sauce connect tunnel: macysParentTunnel");
                } else {
                    capabilities.setCapability("tunnel-identifier", tunnelIdentifier);
                    logger.info("Using sauce connect tunnel: " + tunnelIdentifier);
                }
            } else {
                logger.info("running without sauce connect");
            }

            // need to increase resolution or we get tablet layout
            // not supported on win10 and mac OSX El Capitan (10.11)
            if (!StepUtils.mobileDevice() && !remoteOS.matches("^(.*?)10.11|(.*?)10.12$")) {
                capabilities.setCapability("screenResolution", "1280x1024");
            }
            if (!StepUtils.mobileDevice() && remoteOS.matches("^(.*?)10.11|(.*?)10.12$")) {
                capabilities.setCapability("screenResolution", "1376x1032");
            }

            if (useAppium) { // iOS or Android
                return initAppiumDevice(capabilities);
            } else if (StepUtils.safari()) {    // Desktop Safari
                // safari driver is not stable, try up to 3 times
                int count = 0;
                while (count++ < 3) {
                    try {
                        return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
                    } catch (Error | Exception e) {
                        Utils.threadSleep(1000, null);
                        if (count == 3) {
                            Assert.fail("Failed to initialize Sauce Labs connection: " + e);
                        }
                    }
                }
            } else if (StepUtils.firefox()) {   // Desktop Firefox
                try {
                    // depending on firefox version, marionette is set
                    if (browserVersion != null && (browserVersion.compareTo("48.0") >= 0 || browserVersion.equalsIgnoreCase("beta")))   {
                        capabilities.setCapability("seleniumVersion", "3.0.1");
                        capabilities.setCapability("marionette", true);
                    } else if (browserVersion != null && browserVersion.compareTo("48.0") < 0) {
                        capabilities.setCapability("marionette", false);
                    }
                    return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
                } catch (IllegalStateException | SessionNotCreatedException e) {
                    logger.warn("ERROR - SCRIPT : error to instantiate firefox remote driver for saucelabs. Will retry with marionette true.");

                    // retry instantiating driver.
                    capabilities.setCapability("marionette", true);
                    return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
                }
            } else { // other Desktop Browsers
                return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
            }

        } catch (Exception e) {
            logger.error("ERROR - SCRIPT : Could not create remote web driver: " + e);
        }
        Assert.fail("ERROR - SCRIPT : Unable to initialize driver");
        return null;
    }

    /**
     * initiate appium driver (ios or android) with given capabilities for local execution or saucelabs
     *
     * @param capabilities preferred configurations for ios or android driver
     * @return instance of appium device ios or android driver
     */
    private static WebDriver initAppiumDevice(DesiredCapabilities capabilities) {
        if (appTest) {
            capabilities.setCapability(MobileCapabilityType.APP, appLocation);
            capabilities.setCapability("browserName", StepUtils.iOS() ? "IOS" : "Android");
            capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        } else {
            capabilities.setCapability("browserName", browser);
        }

        // setting appium version
        if (useSauceLabs) { // for saucelabs execution
            if (StepUtils.iOS() && Integer.parseInt(remoteOS.split(Pattern.quote("."))[0]) >= 10) {
                capabilities.setCapability("appiumVersion", "1.6");
            } else {
                capabilities.setCapability("appiumVersion", "1.5.3");
            }
        } else {    // for non saucelabs execution
            capabilities.setCapability("appiumVersion", "1.6");
        }

        try {
            URL url;

            // URL creation
            if (useSauceLabs) {
                url = new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub");
            } else {
                String appiumURL = getEnvOrExParam("appium_server");
                appiumURL = appiumURL == null ? "http://127.0.0.1" : appiumURL;
                if (!appiumURL.startsWith("http://")) {
                    appiumURL = "http://" + appiumURL;
                }
                if (!appiumURL.matches("(.*?):[0-9][0-9][0-9][0-9](.*?)")) {
                    appiumURL += ":4723";
                }
                if (!appiumURL.endsWith("/wd/hub")) {
                    appiumURL += "/wd/hub";
                }
                url = new URL(appiumURL);
            }

            if (StepUtils.iOS()) {
                return new IOSDriver(url, capabilities);
            } else {
                return new AndroidDriver(url, capabilities);
            }
        } catch (MalformedURLException e) {
            logger.error("Could not create appium driver: " + e);
        }
        return null;
    }

    /**
     * This method sets default browser version based on browser asked
     *
     * @return default version of browser asked
     */
    static String defaultBrowserVersion() {
        switch (browser) {
            case "ie":
                return "11.0";
            case "edge":
                return "25.10586";
            case "safari":
                String version;
                if (remoteOS == null) {
                    version = "9.0";
                } else if (remoteOS.contains("10.12")) {
                    version = "10.0";
                } else if (remoteOS.contains("10.11")) {
                    version = "9.0";
                } else if (remoteOS.contains("10.10")) {
                    version = "8.0";
                } else if (remoteOS.contains("10.9")) {
                    version = "7.0";
                } else if (remoteOS.contains("10.8")) {
                    version = "6.0";
                } else {
                    version = "0";
                }
                return version;
            case "chrome":
                return "56.0";
            default: // firefox version we have in Lorraine VM's
                return "46.0";
        }
    }

    /**
     * This method initiates driver having capability of BrowserMob proxy.
     * BrowserMob server runs on port 7000.
     *
     * @return instance of the driver having capability of BrowserMob proxy
     */
    static WebDriver initDriverWithProxy() {
        if (browsermobServer != null) {
            logger.error("Aborting prev proxy server:" + browsermobServer.getPort());
            try {
                browsermobServer.abort();
            } catch (Exception ex) {
                logger.error("Failed to abort prev proxy server:" + browsermobServer.getPort());
            }
        }

        logger.info("Initializing proxy server...");
        int port = 7000;
        boolean found = false;
        for (int i = 0; i < 10; i++) {
            try {
                browsermobServer = new BrowserMobProxyServer();

                // Disable upstream server certificate verification to avoid SSLHandshakeException
                // as websites used for testing are trusted.
                browsermobServer.setTrustAllServers(true);

                browsermobServer.start(port);
                logger.info("using port : " + port);
                found = true;
                break;
            } catch (Exception ex) {
                logger.info("port " + port + " is in use" + ex.getMessage());
                port++;
            }
        }
        if (!found) {
            logger.error("Cannot find open port for proxy server");
            logger.error("Abort run.");
            System.exit(-1);
        }

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(browsermobServer);
        DesiredCapabilities capabilities = StepUtils.mobileDevice() ? initDeviceCapabilities() : initBrowserCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        WebDriver driver = initDriver(capabilities);
        browsermobServer.newHar(System.currentTimeMillis() + "");
        if (userAgent != null) {
            browsermobServer.addHeader("User-Agent", userAgent);
        }

        if (!StepUtils.mobileDevice() && !StepUtils.MEW()) {
            browsermobServer.addRequestFilter(new ProxyFilters.ProxyRequestFilter(url));
            browsermobServer.addResponseFilter(new ProxyFilters.ProxyResponseFilter());
        }
        return driver;
    }
}
