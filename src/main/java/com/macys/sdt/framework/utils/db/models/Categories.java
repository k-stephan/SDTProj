package com.macys.sdt.framework.utils.db.models;

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

public class Categories {
    private static final String SERVICE_ENDPOINT = "catalog/v2/categories/";
    private static final int MAX_ACTIVE_CATEGORY_LIMIT = 1000;
    private static boolean useParasoftHost = true;

    public static JSONObject category(String cat) {
        JSONObject jsonResponse;
        String serviceUrl = null;
        if (useParasoftHost) {
            serviceUrl = getServiceURL() + cat + "?_fields=id,name,parentCategoryId,externalHostUrl,attributes,leaf,canvasIds&_expand=id,live,countryEligible,subCategories(name,leaf).depth%3D2,parentCategory(live).depth%3D2147483647&sdpGrid=primary&ip=" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080";
        } else {
            serviceUrl = getServiceURL() + cat + "?_fields=id,name,parentCategoryId,externalHostUrl,attributes,leaf,canvasIds&_expand=id,live,countryEligible,subCategories(name,leaf).depth%3D2,parentCategory(live).depth%3D2147483647&sdpGrid=primary";
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serviceUrl);
        httpGet.addHeader("X-Macys-ClientId", "NavApp");
        httpGet.addHeader("X-Macys-Customer-Id", "1234");
        httpGet.addHeader("X-Macys-RequestId", "123456");
        try (
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpResponse.getEntity().getContent()))) {

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONObject("category");
        } catch (IOException | JSONException e) {
            System.err.println("Unable to retrieve data for category: " + cat);
        }
        return null;
    }

    public static boolean activeCategory(String cat) {
        JSONObject jsonResponse;
        String serviceUrl = null;
        if (useParasoftHost) {
            serviceUrl = getServiceURL() + cat + "?_fields=live&ip=" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080";
        } else {
            serviceUrl = getServiceURL() + cat + "?_fields=live";
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serviceUrl);
        httpGet.addHeader("X-Macys-ClientId", "NavApp");
        httpGet.addHeader("X-Macys-Customer-Id", "1234");
        httpGet.addHeader("X-Macys-RequestId", "123456");
        try (
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpResponse.getEntity().getContent()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            jsonResponse = new JSONObject(response.toString());
            reader.close();
            httpClient.close();
            return jsonResponse.getJSONObject("category").getBoolean("live");
        } catch (IOException | JSONException e) {
            System.err.println("Unable to find status of category: " + cat);
        }
        return false;
    }

    private static String getServiceURL() {
        return (useParasoftHost ? ("http://esu2v293:9080/api/" + SERVICE_ENDPOINT) : ("http://" + EnvironmentDetails.otherApp("FCC").ipAddress + ":8080/api/" + SERVICE_ENDPOINT));
    }
}
