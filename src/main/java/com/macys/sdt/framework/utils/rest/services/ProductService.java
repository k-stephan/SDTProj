package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProductService {



    /**
     * To find the product id is available for checkout in environment using FCC service
     *
     * @param productId ID of product to check
     * @return true if product is available
     */
    public static boolean checkoutAvailability(String productId){
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

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/" + RESTEndPoints.PRODUCTS_ENDPOINT;
    }

}
