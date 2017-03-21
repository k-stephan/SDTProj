package com.macys.sdt.framework.model.user;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.macys.sdt.framework.model.addresses.ProfileAddress;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * This class represents a User and contains all the information about that User
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName("user")
public class User {

    private String dateOfBirth;
    private String gender;
    private Boolean subscribedToNewsLetter = true;
    private UserPasswordHint userPasswordHint;
    private ProfileAddress profileAddress;
    private LoginCredentials loginCredentials;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TokenCredentials tokenCredentials;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

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

    public TokenCredentials getTokenCredentials() {
        return tokenCredentials;
    }

    public void setTokenCredentials(TokenCredentials tokenCredentials) {
        this.tokenCredentials = tokenCredentials;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the DefaultUser of User
     *
     * @return User DefaultUser
     */
    public static User getDefaultUser() {
        UserPasswordHint hint = UserPasswordHint.getDefaultUserPasswordHint();
        ProfileAddress address = ProfileAddress.getDefaultProfileAddress();
        LoginCredentials credentials = LoginCredentials.getDefaultLoginCredentials();
        return new User("1989-11-09", "Male", true, hint, address, credentials);
    }

    /**
     * Gets the DateOfBirth of User
     *
     * @return User DateOfBirth
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the DateOfBirth of User
     *
     * @param dateOfBirth User
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Gets the DateOfBirth of User
     *
     * @param date User DateOfBirth as String
     * @return User DateOfBirth in Date Time Formatter
     */
    public LocalDate getDateOfBirth(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Gets the Gender of User
     *
     * @return User Gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the Gender of User
     *
     * @param gender User
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Gets the SubscribedToNewsLetter of User
     *
     * @return User SubscribedToNewsLetter
     */
    public Boolean getSubscribedToNewsLetter() {
        return subscribedToNewsLetter;
    }

    /**
     * Sets the SubscribedToNewsLetter of User
     *
     * @param subscribedToNewsLetter User
     */
    public void setSubscribedToNewsLetter(Boolean subscribedToNewsLetter) {
        this.subscribedToNewsLetter = subscribedToNewsLetter;
    }

    /**
     * Gets the UserPasswordHint of User
     *
     * @return User UserPasswordHint
     */
    public UserPasswordHint getUserPasswordHint() {
        return userPasswordHint;
    }

    /**
     * Sets the UserPasswordHint of User
     *
     * @param userPasswordHint User
     */
    public void setUserPasswordHint(UserPasswordHint userPasswordHint) {
        this.userPasswordHint = userPasswordHint;
    }

    /**
     * Gets the ProfileAddress of User
     *
     * @return User ProfileAddress
     */
    public ProfileAddress getProfileAddress() {
        return profileAddress;
    }

    /**
     * Sets the ProfileAddress of User
     *
     * @param profileAddress User
     */
    public void setProfileAddress(ProfileAddress profileAddress) {
        this.profileAddress = profileAddress;
    }

    /**
     * Gets the LoginCredentials of User
     *
     * @return User LoginCredentials
     */
    public LoginCredentials getLoginCredentials() {
        return loginCredentials;
    }

    /**
     * Sets the LoginCredentials of User
     *
     * @param loginCredentials User
     */
    public void setLoginCredentials(LoginCredentials loginCredentials) {
        this.loginCredentials = loginCredentials;
    }

    /**
     * Gets the UslInfo of User
     *
     * @return User UslInfo
     */
    public UslInfo getUslInfo() {
        return uslInfo;
    }

    /**
     * Sets the UslInfo of User
     *
     * @param uslInfo User
     */
    public void setUslInfo(UslInfo uslInfo) {
        this.uslInfo = uslInfo;
    }

    /**
     * Gets the LoyalistDetails of User
     *
     * @return User LoyalistDetails
     */
    public LoyalistDetails getLoyalistDetails() {
        return loyalistDetails;
    }

    /**
     * Sets the LoyalistDetails of User
     *
     * @param loyalistDetails User
     */
    public void setLoyalistDetails(LoyalistDetails loyalistDetails) {
        this.loyalistDetails = loyalistDetails;
    }
}

