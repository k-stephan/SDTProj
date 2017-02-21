package com.macys.sdt.framework.utils;

import com.google.gson.Gson;
import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.MainRunner;
import cucumber.api.Scenario;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static com.macys.sdt.framework.runner.MainRunner.appTest;

/**
 * This class contains page interaction and information methods to help write test steps.
 */
public abstract class StepUtils {

    /**
     * A regex string that will match allowed mobile devices
     */
    public static final String MOBILE_DEVICES = "(?i)Android|iPhone 6|Google Nexus [0-9](p|x)?|Samsung Galaxy S4|Android Emulator|Nexus [0-9]";

    /**
     * A regex string that will match allowed tablets
     */
    public static final String TABLETS = "(?i)ipad( 2)?|galaxy note 10.1|Google Nexus ([0-9][0-9]|[7-9])";

    /**
     * A regex string that will match allowed ipads
     */
    public static final String IPAD = "(?i)ipad( 2)?";

    /**
     * to track ajax check
     */
    public static boolean ajaxCheck = false;

    /**
     * to track Coremetrics HAR info
     */
    public static ArrayList<HarEntry> harBuffer = null;

    /**
     * Checks if using chrome
     *
     * @return true if using chrome
     */
    public static boolean chrome() {
        return MainRunner.browser.equalsIgnoreCase("chrome");
    }

    /**
     * Checks if using firefox
     *
     * @return true if using firefox
     */
    public static boolean firefox() {
        return MainRunner.browser.equalsIgnoreCase("firefox");
    }

    /**
     * Checks if using Internet Explorer
     *
     * @return true if using Internet Explorer
     */
    public static boolean ie() {
        return MainRunner.browser.equalsIgnoreCase("ie");
    }

    /**
     * Checks if using safari
     *
     * @return true if using safari
     */
    public static boolean safari() {
        return MainRunner.browser.equalsIgnoreCase("safari");
    }

    /**
     * Checks if using edge
     *
     * @return true if using edge browser
     */
    public static boolean edge() {
        return MainRunner.browser.equalsIgnoreCase("edge");
    }

    /**
     * Checks if currently on a production environment
     *
     * @return true if on a prod env
     */
    public static boolean prodEnv() {
        return MainRunner.url.contains(macys() ? "macys.com" : "bloomingdales.com");
    }

    /**
     * Checks if browser is on a macys page
     *
     * @return true if on macys website
     */
    public static boolean macys() {
        if (MainRunner.brand != null) {
            return MainRunner.brand.equalsIgnoreCase("mcom");
        }

        return MainRunner.url.matches(".*?(macys|mcom).*?");
    }

    /**
     * Checks if browser is on a bloomingdales page
     *
     * @return true if on bloomingdales website
     */
    public static boolean bloomingdales() {
        if (MainRunner.brand != null) {
            return MainRunner.brand.equalsIgnoreCase("bcom");
        }

        return MainRunner.url.matches(".*?(bloomingdales|bcom).*?");
    }

    /**
     * Checks if user is currently logged in
     *
     * @return true if user is currently logged in
     */
    public static boolean signedIn() {
        return Cookies.getCookieValue("SignedIn").equals("1");
    }

    /**
     * Checks if using a mobile device
     *
     * @return true if using a mobile device
     */
    public static boolean mobileDevice() {
        return MainRunner.device != null;
    }

    /**
     * Checks if on MEW
     *
     * @return true if on MEW
     */
    public static boolean MEW() {
        String url = url();
        if (url.isEmpty()) {
            url = MainRunner.url;
        }
        return url.matches(".*?m(2qa1)?\\.(qa[0-9][0-9]?code)?(macys|mcom|bcom|bloomingdales).*?");
    }

    /**
     * Checks if on mobile version due to device or website
     *
     * @return true if using a mobile device or on MEW
     */
    public static boolean mobile()  {
        return mobileDevice() || MEW();
    }

