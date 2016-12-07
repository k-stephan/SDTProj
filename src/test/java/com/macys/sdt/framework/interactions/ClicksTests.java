package com.macys.sdt.framework.interactions;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class ClicksTests {

    @Test
    public void testClick() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring Click Test", Wait.untilElementPresent("ui_standards.buttons_link"));
        Clicks.click("ui_standards.buttons_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.primary_buttons_link") || MainRunner.getCurrentUrl().contains("#Buttons"));
    }

    @Test
    public void testJavascriptClick() throws Exception {
        Assume.assumeTrue("Precondition not met.", InteractionsTestSuite.preCondition);
        Assume.assumeTrue("Test element not present - Ignoring Javascript Click Test", Wait.untilElementPresent("ui_standards.forms_link"));
        Clicks.javascriptClick("ui_standards.forms_link");
        Assert.assertTrue(Wait.untilElementPresent("ui_standards.dropdown_select_menu_link") || MainRunner.getCurrentUrl().contains("#Forms"));
    }

}
