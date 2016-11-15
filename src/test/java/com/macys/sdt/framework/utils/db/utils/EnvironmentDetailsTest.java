package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.Test;

public class EnvironmentDetailsTest {

    @Test
    public void gceEnvDetailsTest() {
        final String WEBSITE = "http://mcom-1114.c4d.devops.fds.com/";
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url, "http://c4d.devops.fds.com/reinfo/mcom-1114");
    }

    @Test
    public void stage5EnvDetailsTest() {
        final String WEBSITE = "http://www.qa17codemacys.fds.com/";
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url,
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/qa17codemacys");
    }
}
