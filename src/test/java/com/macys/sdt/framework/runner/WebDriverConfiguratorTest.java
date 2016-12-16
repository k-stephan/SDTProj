package com.macys.sdt.framework.runner;


import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;

import static com.macys.sdt.framework.runner.MainRunner.browserVersion;

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

    @Test
    public void testInitBrowserCapabilitiesChrome() {
        String browser = "chrome";
        String browserVersion = "54";
        MainRunner.url = "http://www.macys.com";
        MainRunner.browser = browser;
        MainRunner.browserVersion = browserVersion;

        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();
        Assert.assertEquals(caps.getBrowserName(), browser);
        Assert.assertEquals(caps.getVersion(), browserVersion);

        MainRunner.url = null;
        MainRunner.browser = null;
        MainRunner.browserVersion = null;
    }

    @Test
    public void testInitBrowserCapabilitiesFirefox() {
        MainRunner.browser = "";
        MainRunner.url = "http://www.macys.com";

        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();
        Assert.assertEquals(caps.getBrowserName(), "firefox");

        MainRunner.url = null;
        MainRunner.browser = null;
    }

    @Test
    public void testInitBrowserCapabilitiesIE() {
        String browser = "ie";
        String browserVersion = "10";
        MainRunner.url = "http://www.macys.com";
        MainRunner.browser = browser;
        MainRunner.browserVersion = browserVersion;

        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();
        Assert.assertEquals(caps.getBrowserName(), "internet explorer");
        Assert.assertEquals(caps.getVersion(), browserVersion);

        MainRunner.url = null;
        MainRunner.browser = null;
        MainRunner.browserVersion = null;
    }

    @Test
    public void testInitBrowserCapabilitiesSafari() {
        String browser = "safari";
        String browserVersion = "5";
        MainRunner.url = "http://www.macys.com";
        MainRunner.browser = browser;
        MainRunner.browserVersion = browserVersion;

        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();
        Assert.assertEquals(caps.getBrowserName(), browser);
        Assert.assertEquals(caps.getVersion(), browserVersion);

        MainRunner.url = null;
        MainRunner.browser = null;
        MainRunner.browserVersion = null;
    }

    @Test
    public void testInitBrowserCapabilitiesEdge() {
        String browser = "edge";
        String browserVersion = "20";
        MainRunner.url = "http://www.macys.com";
        MainRunner.browser = browser;
        MainRunner.browserVersion = browserVersion;

        DesiredCapabilities caps = WebDriverConfigurator.initBrowserCapabilities();
        Assert.assertEquals(caps.getBrowserName(), "MicrosoftEdge");
        Assert.assertEquals(caps.getVersion(), browserVersion);

        MainRunner.url = null;
        MainRunner.browser = null;
        MainRunner.browserVersion = null;
    }
}
