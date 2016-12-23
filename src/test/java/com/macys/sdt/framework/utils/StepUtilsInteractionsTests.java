package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.InteractionsSuiteTest;
import com.macys.sdt.framework.runner.MainRunner;
import org.junit.*;
import org.openqa.selenium.Point;

import java.io.File;
import java.util.ArrayList;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class StepUtilsInteractionsTests {

    static boolean forTestIfPresentDo = false;

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.getPreCondition());
        Assume.assumeTrue(InteractionsSuiteTest.getTestPageUrl() != null);
    }

    @Before
    public void visitTestPage() {
        MainRunner.getWebDriver().get(InteractionsSuiteTest.getTestPageUrl());
    }

    @Test
    public void testSwitchToFrame() throws Exception {
        StepUtils.switchToFrame("unit_test_page.frame");
        Assert.assertEquals("Inside Frame", Elements.getText("unit_test_page.frame_content"));
        StepUtils.switchToFrame("default");
    }

    @Test
    public void testClosePopup() throws Exception {
        Clicks.click("unit_test_page.open_popup_window");
        Assume.assumeTrue(new ArrayList<>(MainRunner.getWebDriver().getWindowHandles()).size() == 2);
        MainRunner.brand = "bcom";
        StepUtils.closePopup();
        MainRunner.brand = null;
        Assert.assertEquals(1, new ArrayList<>(MainRunner.getWebDriver().getWindowHandles()).size());
        Assert.assertEquals("SDT Framework Interactions Unit Testing", StepUtils.title());
    }

    @Test
    public void testCloseAlert() throws Exception {
        Clicks.click("unit_test_page.open_alert");
        StepUtils.closeAlert();
        Assert.assertEquals("SDT Framework Interactions Unit Testing", StepUtils.title());
    }

    @Test
    public void testIfPresentDo() throws Exception {
        forTestIfPresentDo = false;
        StepUtils.ifPresentDo("unit_test_page.verify_page", () -> forTestIfPresentDo = true);
        Assert.assertTrue(forTestIfPresentDo);
    }

    @Test
    public void testOnPage() throws Exception {
        Assert.assertTrue(StepUtils.onPage("unit_test_page"));
        Assert.assertFalse(StepUtils.onPage("ui_standards"));
        Assert.assertTrue(StepUtils.onPage("ui_standards", "unit_test_page"));
    }

    @Test
    public void testShouldBeOnPage() throws Exception {
        try {
            StepUtils.shouldBeOnPage("unit_test_page");
        } catch (Exception e) {
            Assert.fail("Failed testShouldBeOnPage : " + e.getMessage());
        }
    }

    @Test(expected = Exceptions.EnvException.class)
    public void testShouldBeOnPageNegative() throws Exception {
        StepUtils.shouldBeOnPage("ui_standards");
    }

    @Test
    public void testScrollToLazyLoadElement() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.lazy_load"));
        StepUtils.scrollToLazyLoadElement("unit_test_page.lazy_load");
        Assert.assertTrue(Elements.elementPresent("unit_test_page.lazy_load"));
    }

    @Test
    public void testTitle() throws Exception {
        Assert.assertEquals("SDT Framework Interactions Unit Testing", StepUtils.title());
    }

    @Test
    public void testUrl() throws Exception {
        MainRunner.getCurrentUrl();
        Assert.assertEquals(InteractionsSuiteTest.getTestPageUrl(), StepUtils.url());
    }

    @Test
    public void testStopPageLoad() throws Exception {
        Assert.assertTrue(StepUtils.stopPageLoad());
    }

    @Test
    public void testBrowserScreenCapture() throws Exception {
        MainRunner.logs = "logs/";
        Utils.createDirectory(MainRunner.logs, false);
        String fileName = "testBrowserScreenCapture.png";
        StepUtils.browserScreenCapture(fileName);
        Assert.assertTrue(new File(MainRunner.logs + fileName).exists());
    }

    @Test
    public void testMaximizeWindow() throws Exception {
        try {
            Point originalPosition = MainRunner.getWebDriver().manage().window().getPosition();
            StepUtils.maximizeWindow();
            MainRunner.getWebDriver().manage().window().setPosition(originalPosition);
        } catch (Exception e) {
            Assert.fail("Failed testMaximizeWindow :" + e.getMessage());
        }
    }

    @Test
    public void testMinimizeWindow() throws Exception {
        try {
            Point originalPosition = MainRunner.getWebDriver().manage().window().getPosition();
            StepUtils.minimizeWindow();
            MainRunner.getWebDriver().manage().window().setPosition(originalPosition);
        } catch (Exception e) {
            Assert.fail("Failed testMaximizeWindow :" + e.getMessage());
        }
    }
}
