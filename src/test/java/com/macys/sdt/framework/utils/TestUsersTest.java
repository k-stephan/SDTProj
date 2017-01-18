package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.model.*;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.registry.Registry;
import com.macys.sdt.framework.model.user.*;
import com.macys.sdt.framework.runner.MainRunner;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public class TestUsersTest {

    @BeforeClass
    public static void setUp() throws Exception {
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";
        MainRunner.url = "http://www.qa0codemacys.fds.com";
    }


    @Test
    public void testGetSiteType() throws Exception {
        Assert.assertEquals("mcom", TestUsers.getSiteType());
    }

    @Test
    public void testSetCurrentCustomer() throws Exception {
        UserProfile customer = TestUsers.getCustomer(null);
        TestUsers.clearCustomer();
        Assert.assertNull(TestUsers.getCustomerInformation());
        TestUsers.setCurrentCustomer(customer);
        Assert.assertNotNull(TestUsers.getCustomerInformation());
    }

    @Test
    public void testGetCustomer() throws Exception {
        UserProfile customer = TestUsers.getCustomer(null);
        Assert.assertNotNull(customer);
        User user = customer.getUser();
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getGender());
        Assert.assertNotNull(user.getDateOfBirth());
        Assert.assertNotNull(user.getProfileAddress());
        UserPasswordHint userPasswordHint = user.getUserPasswordHint();
        Assert.assertNotNull(userPasswordHint);
        Assert.assertNotNull(userPasswordHint.getId());
        Assert.assertNotNull(userPasswordHint.getQuestion());
        Assert.assertNotNull(userPasswordHint.getAnswer());
        LoginCredentials loginCredentials = user.getLoginCredentials();
        Assert.assertNotNull(loginCredentials);
    }

    @Test
    public void testGetNewRegistryUser() throws Exception {
        UserProfile customer = TestUsers.getNewRegistryUser();
        Assert.assertNotNull(customer);
        Registry registry = customer.getRegistry();
        Assert.assertNotNull(registry);
        Assert.assertNotNull(registry.getCoRegistrantFirstName());
        Assert.assertNotNull(registry.getCoRegistrantLastName());
        Assert.assertNotNull(registry.getEventType());
        Assert.assertNotNull(registry.getEventMonth());
        Assert.assertNotNull(registry.getEventDay());
        Assert.assertNotNull(registry.getEventYear());
        Assert.assertNotNull(registry.getEventLocation());
        Assert.assertNotNull(registry.getNumberOfGuest());
        Assert.assertNotNull(registry.getPreferredStoreState());
        Assert.assertNotNull(registry.getPreferredStore());
    }

    @Test
    public void testGetExistingRegistryUser() throws Exception {
        UserProfile customer = TestUsers.getExistingRegistryUser();
        Assert.assertNotNull(customer);
        Registry registry = customer.getRegistry();
        Assert.assertNotNull(registry);
        Assert.assertNotNull(registry.getCoRegistrantFirstName());
        Assert.assertNotNull(registry.getCoRegistrantLastName());
        Assert.assertNotNull(registry.getEventType());
        Assert.assertNotNull(registry.getEventMonth());
        Assert.assertNotNull(registry.getEventDay());
        Assert.assertNotNull(registry.getEventYear());
        Assert.assertNotNull(registry.getEventLocation());
        Assert.assertNotNull(registry.getNumberOfGuest());
        Assert.assertNotNull(registry.getPreferredStoreState());
        Assert.assertNotNull(registry.getPreferredStore());
    }

    @Test
    public void testGetuslCustomer() throws Exception {
        UserProfile customer = TestUsers.getuslCustomer(null, null);
        Assert.assertNotNull(customer);
        User user = customer.getUser();
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getGender());
        Assert.assertNotNull(user.getDateOfBirth());
        Assert.assertNotNull(user.getProfileAddress());
        UserPasswordHint userPasswordHint = user.getUserPasswordHint();
        Assert.assertNotNull(userPasswordHint);
        Assert.assertNotNull(userPasswordHint.getId());
        Assert.assertNotNull(userPasswordHint.getQuestion());
        Assert.assertNotNull(userPasswordHint.getAnswer());
        LoginCredentials loginCredentials = user.getLoginCredentials();
        Assert.assertNotNull(loginCredentials);
    }

    @Test
    public void testGetCustomerInformation() throws Exception {
        TestUsers.getCustomer(null);
        Assert.assertNotNull(TestUsers.getCustomerInformation());
    }

    @Test
    public void testGetLoyallistInformation() throws Exception {
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        LoyalistDetails loyalist = TestUsers.getLoyallistInformation("basetier_loyallist");
        Assert.assertNotNull(loyalist);
        Assert.assertNotNull(loyalist.getLoyaltyId());
        Assert.assertNotNull(loyalist.getLastName());
        Assert.assertNotNull(loyalist.getZipCode());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
    }

    @Test
    public void testClearCustomer() throws Exception {
        TestUsers.getCustomer(null);
        TestUsers.clearCustomer();
        Assert.assertNull(TestUsers.getCustomerInformation());
    }

    @Test
    public void testGetUSLInformation() throws Exception {
        UslInfo uslInfo = TestUsers.getUSLInformation();
        Assert.assertNotNull(uslInfo);
        Assert.assertNotNull(uslInfo.getPlentiId());
        Assert.assertNotNull(uslInfo.getUslPhone());
        Assert.assertNotNull(uslInfo.getUslPin());
    }

    @Test
    public void testGetPayPalInformation() throws Exception {
        HashMap paypalInfo = TestUsers.getPayPalInformation();
        Assert.assertNotNull(paypalInfo);
        Assert.assertNotNull(paypalInfo.get("email"));
        Assert.assertNotNull(paypalInfo.get("password"));
    }

    @Test
    public void testGenerateRandomEmail() throws Exception {
        int emailLength = 12;
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        String email = TestUsers.generateRandomEmail(emailLength);
        Assert.assertNotNull(email);
        Assert.assertTrue(pattern.matcher(email).matches());
        Assert.assertEquals(emailLength, email.split("@")[0].length());
        String anotherEmail = TestUsers.generateRandomEmail(0);
        Assert.assertNotNull(anotherEmail);
        Assert.assertTrue(pattern.matcher(anotherEmail).matches());
    }

    @Test
    public void testGenerateRandomString() throws Exception {
        int strLength = 20;
        String str = TestUsers.generateRandomString(strLength);
        Assert.assertNotNull(str);
        Assert.assertEquals(strLength, str.length());
    }

    @Test
    public void testGenerateRandomFirstName() throws Exception {
        Assert.assertNotNull(TestUsers.generateRandomFirstName());
    }

    @Test
    public void testGenerateRandomLastName() throws Exception {
        Assert.assertNotNull(TestUsers.generateRandomLastName());
    }

    @Test
    public void testGenerateRandomMonth() throws Exception {
        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};
        Assert.assertTrue(Arrays.asList(monthArray).contains(TestUsers.generateRandomMonth()));
    }

    @Test
    public void testGenerateRandomMonthWithIndex() throws Exception {
        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};
        String[] monthIndexArray = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        String[] monthAndIndex = TestUsers.generateRandomMonthWithIndex();
        Assert.assertTrue(Arrays.asList(monthArray).contains(monthAndIndex[0]));
        Assert.assertTrue(Arrays.asList(monthIndexArray).contains(monthAndIndex[1]));
    }

    @Test
    public void testGenerateRandomDateIndex() throws Exception {
        int dateIndex = TestUsers.generateRandomDateIndex();
        Assert.assertTrue(dateIndex >= 1 && dateIndex <= 31);
    }

    @Test
    public void testGenerateRandomDate() throws Exception {
        Date date = TestUsers.generateRandomDate();
        Assert.assertNotNull(date);
    }

    @Test
    public void testGenerateRandomYearIndex() throws Exception {
        int year = TestUsers.generateRandomYearIndex();
        Assert.assertTrue(year >= 1917 && year <= 1987);
    }

    @Test
    public void testGenerateRandomGender() throws Exception {
        String[] genderArray = {"Female", "Male"};
        Assert.assertTrue(Arrays.asList(genderArray).contains(TestUsers.generateRandomGender()));
    }

    @Test
    public void testGenerateRandomSecurityAnswer() throws Exception {
        Assert.assertNotNull(TestUsers.generateRandomSecurityAnswer());
    }

    @Test
    public void testGenerateRandomPhoneNumber() throws Exception {
        String phoneNumber = TestUsers.generateRandomPhoneNumber();
        Assert.assertNotNull(phoneNumber);
        Assert.assertEquals(10, phoneNumber.length());
        Assert.assertNotEquals(0, phoneNumber.charAt(0));
        Assert.assertNotEquals(1, phoneNumber.charAt(0));
    }

    @Test
    public void testGenerateRandomPhoneAreaCodeExchange() throws Exception {
        String areaCode = TestUsers.generateRandomPhoneAreaCodeExchange();
        Assert.assertNotNull(areaCode);
        Assert.assertEquals(3, areaCode.length());
        Assert.assertNotEquals(0, areaCode.charAt(0));
    }

    @Test
    public void testGenerateRandomPhoneSubscriber() throws Exception {
        String subscriberNumber = TestUsers.generateRandomPhoneSubscriber();
        Assert.assertNotNull(subscriberNumber);
        Assert.assertEquals(4, subscriberNumber.length());
        Assert.assertNotEquals(0, subscriberNumber.charAt(0));
    }

    @Test
    public void testGetValidIshipAddress() throws Exception {
        String country = "India";
        JSONObject ishipAddress = TestUsers.getValidIshipAddress(country);
        Assert.assertNotNull(ishipAddress);
        Assert.assertEquals(country, ishipAddress.get("country"));
        Assert.assertNotNull(ishipAddress.get("address_line_1"));
        Assert.assertNotNull(ishipAddress.get("address_city"));
        Assert.assertNotNull(ishipAddress.get("address_state"));
        Assert.assertNotNull(ishipAddress.get("address_zip_code"));
    }

    @Test
    public void testGetValidVisaCreditCard() throws Exception {
        CreditCard creditCard = TestUsers.getValidVisaCreditCard();
        Assert.assertNotNull(creditCard);
        Assert.assertEquals(CreditCard.CardType.VISA, creditCard.getCardType());
        Assert.assertNotNull(creditCard.getCardNumber());
        Assert.assertNotNull(creditCard.getSecurityCode());
        Assert.assertNotNull(creditCard.getBalance());
        Assert.assertNotNull(creditCard.getExpiryMonth());
        Assert.assertNotNull(creditCard.getExpiryMonthIndex());
        Assert.assertNotNull(creditCard.getExpiryYear());
    }

    @Test
    public void testGetValid3DSecureCard() throws Exception {
        CreditCard creditCard = TestUsers.getValid3DSecureCard("MasterCard");
        Assert.assertNotNull(creditCard);
        Assert.assertEquals(CreditCard.CardType.fromString("MasterCard"), creditCard.getCardType());
        Assert.assertNotNull(creditCard.getCardNumber());
        Assert.assertNotNull(creditCard.getSecurityCode());
        Assert.assertNotNull(creditCard.getBalance());
        Assert.assertNotNull(creditCard.getExpiryMonth());
        Assert.assertNotNull(creditCard.getExpiryMonthIndex());
        Assert.assertNotNull(creditCard.getExpiryYear());
        Assert.assertTrue(creditCard.has3DSecure());
        Assert.assertNotNull(creditCard.getSecurePassword());
    }

    @Test
    public void testGetEnrolledUslId() throws Exception {
        UslInfo uslInfo = TestUsers.getEnrolledUslId();
        Assert.assertNotNull(uslInfo);
        Assert.assertNotNull(uslInfo.getPlentiId());
        Assert.assertNotNull(uslInfo.getUslPhone());
        Assert.assertNotNull(uslInfo.getUslPin());
    }

    @Test
    public void testGetLoyallistDetails() throws Exception {
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        String loyalistType = "toptier_loyallist";
        LoyalistDetails loyalist = TestUsers.getLoyallistDetails(loyalistType);
        Assert.assertNotNull(loyalist);
        Assert.assertEquals(loyalistType, loyalist.getLoyallistType());
        Assert.assertNotNull(loyalist.getLoyaltyId());
        Assert.assertNotNull(loyalist.getLastName());
        Assert.assertNotNull(loyalist.getZipCode());
        Assert.assertNotNull(loyalist.getCardNumber());
        Assert.assertNotNull(loyalist.getSecurityCode());
        Assert.assertNotNull(loyalist.getBillingLastName());
        Assert.assertNotNull(loyalist.getBillingZipCode());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
    }

    @Test
    public void testGetValidPromotion() throws Exception {
        JSONObject promotion = TestUsers.getValidPromotion();
        Assert.assertNotNull(promotion);
        Assert.assertNotNull(promotion.get("promo_code"));
    }

    @Test
    public void testGetRandomValidAddress() throws Exception {
        HashMap<String, String> options = new HashMap<>();
        options.put("checkout_eligible", "true");
        ProfileAddress address =  new ProfileAddress();
        TestUsers.getRandomValidAddress(options, address);
        Assert.assertNotNull(address);
        Assert.assertNotNull(address.getFirstName());
        Assert.assertNotNull(address.getLastName());
        Assert.assertNotNull(address.getAddressLine1());
        Assert.assertNotNull(address.getCity());
        Assert.assertNotNull(address.getState());
        Assert.assertNotNull(address.getZipCode());
        Assert.assertNotNull(address.getBestPhone());
        Assert.assertNotNull(address.getEmail());
    }

    @Test
    public void testGetRandomValidShippingAddress() throws Exception {
        JSONObject address = TestUsers.getRandomValidShippingAddress(null);
        Assert.assertNotNull(address);
        Assert.assertEquals("United States", address.get("country"));
        Assert.assertNotNull(address.get("address_line_1"));
        Assert.assertNotNull(address.get("address_city"));
        Assert.assertNotNull(address.get("address_state"));
        Assert.assertNotNull(address.get("address_zip_code"));
    }

    @Test
    public void testGetRandomProduct() throws Exception {
        HashMap<String, Boolean> options = new HashMap<>();
        options.put("gift_wrappable", true);
        Product product = TestUsers.getRandomProduct(options);
        Assert.assertNotNull(product);
        Assert.assertNotNull(product.id);
    }
}
