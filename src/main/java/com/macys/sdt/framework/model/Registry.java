package com.macys.sdt.framework.model;

@SuppressWarnings("unused")
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventMonth() {
        return eventMonth;
    }

    public void setEventMonth(String eventMonth) {
        this.eventMonth = eventMonth;
    }

    public String getEventDay() {
        return eventDay;
    }

    public void setEventDay(String eventDay) {
        this.eventDay = eventDay;
    }

    public String getEventYear() {
        return eventYear;
    }

    public void setEventYear(String eventYear) {
        this.eventYear = eventYear;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getNumberOfGuest() {
        return numberOfGuest;
    }

    public void setNumberOfGuest(String numberOfGuest) {
        this.numberOfGuest = numberOfGuest;
    }

    public String getPreferredStoreState() {
        return preferredStoreState;
    }

    public void setPreferredStoreState(String preferredStoreState) {
        this.preferredStoreState = preferredStoreState;
    }

    public String getPreferredStore() {
        return preferredStore;
    }

    public void setPreferredStore(String preferredStore) {
        this.preferredStore = preferredStore;
    }

    public String getCoRegistrantFirstName() {
        return coRegistrantFirstName;
    }

    public void setCoRegistrantFirstName(String coRegistrantFirstName) {
        this.coRegistrantFirstName = coRegistrantFirstName;
    }

    public String getCoRegistrantLastName() {
        return coRegistrantLastName;
    }

    public void setCoRegistrantLastName(String coRegistrantLastName) {
        this.coRegistrantLastName = coRegistrantLastName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
