package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class InteractionsTest {

    @BeforeClass
    public static void setUp() throws Exception {
        boolean preCondition = false;
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
        }
        catch (Exception e) {
            System.err.println("-->Error - Test setUp:" + e.getMessage());
            try {
                MainRunner.getWebDriver().quit();
            }
            catch (Exception ignored) {
            }
        }
        Assume.assumeTrue("Not navigated to ui_standards page - Ignoring all Interactions Test", preCondition);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            MainRunner.getWebDriver().quit();
        }
        catch (Exception e) {
            System.err.println("-->Error - Test tearDown:" + e.getMessage());
        }
    }

//    @Test
    public void testClick() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring Click Test", Wait.untilElementPresent("ui_standards.buttons_link"));
        Clicks.click("ui_standards.buttons_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.primary_buttons_link"));
    }

//    @Test
    public void testJavascriptClick() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring Javascript Click Test", Wait.untilElementPresent("ui_standards.forms_link"));
        Clicks.javascriptClick("ui_standards.forms_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.dropdown_select_menu_link"));
    }

//    @Test
    public void testTypeTextBox() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring TypeTextBox Test", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        String firstName = "First Name";
        TextBoxes.typeTextbox("ui_standards.first_name_text_box", firstName);
        Assert.assertEquals(Elements.getElementAttribute("ui_standards.first_name_text_box", "value"), firstName);
    }

//    @Test
    public void testTypeTextNEnter() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring TypeTextNEnter Test", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        String firstName = "First Name";
        TextBoxes.typeTextNEnter("ui_standards.first_name_text_box", firstName);
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.error_msg"));
        Assert.assertEquals(Elements.getElementAttribute("ui_standards.first_name_text_box", "value"), firstName);
    }
}