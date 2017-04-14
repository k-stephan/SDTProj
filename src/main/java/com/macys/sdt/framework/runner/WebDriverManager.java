package com.macys.sdt.framework.runner;

import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.utils.Utils;
import com.saucelabs.saucerest.SauceREST;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.macys.sdt.framework.utils.StepUtils.ie;

/**
 * Owns and manages the Selenium Web driver object
 */
public class WebDriverManager {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverManager.class);

    /**
     * WebDriver instance - could be ChromeDriver, FirefoxDriver, IOSDriver, etc.
     */
    static WebDriver driver = null;

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
     * @throws DriverNotInitializedException if driver isn't initialized
     */
    public static AppiumDriver getAppiumDriver() throws DriverNotInitializedException {
        if (driver == null) {
            throw new DriverNotInitializedException("Driver is not initialized!");
        }
        return driver instanceof AppiumDriver ? (AppiumDriver) driver : null;
    }

    /**
     * Gets the active iOS driver if the driver is an iOS driver, otherwise null
     *
     * @return the active iOS driver
     * @throws DriverNotInitializedException if driver isn't initialized
     */
    public static IOSDriver getIOSDriver() throws DriverNotInitializedException {
        if (driver == null) {
            throw new DriverNotInitializedException("Driver is not initialized!");
        }
        return driver instanceof IOSDriver ? (IOSDriver) driver : null;
    }

    /**
     * Gets the active android driver if the driver is an android driver, otherwise null
     *
     * @return the active Android driver
     * @throws DriverNotInitializedException if driver isn't initialized
     */
    public static AndroidDriver getAndroidDriver() throws DriverNotInitializedException {
        if (driver == null) {
            throw new DriverNotInitializedException("Driver is not initialized!");
        }
        return driver instanceof AndroidDriver ? (AndroidDriver) driver : null;
    }

    /**
     * Gets the current webDriver instance or tries to create one
     *
     * @return current webDriver instance
     * @throws DriverNotInitializedException if driver isn't initialized
     */
    public static synchronized WebDriver getWebDriver() throws DriverNotInitializedException {
        if (MainRunner.URLStack.size() == 0) {
            MainRunner.URLStack.add(RunConfig.url);
        }

        if (driver == null) {
            throw new DriverNotInitializedException("Driver is not initialized!");
        }

        if (!MainRunner.URLStack.get(MainRunner.URLStack.size() - 1).equals(MainRunner.currentURL)) {
            MainRunner.URLStack.add(MainRunner.currentURL);
        }
        return driver;
    }

    public static synchronized WebDriver startWebDriver() {
        if (driver != null) {
            driver.quit();
        }
        for (int i = 0; i < 2; i++) {
            if (RunConfig.useProxy) {
                driver = WebDriverConfigurator.initDriverWithProxy();
            } else {
                driver = WebDriverConfigurator.initDriver(null);
            }

            try {
                if (!RunConfig.useAppium) {
                    if (RunConfig.browser.equals("safari")) {
                        Dimension dimension = new Dimension(1280, 1024);
                        driver.manage().window().setSize(dimension);
                    } else {
                        driver.manage().window().maximize();
                    }
                    String windowSize = driver.manage().window().getSize().toString();
                    logger.info("Init driver: browser window size = " + windowSize);
                }
                return driver;
            } catch (Exception ex) {
                logger.error("Failed initialized webdriver: retry" + i + ":" + ex.getMessage());
                Utils.threadSleep(2000, null);
            }
        }
        return driver;
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

    /**
     * quit the webdriver
     */
    static void driverQuit() {
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
            logger.error("Error closing driver. You may need to clean up execution machine. error: " + e);
        }
        driver = null;
    }

    public static void setPassed(boolean passed) {
        SauceREST client = new SauceREST(RunConfig.sauceUser, RunConfig.sauceKey);
        String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
        if (passed) {
            client.jobPassed(sessionId);
        } else {
            client.jobFailed(sessionId);
        }
    }

    /**
     * Resets the driver
     *
     * @param quit whether to close the driver
     */
    public static void resetDriver(boolean quit) {
        try {
            if (quit || RunConfig.useSauceLabs) {
                driver.quit();
                logger.info("webdriver quit successful");
                if (ie()) {
                    // workaround for IE browser not closing the first time
                    driver.quit();
                    logger.info("webdriver quit successful for ie");
                }

            }
            driver = null;
        } catch (Exception e) {
            logger.error("error in reset webdriver : " + e.getMessage());
            driver = null;
        } finally {
            MainRunner.currentURL = "";
        }
    }

    /**
     * Get the current URL from browser and/or URL param
     *
     * @return the current url the browser is on
     */
    public static String getCurrentUrl() {
        // apps don't have urls!
        if (RunConfig.appTest) {
            return "";
        }

        if (!driverInitialized()) {
            return RunConfig.url;
        }

        String curUrl = driver.getCurrentUrl();
        if (curUrl.matches(".*?data.*?")) {
            return RunConfig.url;
        }
        MainRunner.currentURL = curUrl;

        return curUrl;
    }

    /**
     * Gets the current title of the browser or app
     *
     * @return page or app title
     */
    public static String getCurrentTitle() {
        if (!driverInitialized()) {
            return "";
        }
        return driver.getTitle();
    }
}
