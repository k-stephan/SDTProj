package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.io.IOException;



public class GiftCardService {

    /**
     * Returns valid gift card object
     *
     * @param cardType Type of gift card to get info for
     * @return GiftCard object.
     * @throws Exceptions.ProductionException if called while executing against production
     */
    public static GiftCard getValidGiftCardDetails(GiftCard.CardType cardType) throws Exceptions.ProductionException {
        if (StepUtils.prodEnv()) {
            throw new Exceptions.ProductionException("Cannot use services on prod!");
        }
        try {
            JSONArray json = new JSONArray(getGiftCardsResponse(cardType));
            for (Object o : json) {
                if (!(o instanceof JSONObject)) {
                    continue;
                }
                JSONObject giftCardJSON = (JSONObject) o;
                if (giftCardJSON.getDouble("Balance") > 0 && giftCardJSON.getDouble("Current_Balance") > 0) {
                    return new GiftCard(cardType,
                            giftCardJSON.getString("Gift_Card"),
                            giftCardJSON.getString("CardDesc"),
                            giftCardJSON.getString("CID"),
                            giftCardJSON.getString("ECID"),
                            null,
                            giftCardJSON.getDouble("Balance"),
                            giftCardJSON.getDouble("Current_Balance"),
                            giftCardJSON.getInt("Division"));

                }
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns getGiftCardsResponse
     *
     * @param cardType type of gift card to retrieve
     * @return getGiftCardsResponse
     * @throws IOException if response is unreadable
     * @throws Exceptions.ProductionException if called while executing against production
     */
    public static String getGiftCardsResponse(GiftCard.CardType cardType) throws IOException, Exceptions.ProductionException {
        if (StepUtils.prodEnv()) {
            throw new Exceptions.ProductionException("Cannot use services on prod!");
        }
        String serviceUrl = getGiftCardServiceUrl(cardType);
        Response response = RESTOperations.doGET(serviceUrl, null);
        return response.readEntity(String.class);
    }

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
        return RESTEndPoints.SIM_URL + environmentUrl + cardFullPath + AUTH_TOKEN;
    }
}
