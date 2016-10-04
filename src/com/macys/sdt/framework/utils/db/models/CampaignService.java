package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignService extends StepUtils {

    /**
     * Method to return all mb money campaign periods
     *
     * @return all mb money campaign periods
     */
    public static Map<String, Boolean> getAllMbmoneyCampaignPeriods() {
        Map<String, Boolean> campaign = new HashMap<>();

        List<Map<String, Object>> activeCampaigns = getActiveCampaignDetails();

        String earnPeriodType = macys() ? "MEarn" : "BEarn";
        String redeemPeriodType = macys() ? "MRedeem" : "BRedeem";
        String parentCampaignName = macys() ? "MMoney" : "BMoney";

        boolean earn = false, redeem = false, inBetween = false, outside = false;

        for (Map aCampaign : activeCampaigns) {
            String campaignType = aCampaign.get("name").toString();

            if (campaignType.equals(earnPeriodType)) {
                earn = true;
                break;
            } else if (campaignType.equals(redeemPeriodType)) {
                redeem = true;
                break;
            } else if (campaignType.equals(parentCampaignName) && !(campaignType.equals(earnPeriodType)) && !(campaignType.equals(redeemPeriodType))) {
                inBetween = true;
                break;
            } else if (!(campaignType.equals(parentCampaignName)) && !(campaignType.equals(earnPeriodType)) && !(campaignType.equals(redeemPeriodType))) {
                outside = true;
                break;
            }
        }

        campaign.put("earn", earn);
        campaign.put("redeem", redeem);
        campaign.put("in_between", inBetween);
        campaign.put("outside", outside);

        return campaign;
    }

    /**
     * Method to update mb money campaign details
     *
     * @param campaignName to update
     */
    public static void updateMbmoneyCampaignDetailsInDatabase(String campaignName) {
        updateMbmoneyCampaignDetails(campaignName);
        clearAllMbmoneyRelatedCaches();
        System.out.println("Updated MB Money campaign to " + campaignName + " in database and cleared all MB Money caches!!");
    }

    /**
     * Returns all the details of mbmoney campaign attributes
     *
     * @param campaignName to get campaign attribute values
     * @return campaign attribute values
     */
    public static List<Map<String, String>> getMbmoneyCampaignAttributeDetails(String campaignName) {
        List<Map<String, String>> campaignAttributes = new ArrayList<>();
        try {
            JSONObject mbmoneyQueries = (JSONObject) Utils.getSqlQueries().get("mb_money");
            String sqlQuery = mbmoneyQueries.get("mb_money_attributes").toString();
            Connection con = DBUtils.setupDBConnection();

            PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
            preparedStatement.setString(1, campaignName);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Map<String, String> cAttributes = new HashMap<>();

                cAttributes.put("campaignId", rs.getString("campaign_id"));
                cAttributes.put("campaignAttrType", rs.getString("campaign_attr_type"));
                cAttributes.put("campaignAttrName", rs.getString("campaign_attr_name"));
                cAttributes.put("campaignAttrValue", rs.getString("campaign_attr_value"));

                campaignAttributes.add(cAttributes);
            }
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to get MBMoeny campaign exclusion details: " + e);
        }
        return campaignAttributes;
    }

    /**
     * Returns all mbmoney campaign exclusions using campaign name
     *
     * @param campaignName to get campaign exclusions
     * @return campaign exclusion details
     */
    public static List<Map<String, String>> getMbmoneyCampaignExclusionDetails(String campaignName) {
        List<Map<String, String>> campaignExclusions = new ArrayList<>();
        try {
            JSONObject mbmoneyQueries = (JSONObject) Utils.getSqlQueries().get("mb_money");
            String sqlQuery = mbmoneyQueries.get("mb_money_exclusions").toString();
            Connection con = DBUtils.setupDBConnection();

            PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
            preparedStatement.setString(1, campaignName);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Map<String, String> cExclusions = new HashMap<>();

                cExclusions.put("campaignId", rs.getString("campaign_id"));
                cExclusions.put("exclusionName", rs.getString("exclusion_name"));
                cExclusions.put("exclusionValue", rs.getString("exclusion_value"));

                campaignExclusions.add(cExclusions);
            }
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to get MBMoeny campaign exclusion details: " + e);
        }
        return campaignExclusions;
    }

    /**
     * To get the active campaign details from database
     *
     * @return active campaign details
     */
    private static List<Map<String, Object>> getActiveCampaignDetails() {
        List<Map<String, Object>> activeCampaigns = new ArrayList<>();

        try {
            JSONObject mbmoneyQueries = (JSONObject) Utils.getSqlQueries().get("mb_money");
            String sqlQuery = mbmoneyQueries.get("all_campaigns").toString();
            Connection con = DBUtils.setupDBConnection();

            Statement st = con.createStatement();
            ResultSet campaignDetails = st.executeQuery(sqlQuery);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate customDate = LocalDate.parse(df.format(DBUtils.getCustomDate()));

            while (campaignDetails.next()) {
                if ((LocalDate.parse(df.format(campaignDetails.getTimestamp("effective_date"))).compareTo(customDate)) <= 0 && (LocalDate.parse(df.format(campaignDetails.getTimestamp("expiration_date"))).compareTo(customDate)) >= 0) {
                    Map<String, Object> campaign = new HashMap<>();

                    campaign.put("campaignId", campaignDetails.getString("campaign_id"));
                    campaign.put("campaignCode", campaignDetails.getString("campaign_code"));
                    campaign.put("name", campaignDetails.getString("name"));
                    campaign.put("parentId", campaignDetails.getString("parent_id"));
                    campaign.put("effectiveDate", campaignDetails.getTimestamp("effective_date"));
                    campaign.put("expirationDate", campaignDetails.getTimestamp("expiration_date"));

                    activeCampaigns.add(campaign);
                }
            }
        } catch (JSONException | SQLException e) {
            System.err.println("Failed to get active campaign details: " + e);
        }
        return activeCampaigns;
    }

    /**
     * Method used to update the details of campaign
     *
     * @param campaignPeriodType to update mbmoney campaign details
     */
    private static void updateMbmoneyCampaignDetails(String campaignPeriodType) {
        String earnPeriodType = macys() ? "MEarn" : "BEarn";
        String redeemPeriodType = macys() ? "MRedeem" : "BRedeem";
        String parentCampaignName = macys() ? "MMoney" : "BMoney";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate customDate = LocalDate.parse(df.format(DBUtils.getCustomDate()));

        List<Map<String, Object>> newCampaignData = new ArrayList<>();
        Map<String, Object> earnCampaign = new HashMap<>();
        Map<String, Object> redeemCampaign = new HashMap<>();
        Map<String, Object> parentCampaign = new HashMap<>();

        if (campaignPeriodType.equals(earnPeriodType)) {
            earnCampaign.put("campaignName", earnPeriodType);
            earnCampaign.put("effectiveDate", customDate.minusDays(1));
            earnCampaign.put("expirationDate", customDate);
            newCampaignData.add(earnCampaign);
            redeemCampaign.put("campaignName", redeemPeriodType);
            redeemCampaign.put("effectiveDate", customDate.plusDays(1));
            redeemCampaign.put("expirationDate", customDate.plusDays(2));
            newCampaignData.add(redeemCampaign);
            parentCampaign.put("campaignName", parentCampaignName);
            parentCampaign.put("effectiveDate", customDate.minusDays(1));
            parentCampaign.put("expirationDate", customDate.plusDays(2));
            newCampaignData.add(parentCampaign);
        } else if (campaignPeriodType.equals(redeemPeriodType)) {
            earnCampaign.put("campaignName", earnPeriodType);
            earnCampaign.put("effectiveDate", customDate.minusDays(2));
            earnCampaign.put("expirationDate", customDate.minusDays(1));
            newCampaignData.add(earnCampaign);
            redeemCampaign.put("campaignName", redeemPeriodType);
            redeemCampaign.put("effectiveDate", customDate);
            redeemCampaign.put("expirationDate", customDate.plusDays(1));
            newCampaignData.add(redeemCampaign);
            parentCampaign.put("campaignName", parentCampaignName);
            parentCampaign.put("effectiveDate", customDate.minusDays(2));
            parentCampaign.put("expirationDate", customDate.plusDays(1));
            newCampaignData.add(parentCampaign);
        } else if (campaignPeriodType.equals("in_between")) {
            earnCampaign.put("campaignName", earnPeriodType);
            earnCampaign.put("effectiveDate", customDate.minusDays(2));
            earnCampaign.put("expirationDate", customDate.minusDays(1));
            newCampaignData.add(earnCampaign);
            redeemCampaign.put("campaignName", redeemPeriodType);
            redeemCampaign.put("effectiveDate", customDate.plusDays(1));
            redeemCampaign.put("expirationDate", customDate.plusDays(2));
            newCampaignData.add(redeemCampaign);
            parentCampaign.put("campaignName", parentCampaignName);
            parentCampaign.put("effectiveDate", customDate.minusDays(2));
            parentCampaign.put("expirationDate", customDate.plusDays(2));
            newCampaignData.add(parentCampaign);
        } else {
            earnCampaign.put("campaignName", earnPeriodType);
            earnCampaign.put("effectiveDate", customDate.minusDays(4));
            earnCampaign.put("expirationDate", customDate.minusDays(3));
            newCampaignData.add(earnCampaign);
            redeemCampaign.put("campaignName", redeemPeriodType);
            redeemCampaign.put("effectiveDate", customDate.minusDays(2));
            redeemCampaign.put("expirationDate", customDate.minusDays(1));
            newCampaignData.add(redeemCampaign);
            parentCampaign.put("campaignName", parentCampaignName);
            parentCampaign.put("effectiveDate", customDate.minusDays(4));
            parentCampaign.put("expirationDate", customDate.minusDays(1));
            newCampaignData.add(parentCampaign);
        }

        for (Map campaign : newCampaignData) {
            updateMbmoneyCampaignDates(campaign.get("campaignName").toString(), campaign.get("effectiveDate").toString() + " 00:00:00.0", campaign.get("expirationDate").toString() + " 23:59:59.0");
        }
    }

    /**
     * Method to clear MB Money campaign cache
     */
    private static void clearAllMbmoneyRelatedCaches() {
        clearCustomerCampaignCache();
        clearOrderCampaignCache();
        clearShopAppCampaignCache();
        System.out.println("All MBMoney related caches are updated!!");
    }

    /**
     * Method used to update the details of parent or earn or redeem campaign details
     *
     * @param campaignPeriodType to updated campaign details
     * @param effectiveStart     to update campaign effective date
     * @param expiryDate         to update campaign expiration date
     */
    private static void updateMbmoneyCampaignDates(String campaignPeriodType, String effectiveStart, String expiryDate) {
        String campaignId = getMbmoneyCampaignDetails(campaignPeriodType).get("campaignId").toString();

        try {
            JSONObject mbmoneyQueries = (JSONObject) Utils.getSqlQueries().get("mb_money");
            String sqlQuery = mbmoneyQueries.get("update_mb_money_campaign").toString();
            Connection con = DBUtils.setupDBConnection();

            PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(effectiveStart));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(expiryDate));
            preparedStatement.setString(3, campaignId);
            preparedStatement.executeUpdate();
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to update MBMoeny campaign dates: " + e);
        }
    }

    /**
     * Returns an active record with all the details of a campaign using campaign name from site DB
     *
     * @param campaignType to get campaign details
     * @return campaign details
     */
    private static Map<String, Object> getMbmoneyCampaignDetails(String campaignType) {
        Map<String, Object> campaignDetails = new HashMap<>();

        try {
            JSONObject mbmoneyQueries = (JSONObject) Utils.getSqlQueries().get("mb_money");
            String sqlQuery = mbmoneyQueries.get("mb_money_campaign_details").toString();
            Connection con = DBUtils.setupDBConnection();

            PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
            preparedStatement.setString(1, campaignType);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    campaignDetails.put("campaignId", rs.getString("campaign_id"));
                    campaignDetails.put("campaignCode", rs.getString("campaign_code"));
                    campaignDetails.put("name", rs.getString("name"));
                    campaignDetails.put("parentId", rs.getString("parent_id"));
                    campaignDetails.put("effectiveDate", rs.getTimestamp("effective_date"));
                    campaignDetails.put("expirationDate", rs.getTimestamp("expiration_date"));
                }
            } else {
                insertCampaignDetails();
                campaignDetails = getMbmoneyCampaignDetails(campaignType);
            }
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to get MBMoney campaign details: " + e);
        }
        return campaignDetails;
    }

    private static void insertCampaignDetails() {
        try {
            JSONObject mbmoneyQueries = (JSONObject) Utils.getSqlQueries().get("mb_money");
            Connection con = DBUtils.setupDBConnection();
            Statement statement = con.createStatement();
            for (int index = 0; index < mbmoneyQueries.getJSONArray("insert_campaign_configuration").length(); index++)
                statement.executeUpdate(mbmoneyQueries.getJSONArray("insert_campaign_configuration").getString(index));
            for (int index = 0; index < mbmoneyQueries.getJSONArray("insert_campaign_exclusions").length(); index++)
                statement.executeUpdate(mbmoneyQueries.getJSONArray("insert_campaign_exclusions").getString(index));
            for (int index = 0; index < mbmoneyQueries.getJSONArray("insert_campaign_attributes").length(); index++)
                statement.executeUpdate(mbmoneyQueries.getJSONArray("insert_campaign_attributes").getString(index));
            System.out.println("Inserted MB Money campaign details !!");
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to insert Campaign details: " + e);
        }
    }

    /**
     * Method to clear customer campaign cache
     */
    private static void clearCustomerCampaignCache() {
        String uri = "http://" + EnvironmentDetails.otherApp("mspcustomer").ipAddress + ":8080/api/customer/v1/customers/cache/clearcampaigns";
        Response response = RESTOperations.doDELETE(uri);
        if (response.getStatus() != 204) {
            throw new RuntimeException("HTTP error code : " + response.getStatus());
        }
        System.out.println("Customer campaign cache cleared...");
    }

    /**
     * Method to clear order campaign cache
     */
    private static void clearOrderCampaignCache() {
        String uri = "http://" + EnvironmentDetails.otherApp("msporder").ipAddress + ":8080/sdp/cache/purge-campaigns-cache";
        Response response = RESTOperations.doGET(uri);
        if (response.getStatus() != 200) {
            throw new RuntimeException("HTTP error code : " + response.getStatus());
        }
        System.out.println("Order campaign cache cleared...");
    }

    /**
     * Method to clear shopapp cache
     */
    private static void clearShopAppCampaignCache() {
        String url = "/account/campaigncontent?removeCache=true";

        try {
            Response response = RESTOperations.doGET(MainRunner.url + url);
            if (response.getStatus() != 302) {
                throw new Exception("ShopApp MBMoney cache is not cleared properly");
            }
            System.out.println("ShopApp campaign cache cleared...");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
