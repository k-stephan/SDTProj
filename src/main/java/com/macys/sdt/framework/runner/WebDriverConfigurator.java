package com.macys.sdt.framework.runner;

import com.macys.sdt.framework.utils.ProxyFilters;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.macys.sdt.framework.runner.MainRunner.*;

public class WebDriverConfigurator {

    public static WebDriver initDriver(DesiredCapabilities capabilities) {
        WebDriver driver = null;
        if (capabilities == null) {
            capabilities = StepUtils.mobileDevice() ? initDeviceCapabilities() : initCapabilities();
        }
        if (useChromeEmulation && !useSauceLabs) {
            if (MainRunner.device.equalsIgnoreCase("firefoxbrowser")) {
                return new FirefoxDriver(capabilities);
            } else {
                return new ChromeDriver(capabilities);
            }
        } else if (!useAppium && !useSauceLabs) {
            switch (MainRunner.browser) {
                case "ie":
                    capabilities.setCapability("version", browserVersion);
                    String path = "shared/resources/framework/selenium_drivers/IEDriverServer.exe";
                    File file = new File(path);
                    if (!file.exists()) {
                        file = new File(MainRunner.workspace + "com/macys/sdt/" + path);
                        if (!file.exists() && Utils.isWindows())
                        	file = new File(System.getenv("HOME") + "/IEDriverServer.exe");
                    }
                    System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
                    driver = new InternetExplorerDriver(capabilities);
                    break;
                case "chrome":
                    setChromeDriverLocation(capabilities);
                    driver = new ChromeDriver(capabilities);
                    break;
                case "safari":
                    capabilities.setCapability("version", browserVersion);
                    // safari driver is not stable, retry 3 times
                    int count = 0;
                    while (driver == null && count++ < 3)
                        try {
                            driver = new SafariDriver(capabilities);
                        } catch (Exception e) {
                            Utils.threadSleep(5000, null);
                        }
                    break;
                case "edge":
                    driver = new EdgeDriver(capabilities);
                    break;
                default:
                    FirefoxProfile firefoxProfile = new FirefoxProfile();
                    ArrayList<File> extensions = new ArrayList<>();
                    if (tagCollection) {
                        System.out.println("tag collection started");
                        path = "shared/resources/framework/plugins/firefox/coremetricstools@coremetrics.xpi";
                        file = new File(path);
                        if (!file.exists()) {
                            file = new File("com/macys/sdt/" + path);
                        }
                        extensions.add(file);
                    }
                    String envExtensions = MainRunner.getEnvOrExParam("firefox_extensions");
                    if (envExtensions != null) {
                        String[] extensionSplit = envExtensions.split(" ");
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

                    try {
                        driver = new FirefoxDriver(capabilities);
                    } catch (IllegalStateException e) {
                        capabilities.setCapability("marionette", false);
                        driver = new FirefoxDriver(capabilities);
                    }
                    break;
            }
        }

        if (useSauceLabs) {
            driver = initSauceLabs(capabilities);
        } else if (useAppium) {
            driver = createAppiumDevice(capabilities);
        }

        Assert.assertNotNull("Driver should have been initialized by now", driver);

        if (!remoteOS.equals("Linux") && !appTest) {
            WebDriver.Timeouts to = driver.manage().timeouts();
            to.pageLoadTimeout(MainRunner.timeout, TimeUnit.SECONDS);
            to.setScriptTimeout(MainRunner.timeout, TimeUnit.SECONDS);
        }

        return driver;
    }

    private static void setChromeDriverLocation(DesiredCapabilities capabilities) {
        if (StepUtils.chrome()) {
            capabilities.setCapability("version", browserVersion);
            String fileName = "chromedriver.exe";
            if (Utils.isOSX()) {
                fileName = "chromedriver";
            }
            String path = "shared/resources/framework/selenium_drivers/" + fileName;
            File file = new File(MainRunner.workspace + path);
            if (!file.exists()) {
                file = new File(MainRunner.workspace + "com/macys/sdt/" + path);
                if (!file.exists() && Utils.isWindows())
                	file = new File(System.getenv("HOME") + "/" + fileName);
            }
            
            System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        }
    }

    public static DesiredCapabilities disabledProxyCap(DesiredCapabilities desiredCap) {
        if (MainRunner.disableProxy) {
            //			Proxy py = new Proxy();
            //			py.setNoProxy( "DIRECT" );
            desiredCap.setCapability(CapabilityType.ForSeleniumServer.AVOIDING_PROXY, true);
            desiredCap.setCapability(CapabilityType.ForSeleniumServer.PROXYING_EVERYTHING, false);
            //			desiredCap.setCapability ( CapabilityType.PROXY, py );
        }
        return desiredCap;
    }

    private static DesiredCapabilities initCapabilities() {
        switch (MainRunner.browser) {
            case "ie":
                DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                ieCapabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, true);
                ieCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                ieCapabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
                // changing requireWindowFocus to default value 'false' to avoid window / page freeze issue when the focus is not on the window
                ieCapabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
                ieCapabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
                return disabledProxyCap(ieCapabilities);
            case "chrome":
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                ChromeOptions chrome = new ChromeOptions();
                chrome.addArguments("test-type");
                chrome.addArguments("--disable-extensions");
                capabilities.setCapability(ChromeOptions.CAPABILITY, chrome);
                return disabledProxyCap(capabilities);
            case "safari":
                DesiredCapabilities sfCapabilities = DesiredCapabilities.safari();
                sfCapabilities.setCapability("unexpectedAlertBehaviour", "accept");
                return disabledProxyCap(sfCapabilities);
            case "edge":
                return DesiredCapabilities.edge();
            default:
                return disabledProxyCap(DesiredCapabilities.firefox());
        }
    }

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

