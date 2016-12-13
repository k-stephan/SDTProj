package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

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

   @Test(expected = NoSuchElementException.class)
    public void testClickNegative() throws Exception {
       Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
       WebElement element = Elements.findElement("ui_standards.not_present");
       Clicks.click(element);
   }

    @Test
    public void testClickRandomElement() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Navigate.visit(MainRunner.url);
        Assume.assumeTrue("Test element not present - Ignoring ClickRandomElement Test", Wait.untilElementPresent("ui_standards.left_nav_links"));
        Clicks.clickRandomElement("ui_standards.left_nav_links");
        Wait.untilElementPresent("ui_standards.left_sub_nav");
        Assert.assertTrue(Elements.anyPresent("ui_standards.left_sub_nav") || MainRunner.getCurrentUrl().contains("#"));
    }

    @Test
    public void testClickRandomElementPredicate() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Navigate.visit(MainRunner.url);
        Assume.assumeTrue("Test element not present - Ignoring ClickRandomElementPredicate Test", Wait.untilElementPresent("ui_standards.left_nav_links"));
        Clicks.clickRandomElement("ui_standards.left_nav_links", WebElement::isDisplayed);
        Wait.untilElementPresent("ui_standards.left_sub_nav");
        Assert.assertTrue(Elements.anyPresent("ui_standards.left_sub_nav") || MainRunner.getCurrentUrl().contains("#"));
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

    @Test
    public void testClickElementByText() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Navigate.visit(MainRunner.url);
        Assume.assumeTrue("Test element not present - Ignoring ClickElementByText Test", Wait.untilElementPresent("ui_standards.left_nav_links"));
        Clicks.clickElementByText("ui_standards.left_nav_links", "Accessibility");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.accessibility_remarks_link") || MainRunner.getCurrentUrl().contains("#Accessibility"));
    }

    @Test
    public void testSendEnter() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Navigate.visit(MainRunner.url);
        Assume.assumeTrue("TestDemo element not present - Ignoring SendEnter TestDemo", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        Clicks.sendEnter("ui_standards.first_name_text_box");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.error_msg") || MainRunner.getCurrentUrl().contains("profile.firstname="));
    }

    @Test
    public void testSendRandomEnter() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Navigate.visit(MainRunner.url);
        Assume.assumeTrue("TestDemo element not present - Ignoring SendRandomEnter TestDemo", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        Clicks.sendRandomEnter("ui_standards.first_name_text_box");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.error_msg") || MainRunner.getCurrentUrl().contains("profile.firstname="));
    }
}
