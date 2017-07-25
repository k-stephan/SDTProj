package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.model.Promotion;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.utils.ObjectMapperProvider;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.db.models.OrderServices;
import com.macys.sdt.framework.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public static Map<String, String> checkoutHeaders = new HashMap<>();

    /**
     * To find the product id is available for checkout in environment using FCC service
     *
     * @param productId ID of product to check
     * @return true if product is available
     */
    public static boolean checkoutAvailability(String productId) {
        try {
            JSONObject jsonResponse = getProductDetails(productId);
            if (jsonResponse == null) {
                return false;
            }
            // We need to check these three parameter values to confirm the product is available for checkout or not
            return jsonResponse.getBoolean("active") && jsonResponse.getBoolean("live") && !jsonResponse.getBoolean("archived");
        } catch (JSONException e) {
            logger.error("Unable to get product information from FCC: " + e.getMessage());
        }
        return false;
    }

    /**
     * To fetch complete product details for a given product id using FCC service
     *
     * @param productId ID of product to fetch
     * @return product details
     */
    public static JSONObject getProductDetails(String productId) {
        Response response = RESTOperations.doGET(getServiceURL() + productId, null);
        try {
            return new JSONObject(response.readEntity(String.class)).getJSONObject("product");
        } catch (JSONException e) {
            logger.error("Unable to get product information from FCC: " + e.getMessage());
        }
        return null;
    }

    /**
     * To fetch the price details for a given Product Id
     *
     * @param productId ID of product to fetch
     * @return product prices
     */
    public static Map<String, Object> getProductPrices(String productId) {
        try {
            JSONObject details = getProductDetails(productId);
            if (details == null) {
                return null;
            }
            return details.getJSONObject("price").toMap();
        } catch (JSONException | NullPointerException e) {
            logger.error("Unable to get product information from FCC: " + e.getMessage());
        }
        return null;
    }

    /**
     * To find all upc ids for product id
     *
     * @param productId ID of product to check
     * @return List all upc ids
     */
    public static List<String> getAllUpcIds(String productId) {
        List<String> upcIds = new ArrayList<>();
        try {
            JSONObject details = getProductDetails(productId);
            if (details == null) {
                return null;
            }
            JSONArray upcsArray = details.getJSONArray("upcs");
            for (int index = 0; index < upcsArray.length(); index++)
                upcIds.add(((JSONObject) upcsArray.get(index)).getBigInteger("id").toString());
        } catch (JSONException e) {
            logger.error("Unable to get product information from FCC: " + e.getMessage());
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
            logger.warn("Unable to get product availability from MST due to : " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all upc ids and BT status for given product
     *
     * @param prodId ID of product
     * @param zipCode zip code for delivery
     * @return List with all upc and status
     */
    public static List<HashMap> getBTProductDeliverabilityStatus(String prodId, String zipCode) {
        List<HashMap> upcList = new ArrayList<>();
        try {
            String url = getServiceURL() + prodId + "?_fields=upcs(deliverability)&zipcode=" + zipCode;
            JSONArray jsonArray = new JSONObject(RESTOperations.doGET(url, null).readEntity(String.class)).getJSONObject("product").getJSONArray("upcs");
            for (int index = 0; index < jsonArray.length(); index++) {
                HashMap<String, String> upcInfo = new HashMap<>();
                upcInfo.put("updId", jsonArray.getJSONObject(index).get("id").toString());
                upcInfo.put("status", jsonArray.getJSONObject(index).getJSONObject("deliverability").getString("status"));
                upcList.add(upcInfo);
            }
        }
        catch (Exception e) {
            logger.warn("Unable to get all upc ids and BT status due to : " + e.getMessage());
        }
        return upcList;
    }

    /**
     * check item is BOPS available/not available
     *
     * @param productId ID of product
     * @param storeId ID of store to check availability for
     * @return is product  be available
     */
    public static boolean checkProductBopsAvailability(String productId, String storeId) {
        String url = String.format("%s/%s?productId=%s&_fields=name,inventories", getStoresURL(), storeId, productId);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Macys-ClientId", "NavApp");
        String response = RESTOperations.doGET(url, headers).readEntity(String.class);
        try {
            JSONArray stores = new JSONObject(response)
                    .getJSONObject("stores")
                    .getJSONArray("store");
            JSONArray inventories = stores
                    .getJSONObject(0)
                    .getJSONObject("inventories")
                    .getJSONArray("inventory");

            for (int i = 0; i < inventories.length(); i++) {
                String bopsAvailability = inventories.getJSONObject(i)
                        .getJSONObject("storeInventory")
                        .getString("bopsAvailability");
                if (bopsAvailability.equals("AVAILABLE")) {
                    return true;
                }
            }
        } catch (NullPointerException|JSONException e) {
            logger.warn(String.format("Unable to check bops availability for prodID %s, storeID %s.\\n Response %s.\\n Error %s",
                    productId, storeId, response, e.getMessage()));
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
            Promotion promotion = ObjectMapperProvider.getJsonMapper().readValue(promoJSON, Promotion.class);
            Product p = new Product(promotion.productIds.get(0));
            p.promo = promotion;
            return p;
        } catch (Exception e) {
            logger.error("Unable to get or read data from SIM product service: " + e.getMessage());
        }
        // fall back to DML product
        HashMap<String, Object> map = new HashMap<>();
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

    private static String getStoresURL() {
        return "http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/" + RESTEndPoints.STORES_ENDPOINT;
    }

}
