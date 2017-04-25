package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.exceptions.ProductionException;
import com.macys.sdt.framework.exceptions.UserException;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Random;

/**
 * Retrieves credit card data from SIM service
 */
public class CreditCardService {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardService.class);

    /**
     * Gets a random credit card from the SIM Data Delivery service based on given attributes
     *
     * <p>As of 3/2/17, this service only supports AMEX and PROP cards. More should come soon.</p>
     *
     * @param type       Type of card to get
     * @param newAccount True for new account, false for converted account
     * @param singleLine True for single line, false for Dual line
     * @param activation True if used for activating an account, false if used for checkout
     * @return CreditCard object containing data returned by service
     */
    public static CreditCard getCreditCard(CreditCard.CardType type, boolean newAccount, boolean singleLine, boolean activation) {
        if (!type.abbreviation.matches("^AMEX$|^PROP$")) {
            logger.error("SIM credit card service only supports AMEX and PROP cards");
            return null;
        }

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
            logger.error("Unable to get credit card from SIM Service: " + e.getMessage());
            return null;
        }
    }

    /**
     * Method to add credit card to exisitng user wallet using Rest services
     *
     * @param card Credit card instance
     * @param defaultCard true to make card as default card, else false
     * @throws ProductionException while trying to add credit card in production environment
     * @throws RuntimeException while failed to add credit card using services
     **/
    public static void addCreditCardToWallet(CreditCard card, boolean defaultCard) throws Throwable {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        }
        HashMap<String, String> headers = new HashMap<>();
        User user = TestUsers.getCustomer(null).getUser();
        if (user.getTokenCredentials() == null) {
            throw new UserException("Add card to wallet service requires sign in user or user with valid user ID");
        }
        headers.put("X-Macys-SecurityToken", user.getTokenCredentials().getToken());
        headers.put("Content-Type", "application/xml");
        Response response = RESTOperations.doPOST(getAddCreditCardToWalletServiceURL(user.getId()), MediaType.APPLICATION_XML, addCreditCardToWalletPayload(card,defaultCard), headers);
        if(response.getStatus() != 200){
            throw new RuntimeException("Failed to add credit card to wallet");
        }
        logger.info("Credit card successfully added to user profile");
    }

    /**
     * Method to get add credit card request payload
     *
     * @param card Credit card instance
     * @param makeDefault true to make the card as default one, false to make the card as non-default credit card
     * @return request payload for add credit card
     **/
    private static String addCreditCardToWalletPayload(CreditCard card, boolean makeDefault) {
        UserProfile customer = TestUsers.getCustomer(null);
        ProfileAddress address = customer.getUser().getProfileAddress();
        HashMap<String, String> cardTypes = new HashMap<>();
        cardTypes.put("American Express", "A");
        cardTypes.put("Macy's American Express", "B");
        cardTypes.put("Bloomingdale's American Express", "B");
        cardTypes.put("Employee Card", "F");
        cardTypes.put("Bloomingdale's Employee Card", "F");
        cardTypes.put("MasterCard", "M");
        cardTypes.put("Discover", "O");
        cardTypes.put("Visa", "V");
        cardTypes.put("Macy's", "Y");
        cardTypes.put("Bloomingdale's", "U");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<tender>\n" +
                "<type>CREDITCARD</type>\n" +
                "<cardType>"+cardTypes.get(card.getCardType().name)+"</cardType>\n" +
                "<primary>"+makeDefault+"</primary>\n" +
                "<cardNumber>"+card.getCardNumber()+"</cardNumber>\n" +
                "<expMonth>"+card.getExpiryMonthIndex()+"</expMonth>\n" +
                "<expYear>"+card.getExpiryYear()+"</expYear>\n" +
                "<billingAddress>\n" + "<firstName>"+address.getFirstName()+"</firstName>\n" +
                "<lastName>"+address.getLastName()+"</lastName>\n" +
                "<line1>"+address.getAddressLine1()+"</line1>\n" +
                "<line2>"+address.getAddressLine2()+"</line2>\n" +
                "<city>"+address.getCity()+"</city>\n" +
                "<state>"+address.getState()+"</state>\n" +
                "<zipCode>"+address.getZipCode()+"</zipCode>\n" +
                "<countryCode>"+address.getCountryCode()+"</countryCode>\n" +
                "<email>"+address.getEmail()+"</email>\n" +
                "<bestPhone>"+address.getBestPhone()+"</bestPhone>\n" +
                "</billingAddress>\n" +
                "</tender>";
    }

    /**
     * Method to get add credit card to wallet service URL
     *
     * @param userid uid of the user
     * @return URL for add credit card to wallet service
     **/
    private static String getAddCreditCardToWalletServiceURL(String userid) {
        return "http://" + EnvironmentDetails.otherApp("MSPCUSTOMER").ipAddress + ":8080/api/customer/v2/customers/" + userid + "/wallet/tenders";
    }

}
