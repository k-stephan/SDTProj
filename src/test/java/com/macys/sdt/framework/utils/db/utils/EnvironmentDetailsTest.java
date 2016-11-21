package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Test;

public class EnvironmentDetailsTest {

    @Test
    public void gceEnvDetailsTest() {
        String WEBSITE = "http://mcom-1114.c4d.devops.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.ENV_URL = WEBSITE;
        EnvironmentDetails.updateStage5();
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url, "http://c4d.devops.fds.com/reinfo/mcom-1114");
    }

    @Test
    public void stage5EnvDetailsTest() {
        String WEBSITE = "http://qa11codemacys.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.ENV_URL = WEBSITE;
        EnvironmentDetails.updateStage5();
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url,
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/qa11codemacys");
    }

    @Test
    public void stage5BcomEnvDetailsTest() {
        String WEBSITE = "http://qa7codebloomingdales.fds.com/";
        MainRunner.url = WEBSITE;
        EnvironmentDetails.ENV_URL = WEBSITE;
        EnvironmentDetails.updateStage5();
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url,
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/qa7codebloomingdales");
    }
}
