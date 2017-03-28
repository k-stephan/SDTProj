package com.macys.sdt.framework.runner;

import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class RunConfigTest {

    @Before
    public void setup() {
        MainRunner.configureLogs();
        RunConfig.openLog();
    }

    @Test
    public void testScenarioRecognition() {
        RunConfig.scenarios = "src/test/java/com/macys/sdt/framework/Features/website/mcom/test.feature " +
                "src/test/java/com/macys/sdt/framework/Features/website/mcom/test2.feature";

        String website = "http://www.macys.com";
        String browser = "firefox";
        String project = "framework";
        RunConfig.url = website;
        RunConfig.browser = browser;
        RunConfig.project = project;

        ArrayList<String> scenarios = RunConfig.getFeatureScenarios();
        Assert.assertTrue(scenarios.size() == 2);
        Assert.assertTrue(scenarios.get(0).equals(RunConfig.scenarios.split(" ")[0]));
        Assert.assertTrue(scenarios.get(1).equals(RunConfig.scenarios.split(" ")[1]));
    }

    @Test
    public void testGetEnvVars() throws Exception {
        RunConfig.project = null;
        RunConfig.browser = null;
        RunConfig.workspace = null;
        RunConfig.debugMode = false;
        RunConfig.url = "http://www.qa0codemacys.fds.com";
        RunConfig.scenarios = "sdt/framework/features/website/mcom/test.feature";

        RunConfig.getEnvVars(null);

        Assert.assertEquals("sdt.framework", RunConfig.project);
        Assert.assertEquals("chrome", RunConfig.browser);
        Assert.assertEquals(System.getProperty("user.dir").replace("\\", "/") + "/", RunConfig.workspace);
        Assert.assertFalse(RunConfig.debugMode);
        Assert.assertTrue(RunConfig.closeBrowserAtExit);
        Assert.assertNotNull(RunConfig.remoteOS);
        Assert.assertNotNull(RunConfig.browserVersion);
        Assert.assertNotNull(RunConfig.timeout);
    }

    @Test
    public void testGetExParam() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("ex_params", "testExParam=testExParamValue");
        TestUtils.setEnv(env);
        Assert.assertEquals("testExParamValue", RunConfig.getExParam("testExParam"));
    }

    @Test
    public void testGetEnvOrExParam() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("ex_params", "testGetEnvOrExParam=testGetEnvOrExParamValue");
        env.put("testGetEnvOrExParam1", "testGetEnvOrExParamValue1");
        TestUtils.setEnv(env);
        Assert.assertEquals("testGetEnvOrExParamValue", RunConfig.getEnvOrExParam("testGetEnvOrExParam"));
        Assert.assertEquals("testGetEnvOrExParamValue1", RunConfig.getEnvOrExParam("testGetEnvOrExParam1"));
    }

    @Test
    public void testGetEnvVar() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("testGetEnvVar", "testGetEnvVarVal");
        TestUtils.setEnv(env);
        Assert.assertEquals("testGetEnvVarVal", RunConfig.getEnvVar("testGetEnvVar"));
    }

    @Test
    public void testBooleanParam() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("testBooleanParam", "true");
        TestUtils.setEnv(env);
        Assert.assertTrue(RunConfig.booleanParam("testBooleanParam"));
    }
}
