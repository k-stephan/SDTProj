package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.utils.StepUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.macys.sdt.framework.runner.RunConfig.appTest;

/**
 * A collection of ways to click elements on the page
 */
public class Clicks {

    private static final Logger logger = LoggerFactory.getLogger(Clicks.class);

    /**
     * Sends an enter key to an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void sendEnter(String selector) {
        sendEnter(Elements.element(selector));
    }

    /**
     * Sends an enter key to an element
     *
     * @param selector By selector to use
     */
    public static void sendEnter(By selector) {
        Navigate.runBeforeNavigation();
        WebElement el = Elements.findElement(selector);
        if (el != null) {
            el.sendKeys(Keys.ENTER);
            Navigate.runAfterNavigation();
        }
    }

    /**
     * Sends an enter key to an element
     *
     * @param el element to use
     */
    public static void sendEnter(WebElement el) {
        Navigate.runBeforeNavigation();
        el.sendKeys(Keys.ENTER);
        Navigate.runAfterNavigation();
    }

    /**
     * Checks a checkbox if it is not already checked
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void selectCheckbox(String selector) {
        selectCheckbox(Elements.element(selector));
    }

    /**
     * Checks a checkbox if it is not already checked
     *
     * @param selector By selector to use
     */
    public static void selectCheckbox(By selector) {
        WebElement checkBox = Elements.findElement(selector);
        if (checkBox != null && !checkBox.isSelected()) {
            click(checkBox);
        }
    }

    /**
     * Unchecks a checkbox if it is already checked
     *
     * @param selector By selector to use
     */
    public static void unSelectCheckbox(By selector) {
        WebElement checkBox = Elements.findElement(selector);
        if (checkBox != null && checkBox.isSelected()) {
            Clicks.click(checkBox);
        }
    }

    /**
     * Unchecks a checkbox if it is already checked
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void unSelectCheckbox(String selector) {
        unSelectCheckbox(Elements.element(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void hoverForSelection(String selector) {
        hoverForSelection(Elements.element(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector By selector to use
     */
    public static void hoverForSelection(By selector) {
        hoverForSelection(Elements.findElement(selector));
    }

    /**
     * Hovers over an element
     *
     * @param el element to hover over
     */
    public static void hoverForSelection(WebElement el) {
        if (StepUtils.safari() || StepUtils.firefox()) {
            javascriptHover(el);
        } else {
            try {
                Actions action = new Actions(WebDriverManager.getWebDriver());
                action.moveToElement(el).build().perform();
            } catch (DriverNotInitializedException e) {
                Assert.fail("Driver not initialized");
            }
        }
        Wait.forPageReady();
    }

    /**
     * Hovers over an element
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void hover(String selector) {
        hover(Elements.element(selector));
    }

    /**
     * Hovers over an element
     *
     * @param selector By selector to use
     */
    public static void hover(By selector) {
        try {
            new WebDriverWait(WebDriverManager.getWebDriver(), RunConfig.timeout).until(
                    ExpectedConditions.visibilityOfElementLocated(selector)
            );
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }
    }

    /**
     * Hovers over an element using javascript
     *
     * @param element Element to hover over
     */
    public static void javascriptHover(WebElement element) {
        Navigate.runBeforeNavigation();
        Navigate.execJavascript("if(document.createEvent){"
                + "var evObj = document.createEvent('MouseEvents');"
                + "evObj.initEvent('mouseover', true, false); "
                + "arguments[0].focus();arguments[0].dispatchEvent(evObj);"
                + "}else if(document.createEventObject){"
                + "arguments[0].focus();arguments[0].fireEvent('onmouseover');"
                + "}", element);
        Navigate.runAfterNavigation();
    }

    /**
     * Clicks an element using javascript
     *
     * @param selector By selector to use
     */
    public static void javascriptClick(String selector) {
        javascriptClick(Elements.element(selector));
    }

    /**
     * Clicks an element using javascript
     *
     * @param selector By selector to use
     */
    public static void javascriptClick(By selector) {
        javascriptClick(Elements.findElement(selector));
    }

    /**
     * Clicks an element e using javascript
     *
     * @param e element to be clicked
     */
    public static void javascriptClick(WebElement e) {
        Navigate.runBeforeNavigation();

        Navigate.execJavascript("arguments[0].focus();arguments[0].click();", e);

        Wait.forPageReady();
        Navigate.runAfterNavigation();
    }

