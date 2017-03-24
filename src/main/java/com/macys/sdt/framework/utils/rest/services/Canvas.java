package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Canvas {

    private static final String CONTEXT_SERVICE_ENDPOINT = "content/v2/canvas/";
    private static final Logger LOGGER = LoggerFactory.getLogger(Canvas.class);

    public static JSONObject contextPoolMedia(String canvasId, String categoryId, List context, String application, List<String> canvasIdsInheritable) {
        if (application == null) {
            application = "SITE";
        }
        JSONObject jsonResponse;
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
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Macys-ClientId", "NavApp");
        headers.put("X-Macys-Customer-Id", "1234");
        headers.put("X-Macys-RequestId", "123456");
        if (RunConfig.debugMode){
            LOGGER.info("--> Service Request URL: -> "+serviceUrl);
            LOGGER.info("--> Headers: -> "+headers.toString());
        }
        jsonResponse = new JSONObject(RESTOperations.doGET(serviceUrl, headers).readEntity(String.class));
        return jsonResponse;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("f5_vip").ipAddress + ":85/api/" + CONTEXT_SERVICE_ENDPOINT;
    }
}
