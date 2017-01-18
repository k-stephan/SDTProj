package com.macys.sdt.framework.model.registry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.macys.sdt.framework.model.addresses.CurrentAddress;
import com.macys.sdt.framework.model.addresses.FutureAddress;

import java.util.Random;

import static com.macys.sdt.framework.utils.TestUsers.*;

/**
 * This class represents a Registry and contains all the information about that Registry
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Registry {

    // These fields are to satisfy the format of the Registry service representation of a registry
    private String userId;
    private String guestMessage = "Welcome to registry";
    private String weddingWebsite = "www.mywedding.com";
    private String registryType = "ANNIVERSARY";
    private String sourceType = "Internet";
    private String macysAccountNumber;
    private Event event = new Event();
    private Preferences preferences = new Preferences();
    private ContactInfo contactInfo = new ContactInfo();
    private ContactInfo coRegistrantContactInfo = new ContactInfo();
    private CurrentAddress currentAddress = new CurrentAddress();
    private FutureAddress futureAddress = new FutureAddress();

    // These fields are to satisfy the format of the User Service representation of a registry
    private String eventLocation;
    private String numberOfGuest;
    private String preferredStoreState;
    private String preferredStore;
    private String coRegistrantFirstName;
    private String coRegistrantLastName;
    private String id;
    private String eventMonth;
    private String eventDay;
    private String eventYear;

    // These fields are shared between the two
    @JsonProperty("type")
    public String eventType = "WEDDING";

    /**
     * Creates a copy of a registry with only the values present in User Service version of registry
     *
     * @param registry registry with data to copy
     * @return registry with user service values
     */
    public static Registry getUserServiceRegistry(Registry registry) {
        Registry copy = new Registry();
        copy.eventLocation = registry.eventLocation;
        copy.numberOfGuest = registry.numberOfGuest;
        copy.preferredStore = registry.preferredStore;
        copy.preferredStoreState = registry.preferredStoreState;
        copy.coRegistrantFirstName = registry.coRegistrantFirstName;
        copy.coRegistrantLastName = registry.coRegistrantLastName;
        copy.id = registry.id;
        copy.eventType = registry.eventType;
        copy.eventMonth = registry.eventMonth;
        copy.eventDay = registry.eventDay;
        copy.eventYear = registry.eventYear;

        // null out all values filled in by default
        copy.guestMessage = null;
        copy.userId = null;
        copy.weddingWebsite = null;
        copy.registryType = null;
        copy.sourceType = null;
        copy.macysAccountNumber = null;
        copy.event = null;
        copy.preferences = null;
        copy.contactInfo = null;
        copy.coRegistrantContactInfo = null;
        copy.currentAddress = null;
        copy.futureAddress = null;

        return copy;
    }

    /**
     * Creates a copy of a registry with only the values present in Registry Service version of registry
     *
     * @param registry registry with data to copy
     * @return registry with registry service values
     */
    public static Registry getRegistryServiceRegistry(Registry registry) {
        Registry copy = new Registry();
        copy.eventLocation = null;
        copy.numberOfGuest = null;
        copy.preferredStore = null;
        copy.preferredStoreState = null;
        copy.coRegistrantFirstName = null;
        copy.coRegistrantLastName = null;
        copy.id = null;
        copy.eventMonth = null;
        copy.eventDay = null;
        copy.eventYear = null;

        copy.eventType = registry.eventType;
        copy.userId = registry.userId;
        copy.guestMessage = registry.guestMessage;
        copy.weddingWebsite = registry.weddingWebsite;
        copy.registryType = registry.registryType;
        copy.sourceType = registry.sourceType;
        copy.macysAccountNumber = registry.macysAccountNumber;
        copy.event = registry.event;
        copy.preferences = registry.preferences;
        copy.contactInfo = registry.contactInfo;
        copy.coRegistrantContactInfo = registry.coRegistrantContactInfo;
        copy.currentAddress = registry.currentAddress;
        copy.futureAddress = registry.futureAddress;

        return copy;
    }

    /**
     * Gets the EventType of Registry
     *
     * @return Registry EventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the EventType of Registry
     *
     * @param eventType Event eventType
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Gets the EventMonth of Registry
     *
     * @return Registry EventMonth
     */
    public String getEventMonth() {
        return event.getEventMonthText();
    }

    /**
     * Sets the EventMonth of Registry
     *
     * @param eventMonth Registry
     */
    public void setEventMonth(String eventMonth) {
        event.setEventMonth(eventMonth);
        this.eventMonth = eventMonth;
    }

    /**
     * Gets the EventDay of Registry
     *
     * @return Registry EventDay
     */
    public String getEventDay() {
        return event.getEventDay();
    }

    /**
     * Sets the EventDay of Registry
     *
     * @param eventDay Registry
     */
    public void setEventDay(String eventDay) {
        event.setEventDay(eventDay);
        this.eventDay = eventDay;
    }

    /**
     * Gets the EventYear of Registry
     *
     * @return Registry EventYear
     */
    public String getEventYear() {
        return event.getEventYear();
    }

    /**
     * Sets the EventYear of Registry
     *
     * @param eventYear Registry
     */
    public void setEventYear(String eventYear) {
        event.setEventYear(eventYear);
        this.eventYear = eventYear;
    }

    /**
     * Gets the EventLocation of Registry
     *
     * @return Registry EventLocation
     */
    public String getEventLocation() {
        return eventLocation;
    }

    /**
     * Sets the EventLocation of Registry
     *
     * @param eventLocation Registry
     */
    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    /**
     * Gets the NumberOfGuest of Registry
     *
     * @return Registry NumberOfGuest
     */
    public String getNumberOfGuest() {
        return numberOfGuest;
    }

    /**
     * Sets the NumberOfGuest of Registry
     *
     * @param numberOfGuest Registry
     */
    public void setNumberOfGuest(String numberOfGuest) {
        this.numberOfGuest = numberOfGuest;
        this.event.setNoOfGuests(Integer.valueOf(numberOfGuest));
    }

    /**
     * Gets the PreferredStoreState of Registry
     *
     * @return Registry PreferredStoreState
     */
    public String getPreferredStoreState() {
        return preferredStoreState;
    }

    /**
     * Sets the PreferredStoreState of Registry
     *
     * @param preferredStoreState Registry
     */
    public void setPreferredStoreState(String preferredStoreState) {
        this.preferredStoreState = preferredStoreState;
    }

    /**
     * Gets the PreferredStore of Registry
     *
     * @return Registry PreferredStore
     */
    public String getPreferredStore() {
        return preferredStore;
    }

    /**
     * Sets the PreferredStore of Registry
     *
     * @param preferredStore Registry
     */
    public void setPreferredStore(String preferredStore) {
        this.preferredStore = preferredStore;
    }

    /**
     * Gets the CoRegistrantFirstName of Registry
     *
     * @return Registry CoRegistrantFirstName
     */
    public String getCoRegistrantFirstName() {
        return coRegistrantFirstName;
    }

    /**
     * Sets the CoRegistrantFirstName of Registry
     *
     * @param coRegistrantFirstName Registry
     */
    public void setCoRegistrantFirstName(String coRegistrantFirstName) {
        this.coRegistrantFirstName = coRegistrantFirstName;
        this.coRegistrantContactInfo.setFirstName(coRegistrantFirstName);
    }

    /**
     * Gets the CoRegistrantLastName of Registry
     *
     * @return Registry CoRegistrantLastName
     */
    public String getCoRegistrantLastName() {
        return coRegistrantLastName;
    }

    /**
     * Sets the CoRegistrantLastName of Registry
     *
     * @param coRegistrantLastName Registry
     */
    public void setCoRegistrantLastName(String coRegistrantLastName) {
        this.coRegistrantLastName = coRegistrantLastName;
        this.coRegistrantContactInfo.setLastName(coRegistrantLastName);
    }

    /**
     * Gets the Id of Registry
     *
     * @return Registry Id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the Id of Registry
     *
     * @param id Registry
     */
    public void setId(String id) {
        this.id = id;
    }



    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGuestMessage() {
        return guestMessage;
    }

    public void setGuestMessage(String guestMessage) {
        this.guestMessage = guestMessage;
    }

    public String getWeddingWebsite() {
        return weddingWebsite;
    }

    public void setWeddingWebsite(String weddingWebsite) {
        this.weddingWebsite = weddingWebsite;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getMacysAccountNumber() {
        return macysAccountNumber;
    }

    public void setMacysAccountNumber(String macysAccountNumber) {
        this.macysAccountNumber = macysAccountNumber;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public ContactInfo getCoRegistrantContactInfo() {
        return coRegistrantContactInfo;
    }

    public void setCoRegistrantContactInfo(ContactInfo coRegistrantContactInfo) {
        this.coRegistrantContactInfo = coRegistrantContactInfo;
    }

    public CurrentAddress getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(CurrentAddress currentAddress) {
        this.currentAddress = currentAddress;
    }

    public FutureAddress getFutureAddress() {
        return futureAddress;
    }

    public void setFutureAddress(FutureAddress futureAddress) {
        this.futureAddress = futureAddress;
    }

    public void addRandomData() {
        // ContactInfo data
        String firstName = generateRandomFirstName();
        coRegistrantContactInfo.setFirstName(firstName);
        coRegistrantContactInfo.setLastName(generateRandomLastName());
        coRegistrantContactInfo.setBestPhone(generateRandomPhoneNumber());
        coRegistrantContactInfo.setEmail(generateRandomEmail(10));
        CurrentAddress coAddress = coRegistrantContactInfo.getCurrentAddress();
        getRandomValidAddress(null, coAddress);
        coAddress.setAttention(firstName);

        // CoRegistrant ContactInfo data
        firstName = generateRandomFirstName();
        contactInfo.setFirstName(firstName);
        contactInfo.setLastName(generateRandomLastName());
        contactInfo.setBestPhone(generateRandomPhoneNumber());
        contactInfo.setEmail(generateRandomEmail(10));
        getRandomValidAddress(null, currentAddress);
        currentAddress.setAttention(firstName);
        contactInfo.setCurrentAddress(currentAddress);

        // Event data
        event.setDate("01/01/2020");
        event.setLocationStateName("Alaska");
        event.setLocationStateCode("AK");

        // FutureAddress data
        getRandomValidAddress(null, futureAddress);

        // other data
        userId = "";
        macysAccountNumber = "";
        setCoRegistrantFirstName(generateRandomFirstName());
        setCoRegistrantLastName(generateRandomLastName());
        setEventType("WEDDING");
        setEventMonth("April");
        setEventDay("18");
        setEventYear("2016");
        setEventLocation("Alaska");
        setNumberOfGuest("110");
        setPreferredStoreState("New York");
        setPreferredStore("New York - Herald Square");
    }
}
