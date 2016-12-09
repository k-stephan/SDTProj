package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ClicksTests.class, TextBoxesTests.class, DropDownsTests.class, ElementsTests.class})
public class InteractionsSuiteTest {

    static boolean preCondition = false;

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            MainRunner.project = "framework";
            MainRunner.workspace = "";
            MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";
            MainRunner.browser = "firefox";
            MainRunner.remoteOS = "Windows 7";
            MainRunner.timeout = 90;
            MainRunner.url = "http://ui-standards.herokuapp.com/";
            MainRunner.PageHangWatchDog.init();
            Navigate.visit(MainRunner.url);
            StepUtils.shouldBeOnPage("ui_standards");
            preCondition = true;
        } catch (Exception e) {
            System.err.println("-->Error - Test setUp:" + e.getMessage());
            try {
                MainRunner.getWebDriver().quit();
            } catch (Exception ignored) {
            }
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            MainRunner.getWebDriver().quit();
        } catch (Exception e) {
            System.err.println("-->Error - Test tearDown:" + e.getMessage());
        }
    }
}