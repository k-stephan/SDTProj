package com.macys.sdt.framework.utils.rest.utils;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Utils;

public class RESTEndPoints {

    public static final String CREATE_USER_PROFILE = "/customer/v1/users?_body=true";
    public static final String PRODUCTS_ENDPOINT = "catalog/v2/products/";
    public static final String ADD_TO_BAG = "order/v1/bags";
    public static final String INITIATE_CHECKOUT = "order/v1/checkout/initiate";
    public static final String PROCESS_CHECKOUT = "order/v1/checkout/process";
    public static final String SIM_URL = "http://sim.delivery.fds/sim/environments/";
    // http://11.168.39.100:8080/api/customer/v3/registries/
    public static final String CREATE_REGISTRY = "/api/customer/v3/registries";
    public static final String MCOM_API_KEY = "zkvrhg8ueup56sccnm83ef8m";
    public static final String BCOM_API_KEY = "ruq7dvpjbys2yv2w6czfcptq";
    public static final String SIM_AUTH_TOKEN = "N_GUrqG6Eq8oeCrvE0aZLA";

    /**
     * Gets the given environment - macys.com, bloomingdales.com, qa15codemacys.fds.com, etc.
     *
     * @return environment stripped of all http/www data and parameters
     */
    public static String getEnvironment() {
        return Utils.removeFromString(MainRunner.url, "www1.", "www.", "http://", "https://", "m.")
                .split("\\.")[0] + "/";
    }

    /**
     * Returns the full SIM url given a bucket type and any parameters
     *
     * @param bucket Which "bucket" your request falls under
     * @param params any parameters for the call
     * @return String containing full URL for REST call
     */
    public static String getSimUrl(SimBucket bucket, String params) {
        String url = SIM_URL + getEnvironment() + bucket.url + params + "?auth_token=" + RESTEndPoints.SIM_AUTH_TOKEN;
        return url.replaceAll(" ", "%20");
    }

    /**
     * All "buckets" in SIM that are currently working and supported
     */
    public enum SimBucket {
        PRODUCT("buckets/Products/"),
        GIFT_CARD("buckets/Gift Cards/"),
        CREDIT_CARD("buckets/Credit Cards/"),
        LOYALTY("buckets/Loyalty/");

        public final String url;

        SimBucket(String url) {
            this.url = url;
        }
    }


}