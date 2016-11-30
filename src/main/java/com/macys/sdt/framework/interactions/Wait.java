package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.macys.sdt.framework.runner.MainRunner.appTest;
import static com.macys.sdt.framework.runner.MainRunner.useAppium;
import static com.macys.sdt.framework.utils.Utils.errLog;

/**
 * A collection of ways to wait for expected conditions
 */
public class Wait {

    public static boolean until(BooleanSupplier condition) {
        return until(condition, null);
    }

    public static boolean until(BooleanSupplier condition, Integer seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds != null ? seconds : 5);
            wait.until((WebDriver driver) -> condition.getAsBoolean());
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:until: " + condition + ": " + ex.getMessage());
            }
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
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementNotPresent(): " + selector.toString() + ": " + ex.getMessage());
            }
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
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);
            wait.until(ExpectedConditions.invisibilityOfAllElements(list));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementNotPresent(): " + ex.getMessage());
            }
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
        return secondsUntilElementPresent(Elements.element(selector), 5);
    }

    /**
     * Wait until an element is present
     *
     * @param selector By selector to use
     * @return true if element is present
     */
    public static boolean untilElementPresent(By selector) {
        return secondsUntilElementPresent(selector, 3);
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
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds);
            wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:secondsUntilElementPresent(): " +
                        Utils.listToString(Utils.getCallFromFunction("secondsUntilElementPresent"), "\n\t ", null) +
                        ": " + selector.toString());
            }
//            errLog.println(ex.getMessage());
            return false;
        }
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
            WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), seconds);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
            return true;
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:secondsUntilElementNotPresent(): " + selector.toString());
            }
//            errLog.println(ex.getMessage());
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
                if (secondsUntilElementPresent(selector, 3)) {
                    return;
                }
                Navigate.browserRefresh();
            }
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementPresentWithRefresh(): " + selector.toString() + ": " + ex.getMessage());
            }
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
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementPresentWithRefresh(): " + el1.toString() + ": " + el2.toString() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Wait for the first element. If it is not present, refresh browser and click the second element.
     *
     * @param waitFor  By selector to use
     * @param toClick  By selector to use if waitFor does not appear
     */
    public static void untilElementPresentWithRefreshAndClick(By waitFor, By toClick) {
        try {
            for (int i = 0; i < 2; i++) {
                if (secondsUntilElementPresent(waitFor, 3)) {
                    Clicks.click(toClick);
                    return;
                }
            }
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:untilElementPresentWithRefreshAndClick(): " + waitFor.toString() + ": " + toClick.toString());
            }
//            errLog.println(ex.getMessage());
        }
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param selector     String selector in format "page_name.element_name"
     * @param attr         attribute to use
     * @param initialValue value that should change
     */
    public static void attributeChanged(String selector, String attr, String initialValue) {
        attributeChanged(Elements.element(selector), attr, initialValue);
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param selector     By selector to use
     * @param attr         attribute to use
     * @param initialValue value that should change
     */
    public static void attributeChanged(By selector, String attr, String initialValue) {
        attributeChanged(Elements.findElement(selector), attr, initialValue);
    }

    /**
     * Wait until an element's attribute has changed
     *
     * @param element       element to use
     * @param attr          attribute to use
     * @param expectedValue value to wait for attribute to become
     */
    public static void attributeChanged(WebElement element, String attr, String expectedValue) {
        WebDriverWait wait = new WebDriverWait(MainRunner.getWebDriver(), MainRunner.timeout);

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
                if (MainRunner.debugMode) {
                    System.out.println("wait: init = (" + expectedValue + "), enabled = (" + enabled + ")");
                }
                return enabled.matches(this.expectedValue);
            }
        }.init(element, attr, expectedValue));
    }

    /**
     * Wait for any loading activities on the page to complete
     */
    public static void forPageReady() {
        forPageReady(null);
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
        if (appTest) {
            return true;
        }

        int waitTime = MainRunner.timeout;
        //final long ts = System.currentTimeMillis();
        //List<String> stacks = Utils.getCallFromFunction(".forPageReady(");
        //if (stacks.toString().contains(".StepUtils.click("))
        //    Utils.threadSleep(safari() ? 3000 : 500, null);
        //String stack = Utils.listToString(stacks, "\n\t ", new String[]{});
        //System.out.print("..." + new Date() + "-forPageReady():\n\t");

        new WebDriverWait(MainRunner.getWebDriver(), waitTime).until((WebDriver wDriver) -> {
            //System.out.print("\t: " + (System.currentTimeMillis() - ts));
            // Safari takes a bit to update to "loading" status after an action
            try {
                if (StepUtils.safari()) {
                    Utils.threadSleep(100, null);
                }
                return animationDone() && ajaxDone() && isPageLoaded();
            } catch (Exception e) {
                // IE likes to throw a lot of garbage exceptions, don't bother printing them out
                if (MainRunner.debugMode && !StepUtils.ie() && !StepUtils.safari()) {
                    System.out.println("Exception in forPageReady: ");
                    System.err.println(e.getMessage());
                }
                return false;
            }
        });


        if (pageName != null) {
            By verifyElement = Elements.element(pageName + ".verify_page");
            if (verifyElement != null) {
                untilElementPresent(verifyElement);
            }
        }

        StepUtils.closeJQueryPopup();
        //System.out.println(".exit");
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
        return done instanceof Boolean ? (Boolean)done : true;
    }

    /**
     * Checks if the document ready state is no longer loading
     *
     * @return true if page is loaded
     */
    public static boolean isPageLoaded() {
        String state = (String) Navigate.execJavascript("return document.readyState;");
        //System.out.print("." + ret);
        return state.matches("complete|loaded|interactive");
    }

    /**
     * Checks if all ajax calls are complete
     *
     * @return true if no active ajax calls
     */
    public static boolean ajaxDone() {
        if (useAppium) {
            return true;
        }
        StepUtils.ajaxCheck = true;
        Utils.redirectSErr();
        try {

            //below script returns either string or long value, so fetching the results conditionally to avoid type cast error
            Object jsResponse = Navigate.execJavascript("return jQuery.active;");
            Long queries;

            if (jsResponse instanceof Long) {
                queries = (Long) jsResponse;
            } else {
                System.err.println("Unable to get num ajax calls!");
                return true;
            }
            //System.out.print("." + queries + " AJAX");

            // TEMPORARY - currently a bug in BCOM sign in, checkout, MEW search and MCOM VGC PDP page that leaves AJAX calls hanging
            MainRunner.getCurrentUrl();
            if ((StepUtils.bloomingdales() || StepUtils.MEW()) || (StepUtils.macys() && MainRunner.currentURL.contains("product")))
                if (MainRunner.currentURL.matches(".*?(signin|chkout|profile|product).*?")
                        || (StepUtils.MEW() && MainRunner.currentURL.contains("/shop"))) {
                    return queries <= 1;
                }
            // END TEMPORARY FIX

            return queries == 0;
        } finally {
            Utils.resetSErr();
            StepUtils.ajaxCheck = false;
        }
    }

    /**
     * Wait for an element to appear and then disappear again (such as a loading symbol)
     *
     * @param selector By selector to use
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
        untilElementPresent(selector);
        untilElementNotPresent(selector);
    }

    private static String getPageText() {
        try {
            return Navigate.execJavascript("return document.body.textContent").toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
