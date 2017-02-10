package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class ConvertedSteps extends StepUtils {

    public static void maximizeBrowser() {
        MainRunner.getWebDriver().manage().window().maximize();
    }

    /**
     * Clicks the mouse at its current location
     */
    public static void clickMouse() {
        Actions actions = new Actions(MainRunner.getWebDriver());
        actions.click().perform();
    }

    /**
     * Moves the mouse to the position of the given element
     *
     * @param element element to move the mouse over
     */
    public static void moveMouseTo(WebElement element) {
        Actions actions = new Actions(MainRunner.getWebDriver());
        actions.moveToElement(element).perform();
    }

    public static void acceptAlert() {
        MainRunner.getWebDriver().switchTo().alert().accept();
    }

    public static String getPageSource() {
        return MainRunner.getWebDriver().getPageSource();
    }

    public static String getAlertText() {
        return MainRunner.getWebDriver().switchTo().alert().getText();
    }

}
