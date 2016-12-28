package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
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

    public static String earnPeriodType, redeemPeriodType, parentCampaignName, campaignName, campaignCode, queryPath;
    public static Statement statement;
    public static Connection connection;
    public static JSONObject campaignQueries = null;
    public static LocalDate customDate = null;
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void setCampaignName(String name) {
        campaignName = name;
        setCampaignInfo();
    }

    /**
     * Method to return all campaign periods
     *
     * @return all campaign periods
     */
    public static Map<String, Boolean> getAllCampaignPeriods() {
        Map<String, Boolean> campaign = new HashMap<>();
        if (statement == null) {
            setupConnection();
        }
        if (macys()) {
            disableOtherCampaign((campaignName.contains("ICW") ? "MMoney" : "ICWMMoney"));
        }
        List<Map<String, Object>> activeCampaigns = getActiveCampaignDetails(campaignCode);
        boolean earn = false, redeem = false, inBetween = false, outside = false;
        for (Map aCampaign : activeCampaigns) {
            String campaignType = aCampaign.get("name").toString();
            if (!(aCampaign.get("campaignCode").toString().equals(campaignCode))) {
                continue;
            }
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
     * Method to update campaign details
     *
     * @param campaignName to update
     */
    public static void updateCampaignDetailsInDatabase(String campaignName) {
        updateCampaignDetails(campaignName);
        System.out.println("Updated MB Money campaign to " + campaignName + " in database and cleared all MB Money caches!!");
    }

    /**
     * Returns all the details of campaign attributes
     *
     * @return campaign attribute values
     */
    public static List<Map<String, String>> getCampaignAttributeDetails() {
        List<Map<String, String>> campaignAttributes = new ArrayList<>();
        try {
            String sqlQuery = campaignQueries.getJSONObject("mb_money").getString("mb_money_attributes");
            if (statement == null) {
                setupConnection();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, earnPeriodType);
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
     * Returns all campaign exclusions using campaign name
     *
     * @return campaign exclusion details
     */
    public static List<Map<String, String>> getCampaignExclusionDetails() {
        List<Map<String, String>> campaignExclusions = new ArrayList<>();
        try {
            String sqlQuery = campaignQueries.getJSONObject("mb_money").getString("mb_money_exclusions");
            if (statement == null) {
                setupConnection();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, earnPeriodType);
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
    public static List<Map<String, Object>> getActiveCampaignDetails(String campaignCode) {
        List<Map<String, Object>> activeCampaigns = new ArrayList<>();

        try {
            String sqlQuery = campaignQueries.getJSONObject("mb_money").getString("all_campaigns");
            if (statement == null) {
                setupConnection();
            }
            ResultSet campaignDetails = statement.executeQuery(sqlQuery);
            if (customDate == null) {
                customDate = LocalDate.parse(dateFormat.format(DBUtils.getCustomDate()));
            }
            while (campaignDetails.next()) {
                if ((LocalDate.parse(dateFormat.format(campaignDetails.getTimestamp("effective_date"))).compareTo(customDate)) <= 0 && (LocalDate.parse(dateFormat.format(campaignDetails.getTimestamp("expiration_date"))).compareTo(customDate)) >= 0 && campaignCode.equals(campaignDetails.getString("campaign_code"))) {
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
     * Method to clear campaign cache
     */
    public static void clearAllMbmoneyRelatedCaches() {
        clearCustomerCampaignCache();
        clearOrderCampaignCache();
        clearShopAppCampaignCache();
        System.out.println("All MBMoney related caches are updated!!");
    }

    /**
     * Method used to update the details of campaign
     *
     * @param campaignPeriodType to update mbmoney campaign details
     */
    private static void updateCampaignDetails(String campaignPeriodType) {
        if (customDate == null) {
            customDate = LocalDate.parse(dateFormat.format(DBUtils.getCustomDate()));
        }
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
            updateCampaignDates(campaign.get("campaignName").toString(), campaign.get("effectiveDate").toString() + " 00:00:00.0", campaign.get("expirationDate").toString() + " 23:59:59.0");
        }
    }

    /**
     * Method used to update the details of parent or earn or redeem campaign details
     *
     * @param campaignPeriodType to updated campaign details
     * @param effectiveStart     to update campaign effective date
     * @param expiryDate         to update campaign expiration date
     */
    private static void updateCampaignDates(String campaignPeriodType, String effectiveStart, String expiryDate) {
        String campaignId = getCampaignDetails(campaignPeriodType, true).get("campaignId").toString();

        try {
            String sqlQuery = campaignQueries.getJSONObject("mb_money").getString("update_mb_money_campaign");
            if (statement == null) {
                setupConnection();
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(effectiveStart));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(expiryDate));
            preparedStatement.setString(3, campaignId);
            preparedStatement.executeUpdate();
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to update MBMoeny campaign dates: " + e);
        }
    }

    /**
     * Method used to disable the campaign
     *
     * @param name to updated campaign details
     */
    private static void disableOtherCampaign(String name) {
        List<Map<String, Object>> activeCampaigns = getActiveCampaignDetails((name.contains("ICW") ? "ICW" : (macys() ? "MMON" : "BMON")));
        if (customDate == null) {
            customDate = LocalDate.parse(dateFormat.format(DBUtils.getCustomDate()));
        }
        if (!activeCampaigns.isEmpty()) {
            updateCampaignDates(name, (customDate.minusDays(4)).toString() + " 00:00:00.0", (customDate.minusDays(1)).toString() + " 23:59:59.0");
            updateCampaignDates((name.contains("ICW") ? "ICWMEarn" : (macys() ? "MEarn" : "BEarn")), (customDate.minusDays(4)).toString() + " 00:00:00.0", (customDate.minusDays(3)).toString() + " 23:59:59.0");
            updateCampaignDates((name.contains("ICW") ? "ICWMRedeem" : (macys() ? "MRedeem" : "BRedeem")), (customDate.minusDays(2)).toString() + " 00:00:00.0", (customDate.minusDays(1)).toString() + " 23:59:59.0");
        }
    }

    /**
     * Returns an active record with all the details of a campaign using campaign name from site DB
     *
     * @param campaignType to get campaign details
     * @return campaign details
     */
    public static Map<String, Object> getCampaignDetails(String campaignType, boolean insertCampaignFlag) {
        Map<String, Object> campaignDetails = new HashMap<>();

        try {
            String sqlQuery = campaignQueries.getJSONObject("mb_money").getString("mb_money_campaign_details");
            if (statement == null) {
                setupConnection();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
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
            } else if (insertCampaignFlag) {
                insertCampaignDetails();
                campaignDetails = getCampaignDetails(campaignType, false);
            }
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to get MBMoney campaign details: " + e);
        }
        return campaignDetails;
    }

    /**
     * Method to insert campaign details
     */
    private static void insertCampaignDetails() {
        try {
            JSONArray campaignConfigurationQueries = campaignQueries.getJSONObject(queryPath).getJSONArray("insert_campaign_configuration");
            JSONArray campaignExclusionsQueries = campaignQueries.getJSONObject("mb_money").getJSONArray("insert_campaign_exclusions");
            JSONArray campaignAttributesQueries = campaignQueries.getJSONObject(queryPath).getJSONArray("insert_campaign_attributes");
            if (statement == null) {
                setupConnection();
            }
            for (int index = 0; index < campaignConfigurationQueries.length(); index++)
                statement.executeUpdate(campaignConfigurationQueries.getString(index));
            for (int index = 0; index < campaignExclusionsQueries.length(); index++) {
                PreparedStatement preparedStatement = connection.prepareStatement(campaignExclusionsQueries.getString(index));
                preparedStatement.setString(1, earnPeriodType);
                preparedStatement.executeUpdate();
            }
            for (int index = 0; index < campaignAttributesQueries.length(); index++)
                statement.executeUpdate(campaignAttributesQueries.getString(index));
            System.out.println("Inserted " + campaignName + " campaign details !!");
        } catch (JSONException | SQLException e) {
            System.err.println("Unable to insert Campaign details: " + e);
        }
    }

    /**
     * Method to clear customer campaign cache
     */
    private static void clearCustomerCampaignCache() {
        String uri = "http://" + EnvironmentDetails.otherApp("mspcustomer").ipAddress + ":8080/api/customer/v1/customers/cache/clearcampaigns";
        Response response = RESTOperations.doDELETE(uri, null);
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
        Response response = RESTOperations.doGET(uri, null);
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
            Response response = RESTOperations.doGET(MainRunner.url.replace("http:", "https:") + url, null);
            if (response.getStatus() != 302) {
                throw new Exception("ShopApp MBMoney cache is not cleared properly");
            }
            System.out.println("ShopApp campaign cache cleared...");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /*
        To setup DB connection
     */
    private static void setupConnection() {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                System.out.println("Error occure while craeting database connection" + e.getMessage());
            }
        }
    }

    /*
        To define other campaign details.
     */
    private static void setCampaignInfo() {
        earnPeriodType = campaignName.contains("ICW") ? "ICWMEarn" : (macys() ? "MEarn" : "BEarn");
        redeemPeriodType = campaignName.contains("ICW") ? "ICWMRedeem" : (macys() ? "MRedeem" : "BRedeem");
        parentCampaignName = campaignName.contains("ICW") ? "ICWMoney" : (macys() ? "MMoney" : "BMoney");
        campaignCode = (campaignName.contains("ICW") ? "ICW" : (macys() ? "MMON" : "BMON"));
        queryPath = (campaignName.contains("ICW") ? "icw" : "mb") + "_money";
        campaignQueries = Utils.getSqlQueries();
    }

}
