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

    public static String getEnvironment() {
        return Utils.removeFromString(MainRunner.url, "www1.", "www.", "http://", "https://", "m.")
                .split("\\.")[0] + "/";
    }


}