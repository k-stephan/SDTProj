package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Categories {

    private static final Logger logger = LoggerFactory.getLogger(Categories.class);

    private static final String SERVICE_ENDPOINT = "catalog/v2/categories/";
    private static final int MAX_ACTIVE_CATEGORY_LIMIT = 1000;
    private static boolean useParasoftHost = true;

    public static JSONObject category(String cat) {
        JSONObject jsonResponse = null;
        String serviceUrl = "";
        if (useParasoftHost) {
            serviceUrl = getServiceURL() + cat + "?_fields=id,name,parentCategoryId,externalHostUrl,attributes,leaf,canvasIds&_expand=id,live,countryEligible,subCategories(name,leaf).depth%3D2,parentCategory(live).depth%3D2147483647&sdpGrid=primary&ip=" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080";
        } else {
            serviceUrl = getServiceURL() + cat + "?_fields=id,name,parentCategoryId,externalHostUrl,attributes,leaf,canvasIds&_expand=id,live,countryEligible,subCategories(name,leaf).depth%3D2,parentCategory(live).depth%3D2147483647&sdpGrid=primary";
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Macys-ClientId", "NavApp");
        headers.put("X-Macys-Customer-Id", "1234");
        headers.put("X-Macys-RequestId", "123456");
        if (RunConfig.debugMode) {
            logger.info("--> Service Request URL: -> " + serviceUrl);
            logger.info("--> Headers: -> " + headers.toString());
        }
        try {
            jsonResponse = new JSONObject(RESTOperations.doGET(serviceUrl, headers).readEntity(String.class)).getJSONObject("category");
        } catch (JSONException e) {
            logger.warn("Unable to get information for cat id : " + cat);
        }
        return jsonResponse;
    }

    public static JSONObject getProductsByCategory(String categoryId, HashMap<String, String> otherParams) {
        HashMap<String, String> params = new HashMap<>();
        params.put("productsPerPage", "40");
        params.put("pageNumber", "1");
        params.put("countryCode", "US");
        params.put("deviceType", "DESKTOP");
        if (otherParams != null) {
            params.putAll(otherParams);
        }
        String serviceUrl = "http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/catalog/v2/categories/" + categoryId + "/products?_offset=" + params.get("pageNumber") + "&_limit=" + params.get("productsPerPage") + "&_sortby=ORIGINAL&sdpGrid=primary&returnNavigationProductPool=true&_deviceType=" + params.get("deviceType") + "&_shoppingMode=SITE&_regionCode=" + params.get("countryCode") + "&_application=SITE&_navigationType=BROWSE&matchAll=false&_customerExperiment=NO_EXPERIMENT";
        logger.debug("Service Request URL: " + serviceUrl);
        return new JSONObject(RESTOperations.doGET(serviceUrl, null).readEntity(String.class));
    }

    public static boolean activeCategory(String cat) {
        JSONObject jsonResponse;
        String serviceUrl = null;
        boolean status = false;
        if (useParasoftHost) {
            serviceUrl = getServiceURL() + cat + "?_fields=live&ip=" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080";
        } else {
            serviceUrl = getServiceURL() + cat + "?_fields=live";
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Macys-ClientId", "NavApp");
        headers.put("X-Macys-Customer-Id", "1234");
        headers.put("X-Macys-RequestId", "123456");
        if (RunConfig.debugMode) {
            logger.info("--> Service Request URL: -> " + serviceUrl);
            logger.info("--> Headers: -> " + headers.toString());
        }
        try {
            jsonResponse = new JSONObject(RESTOperations.doGET(serviceUrl, headers).readEntity(String.class));
            status = jsonResponse.getJSONObject("category").getBoolean("live");
        } catch (JSONException e) {
            logger.warn("Unable to find the status for cat id: " + cat);
        }
        return status;
    }

    private static String getServiceURL() {
        return (useParasoftHost ? ("http://esu2v293:9080/api/" + SERVICE_ENDPOINT) : ("http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/" + SERVICE_ENDPOINT));
    }
}