    private static DesiredCapabilities setupChromeEmulator() {
        Map<String, String> emulationOptions = new HashMap<>();
        switch (MainRunner.device.toLowerCase()) {
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

    private static DesiredCapabilities getChromeEmulatorConfig(Map<String, String> emulationOptions) {
        Map<String, Object> chromeOptions = new HashMap<>();
        chromeOptions.put("mobileEmulation", emulationOptions);
        ArrayList<String> args = new ArrayList<>();
        args.add("--disable-extensions");
        chromeOptions.put("args", args);
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        setChromeDriverLocation(capabilities);
        return capabilities;
    }

    private static WebDriver initSauceLabs(DesiredCapabilities capabilities) {
        try {
            // remove quoted chars
            remoteOS = remoteOS.replace("\"", "");
            remoteOS = remoteOS.replace("'", "");
            if (!appTest) {
                capabilities.setCapability("platform", remoteOS);
                capabilities.setCapability("version", browserVersion);
                capabilities.setCapability("idleTimeout", 240);
            }
            capabilities.setCapability("tags", getEnvOrExParam("tags"));
            capabilities.setCapability("name", (StepUtils.macys() ? "Macy's" : "Bloomingdales") +
                    " SDT " + (project != null ? project : "") + " test");
            capabilities.setCapability("maxDuration", 3600);
            if (MainRunner.tunnelIdentifier != null) {
                capabilities.setCapability("tunnel-identifier", MainRunner.tunnelIdentifier);
                System.out.println("Using sauce connect tunnel: " + MainRunner.tunnelIdentifier);
            } else {
                System.out.println("running without sauce connect");
            }
            // need to increase res or we get tablet layout
            // not supported on win10 and mac OSX El Capitan (10.11)
            if (!StepUtils.mobileDevice() && !remoteOS.matches("^Windows 10|(.*?)10.11$")) {
                capabilities.setCapability("screenResolution", "1280x1024");
            }
            if (StepUtils.edge()) {
                System.err.println("WARNING: Microsoft's Edge Driver is not fully implemented yet. There may" +
                        " be strange or unexpected errors.");
            }
            if (StepUtils.safari()) {
                // safari driver is not stable, try up to 3 times
                int count = 0;
                while (count++ < 3)
                    try {
                        return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
                    } catch (Error | Exception e) {
                        Utils.threadSleep(1000, null);
                        if (count == 3) {
                            Assert.fail("Failed to initialize saucelabs connection: " + e);
                        }
                    }
            } else if (useAppium) {
                return createAppiumDevice(capabilities);
            } else {
                return new RemoteWebDriver(new URL("http://" + sauceUser + ":" + sauceKey + "@ondemand.saucelabs.com:80/wd/hub"), capabilities);
            }

        } catch (Exception e) {
            System.err.println("Could not create remove web driver: " + e);
        }
        Assert.fail("Unable to initialize driver");
        return null;
    }

    private static WebDriver createAppiumDevice(DesiredCapabilities capabilities) {
        if (appTest) {
            capabilities.setCapability(MobileCapabilityType.APP, MainRunner.appLocation);
            capabilities.setCapability("BROWSER_NAME", StepUtils.iOS() ? "IOS" : "Android");
        } else {
            capabilities.setCapability("BROWSER_NAME", MainRunner.browser);
        }
        capabilities.setCapability("appiumVersion", "1.6");
        if (appTest) {
            capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        }
        try {
            URL url;
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
            System.err.println("Could not create appium driver: " + e);
        }
        return null;
    }

    public static String defaultBrowserVersion() {
        switch (MainRunner.browser) {
            case "ie":
                return "11.0";
            case "edge":
                return "20.10240";
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
            default:
                return "54.0";
        }
    }

    public static WebDriver initDriverWithProxy() {
        if (browsermobServer != null) {
            System.err.println("-->Aborting prev proxy server:" + browsermobServer.getPort());
            try {
                browsermobServer.abort();
            } catch (Exception ex) {
                System.err.println("-->Failed to abort prev proxy server:" + browsermobServer.getPort());
            }
        }

        System.out.print("Initializing proxy server...");
        int port = 7000;
        boolean found = false;
        for (int i = 0; i < 10; i++) {
            try {
                browsermobServer = new BrowserMobProxyServer();
                browsermobServer.start(port);
                System.out.println("using port " + port);
                found = true;
                break;
            } catch (Exception ex) {
                System.out.println("port " + port + " is in use:" + ex.getMessage());
                port++;
            }
        }
        if (!found) {
            System.out.println("Cannot find open port for proxy server");
            System.out.println("Abort run.");
            System.exit(-1);
        }

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(browsermobServer);
        DesiredCapabilities capabilities = StepUtils.mobileDevice() ? initDeviceCapabilities() : initCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        WebDriver driver = initDriver(capabilities);
        browsermobServer.newHar(browsermobServerHarTs);

        if (!StepUtils.mobileDevice() && !StepUtils.MEW()) {
            browsermobServer.addRequestFilter(new ProxyFilters.ProxyRequestFilter(url));
            browsermobServer.addResponseFilter(new ProxyFilters.ProxyResponseFilter());
        }
        return driver;
    }
}
