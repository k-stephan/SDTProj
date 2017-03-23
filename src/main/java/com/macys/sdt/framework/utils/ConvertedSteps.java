package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.runner.WebDriverManager;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class ConvertedSteps extends StepUtils {

    public static void maximizeBrowser() {
        try {
            WebDriverManager.getWebDriver().manage().window().maximize();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }
    }

    /**
     * Clicks the mouse at its current location
     */
    public static void clickMouse() {
        try {
            Actions actions = new Actions(WebDriverManager.getWebDriver());
            actions.click().perform();
        } catch (DriverNotInitializedException e) {
                Assert.fail("Driver not initialized");
            }
    }

    /**
     * Moves the mouse to the position of the given element
     *
     * @param element element to move the mouse over
     */
    public static void moveMouseTo(WebElement element) {
        try {
            Actions actions = new Actions(WebDriverManager.getWebDriver());
            actions.moveToElement(element).perform();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }
    }

    public static void acceptAlert() {
        try {
            WebDriverManager.getWebDriver().switchTo().alert().accept();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }
    }

    public static String getPageSource() {
        try {
            return WebDriverManager.getWebDriver().getPageSource();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
            return null;
        }
    }

    public static String getAlertText() {
        try {
            return WebDriverManager.getWebDriver().switchTo().alert().getText();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
            return null;
        }
    }

    public static String getWindowHandle() {
        try {
            return WebDriverManager.getWebDriver().getWindowHandle();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
            return null;
        }
    }

}
