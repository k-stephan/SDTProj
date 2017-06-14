package com.macys.sdt.framework.model.user;

/**
 * This class represents a Loyalist and contains all the details about that Loyalist
 */
public class LoyalistDetails {

    private LoyallistType loyallistType;
    private String loyaltyId;
    private String lastName;
    private String zipCode;
    private String cardNumber;
    private String securityCode;
    private String billingLastName;
    private String billingZipCode;

    public LoyalistDetails(LoyallistType loyallistType, String loyaltyId, String lastName, String zipCode,
                   String cardNumber, String securityCode, String billingLastName, String billingZipCode ) {
        this.loyallistType = loyallistType;
        this.loyaltyId = loyaltyId;
        this.lastName = lastName;
        this.zipCode = zipCode;
        this.cardNumber = cardNumber;
        this.securityCode = securityCode;
        this.billingLastName = billingLastName;
        this.billingZipCode = billingZipCode;
    }

    /**
     * Gets the loyalty type of Loyalist
     *
     * @return loyalty type
     */
    public LoyallistType getLoyallistType() {
        return loyallistType;
    }

    /**
     * Sets the loyalty type of Loyalist
     *
     * @param loyallistType loyalty type of Loyalist
     */
    public void setLoyallistType(LoyallistType loyallistType) {
        this.loyallistType = loyallistType;
    }

    /**
     * Gets the loyalty id of Loyalist
     *
     * @return Loyalty id of Loyalist
     */
    public String getLoyaltyId() {
        return loyaltyId;
    }

    /**
     * Sets the loyalty id of Loyalist
     *
     * @param loyaltyId loyalty id of Loyalist
     */
    public void setLoyaltyId(String loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    /**
     * Gets the last name of Loyalist
     *
     * @return last name of Loyalist
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of Loyalist
     *
     * @param lastName Last name of Loyalist
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the Zip code of Loyalist
     *
     * @return Zip code of Loyalist
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the the Zip code of Loyalist
     *
     * @param zipCode Zip code of Loyalist
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Gets the Card number of Loyalist
     *
     * @return Card number of Loyalist
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Sets the Card number of Loyalist
     *
     * @param cardNumber Card number of Loyalist
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Gets the Security code of Loyalist
     *
     * @return Security code of Loyalist
     */
    public String getSecurityCode() {
        return securityCode;
    }

    /**
     * Sets the Security code of Loyalist
     *
     * @param securityCode Security code of Loyalist
     */
    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    /**
     * Gets the billing last name of Loyalist
     *
     * @return billing last name of Loyalist
     */
    public String getBillingLastName() {
        return billingLastName;
    }

    /**
     * Sets the billing last name of Loyalist
     *
     * @param billingLastName last name of Loyalist
     */
    public void setBillingLastName(String billingLastName) {
        this.billingLastName = billingLastName;
    }

    /**
     * Gets the billing zip code of Loyalist
     *
     * @return billing zip code of Loyalist
     */
    public String getBillingZipCode() {
        return billingZipCode;
    }

    /**
     * Sets the billing zip code of Loyalist
     *
     * @param billingZipCode billing zip code of Loyalist
     */
    public void setBillingZipCode(String billingZipCode) {
        this.billingZipCode = billingZipCode;
    }

    /**
     * Different Loyallist Types
     */
    public enum LoyallistType {
        TOP_TIER("top_tier"),
        BASE_TIER("base_tier"),
        THIRDPARTY("thirdparty"),
        REWARD("reward"),
        INACTIVE("inactive");

        private final String name;

        LoyallistType(String name) {
            this.name = name;
        }

        private static LoyallistType fromString(String value) {
            for (LoyallistType type : LoyallistType.values())
                if (value.equalsIgnoreCase(type.name)) {
                    return type;
                }
            return null;
        }
    }
}
