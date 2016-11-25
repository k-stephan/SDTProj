package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProductService {

    public static String SERVICE_ENDPOINT = "catalog/v2/products/";

    /**
     * To find the product id is available in environment using FCC service
     *
     * @param productId
     * @return true if product is available
     */
    public static boolean availability(String productId){
        boolean availability = false;
        JSONObject jsonResponse = null;
        String serviceUrl = getServiceURL() + productId;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serviceUrl);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
             BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = reader.readLine()) != null)
                response.append(inputLine);
            jsonResponse = new JSONObject(response.toString()).getJSONObject("product");
            availability = jsonResponse.getBoolean("active") && jsonResponse.getBoolean("live") && !jsonResponse.getBoolean("archived");
        } catch (IOException | JSONException e) {
            System.err.println("Unable to get product information: " + e);
        }
        System.out.println("Product ("+productId+") is"+(availability ? "" : " not")+" available at FCC!!");
        return availability;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/" + SERVICE_ENDPOINT;
    }

}
