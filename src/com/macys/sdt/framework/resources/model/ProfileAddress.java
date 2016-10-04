package com.macys.sdt.framework.resources.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.macys.sdt.framework.utils.TestUsers;

public class ProfileAddress {
    private Long id;
    private String attention;
    private Long sequenceNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private Integer zipCode;
    private String countryCode;
    private String email;
    private String bestPhone;
    private Boolean primaryFlag;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String province;

    public ProfileAddress() {

    }

    public ProfileAddress(Long id, String attention, Long sequenceNumber, String firstName, String lastName,
                          String middleName, String addressLine1, String addressLine2, String city, String state,
                          Integer zipCode, String countryCode, String email, String bestPhone, Boolean primaryFlag) {
        this.id = id;
        this.attention = attention;
        this.sequenceNumber = sequenceNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.countryCode = countryCode;
        this.email = email;
        this.bestPhone = bestPhone;
        this.primaryFlag = primaryFlag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
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

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean getPrimaryFlag() {
        return primaryFlag;
    }

    public void setPrimaryFlag(Boolean primaryFlag) {
        this.primaryFlag = primaryFlag;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBestPhone() {
        // phone number cannot be started with '0' or '1'
        char[] notStartWith1 = bestPhone.toCharArray();
        if (notStartWith1[0] == '0' || notStartWith1[0] == '1') {
            notStartWith1[0] = '2';
        }
        return String.valueOf(notStartWith1);
    }

    public void setBestPhone(String bestPhone) {
        this.bestPhone = bestPhone;
        String area_code = TestUsers.generateRandomPhoneAreaCodeExchange();
        while (area_code.length() != 3)
            area_code = TestUsers.generateRandomPhoneAreaCodeExchange();
        String phone_number = TestUsers.generateRandomPhoneSubscriber();
        while (phone_number.length() != 4)
            phone_number = TestUsers.generateRandomPhoneSubscriber();
        if (this.bestPhone.length() != 10) {
            this.bestPhone += area_code + phone_number;
        }
        if (this.bestPhone.length() > 10) {
            this.bestPhone = this.bestPhone.substring(0, 10);
        }
    }

    public String getPhoneAreaCode() {
        return bestPhone.substring(0, 3);
    }

    public String getPhoneExchange() {
        return bestPhone.substring(3, 6);
    }

    public String getPhoneSubscriber() {
        return bestPhone.substring(6, 10);
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
