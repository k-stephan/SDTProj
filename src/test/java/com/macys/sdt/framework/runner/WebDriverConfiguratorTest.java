package com.macys.sdt.framework.runner;

import com.macys.sdt.framework.utils.TestUtils;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;

/**
 * Tests for WebDriverConfigurator
 */
public class WebDriverConfiguratorTest {

    WebDriver driver = null;

    @After
    public void reset() throws Exception {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        } catch (Exception e) {
            System.err.println("--> Error - After Test reset driver: " + e.getMessage());
        }
        MainRunner.browser = null;
        MainRunner.remoteOS = null;
        MainRunner.device = null;
    }

    @Test
    public void testDefaultBrowserVersion() throws Exception {
        MainRunner.browser = "firefox";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        MainRunner.browser = "edge";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        MainRunner.browser = "ie";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        MainRunner.browser = "safari";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        MainRunner.remoteOS = "OS";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        MainRunner.browser = "chrome";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitDriverWithProxy() throws Exception {
        MainRunner.browser = "firefox";
        MainRunner.remoteOS = "Windows 7";
        MainRunner.disableProxy = false;
        try {
            driver = WebDriverConfigurator.initDriverWithProxy();
            Assert.assertNotNull(MainRunner.browsermobServer);
            Assert.assertTrue(MainRunner.browsermobServer.isStarted());
            Assert.assertNotNull(MainRunner.browsermobServer.getPort());
            MainRunner.browsermobServer.stop();
            Assert.assertTrue(((BrowserMobProxyServer) MainRunner.browsermobServer).isStopped());
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
        finally {
            MainRunner.disableProxy = true;
            MainRunner.browsermobServer = null;
        }
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitFirefoxDriver() throws Exception {
        MainRunner.browser = "";
        MainRunner.remoteOS = "Windows 7";
        try {
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertEquals(((FirefoxDriver) driver).getCapabilities().getBrowserName(), "firefox");
        }
        catch (Exception e){
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitChromeDriver() throws Exception {
        String browser = "chrome";
        MainRunner.browser = browser;
        MainRunner.remoteOS = "Windows 7";
        try {
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertEquals(((ChromeDriver) driver).getCapabilities().getBrowserName(), browser);
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitInternetExplorerDriver() throws Exception {
        MainRunner.browser = "ie";
        MainRunner.remoteOS = "Windows 7";
        HashMap<String, String> env = new HashMap<>();
        env.put("HOME", "src/test/java/com/macys/sdt/framework/resources");
        TestUtils.setEnv(env);
        try {
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertEquals(((InternetExplorerDriver) driver).getCapabilities().getBrowserName(), "internet explorer");
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testSetupChromeEmulator() throws Exception {
        MainRunner.useChromeEmulation = true;
        MainRunner.browser = "chrome";
        MainRunner.remoteOS = "Windows 7";
        try {
            MainRunner.device = "Google Nexus 6";
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertTrue(((ChromeDriver) driver).getCapabilities().is("mobileEmulationEnabled"));
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
        finally {
            MainRunner.useChromeEmulation = false;
        }
    }

    @Test
    public void testInitSauceLabs() throws Exception {
        MainRunner.browser = "safari";
        MainRunner.remoteOS = "OS X 10.11";
        MainRunner.browserVersion = "10";
        MainRunner.brand = "MCOM";
        MainRunner.useSauceLabs = true;
        MainRunner.sauceUser = "satish-macys";
        MainRunner.sauceKey = "4fc927f7-c0bd-4f1d-859b-ed3aea2bcc40";
        try {
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertNotNull(((RemoteWebDriver) driver).getCapabilities().getCapability("webdriver.remote.sessionid"));
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
        finally {
            MainRunner.useSauceLabs = false;
            MainRunner.sauceUser = null;
            MainRunner.sauceKey = null;
            MainRunner.browserVersion = null;
            MainRunner.brand = null;
        }
    }
}
