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

    private static JSONObject getAddTUXItemPayload() {
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
        warehouseAddress.put("city",address.getCity());
        warehouseAddress.put("countryCode", address.getCountryCode());
        warehouseAddress.put("postalCode",address.getZipCode());
        warehouseAddress.put("stateAbbrev",address.getState());
        warehouseAddress.put("validatedFlag",true);

        JSONObject upc1 = new JSONObject();
        upc1.put("number", 492018595416L);
        upc1.put("price", 10);

        JSONObject upc2 = new JSONObject();
        upc2.put("number", (new Random().nextInt(999) + 111));
        upc2.put("price", (new Random().nextInt(99) + 11));

        JSONArray mkpUpcs = new JSONArray();
        mkpUpcs.put(upc1);
        mkpUpcs.put(upc2);

        JSONObject description = new JSONObject();
        description.put("description", "BLACK by Vera Wang Gray Satin Edged Notch Lapel Tuxedo (1170)");
        description.put("garmentType","COAT");
        description.put("garmentId","1170");
        description.put("imageUrl","https://images.menswearhouse.com/is/image/TMW/1170_Vera_Coat_Gray_Hero");

        JSONArray mkpItems = new JSONArray();
        mkpItems.put(description);

        JSONObject data = new JSONObject();
        data.put("mkpToken","a4ec2af112d41f4a470ed");
        data.put("description","BLACK by Vera Wang Gray Satin Edged Notch Lapel Tuxedo");
        data.put("mkpReservationId", Long.toString((long) (Math.random() * 999999999999L + 111111111111L)));
        // TODO: Remove mkpReservationId from DB if already present
        data.put("eventType","WEDDING");
        data.put("renterToFirstName", TestUsers.generateRandomFirstName());
        data.put("renterToLastName", TestUsers.generateRandomLastName());
        data.put("eventDate",eventDate);
        data.put("estimatedShipDate",estimatedShipDate);
        data.put("estimatedReturnDate",estimatedReturnDate);
        data.put("expectedDeliveryDate",expectedDeliveryDate);
        data.put("imageUrl","https://images.menswearhouse.com/is/image/TMW/1170_Vera_Coat_Gray_Hero");
        data.put("pdpUrl","https://tuxedo.qa20codemacys.fds.com:38080/create-your-look/reservation/90599009#https://tuxedo.qa20codemacys.fds.com:38080/create-your-look/reservation/90599009");
        data.put("warehouseAddress",warehouseAddress);
        data.put("mkpUpcs",mkpUpcs);
        data.put("mkpItems",mkpItems);

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
