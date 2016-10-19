package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.resources.model.GiftCard;
import com.macys.sdt.framework.runner.MainRunner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class GiftCardService {

    /**
     * Returns valid gift card object
     *
     * @return GiftCard object.
     * @throws IOException
     */
    public static GiftCard getValidGiftCardDetails(GiftCard.CardType cardType) {
        GiftCard giftCard = null;
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

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return giftCard;
    }

    /**
     * Returns getGiftCardsResponse
     *
     * @return getGiftCardsResponse.
     * @throws IOException
     */
    public static String getGiftCardsResponse(GiftCard.CardType cardType) throws IOException {
        String serviceUrl = getGiftCardServiceUrl(cardType);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serviceUrl);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = reader.readLine()) != null)
            response.append(inputLine);
        reader.close();
        httpClient.close();
        return response.toString();
    }

    private static String getGiftCardServiceUrl(GiftCard.CardType cardType){
        String environmentUrl = MainRunner.url.split("\\.")[1], cardPath = "Min Balance (<$50)", cardFullPath = null, requestUrl = null, tokenName = "N_GUrqG6Eq8oeCrvE0aZLA";
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
