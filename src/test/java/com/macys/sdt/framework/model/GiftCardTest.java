package com.macys.sdt.framework.model;

import org.junit.Assert;
import org.junit.Test;

public class GiftCardTest {

    private GiftCard giftCard;

    public GiftCardTest() {
        giftCard = new GiftCard(GiftCard.CardType.EGC, "654177010016552", "MACYS GIFT CARD-NOEXP", "797", "7940", "01-01-2021", 2000, 1644.22, 71);
    }

    @Test
    public void testGetCardType() throws Exception {
        Assert.assertEquals(GiftCard.CardType.EGC, giftCard.getCardType());
        giftCard.setCardType(GiftCard.CardType.fromString("VGC"));
        Assert.assertEquals(GiftCard.CardType.VGC, giftCard.getCardType());
    }

    @Test
    public void testGetCardNumber() throws Exception {
        Assert.assertEquals("654177010016552", giftCard.getCardNumber());
        String cardNumber = "851188011718056";
        giftCard.setCardNumber(cardNumber);
        Assert.assertEquals(cardNumber, giftCard.getCardNumber());
    }

    @Test
    public void testGetCardDescription() throws Exception {
        Assert.assertEquals("MACYS GIFT CARD-NOEXP", giftCard.getCardDescription());
        String cardDescription = "VIRTUAL GIFT CARD NOEXP";
        giftCard.setCardDescription(cardDescription);
        Assert.assertEquals(cardDescription, giftCard.getCardDescription());
    }

    @Test
    public void testGetBalance() throws Exception {
        Assert.assertTrue(giftCard.getBalance() == 2000);
        Double balance = 2000.22;
        giftCard.setBalance(balance);
        Assert.assertTrue(giftCard.getBalance() == balance);
    }

    @Test
    public void testGetDivision() throws Exception {
        Assert.assertTrue(giftCard.getDivision() == 71);
        Integer division = 70;
        giftCard.setDivision(division);
        Assert.assertTrue(giftCard.getDivision() == division);
    }

    @Test
    public void testGetCurrentBalance() throws Exception {
        Assert.assertTrue(giftCard.getCurrentBalance() == 1644.22);
        Double currentBalance = 2000.0;
        giftCard.setCurrentBalance(currentBalance);
        Assert.assertTrue(giftCard.getCurrentBalance() == currentBalance);
    }

    @Test
    public void testGetCid() throws Exception {
        Assert.assertEquals("797", giftCard.getCid());
        String cid = "580";
        giftCard.setCid(cid);
        Assert.assertEquals(cid, giftCard.getCid());
    }

    @Test
    public void testGetEcid() throws Exception {
        Assert.assertEquals("7940", giftCard.getEcid());
        String ecid = "5730";
        giftCard.setEcid(ecid);
        Assert.assertEquals(ecid, giftCard.getEcid());
    }

    @Test
    public void testGetExpireDate() throws Exception {
        Assert.assertEquals("01-01-2021", giftCard.getExpireDate());
        String expireDate = "01-01-2020";
        giftCard.setExpireDate(expireDate);
        Assert.assertEquals(expireDate, giftCard.getExpireDate());
    }
}