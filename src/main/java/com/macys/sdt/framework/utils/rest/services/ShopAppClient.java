package com.macys.sdt.framework.utils.rest.services;


import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShopAppClient {
    private static String shopUrl = RunConfig.url.replace("http://", "https://") + "/chkout";
    private static Map<String, String> headers;

    static {
        headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("X-requested-With", "XMLHttpRequest");
    }

    public static JSONObject getOrderSummary() {
        return getDataFromClient("rcordersummary", "responsiveOrderSummary");
    }

    public static JSONObject getShippingOptions() {
        return getDataFromClient("rcshipping", "responsiveShipping");
    }

    public static JSONObject getPaymentOptions() {
        return getDataFromClient("rcpayment", "responsivePayment");
    }

    public static JSONObject getSignedInOrderDetails() {
        return getDataFromClient("order", "order");
    }

    public static JSONObject getSelectedCreditCard() {
        return getDataFromClient("order/selectedcreditcard", "order");
    }

    public static JSONObject getSelectedShippingAddress() {
        return getDataFromClient("order/selectedshippingaddress", "order");
    }

    public static JSONObject getSignedInPlaceOrderDetails() {
        return getDataFromClient("order/placeorder", "order");
    }

    public static JSONObject get3DSLookup() {
        return getDataFromClient("rcplaceorder/populateThreeDSecureLookupResponse", "threeDSecureLookupResponse");
    }

    public static JSONObject getPrePurchasePoints() {
        return getDataFromClient("prepurchasepoints", "checkoutPrePurchasePointsVB");
    }

    private static JSONObject getDataFromClient(String path, String jsonName) {
        headers.put("Cookie", (String) Navigate.execJavascript("return document.cookie"));
        return new JSONObject(RESTOperations.doGET(shopUrl + "/" + path, headers).readEntity(String.class))
                .getJSONObject(jsonName);
    }


}
