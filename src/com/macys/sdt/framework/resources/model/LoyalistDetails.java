package com.macys.sdt.framework.resources.model;

public class LoyalistDetails {

    private String loyallistType;
    private String loyaltyId;
    private String lastName;
    private String zipCode;
    private String cardNumber;
    private String securityCode;
    private String billingLastName;
    private String billingZipCode;

    public String getLoyallistType() {
        return loyallistType;
    }

    public void setLoyallistType(String loyallistType) {
        this.loyallistType = loyallistType;
    }

    public String getLoyaltyId() {
        return loyaltyId;
    }

    public void setLoyaltyId(String loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
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

    public String getBillingLastName() {
        return billingLastName;
    }

    public void setBillingLastName(String billingLastName) {
        this.billingLastName = billingLastName;
    }

    public String getBillingZipCode() {
        return billingZipCode;
    }

    public void setBillingZipCode(String billingZipCode) {
        this.billingZipCode = billingZipCode;
    }
}
