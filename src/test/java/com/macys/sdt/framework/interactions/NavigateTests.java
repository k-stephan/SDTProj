package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class NavigateTests {

    static boolean forRunnableTest;

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.getPreCondition());
        Navigate.visit(MainRunner.url);
        Wait.forPageReady();
        Assume.assumeTrue(StepUtils.onPage("ui_standards"));
    }

    @Test
    public void testBrowserBack() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring BrowserBack Test", Wait.untilElementPresent("ui_standards.sample_form_link"));
        Clicks.javascriptClick("ui_standards.sample_form_link");
        Assume.assumeTrue(Wait.until(() -> MainRunner.getCurrentUrl().contains("prototyping/index_valid.html")));
        Navigate.browserBack();
        Assert.assertTrue(Wait.until(() -> MainRunner.getWebDriver().getTitle().contains("style guide")));
    }

    @Test
    public void testBrowserRefresh() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring BrowserRefresh Test", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        String firstName = "First Name";
        TextBoxes.typeTextbox("ui_standards.first_name_text_box", firstName);
        Assert.assertEquals(Elements.getElementAttribute("ui_standards.first_name_text_box", "value"), firstName);
        Navigate.browserRefresh();
        Assume.assumeTrue("Test element not present - Ignoring BrowserRefresh Test", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        Assert.assertEquals(Elements.getElementAttribute("ui_standards.first_name_text_box", "value"), "");
    }

    @Test
    public void testBrowserReset() throws Exception {
        WebDriver webDriver = MainRunner.getWebDriver();
        Navigate.browserReset();
        Assert.assertFalse(webDriver.equals(MainRunner.getWebDriver()));
        Navigate.visit("ui_standards");
        Wait.forPageReady();
        Assert.assertTrue(StepUtils.onPage("ui_standards"));
    }

    @Test
    public void testExecJavascript() throws Exception {
        Navigate.execJavascript("window.scrollTo(0, document.body.scrollHeight)");
        Assume.assumeTrue("Test element not present - Ignoring scrollPage Test", Wait.untilElementPresent("ui_standards.copyright"));
        Assert.assertTrue(Elements.isElementInView(Elements.findElement("ui_standards.copyright")));
        Navigate.scrollPage(0,-1000);
        Assert.assertFalse(Elements.isElementInView(Elements.findElement("ui_standards.copyright")));
    }

    @Test
    public void testFindIndexOfWindow() throws Exception {
        Assume.assumeTrue("Test element not present - Ignoring FindIndexOfWindow Test", Wait.untilElementPresent("ui_standards.header_comp_button"));
        Clicks.javascriptClick("ui_standards.header_comp_button");
        Assert.assertEquals(Navigate.findIndexOfWindow("Third Party Header Component"), 1);
        Assert.assertTrue(Navigate.switchWindow(1).getTitle().equalsIgnoreCase("Third Party Header Component"));
        Navigate.switchWindowClose();
        Assert.assertTrue(MainRunner.getWebDriver().getTitle().contains("style guide"));
    }

    @Test
    public void testBeforeNavigation() throws Exception {
        forRunnableTest = false;
        Runnable runnable = Navigate.addBeforeNavigation(() -> forRunnableTest = true);
        Assert.assertFalse(forRunnableTest);
        Navigate.runBeforeNavigation();
        Assert.assertTrue(forRunnableTest);

        forRunnableTest = false;
        Navigate.removeBeforeNavigation(runnable);
        Navigate.runBeforeNavigation();
        Assert.assertFalse(forRunnableTest);

        Navigate.addBeforeNavigation(runnable);
        Navigate.removeAllBeforeNavigation();
        Navigate.runBeforeNavigation();
        Assert.assertFalse(forRunnableTest);

        Navigate.addBeforeNavigation(runnable);
        Navigate.removeBeforeNavigation(0);
        Navigate.runBeforeNavigation();
        Assert.assertFalse(forRunnableTest);

        Navigate.addBeforeNavigation(runnable);
        Navigate.clearNavigationMethods();
        Navigate.runBeforeNavigation();
        Assert.assertFalse(forRunnableTest);
    }

    @Test
    public void testAfterNavigation() throws Exception {
        forRunnableTest = false;
        Runnable runnable = Navigate.addAfterNavigation(() -> forRunnableTest = true);
        Assert.assertFalse(forRunnableTest);
        Navigate.runAfterNavigation();
        Assert.assertTrue(forRunnableTest);

        forRunnableTest = false;
        Navigate.removeAfterNavigation(runnable);
        Navigate.runAfterNavigation();
        Assert.assertFalse(forRunnableTest);

        Navigate.addAfterNavigation(runnable);
        Navigate.removeAllAfterNavigation();
        Navigate.runAfterNavigation();
        Assert.assertFalse(forRunnableTest);

        Navigate.addAfterNavigation(runnable);
        Navigate.removeAfterNavigation(0);
        Navigate.runAfterNavigation();
        Assert.assertFalse(forRunnableTest);

        Navigate.addAfterNavigation(runnable);
        Navigate.clearNavigationMethods();
        Navigate.runAfterNavigation();
        Assert.assertFalse(forRunnableTest);
    }
}