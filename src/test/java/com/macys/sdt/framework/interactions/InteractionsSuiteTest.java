package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ClicksTests.class,
        TextBoxesTests.class,
        DropDownsTests.class,
        ElementsTests.class,
        WaitTests.class,
        NavigateTests.class
})
public class InteractionsSuiteTest {

    static boolean preCondition = false;
    static String testPageUrl = null;

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            MainRunner.project = "framework";
            MainRunner.workspace = "";
            MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";
            MainRunner.browser = "firefox";
            MainRunner.remoteOS = "Windows 7";
            MainRunner.timeout = 90;
            MainRunner.url = "http://ui-standards.herokuapp.com";
            MainRunner.PageHangWatchDog.init();
            Navigate.visit(MainRunner.url);
            StepUtils.shouldBeOnPage("ui_standards");
            preCondition = true;
            File htmlFile = new File("src/test/java/com/macys/sdt/framework/resources/unit_test_page.html");
            if (htmlFile.exists()) {
                testPageUrl = "file://" + htmlFile.getAbsolutePath();
            }
        } catch (Exception e) {
            System.err.println("-->Error - Test setUp:" + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            if (MainRunner.driverInitialized()) {
                MainRunner.getWebDriver().quit();
            }
        } catch (Exception e) {
            System.err.println("-->Error - Test tearDown:" + e.getMessage());
        }
    }
}