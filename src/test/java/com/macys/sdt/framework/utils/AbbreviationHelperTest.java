package com.macys.sdt.framework.utils;

import org.junit.Assert;
import org.junit.Test;

public class AbbreviationHelperTest {

    @Test
    public void testAbbreviations() {
        Assert.assertEquals(AbbreviationHelper.translateStateAbbreviation("MN"), "Minnesota");
        Assert.assertEquals(AbbreviationHelper.translateStateAbbreviation("CA"), "California");
        Assert.assertEquals(AbbreviationHelper.translateStateAbbreviation("TX"), "Texas");
        Assert.assertEquals(AbbreviationHelper.translateStateAbbreviation("OR"), "Oregon");
        Assert.assertNull(AbbreviationHelper.translateStateAbbreviation("BY"));
    }

    @Test
    public void testFullName() {
        Assert.assertEquals(AbbreviationHelper.getStateAbbreviation("Minnesota"), "MN");
        Assert.assertEquals(AbbreviationHelper.getStateAbbreviation("California"), "CA");
        Assert.assertEquals(AbbreviationHelper.getStateAbbreviation("Texas"), "TX");
        Assert.assertEquals(AbbreviationHelper.getStateAbbreviation("Oregon"), "OR");
        Assert.assertNull(AbbreviationHelper.getStateAbbreviation("This is not a state"));
    }
}
