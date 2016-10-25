package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import javax.ws.rs.core.Response;


public class GiftCardService {

    /**
     * Returns valid gift card object
     *
     * @return GiftCard object
     */
    public static GiftCard getValidGiftCardDetails(GiftCard.CardType cardType) {
        GiftCard giftCard;
        try {
            String json = getGiftCardsResponse(cardType);
            JSONArray jsonObject = new JSONArray(json);
            for (int index = 0;index<jsonObject.length();index++){
                JSONObject giftCardObject = jsonObject.getJSONObject(index);
                if(giftCardObject.getDouble("Balance") > 0 && giftCardObject.getDouble("Current_Balance") > 0){
                    giftCard = new GiftCard(GiftCard.CardType.fromString(giftCardObject.getString("Card_Type")),
                            giftCardObject.getString("Gift_Card"),
                            giftCardObject.getString("CardDesc"),
                            giftCardObject.getString("CID"),
                            giftCardObject.getString("ECID"),
                            null,
                            giftCardObject.getDouble("Balance"),
                            giftCardObject.getDouble("Current_Balance"),
                            giftCardObject.getInt("Division"));
                    return giftCard;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns getGiftCardsResponse
     *
     * @return getGiftCardsResponse.
     */
    public static String getGiftCardsResponse(GiftCard.CardType cardType) {
        try {
            String serviceUrl = getGiftCardServiceUrl(cardType);
            final Response response = RESTOperations.doGET(serviceUrl, null);
            String responseBody = response.readEntity(String.class);
            response.close();
            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getGiftCardServiceUrl(GiftCard.CardType cardType){
        String environmentUrl = MainRunner.url.split("\\.")[1], cardPath = "Min Balance (<$50)", cardFullPath = null, requestUrl, tokenName = "N_GUrqG6Eq8oeCrvE0aZLA";
        switch (cardPath){
            case "Min Balance (<$50)":
                cardFullPath = "/buckets/Gift%20Cards/Gift%20Cards::" + cardType + "::Min%20Balance%20(%3C$50)?auth_token=";
                break;
            case "Regular Balance ($50 - $2000)":
                cardFullPath = "/buckets/Gift%20Cards/Gift%20Cards::" + cardType + "::Regular%20Balance%20($50%20-%20$2000)?auth_token=";
                break;
            default:
                Assert.fail("Incorrect cardPath ("+cardPath+") found!!");
        }
        requestUrl = "http://sim.delivery.fds/sim/environments/" + environmentUrl + cardFullPath + tokenName;
        return requestUrl;
    }
}
