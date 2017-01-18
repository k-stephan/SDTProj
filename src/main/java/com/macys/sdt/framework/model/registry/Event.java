package com.macys.sdt.framework.model.registry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.macys.sdt.framework.utils.AbbreviationHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {
    @JsonIgnore
    private String eventMonthText;
    @JsonIgnore
    private String eventMonthNum;
    @JsonIgnore
    private String eventDay;
    @JsonIgnore
    private String eventYear;
    private int noOfGuests = 200;
    private String locationStateCode;
    private String locationStateName;

    @JsonSetter("locationStateCode")
    public void setLocationStateCode(String code) {
        locationStateCode = code;
        locationStateName = AbbreviationHelper.translateStateAbbreviation(code);
    }

    @JsonSetter("locationStateName")
    public void setLocationStateName(String name) {
        locationStateName = name;
        locationStateCode = AbbreviationHelper.getStateAbbreviation(name);
    }

    @JsonProperty("date")
    public String getDate() {
        return eventMonthNum + "/" + eventDay + "/" + eventYear;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        eventMonthNum = date.substring(0, 2);
        eventMonthText = AbbreviationHelper.translateMonthAbbreviation(eventMonthNum);
        eventDay = date.substring(3, 5);
        eventYear = date.substring(6);
    }

    @JsonSetter("eventMonth")
    public void setEventMonth(String month) {
        month = month.trim();
        if (month.matches("[0-9][1-9]?")) {
            eventMonthNum = month;
            eventMonthText = AbbreviationHelper.translateMonthAbbreviation(month);
        } else {
            eventMonthText = month;
            eventMonthNum = AbbreviationHelper.getMonthAbbreviation(month);
        }
    }

    public int getNoOfGuests() {
        return noOfGuests;
    }

    public void setNoOfGuests(int noOfGuests) {
        this.noOfGuests = noOfGuests;
    }

    public String getLocationStateCode() {
        return locationStateCode;
    }

    public String getLocationStateName() {
        return locationStateName;
    }

    public String getEventMonthText() {
        return eventMonthText;
    }

    public void setEventMonthText(String eventMonthText) {
        this.setEventMonth(eventMonthText);
    }

    public String getEventMonthNum() {
        return eventMonthNum;
    }

    public void setEventMonthNum(String eventMonthNum) {
        this.setEventMonth(eventMonthNum);
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
}
