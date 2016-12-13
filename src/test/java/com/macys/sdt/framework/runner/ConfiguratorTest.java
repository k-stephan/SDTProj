package com.macys.sdt.framework.runner;


import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;

/**
 * Tests for WebDriverConfigurator
 */
public class ConfiguratorTest {

    @Test
    public void testBasicFirefox() {
        String website = "http://www.macys.com";
        String browser = "firefox";
        MainRunner.url = website;
        MainRunner.browser = browser;
        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();

        Assert.assertEquals(caps.getBrowserName(), browser);
        Assert.assertEquals(MainRunner.url, website);
        Assert.assertTrue(StepUtils.macys());
        Assert.assertTrue(StepUtils.firefox());
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.iOS());
        Assert.assertFalse(StepUtils.MEW());
        Assert.assertFalse(StepUtils.mobileDevice());

        MainRunner.url = null;
        MainRunner.browser = null;
    }

    @Test
    public void testBasicChromeMEW() {
        String website = "m.macys.com";
        String browser = "chrome";
        String device = "Google Nexus 6";
        MainRunner.url = website;
        MainRunner.browser = browser;
        MainRunner.device = device;

        DesiredCapabilities caps = WebDriverConfigurator.initDeviceCapabilities();

        Assert.assertEquals(caps.getBrowserName(), "android");
        Assert.assertEquals(MainRunner.url, website);
        Assert.assertTrue(StepUtils.macys());
        Assert.assertTrue(StepUtils.chrome());
        Assert.assertTrue(StepUtils.mobileDevice());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.iOS());

        MainRunner.url = null;
        MainRunner.browser = null;
        MainRunner.device = null;
    }

    @Test
    public void testBasicAndroid() {
        String brand = "mcom";
        String browser = "chrome";
        String device = "Google Nexus 6";
        String osVersion = "6.0";
        HashMap<String, String> env = new HashMap<>();
        env.put("appium_server", "127.0.0.1");
        TestUtils.setEnv(env);
        MainRunner.browser = browser;
        MainRunner.device = device;
        MainRunner.useAppium = true;
        MainRunner.appLocation = "/path";
        MainRunner.remoteOS = osVersion;
        MainRunner.brand = brand;

        DesiredCapabilities caps = WebDriverConfigurator.initDeviceCapabilities();

        Assert.assertEquals(caps.getCapability("platformName"), "Android");
        Assert.assertEquals(caps.getCapability("platformVersion"), osVersion);
        Assert.assertEquals(caps.getCapability("deviceName"), device);
        Assert.assertTrue(StepUtils.macys());
        Assert.assertTrue(StepUtils.mobileDevice());
        Assert.assertFalse(StepUtils.iOS());

        MainRunner.browser = null;
        MainRunner.device = null;
        MainRunner.useAppium = false;
        MainRunner.appLocation = null;
        MainRunner.remoteOS = null;
        MainRunner.brand = null;
    }
}
