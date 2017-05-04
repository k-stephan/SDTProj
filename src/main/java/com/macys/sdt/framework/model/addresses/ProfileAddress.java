package com.macys.sdt.framework.model.addresses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.macys.sdt.framework.utils.TestUsers;
import org.json.JSONObject;

import static com.macys.sdt.framework.utils.TestUsers.*;

/**
 * This class represents a ProfileAddress and contains all the information about that ProfileAddress
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileAddress extends Address {
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
    private String zipCode;
    private String countryCode;
    private String country;
    private String email;
    private String bestPhone;
    private Boolean primaryFlag = true;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String province;

    public ProfileAddress() {}

    public ProfileAddress(Long id, String attention, Long sequenceNumber, String firstName, String lastName,
                          String middleName, String addressLine1, String addressLine2, String city, String state,
                          String zipCode, String countryCode, String email, String bestPhone, Boolean primaryFlag) {
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

    /**
     * Gets the DefaultProfileAddress of ProfileAddress
     *
     * @return ProfileAddress DefaultProfileAddress
     */
    public static ProfileAddress getDefaultProfileAddress() {
        return new ProfileAddress(null, "testattention", 11L, "first", "last", "middle", "postbox", "AP", "Hyderabad",
                "AL", "32701", "USA", TestUsers.generateRandomEmail(7), "123-444-5577", true);
    }

    public void fillFromJson(JSONObject address) {
        this.setFirstName(generateRandomFirstName());
        this.setLastName(generateRandomLastName());
        this.setEmail(generateRandomEmail(16));
        this.setAddressLine1(address.getString("address_line_1"));
        this.setAddressLine2(address.getString("address_line_2"));
        this.setCity(address.getString("address_city"));
        this.setState(address.getString("address_state"));
        this.setZipCode(address.getString("address_zip_code").trim());
        this.setBestPhone(generateRandomPhoneNumber());
        this.setCountry(address.getString("country"));
        if (address.has("country_code")) {
            this.setCountryCode(address.getString("country_code"));
        } else {
            this.setCountryCode("US");
        }
    }

    /**
     * Gets the country of the ProfileAdress
     *
     * @return name of country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country of ProfileAddress
     *
     * @param country name of country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the Id of ProfileAddress
     *
     * @return ProfileAddress Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the Id of ProfileAddress
     *
     * @param id Profile Address
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the Attention of ProfileAddress
     *
     * @return ProfileAddress Attention
     */
    public String getAttention() {
        return attention;
    }

    /**
     * Sets the Attention of ProfileAddress
     *
     * @param attention Profile Address
     */
    public void setAttention(String attention) {
        this.attention = attention;
    }

    /**
     * Gets the SequenceNumber of ProfileAddress
     *
     * @return ProfileAddress SequenceNumber
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the SequenceNumber of ProfileAddress
     *
     * @param sequenceNumber Profile Address
     */
    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the FirstName of ProfileAddress
     *
     * @return ProfileAddress FirstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the FirstName of ProfileAddress
     *
     * @param firstName Profile Address
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the LastName of ProfileAddress
     *
     * @return ProfileAddress LastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the LastName of ProfileAddress
     *
     * @param lastName Profile Address
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the MiddleName of ProfileAddress
     *
     * @return ProfileAddress MiddleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets the MiddleName of ProfileAddress
     *
     * @param middleName Profile Address
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Gets the AddressLine1 of ProfileAddress
     *
     * @return ProfileAddress AddressLine1
     */
    public String getAddressLine1() {
        return addressLine1;
    }

    /**
     * Sets the AddressLine1 of ProfileAddress
     *
     * @param addressLine1 Profile Address
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * Gets the AddressLine2 of ProfileAddress
     *
     * @return ProfileAddress AddressLine2
     */
    public String getAddressLine2() {
        return addressLine2;
    }

    /**
     * Sets the AddressLine2 of ProfileAddress
     *
     * @param addressLine2 Profile Address
     */
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * Gets the City of ProfileAddress
     *
     * @return ProfileAddress City
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the City of ProfileAddress
     *
     * @param city Profile Address
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the State of ProfileAddress
     *
     * @return ProfileAddress State
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the State of ProfileAddress
     *
     * @param state Profile Address
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the ZipCode of ProfileAddress
     *
     * @return ProfileAddress ZipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the ZipCode of ProfileAddress
     *
     * @param zipCode Profile Address
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Gets the CountryCode of ProfileAddress
     *
     * @return ProfileAddress CountryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the CountryCode of ProfileAddress
     *
     * @param countryCode Profile Address
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Gets the PrimaryFlag of ProfileAddress
     *
     * @return ProfileAddress PrimaryFlag
     */
    public Boolean getPrimaryFlag() {
        return primaryFlag;
    }

    /**
     * Sets the PrimaryFlag of ProfileAddress
     *
     * @param primaryFlag Profile Address
     */
    public void setPrimaryFlag(Boolean primaryFlag) {
        this.primaryFlag = primaryFlag;
    }

    /**
     * Gets the Email of ProfileAddress
     *
     * @return ProfileAddress Email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the Email of ProfileAddress
     *
     * @param email Profile Address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the BestPhone of ProfileAddress
     *
     * @return ProfileAddress BestPhone
     */
    public String getBestPhone() {
        if (bestPhone == null) {
            return null;
        }
        // phone number cannot be started with '0' or '1'
        char[] notStartWith1 = bestPhone.toCharArray();
        if (notStartWith1[0] == '0' || notStartWith1[0] == '1') {
            notStartWith1[0] = '2';
        }
        return String.valueOf(notStartWith1);
    }

    /**
     * Sets the BestPhone of ProfileAddress
     *
     * @param bestPhone Profile Address
     */
    public void setBestPhone(String bestPhone) {
        this.bestPhone = bestPhone;
    }

    /**
     * Gets the PhoneAreaCode of ProfileAddress
     *
     * @return ProfileAddress PhoneAreaCode
     */
    public String getPhoneAreaCode() {
        if (bestPhone == null) {
            return null;
        }
        return bestPhone.substring(0, 3);
    }

    /**
     * Gets the PhoneExchange of ProfileAddress
     *
     * @return ProfileAddress PhoneExchange
     */
    public String getPhoneExchange() {
        if (bestPhone == null) {
            return null;
        }
        return bestPhone.substring(3, 6);
    }

    /**
     * Gets the PhoneSubscriber of ProfileAddress
     *
     * @return ProfileAddress PhoneSubscriber
     */
    public String getPhoneSubscriber() {
        if (bestPhone == null) {
            return null;
        }
        return bestPhone.substring(6, 10);
    }

    /**
     * Gets the Province of ProfileAddress
     *
     * @return ProfileAddress Province
     */
    public String getProvince() {
        return province;
    }

    /**
     * Sets the Province of ProfileAddress
     *
     * @param province Profile Address
     */
    public void setProvince(String province) {
        this.province = province;
    }
}
