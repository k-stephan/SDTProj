package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.List;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class WaitTests {

    static String testPageUrl;

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        File htmlFile = new File("src/test/java/com/macys/sdt/framework/resources/unit_test_page.html");
        Assume.assumeTrue(htmlFile.exists());
        testPageUrl = "file://" + htmlFile.getAbsolutePath();
        MainRunner.debugMode = true;
    }

    @AfterClass
    public static void tearDown() {
        MainRunner.debugMode = false;
        if (InteractionsSuiteTest.preCondition) {
            Navigate.visit(MainRunner.url);
        }
    }

    @Before
    public void visitTestPage() {
        MainRunner.getWebDriver().get(testPageUrl);
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
        int timeout = MainRunner.timeout;
        MainRunner.timeout = 2;
        Assert.assertFalse(Wait.untilElementNotPresent("unit_test_page.div_hide"));
        Assert.assertFalse(Wait.untilElementNotPresent(Elements.findElement("unit_test_page.div_hide")));
        MainRunner.timeout = timeout;
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
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#button"));
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
        int timeout = MainRunner.timeout;
        MainRunner.timeout = 2;
        Assert.assertFalse(Wait.secondsUntilElementNotPresent("unit_test_page.div_hide", 2));
        Assert.assertFalse(Wait.secondsUntilElementNotPresent(Elements.element("unit_test_page.nod_defined"), 2));
        MainRunner.timeout = timeout;
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
}