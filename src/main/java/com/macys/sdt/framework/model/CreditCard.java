package com.macys.sdt.framework.model;

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

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryMonthIndex() {
        return expiryMonthIndex;
    }

    public void setExpiryMonthIndex(String expiryMonthIndex) {
        this.expiryMonthIndex = expiryMonthIndex;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getSecurePassword() {
        return securePassword;
    }

    public void setSecurePassword(String securePassword) {
        this.securePassword = securePassword;
    }

    public boolean has3DSecure() {
        return has3DSecure;
    }

    public void setHas3DSecure(boolean has3DSecure) {
        this.has3DSecure = has3DSecure;
    }

    public enum CardType {
        VISA("Visa"),
        MASTER_CARD("MasterCard"),
        AMERICAN_EXPRESS("American Express"),
        DISCOVER("Discover");

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