    /**
     * Clicks a random element from a group of elements using javascript
     *
     * @param selector String selector in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void randomJavascriptClick(String selector) throws NoSuchElementException {
        randomJavascriptClick(Elements.element(selector));
    }

    /**
     * Clicks a random element from a group of elements using javascript
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void randomJavascriptClick(By selector) throws NoSuchElementException {
        javascriptClick(Elements.getRandomElement(selector));
    }

    /**
     * Clicks an element that may be lazily loaded
     *
     * @param selector string locator in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickLazyElement(String selector) throws NoSuchElementException {
        StepUtils.scrollToLazyLoadElement(selector);
        click(selector);
    }

    /**
     * Clicks an element
     *
     * @param selector string locator in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(String selector) throws NoSuchElementException {
        click(Elements.element(selector));
    }

    /**
     * Clicks an element
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(By selector) throws NoSuchElementException {
        click(Elements.findElement(selector));
    }

    /**
     * Runs a pre-condition lambda, then clicks an element
     *
     * @param preCondition code to run before element is clicked
     * @param selector     By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(Runnable preCondition, By selector) throws NoSuchElementException {
        preCondition.run();
        click(Elements.findElement(selector));
    }

    /**
     * Clicks an element, then runs an exit condition lambda
     *
     * @param selector      By selector to use
     * @param exitCondition code to run after element is clicked
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(By selector, Runnable exitCondition) throws NoSuchElementException {
        click(Elements.findElement(selector));
        exitCondition.run();
    }

    /**
     * clicks a WebElement e
     *
     * @param el By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void click(WebElement el) throws NoSuchElementException {
        if (el == null) {
            throw new NoSuchElementException("Unable to click null element");
        }

        if (appTest) {
            ((MobileElement) el).tap(1, 250);
            return;
        }

        Navigate.runBeforeNavigation();
        try {
            WebDriver driver = WebDriverManager.getWebDriver();
            Actions actions = new Actions(driver);
            try {
                el = new WebDriverWait(driver, RunConfig.timeout).until(ExpectedConditions.elementToBeClickable(el));
            } catch (Exception ex) {
                try {
                    throw new NoSuchElementException("Element not clickable: " + el.getTagName() + ": " + el.getText() + ": " + ex.getMessage());
                } catch (StaleElementReferenceException exc) {
                    throw new NoSuchElementException("Element not clickable: " + exc.getMessage());
                }
            }
            if (RunConfig.analytics != null) {
                String contents = (String) Navigate.execJavascript("return arguments[0].outerHTML;", el);
                RunConfig.analytics.recordClickElement(contents);
            }
            try {
                // actions not supported in safari and still in progress for FF marionette driver
                if (StepUtils.safari() || (StepUtils.firefox() && RunConfig.browserVersion.compareTo("48.0") >= 0)) {
                    javascriptHover(el);
                    el.click();
                } else if (StepUtils.ipad()) {
                    javascriptHover(el);
                    javascriptClick(el);
                } else {
                    actions.moveToElement(el).perform();
                    actions.click().perform();
                }
            } catch (WebDriverException ex) {
                logger.warn("Error while clicking, trying JS: " + ex.getMessage());
                javascriptClick(el);
            }
            StepUtils.closeAlert();
            Wait.forPageReady();

            if (StepUtils.ie() || StepUtils.firefox()) {
                // IE & firefox like to leave the mouse over dropdown menus
                if (Elements.elementPresent("home.open_flyout") || Elements.elementPresent("home.my_account_menu")
                        || Elements.elementPresent("home.quickbag_items_list")) {
                    try {
                        actions.moveToElement(Elements.findElement("home.verify_page")).perform();
                    } catch (Exception | Error ex) {
                        // ignore
                    }
                }
            }
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }
        Navigate.runAfterNavigation();
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickRandomElement(String selector) throws NoSuchElementException {
        clickRandomElement(Elements.element(selector));
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector  By selector to use
     * @param predicate predicate to use to filter elements
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickRandomElement(String selector, Predicate<WebElement> predicate) throws NoSuchElementException {
        click(Elements.getRandomElement(Elements.element(selector), predicate));
    }

    /**
     * Clicks a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickRandomElement(By selector) throws NoSuchElementException {
        click(Elements.getRandomElement(selector));
    }

    /**
     * Clicks a point in a map area as a workaround of Selenium on Firefox/IE/Safari
     *
     * @param attribute   attribute to look for
     * @param searchValue expected value of attribute given by "attribute" arg
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void clickArea(String attribute, String searchValue) throws NoSuchElementException {
        List<WebElement> allOptions = Elements.findElements(By.tagName("area"), WebElement::isDisplayed);
        if (allOptions == null) {
            throw new NoSuchElementException("Unable to find any 'area' elements on this page");
        }
        boolean found = false;
        for (WebElement option : allOptions) {
            if (searchValue.equals(option.getAttribute(attribute))) {
                click(option);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new NoSuchElementException("No 'area' elements found with " + attribute + " = " + searchValue);
        }
    }

    /**
     * Clicks on an element and holds for 2 seconds
     *
     * @param el string selector to use
     */
    public static void longPress(String el) {
        longPress(Elements.findElement(el), 2000);
    }

    /**
     * Clicks on an element and holds for the given duration
     *
     * @param el string selector to use
     * @param pause how long to hold the mouse down
     */
    public static void longPress(String el, long pause) {
        longPress(Elements.findElement(el), pause);
    }

