package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Tests for Wait Interactions
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class WaitTests {

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.getPreCondition());
        Assume.assumeTrue(InteractionsSuiteTest.getTestPageUrl() != null);
    }

    @Before
    public void visitTestPage() {
        WebDriverManager.startWebDriver().get(InteractionsSuiteTest.getTestPageUrl());
    }

    @Test
    public void testUntil() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.div_show"));
        Clicks.click("unit_test_page.show_after_2s");
        Assert.assertTrue(Wait.until(()->Elements.elementPresent("unit_test_page.div_show")));
        Assert.assertTrue(Elements.elementPresent("unit_test_page.div_show"));
    }

    @Test
    public void testUntilNegative() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.div_show"));
        Assert.assertFalse(Wait.until(()->Elements.elementPresent("unit_test_page.div_show")));
    }

    @Test
    public void testUntilElementNotPresent() throws Exception {
        Assume.assumeTrue(Elements.elementPresent("unit_test_page.div_hide"));
        Clicks.click("unit_test_page.hide_after_2s");
        Assert.assertTrue(Wait.untilElementNotPresent("unit_test_page.div_hide"));
        Assert.assertFalse(Elements.elementPresent("unit_test_page.div_hide"));
    }

    @Test
    public void testUntilElementNotPresentNegative() throws Exception {
        Assume.assumeTrue(Elements.elementPresent("unit_test_page.div_hide"));
        int timeout = RunConfig.timeout;
        RunConfig.timeout = 2;
        Assert.assertFalse(Wait.untilElementNotPresent("unit_test_page.div_hide"));
        Assert.assertFalse(Wait.untilElementNotPresent(Elements.findElement("unit_test_page.div_hide")));
        RunConfig.timeout = timeout;
    }

    @Test
    public void testUntilElementNotPresentAll() throws Exception {
        Assume.assumeTrue(Elements.elementPresent("unit_test_page.div_hide"));
        List<WebElement> elementList = Elements.findElements("unit_test_page.div_hide");
        Clicks.click("unit_test_page.hide_after_2s");
        Assert.assertTrue(Wait.untilElementNotPresent(elementList));
        Assert.assertFalse(Elements.anyPresent("unit_test_page.div_hide"));
    }

    @Test
    public void testUntilElementPresent() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.div_show"));
        Clicks.click("unit_test_page.show_after_2s");
        Assert.assertTrue(Wait.untilElementPresent("unit_test_page.div_show"));
        Assert.assertTrue(Wait.untilElementPresent(Elements.element("unit_test_page.div_show")));
        Assert.assertTrue(Elements.elementPresent("unit_test_page.div_show"));
    }

    @Test
    public void testSecondsUntilElementPresentNegative() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.div_show"));
        Assert.assertFalse(Wait.secondsUntilElementPresent("unit_test_page.div_show", 2));
        Assert.assertFalse(Wait.secondsUntilElementPresent(Elements.element("unit_test_page.not_defined"), 2));
    }

    @Test
    public void testSecondsUntilElementPresentAndClick() throws Exception {
        Wait.secondsUntilElementPresentAndClick("unit_test_page.goto_button", 2);
        Assert.assertTrue(WebDriverManager.getCurrentUrl().contains("#button"));
    }

    @Test
    public void testSecondsUntilElementNotPresent() throws Exception {
        Assume.assumeTrue(Elements.elementPresent("unit_test_page.div_hide"));
        Clicks.click("unit_test_page.hide_after_2s");
        Assert.assertTrue(Wait.secondsUntilElementNotPresent("unit_test_page.div_hide", 4));
        Assert.assertFalse(Elements.elementPresent("unit_test_page.div_hide"));
    }

    @Test
    public void testSecondsUntilElementNotPresentNegative() throws Exception {
        Assume.assumeTrue(Elements.elementPresent("unit_test_page.div_hide"));
        int timeout = RunConfig.timeout;
        RunConfig.timeout = 2;
        Assert.assertFalse(Wait.secondsUntilElementNotPresent("unit_test_page.div_hide", 2));
        Assert.assertFalse(Wait.secondsUntilElementNotPresent(Elements.element("unit_test_page.nod_defined"), 2));
        RunConfig.timeout = timeout;
    }

    @Test
    public void testUntilElementPresentWithRefresh() throws Exception {
        Clicks.click("unit_test_page.hide_after_2s");
        Assume.assumeTrue(Wait.untilElementNotPresent("unit_test_page.div_hide"));
        Wait.untilElementPresentWithRefresh(Elements.element("unit_test_page.div_hide"));
        Assert.assertTrue(Elements.elementPresent("unit_test_page.div_hide"));
    }

    @Test
    public void testUntilElementPresentWithRefreshAny() throws Exception {
        Clicks.click("unit_test_page.hide_after_2s");
        Assume.assumeTrue(Wait.untilElementNotPresent("unit_test_page.div_show"));
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.div_show"));
        Wait.untilElementPresentWithRefresh(Elements.element("unit_test_page.div_show"), Elements.element("unit_test_page.div_hide"));
        Assert.assertTrue(Elements.elementPresent("unit_test_page.div_hide"));
    }

    @Test
    public void testUntilElementPresentWithRefreshAndClick() throws Exception {
        Wait.untilElementPresentWithRefreshAndClick(Elements.element("unit_test_page.div_hide"), Elements.element("unit_test_page.hide_after_2s"));
        Assert.assertTrue(Wait.untilElementNotPresent("unit_test_page.div_hide"));
    }

    @Test
    public void testAttributeChanged() throws Exception {
        Assume.assumeTrue(Elements.getElementAttribute("unit_test_page.change_attribute", "name").equals("oldName"));
        Clicks.click("unit_test_page.change_attribute");
        try {
            Wait.attributeChanged("unit_test_page.change_attribute", "name", "newName");
        } catch (Exception e) {
            Assert.fail("Failed testAttributeChanged : " + e.getMessage());
        }
    }

    @Test
    public void testForLoading() throws Exception {
        Clicks.click("unit_test_page.start_loading");
        try {
            Wait.forLoading("unit_test_page.loader");
        } catch (Exception e){
            Assert.fail("Failed testForLoading : " + e.getMessage());
        }
    }
}