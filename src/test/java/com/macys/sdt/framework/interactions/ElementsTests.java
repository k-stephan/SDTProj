package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Exceptions;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;

import java.util.List;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class ElementsTests {

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
    public void testFindElement() throws Exception {
        Assert.assertNotNull(Elements.findElement("unit_test_page.heading"));
        Assert.assertNotNull(Elements.findElement("unit_test_page.links"));
        Assert.assertNull(Elements.findElement("unit_test_page.not_present"));
    }

    @Test
    public void testFindElements() throws Exception {
        List<WebElement> webElements = Elements.findElements("unit_test_page.links");
        Assert.assertNotNull(webElements);
        Assert.assertTrue(webElements.size() > 1);
        Assert.assertFalse(Elements.findElements("unit_test_page.links", WebElement::isDisplayed).isEmpty());
        Assert.assertTrue(Elements.findElements("unit_test_page.not_present").isEmpty());
    }

    @Test
    public void testGetElementAttribute() throws Exception {
        Assert.assertEquals("heading", Elements.getElementAttribute("unit_test_page.heading", "id"));
        Assert.assertEquals("", Elements.getElementAttribute("unit_test_page.heading", "name"));
        Assert.assertEquals("", Elements.getElementAttribute("unit_test_page.not_present", "class"));
    }

    @Test
    public void testGetRandomElement() throws Exception {
        try {
            Assert.assertNotNull(Elements.getRandomElement("unit_test_page.links"));
            Assert.assertNotNull(Elements.getRandomElement(Elements.element("unit_test_page.links")));
            Assert.assertNotNull(Elements.getRandomElement(Elements.element("unit_test_page.links"), WebElement::isDisplayed));
        } catch (Exception e) {
            Assert.fail("Failed testGetRandomElement : " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetRandomElementNegative() throws Exception {
        Elements.getRandomElement("unit_test_page.not_present");
    }

    @Test
    public void testElementPresent() throws Exception {
        Assert.assertTrue(Elements.elementPresent("unit_test_page.heading"));
        Assert.assertFalse(Elements.elementPresent("unit_test_page.not_present"));
    }

    @Test
    public void testAnyPresent() throws Exception {
        Assert.assertTrue(Elements.anyPresent("unit_test_page.links"));
        Assert.assertFalse(Elements.anyPresent("unit_test_page.not_present"));
    }

    @Test
    public void testElementShouldBePresent() throws Exception {
        try {
            Elements.elementShouldBePresent("unit_test_page.heading");
            Elements.elementShouldBePresent(Elements.findElement("unit_test_page.heading"));
            Elements.elementShouldBePresent(Elements.findElements("unit_test_page.heading"));
        } catch (Exception e) {
            Assert.fail("Failed testElementShouldBePresent : " + e.getMessage());
        }
    }

    @Test(expected = Exceptions.EnvException.class)
    public void testElementShouldBePresentNegative() throws Exception {
        Elements.elementShouldBePresent("unit_test_page.not_present");
    }

    @Test
    public void testElementInView() throws Exception {
        // For now just testing should not throw any error, need to find out better way to test
        try {
            Elements.elementInView("unit_test_page.heading");
            Elements.elementInView(Elements.element("unit_test_page.heading"));
        } catch (Exception e) {
            Assert.fail("Failed testElementInView : " + e.getMessage());
        }
    }

    @Test
    public void testGetValues() throws Exception {
        Assert.assertFalse(Elements.getValues("unit_test_page.heading").isEmpty());
        Assert.assertTrue(Elements.getValues("unit_test_page.not_defined").isEmpty());
    }

    @Test
    public void testElement() throws Exception {
        By element = Elements.element("unit_test_page.heading");
        Assert.assertNotNull(element);
        Assert.assertTrue(element instanceof ByAll);
        Assert.assertEquals("By.all({By.id: heading})", element.toString());

        By anotherElement = Elements.element("unit_test_page.multi_value");
        Assert.assertNotNull(anotherElement);
        Assert.assertTrue(anotherElement instanceof ByAll);
        Assert.assertEquals("By.all({By.linkText: Some Text,By.name: someName})", anotherElement.toString());

        Assert.assertNull(Elements.element("unit_test_page.not_defined"));
    }

    @Test
    public void testParamElement() throws Exception {
        By element = Elements.paramElement("unit_test_page.param_element", "a1", "a2");
        Assert.assertNotNull(element);
        Assert.assertTrue(element instanceof ByAll);
        Assert.assertEquals("By.all({By.className: row_a1_a2})", element.toString());
        Assert.assertNull(Elements.paramElement("unit_test_page.not_defined", "a1"));
    }

    @Test
    public void testGetText() throws Exception {
        Assert.assertEquals("SDT Framework Interactions Unit Testing", Elements.getText("unit_test_page.heading"));
        Assert.assertEquals("null", Elements.getText("unit_test_page.not_present"));
    }
}