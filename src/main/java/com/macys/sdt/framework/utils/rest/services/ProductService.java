package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import com.macys.sdt.shared.utils.db.models.OrderServices;
import org.w3c.dom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * To find all upc ids for product id
     *
     * @param productId ID of product to check
     * @return List all upc ids
     */
    public static List<String> getAllUpcIds(String productId) {
        List<String> upcIds = new ArrayList<>();
        Response response = RESTOperations.doGET(getServiceURL() + productId, null);
        try {
            JSONObject jsonResponse = new JSONObject(response.readEntity(String.class)).getJSONObject("product");
            for(int index=0; index < jsonResponse.getJSONArray("upcs").length(); index ++)
                upcIds.add(((JSONObject)jsonResponse.getJSONArray("upcs").get(index)).getJSONObject("upc").getString("id"));
        } catch (JSONException e) {
            System.err.println("Unable to get product information from FCC: " + e);
        }
        return upcIds;
    }

    /**
     * To find the upc id is available for checkout at MST
     *
     * @param upcId ID of product to check
     * @return true if upc is available
     */
    public static boolean checkProductAvailabilityAtMST(String upcId) {
        checkoutHeaders.put("Accept", "application/json");
        checkoutHeaders.put("Content-Type", "application/xml");
        boolean isItemUnavailable = false;
        Element addToBagResponse = new OrderServices().getXmlElements(addUpcIdToBag(upcId));
        if (addToBagResponse.getElementsByTagName("message").item(0) != null)
            return isItemUnavailable;
        Element initiateCheckoutResponse = new OrderServices().getXmlElements(initiateCheckout(addToBagResponse.getElementsByTagName("userId").item(0).getTextContent()));
        if (initiateCheckoutResponse.getElementsByTagName("message").item(0) != null)
            return isItemUnavailable;
        Element processCheckoutResponse = new OrderServices().getXmlElements(processCheckout(initiateCheckoutResponse.getElementsByTagName("number").item(0).getTextContent(), initiateCheckoutResponse.getElementsByTagName("guid").item(0).getTextContent()));
        if (Boolean.parseBoolean(processCheckoutResponse.getElementsByTagName("orderHasError").item(0).getTextContent()))
            isItemUnavailable = processCheckoutResponse.getElementsByTagName("message").item(0).getTextContent().equals("CS_ITEM_UNAVAILABLE");
        return isItemUnavailable;
    }

    /**
     * Add item (UPC ID) to bag using MSPOrder IP and get the response
     *
     * @param upcId ID of product to check
     * @return String Add to bag service response
     */
    public static String addUpcIdToBag(String upcId) {
        return RESTOperations.doPOST(getAddToBagURL(), MediaType.APPLICATION_XML, getAddToBagBody(upcId), checkoutHeaders).readEntity(String.class);
    }

    /**
     * Initiate checkout with user id and get the repsonse
     *
     * @param userId ID who has added an item earlier.
     * @return String initiate checkout service response
     */
    public static String initiateCheckout(String userId) {
        return RESTOperations.doPOST(getInitiateCheckoutURL(), MediaType.APPLICATION_XML, getInitiateCheckoutBody(userId), checkoutHeaders).readEntity(String.class);
    }

    /**
     * process checkout with user id and get the repsonse
     *
     * @param orderNumber get order number from initiate response
     * @param guid get guid from initiate response
     * @return String initiate checkout service response
     */
    public static String processCheckout(String orderNumber, String guid) {
        return RESTOperations.doPOST(getProcessCheckoutURL(), MediaType.APPLICATION_XML, getProcessCheckoutBody(orderNumber, guid), checkoutHeaders).readEntity(String.class);
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
