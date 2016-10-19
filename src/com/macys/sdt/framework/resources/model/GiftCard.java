package com.macys.sdt.framework.resources.model;

public class GiftCard {
    public enum CardType {
        EGC("EGC"),
        VGC("VGC"),
        VRC("VRC");

        public final String name;

        CardType(String name) {
            this.name = name;
        }

        public static CardType fromString(String value) {
            for (CardType type : CardType.values())
                if (value.equals(type.name)) {
                    return type;
                }

            return null;
        }
    }

    private CardType cardType;
    private String cardNumber, cardDescription, cid, ecid, expireDate;
    private int division;
    private double balance, currentBalance;

    public GiftCard(CardType cardType, String cardNumber, String cardDescription, String cid, String ecid, String expireDate, double balance, double currentBalance, int division) {
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.cardDescription = cardDescription;
        this.cid = cid;
        this.ecid = ecid;
        this.expireDate = expireDate;
        this.balance = balance;
        this.currentBalance = currentBalance;
        this.division = division;
    }

    public CardType getCardType() {
        return cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public double getBalance() {
        return balance;
    }

    public int getDivision() {
        return division;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public String getCid() {
        return cid;
    }

    public String getEcid() {
        return ecid;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setEcid(String ecid) {
        this.ecid = ecid;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public void setDivision(int division) {
        this.division = division;
    }

}
