package com.macys.sdt.framework.utils;


import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestObjectUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestObjectUtil.class);
    public static List<HashMap<String, String>> testObjectDevices = loadTestObjectDevices();
    public static boolean resetDeviceList = true;
    public static final String testObjectServiceUrl = "https://app.testobject.com/api/rest/v1/devices/";
    public static final String testObjectDeviceStatusEndpoint = "/status";

    /**
     * To fetch the random test object device id for given RunConfig
     *
     * @return available device Id
     */
    public static String getAvailableTestObjectDevice() {
        String osType = StepUtils.iOS() ? "IOS" : "Android";
        String osVersion = RunConfig.remoteOS;
        Predicate<HashMap<String, String>> filter = d -> d.get("available").equals("true"); // && checkDeviceAvailability(deviceId);
        Optional<HashMap<String, String>> device = testObjectDevices.stream().filter(filter).findFirst();

        // give one more chance
        if (!device.isPresent() && resetDeviceList) {
            logger.info("Resetting TestObject devices availability");
            testObjectDevices = loadTestObjectDevices();
            device = testObjectDevices.stream().filter(filter).findFirst();
            resetDeviceList = false;
        }

        Assert.assertTrue("No available matching devices found for the given OSType: " + osType + ", OSVersion: " + osVersion, device.isPresent());
        logger.info("Matching device " + device.get().get("device") + " is available for test");
        return device.get().get("device");
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
            logger.debug("JSON Response from the TestObject DeviceStatus API for the device: " + deviceId + " is: " + jsonArray);
            // There can be multiple device statuses for same device
            ArrayList<JSONObject> deviceStatusList = Utils.jsonArrayToList(jsonArray);
            if (deviceStatusList.size() > 0) {
                for (JSONObject device : deviceStatusList) {
                    deviceStatus = device.getString("status");
                    if (deviceStatus.equalsIgnoreCase("AVAILABLE")) {
                        found = true;
                        return found;
                    }
                }
            } else {
                logger.info("Not a valid JSON Response from TestObject DeviceStatus API for the device: "
                        + deviceId + ", try another device!!");
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
    private static List<HashMap<String, String>> loadTestObjectDevices() {
        JSONArray deviceJson = Utils.getFileDataInJson(Utils.getResourceFile("testobject_devices.json")).getJSONObject(StepUtils.iOS() ? "IOS" : "Android").getJSONArray(RunConfig.remoteOS);
        List<HashMap<String, String>> deviceList = new ArrayList<>();
        for (Object a : deviceJson) {
            HashMap<String, String> device = new HashMap<>();
            device.put("device", ((JSONObject)a).getString("device"));
            device.put("available", ((JSONObject)a).getString("available"));
            deviceList.add(device);
        }
        return deviceList;
    }

    public static void setAvailability(String device, String availability) {
        testObjectDevices = testObjectDevices.stream()
                .map(d -> {
                    if (d.get("device").equals(device))
                        d.put("available", availability);
                    return d;
                }).collect(Collectors.toList());
        logger.info("Setting device " + device + " availability to " + availability);
    }
}
