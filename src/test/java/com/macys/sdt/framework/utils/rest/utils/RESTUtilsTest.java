package com.macys.sdt.framework.utils.rest.utils;

import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Tests for RESTUtils
 */
public class RESTUtilsTest {

    @Test
    public void testFullURI() throws Exception {
        Assert.assertNotNull(RESTUtils.fullURI("/api/relative/path"));
        HashMap<String, String> env = new HashMap<>();
        env.put("website", "http://www.qa0codemacys.fds.com");
        TestUtils.setEnv(env);
        Assert.assertNotNull(RESTUtils.fullURI("/api/relative/path"));
    }

    @Test
    public void testCreateTarget() throws Exception {
        try {
            RESTUtils.createTarget(RESTUtils.createClient(), "http://www.qa0codemacys.fds.com/api/path/someapi");
            HashMap<String, String> env = new HashMap<>();
            env.put("website", "http://www.qa0codemacys.fds.com");
            TestUtils.setEnv(env);
            RESTUtils.createTarget(RESTUtils.createClient(), "/api/path/someapi");
        } catch (Exception e) {
            Assert.fail("Failed testCreateTarget " + e.getMessage());
        }
    }
}