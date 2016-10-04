package com.macys.sdt.framework.interactions;


import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;

/**
 * A collection of ways to navigate between pages and handle navigation
 */
public class Navigate {
    private static ArrayList<Runnable> beforeNavigate = new ArrayList<>();
    private static ArrayList<Runnable> afterNavigate = new ArrayList<>();

    /**
     * Adds a method (or lambda) to the list of methods to run before any clicks or other navigation
     *
     * @param toRun method or lambda to run
     * @return Runnable object which can be used to remove this method / lambda
     */
    public static Runnable addBeforeNavigation(Runnable toRun) {
        beforeNavigate.add(toRun);
        return toRun;
    }

    /**
     * Adds a method (or lambda) to the list of methods to run after any clicks or other navigation
     *
     * @param toRun method or lambda to run
     * @return Runnable object which can be used to remove this method / lambda
     */
    public static Runnable addAfterNavigation(Runnable toRun) {
        afterNavigate.add(toRun);
        return toRun;
    }

    /**
     * Removes a method (or lambda) from the list of methods to run before navigation
     *
     * @param toRemove method to remove
     */
    public static void removeBeforeNavigation(Runnable toRemove) {
        beforeNavigate.remove(toRemove);
    }

    /**
     * Removes a method (or lambda) from the list of methods to run after navigation
     *
     * @param toRemove method to remove
     */
    public static void removeAfterNavigation(Runnable toRemove) {
        afterNavigate.remove(toRemove);
    }

    /**
     * Removes a method (or lambda) from the list of methods to run before navigation
     *
     * @param toRemove index of method to remove
     */
    public static void removeBeforeNavigation(int toRemove) {
        beforeNavigate.remove(toRemove);
    }

    /**
     * Removes a method (or lambda) from the list of methods to run after navigation
     *
     * @param toRemove index of method to remove
     */
    public static void removeAfterNavigation(int toRemove) {
        afterNavigate.remove(toRemove);
    }

    /**
     * Runs all methods / lambdas that have been set to run before navigation
     */
    public static void runBeforeNavigation() {
        beforeNavigate.forEach(Runnable::run);
    }

    /**
     * Runs all methods / lambdas that have been set to run after navigation
     */
    public static void runAfterNavigation() {
        afterNavigate.forEach(Runnable::run);
        MainRunner.currentURL = MainRunner.getCurrentUrl();
    }

    /**
     * Removes all methods / lambdas that have been set to run before navigation
     */
    public static void removeAllBeforeNavigation() {
        beforeNavigate.clear();
    }

    /**
     * Removes all methods / lambdas that have been set to run after navigation
     */
    public static void removeAllAfterNavigation() {
        afterNavigate.clear();
    }

    /**
     * Removes any saved before or after navigation methods
     */
    public static void clearNavigationMethods() {
        beforeNavigate.clear();
        afterNavigate.clear();
    }

