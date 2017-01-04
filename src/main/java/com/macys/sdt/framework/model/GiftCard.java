package com.macys.sdt.framework.model;

/**
 * This class represents a GiftCard and contains all the information about that GiftCard
 */
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

    /**
     * Gets the CardType of GiftCard
     *
     * @return GiftCard CardType
     */
    public CardType getCardType() {
        return cardType;
    }

    /**
     * Sets the CardType of GiftCard
     *
     * @param cardType GiftCard Type enum
     */
    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    /**
     * Gets the CardNumber of GiftCard
     *
     * @return GiftCard CardNumber
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Sets the CardNumber of CreditCard
     *
     * @param cardNumber Credit Card
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Gets the CardDescription of GiftCard
     *
     * @return GiftCard CardDescription
     */
    public String getCardDescription() {
        return cardDescription;
    }

    /**
     * Sets the CardDescription of CreditCard
     *
     * @param cardDescription Credit Card
     */
    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }

    /**
     * Gets the Balance of GiftCard
     *
     * @return GiftCard Balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the Balance of CreditCard
     *
     * @param balance Credit Card
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Gets the Division of GiftCard
     *
     * @return GiftCard Division
     */
    public int getDivision() {
        return division;
    }

    /**
     * Sets the Division of CreditCard
     *
     * @param division Credit Card
     */
    public void setDivision(int division) {
        this.division = division;
    }

    /**
     * Gets the CurrentBalance of GiftCard
     *
     * @return GiftCard CurrentBalance
     */
    public double getCurrentBalance() {
        return currentBalance;
    }

    /**
     * Sets the CurrentBalance of CreditCard
     *
     * @param currentBalance Credit Card
     */
    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    /**
     * Gets the Cid of GiftCard
     *
     * @return GiftCard Cid
     */
    public String getCid() {
        return cid;
    }

    /**
     * Sets the Cid of CreditCard
     *
     * @param cid Credit Card
     */
    public void setCid(String cid) {
        this.cid = cid;
    }

    /**
     * Gets the Ecid of GiftCard
     *
     * @return GiftCard Ecid
     */
    public String getEcid() {
        return ecid;
    }

    /**
     * Sets the Ecid of CreditCard
     *
     * @param ecid Credit Card
     */
    public void setEcid(String ecid) {
        this.ecid = ecid;
    }

    /**
     * Gets the ExpireDate of GiftCard
     *
     * @return GiftCard ExpireDate
     */
    public String getExpireDate() {
        return expireDate;
    }

    /**
     * Sets the ExpireDate of CreditCard
     *
     * @param expireDate Credit Card
     */
    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    /**
     * Different Card Types
     */
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
