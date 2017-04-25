package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.exceptions.DataException;
import com.macys.sdt.framework.model.CreditCard;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.macys.sdt.framework.utils.StepUtils.macys;
import static com.macys.sdt.framework.utils.Utils.getResourceFile;
import static com.macys.sdt.framework.utils.Utils.readTextFile;

/**
 * Methods to operate with credit cards
 */
public class CreditCards {

    /**
     * Method to get a valid credit card and not a 3d secure eligible
     *
     * @param type credit card type
     * @return CreditCard of specified type
     **/
    public static CreditCard getValidCard(String type) throws DataException {
        return getValidCard(type, false);
    }

    /**
     * Method to get a valid credit card and with or without 3d secure eligible
     *
     * @param type credit card type
     * @param secure3d true for 3d secure eligible card, else false
     * @return CreditCard of specified type
     **/
    public static CreditCard getValidCard(String type, boolean secure3d) throws DataException {
        return getValidCards().stream().
                filter(card -> card.getCardType().name.equalsIgnoreCase(type)
                        && secure3d == card.has3DSecure()).findFirst().
                orElseThrow(() -> new DataException(String.format("No valid cards of type %s found", type)));
    }

    /**
     * gets cards from the file "valid_cards". the location of the file defined by internal call.
     * @return credit cards found
     */
    public static List<CreditCard> getValidCards() {
        return getCards("valid_cards.json");
    }

    /**
     *  /**
     * gets cards from the file the location of the file defined by internal call.
     * @param filename the name of the file to look for credit cards. for example "valid_cards.json"
     * @return credit cards found
     */
    public static List<CreditCard> getCards(String filename) {
        List<CreditCard> cards = new ArrayList<>();
        try {
            String json = readTextFile(getResourceFile(filename));
            JSONArray jsonCards = new JSONObject(json).getJSONArray(macys() ? "macys" : "bloomingdales");

            for (int i = 0; i < jsonCards.length(); i++) {
                JSONObject card = jsonCards.getJSONObject(i);
                cards.add(new CreditCard(
                        CreditCard.CardType.fromString(card.getString("card_type")),
                        card.getString("card_number"),
                        getNullableStringFromJsonCard(card, "security_code"),
                        card.has("balance") ? card.getDouble("balance") : 0.00,
                        getNullableStringFromJsonCard(card, "expiry_month"),
                        getNullableStringFromJsonCard(card, "expiry_month_index"),
                        getNullableStringFromJsonCard(card, "expiry_year"),
                        card.has("3d_secure") && card.getBoolean("3d_secure"),
                        getNullableStringFromJsonCard(card, "3d_secure_password")
                ));
            }
            return cards;
        } catch (Exception e) {
            throw new AssertionError("Can't extract cards from file: " + filename, e);
        }
    }

    /**
     * allows to return null instead of throwing of exception.
     * helpful for cards that can not to initialize all the fields, like expiryMonth and expiryYear.
     * Works only for Strings
     *
     * @param card json representation of the credit card
     * @param key json key to check
     * @return json String value for the key if present or null otherwise
     */
    private static String getNullableStringFromJsonCard(JSONObject card, String key) {
        return card.has(key) ? card.getString(key) : null;
    }
}