    /**
     * Checks if using a tablet device
     *
     * @return true if using a tablet device
     */
    public static boolean tablet() {
        return MainRunner.device != null && MainRunner.device.matches(TABLETS);
    }

    /**
     * Checks if using an IPAD
     *
     * @return true if using an IPAD
     */
    public static boolean ipad() {
        return MainRunner.device != null && MainRunner.device.matches(IPAD);
    }

    /**
     * checks if using an apple device
     *
     * @return true if using an apple device
     */
    public static boolean iOS() {
        return MainRunner.device != null && (MainRunner.device.toLowerCase().contains("ipad")
                || MainRunner.device.toLowerCase().contains("iphone"));
    }

    /**
     * Pauses the PageHangWatchDog
     */
    public static void pausePageHangWatchDog() {
        MainRunner.PageHangWatchDog.pause(true);
    }

    /**
     * Resumes the PageHangWatchDog
     */
    public static void resumePageHangWatchDog() {
        MainRunner.PageHangWatchDog.pause(false);
    }

    /**
     * Switches to frame using frame
     *
     * @param frame either "default" for default frame or selector in format "page_name.element_name"
     */
    public static void switchToFrame(String frame) {
        try {
            if (frame.equalsIgnoreCase("default")) {
                MainRunner.getWebDriver().switchTo().defaultContent();
            } else {
                MainRunner.getWebDriver().switchTo().frame(Elements.findElement(Elements.element(frame)));
            }
        } catch (NullPointerException e) {
            System.out.println("Frame " + frame + " does not exist.");
        }
    }

    //=======================================================================
    // UI steps
    //=======================================================================

    /**
     * Closes the popup window that loads on initial load of bloomingdales
     */
    public static void closeBcomPopup() {
        if (bloomingdales()) {
            Navigate.switchWindow(1);
            Navigate.switchWindowClose();
            //close feedback dialog
            if (MEW()) {
                Clicks.clickIfPresent("category_browse.close_feedback_dialog");
            }
        }
    }

    /**
     * Closes the mcom feedback popup
     */
    public static void closeMcomPopup() {
        if (macys() && !appTest) {
            // Customer Feedback Popup
            Clicks.clickIfPresent("home.feedback_popup_close");
            // Foresee Survey popup
            if (MEW()) {
                Clicks.clickIfPresent("home.mew_foresee_feedback_close");
            }
        }
    }

    /**
     * Closes any popup window present
     */
    public static void closePopup() {
        if (macys()) {
            closeMcomPopup();
        } else {
            closeBcomPopup();
        }
    }

    /**
     * Closes BCOM Loyalty Promotion Video popup present
     */
    public static void closeBcomLoyaltyPromotionVideoOverlay() {
        if (bloomingdales()){
            Wait.untilElementPresent(By.id("close-loyallist-video"));
            Clicks.clickIfPresent(By.id("close-loyallist-video"));
        }
    }

    /**
     * Closes an alert if present - if no alert, nothing happens
     */
    public static void closeAlert() {
        // safari doesn't support alerts
        if (safari()) {
            return;
        }

        MainRunner.closeAlert();
    }

    /**
     * Closes the tutorial overlay on MEW experience if present
     */
    public static void closeMewTutorial() {
        Clicks.clickIfPresent("home.tutorial_close");
    }

    /**
     * Checks if an error panel is visible
     *
     * @return true if error panel is visible
     */
    public static boolean isErrorPaneVisible() {
        try {
            WebElement errPane = Elements.findElement(By.className("errorPageMessagePanelHd"));
            if (errPane != null && errPane.isDisplayed() && errPane.getText().contains("Oops!")) {
                return true;
            }
        } catch (Exception e) {
            // exception means we couldn't find the error pane
        }
        return false;
    }

    /**
     * Checks if an element is present, then performs the given lambda/function if it is
     * <p>
     * ex: ifPresentDo("home.popup", () -&#62; click("home.popup_close"));<br>
     * This would be the code to check for a popup and then close it if it is present
     * </p>
     *
     * @param selector String selector in format "page_name.element_name"
     * @param toRun    Lambda/function reference to be run
     */
    public static void ifPresentDo(String selector, Runnable toRun) {
        if (Elements.elementPresent(selector)) {
            toRun.run();
        }
    }

