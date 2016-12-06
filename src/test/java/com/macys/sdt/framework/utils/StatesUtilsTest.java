package com.macys.sdt.framework.utils;

import org.junit.Assert;
import org.junit.Test;

public class StatesUtilsTest {

    @Test
    public void testAbbreviations() {
        Assert.assertEquals(StatesUtils.translateAbbreviation("MN"), "Minnesota");
        Assert.assertEquals(StatesUtils.translateAbbreviation("CA"), "California");
        Assert.assertEquals(StatesUtils.translateAbbreviation("TX"), "Texas");
        Assert.assertEquals(StatesUtils.translateAbbreviation("OR"), "Oregon");
        Assert.assertNull(StatesUtils.translateAbbreviation("BY"));
    }

    @Test
    public void testFullName() {
        Assert.assertEquals(StatesUtils.getAbbreviation("Minnesota"), "MN");
        Assert.assertEquals(StatesUtils.getAbbreviation("California"), "CA");
        Assert.assertEquals(StatesUtils.getAbbreviation("Texas"), "TX");
        Assert.assertEquals(StatesUtils.getAbbreviation("Oregon"), "OR");
        Assert.assertNull(StatesUtils.getAbbreviation("This is not a state"));
    }
}
