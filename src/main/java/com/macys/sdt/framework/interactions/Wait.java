package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.macys.sdt.framework.runner.MainRunner.*;

/**
 * A collection of ways to wait for expected conditions
 */
public class Wait {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Waits until the given condition method returns true
     *
     * @param condition method to check whether we're done waiting
     * @return true if condition returned true, false if timeout occurred
     */
    public static boolean until(BooleanSupplier condition) {
        return until(condition, MainRunner.timeouts().general());
    }

    /**
     * Waits a number of seconds until the given condition method returns true
     *
     * @param condition method to check whether we're done waiting
     * @param seconds   number of seconds to wait before timing out (default 5)
     * @return true if condition returned true, false if timeout occurred
     */
    public static boolean until(BooleanSupplier condition, Integer seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverManager.getWebDriver(), seconds);
            wait.until((WebDriver driver) -> condition.getAsBoolean());
            return true;
        } catch (Exception ex) {
            logger.debug("issue in until condition : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Wait until an element is no longer present
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element disappears/was not present
     */
    public static boolean untilElementNotPresent(String selector) {
        return untilElementNotPresent(Elements.element(selector));
    }

    /**
     * Wait until an element is no longer present
     *
     * @param selector By selector to use
     * @return true if element disappears/was not present
     */
    public static boolean untilElementNotPresent(By selector) {
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverManager.getWebDriver(), RunConfig.timeout);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            logger.debug("issue in until an element " + selector.toString() + " no longer present " + ex.getMessage());
            return false;
        }
    }

    /**
     * Wait until all elements are no longer present
     *
     * @param list WebElements to watch for
     * @return true if elements disappear/were not present
     */
    public static boolean untilElementNotPresent(List<WebElement> list) {
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverManager.getWebDriver(), RunConfig.timeout);
            wait.until(ExpectedConditions.invisibilityOfAllElements(list));
            return true;
        } catch (Exception ex) {
            logger.debug("Unable to ensure if elements are no longer present due to " + ex.getMessage());
            return false;
        }
    }

    /**
     * Wait until an element is no longer present
     *
     * @param el WebElement to watch for
     * @return true if element disappears/was not present
     */
    public static boolean untilElementNotPresent(WebElement el) {
        ArrayList<WebElement> list = new ArrayList<>();
        list.add(el);
        return untilElementNotPresent(list);
    }

    /**
     * Wait until the text of an element matches the given expected text
     *
     * @param selector     String selector in format "page_name.element_name"
     * @param expectedText Expected value of text
     * @param waitTime     Number of seconds to wait or 0 to use default wait time
     * @return true if change occurred, false if not
     */
    public static boolean untilTextChanged(String selector, String expectedText, int waitTime) {
        if (waitTime == 0) {
            waitTime = timeouts().general();
        }
        return untilTextChanged(Elements.element(selector), expectedText, waitTime);
    }

    /**
     * Wait until the text of an element matches the given expected text
     *
     * @param selector     By selector to use
     * @param expectedText Expected value of text
     * @param waitTime     Number of seconds to wait or 0 to use default wait time
     * @return true if change occurred, false if not
     */
    public static boolean untilTextChanged(By selector, String expectedText, int waitTime) {
        if (waitTime == 0) {
            waitTime = timeouts().general();
        }
        return until(() -> Elements.getText(selector).equalsIgnoreCase(expectedText), waitTime);
    }

    /**
     * Waits until the given text is present somewhere on the page
     * <p>
     * NOTE: This element is NOT safe from picking up comments in the page source. Be careful of false positives.
     * </p>
     *
     * @param text     text to wait to be present
     * @param waitTime Number of seconds to wait or 0 to use default wait time
     * @return true if text showed up before timeout
     */
    public static boolean untilTextPresent(String text, int waitTime) {
        if (waitTime == 0) {
            waitTime = timeouts().general();
        }
        return until(() -> {
            try {
                return WebDriverManager.getWebDriver().getPageSource().contains(text);
            } catch (DriverNotInitializedException e) {
                return false;
            }
        }, waitTime);
    }

    /**
     * Wait until an element is present and click it.
     *
     * @param selector    By selector to use
     * @param waitSeconds time to wait in seconds
     */
    public static void secondsUntilElementPresentAndClick(String selector, int waitSeconds) {
        By elClick = Elements.element(selector);
        secondsUntilElementPresent(elClick, waitSeconds);
        Clicks.click(elClick);
    }

    /**
     * Wait until an element is present
     *
     * @param selector String selector in format "page_name.element_name"
     * @return true if element is present
     */
    public static boolean untilElementPresent(String selector) {
        return secondsUntilElementPresent(Elements.element(selector), MainRunner.timeouts().untilElementPresent());
    }

    /**
     * Wait until an element is present
     *
     * @param selector By selector to use
     * @return true if element is present
     */
    public static boolean untilElementPresent(By selector) {
        return secondsUntilElementPresent(selector, MainRunner.timeouts().untilElementPresent());
    }

    /**
     * Wait until an element is present
     *
     * @param selector String selector in format "page_name.element_name"
     * @param seconds  time to wait in seconds
     * @return true if element appeared/was already present
     */
    public static boolean secondsUntilElementPresent(String selector, int seconds) {
        return secondsUntilElementPresent(Elements.element(selector), seconds);
    }

    /**
     * Wait until an element is present
     *
     * @param selector By selector to use
     * @param seconds  time to wait in seconds
     * @return true if element appeared/was already present
     */
    public static boolean secondsUntilElementPresent(By selector, int seconds) {
        if (selector == null) {
            return false;
        }
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverManager.getWebDriver(), seconds);
            wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            return true;
        } catch (NoSuchElementException e)  {
            logger.warn("Cannot locate an element using " + selector);
        } catch (Exception e) {
            String errorMessage = e.getMessage().contains("Build info") ? e.getMessage().split("Build info")[0] : e.getMessage();
            logger.warn("issue in waiting for element due to : " + errorMessage);
            logger.debug(Utils.listToString(Utils.getCallFromFunction(
                    "secondsUntilElementPresent"), "\n\t ", null) + ": " + selector.toString());
        }
        return false;
    }

    /**
     * Wait for an element to not be present
     *
     * @param selector String selector in format "page_name.element_name"
     * @param seconds  timeout in seconds
     * @return true if element went away/was not present
     */
    public static boolean secondsUntilElementNotPresent(String selector, int seconds) {
        return secondsUntilElementNotPresent(Elements.element(selector), seconds);
    }

    /**
     * Wait for an element to not be present
     *
     * @param selector By selector to use
     * @param seconds  timeout in seconds
     * @return true if element went away/was not present
     */
    public static boolean secondsUntilElementNotPresent(By selector, int seconds) {
        if (selector == null) {
            return false;
        }
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverManager.getWebDriver(), seconds);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception e) {
            String errorMessage = e.getMessage().contains("Build info") ? e.getMessage().split("Build info")[0] : e.getMessage();
            logger.warn(String.format("issue in waiting for element %s to not be present : %s ", selector.toString(), errorMessage));
            return false;
        }
    }

    /**
     * Wait for an element to be present, refresh the page if it does not appear and check again
     *
     * @param selector By selector to use
     */
    public static void untilElementPresentWithRefresh(By selector) {
        try {
            for (int i = 0; i < 2; i++) {
                if (secondsUntilElementPresent(selector, MainRunner.timeouts().untilElementPresent())) {
                    return;
                }
                Navigate.browserRefresh();
            }
        } catch (Exception ex) {
            logger.warn("error in locating element with selector: " + selector + " : " + ex.getMessage());
        }
    }

    /**
     * Try to wait for el1 to be present. If it is not present, wait for el2 to be present.
     *
     * @param el1 first By selector to use
     * @param el2 second By selector to use
     */
    public static void untilElementPresentWithRefresh(By el1, By el2) {
        try {
            int cnt = 0;
            while (!untilElementPresent(el1) && cnt++ < 1) {
                Navigate.browserRefresh();
            }
            cnt = 0;
            while (!untilElementPresent(el2) && cnt++ < 1) {
                Navigate.browserRefresh();
            }
        } catch (Exception ex) {
            logger.warn(String.format("Elements %s and %s are not present with error message : %s", el1.toString(), el2.toString(), ex.getMessage()));

        }
    }

    /**
     * Wait for the first element. If it is not present, refresh browser and click the second element.
     *
     * @param waitFor By selector to use
     * @param toClick By selector to use if waitFor does not appear
     */
    public static void untilElementPresentWithRefreshAndClick(By waitFor, By toClick) {
        logger.debug("wait for element: " + waitFor.toString() + " and click element: " + toClick.toString());
        try {
            for (int i = 0; i < 2; i++) {
                if (secondsUntilElementPresent(waitFor, MainRunner.timeouts().untilElementPresent())) {
                    Clicks.click(toClick);
                    return;
                }
            }
        } catch (Exception e) {
            logger.warn("error in click element with refresh due to : " + e.getMessage());
        }
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param selector      String selector in format "page_name.element_name"
     * @param attr          attribute to use
     * @param expectedValue value to wait for attribute to become
     */
    public static void attributeChanged(String selector, String attr, String expectedValue) {
        attributeChanged(Elements.element(selector), attr, expectedValue);
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param selector      By selector to use
     * @param attr          attribute to use
     * @param expectedValue value to wait for attribute to become
     */
    public static void attributeChanged(By selector, String attr, String expectedValue) {
        attributeChanged(Elements.findElement(selector), attr, expectedValue);
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param element       element to use
     * @param attr          attribute to use
     * @param expectedValue value to wait for attribute to become
     */
    public static void attributeChanged(WebElement element, String attr, String expectedValue) {
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverManager.getWebDriver(), RunConfig.timeout);

            wait.until(new ExpectedCondition<Boolean>() {
                private WebElement element;
                private String attr;
                private String expectedValue;

                private ExpectedCondition<Boolean> init(WebElement element, String attr, String expectedValue) {
                    this.element = element;
                    this.attr = attr;
                    this.expectedValue = expectedValue;
                    return this;
                }

                public Boolean apply(WebDriver driver) {
                    String enabled = element.getAttribute(this.attr);
                    logger.debug("wait: init = (" + expectedValue + "), enabled = (" + enabled + ")");
                    return enabled.matches(this.expectedValue);
                }
            }.init(element, attr, expectedValue));
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }
    }

    /**
     * Wait for any loading activities on the page to complete
     */
    public static void forPageReady() {
        forPageReady(null);
    }

    private static boolean waitDone = false;

    public static void setWaitDone() {
        waitDone = true;
    }

    public static void setWaitRequired() {
        waitDone = false;
    }

    /**
     * Wait for any loading activities and waits for page verify_page element to load (if provided)
     *
     * @param pageName page you expect to be loaded
     * @return true if page is loaded and pageName.verify_page element is loaded
     */
    public static boolean forPageReady(final String pageName) {
        // app loading is handled much better, there are already built-in waits in appium interactions
        // that work perfectly well. Sadly, not the same for the website.
        if (RunConfig.appTest || waitDone) {
            return true;
        }

        int waitTime = RunConfig.timeout;
        //final long ts = System.currentTimeMillis();
        try {
            new WebDriverWait(WebDriverManager.getWebDriver(), waitTime).until((WebDriver wDriver) -> {
                // Safari takes a bit to update to "loading" status after an action
                try {
                    if (StepUtils.safari()) {
                        Utils.threadSleep(200, null);
                    }
                    return animationDone() && ajaxDone() && isPageLoaded();
                } catch (Exception e) {
                    // IE likes to throw a lot of garbage exceptions, don't bother printing them out
                    if (!StepUtils.ie() && !StepUtils.safari()) {
                        logger.debug("Exception while checking for page ready : " + e.getMessage());
                    }
                    return false;
                }
            });
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }

        if (pageName != null) {
            By verifyElement = Elements.element(pageName + ".verify_page");
            if (verifyElement != null) {
                if (!untilElementPresent(verifyElement)) {
                    logger.warn("element " + verifyElement + " not on page " + pageName);
                }
            } else {
                logger.debug("page does not have element to validate page");
            }
        } else {
            logger.debug("No page name provided");
        }
        StepUtils.closeJQueryPopup();
        return true;
    }

    /**
     * Checks if any JQuery animations are currently running
     *
     * @return true if an animation is running
     */
    public static boolean animationDone() {
        if (StepUtils.safari()) {
            return true;
        }
        Object done = Navigate.execJavascript("return $(\":animated\").length == 0;");
        logger.trace("animation done js response : " + done);
        return done instanceof Boolean ? (Boolean) done : true;
    }

    /**
     * Checks if the document ready state is no longer loading
     *
     * @return true if page is loaded
     */
    public static boolean isPageLoaded() {
        String state = (String) Navigate.execJavascript("return document.readyState;");
        logger.trace("document ready state : " + state);
        return state.matches("complete|loaded|interactive");
    }

    /**
     * Checks if all ajax calls are complete
     *
     * @return true if no active ajax calls
     */
    public static boolean ajaxDone() {
        if (RunConfig.useAppium) {
            return true;
        }
        StepUtils.ajaxCheck = true;
        try {

            //below script returns either string or long value, so fetching the results conditionally to avoid type cast error
            Object jsResponse = Navigate.execJavascript("return jQuery.active;");
            logger.trace("response for jQuery active : " + jsResponse);
            Long queries;

            if (jsResponse instanceof Long) {
                queries = (Long) jsResponse;
            } else if (jsResponse instanceof String) {
                // this means either jquery is not on the current page or not working correctly
                String response = (String) jsResponse;
                return (response.startsWith("{\"hCode\"") || response.isEmpty());
            } else {
                logger.trace("Unable to get num ajax calls!");
                return true;
            }
            //logger.info("." + queries + " AJAX");

            // TEMPORARY - currently a bug in BCOM sign in, checkout, MEW search and MCOM VGC PDP page that leaves AJAX calls hanging
            WebDriverManager.getCurrentUrl();
            if ((StepUtils.bloomingdales() || StepUtils.MEW()) || (StepUtils.macys())) {
                // now order review page and pdp have 2 open.
                if (MainRunner.currentURL.matches(".*?(chkout|product).*?")) {
                    return queries <= 2;
                }
                if (MainRunner.currentURL.matches(".*?(signin|profile|myaccount|addressbook|shop).*?")
                        || (StepUtils.MEW() && MainRunner.currentURL.contains("/shop"))) {
                    return queries <= 1;
                }
            }
            // END TEMPORARY FIX

            return queries == 0;
        } finally {
            StepUtils.ajaxCheck = false;
        }
    }

    /**
     * Wait for an element to appear and then disappear again (such as a loading symbol)
     *
     * @param selector element path in format "page_name.element_name"
     */
    public static void forLoading(String selector) {
        forLoading(Elements.element(selector));
    }

    /**
     * Wait for an element to appear and then disappear again (such as a loading symbol)
     *
     * @param selector By selector to use
     */
    public static void forLoading(By selector) {
        logger.trace("selector used to verify for loading : " + selector);
        untilElementPresent(selector);
        untilElementNotPresent(selector);
    }

    /**
     * get page text
     *
     * @return page text content
     */
    public static String getPageText() {
        try {
            return Navigate.execJavascript("return document.body.textContent").toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
