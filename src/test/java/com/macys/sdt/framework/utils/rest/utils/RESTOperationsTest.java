package com.macys.sdt.framework.utils.rest.utils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for RESTOperations
 */
public class RESTOperationsTest {

    //TODO: this URL is not available from build server, need to replace it
    @Test @Ignore
    public void testDoPOST() throws Exception {
        String url = "http://mdc2vr4073:9099/RAPADDashboardConfig/getEnvDetails.html";
        String payload = "{\"stream\": \"MCOM\", \"stage\": \"5\", \"release\": \"15R\"}";
        Assert.assertNotNull(RESTOperations.doPOST(url, "application/json", payload, null));
    }

    @Test
    public void testDoGET() throws Exception {
        Assert.assertNotNull(RESTOperations.doGET("http://segments.macys.com/campaign/all", null));
    }

    @Test @Ignore("WIP: Need to find a service to perform DELETE operation")
    public void testDoDELETE() throws Exception {

    }

    @Test @Ignore("WIP: Need to find a service to perform PUT operation")
    public void testDoPUT() throws Exception {

    }
}