package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.runner.MainRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Date;

/**
 * This is a base class for translated ruby tests
 */
public abstract class TranslatedSteps extends StepUtils {

    public static WebElement findElementBy(String by, String value) {
        WebElement element;
        switch (by.toLowerCase()) {
            case "link text":
                element = Elements.findElement(By.linkText(value));
                break;
            case "name":
                element = Elements.findElement(By.name(value));
                break;
            case "tag name":
                element = Elements.findElement(By.tagName(value));
                break;
            case "class name":
                element = Elements.findElement(By.className(value));
                break;
            case "css selector":
                element = Elements.findElement(By.cssSelector(value));
                break;
            case "xpath":
                element = Elements.findElement(By.xpath(value));
                break;
            case "partial link text":
                element = Elements.findElement(By.partialLinkText(value));
                break;
            default:
                element = Elements.findElement(By.id(value));

        }
        return element;
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
