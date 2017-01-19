package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
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
        MainRunner.browser = "chrome";
        Assert.assertTrue(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testFirefox() throws Exception {
        MainRunner.browser = "firefox";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertTrue(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testIe() throws Exception {
        MainRunner.browser = "ie";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertTrue(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testSafari() throws Exception {
        MainRunner.browser = "safari";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertTrue(StepUtils.safari());
        Assert.assertFalse(StepUtils.edge());
    }

    @Test
    public void testEdge() throws Exception {
        MainRunner.browser = "edge";
        Assert.assertFalse(StepUtils.chrome());
        Assert.assertFalse(StepUtils.firefox());
        Assert.assertFalse(StepUtils.ie());
        Assert.assertFalse(StepUtils.safari());
        Assert.assertTrue(StepUtils.edge());
    }

    @Test
    public void testProdEnv() throws Exception {
        MainRunner.url = "http://www.macys.com";
        Assert.assertTrue(StepUtils.prodEnv());
        MainRunner.url = "http://www.bloomingdales.com";
        Assert.assertTrue(StepUtils.prodEnv());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        Assert.assertFalse(StepUtils.prodEnv());
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        Assert.assertFalse(StepUtils.prodEnv());
    }

    @Test
    public void testMacys() throws Exception {
        MainRunner.brand = "mcom";
        Assert.assertTrue(StepUtils.macys());
        Assert.assertFalse(StepUtils.bloomingdales());
        MainRunner.brand = null;
        MainRunner.url = "http://www.macys.com";
        Assert.assertTrue(StepUtils.macys());
        Assert.assertFalse(StepUtils.bloomingdales());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        Assert.assertTrue(StepUtils.macys());
        Assert.assertFalse(StepUtils.bloomingdales());
    }

    @Test
    public void testBloomingdales() throws Exception {
        MainRunner.brand = "bcom";
        Assert.assertTrue(StepUtils.bloomingdales());
        Assert.assertFalse(StepUtils.macys());
        MainRunner.brand = null;
        MainRunner.url = "http://www.bloomingdales.com";
        Assert.assertTrue(StepUtils.bloomingdales());
        Assert.assertFalse(StepUtils.macys());
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        Assert.assertTrue(StepUtils.bloomingdales());
        Assert.assertFalse(StepUtils.macys());
    }

    @Test
    public void testMobileDevice() throws Exception {
        MainRunner.device = "iPhone 6";
        Assert.assertTrue(StepUtils.mobileDevice());
        MainRunner.device = null;
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
        MainRunner.device = "iPad";
        Assert.assertTrue(StepUtils.tablet());
        MainRunner.device = "iPhone 6";
        Assert.assertFalse(StepUtils.tablet());
        MainRunner.device = null;
        Assert.assertFalse(StepUtils.tablet());
    }

    @Test
    public void testIpad() throws Exception {
        MainRunner.device = "iPad";
        Assert.assertTrue(StepUtils.ipad());
        MainRunner.device = "Google Nexus 7";
        Assert.assertFalse(StepUtils.ipad());
        MainRunner.device = null;
        Assert.assertFalse(StepUtils.ipad());
    }

    @Test
    public void testIOS() throws Exception {
        MainRunner.device = "iPad";
        Assert.assertTrue(StepUtils.iOS());
        MainRunner.device = "iPhone 6";
        Assert.assertTrue(StepUtils.iOS());
        MainRunner.device = "Google Nexus 7";
        Assert.assertFalse(StepUtils.iOS());
        MainRunner.device = null;
        Assert.assertFalse(StepUtils.iOS());
    }

    @Test @Ignore("Not working in Jenkins")
    public void testDesktopScreenCapture() throws Exception {
        MainRunner.logs = "logs/";
        Utils.createDirectory(MainRunner.logs, false);
        File file = new File(MainRunner.logs + "StepUtilsTestDesktopScreenCapture.jpg");
        StepUtils.desktopScreenCapture(file);
        Assert.assertTrue(file.exists());
    }
}