    /**
     * Clicks on an element and holds for the given duration
     *
     * @param el By selector to find element with
     * @param pause how long to hold the mouse down
     */
    public static void longPress(By el, long pause) {
        longPress(Elements.findElement(el), pause);
    }

    /**
     * Clicks on an element and holds for the given duration
     *
     * @param el Element to long press
     * @param pause how long to hold the mouse down
     */
    public static void longPress(WebElement el, long pause) {
        WebDriver driver;
        try {
            driver = WebDriverManager.getWebDriver();
        } catch (DriverNotInitializedException e) {
            logger.error("Driver not initialized");
            return;
        }
        Actions action = new Actions(driver);
        action.clickAndHold(el).perform();
        action.pause(pause);
    }

    /**
     * Sends an enter key to a random element from a group of elements
     *
     * @param selector String selector in format "page_name.element_name"
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void sendRandomEnter(String selector) throws NoSuchElementException {
        sendRandomEnter(Elements.element(selector));
    }

    /**
     * Sends an enter key to a random element from a group of elements
     *
     * @param selector By selector to use
     * @throws NoSuchElementException thrown if no element is found
     */
    public static void sendRandomEnter(By selector) throws NoSuchElementException {
        sendEnter(Elements.getRandomElement(selector));
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(String selector) {
        return clickIfPresent(Elements.element(selector));
    }

    /**
     * Clicks the element if it is displayed on the page
     *
     * @param selector By selector to use
     * @return true if element was clicked
     */
    public static boolean clickIfPresent(By selector) {
        Wait.forPageReady();
        try {
            if (Elements.elementPresent(selector)) {
                click(selector);
                return true;
            } else {
                throw new NoSuchElementException("Element not present");
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Wait until an element is present then clicks it
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element was clicked
     */
    public static boolean clickWhenPresent(String selector) {
        return clickWhenPresent(Elements.element(selector));
    }

    /**
     * Wait until an element is present then clicks it
     *
     * @param selector By selector to use
     * @return true if element was clicked
     */
    public static boolean clickWhenPresent(By selector) {
        if (Wait.untilElementPresent(selector)) {
            click(selector);
            return true;
        }
        return false;
    }

    /**
     * Selects an element from a list using the given text
     *
     * @param selector String selector in format "page_name.element_name"
     * @param find     text to look for
     */
    public static void clickElementByText(String selector, String find) {
        clickElementByText(Elements.element(selector), find);
    }

    /**
     * Selects an element from a list using the given text
     *
     * @param selector By selector to use
     * @param find     text to look for
     */
    public static void clickElementByText(By selector, String find) {
        List<WebElement> list = Elements.findElements(selector);
        if (list == null || find == null || list.size() == 0) {
            Assert.fail((list == null || list.size() == 0 ? "List to find from is empty " : "") + (find == null ? "String to find is null" : ""));
        }
        Optional<WebElement> element = list.stream()
                .filter(WebElement::isDisplayed)
                .filter(el -> el.isDisplayed() && el.getText().equalsIgnoreCase(find))
                .findFirst();

        if (element.isPresent()) {
            click(element.get());
        } else {
            Assert.fail("element with text \"" + find + "\" not found");
        }
    }

    /**
     * Swipe from one element to another - equivalent to drag and drop
     *
     * @param from String selector for element to start at
     * @param to String selector for element to end at
     */
    public static void swipe(String from, String to) {
        swipe(Elements.findElement(from), Elements.findElement(to));
    }

    /**
     * Swipe from one element to another - equivalent to drag and drop
     *
     * @param from By for element to start at
     * @param to By for element to end at
     */
    public static void swipe(By from, By to) {
        swipe(Elements.findElement(from), Elements.findElement(to));
    }

    /**
     * Swipe from one element to another - equivalent to drag and drop
     *
     * @param from element to start at
     * @param to element to end at
     */
    public static void swipe(WebElement from, WebElement to) {
        Wait.forPageReady();
        WebDriver driver;
        try {
            driver = WebDriverManager.getWebDriver();
        } catch (DriverNotInitializedException e) {
            logger.error("Driver not initialized");
            return;
        }
        if (driver instanceof AppiumDriver) {
            AppiumDriver appiumDriver = (AppiumDriver)driver;

            Dimension dimension = from.getSize();
            Point upperLeft = from.getLocation();
            Point fromCenter = new Point(upperLeft.getX() + dimension.getWidth() / 2, upperLeft.getY() + dimension.getHeight() / 2);

            dimension = to.getSize();
            upperLeft = to.getLocation();
            Point toCenter = new Point(upperLeft.getX() + dimension.getWidth() / 2, upperLeft.getY() + dimension.getHeight() / 2);

            // swipe args: startX, startY, endX, endY, duration
            appiumDriver.swipe(fromCenter.x, fromCenter.y, toCenter.x, toCenter.y, 200);
        } else {
            Actions action = new Actions(driver);
            action.dragAndDrop(from, to).perform();
        }
    }
}
