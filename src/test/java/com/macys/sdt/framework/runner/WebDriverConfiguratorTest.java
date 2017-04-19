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
        RunConfig.browser = null;
        RunConfig.remoteOS = null;
        RunConfig.device = null;
    }

    @Test
    public void testDefaultBrowserVersion() throws Exception {
        RunConfig.browser = "firefox";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        RunConfig.browser = "edge";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        RunConfig.browser = "ie";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        RunConfig.browser = "safari";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        RunConfig.remoteOS = "OS";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
        RunConfig.browser = "chrome";
        Assert.assertNotNull(WebDriverConfigurator.defaultBrowserVersion());
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitDriverWithProxy() throws Exception {
        RunConfig.browser = "firefox";
        RunConfig.remoteOS = "Windows 7";
        RunConfig.useProxy = true;
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
            RunConfig.useProxy = false;
            MainRunner.browsermobServer = null;
        }
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitFirefoxDriver() throws Exception {
        RunConfig.browser = "";
        RunConfig.remoteOS = "Windows 7";
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
        RunConfig.browser = browser;
        RunConfig.remoteOS = "Windows 7";
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
        RunConfig.browser = "ie";
        RunConfig.remoteOS = "Windows 7";
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
        RunConfig.useChromeEmulation = true;
        RunConfig.browser = "chrome";
        RunConfig.remoteOS = "Windows 7";
        try {
            RunConfig.device = "Google Nexus 6";
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertTrue(((ChromeDriver) driver).getCapabilities().is("mobileEmulationEnabled"));
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " +e.getMessage());
        }
        finally {
            RunConfig.useChromeEmulation = false;
        }
    }

    @Test
    public void testInitSauceLabs() throws Exception {
        RunConfig.browser = "safari";
        RunConfig.remoteOS = "OS X 10.11";
        RunConfig.browserVersion = "10";
        RunConfig.brand = "MCOM";
        RunConfig.useSauceLabs = true;
        RunConfig.sauceUser = "cbt_mew_tablet";
        RunConfig.sauceKey = "64c1f147-c498-4a12-b10e-48124ce70d8b";
        try {
            driver = WebDriverConfigurator.initDriver(null);
            Assert.assertNotNull(((RemoteWebDriver) driver).getCapabilities().getCapability("webdriver.remote.sessionid"));
        }
        catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " + e.getMessage());
        }
        finally {
            RunConfig.useSauceLabs = false;
            RunConfig.sauceUser = null;
            RunConfig.sauceKey = null;
            RunConfig.browserVersion = null;
            RunConfig.brand = null;
        }
    }

    @Test @Ignore("Ignore for Jenkins")
    public void testInitAppiumDevice() throws Exception {
        RunConfig.browser = "safari";
        RunConfig.device = "iPhone 6";
        RunConfig.brand = "MCOM";
        RunConfig.useAppium = true;
        try {
            driver = WebDriverConfigurator.initDriver(null);
        } catch (Exception e) {
            Assert.fail("Failed to initialize the driver due to: " + e.getMessage());
        } finally {
            RunConfig.useAppium = false;
            RunConfig.brand = null;
        }
    }
}
