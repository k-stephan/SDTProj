package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.macys.sdt.framework.runner.MainRunner.appTest;

/**
 * A collection of ways to interact with text boxes
 */
public class TextBoxes {
    /**
     * Types text into a text box
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to type in
     */
    public static void typeTextbox(String selector, String text) {
        typeTextbox(Elements.element(selector), text);
        System.out.println("Typing: " + selector.split("\\.")[1] + " = \"" + text + "\"");
    }

    /**
     * Types text into a text box
     *
     * @param selector By selector to use
     * @param text     text to type in
     */
    public static void typeTextbox(By selector, String text) {
        Navigate.runBeforeNavigation();
        Wait.forPageReady();
        try {
            new WebDriverWait(WebDriverManager.getWebDriver(), MainRunner.timeout).until(ExpectedConditions.elementToBeClickable(selector));
            WebElement element = Elements.findElement(selector);
            if (element != null) {
                element.clear();
                element.sendKeys(text);
                Wait.forPageReady();
            } else {
                System.err.println("Could not type text \"" + text +
                        "\"\n into text box " + selector + "\nbecause no element was found");
            }
            Navigate.runAfterNavigation();
        } catch (DriverNotInitializedException e) {

        }
    }

    /**
     * Types text into element and sends an enter key
     *
     * @param selector String selector in format "page_name.element_name"
     * @param text     text to type in
     */
    public static void typeTextNEnter(String selector, String text) {
        typeTextNEnter(Elements.element(selector), text);
        System.out.println(selector.split("\\.")[1] + ": " + text);
    }

    /**
     * Types text into element and sends an enter key
     *
     * @param selector By selector to use
     * @param text     text to type in
     */
    public static void typeTextNEnter(By selector, String text) {
        Navigate.runBeforeNavigation();
        WebElement element = Elements.findElement(selector);
        if (element == null) {
            return;
        }

        if (appTest) {
            text += StepUtils.iOS() ? "" : "\n";
            element.clear();
            element.sendKeys(text);
            if (StepUtils.iOS()) {
                Clicks.click(By.id("Search"));
            }
        } else {
            element.click();
            element.clear();
            element.sendKeys(text);

            if (StepUtils.safari()) {
                try {
                    element.submit();
                } catch (Exception e) {
                    element.sendKeys(Keys.RETURN);
                }
            } else {
                element.sendKeys(Keys.RETURN);
            }
        }
        Navigate.runAfterNavigation();
    }
}
