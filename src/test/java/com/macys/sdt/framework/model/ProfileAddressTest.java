package com.macys.sdt.framework.model;

import org.junit.Assert;
import org.junit.Test;

public class ProfileAddressTest {

    private ProfileAddress profileAddress = null;

    public ProfileAddressTest()    {
        profileAddress = new ProfileAddress();
    }

    @Test
    public void testGetPhoneAreaCode() throws Exception {
        profileAddress.setBestPhone("3245678904");
        String phoneAreaCode = profileAddress.getPhoneAreaCode();
        Assert.assertEquals("324", phoneAreaCode);
    }

    @Test
    public void testGetPhoneExchange() throws Exception {
        profileAddress.setBestPhone("3245678904");
        String phoneExchange = profileAddress.getPhoneExchange();
        Assert.assertEquals("567", phoneExchange);
    }

    @Test
    public void testGetPhoneSubscriber() throws Exception {
        profileAddress.setBestPhone("3245678904");
        String phoneSubscriber = profileAddress.getPhoneSubscriber();
        Assert.assertEquals("8904", phoneSubscriber);
    }

}