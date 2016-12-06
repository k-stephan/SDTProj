package com.macys.sdt.framework.interactions;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class TextBoxesTests {

    @Test
    public void testTypeTextBox() throws Exception {
        Assume.assumeTrue("Precondition not met.", TestSuite.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring TypeTextBox Test", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        String firstName = "First Name";
        TextBoxes.typeTextbox("ui_standards.first_name_text_box", firstName);
        Assert.assertEquals(Elements.getElementAttribute("ui_standards.first_name_text_box", "value"), firstName);
    }

    @Test
    public void testTypeTextNEnter() throws Exception {
        Assume.assumeTrue("Precondition not met.", TestSuite.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring TypeTextNEnter Test", Wait.untilElementPresent("ui_standards.first_name_text_box"));
        String firstName = "First Name";
        TextBoxes.typeTextNEnter("ui_standards.first_name_text_box", firstName);
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.error_msg"));
        Assert.assertEquals(Elements.getElementAttribute("ui_standards.first_name_text_box", "value"), firstName);
    }
}
