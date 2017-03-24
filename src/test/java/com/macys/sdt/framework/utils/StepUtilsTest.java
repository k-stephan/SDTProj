package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.runner.RunConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Tests for StepUtils
 */
public class StepUtilsTest {

    @Test
    public void testChrome() throws Exception {
        RunConfig.browser = "chrome";
        Assert.assertTrue(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testFirefox() throws Exception {
        RunConfig.browser = "firefox";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertTrue(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testIe() throws Exception {
        RunConfig.browser = "ie";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertTrue(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testSafari() throws Exception {
        RunConfig.browser = "safari";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertTrue(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testEdge() throws Exception {
        RunConfig.browser = "edge";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertTrue(StepUtils.edge());
    }

    @Test
    public void testProdEnv() throws Exception {
        RunConfig.url = "http://www.macys.com";
        Assert.assertTrue(StepUtils.prodEnv());
        RunConfig.url = "http://www.bloomingdales.com";
        Assert.assertTrue(StepUtils.prodEnv());
        RunConfig.url = "http://www.qa0codemacys.fds.com";
        Assert.assertFalse(StepUtils.prodEnv());
        RunConfig.url = "http://www.qa0codebloomingdales.fds.com";
        Assert.assertFalse(StepUtils.prodEnv());
    }

    @Test
    public void testMacys() throws Exception {
        RunConfig.brand = "mcom";
        Assert.assertTrue(StepUtils.macys());
        Assert.assertFalse(StepUtils.bloomingdales());
        RunConfig.brand = null;
        RunConfig.url = "http://www.macys.com";
        Assert.assertTrue(StepUtils.macys());
        Assert.assertFalse(StepUtils.bloomingdales());
        RunConfig.url = "http://www.qa0codemacys.fds.com";
        Assert.assertTrue(StepUtils.macys());
        Assert.assertFalse(StepUtils.bloomingdales());
    }

    @Test
    public void testBloomingdales() throws Exception {
        RunConfig.brand = "bcom";
        Assert.assertTrue(StepUtils.bloomingdales());
        Assert.assertFalse(StepUtils.macys());
        RunConfig.brand = null;
        RunConfig.url = "http://www.bloomingdales.com";
        Assert.assertTrue(StepUtils.bloomingdales());
        Assert.assertFalse(StepUtils.macys());
        RunConfig.url = "http://www.qa0codebloomingdales.fds.com";
        Assert.assertTrue(StepUtils.bloomingdales());
        Assert.assertFalse(StepUtils.macys());
    }

    @Test
    public void testMobileDevice() throws Exception {
        RunConfig.device = "iPhone 6";
        Assert.assertTrue(StepUtils.mobileDevice());
        RunConfig.device = null;
        Assert.assertFalse(StepUtils.mobileDevice());
    }

    @Test
    public void testMEW() throws Exception {
        MainRunner.currentURL = "http://m.qa0codemacys.fds.com";
        Assert.assertTrue(StepUtils.MEW());
        MainRunner.currentURL = "http://www.qa0codemacys.fds.com";
        Assert.assertFalse(StepUtils.MEW());
    }

    @Test
    public void testTablet() throws Exception {
        RunConfig.device = "iPad";
        Assert.assertTrue(StepUtils.tablet());
        RunConfig.device = "iPhone 6";
        Assert.assertFalse(StepUtils.tablet());
        RunConfig.device = null;
        Assert.assertFalse(StepUtils.tablet());
    }

    @Test
    public void testIpad() throws Exception {
        RunConfig.device = "iPad";
        Assert.assertTrue(StepUtils.ipad());
        RunConfig.device = "Google Nexus 7";
        Assert.assertFalse(StepUtils.ipad());
        RunConfig.device = null;
        Assert.assertFalse(StepUtils.ipad());
    }

    @Test
    public void testIOS() throws Exception {
        RunConfig.device = "iPad";
        Assert.assertTrue(StepUtils.iOS());
        RunConfig.device = "iPhone 6";
        Assert.assertTrue(StepUtils.iOS());
        RunConfig.device = "Google Nexus 7";
        Assert.assertFalse(StepUtils.iOS());
        RunConfig.device = null;
        Assert.assertFalse(StepUtils.iOS());
    }

    @Test @Ignore("Not working in Jenkins")
    public void testDesktopScreenCapture() throws Exception {
        RunConfig.logs = "logs/";
        Utils.createDirectory(RunConfig.logs, false);
        File file = new File(RunConfig.logs + "StepUtilsTestDesktopScreenCapture.jpg");
        StepUtils.desktopScreenCapture(file);
        Assert.assertTrue(file.exists());
    }
}