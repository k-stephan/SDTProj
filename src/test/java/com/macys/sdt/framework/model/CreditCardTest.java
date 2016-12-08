package com.macys.sdt.framework.model;

import org.junit.Assert;
import org.junit.Test;

public class CreditCardTest {

    private CreditCard creditCard;
    private CreditCard creditCard3DSecure;

    public CreditCardTest() {
        creditCard = new CreditCard(CreditCard.CardType.VISA, "4445222299990007", "222", 562, "January", "01", "2017");
        creditCard3DSecure = new CreditCard(CreditCard.CardType.VISA, "4445222299990007", "222", 562, "January", "01", "2017", true, "12345");
    }

    @Test
    public void testGetCardType() throws Exception {
        Assert.assertEquals(CreditCard.CardType.VISA, creditCard.getCardType());
        creditCard.setCardType(CreditCard.CardType.fromString("American Express"));
        Assert.assertEquals(CreditCard.CardType.AMERICAN_EXPRESS, creditCard.getCardType());
    }

    @Test
    public void testGetCardNumber() throws Exception {
        Assert.assertEquals("4445222299990007", creditCard.getCardNumber());
        String cardNumber = "4048222298990007";
        creditCard.setCardNumber(cardNumber);
        Assert.assertEquals(cardNumber, creditCard.getCardNumber());
    }

    @Test
    public void testGetSecurityCode() throws Exception {
        Assert.assertEquals("222", creditCard.getSecurityCode());
        String securityCode = "212";
        creditCard.setSecurityCode(securityCode);
        Assert.assertEquals(securityCode, creditCard.getSecurityCode());
    }

    @Test
    public void testGetBalance() throws Exception {
        Assert.assertTrue(creditCard.getBalance() == 562);
        Double balance = 592.22;
        creditCard.setBalance(balance);
        Assert.assertTrue(creditCard.getBalance() == balance);
    }

    @Test
    public void testGetExpiryMonth() throws Exception {
        Assert.assertEquals("January", creditCard.getExpiryMonth());
        String expiryMonth = "March";
        creditCard.setExpiryMonth(expiryMonth);
        Assert.assertEquals(expiryMonth, creditCard.getExpiryMonth());
    }

    @Test
    public void testGetExpiryMonthIndex() throws Exception {
        Assert.assertEquals("01", creditCard.getExpiryMonthIndex());
        String expiryMonthIndex = "05";
        creditCard.setExpiryMonthIndex(expiryMonthIndex);
        Assert.assertEquals(expiryMonthIndex, creditCard.getExpiryMonthIndex());
    }

    @Test
    public void testGetExpiryYear() throws Exception {
        Assert.assertEquals("2017", creditCard.getExpiryYear());
        String expiryYear = "2019";
        creditCard.setExpiryYear(expiryYear);
        Assert.assertEquals(expiryYear, creditCard.getExpiryYear());
    }

    @Test
    public void testGetSecurePassword() throws Exception {
        Assert.assertNull(creditCard.getSecurePassword());
        Assert.assertEquals("12345", creditCard3DSecure.getSecurePassword());
        String securePassword = "test1";
        creditCard.setSecurePassword(securePassword);
        Assert.assertEquals(securePassword, creditCard.getSecurePassword());
    }

    @Test
    public void testHas3DSecure() throws Exception {
        Assert.assertFalse(creditCard.has3DSecure());
        Assert.assertTrue(creditCard3DSecure.has3DSecure());
        creditCard.setHas3DSecure(true);
        Assert.assertTrue(creditCard.has3DSecure());
    }
}