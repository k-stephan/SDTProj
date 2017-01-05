package com.macys.sdt.framework.model;

/**
 * This class represents a UslInfo and contains all the information about that UslInfo
 */
public class UslInfo {

    private String plentiId;
    private String uslPhone;
    private String uslPin;
    private String redeemedPlentiPoints;

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
}