package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class ClicksTests {

    @Test
    public void testClick() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring Click Test", Wait.untilElementPresent("ui_standards.buttons_link"));
        Clicks.click("ui_standards.buttons_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.primary_buttons_link") || MainRunner.getCurrentUrl().contains("#Buttons"));
    }

    @Test
    public void testJavascriptClick() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring Javascript Click Test", Wait.untilElementPresent("ui_standards.forms_link"));
        Clicks.javascriptClick("ui_standards.forms_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.dropdown_select_menu_link") || MainRunner.getCurrentUrl().contains("#Forms"));
    }

    @Test
    public void testSelectCheckbox() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring SelectCheckbox Test", Wait.untilElementPresent("ui_standards.check_box"));
        Clicks.click("ui_standards.check_box_label");
        Clicks.selectCheckbox("ui_standards.check_box");
        Assert.assertTrue(Elements.findElement("ui_standards.check_box").isSelected());
    }

    @Test
    public void testUnSelectCheckbox() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring UnSelectCheckbox Test", Wait.untilElementPresent("ui_standards.check_box"));
        Clicks.click("ui_standards.check_box_label");
        Clicks.unSelectCheckbox("ui_standards.check_box");
        Assert.assertFalse(Elements.findElement("ui_standards.check_box").isSelected());
    }

    @Test
    public void testClickIfPresent() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring ClickIfPresent Test", Wait.untilElementPresent("ui_standards.foundation_link"));
        Clicks.clickIfPresent("ui_standards.foundation_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.foundation_components_link") || MainRunner.getCurrentUrl().contains("#Foundation"));
        try {
            Clicks.clickIfPresent("ui_standards.not_present");
        } catch (Exception e) {
            Assert.fail("Failed testClickIfPresent : " + e.getMessage());
        }
    }

    @Test
    public void testClickWhenPresent() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring ClickWhenPresent Test", Wait.untilElementPresent("ui_standards.accessibility_link"));
        Clicks.clickWhenPresent("ui_standards.accessibility_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.accessibility_remarks_link") || MainRunner.getCurrentUrl().contains("#Accessibility"));
    }
}