    /**
     * Navigates the browser back a page
     */
    public static void browserBack() {
        if (StepUtils.safari() || StepUtils.ie()) {
            try {
                int urlStackSize = MainRunner.URLStack.size();
                if (urlStackSize <= 1) {
                    visit("home");
                } else {
                    MainRunner.getWebDriver().get(MainRunner.URLStack.get(urlStackSize - 2));
                }
                Utils.threadSleep(1000, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MainRunner.getWebDriver().navigate().back();
        }
        Wait.forPageReady();
    }

    /**
     * Refreshes the current browser page
     */
    public static void browserRefresh() {
        if (StepUtils.safari() || StepUtils.ie()) {
            execJavascript("document.location.reload()");
            Utils.threadSleep(1000, null);
        } else {
            MainRunner.getWebDriver().navigate().refresh();
        }
        Wait.forPageReady();
    }

    /**
     * Restarts the browser session
     */
    public static void browserReset() {
        MainRunner.resetDriver(true);
        MainRunner.getWebDriver();
    }

    /**
     * Navigates to a page by url OR JSON page file name
     * <p>
     * Navigates to a url. If pageURL is not a valid url, it assumes it's a page json file name.
     * It uses the "url" element from the json file as relative path from "website" environment variable.
     * So if page_name.url = "/shop" and website = "www.macys.com" you will go to "www.macys.com/shop".
     * </p>
     *
     * @param pageURL either valid url or JSON page file name
     */
    public static void visit(String pageURL) {
        runBeforeNavigation();
        if (pageURL == null) {
            return;
        }

        try {
            String givenURL = MainRunner.url;
            // check if pageURL has product, then directly navigate to the given url
            if (pageURL.contains("product") && pageURL.contains("ID=") && !pageURL.startsWith("http://")) {
                pageURL = "http://" + pageURL;
            }

            if (!pageURL.startsWith("http")) {
                if (!(pageURL.matches(".*\\.url$"))) {
                    pageURL += ".url";
                }
                // grab the first one on the list (if there are multiple)
                String jsonURL = Elements.getValues(pageURL).get(0);

                if (givenURL == null) {
                    if (jsonURL == null) {
                        Assert.fail("No URL is given");
                    } else {
                        givenURL = jsonURL;
                    }
                } else {
                    if (jsonURL != null) {
                        givenURL = givenURL + jsonURL;
                    }
                }
            } else {
                givenURL = pageURL;
            }
            // selenium doesn't like links that don't start with http://
            if (!givenURL.startsWith("http://")) {
                givenURL = "http://" + givenURL;
            }

            System.out.println("...Loading " + givenURL);
            // start checking IE windows authentication popup
            if (StepUtils.ie() && MainRunner.booleanExParam("require_authentication")) {
                MainRunner.authenticationIeInit();
            }
            //Utils.ThreadWatchDog twd = new Utils.ThreadWatchDog(null, 60000, "ThreadWatchDog:visit(" + link + ")", () -> stopPageLoad());
            MainRunner.getWebDriver().get(givenURL);
            //twd.interrupt();
            Wait.forPageReady();
            StepUtils.closeAlert();
            if (MainRunner.analytics != null) {
                MainRunner.analytics.recordPageSource(givenURL, getPageSource());
            }
        } catch (Exception ex) {
            if (MainRunner.debugMode) {
                System.err.println("-->Error:StepUtils.visit(): " + pageURL + ": " + ex.getMessage());
            }
        }
        runAfterNavigation();
    }

    private static String getPageSource() {
        try {
            return execJavascript("return document.documentElement.outerHTML").toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Scrolls the web page by a specified amount
     *
     * @param horizontal distance to scroll horizontally (right is positive, left is negative)
     * @param vertical   distance to scroll vertically (down is positive, up is negative)
     */
    public static void scrollPage(int horizontal, int vertical) {
        execJavascript("window.scrollBy(" + horizontal + ", " + vertical + ")");
    }

    /**
     * Gets the index of the browser window with a given title
     *
     * @param title the title to look for
     * @return the index of the window, -1 if not found
     */
    public static int findIndexOfWindow(String title) {
        WebDriver driver = MainRunner.getWebDriver();
        ArrayList<String> newTabs = new ArrayList<>(driver.getWindowHandles());
        int index = -1;

        for (int i = 0; i < newTabs.size(); i++) {
            driver.switchTo().window(newTabs.get(i));
            Wait.forPageReady();
            if (driver.getTitle().contains(title)) {
                index = i;
                break;
            }
        }
        driver.switchTo().window(newTabs.get(0));
        return index;
    }

    /**
     * Switches browser to window at given index
     *
     * @param index index of window to switch to
     * @return WebDriver which can control the window
     */
    public static WebDriver switchWindow(int index) {
        ArrayList<String> newTab = new ArrayList<>(MainRunner.getWebDriver().getWindowHandles());
        if (newTab.size() > index) {
            MainRunner.getWebDriver().switchTo().window(newTab.get(index));
        }
        return MainRunner.getWebDriver();
    }

    /**
     * Closes the current window and switches to the base window
     */
    public static void switchWindowClose() {
        WebDriver driver = MainRunner.getWebDriver();
        ArrayList<String> newTab = new ArrayList<>(driver.getWindowHandles());
        if (newTab.size() > 1) {
            driver.close();
            driver.switchTo().window(newTab.get(0));
        }
    }

    /**
     * Executes javascript code using given arguments
     *
     * @param script String containing JS code
     * @param args   Any arguments to JS code in script
     * @return returned value of JS code (if any)
     */
    public static synchronized Object execJavascript(String script, Object... args) {
        if (!MainRunner.driverInitialized()) {
            return "";
        }

        JavascriptExecutor scriptExe = ((JavascriptExecutor) MainRunner.getWebDriver());
        try {
            //System.err.print("$");
            //System.out.print("StepUtils.execJavascript(): " + script + ":");
            return scriptExe.executeScript(script, args);
        } catch (Exception ex) {
            return "";
        }
    }
}
