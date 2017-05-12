package com.macys.sdt.framework.utils.rest.utils;


import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Random;

public class TestObjectUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestObjectUtil.class);
    public static JSONObject testObjectDevices = loadTestObjectDevices();
    public static final String testObjectServiceUrl = "https://app.testobject.com/api/rest/v1/devices/";
    public static final String testObjectDeviceStatusEndpoint = "/status";

    /**
     * To fetch the random test object device id for a given os type and version
     *
     * @param osType    : required os type (IOS, Android)
     * @param osVersion : required os version(9.3, 10.0, 5.1, 6.0 etc.,)
     * @return random device Id
     */
    public static String getRandomTestObjectDevice(String osType, String osVersion) {
        boolean found = false;
        JSONArray deviceArray = testObjectDevices.getJSONObject(osType).getJSONArray(osVersion);
        String deviceId = "";
        if (deviceArray != null) {
            for (int i = 0; i < deviceArray.length(); i++) {
                deviceId = (String) deviceArray.get(new Random().nextInt(deviceArray.length()));
                if (checkDeviceAvailability(deviceId)) {
                    logger.info("Matching device " + deviceId + " is available for test");
                    return deviceId;
                }
            }
        } else {
            logger.info("No matching devices found for the given OSType: " + osType + ", OSVersion: " + osVersion);
        }
        return deviceId;
    }

    /**
     * Check whether TestObject device is Available to test or In Use.
     *
     * @param deviceId : required device id
     * @return Return device status
     */
    public static boolean checkDeviceAvailability(String deviceId) {
        boolean found = false;
        String serviceUrl = testObjectServiceUrl + deviceId + testObjectDeviceStatusEndpoint;
        Response response = RESTOperations.doGETWithBasicAuth(serviceUrl, null,
                RunConfig.testObjectUser, RunConfig.testObjectAPIKey);
        String deviceStatus = "";
        try {
            JSONArray jsonArray = new JSONArray(response.readEntity(String.class));
            logger.debug("JSONResponse for TestOjbect device status: "+jsonArray);
            ArrayList<JSONObject> list = Utils.jsonArrayToList(jsonArray);
            for (JSONObject js : list) {
                deviceStatus = js.getString("status");
            }

            if (deviceStatus.equalsIgnoreCase("AVAILABLE")) {
                found = true;
            } else {
                logger.info("Currently, requested device: " + deviceId
                        + " is not available to test. Try another device!!");
            }
        } catch (JSONException e) {
            logger.error("Unable to check the device status: " + e.getMessage());
        }
        return found;
    }

    /**
     * Load TestObject devices
     *
     * @return devices
     */
    private static JSONObject loadTestObjectDevices() {
        return Utils.getFileDataInJson(Utils.getResourceFile("testobject_devices.json"));
    }
}
