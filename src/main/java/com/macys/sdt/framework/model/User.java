package com.macys.sdt.framework.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User {

    private String dateOfBirth;
    private String gender;
    private Boolean subscribedToNewsLetter;
    private UserPasswordHint userPasswordHint;
    private ProfileAddress profileAddress;
    private LoginCredentials loginCredentials;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UslInfo uslInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LoyalistDetails loyalistDetails;

    public User() {

    }

    public User(String dateOfBirth, String gender, Boolean subscribedToNewsLetter, UserPasswordHint userPasswordHint, ProfileAddress profileAddress, LoginCredentials loginCredentials) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.subscribedToNewsLetter = subscribedToNewsLetter;
        this.userPasswordHint = userPasswordHint;
        this.profileAddress = profileAddress;
        this.loginCredentials = loginCredentials;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getDateOfBirth(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getSubscribedToNewsLetter() {
        return subscribedToNewsLetter;
    }

    public void setSubscribedToNewsLetter(Boolean subscribedToNewsLetter) {
        this.subscribedToNewsLetter = subscribedToNewsLetter;
    }

    public UserPasswordHint getUserPasswordHint() {
        return userPasswordHint;
    }

    public void setUserPasswordHint(UserPasswordHint userPasswordHint) {
        this.userPasswordHint = userPasswordHint;
    }

    public ProfileAddress getProfileAddress() {
        return profileAddress;
    }

    public void setProfileAddress(ProfileAddress profileAddress) {
        this.profileAddress = profileAddress;
    }

    public LoginCredentials getLoginCredentials() {
        return loginCredentials;
    }

    public void setLoginCredentials(LoginCredentials loginCredentials) {
        this.loginCredentials = loginCredentials;
    }

    public UslInfo getUslInfo() {
        return uslInfo;
    }

    public void setUslInfo(UslInfo uslInfo) {
        this.uslInfo = uslInfo;
    }

    public LoyalistDetails getLoyalistDetails() {
        return loyalistDetails;
    }

    public void setLoyalistDetails(LoyalistDetails loyalistDetails) {
        this.loyalistDetails = loyalistDetails;
    }
}

