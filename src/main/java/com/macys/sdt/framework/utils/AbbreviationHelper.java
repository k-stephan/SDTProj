package com.macys.sdt.framework.utils;

import java.util.HashMap;
import java.util.Optional;

/**
 * This class is for translating to and from state abbreviations
 */
public class AbbreviationHelper {

    /**
     * HashMap containing translation between state/area abbreviation and name
     */
    private static final HashMap<String, String> STATE_MAP;

    static {
        STATE_MAP = new HashMap<>();
        STATE_MAP.put("AL", "Alabama");
        STATE_MAP.put("AK", "Alaska");
        STATE_MAP.put("AS", "American Samoa");
        STATE_MAP.put("AB", "Alberta");
        STATE_MAP.put("AZ", "Arizona");
        STATE_MAP.put("AR", "Arkansas");
        STATE_MAP.put("AA", "Armed Forces Americas");
        STATE_MAP.put("AE", "Armed Forces Europe");
        STATE_MAP.put("AP", "Armed Forces Pacific");
        STATE_MAP.put("BC", "British Columbia");
        STATE_MAP.put("CA", "California");
        STATE_MAP.put("CO", "Colorado");
        STATE_MAP.put("CT", "Connecticut");
        STATE_MAP.put("DE", "Delaware");
        STATE_MAP.put("DC", "District Of Columbia");
        STATE_MAP.put("FM", "Federated States of Micronesia");
        STATE_MAP.put("FL", "Florida");
        STATE_MAP.put("GA", "Georgia");
        STATE_MAP.put("GU", "Guam");
        STATE_MAP.put("HI", "Hawaii");
        STATE_MAP.put("ID", "Idaho");
        STATE_MAP.put("IL", "Illinois");
        STATE_MAP.put("IN", "Indiana");
        STATE_MAP.put("IA", "Iowa");
        STATE_MAP.put("KS", "Kansas");
        STATE_MAP.put("KY", "Kentucky");
        STATE_MAP.put("LA", "Louisiana");
        STATE_MAP.put("ME", "Maine");
        STATE_MAP.put("MH", "Marshall Islands");
        STATE_MAP.put("MB", "Manitoba");
        STATE_MAP.put("MD", "Maryland");
        STATE_MAP.put("MA", "Massachusetts");
        STATE_MAP.put("MI", "Michigan");
        STATE_MAP.put("MN", "Minnesota");
        STATE_MAP.put("MS", "Mississippi");
        STATE_MAP.put("MO", "Missouri");
        STATE_MAP.put("MT", "Montana");
        STATE_MAP.put("NE", "Nebraska");
        STATE_MAP.put("NV", "Nevada");
        STATE_MAP.put("NB", "New Brunswick");
        STATE_MAP.put("NH", "New Hampshire");
        STATE_MAP.put("NJ", "New Jersey");
        STATE_MAP.put("NM", "New Mexico");
        STATE_MAP.put("NY", "New York");
        STATE_MAP.put("NF", "Newfoundland");
        STATE_MAP.put("NC", "North Carolina");
        STATE_MAP.put("ND", "North Dakota");
        STATE_MAP.put("NT", "Northwest Territories");
        STATE_MAP.put("NS", "Nova Scotia");
        STATE_MAP.put("NU", "Nunavut");
        STATE_MAP.put("MP", "Northern Mariana");
        STATE_MAP.put("OH", "Ohio");
        STATE_MAP.put("OK", "Oklahoma");
        STATE_MAP.put("ON", "Ontario");
        STATE_MAP.put("OR", "Oregon");
        STATE_MAP.put("PA", "Pennsylvania");
        STATE_MAP.put("PE", "Prince Edward Island");
        STATE_MAP.put("PR", "Puerto Rico");
        STATE_MAP.put("QC", "Quebec");
        STATE_MAP.put("RI", "Rhode Island");
        STATE_MAP.put("SK", "Saskatchewan");
        STATE_MAP.put("SC", "South Carolina");
        STATE_MAP.put("SD", "South Dakota");
        STATE_MAP.put("TN", "Tennessee");
        STATE_MAP.put("TX", "Texas");
        STATE_MAP.put("UT", "Utah");
        STATE_MAP.put("VT", "Vermont");
        STATE_MAP.put("VI", "U.S. Virgin Islands");
        STATE_MAP.put("VA", "Virginia");
        STATE_MAP.put("WA", "Washington");
        STATE_MAP.put("WV", "West Virginia");
        STATE_MAP.put("WI", "Wisconsin");
        STATE_MAP.put("WY", "Wyoming");
        STATE_MAP.put("YT", "Yukon Territory");
    }

    /**
     * Translates a state or territory abbreviation into the full state or territory name
     *
     * @param abbreviation state abbreviation to translate
     * @return Full state name
     */
    public static String translateStateAbbreviation(String abbreviation) {
        return STATE_MAP.get(abbreviation.toUpperCase());
    }

    /**
     * Gets the 2-letter abbreviation for a state or territory
     *
     * @param state full name of the state/province
     * @return 2-letter name abbreviation
     */
    public static String getStateAbbreviation(String state) {
        // check if already state Abbreviation format
        if ((state.length() == 2) && STATE_MAP.containsKey(state.toUpperCase())) {
            return state.toUpperCase();
        }

        Optional<String> result = STATE_MAP.keySet().stream()
                .filter(abb -> STATE_MAP.get(abb).equalsIgnoreCase(state))
                .findFirst();
        return result.isPresent() ? result.get() : null;
    }

    /**
     * HashMap containing translation between 2-digit month abbreviation and full name
     */
    private static final HashMap<String, String> MONTH_MAP;

    static {
        MONTH_MAP = new HashMap<>();
        MONTH_MAP.put("01", "January");
        MONTH_MAP.put("02", "February");
        MONTH_MAP.put("03", "March");
        MONTH_MAP.put("04", "April");
        MONTH_MAP.put("05", "May");
        MONTH_MAP.put("06", "June");
        MONTH_MAP.put("07", "July");
        MONTH_MAP.put("08", "August");
        MONTH_MAP.put("09", "September");
        MONTH_MAP.put("10", "October");
        MONTH_MAP.put("11", "November");
        MONTH_MAP.put("12", "December");
    }

    /**
     * Translates the 2-digit month number into the full month name
     *
     * @param abbreviation 2-digit month number to translate
     * @return Full month name
     */
    public static String translateMonthAbbreviation(String abbreviation) {
        return MONTH_MAP.get(abbreviation.toUpperCase());
    }

    /**
     * Gets the 2-digit equivalent for a month
     *
     * @param month full name of the month
     * @return 2-digit state number
     */
    public static String getMonthAbbreviation(String month) {
        // check if already state Abbreviation format
        if ((month.length() == 2) && MONTH_MAP.containsKey(month.toUpperCase())) {
            return month.toUpperCase();
        }

        Optional<String> result = MONTH_MAP.keySet().stream()
                .filter(abb -> MONTH_MAP.get(abb).equalsIgnoreCase(month))
                .findFirst();
        return result.isPresent() ? result.get() : null;
    }


}
