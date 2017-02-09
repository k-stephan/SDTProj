package com.macys.sdt.framework.runner;

import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tests for MainRunner
 */
public class MainRunnerTest {
    @Test
    public void testScenarioRecognition() {
        MainRunner.scenarios = "src/test/java/com/macys/sdt/framework/Features/website/mcom/test.feature " +
                "src/test/java/com/macys/sdt/framework/Features/website/mcom/test2.feature";

        String website = "http://www.macys.com";
        String browser = "firefox";
        String project = "framework";
        MainRunner.url = website;
        MainRunner.browser = browser;
        MainRunner.project = project;

        ArrayList<String> scenarios = MainRunner.getFeatureScenarios();
        Assert.assertTrue(scenarios.size() == 2);
        Assert.assertTrue(scenarios.get(0).equals(MainRunner.scenarios.split(" ")[0]));
        Assert.assertTrue(scenarios.get(1).equals(MainRunner.scenarios.split(" ")[1]));
    }

    @Test
    public void testGetEnvVars() throws Exception {
        MainRunner.project = null;
        MainRunner.browser = null;
        MainRunner.workspace = null;
        MainRunner.debugMode = false;
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.scenarios = "sdt/framework/features/website/mcom/test.feature";

        MainRunner.getEnvVars(null);

        Assert.assertEquals("sdt.framework", MainRunner.project);
        Assert.assertEquals("chrome", MainRunner.browser);
        Assert.assertEquals("./", MainRunner.workspace);
        Assert.assertFalse(MainRunner.debugMode);
        Assert.assertTrue(MainRunner.closeBrowserAtExit);
        Assert.assertNotNull(MainRunner.remoteOS);
        Assert.assertNotNull(MainRunner.browserVersion);
        Assert.assertNotNull(MainRunner.timeout);
    }

    @Test
    public void testGetExParam() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("ex_params", "testExParam=testExParamValue");
        TestUtils.setEnv(env);
        Assert.assertEquals("testExParamValue", MainRunner.getExParam("testExParam"));
    }

    @Test
    public void testGetEnvOrExParam() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("ex_params", "testGetEnvOrExParam=testGetEnvOrExParamValue");
        env.put("testGetEnvOrExParam1", "testGetEnvOrExParamValue1");
        TestUtils.setEnv(env);
        Assert.assertEquals("testGetEnvOrExParamValue", MainRunner.getEnvOrExParam("testGetEnvOrExParam"));
        Assert.assertEquals("testGetEnvOrExParamValue1", MainRunner.getEnvOrExParam("testGetEnvOrExParam1"));
    }

    @Test
    public void testGetEnvVar() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("testGetEnvVar", "testGetEnvVarVal");
        TestUtils.setEnv(env);
        Assert.assertEquals("testGetEnvVarVal", MainRunner.getEnvVar("testGetEnvVar"));
    }

    @Test
    public void testBooleanParam() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("testBooleanParam", "true");
        TestUtils.setEnv(env);
        Assert.assertTrue(MainRunner.booleanParam("testBooleanParam"));
    }

    @Test @Ignore("MainRunner.main is having System.exit call")
    public void testMain() throws Throwable {
        MainRunner.scenarios = "src/test/java/com/macys/sdt/framework/Features/website/mcom/test.feature";
        MainRunner.url = "http://www.macys.com";
        MainRunner.browser = "firefox";
        MainRunner.project = "sdt.framework";
        try {
            MainRunner.main(new String[]{});
        } catch (Exception e) {
            Assert.fail("MainRunner.main : Failed to execute simple scenario : " + e.getMessage());
        }
    }
}
