package com.macys.sdt.framework.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ProxyFilters
 */
public class ProxyFiltersTest {

    @Test
    public void testCreateFilterDomains() throws Exception {
        ProxyFilters.createFilterDomains("http://test.createFilterDomains.com");
        Assert.assertTrue(ProxyFilters.filterDomains.contains("assets.test.createFilterDomains.com"));
    }

    @Test
    public void testGetDomain() throws Exception {
        Assert.assertEquals("test.getDomain.com", ProxyFilters.getDomain("http://test.getDomain.com"));
    }

    @Test
    public void testMain() throws Exception {
        ProxyFilters.main(new String[]{});
        Assert.assertTrue(ProxyFilters.filterDomains.contains("assets.data.coremetrics.com"));
    }
}