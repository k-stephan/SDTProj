package com.macys.sdt.framework.model;

public class GiftCard {
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

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getDivision() {
        return division;
    }

    public void setDivision(int division) {
        this.division = division;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getEcid() {
        return ecid;
    }

    public void setEcid(String ecid) {
        this.ecid = ecid;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

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

}
