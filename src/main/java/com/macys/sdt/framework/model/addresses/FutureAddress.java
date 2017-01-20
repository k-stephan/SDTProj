package com.macys.sdt.framework.model.addresses;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * The future address of a registry
 */
public class FutureAddress extends CurrentAddress {

    private String effectiveDate;
    private String futureShipAddressEffectiveDate;
    private String futureShippingAddressSource = "REGISTRANT";
    private String shippingAddressSource = "REGISTRANT";
    private String creationType = "Internet";

    public void fillFromJson(JSONObject address) {
        super.fillFromJson(address);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 5);
        effectiveDate = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR);
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getFutureShipAddressEffectiveDate() {
        return futureShipAddressEffectiveDate;
    }

    public void setFutureShipAddressEffectiveDate(String futureShipAddressEffectiveDate) {
        this.futureShipAddressEffectiveDate = futureShipAddressEffectiveDate;
    }

    public String getFutureShippingAddressSource() {
        return futureShippingAddressSource;
    }

    public void setFutureShippingAddressSource(String futureShippingAddressSource) {
        this.futureShippingAddressSource = futureShippingAddressSource;
    }

    public String getShippingAddressSource() {
        return shippingAddressSource;
    }

    public void setShippingAddressSource(String shippingAddressSource) {
        this.shippingAddressSource = shippingAddressSource;
    }

    public String getCreationType() {
        return creationType;
    }

    public void setCreationType(String creationType) {
        this.creationType = creationType;
    }
}
