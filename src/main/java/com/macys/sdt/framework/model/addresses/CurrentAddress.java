package com.macys.sdt.framework.model.addresses;

import org.json.JSONObject;

/**
 * The current address of a Registry
 */
public class CurrentAddress extends Address {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String attention;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public void fillFromJson(JSONObject address) {
        this.setAddressLine1(address.getString("address_line_1"));
        this.setAddressLine2(address.getString("address_line_2"));
        this.setCity(address.getString("address_city"));
        this.setState(address.getString("address_state"));
        this.setPostalCode(address.getString("address_zip_code").trim());
        this.setCountry(address.getString("country"));
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
