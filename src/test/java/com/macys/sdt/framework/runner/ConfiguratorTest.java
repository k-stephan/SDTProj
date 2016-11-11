package com.macys.sdt.framework.runner;


import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ConfiguratorTest {


    @Test
    public void testBasicFirefox() {
        final String FF = "firefox";

        MainRunner.browser = FF;
        DesiredCapabilities caps = WebDriverConfigurator.initCapabilities();

        Assert.assertEquals(caps.getBrowserName(), FF);

    }
}
