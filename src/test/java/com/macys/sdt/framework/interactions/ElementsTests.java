package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.utils.Exceptions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;

import java.util.List;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class ElementsTests {

    @Test
    public void testFindElement() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assert.assertNotNull(Elements.findElement("ui_standards.verify_page"));
        Assert.assertNotNull(Elements.findElement("ui_standards.left_nav_links"));
        Assert.assertNull(Elements.findElement("ui_standards.not_present"));
    }

    @Test
    public void testFindElements() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        List<WebElement> webElements = Elements.findElements("ui_standards.left_nav_links");
        Assert.assertNotNull(webElements);
        Assert.assertTrue(webElements.size() > 1);
        Assert.assertFalse(Elements.findElements("ui_standards.left_nav_links", WebElement::isDisplayed).isEmpty());
        Assert.assertTrue(Elements.findElements("ui_standards.not_present").isEmpty());
    }

    @Test
    public void testGetElementAttribute() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assert.assertEquals("logo-label", Elements.getElementAttribute("ui_standards.verify_page", "id"));
        Assert.assertEquals("", Elements.getElementAttribute("ui_standards.verify_page", "name"));
        Assert.assertEquals("", Elements.getElementAttribute("ui_standards.not_present", "class"));
    }

    @Test
    public void testGetRandomElement() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        try {
            Assert.assertNotNull(Elements.getRandomElement("ui_standards.left_nav_links"));
            Assert.assertNotNull(Elements.getRandomElement(Elements.element("ui_standards.left_nav_links")));
            Assert.assertNotNull(Elements.getRandomElement(Elements.element("ui_standards.left_nav_links"), WebElement::isDisplayed));
        } catch (Exception e) {
            Assert.fail("Failed testGetRandomElement : " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetRandomElementNegative() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Elements.getRandomElement("ui_standards.not_present");
    }

    @Test
    public void testElementPresent() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assert.assertTrue(Elements.elementPresent("ui_standards.verify_page"));
        Assert.assertFalse(Elements.elementPresent("ui_standards.not_present"));
    }

    @Test
    public void testAnyPresent() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assert.assertTrue(Elements.anyPresent("ui_standards.left_nav_links"));
        Assert.assertFalse(Elements.anyPresent("ui_standards.not_present"));
    }

    @Test
    public void testElementShouldBePresent() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        try {
            Elements.elementShouldBePresent("ui_standards.verify_page");
            Elements.elementShouldBePresent(Elements.findElement("ui_standards.verify_page"));
            Elements.elementShouldBePresent(Elements.findElements("ui_standards.verify_page"));
        } catch (Exception e) {
            Assert.fail("Failed testElementShouldBePresent : " + e.getMessage());
        }
    }

    @Test(expected = Exceptions.EnvException.class)
    public void testElementShouldBePresentNegative() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Elements.elementShouldBePresent("ui_standards.not_present");
    }

    @Test
    public void testElementInView() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        // For now just testing should not throw any error, need to find out better way to test
        try {
            Elements.elementInView("ui_standards.verify_page");
            Elements.elementInView(Elements.element("ui_standards.verify_page"));
        } catch (Exception e) {
            Assert.fail("Failed testElementInView : " + e.getMessage());
        }
    }

    @Test
    public void testGetValues() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assert.assertFalse(Elements.getValues("ui_standards.verify_page").isEmpty());
        Assert.assertTrue(Elements.getValues("ui_standards.not_defined").isEmpty());
    }

    @Test
    public void testElement() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        By element = Elements.element("ui_standards.verify_page");
        Assert.assertNotNull(element);
        Assert.assertTrue(element instanceof ByAll);
        Assert.assertEquals("By.all({By.id: logo-label})", element.toString());

        By anotherElement = Elements.element("ui_standards.multi_value");
        Assert.assertNotNull(anotherElement);
        Assert.assertTrue(anotherElement instanceof ByAll);
        Assert.assertEquals("By.all({By.linkText: Some Text,By.name: someName})", anotherElement.toString());

        Assert.assertNull(Elements.element("ui_standards.not_defined"));
    }

    @Test
    public void testParamElement() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        By element = Elements.paramElement("ui_standards.param_element", "a1", "a2");
        Assert.assertNotNull(element);
        Assert.assertTrue(element instanceof ByAll);
        Assert.assertEquals("By.all({By.className: row_a1_a2})", element.toString());
        Assert.assertNull(Elements.paramElement("ui_standards.not_defined", "a1"));
    }

    @Test
    public void testGetText() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsSuiteTest.preCondition);
        Assert.assertEquals("Coded Styleguide", Elements.getText("ui_standards.verify_page"));
        Assert.assertEquals("null", Elements.getText("ui_standards.not_present"));
    }
}