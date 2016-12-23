package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * These test can be executed from InteractionsSuiteTest only, hence the class name is ending with 'Tests' instead of 'Test'
 */
public class TextBoxesTests {

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
    public void testTypeTextBox() throws Exception {
        String firstName = "First Name";
        TextBoxes.typeTextbox("unit_test_page.text_box", firstName);
        Assert.assertEquals(Elements.getElementAttribute("unit_test_page.text_box", "value"), firstName);
    }

    @Test
    public void testTypeTextNEnter() throws Exception {
        String firstName = "FirstName";
        TextBoxes.typeTextNEnter("unit_test_page.text_box", firstName);
        Assert.assertTrue(MainRunner.getCurrentUrl().contains("text=" + firstName));
    }
}
