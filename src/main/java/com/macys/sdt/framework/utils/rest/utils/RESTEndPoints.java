package com.macys.sdt.framework.utils.rest.utils;

import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;

public class RESTEndPoints {

    public static final String CREATE_USER_PROFILE = "/api/customer/v1/users";
    public static final String PRODUCTS_ENDPOINT = "catalog/v2/products/";
    public static final String ADD_TO_BAG = "order/v1/bags";
    public static final String INITIATE_CHECKOUT = "order/v1/checkout/initiate";
    public static final String PROCESS_CHECKOUT = "order/v1/checkout/process";

    public static String getGiftCardServiceUrl(GiftCard.CardType cardType) {
        final String AUTH_TOKEN = "N_GUrqG6Eq8oeCrvE0aZLA";
        String environmentUrl = MainRunner.url.split("\\.")[1], cardPath = "Min Balance (<$50)";
        String cardFullPath = null;
        switch (cardPath) {
            case "Min Balance (<$50)":
                cardFullPath = "/buckets/Gift%20Cards/Gift%20Cards::" + cardType + "::Min%20Balance%20(%3C$50)?auth_token=";
                break;
            case "Regular Balance ($50 - $2000)":
                cardFullPath = "/buckets/Gift%20Cards/Gift%20Cards::" + cardType + "::Regular%20Balance%20($50%20-%20$2000)?auth_token=";
                break;
            default:
                Assert.fail("Incorrect cardPath (" + cardPath + ") found!!");
        }
        return "http://sim.delivery.fds/sim/environments/" + environmentUrl + cardFullPath + AUTH_TOKEN;
    }

}