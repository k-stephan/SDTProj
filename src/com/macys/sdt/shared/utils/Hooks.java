package com.macys.sdt.shared.utils;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.framework.utils.analytics.DATagCollector;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import gherkin.formatter.model.Result;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.junit.Assert;

import java.util.Map;

import static com.macys.sdt.framework.runner.MainRunner.getIOSDriver;
import static com.macys.sdt.framework.utils.ScenarioHelper.*;

public class Hooks extends StepUtils {

    private SingletonScenario singletonScenario;
    private boolean keepBrowser = MainRunner.booleanExParam("keep_browser");
    private long scenarioStartTime;
    private long stepStartTime;
    private boolean scenarioSetupComplete = false;
    private boolean mewFixSet = false;
    private static boolean firstScenario = true;
    private static boolean foreseeFlag = true;

    private static void checkForErrorPage() {
        if (MainRunner.useAppium) {
            return;
        }

        String[] error_categories = {"Catalog - Not Available", "Site Unavailable", "Access Denied", "Not Found",
                "Back in a few", "Registry Error", "Registry Test", "Network Error", "Registry Create Account Confirm"};
        String current_browser_title = title().toLowerCase();
        for (String category : error_categories) {
            if (current_browser_title.contains(category.toLowerCase())) {
                Assert.fail("ERROR - ENV: " + category.toLowerCase() + " on URL: " + url() + "");
            }
        }
        if (current_browser_title.contains("Product - Not Available".toLowerCase())) {
            Assert.fail("ERROR - DATA: Product is Not Available on URL: " + url() + "");
        }
    }

    private Map getScenarioInfo(Scenario scenario) {
        for (Object key : MainRunner.features.keySet()) {
            String scenarioKey = key.toString();
            if (MainRunner.features.get(scenarioKey) != null) {
                Map scenarioInfo = MainRunner.features.get(scenarioKey);
                if (scenarioInfo.get("name").equals(scenario.getName())) {
                    return scenarioInfo;
                }
            }
        }
        return null;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        if (RunFeature.checkAborted()) {
            Assert.fail("Run has been aborted");
        }
        // make sure driver is initialized
        MainRunner.getWebDriver();

        scenarioStartTime = System.currentTimeMillis();
        init(scenario);
        Map sinfo = getScenarioInfo(scenario);
        String line = "";
        if (sinfo != null) {
            line = Utils.parseInt(sinfo.get("line"), -1) + " ";
        }
        System.out.println("\n\nScenario:" + line + scenario.getSourceTagNames() + " - " + scenario.getName());
        if (isScenarioOutline()) {
            System.out.println("Examples: " + getScenarioExamples());
        }
        try {
            singletonScenario = new SingletonScenario(scenario);
        } catch (Exception ex) {
            System.out.println("--> Error Common.beforeScenario(): " + ex.getMessage());
            singletonScenario = null;
        }

        if (MainRunner.tagCollection) {
            DATagCollector.start(scenario);
        }

        if (!MainRunner.browser.equals("none")) {
            Cookies.deleteAllCookies();
        }
        if (MainRunner.useAppium && !firstScenario) {
            if (iOS()) {
                IOSDriver driver = MainRunner.getIOSDriver();
                if (driver != null) {
                    driver.launchApp();
                }
            } else {
                AndroidDriver driver = MainRunner.getAndroidDriver();
                if (driver != null) {
                    driver.launchApp();
                }
            }
        }
        firstScenario = false;
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            this.flushAnalytics();
            if (MainRunner.tagCollection) {
                DATagCollector.flush();
            }
            if (scenario.isFailed()) {
                Result result = getFailedStepResult();
                String errorMsg = "Unknown";
                if (result != null) {
                    String error = result.getErrorMessage();
                    if (error != null) {
                        errorMsg = error.trim();
                    }
                }

                System.err.println("<--------------------->" + "\nFAILED SCENARIO: " + scenario.getName().trim());
                if (isScenarioOutline()) {
                    System.err.println("FAILED EXAMPLES: " + getScenarioExamples());
                }
                System.err.println("FAILED STEP: " + getScenarioStepName(getScenarioIndex() - 1).trim() + "\nERROR: " + errorMsg + "\n<--------------------->\n\n");
                if (errorMsg.startsWith("sdt.utils.StepUtils$ProductionException:") || errorMsg.startsWith("sdt.utils.StepUtils$SkipException:")) {
                    clearStepResult(-1);
                }
                checkForErrorPage();
                CommonUtils.checkProductUnavailability();
            }
            System.out.println("\n--> DURATION: " + Utils.toDuration(System.currentTimeMillis() - scenarioStartTime) + "\n\n\n\n");

        } finally {
            if (MEW()) {
                foreseeFlag = true;
            }

            if (MainRunner.useAppium) {
                AppiumDriver driver = MainRunner.getAppiumDriver();
                if (driver != null) {
                    driver.closeApp();
                }
            }

            if (singletonScenario != null) {
                singletonScenario.release();
                singletonScenario = null;
            }

            MainRunner.PageHangWatchDog.pause(false);

            TestUsers.releaseProductionCustomer();
            scenarioSetupComplete = false;

            if (!keepBrowser) {
                if (MainRunner.debugMode) {
                    MainRunner.resetDriver(isScenarioPassed());
                } else {
                    MainRunner.resetDriver(true);
                }
            }
        }
    }

    @Before("@Step")
    public void before_step() {
        // an extra result is added by every @Before hook. Need to adjust for this.
        if (!scenarioSetupComplete) {
            resetScenarioOffset();
            try {
                String stepName = getScenarioStepName(getScenarioIndex());
                // the first step will be step 0 and will start with "0:[lineNum] - [step name]"
                while (!stepName.startsWith("0")) {
                    incrementStepIndexOffset();
                    stepName = getScenarioStepName(getScenarioIndex());
                }
            } catch (NullPointerException e) {
                // not a problem
            }
            if (MEW()) {
                // check for foresee and close it
                Navigate.addAfterNavigation(() -> {
                    if (foreseeFlag) {
                        if (Wait.secondsUntilElementPresent("product_display.foresee", 5)) {
                            try {
                                // can't use click("..."), will cause infinite loop
                                MainRunner.getWebDriver().findElement(Elements.element("product_display.foresee_no_thanks")).click();
                                foreseeFlag = false;
                            } catch (Exception | Error e) {
                                // ignore any error/exception. Means popup was not present
                            }
                        }
                    }
                });
            }
            scenarioSetupComplete = true;
        }
        stepStartTime = System.currentTimeMillis();
        String stepName = getScenarioStepName(getScenarioIndex());
        System.out.println("\n--->Step " + stepName);

        closePopup();
    }

    @After("@Step")
    public void after_step(Scenario scenario) {
        if (!MainRunner.disableProxy) {
            this.analyticsTest();
            if (MainRunner.tagCollection) {
                DATagCollector.capture(getScenarioStepName(getScenarioIndex()));
            }
        }
        closePopup();
        if (scenario.isFailed()) {
            checkForErrorPage();
            CommonUtils.checkProductUnavailability();
        }
        if (MainRunner.PageHangWatchDog.timedOut) {
            resumePageHangWatchDog();
            Assert.fail("PageHangWatchDog timed out, failing test");
        }
        if (MEW() && !mewFixSet && Elements.elementPresent("home.sidebar_iframe")) {
            Navigate.addBeforeNavigation(CommonUtils::scrollDownPageWhenSidebarPresent);
            mewFixSet = true;
        }

        System.out.println("-->Step duration: " + Utils.toDuration(System.currentTimeMillis() - stepStartTime) + "\n");
    }


}
