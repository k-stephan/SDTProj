package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.IOException;


public class GiftCardService {

    /**
     * Returns valid gift card object
     *
     * @param cardType Type of gift card to get info for
     * @return GiftCard object.
     */
    public static GiftCard getValidGiftCardDetails(GiftCard.CardType cardType) {
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
     */
    public static String getGiftCardsResponse(GiftCard.CardType cardType) throws IOException {
        String serviceUrl = RESTEndPoints.getGiftCardServiceUrl(cardType);
        Response response = RESTOperations.doGET(serviceUrl, null);
        return response.readEntity(String.class);
    }

}
