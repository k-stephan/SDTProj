package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddToBagService {

    /**
     * Add TUX Item to bag using MSPOrder IP and get the response
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
        return RESTOperations.doPOST(getServiceURL() + "?userId=" + uid, MediaType.APPLICATION_JSON, payload.toString(), headers).readEntity(String.class);
    }

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

        String[] eventTypes = {"WEDDING", "SPECIAL"};
        String[] garmentTypes = {"COAT", "PANT", "VEST", "TIE", "SHOES", "SHIRT", "CUFFLINKS"};
        String[] garmentIds = {"8857HBBLO", "8857HBBLN", "8857HBBLM", "8857HBBLK", "8857HBBLS"};
        String[] itemDescriptions = {"Flat-front trousers feature", "Grosgrain stripe down", "Leg adding polish", "This classic two-button Side Vented tuxedo", "Cut from high-quality 100% wool", "Covered jacket buttons and satin-striped pants"};
        String[] descriptions = {"BLACK by Vera Wans", "Joseph Abboud Light", "Gray Satin Edged Notch Lapel", "Calvin Klein Lapel", "Black Satin Edged Notch"};
        String[] imageUrls = {"https://image1", "https://image2", "https://image3", "https://image4", "https://image5"};
        
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

        JSONObject upc2 = new JSONObject();
        upc2.put("number", (new Random().nextInt(999) + 111));
        upc2.put("price", (new Random().nextInt(99) + 11));

        JSONArray mkpUpcs = new JSONArray();
        mkpUpcs.put(upc1);
        mkpUpcs.put(upc2);

        JSONObject garment = new JSONObject();
        garment.put("description", itemDescriptions[new Random().nextInt(itemDescriptions.length)]);
        garment.put("garmentType", garmentTypes[new Random().nextInt(garmentTypes.length)]);
        garment.put("garmentId", garmentIds[new Random().nextInt(garmentIds.length)]);
        garment.put("imageUrl", imageUrls[new Random().nextInt(imageUrls.length)]);

        JSONArray mkpItems = new JSONArray();
        mkpItems.put(garment);

        JSONObject data = new JSONObject();
        data.put("mkpToken", Integer.toString(new Random().nextInt(9999) + 1111));
        data.put("description", descriptions[new Random().nextInt(descriptions.length)]);
        data.put("mkpReservationId", Long.toString((long) (Math.random() * 999999999999L + 111111111111L)));
        // TODO: Remove mkpReservationId from DB if already present
        data.put("eventType", eventTypes[new Random().nextInt(eventTypes.length)]);
        data.put("renterToFirstName", TestUsers.generateRandomFirstName());
        data.put("renterToLastName", TestUsers.generateRandomLastName());
        data.put("eventDate", eventDate);
        data.put("estimatedShipDate", estimatedShipDate);
        data.put("estimatedReturnDate", estimatedReturnDate);
        data.put("expectedDeliveryDate", expectedDeliveryDate);
        data.put("imageUrl", imageUrls[new Random().nextInt(imageUrls.length)]);
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

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("MSPOrder").ipAddress + ":8080/api/" + RESTEndPoints.ADD_TO_BAG;
    }
}