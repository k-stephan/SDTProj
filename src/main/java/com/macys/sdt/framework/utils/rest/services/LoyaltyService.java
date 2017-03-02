package com.macys.sdt.framework.utils.rest.services;

import com.macys.sdt.framework.model.LoyaltyAccount;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.util.Random;


/**
 * Interfaces with SIM Loyalty service to get loyalty data
 */
public class LoyaltyService {

    public static LoyaltyAccount getLoyaltyAccount(LoyaltyAccount.LoyaltyType type) {
        String url = RESTEndPoints.getSimUrl(RESTEndPoints.SimBucket.LOYALTY, type.simUrl);
        Response response = RESTOperations.doGET(url, null);
        String jsonText = response.readEntity(String.class);
        JSONArray accounts = new JSONArray(jsonText);
        JSONObject accountJSON = accounts.getJSONObject(new Random().nextInt(accounts.length()));
        return new LoyaltyAccount(accountJSON);
    }

}
