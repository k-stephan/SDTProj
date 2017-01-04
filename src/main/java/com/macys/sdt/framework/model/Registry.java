package com.macys.sdt.framework.model;

/**
 * This class represents a Registry and contains all the information about that Registry
 */
//@SuppressWarnings("unused")
public class Registry {

    private String eventType;
    private String eventMonth;
    private String eventDay;
    private String eventYear;
    private String eventLocation;
    private String numberOfGuest;
    private String preferredStoreState;
    private String preferredStore;
    private String coRegistrantFirstName;
    private String coRegistrantLastName;
    private String id;

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
     * @param eventType Registry
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
        return eventMonth;
    }

    /**
     * Sets the EventMonth of Registry
     *
     * @param eventMonth Registry
     */
    public void setEventMonth(String eventMonth) {
        this.eventMonth = eventMonth;
    }

    /**
     * Gets the EventDay of Registry
     *
     * @return Registry EventDay
     */
    public String getEventDay() {
        return eventDay;
    }

    /**
     * Sets the EventDay of Registry
     *
     * @param eventDay Registry
     */
    public void setEventDay(String eventDay) {
        this.eventDay = eventDay;
    }

    /**
     * Gets the EventYear of Registry
     *
     * @return Registry EventYear
     */
    public String getEventYear() {
        return eventYear;
    }

    /**
     * Sets the EventYear of Registry
     *
     * @param eventYear Registry
     */
    public void setEventYear(String eventYear) {
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
}
