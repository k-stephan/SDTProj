package com.macys.sdt.framework.model;

import org.json.JSONObject;

/**
 * This class represents a CreditCard and contains all the information about that CreditCard
 */
public class CreditCard {
    private CardType cardType;
    private String cardNumber;
    private String securityCode;
    private double balance;
    private String expiryMonth;
    private String expiryMonthIndex;
    private String expiryYear;
    private boolean has3DSecure;
    private String securePassword;

    private CreditCard() {}

    public CreditCard(CardType cardType, String cardNumber, String securityCode, double balance, String expiryMonth, String expiryMonthIndex, String expiryYear) {
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.securityCode = securityCode;
        this.balance = balance;
        this.expiryMonth = expiryMonth;
        this.expiryMonthIndex = expiryMonthIndex;
        this.expiryYear = expiryYear;
        this.has3DSecure = false;
        this.securePassword = null;
    }

    public CreditCard(CardType cardType, String cardNumber, String securityCode, double balance, String expiryMonth, String expiryMonthIndex, String expiryYear, boolean has3DSecure, String securePassword) {
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.securityCode = securityCode;
        this.balance = balance;
        this.expiryMonth = expiryMonth;
        this.expiryMonthIndex = expiryMonthIndex;
        this.expiryYear = expiryYear;
        this.has3DSecure = has3DSecure;
        this.securePassword = securePassword;
    }

    /**
     * Uses a JSONObject from the SIM data service to create a new credit card
     *
     * @param cardData JSONObject from SIM data service
     * @param type     type of card to create
     * @return Credit card object with data from service
     */
    public static CreditCard createCardFromSimService(JSONObject cardData, CardType type) {
        CreditCard card = new CreditCard();
        card.cardType = type;
        card.cardNumber = cardData.getString("Account N");
        card.securityCode = cardData.getString("CVV / CID");
        String expiryData = cardData.getString("Exp");
        card.expiryMonth = expiryData.substring(0, 2);
        card.expiryYear = expiryData.substring(2);
        card.has3DSecure = false;
        card.securePassword = null;
        return card;
    }

    /**
     * Gets the CardType of CreditCard
     *
     * @return CreditCard CardType
     */
    public CardType getCardType() {
        return cardType;
    }

    /**
     * Sets the CardType of CreditCard
     *
     * @param cardType Credit Card Type enum
     */
    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    /**
     * Gets the CardNumber of CreditCard
     *
     * @return CreditCard CardNumber
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
     * Gets the SecurityCode of CreditCard
     *
     * @return CreditCard SecurityCode
     */
    public String getSecurityCode() {
        return securityCode;
    }

    /**
     * Sets the SecurityCode of CreditCard
     *
     * @param securityCode Credit Card
     */
    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    /**
     * Gets the Balance of CreditCard
     *
     * @return CreditCard Balance
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
     * Gets the ExpiryMonth of CreditCard
     *
     * @return CreditCard ExpiryMonth
     */
    public String getExpiryMonth() {
        return expiryMonth;
    }

    /**
     * Sets the ExpiryMonth of CreditCard
     *
     * @param expiryMonth Credit Card
     */
    public void setExpiryMonth(String expiryMonth) {
        if (expiryMonth.length() == 1) {
            this.expiryMonth = "0" + expiryMonth;
        } else {
            this.expiryMonth = expiryMonth;
        }
    }

    /**
     * Gets the ExpiryMonthIndex of CreditCard
     *
     * @return CreditCard ExpiryMonthIndex
     */
    public String getExpiryMonthIndex() {
        return expiryMonthIndex;
    }

    /**
     * Sets the ExpiryMonthIndex of CreditCard
     *
     * @param expiryMonthIndex Credit Card
     */
    public void setExpiryMonthIndex(String expiryMonthIndex) {
        this.expiryMonthIndex = expiryMonthIndex;
    }

    /**
     * Gets the ExpiryYear of CreditCard
     *
     * @return CreditCard ExpiryYear
     */
    public String getExpiryYear() {
        return expiryYear;
    }

    /**
     * Sets the ExpiryYear of CreditCard
     *
     * @param expiryYear Credit Card
     */
    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    /**
     * Gets the SecurePassword of CreditCard
     *
     * @return CreditCard SecurePassword
     */
    public String getSecurePassword() {
        return securePassword;
    }

    /**
     * Sets the SecurePassword of CreditCard
     *
     * @param securePassword Credit Card
     */
    public void setSecurePassword(String securePassword) {
        this.securePassword = securePassword;
    }

    /**
     * Gets the has3DSecure of CreditCard
     *
     * @return CreditCard 3DSecure
     */
    public boolean has3DSecure() {
        return has3DSecure;
    }

    /**
     * Sets the has3DSecure of CreditCard
     *
     * @param has3DSecure Credit Card
     */
    public void setHas3DSecure(boolean has3DSecure) {
        this.has3DSecure = has3DSecure;
    }

    /**
     * Different Card Types
     */
    public enum CardType {
        VISA("Visa", ""),
        MASTER_CARD("MasterCard", ""),
        AMERICAN_EXPRESS("American Express", "AMEX"),
        DISCOVER("Discover", ""),
        MACYS("Macy's", "PROP"),
        MACYS_AMERICAN_EXPRESS("Macy's American Express", "PROP"),
        EMPLOYEE_CARD("Employee Card", ""),
        BLOOMINGDALES("Bloomingdale's", "PROP"),
        BLOOMINGDALES_AMERICAN_EXPRESS("Bloomingdale's American Express", "PROP"),
        BLOOMINGDALES_EMPLOYEE_CARD("Bloomingdale's Employee Card", "");

        public final String name;

        // used by SIM data service
        public final String abbreviation;

        CardType(String name, String abbreviation) {
            this.name = name;
            this.abbreviation = abbreviation;
        }

        public static CardType fromString(String value) {
            for (CardType type : CardType.values())
                if (value.equals(type.name) || value.equals(type.abbreviation)) {
                    return type;
                }
            return null;
        }
    }
}
