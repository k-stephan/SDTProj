package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.exceptions.DataException;
import com.macys.sdt.framework.exceptions.EnvException;
import com.macys.sdt.framework.exceptions.ProductionException;
import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.db.models.TuxService;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Collection of MSPOrder bags service related methods
 *
 */
public class AddToBagService {

    private static final Logger logger = LoggerFactory.getLogger(AddToBagService.class);
    private static final String[] eventTypes = {"WEDDING", "SPECIAL"};
    private static final String[] garmentTypes = {"COAT", "PANT", "VEST", "TIE", "SHOES", "SHIRT", "CUFFLINKS"};
    private static final String[] garmentIds = {"8857HBBLO", "8857HBBLN", "8857HBBLM", "8857HBBLK", "8857HBBLS"};
    private static final String[] itemDescriptions = {"Flat-front trousers feature", "Grosgrain stripe down",
            "Leg adding polish", "This classic two-button Side Vented tuxedo", "Cut from high-quality 100% wool",
            "Covered jacket buttons and satin-striped pants"};
    private static final String[] descriptions = {"BLACK by Vera Wans", "Joseph Abboud Light",
            "Gray Satin Edged Notch Lapel", "Calvin Klein Lapel", "Black Satin Edged Notch"};
    private static final String[] imageUrls = {"https://image1", "https://image2", "https://image3",
            "https://image4", "https://image5"};

    /**
     * Add TUX Item to bag using MSPOrder bags service and get the response
     *
     * @param uid user id
     * @param payload request payload in JSON format, to use default payload pass null
     * @return Add to bag service response as String
     */
    public static String addTUXItemToBag(String uid, JSONObject payload) {
        Map<String, String > headers = new HashMap<>();
        headers.put("X-Macys-ClientId", "menswearhouse");
        if (payload == null) {
            payload = getAddTUXItemPayload();
        }
        String mkpReservationId = payload.optJSONObject("item").getJSONArray("mkpReservations").getJSONObject(0).getString("mkpReservationId");
        if (TuxService.isMkpReservationExists(mkpReservationId)) {
            TuxService.deleteMkpReservationRecord(mkpReservationId);
        }
        return RESTOperations.doPOST(getServiceURL() + "?userId=" + uid, MediaType.APPLICATION_JSON, payload.toString(), headers).readEntity(String.class);
    }

    /**
     * Generates random payload for adding TUX item to bag
     *
     * @return Payload for adding TUX item to bag
     */
    public static JSONObject getAddTUXItemPayload() {
        ProfileAddress address = ProfileAddress.getDefaultProfileAddress();

        // For Random Address
        //HashMap<String, String> opts = new HashMap<>();
        //opts.put("checkout_eligible", "true");
        //ProfileAddress address = TestUsers.getRandomValidAddress(opts);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 3);
        String eventDate = new SimpleDateFormat("MM/dd/yy").format(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, 1);
        String estimatedReturnDate = new SimpleDateFormat("MM/dd/yy").format(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, -5);
        String expectedDeliveryDate = new SimpleDateFormat("MM/dd/yy").format(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, -3);
        String estimatedShipDate = new SimpleDateFormat("MM/dd/yy").format(c.getTime());
        
        JSONObject warehouseAddress = new JSONObject();
        warehouseAddress.put("addressLine1", address.getAddressLine1());
        warehouseAddress.put("city", address.getCity());
        warehouseAddress.put("countryCode", address.getCountryCode());
        warehouseAddress.put("postalCode", address.getZipCode());
        warehouseAddress.put("stateAbbrev", address.getState());
        warehouseAddress.put("validatedFlag", true);

        JSONObject upc1 = new JSONObject();
        upc1.put("number", 492018595416L);
        upc1.put("price", 10);
        Random rand = new Random();

        JSONObject upc2 = new JSONObject();
        upc2.put("number", 492018547910L);
        upc2.put("price", 139.99);

        JSONArray mkpUpcs = new JSONArray();
        mkpUpcs.put(upc1);
        mkpUpcs.put(upc2);

        JSONObject garment = new JSONObject();
        garment.put("description", itemDescriptions[rand.nextInt(itemDescriptions.length)]);
        garment.put("garmentType", garmentTypes[rand.nextInt(garmentTypes.length)]);
        garment.put("garmentId", garmentIds[rand.nextInt(garmentIds.length)]);
        garment.put("imageUrl", imageUrls[rand.nextInt(imageUrls.length)]);

        JSONArray mkpItems = new JSONArray();
        mkpItems.put(garment);

        JSONObject data = new JSONObject();
        data.put("mkpToken", Integer.toString(rand.nextInt(9999) + 1111));
        data.put("description", descriptions[rand.nextInt(descriptions.length)]);
        data.put("mkpReservationId", Long.toString((long) (Math.random() * 999999999999L + 111111111111L)));
        data.put("eventType", eventTypes[rand.nextInt(eventTypes.length)]);
        data.put("renterToFirstName", TestUsers.generateRandomFirstName());
        data.put("renterToLastName", TestUsers.generateRandomLastName());
        data.put("eventDate", eventDate);
        data.put("estimatedShipDate", estimatedShipDate);
        data.put("estimatedReturnDate", estimatedReturnDate);
        data.put("expectedDeliveryDate", expectedDeliveryDate);
        data.put("imageUrl", imageUrls[rand.nextInt(imageUrls.length)]);
        data.put("pdpUrl", "https://something.com/someurl#https://something.com/someurl");
        data.put("warehouseAddress", warehouseAddress);
        data.put("mkpUpcs", mkpUpcs);
        data.put("mkpItems", mkpItems);

        JSONArray mkpReservations = new JSONArray();
        mkpReservations.put(data);

        JSONObject item = new JSONObject();
        item.put("mkpReservations", mkpReservations);

        JSONObject object = new JSONObject();
        object.put("item", item);
        return object;
    }

