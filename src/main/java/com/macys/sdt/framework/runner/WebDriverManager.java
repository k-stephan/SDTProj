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

import static com.macys.sdt.framework.utils.StepUtils.ie;

/**
 * Owns and manages the Selenium Web driver object
 */
public class WebDriverManager {


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
     */
    public static synchronized WebDriver getWebDriver() throws DriverNotInitializedException {
        if (MainRunner.URLStack.size() == 0) {
            MainRunner.URLStack.add(MainRunner.url);
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
            if (MainRunner.disableProxy) {
                // System.out.println("DEBUG stack trace: " +
                //        Utils.listToString(Utils.getCallFromFunction("getWebDriver"), "\n\t ", null));
                driver = WebDriverConfigurator.initDriver(null);
            } else {
                driver = WebDriverConfigurator.initDriverWithProxy();
            }

            try {
                if (!MainRunner.useAppium) {
                    if (MainRunner.browser.equals("safari")) {
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
            System.err.println("Error closing driver. You may need to clean up execution machine. error: " + e);
        }
        driver = null;
    }

    public static void setPassed(boolean passed) {
        SauceREST client = new SauceREST(MainRunner.sauceUser, MainRunner.sauceKey);
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
            if (quit || MainRunner.useSauceLabs) {
                driver.quit();
                System.out.println("driver quit");
                if (ie()) {
                    // workaround for IE browser not closing the first time
                    driver.quit();
                }

            }
            driver = null;
            System.out.println("INFO : webdriver set to null");
        } catch (Exception e) {
            System.err.println("ERROR : error in resetDriver : " + e.getMessage());
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
        if (MainRunner.appTest) {
            return "";
        }

        if (!driverInitialized()) {
            return MainRunner.url;
        }

        String curUrl = driver.getCurrentUrl();
        if (curUrl.matches(".*?data.*?")) {
            return MainRunner.url;
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
