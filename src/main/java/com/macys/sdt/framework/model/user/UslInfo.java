package com.macys.sdt.framework.model.user;

/**
 * This class represents a UslInfo and contains all the information about that UslInfo
 */
public class UslInfo {

    private UslType uslType;
    private String plentiId;
    private String uslPhone;
    private String redeemedPlentiPoints;
    private String uslLinkedCard;
    private CardType uslLinkedCardType;
    private String uslPin;

    public UslInfo(UslType uslType, String plentiId, String uslPhone, String uslLinkedCard,
                   CardType uslLinkedCardType, String uslPin, String redeemedPlentiPoints ) {
        this.uslType = uslType;
        this.plentiId = plentiId;
        this.uslPhone = uslPhone;
        this.uslLinkedCardType = uslLinkedCardType;
        this.uslLinkedCard = uslLinkedCard;
        this.uslPin = uslPin;
        this.redeemedPlentiPoints = redeemedPlentiPoints;
    }

    public UslInfo()    {}

    /**
     * Gets the PlentiId of UslInfo
     *
     * @return UslInfo PlentiId
     */
    public String getPlentiId() {
        return plentiId;
    }

    /**
     * Sets the PlentiId of UslInfo
     *
     * @param plentiId UslInfo
     */
    public void setPlentiId(String plentiId) {
        this.plentiId = plentiId;
    }

    /**
     * Gets the UslPhone of UslInfo
     *
     * @return UslInfo UslPhone
     */
    public String getUslPhone() {
        return uslPhone;
    }

    /**
     * Sets the UslPhone of UslInfo
     *
     * @param uslPhone UslInfo
     */
    public void setUslPhone(String uslPhone) {
        this.uslPhone = uslPhone;
    }

    /**
     * Gets the UslPin of UslInfo
     *
     * @return UslInfo UslPin
     */
    public String getUslPin() {
        return uslPin;
    }

    /**
     * Sets the UslPin of UslInfo
     *
     * @param uslPin UslInfo
     */
    public void setUslPin(String uslPin) {
        this.uslPin = uslPin;
    }

    /**
     * Gets the RedeemedPlentiPoints of UslInfo
     *
     * @return UslInfo RedeemedPlentiPoints
     */
    public String getRedeemedPlentiPoints() {
        return redeemedPlentiPoints;
    }

    /**
     * Sets the RedeemedPlentiPoints of UslInfo
     *
     * @param redeemedPlentiPoints UslInfo
     */
    public void setRedeemedPlentiPoints(String redeemedPlentiPoints) {
        this.redeemedPlentiPoints = redeemedPlentiPoints;
    }

    /**
     * Get the credit card linked to plenti
     *
     * @return UslInfo UslLinkedCard
     */
    public String getUslLinkedCard() {
        return uslLinkedCard;
    }

    /**
     * Sets the credit card linked to plenti
     *
     * @param uslLinkedCard UslInfo
     */
    public void setUslLinkedCard(String uslLinkedCard) {
        this.uslLinkedCard = uslLinkedCard;
    }

    /**
     * Get the credit card type linked to plenti
     *
     * @return UslInfo CardType
     */
    public CardType getUslLinkedCardType() {
        return uslLinkedCardType;
    }

    /**
     * Sets the credit card type linked to plenti
     *
     * @param uslLinkedCardType UslInfo
     */
    public void setUslLinkedCardType(CardType uslLinkedCardType) {
        this.uslLinkedCardType = uslLinkedCardType;
    }

    /**
     * Get the Usl type
     *
     * @return UslInfo UslType
     */
    public UslType getUslType() {
        return uslType;
    }

    /**
     * Sets the usl type to plenti
     *
     * @param uslType UslInfo
     */
    public void setUslType(UslType uslType) {
        this.uslType = uslType;
    }

    /**
     * Different Usl Types
     */
    public enum UslType {
        FULL_ENROLLED("fully_enrolled"),
        PRE_ENROLLED("pre_enrolled"),
        ANONYMOUS("anonymous"),
        CANCELLED("cancelled"),
        BLOCKED("blocked");

        private final String name;

        UslType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private static UslType fromString(String value) {
            for (UslType type : UslType.values())
                if (value.equalsIgnoreCase(type.name)) {
                    return type;
                }
            return null;
        }
    }

    /**
     * Different credit card type linked to Usl
     */
    public enum CardType {
        PROP("prop"),
        COBRAND("co_brand");

        private final String name;

        CardType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private static CardType fromString(String value) {
            for (CardType type : CardType.values())
                if (value.equalsIgnoreCase(type.name)) {
                    return type;
                }
            return null;
        }
    }

}