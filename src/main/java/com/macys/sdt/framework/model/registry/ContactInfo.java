package com.macys.sdt.framework.model.registry;

import com.macys.sdt.framework.model.addresses.CurrentAddress;

/**
 * Represents the contact info for a registrant in a registry
 */
public class ContactInfo {

    private String firstName;
    private String lastName;
    private String middleName;
    private String namePrefix;
    private String nameSuffix;
    private String bestPhone;
    private String email;
    private CurrentAddress currentAddress = new CurrentAddress();

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

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getBestPhone() {
        return bestPhone;
    }

    public void setBestPhone(String bestPhone) {
        this.bestPhone = bestPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CurrentAddress getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(CurrentAddress currentAddress) {
        this.currentAddress = currentAddress;
    }

}
