package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.apache.logging.log4j.core.util.Assert;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class ProductService {


    public static Map<String, String > checkoutHeaders = new HashMap<>();

    /**
     * To find the product id is available for checkout in environment using FCC service
     *
     * @param productId ID of product to check
     * @return true if product is available
     */
    public static boolean checkoutAvailability(String productId) {
        Response response = RESTOperations.doGET(getServiceURL() + productId, null);
        try {
            JSONObject jsonResponse = new JSONObject(response.readEntity(String.class)).getJSONObject("product");
            // We need to check these three parameter values to confirm the product is available for checkout or not
            return jsonResponse.getBoolean("active") && jsonResponse.getBoolean("live") && !jsonResponse.getBoolean("archived");
        } catch (JSONException e) {
            System.err.println("Unable to get product information from FCC: " + e);
        }
        return false;
    }

    /**
     * To find the upc id is available for checkout at MST
     *
     * @param upcId ID of product to check
     * @return true if upc is available
     */
    public static boolean checkProductAvailabilityAtMST(String upcId) {
        checkoutHeaders.put("Accept", "application/xml");
        checkoutHeaders.put("Content-Type", "application/xml");
        JSONObject addToBagResponse = new JSONObject(addUpcIdToBag(upcId));
        Assert.requireNonNull(addToBagResponse, "Add Item to bag response is NULL!!");
        JSONObject initiateCheckoutResponse = new JSONObject(initiateCheckout(addToBagResponse.getJSONObject("bag").getJSONObject("owner").getString("userId")));
        Assert.requireNonNull(initiateCheckoutResponse, "initiate checkout response is NULL!!");
        JSONObject processCheckoutResponse = new JSONObject(processCheckout(initiateCheckoutResponse.getJSONObject("order").getString("number"), initiateCheckoutResponse.getJSONObject("order").getString("guid")));
        Assert.requireNonNull(processCheckoutResponse, "process checkout response  is NULL!!");
        boolean isItemUnavailable = false;
        if (processCheckoutResponse.getJSONObject("order").getBoolean("orderHasError")){
            JSONArray shipments = processCheckoutResponse.getJSONObject("order").getJSONArray("shipments");
            JSONArray errors = ((JSONObject)(((JSONObject)shipments.get(0)).getJSONObject("shipment")).getJSONArray("items").get(0)).getJSONObject("item").getJSONArray("errors");
            for (int index = 0; index < errors.length(); index++) {
                isItemUnavailable = ((JSONObject)errors.get(index)).getJSONObject("error").getString("message").equals("CS_ITEM_UNAVAILABLE");
            }
            return isItemUnavailable;
        } else {
            return false;
        }
    }

    /**
     * Add item (UPC ID) to bag using MSPOrder IP and get the response
     *
     * @param upcId ID of product to check
     * @return String Add to bag service response
     */
    public static String addUpcIdToBag(String upcId) {
        Response response = RESTOperations.doPOST(getAddToBagURL(), MediaType.APPLICATION_XML, getAddToBagBody(upcId), checkoutHeaders);
        return response.readEntity(String.class);
    }

    /**
     * Initiate checkout with user id and get the repsonse
     *
     * @param userId ID who has added an item earlier.
     * @return String initiate checkout service response
     */
    public static String initiateCheckout(String userId) {
        Response response = RESTOperations.doPOST(getInitiateCheckoutURL(), MediaType.APPLICATION_XML, getInitiateCheckoutBody(userId), checkoutHeaders);
        return response.readEntity(String.class);
    }

    /**
     * process checkout with user id and get the repsonse
     *
     * @param orderNumber get order number from initiate response
     * @param guid get guid from initiate response
     * @return String initiate checkout service response
     */
    public static String processCheckout(String orderNumber, String guid) {
        Response response = RESTOperations.doPOST(getProcessCheckoutURL(), MediaType.APPLICATION_XML, getProcessCheckoutBody(orderNumber, guid), checkoutHeaders);
        return response.readEntity(String.class);
    }

    private static String getAddToBagBody(String upcId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<item>\n" +
                "<quantity>1</quantity>\n" +
                "<upcId>"+upcId+"</upcId>\n" +
                "</item>";
    }

    private static String getInitiateCheckoutBody(String userId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<order>\n" +
                "<userId>" + userId + "</userId>\n" +
                "</order>\n";
    }
    private static String getProcessCheckoutBody(String orderNumber, String guid) {
        return "<order>\n" +
                "<number>" + orderNumber + "</number>\n" +
                "<guid>" + guid +"</guid>\n" +
                "<shipments>\n" +
                "<shipment>\n" +
                "<shippingContact>\n" +
                "<address>\n" +
                "<addressLine1>500 S.Karaemer Boulevard</addressLine1>\n" +
                "<city>Brea</city>\n" +
                "<state>CA</state>\n" +
                "<country>USA</country>\n" +
                "<postalCode>88898</postalCode>\n" +
                "</address>\n" +
                "<firstName>First</firstName>\n" +
                "<lastName>Last</lastName>\n" +
                "<dayPhone>4326365427</dayPhone>\n" +
                "<emailAddress>cbt@test.com</emailAddress>\n" +
                "</shippingContact>\n" +
                "<shippingMethod>O</shippingMethod>\n" +
                "<giftOptions>\n" +
                "<giftMessage1>test1</giftMessage1>\n" +
                "<giftMessage2>test2</giftMessage2>\n" +
                "<giftMessageSupported>true</giftMessageSupported>\n" +
                "<giftOptionID>100</giftOptionID>\n" +
                "<giftWrappable>true</giftWrappable>\n" +
                "<includeGiftReceipt>true</includeGiftReceipt>\n" +
                "<senderName>muni</senderName>\n" +
                "</giftOptions>\n" +
                "</shipment>\n" +
                "</shipments>\n" +
                "</order>\n";
    }

    private static String getAddToBagURL() {
        return "http://" + EnvironmentDetails.otherApp("MSPOrder").ipAddress + ":8080/api/" + RESTEndPoints.ADD_TO_BAG;
    }

    private static String getInitiateCheckoutURL() {
        return "http://" + EnvironmentDetails.otherApp("MSPOrder").ipAddress + ":8080/api/" + RESTEndPoints.INITIATE_CHECKOUT;
    }

    private static String getProcessCheckoutURL() {
        return "http://" + EnvironmentDetails.otherApp("MSPOrder").ipAddress + ":8080/api/" + RESTEndPoints.PROCESS_CHECKOUT;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/" + RESTEndPoints.PRODUCTS_ENDPOINT;
    }

}
