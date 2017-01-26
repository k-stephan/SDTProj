package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.runner.MainRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Date;
import java.util.List;

/**
 * This is a base class for translated ruby tests
 */
public abstract class TranslatedSteps extends StepUtils {

    public static WebElement findElementBy(String method, String value) {
        By by = Elements.getLocatorMethod(method, value);
        return Elements.findElement(by);
    }

    public static List<WebElement> findElementsBy(String method, String value) {
        By by = Elements.getLocatorMethod(method, value);
        return Elements.findElements(by);
    }

    public String getWindowSize() {
        return MainRunner.getWebDriver().manage().window().getSize().toString();
    }

    public void takeScreenshot() {
        StepUtils.browserScreenCapture("" + new Date().getTime());
    }

    public void setTimeout(int ms) {
        MainRunner.timeout = ms;
    }

    public void clickMouse() {
        Actions actions = new Actions(MainRunner.getWebDriver());
        actions.click().perform();
    }

    public void moveMouseTo(WebElement element) {
        Actions actions = new Actions(MainRunner.getWebDriver());
        actions.moveToElement(element).perform();
    }
}