    /**
     * Add a product to shopping bag using the rest service url
     *
     * @param product - product to add to shopping bag
     * @param userId  - userId to add product to the user
     * @throws ProductionException if called while executing against production
     */
    public static void addToBag(Product product, String userId) throws ProductionException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        }
        String serviceURL = getServiceURL() + "?userId=" + userId;
        if (product.bopsAvailable) {
            serviceURL += "&storeLocNumber=" + product.storeLocationNum;
        }
        Response response = RESTOperations.doPOST(serviceURL, MediaType.APPLICATION_JSON, getPayload(product).toString(), null);
        if (response.getStatus() != 200) {
            throw new EnvException("Failed to add product to bag. Got response code:" + response.getStatus());
        }
        if (product.bopsAvailable) {
            JSONObject jsonObject = new JSONObject(response.readEntity(String.class));
            JSONArray items = jsonObject.getJSONObject("bag").getJSONArray("items");
            items.forEach((item) -> {
                JSONObject itemDetails = (JSONObject) item;
                if (itemDetails.getInt("productId") == product.id && !itemDetails.getBoolean("pickUpFromStore")) {
                    throw new DataException("product id: " + product.id + " is not eligible for pick up.");
                }
            });
        }
        logger.info("Product is added to bag: " + product.id);
    }

    /**
     * Create a payload for the rest service using the product and return payload as jsonObject
     *
     * @param product -  to create payload by getting upcId from this product
     * @return - item payload in jsonObject
     */
    private static JSONObject getPayload(Product product) {
        String upcId = ProductService.getAllUpcIds(Integer.toString(product.id)).get(0);
        JSONObject item = new JSONObject();
        item.put("quantity", "1");
        item.put("upcId", upcId);
        if (product.bopsAvailable) {
            item.put("pickUpFromStore", "true");
        }
        if (product.registryItem) {
            UserProfile customer = TestUsers.getCustomer(null);
            JSONObject regJson = new JSONObject();
            regJson.put("id", customer.getRegistry().getId());
            item.put("registry", regJson);
        }
        if (product.electronicGiftCard) {
            item.put("syndicationId", "3545465");
            item.put("syndicationSource", "ASI");
            item.put("syndicationItemId", "6565");
            item.put("syndicationTransactionId", "565");
            item.put("syndicationItemDesc", "5656");
            item.put("giftCardAmount", "10");
        }
        if (product.virtualGiftCard) {
            item.put("giftCardAmount", "10");
            item.put("giftCardEmailId", "testuser@gmail.com");
            item.put("giftCardFrom", "test user");
            item.put("giftCardMessage", "test message");
            item.put("giftCardSubject", "test subject");
        }
        JSONObject object = new JSONObject();
        object.put("item", item);
        return object;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("MSPOrder").ipAddress + ":8080/api/" + RESTEndPoints.ADD_TO_BAG;
    }
}