package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.utils.EnvironmentDetails;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Stores {

    private static final String PATH = "/api/store/v2/stores/";
    private static final int PORT = 8080;

    public static String zipCodeByStoreNumber(int storeNumber) {
        GetMethod get = new GetMethod(getServiceUrl() + storeNumber);
        get.addRequestHeader("X-Macys-ClientId", "ShopApp");
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(get);
            if (result != 200) {
                throw new RuntimeException("error during request execution of getting zipcode by store number");
            }
            String response = get.getResponseBodyAsString();
            get.releaseConnection();
            JSONObject json = new JSONObject(response);
            return json.getJSONObject("stores").getJSONArray("store").getJSONObject(0)
                    .getJSONObject("address").getString("zipCode");
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getServiceUrl() {
        return "http://" + EnvironmentDetails.otherApp("FCC").hostName + ":" + PORT + PATH;
    }
}
