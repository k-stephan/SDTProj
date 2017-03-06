package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.model.Promotion;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.utils.ObjectMapperProvidor;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.db.models.OrderServices;
import com.macys.sdt.framework.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class ProductService {


    public static Map<String, String> checkoutHeaders = new HashMap<>();

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
            for (int index = 0; index < jsonResponse.getJSONArray("upcs").length(); index++)
                upcIds.add(((JSONObject) jsonResponse.getJSONArray("upcs").get(index)).getBigInteger("id").toString());
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
        try {
            Element addToBagResponse = new OrderServices().getXmlElements(addUpcIdToBag(upcId));
            if (addToBagResponse.getElementsByTagName("message").item(0) != null) {
                return false;
            }
            Element initiateCheckoutResponse = new OrderServices().getXmlElements(initiateCheckout(addToBagResponse.getElementsByTagName("userId").item(0).getTextContent()));
            if (initiateCheckoutResponse.getElementsByTagName("message").item(0) != null) {
                return false;
            }
            Element processCheckoutResponse = new OrderServices().getXmlElements(processCheckout(initiateCheckoutResponse.getElementsByTagName("number").item(0).getTextContent(), initiateCheckoutResponse.getElementsByTagName("guid").item(0).getTextContent()));
            if (Boolean.parseBoolean(processCheckoutResponse.getElementsByTagName("orderHasError").item(0).getTextContent())) {
                boolean isItemUnavailable = processCheckoutResponse.getElementsByTagName("message").item(0).getTextContent().equals("CS_ITEM_UNAVAILABLE");
                return !isItemUnavailable;
            }
            return true; // since there's no errors in response
        } catch (Exception e) {
            // assume error means product not available
            System.err.println("Unable to get product availability from MST" + e);
        }
        return false;
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
     * @param guid        get guid from initiate response
     * @return String initiate checkout service response
     */
    public static String processCheckout(String orderNumber, String guid) {
        return RESTOperations.doPOST(getProcessCheckoutURL(), MediaType.APPLICATION_XML, getProcessCheckoutBody(orderNumber, guid), checkoutHeaders).readEntity(String.class);
    }

    private static String getAddToBagBody(String upcId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<item>\n" +
                "<quantity>1</quantity>\n" +
                "<upcId>" + upcId + "</upcId>\n" +
                "</item>";
    }

    private static String getInitiateCheckoutBody(String userId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<order>\n" +
                "<userId>" + userId + "</userId>\n" +
                "</order>\n";
    }

    private static String getProcessCheckoutBody(String orderNumber, String guid) {
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        ProfileAddress address = new ProfileAddress();
        TestUsers.getRandomValidAddress(opts, address);
        return "<order>\n" +
                "<number>" + orderNumber + "</number>\n" +
                "<guid>" + guid + "</guid>\n" +
                "<shipments>\n" +
                "<shipment>\n" +
                "<shippingContact>\n" +
                "<address>\n" +
                "<addressLine1>" + address.getAddressLine1() + "</addressLine1>\n" +
                "<city>" + address.getCity() + "</city>\n" +
                "<state>" + address.getState() + "</state>\n" +
                "<country>USA</country>\n" +
                "<postalCode>" + address.getZipCode() + "</postalCode>\n" +
                "</address>\n" +
                "<firstName>First</firstName>\n" +
                "<lastName>Last</lastName>\n" +
                "<dayPhone>4326365427</dayPhone>\n" +
                "<emailAddress>cbt@test.com</emailAddress>\n" +
                "</shippingContact>\n" +
                "<shippingMethod>G</shippingMethod>\n" +
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

    /**
     * Gets a product with a given promo type using the SIM data delivery framework
     *
     * @param type Type of promotion to get product for
     * @return a product with the given promo
     */
    public static Product getPromoProduct(Promotion.PromoType type) {
        String fullUrl = RESTEndPoints.getSimUrl(RESTEndPoints.SimBucket.PRODUCT, type.simUrl);
        try {
            Response response = RESTOperations.doGET(fullUrl, null);
            JSONArray promoProducts = new JSONArray(response.readEntity(String.class));
            String promoJSON = promoProducts.getJSONObject(new Random().nextInt(promoProducts.length())).toString();
            Promotion promotion = ObjectMapperProvidor.getMapper().readValue(promoJSON, Promotion.class);
            Product p = new Product(promotion.productIds.get(0));
            p.promo = promotion;
            return p;
        } catch (Exception e) {
            System.err.println("Unable to get or read data from SIM product service: " + e);
        }
        // fall back to DML product
        HashMap<String, Boolean> map = new HashMap<>();
        map.put("promo_code_eligible", true);
        map.put("orderable", true);
        return TestUsers.getRandomProduct(map);
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
