package com.macys.sdt.framework.interactions;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;

public class DropDownsTests {

    @Test
    public void testSelectByText() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Dropdown element not present - Ignoring SelectByText Test", Wait.untilElementPresent("ui_standards.drop_down"));
        DropDowns.selectByText("ui_standards.drop_down", "Visa");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("ui_standards.drop_down")) , "Visa");
        DropDowns.selectByText(Elements.element("ui_standards.drop_down"), "Macy's");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("ui_standards.drop_down")) , "Macy's");
    }

    @Test
    public void testSelectByIndex() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Dropdown element not present - Ignoring selectByIndex Test", Wait.untilElementPresent("ui_standards.drop_down"));
        DropDowns.selectByIndex("ui_standards.drop_down",2);
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("ui_standards.drop_down")) , "Macy's American Express");
    }

    @Test
    public void testSelectByValue() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Dropdown element not present - Ignoring selectByValue Test", Wait.untilElementPresent("ui_standards.drop_down"));
        DropDowns.selectByValue("ui_standards.drop_down","Y");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("ui_standards.drop_down")) , "Macy's");
    }

    @Test
    public void testSelectRandomValue() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Dropdown element not present - Ignoring selectRandomValue Test", Wait.untilElementPresent("ui_standards.drop_down"));
        String selected = DropDowns.selectRandomValue("ui_standards.drop_down");
        Assert.assertEquals(DropDowns.getSelectedValue(Elements.element("ui_standards.drop_down")) , selected);
    }

    @Test
    public void testGetAllValues() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Dropdown element not present - Ignoring getAllValues Test", Wait.untilElementPresent("ui_standards.drop_down"));
        List<String> options = DropDowns.getAllValues("ui_standards.drop_down");
        Assert.assertNotNull(options);
        Assert.assertFalse(options.isEmpty());
    }
}

