package com.macys.sdt.framework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a promotion
 */
public class Promotion {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    /**
     * Promo types supported by SIM data service
     */
    public enum PromoType {
        PWP             ("Promotions::Product with PWP"),
        SITE_WIDE_PWP   ("Promotions::Product with Site-wide PWP"),
        GWP             ("Promotions::Product with Bundled GWP"),
        PRICING         ("Promotions::Product with Promotional Pricing"),
        THRESHOLD_GWP   ("Promotions::Product with Threshold GWP"),
        SITE_WIDE_PROMO ("Product with Site-wide Promo (Omnichannel)"),
        DOLLAR_OFF      ("Product with Dollar Off Order"),
        PERCENT_OFF     ("Promotions::Product with Percentage Off Order");

        public String simUrl;

        PromoType(String simUrl) {
            this.simUrl = simUrl;
        }
    }

    public ArrayList<Integer> productIds = new ArrayList<>();

    @JsonProperty("PROMOTION_EFF_DATE")
    public Date startDate;

    @JsonProperty("PROMOTION_EXP_DATE")
    public Date endDate;

    @JsonProperty("LAST_MODIFIED")
    public Date lastModified;

    @JsonProperty("CREATED")
    public Date created;

    @JsonProperty("PROMOTION_SEQ_NBR")
    public int sequenceNumber;

    @JsonProperty("MAX_TIMES_TO_APPLY")
    public int maxUses;

    @JsonProperty("PROMOTION_ID")
    public int id;

    @JsonProperty("PROMOTION_NAME")
    public String name;

    @JsonProperty("OFFER_DESC")
    public String offerDescription;

    @JsonProperty("LEGAL_DISCLAIMER")
    public String legalDisclaimer;

    @JsonProperty("PROMOTION_SOURCE_CD")
    public String source;

    @JsonProperty("INFO_EXCLUSION")
    public String exclusions;

    @JsonProperty("PROMOTION_DESC")
    public String description;

    @JsonProperty("PROMOTION_SCOPE")
    public String scope;

    @JsonProperty("PROMO_TYPE")
    public String type;

    @JsonProperty("GLOBAL_PROMOTION")
    public boolean global;

    @JsonProperty("VALID_WITH_OTHER")
    public boolean validWithOther;

    @JsonSetter("PROD_ID")
    public void addProduct(int id) {
        productIds.add(id);
    }

    @JsonSetter("GLOBAL_PROMOTION")
    public void setGlobal(String s) {
        global = s.matches("y|Y");
    }

    @JsonSetter("VALID_WITH_OTHER")
    public void setValidWithOther(String s) {
        validWithOther = s.matches("y|Y");
    }

    @JsonSetter("PROMOTION_EFF_DATE")
    public void setStartDate(String dateString) {
        try {
            startDate = format.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Failed to set start date using string " + dateString + ": " + e);
        }
    }

    @JsonSetter("PROMOTION_EXP_DATE")
    public void setEndDate(String dateString) {
        try {
            endDate = format.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Failed to set expiration date using string " + dateString + ": " + e);
        }
    }

    @JsonSetter("LAST_MODIFIED")
    public void setLastModified(String dateString) {
        try {
            lastModified = format.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Failed to set last modified date using string " + dateString + ": " + e);
        }
    }

    @JsonSetter("CREATED")
    public void setCreated(String dateString) {
        try {
            created = format.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Failed to set start date using string " + dateString + ": " + e);
        }
    }


}
