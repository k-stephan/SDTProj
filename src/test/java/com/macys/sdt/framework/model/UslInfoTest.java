package com.macys.sdt.framework.model;

import org.junit.Assert;
import org.junit.Test;

public class UslInfoTest {

    private UslInfo uslInfo;
    public UslInfoTest()    {
        uslInfo = new UslInfo();
    }

    @Test
    public void testGetPlentiId() throws Exception {
        String plentiId = "3104170006123269";
        uslInfo.setPlentiId(plentiId);
        Assert.assertEquals(uslInfo.getPlentiId(), plentiId);
    }

    @Test
    public void testGetUslPhone() throws Exception {
        String uslPhone = "333-222-3044";
        uslInfo.setUslPhone(uslPhone);
        Assert.assertEquals(uslInfo.getUslPhone(), uslPhone);
    }

    @Test
    public void testGetUslPin() throws Exception {
        String uslPin = "1234";
        uslInfo.setUslPin(uslPin);
        Assert.assertEquals(uslInfo.getUslPin(), uslPin);
    }

    @Test
    public void testGetRedeemedPlentiPoints() throws Exception {
        String redeemedPoints = "30,000";
        uslInfo.setRedeemedPlentiPoints(redeemedPoints);
        Assert.assertEquals(uslInfo.getRedeemedPlentiPoints(), redeemedPoints);
    }
}