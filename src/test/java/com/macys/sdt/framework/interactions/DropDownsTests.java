package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Tests for DropDowns Interactions
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class DropDownsTests {

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
    public void testSelectByText() throws Exception {
        DropDowns.selectByText("unit_test_page.dropdown", "Option 2");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("unit_test_page.dropdown")), "Option 2");
        DropDowns.selectByText(Elements.element("unit_test_page.dropdown"), "Option 3");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("unit_test_page.dropdown")), "Option 3");
    }

    @Test
    public void testSelectByIndex() throws Exception {
        DropDowns.selectByIndex("unit_test_page.dropdown", 2);
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("unit_test_page.dropdown")), "Option 3");
    }

    @Test
    public void testSelectByValue() throws Exception {
        DropDowns.selectByValue("unit_test_page.dropdown", "2");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("unit_test_page.dropdown")), "Option 2");
    }

    @Test
    public void testSelectRandomValue() throws Exception {
        String selected = DropDowns.selectRandomValue("unit_test_page.dropdown");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("unit_test_page.dropdown")), selected);
    }

    @Test
    public void testGetAllValues() throws Exception {
        List<String> options = DropDowns.getAllValues("unit_test_page.dropdown");
        Assert.assertNotNull(options);
        Assert.assertFalse(options.isEmpty());
    }

    @Test
    public void testSelectCustomText() throws Exception {
        String value = "August";
        DropDowns.selectCustomText("unit_test_page.custom_dropdown", "unit_test_page.custom_dropdown_options", value);
        Assert.assertEquals(value, Elements.getText("unit_test_page.custom_dropdown_value"));
    }

    @Test
    public void testSelectCustomValue() throws Exception {
        DropDowns.selectCustomValue("unit_test_page.custom_dropdown", "unit_test_page.custom_dropdown_options", 4);
        Assert.assertEquals("April", Elements.getText("unit_test_page.custom_dropdown_value"));
    }

    @Test
    public void testGetAllCustomValues() throws Exception {
        List<String> options = DropDowns.getAllCustomValues("unit_test_page.custom_dropdown", "unit_test_page.custom_dropdown_options");
        Assert.assertNotNull(options);
        Assert.assertFalse(options.isEmpty());
    }
}

