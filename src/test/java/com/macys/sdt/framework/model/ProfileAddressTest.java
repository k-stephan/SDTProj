package com.macys.sdt.framework.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ProfileAddress Model
 */
public class ProfileAddressTest {

    private ProfileAddress profileAddress;

    public ProfileAddressTest() {
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

    @Test
    public void testGetProvince() throws Exception {
        String province = "Alberta";
        profileAddress.setProvince(province);
        Assert.assertEquals(province, profileAddress.getProvince());
    }

    @Test
    public void testGetId() throws Exception {
        Long id = 1234L;
        profileAddress.setId(id);
        Assert.assertEquals(id, profileAddress.getId());
    }

    @Test
    public void testGetAttention() throws Exception {
        String attention = "testattention";
        profileAddress.setAttention(attention);
        Assert.assertEquals(attention, profileAddress.getAttention());
    }

    @Test
    public void testGetSequenceNumber() throws Exception {
        Long sequenceNumber = 11L;
        profileAddress.setSequenceNumber(sequenceNumber);
        Assert.assertEquals(sequenceNumber, profileAddress.getSequenceNumber());
    }

    @Test
    public void testGetFirstName() throws Exception {
        String firstName = "first";
        profileAddress.setFirstName(firstName);
        Assert.assertEquals(firstName, profileAddress.getFirstName());
    }

    @Test
    public void testGetLastName() throws Exception {
        String lastName = "last";
        profileAddress.setLastName(lastName);
        Assert.assertEquals(lastName, profileAddress.getLastName());
    }

    @Test
    public void testGetMiddleName() throws Exception {
        String middleName = "middle";
        profileAddress.setMiddleName(middleName);
        Assert.assertEquals(middleName, profileAddress.getMiddleName());
    }

    @Test
    public void testGetAddressLine1() throws Exception {
        String addressLine1 = "postbox";
        profileAddress.setAddressLine1(addressLine1);
        Assert.assertEquals(addressLine1, profileAddress.getAddressLine1());
    }

    @Test
    public void testGetAddressLine2() throws Exception {
        String addressLine2 = "AP";
        profileAddress.setAddressLine2(addressLine2);
        Assert.assertEquals(addressLine2, profileAddress.getAddressLine2());
    }

    @Test
    public void testGetCity() throws Exception {
        String city = "Hyderabad";
        profileAddress.setCity(city);
        Assert.assertEquals(city, profileAddress.getCity());
    }

    @Test
    public void testGetState() throws Exception {
        String state = "AL";
        profileAddress.setState(state);
        Assert.assertEquals(state, profileAddress.getState());
    }

    @Test
    public void testGetZipCode() throws Exception {
        Integer zipCode = 32701;
        profileAddress.setZipCode(zipCode);
        Assert.assertEquals(zipCode, profileAddress.getZipCode());
    }

    @Test
    public void testGetCountryCode() throws Exception {
        String countryCode = "USA";
        profileAddress.setCountryCode(countryCode);
        Assert.assertEquals(countryCode, profileAddress.getCountryCode());
    }

    @Test
    public void testGetPrimaryFlag() throws Exception {
        profileAddress.setPrimaryFlag(true);
        Assert.assertTrue(profileAddress.getPrimaryFlag());
    }

    @Test
    public void testGetEmail() throws Exception {
        String email = "test1010@blackhole.macys.com";
        profileAddress.setEmail(email);
        Assert.assertEquals(email, profileAddress.getEmail());
    }

    @Test
    public void testGetBestPhone() throws Exception {
        profileAddress.setBestPhone("123-444-5577");
        Assert.assertEquals("223-444-5577", profileAddress.getBestPhone());
    }

    @Test
    public void testGetDefaultProfileAddress() throws Exception {
        ProfileAddress address = ProfileAddress.getDefaultProfileAddress();
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
}