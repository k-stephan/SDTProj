package com.macys.sdt.framework.model;

import com.macys.sdt.framework.model.addresses.ProfileAddress;
import org.json.JSONObject;

/**
 * This class aggregates data that may be associated with a loyalty account
 */
public class LoyaltyAccount {
    public ProfileAddress address;
    public String loyaltyId;
    public String accountNum;
    public String creditCardNum;
    public String tier;
    public String pin;

    public enum LoyaltyType {
        PROP_CARD("Account associated with Prop card"),
        COBRANDED_CARD("Account associated with Cobranded Card"),
        INACTIVE("Account in Inactive Status");

        public final String simUrl;

        LoyaltyType(String simUrl) {
            this.simUrl = simUrl;
        }
    }

    /**
     * This constructor is for serializing data from SIM service
     *
     * @param data object with data from SIM service
     */
    public LoyaltyAccount(JSONObject data) {
        loyaltyId = data.getString("Loyalty ID");
        if (data.has("Account")) {
            accountNum = data.getString("Account");
        }
        Object check = data.get("CreditCards");
        creditCardNum = check instanceof String ? ((String) check).trim() : null;
        tier = data.getString("Tier");
        check = data.get("Pin");
        pin = check instanceof String ? (String) check : "1234";

        address = new ProfileAddress();
        // why is there a space after first name? I don't know. Ask the SIM team.
        address.setFirstName(data.getString("First Name "));
        address.setLastName(data.getString("Last Name"));
        address.setAddressLine1(data.getString("Address"));
        address.setCity(data.getString("City"));
        address.setState(data.getString("State"));
        address.setZipCode(data.getString("Zip"));
        address.setBestPhone(data.getString("Phone"));
        address.setEmail(data.getString("Email"));
        address.setCountryCode(data.getString("Country"));
    }

}
