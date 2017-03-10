package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.db.utils.Environment;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

/**
 * Tests for EnvironmentDetails and AppDetails
 */
public class EnvironmentDetailsTest {

    @BeforeClass
    public static void setUp() {
        HashMap<String, String> env = new HashMap<>();
        env.put("envDetailsUnitTest", "true");
        TestUtils.setEnv(env);
    }

    @AfterClass
    public static void tearDown() {
        MainRunner.url = null;
    }

    @Test
    public void testGceEnvDetails() throws Exception {
        String WEBSITE = "http://mcom-1114.c4d.devops.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.setEnvUrl(WEBSITE);
        EnvironmentDetails.updateStage5();
        EnvironmentDetails.getTestServiceData();
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url, "http://c4d.devops.fds.com/reinfo/mcom-1114");
    }

    @Test
    public void testStage5EnvDetails() throws Exception {
        String WEBSITE = "http://qa11codemacys.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.setEnvUrl(WEBSITE);
        EnvironmentDetails.updateStage5();
        EnvironmentDetails.getTestServiceData();
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url,
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/qa11codemacys");
    }

    @Test
    public void testStage5BcomEnvDetails() throws Exception {
        String WEBSITE = "http://qa7codebloomingdales.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.setEnvUrl(WEBSITE);
        EnvironmentDetails.updateStage5();
        EnvironmentDetails.getTestServiceData();
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url,
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/qa7codebloomingdales");
    }

    @Test
    public void testMyServicesApp() throws Exception {
        String WEBSITE = "http://www.qa0codemacys.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.setEnvUrl(WEBSITE);
        EnvironmentDetails.updateStage5();
        EnvironmentDetails.getTestServiceData();
        EnvironmentDetails.AppDetails envDetails = EnvironmentDetails.myServicesApp("NavApp");
        Assert.assertEquals("11.168.113.137", envDetails.ipAddress);
        Assert.assertEquals("jcie4312", envDetails.hostName);
        Assert.assertEquals("qa0codemacys", envDetails.envName);
    }

    @Test
    public void testOtherApp() throws Exception {
        String WEBSITE = "http://www.qa0codemacys.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.setEnvUrl(WEBSITE);
        EnvironmentDetails.updateStage5();
        EnvironmentDetails.AppDetails envDetails = EnvironmentDetails.otherApp("BagApp");
        Assert.assertEquals("11.168.50.195", envDetails.ipAddress);
        Assert.assertEquals("jcia5694", envDetails.hostName);
        Assert.assertEquals("qa0codemacys", envDetails.envName);
    }
}
