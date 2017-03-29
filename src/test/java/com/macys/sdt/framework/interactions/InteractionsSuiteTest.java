package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.utils.StepUtilsInteractionsTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;

/**
 * Interactions Tests Suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ClicksTests.class,
        TextBoxesTests.class,
        DropDownsTests.class,
        ElementsTests.class,
        WaitTests.class,
        StepUtilsInteractionsTests.class,
        NavigateTests.class
})
public class InteractionsSuiteTest {

    private static boolean preCondition = false;
    private static String testPageUrl = null;

    public static boolean getPreCondition() {
        return preCondition;
    }

    public static String getTestPageUrl() {
        return testPageUrl;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            RunConfig.project = "framework";
            RunConfig.workspace = "";
            RunConfig.projectResourceDir = "src/test/java/com/macys/sdt/framework/resources";
            RunConfig.browser = "firefox";
            RunConfig.browserVersion = "";
            RunConfig.remoteOS = "Windows 7";
            RunConfig.timeout = 90;
            RunConfig.url = "http://ui-standards.herokuapp.com";
            WebDriverManager.getWebDriver();
            preCondition = true;
            File htmlFile = new File("src/test/java/com/macys/sdt/framework/resources/unit_test_page.html");
            if (htmlFile.exists()) {
                testPageUrl = "file://" + htmlFile.getAbsolutePath();
            }
            RunConfig.debugMode = true;
        } catch (Exception e) {
            System.err.println("-->Error - Test setUp:" + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            RunConfig.debugMode = false;
            if (WebDriverManager.driverInitialized()) {
                WebDriverManager.resetDriver(true);
            }
        } catch (Exception e) {
            System.err.println("-->Error - Test tearDown:" + e.getMessage());
        }
    }
}