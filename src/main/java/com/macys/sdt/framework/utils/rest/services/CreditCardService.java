package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * Retrieves credit card data from SIM service
 */
public class CreditCardService {

    /**
     * Gets a random credit card from the SIM Data Delivery service based on given attributes
     *
     * @param type       Type of card to get
     * @param newAccount True for new account, false for converted account
     * @param singleLine True for single line, false for Dual line
     * @param activation True if used for activating an account, false if used for checkout
     * @return CreditCard object containing data returned by service
     */
    public static CreditCard getCreditCard(CreditCard.CardType type, boolean newAccount, boolean singleLine, boolean activation) {
        boolean employeeCard = type == CreditCard.CardType.EMPLOYEE_CARD || type == CreditCard.CardType.BLOOMINGDALES_EMPLOYEE_CARD;
        String cardType = employeeCard ? "Employee" : "Regular";
        String accountType = newAccount ? "New Account " : "Converted Account ";
        String useType = activation ? "Activation" : "Standard Checkout";
        String numLines = singleLine ? "Single Line" : "Dual Line";
        numLines = newAccount ? numLines : "(" + numLines + ")";
        String url = cardType + "::" + type.abbreviation + "::" + accountType + numLines + "::" + useType;

        try {
            url = RESTEndPoints.getSimUrl(RESTEndPoints.SimBucket.CREDIT_CARD, url);
            Response response = RESTOperations.doGET(url, null);
            String jsonText = response.readEntity(String.class);
            JSONArray cards = new JSONArray(jsonText);
            JSONObject card = cards.getJSONObject(new Random().nextInt(cards.length()));
            return CreditCard.createCardFromSimService(card, type);
        } catch (Exception e) {
            System.err.println("Unable to get credit card from SIM Service: " + e);
            return null;
        }
    }
}
