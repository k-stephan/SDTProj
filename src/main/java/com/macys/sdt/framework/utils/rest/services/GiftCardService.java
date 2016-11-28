package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class GiftCardService {

    /**
     * Returns valid gift card object
     *
     * @param cardType Type of gift card to get info for
     * @return GiftCard object.
     */
    public static GiftCard getValidGiftCardDetails(GiftCard.CardType cardType) {
        GiftCard giftCard = null;
        try {
            String json = getGiftCardsResponse(cardType);
            JSONArray jsonObject = new JSONArray(json);
            for (int index = 0; index < jsonObject.length(); index++) {
                JSONObject giftCardObject = jsonObject.getJSONObject(index);
                if (giftCardObject.getDouble("Balance") > 0 && giftCardObject.getDouble("Current_Balance") > 0) {
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
     * @param cardType type of gift card to retrieve
     * @return getGiftCardsResponse
     * @throws IOException if response is unreadable
     */
    public static String getGiftCardsResponse(GiftCard.CardType cardType) throws IOException {
        String serviceUrl = RESTEndPoints.getGiftCardServiceUrl(cardType);
        Response response = RESTOperations.doGET(serviceUrl, null);
        return response.readEntity(String.class);
    }

}