    /**
     * Checks if browser is on a specific page by URL and element if given
     * <p>
     * Uses special page elements "url" and "verify_page" to check if currently on given page.
     * url is required. verify_page may be left out, but should be included whenever possible.
     * </p>
     *
     * @param name name of expected page
     * @return true if on page "name," otherwise false
     */
    public static boolean onPage(String name) {
        // Appium doesn't have a good way to get page/activity info (that we've found yet)
        if (appTest) {
            System.err.println("Use of onPage while testing an app - NOT SUPPORTED");
            return true;
        }

        ArrayList<String> expectedURLs = Elements.getValues(name + ".url");

        String currentURL = MainRunner.getCurrentUrl();
        if (MainRunner.debugMode) {
            System.err.println("---> OnPage call: " + name + "\nfound url: " + currentURL);
        }

        String verifyElementKey = name + ".verify_page";
        List<String> verifyElement = Elements.getValues(verifyElementKey);
        for (String expectedURL : expectedURLs) {
            if (!verifyElement.isEmpty() && expectedURL != null) {
                if (Elements.elementPresent(verifyElementKey) && currentURL.contains(expectedURL)) {
                    return true;
                }
            } else if (expectedURL != null && currentURL.contains(expectedURL)) {
                return true;
            }
        }
        if (MainRunner.debugMode) {
            if (verifyElement == null) {
                System.err.println("-->Error StepUtils.onPage(): No verify_page element defined in page: " + name);
            } else if (!Elements.elementPresent(verifyElementKey)) {
                System.err.println("-->Error StepUtils.onPage(): verify_page element for page " + name + " not present");
            }

            if (expectedURLs.size() == 0) {
                System.err.println("-->Error StepUtils.onPage(): No url element defined in page: " + name);
            } else {
                expectedURLs.forEach(expectedURL -> {
                    if (!currentURL.contains(expectedURL)) {
                        System.err.println("-->Error StepUtils.onPage(): Could not match expected url: " + expectedURL);
                    }
                });
            }
        }
        return false;
    }

