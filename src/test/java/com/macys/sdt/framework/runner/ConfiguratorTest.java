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

    private final String SCENARIOS = "SolutionDevelopment/SampleProject/features/website/mcom/sample.feature";

  /*  @Test
    public void testBasicChrome() {
        final String WEBSITE = "http://www.macys.com";
        final String BROWSER = "chrome";
        HashMap<String, String> env = new HashMap<>();
        env.put("scenarios", SCENARIOS);
        env.put("website", WEBSITE);
        env.put("browser", BROWSER);
        TestUtils.setEnv(env);

        MainRunner.getEnvVars();
        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();

        Assert.assertEquals(caps.getBrowserName(), BROWSER);
        Assert.assertEquals(MainRunner.url, WEBSITE);
        Assert.assertTrue(StepUtils.macys());
        Assert.assertTrue(StepUtils.firefox());
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.iOS());
        Assert.assertFalse(StepUtils.MEW());
        Assert.assertFalse(StepUtils.mobileDevice());
    }*/

 /*   @Test
    public void testBasicChromeMEW() {
        final String WEBSITE = "m.macys.com";
        final String BROWSER = "chrome";
        final String DEVICE = "Google Nexus 6";
        HashMap<String, String> env = new HashMap<>();
        env.put("scenarios", SCENARIOS);
        env.put("website", WEBSITE);
        env.put("browser", BROWSER);
        env.put("device", DEVICE);
        TestUtils.setEnv(env);

        MainRunner.getEnvVars();
        DesiredCapabilities caps = WebDriverConfigurator.initDeviceCapabilities();

        Assert.assertEquals(caps.getBrowserName(), BROWSER);
        Assert.assertEquals(MainRunner.url, WEBSITE);
        Assert.assertTrue(StepUtils.macys());
        Assert.assertTrue(StepUtils.chrome());
        Assert.assertTrue(StepUtils.mobileDevice());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.iOS());
    }*/

  /*  @Test
    public void testBasicAndroid() {
        final String BRAND = "mcom";
        final String BROWSER = "chrome";
        final String DEVICE = "Google Nexus 6";
        final String OS_VERSION = "6.0";
        HashMap<String, String> env = new HashMap<>();
        env.put("scenarios", SCENARIOS);
        env.put("brand", BRAND);
        env.put("browser", BROWSER);
        env.put("device", DEVICE);
        env.put("use_appium", "t");
        env.put("app_location", "/path");
        env.put("appium_server", "127.0.0.1");
        env.put("remote_os", OS_VERSION);
        TestUtils.setEnv(env);

        MainRunner.getEnvVars();
        DesiredCapabilities caps = WebDriverConfigurator.initDeviceCapabilities();

        Assert.assertEquals(caps.getCapability("platformName"), "Android");
        Assert.assertEquals(caps.getCapability("platformVersion"), OS_VERSION);
        Assert.assertEquals(caps.getCapability("deviceName"), DEVICE);
        Assert.assertTrue(StepUtils.macys());
        Assert.assertTrue(StepUtils.mobileDevice());
        Assert.assertFalse(StepUtils.iOS());
    }*/
}
