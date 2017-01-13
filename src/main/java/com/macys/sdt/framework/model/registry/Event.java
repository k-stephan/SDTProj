package com.macys.sdt.framework.model.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.macys.sdt.framework.utils.StatesUtils;

public class Event {
    public String eventMonth;
    public String eventDay;
    public String eventYear;
    public int noOfGuests;
    public String locationStateCode;
    public String locationStateName;

    public void setLocationStateCode(String code) {
        locationStateCode = code;
        locationStateName = StatesUtils.translateAbbreviation(code);
    }

    public void setLocationStateName(String name) {
        locationStateName = name;
        locationStateCode = StatesUtils.getAbbreviation(name);
    }

    @JsonProperty("date")
    public String getDate() {
        return eventMonth + "/" + eventDay + "/" + eventYear;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        eventMonth = date.substring(0, 2);
        eventDay = date.substring(3, 5);
        eventYear = date.substring(6);
    }
}