    /**
     * Checks if browser is on any of a list of pages
     * <p>
     * Uses special page elements "url" and "verify_page" to check if currently on any of the pages listed
     * </p>
     *
     * @param names list of pages to check for
     * @return true if on one of the listed pages
     */
    public static boolean onPage(String... names) {
        closePopup();
        for (String name : names) {
            if (onPage(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Throws an exception if not on one of the listed pages
     * <p>
     * Uses special page elements "url" and "verify_page" to check if currently on any of the pages listed
     * </p>
     *
     * @param names names of all allowed pages
     * @throws Exceptions.EnvException thrown if not on one of the expected pages
     */
    public static void shouldBeOnPage(String... names) throws Exceptions.EnvException {
        Wait.forPageReady();
        // check each allowed page - short timeout to avoid waiting forever
        for (String name : names) {
            Wait.secondsUntilElementPresent(name + ".verify_page", 10);
            if (onPage(name)) {
                return;
            }
        }

        // give the first option some more time just to be sure
        Wait.secondsUntilElementPresent(names[0] + ".verify_page", 10);
        if (onPage(names[0])) {
            return;
        }

        String pages = "";
        for (String name : names)
            pages += " " + name.replace("_", " ") + ", ";
        pages = pages.substring(0, pages.length() - 2);
        throw new Exceptions.EnvException("ERROR - ENV: Not on pages: " + pages);
    }

    /**
     * Scrolls until a lazily loaded element is present
     *
     * @param selector String selector in format "page_name.element_name"
     */
    public static void scrollToLazyLoadElement(String selector) {
        Navigate.execJavascript("window.scrollTo(0, document.body.scrollHeight)");
        Wait.secondsUntilElementPresent(selector, 10);
    }

    /**
     * Gets the title of the current page
     *
     * @return title of the current page
     */
    public static String title() {
        return MainRunner.getWebDriver().getTitle();
    }

    /**
     * Finds the url of the current page OR name of current activity for Android
     * <p>
     * Returns an empty string on iOS as there's no useful info we can get from it (that we've found yet)
     * </p>
     *
     * @return the url of the current page
     */
    public static String url() {
        return MainRunner.currentURL;
    }

    //=======================================================================
    // non-ui public methods
    //=======================================================================

    /**
     * Stops any active loading on the page
     *
     * @return true if stop was successful
     */
    public static boolean stopPageLoad() {
        try {
            Navigate.execJavascript("window.stop()");
            Utils.threadSleep(500, null);
            String res = Navigate.execJavascript("return document.readyState").toString();
            return res != null && res.equals("complete") && Wait.ajaxDone();
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
        return false;
    }

    /**
     * Closes any open jquery popups
     *
     * @return true if a popup was closed
     */
    public static boolean closeJQueryPopup() {
        if (safari()) {
            return true;
        }

        String[] texts = new String[]{"some technical issues"};
        for (String text : texts) {
            try {
                boolean res = (boolean) Navigate.execJavascript(
                        "return $('div.rc-overlay-visible').text().contains('" + text + "')"
                );
                if (res) {
                    Navigate.execJavascript(
                            "$('div.rc-overlay-visible').find('button').click()"
                    );
                }
                return true;
            } catch (Exception ex) {
                //ignore failure, will return false anyway
            }
        }
        return false;
    }

    /**
     * Captures the browser window and saves to a specified file name
     *
     * @param fileName file name to save screenshot as
     */
    public static void browserScreenCapture(String fileName) {
        File imgFile = new File(MainRunner.logs + fileName);
        try {
            File scrFile = ((TakesScreenshot) MainRunner.getWebDriver()).getScreenshotAs(OutputType.FILE);
            boolean success = scrFile.renameTo(imgFile);
            if (!success) {
                System.err.println("Failed to rename screenshot file");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                Utils.desktopCapture(new FileOutputStream(imgFile));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Cannot capture desktop.");
            }
        }
    }

    /**
     * Takes a screenshot and saves to a specified file
     *
     * @param fileName file to save to
     * @throws Exception thrown if there's an error creating the screenshot
     */
    public static void desktopScreenCapture(File fileName) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        ImageIO.write(image, "jpg", fileName);
    }

    /**
     * Maximises the browser window
     */
    public static void maximizeWindow() {
        MainRunner.getWebDriver().manage().window().setPosition(new Point(0, 0));
    }

    /**
     * Minimizes the browser window
     */
    public static void minimizeWindow() {
        MainRunner.getWebDriver().manage().window().setPosition(new Point(-2000, 0));
    }

    /**
     * If analytics is enabled, collect http archive data from this step and analyze it
     */
    public void analyticsTest() {
        Har har = MainRunner.browsermobServer.newHar();
        if (MainRunner.analytics == null) {
            return;
        }
        int step = ScenarioHelper.getScenarioIndex();
        if (step == 0) {
            return;
        }

        ArrayList<HarEntry> entries = new ArrayList<>();
        try {
            for (HarEntry harEntry : har.getLog().getEntries()) {
                HarRequest request = harEntry.getRequest();
                if (request.getUrl().contains("cm?")) {
                    entries.add(harEntry);
                }
            }
            harBuffer = entries;
            MainRunner.analytics.analyze(ScenarioHelper.getScenarioInfo(), step, entries, ScenarioHelper.getLastStepResult());
        } catch (Throwable ex) {
            ex.printStackTrace();
            Assert.fail(MainRunner.analytics.getClass().getSimpleName() + " test failed: " + ex.getMessage());
        }
    }

    /**
     * If analytics is enabled, collect http archive data for the last step and get it
     */
    public static ArrayList getHarBuffer() {
        if (MainRunner.analytics == null) {
            return null;
        }

        return new Gson().fromJson(new Gson().toJson(harBuffer), ArrayList.class);
    }

    /**
     * Flushes all analytics data
     */
    public void flushAnalytics() {
        if (MainRunner.analytics == null) {
            return;
        }
        try {
            MainRunner.analytics.flush(ScenarioHelper.isScenarioPassed());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the browsermob HTTP Archive
     *
     * @return browsermob har
     */
    public Har getHar() {
        try {
            Har har = MainRunner.browsermobServer.getHar();
            har.writeTo(new File(ScenarioHelper.scenario.getName() + "." + ScenarioHelper.getScenarioIndex() + ".har"));
            return har;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * A class for creating and managing singleton scenarios and steps
     */
    public static class SingletonScenario extends Thread {
        private final static String TAG_SINGLETON = "@singleton";
        private static final long TIMEOUT_DURATION = 20 * 50 * 60 * 1000;
        private static final int PORT_SCENARIO = 9001;
        private static final int PORT_STEP = 9002;
        private static SingletonScenario singletonScenario;
        private Scenario scenario;
        private String lockName;
        private int lockType = PORT_SCENARIO;
        private ServerSocket lockSocket;

        /**
         * Creates a singleton scenario
         *
         * @param scenario scenario to make singleton
         * @throws Exception thrown if scenario creation fails
         */
        public SingletonScenario(Scenario scenario) throws Exception {
            this.scenario = scenario;
            this.lockName = this.scenario.getName();
            if (MainRunner.useSauceLabs || firefox() ||
                    !this.scenario.getSourceTagNames().contains(TAG_SINGLETON)) {
                return;
            }

            this.start();
            this.join();
        }

        /**
         * Creates a singleton scenario
         *
         * @param stepName name of scenario to make singleton
         * @throws Exception thrown if scenario creation fails
         */
        public SingletonScenario(String stepName) throws Exception {
            this.lockName = stepName;
            this.lockType = PORT_STEP;
            this.start();
            this.join();
        }

        /**
         * Creates a lock for a single step
         *
         * @param stepName name of step to lock
         */
        public static void createSteplock(String stepName) {
            try {
                singletonScenario = new SingletonScenario(stepName);
            } catch (Exception e) {
                System.err.println("-->Cannot create step singleton");
            }
        }

        /**
         * Releases the current step lock
         */
        public static void releaseSteplock() {
            if (singletonScenario != null) {
                singletonScenario.release();
            }
        }

        /**
         * Runs the current singleton scenario
         */
        public void run() {
            long ts = System.currentTimeMillis();
            boolean running;
            while (running = (System.currentTimeMillis() - ts < TIMEOUT_DURATION)) {
                try {
                    lockSocket = new ServerSocket(lockType);
                    if (lockType == PORT_SCENARIO) {
                        System.err.println("...SingletonScenario: SCENARIO locked: " + this.lockName);
                    } else {
                        System.err.println("...SingletonScenario: STEP locked: " + this.lockName);
                    }
                    break;
                } catch (Exception ex) {
                    //ignore
                }
                Utils.threadSleep(10 * 1000, "...SingletonScenario:waiting for lock: " + this.lockName + "...");
            }
            if (!running) {
                System.err.println("-->Exhausted SingletonScenario:waiting for lock: " + Utils.toDuration(TIMEOUT_DURATION));
            }
        }

        /**
         * Releases the current singleton scenario
         */
        public void release() {
            try {
                if (this.lockSocket != null) {
                    this.lockSocket.close();
                } else {
                    return;
                }

                if (this.isAlive()) {
                    this.interrupt();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.err.println("...SingletonScenario: lock is released: " + this.lockName);
        }
    }

    public static class SDTRunnable implements Runnable {
        protected Object[] params;

        public SDTRunnable(Object[] params) {
            this.params = params;
        }

        @Override
        public void run() {
        }
    }

}
