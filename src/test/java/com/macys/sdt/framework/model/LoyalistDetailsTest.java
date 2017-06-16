package com.macys.sdt.framework.model;

import com.macys.sdt.framework.model.user.LoyalistDetails;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for LoyalistDetails Model
 */
public class LoyalistDetailsTest {

    private LoyalistDetails loyalistDetails;

    public LoyalistDetailsTest() {
        loyalistDetails = new LoyalistDetails();
    }

    @Test
    public void testGetLoyalistType() throws Exception {
        String loyallistType = "top_tier";
        loyalistDetails.setLoyallistType(LoyalistDetails.LoyallistType.TOP_TIER);
        Assert.assertEquals(loyallistType, loyalistDetails.getLoyallistType().getName());
    }

    @Test
    public void testGetLoyaltyId() throws Exception {
        String loyaltyId = "L920000100935";
        loyalistDetails.setLoyaltyId(loyaltyId);
        Assert.assertEquals(loyaltyId, loyalistDetails.getLoyaltyId());
    }

    @Test
    public void testGetLastName() throws Exception {
        String lastName = "test";
        loyalistDetails.setLastName(lastName);
        Assert.assertEquals(lastName, loyalistDetails.getLastName());
    }

    @Test
    public void testGetZipCode() throws Exception {
        String zipCode = "10022";
        loyalistDetails.setZipCode(zipCode);
        Assert.assertEquals(zipCode, loyalistDetails.getZipCode());
    }

    @Test
    public void testGetCardNumber() throws Exception {
        String cardNumber = "6035342200026252";
        loyalistDetails.setCardNumber(cardNumber);
        Assert.assertEquals(cardNumber, loyalistDetails.getCardNumber());
    }

    @Test
    public void testGetSecurityCode() throws Exception {
        String securityCode = "1170";
        loyalistDetails.setSecurityCode(securityCode);
        Assert.assertEquals(securityCode, loyalistDetails.getSecurityCode());
    }

    @Test
    public void testGetBillingLastName() throws Exception {
        String billingLastName = "JONAS";
        loyalistDetails.setBillingLastName(billingLastName);
        Assert.assertEquals(billingLastName, loyalistDetails.getBillingLastName());
    }

    @Test
    public void testGetBillingZipCode() throws Exception {
        String billingZipCode = "94105";
        loyalistDetails.setBillingZipCode(billingZipCode);
        Assert.assertEquals(billingZipCode, loyalistDetails.getBillingZipCode());
    }
}