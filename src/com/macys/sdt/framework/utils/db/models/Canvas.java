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
import java.util.List;
import java.util.Map;

public class Canvas {

    private static final String CONTEXT_SERVICE_ENDPOINT = "content/v2/canvas/";

    public static JSONObject contextPoolMedia(String canvasId, String categoryId, List context, String application, List<String> canvasIdsInheritable) {
        if (application == null) {
            application = "SITE";
        }
        JSONObject jsonResponse = null;
        String requestedContextParams = canvasId + "?catId=" + categoryId + "&_deviceType=" + ((Map) context.get(0)).get("DEVICE_TYPE") + "&_shoppingMode=" + ((Map) context.get(0)).get("SHOPPING_MODE") + "&_regionCode=" + ((Map) context.get(0)).get("REGION_CODE") + "&_application=" + application + "&_navigationType=" + ((Map) context.get(0)).get("NAVIGATION_TYPE") + "&_expand=media";
        if (!canvasIdsInheritable.isEmpty()) {
            String can = null;
            for (int index = 0; index < canvasIdsInheritable.size(); index++)
                if (index < canvasIdsInheritable.size() - 1) {
                    can = can + canvasIdsInheritable.get(index) + ",";
                } else {
                    can = can + canvasIdsInheritable.get(index);
                }

            requestedContextParams = requestedContextParams + "&inheritableCanvasIds=" + can;
        }

        String serviceUrl = getServiceURL() + requestedContextParams;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serviceUrl);
        httpGet.addHeader("X-Macys-ClientId", "NavApp");
        httpGet.addHeader("X-Macys-Customer-Id", "1234");
        httpGet.addHeader("X-Macys-RequestId", "123456");
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(httpResponse.getEntity().getContent()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            jsonResponse = new JSONObject(response.toString());
        } catch (IOException | JSONException e) {
            System.err.println("Unable to get context pool media: " + e);
        }
        return jsonResponse;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("f5_vip").ipAddress + ":85/api/" + CONTEXT_SERVICE_ENDPOINT;
    }
}
