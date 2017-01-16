package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Tests for Clicks Interactions
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class ClicksTests {

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
    public void testClick() throws Exception {
        Clicks.click("unit_test_page.goto_button");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#button"));
    }

   @Test(expected = NoSuchElementException.class)
    public void testClickNegative() throws Exception {
       WebElement element = Elements.findElement("unit_test_page.not_present");
       Clicks.click(element);
   }

    @Test
    public void testClickRandomElement() throws Exception {
        Clicks.clickRandomElement("unit_test_page.links");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#"));
    }

    @Test
    public void testClickRandomElementPredicate() throws Exception {
        Clicks.clickRandomElement("unit_test_page.links", WebElement::isDisplayed);
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#"));
    }

    @Test
    public void testJavascriptClick() throws Exception {
        Clicks.javascriptClick("unit_test_page.goto_text_box");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#text"));
    }

    @Test
    public void testRandomJavascriptClick() throws Exception {
        Clicks.randomJavascriptClick("unit_test_page.links");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#"));
    }

    @Test
    public void testSelectCheckbox() throws Exception {
        Clicks.click("unit_test_page.goto_checkbox");
        Clicks.selectCheckbox("unit_test_page.checkbox");
        Assert.assertTrue(Elements.findElement("unit_test_page.checkbox").isSelected());
        Clicks.unSelectCheckbox("unit_test_page.checkbox");
        Assert.assertFalse(Elements.findElement("unit_test_page.checkbox").isSelected());
    }

    @Test
    public void testClickIfPresent() throws Exception {
        Clicks.clickIfPresent("unit_test_page.goto_radio");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#radio"));
        try {
            Clicks.clickIfPresent("unit_test_page.not_present");
        } catch (Exception e) {
            Assert.fail("Failed testClickIfPresent : " + e.getMessage());
        }
    }

    @Test
    public void testClickWhenPresent() throws Exception {
        Assert.assertTrue(Clicks.clickWhenPresent("unit_test_page.goto_dropdown"));
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#select"));
        Assert.assertFalse(Clicks.clickWhenPresent("unit_test_page.not_present"));
    }

    @Test
    public void testClickElementByText() throws Exception {
        Clicks.clickElementByText("unit_test_page.links", "Go to image");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#image"));
    }

    @Test
    public void testSendEnter() throws Exception {
        Clicks.sendEnter("unit_test_page.text_box");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("text="));
    }

    @Test
    public void testSendRandomEnter() throws Exception {
        Clicks.sendRandomEnter("unit_test_page.text_box");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("text="));
    }

    @Test
    public void testHoverForSelection() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.paragraph"));
        Clicks.hoverForSelection("unit_test_page.heading");
        Assert.assertTrue(Elements.elementPresent("unit_test_page.paragraph"));
    }

    @Test
    public void testHover() throws Exception {
        try {
            Clicks.hover("unit_test_page.heading");
        } catch (Exception e) {
            Assert.fail("Failed testHover : " + e.getMessage());
        }
    }

    @Test
    public void testJavascriptHover() throws Exception {
        try {
            Clicks.javascriptHover(Elements.findElement("unit_test_page.heading"));
        } catch (Exception e) {
            Assert.fail("Failed testHover : " + e.getMessage());
        }
    }

    @Test
    public void testClickArea() throws Exception {
        Clicks.click("unit_test_page.goto_image");
        Clicks.clickArea("alt", "Sun");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#sun"));
    }

    @Test
    public void testClickLazyElement() throws Exception {
        Assume.assumeFalse(Elements.elementPresent("unit_test_page.lazy_load"));
        Clicks.clickLazyElement("unit_test_page.lazy_load");
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("#heading"));
    }

    @Test
    public void testClickWithExitCondition() throws Exception {
        Clicks.click(Elements.element("unit_test_page.show_after_2s"), () -> Wait.untilElementPresent("unit_test_page.div_show"));
        Assert.assertTrue(Elements.elementPresent("unit_test_page.div_show"));
    }

    @Test
    public void testClickWithPreCondition() throws Exception {
        Clicks.click("unit_test_page.show_after_2s");
        try {
            Clicks.click(() -> Wait.untilElementPresent("unit_test_page.div_show"), Elements.element("unit_test_page.div_show"));
        } catch (Exception e) {
            Assert.fail("Failed testClickWithPreCondition : " + e.getMessage());
        }
    }
}
