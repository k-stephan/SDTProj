package com.macys.sdt.framework.model;

import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.user.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

/**
 * Tests for User Model
 */
public class UserTest {

    private User user;

    public UserTest() {
        user = new User();
    }

    @Test
    public void testGetDefaultUser() throws Exception {
        User userdata = User.getDefaultUser();
        Assert.assertNotNull(userdata);
        Assert.assertNotNull(userdata.getDateOfBirth());
        Assert.assertNotNull(userdata.getGender());
        Assert.assertNotNull(userdata.getSubscribedToNewsLetter());
        Assert.assertNotNull(userdata.getUserPasswordHint());
        Assert.assertNotNull(userdata.getProfileAddress());
        Assert.assertNotNull(userdata.getLoginCredentials());
    }

    @Test
    public void testGetDateOfBirth() throws Exception {
        user.setDateOfBirth("1997-07-07");
        Assert.assertEquals("1997-07-07", user.getDateOfBirth());
        LocalDate localDate = user.getDateOfBirth("1997-07-07");
        Assert.assertEquals(localDate.getYear(), 1997);
        Assert.assertEquals(localDate.getMonth(), Month.JULY);
        Assert.assertEquals(localDate.getDayOfMonth(), 7);
    }

    @Test
    public void testGetGender() throws Exception {
        String gender = "M";
        user.setGender(gender);
        Assert.assertEquals(user.getGender(), gender);
    }

    @Test
    public void testGetSubscribedToNewsLetter() throws Exception {
        user.setSubscribedToNewsLetter(true);
        Assert.assertTrue(user.getSubscribedToNewsLetter());
    }

    @Test
    public void testGetUserPasswordHint() throws Exception {
        Long id = 1234L;
        String q = "question";
        String a = "answer";
        user.setUserPasswordHint(new UserPasswordHint(id, q, a));
        UserPasswordHint hint = user.getUserPasswordHint();
        Assert.assertEquals(id, hint.getId());
        Assert.assertEquals(q, hint.getQuestion());
        Assert.assertEquals(a, hint.getAnswer());
    }

    @Test
    public void testGetProfileAddress() throws Exception {
        user.setProfileAddress(ProfileAddress.getDefaultProfileAddress());
        ProfileAddress address = user.getProfileAddress();
        Assert.assertNotNull(address);
        Assert.assertNotNull(address.getId());
        Assert.assertNotNull(address.getAttention());
        Assert.assertNotNull(address.getSequenceNumber());
        Assert.assertNotNull(address.getFirstName());
        Assert.assertNotNull(address.getLastName());
        Assert.assertNotNull(address.getMiddleName());
        Assert.assertNotNull(address.getAddressLine1());
        Assert.assertNotNull(address.getAddressLine2());
        Assert.assertNotNull(address.getCity());
        Assert.assertNotNull(address.getState());
        Assert.assertNotNull(address.getZipCode());
        Assert.assertNotNull(address.getCountryCode());
        Assert.assertNotNull(address.getEmail());
        Assert.assertNotNull(address.getBestPhone());
        Assert.assertNotNull(address.getPrimaryFlag());
    }

    @Test
    public void testGetLoginCredentials() throws Exception {
        user.setLoginCredentials(LoginCredentials.getDefaultLoginCredentials());
        Assert.assertEquals(user.getLoginCredentials().getPassword(), "Macys12345");
    }

    @Test
    public void testGetUslInfo() throws Exception {
        UslInfo uslInfo = new UslInfo();
        uslInfo.setUslPin("1234");
        uslInfo.setPlentiId("356789876756789");
        uslInfo.setRedeemedPlentiPoints("30,000");
        uslInfo.setUslPhone("333-222-1234");
        user.setUslInfo(uslInfo);
        UslInfo info = user.getUslInfo();
        Assert.assertNotNull(info);
        Assert.assertEquals(info.getUslPin(), "1234");
        Assert.assertEquals(info.getPlentiId(), "356789876756789");
        Assert.assertEquals(info.getRedeemedPlentiPoints(), "30,000");
        Assert.assertEquals(info.getUslPhone(), "333-222-1234");
    }

    @Test
    public void testGetLoyalistDetails() throws Exception {
        LoyalistDetails loyallistInfo = new LoyalistDetails();
        loyallistInfo.setLoyaltyId("L920000100935");
        loyallistInfo.setLoyallistType("toptier_loyallist");
        loyallistInfo.setLastName("test");
        loyallistInfo.setZipCode("10022");
        loyallistInfo.setCardNumber("6035342200026252");
        loyallistInfo.setBillingLastName("JONAS");
        loyallistInfo.setBillingZipCode("22102");
        loyallistInfo.setSecurityCode("1170");
        user.setLoyalistDetails(loyallistInfo);
        LoyalistDetails loyallistDetails = user.getLoyalistDetails();
        Assert.assertNotNull(loyallistDetails);
        Assert.assertEquals(loyallistDetails.getLoyaltyId(), "L920000100935");
        Assert.assertEquals(loyallistDetails.getLoyallistType(), "toptier_loyallist");
        Assert.assertEquals(loyallistDetails.getLastName(), "test");
        Assert.assertEquals(loyallistDetails.getZipCode(), "10022");
        Assert.assertEquals(loyallistDetails.getCardNumber(), "6035342200026252");
        Assert.assertEquals(loyallistDetails.getBillingLastName(), "JONAS");
        Assert.assertEquals(loyallistDetails.getBillingZipCode(), "22102");
        Assert.assertEquals(loyallistDetails.getSecurityCode(), "1170");
    }
}