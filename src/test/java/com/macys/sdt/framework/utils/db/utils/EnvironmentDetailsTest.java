package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.Test;

public class EnvironmentDetailsTest {

    @Test
    public void gceEnvDetailsTest() {
        String WEBSITE = "http://mcom-1114.c4d.devops.fds.com/";
        MainRunner.url = WEBSITE;
        String url = EnvironmentDetails.getServiceURL(WEBSITE);
        Assert.assertEquals(url, "http://c4d.devops.fds.com/reinfo/mcom-1114");
    }
}
