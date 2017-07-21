package com.macys.sdt.framework.utils.db.models;


import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ReturnService {

    private static final Logger logger = LoggerFactory.getLogger(ReturnService.class);

    public Statement statement;
    public Connection connection;

    /**
     * Delete return record from order table
     *
     * @param orderNumber order number to delete
     */
    public void deleteReturnRecord(String orderNumber) {
        try {
            setupConnection();
            JSONObject queries = Utils.getSqlQueries();
            ResultSet rs = statement.executeQuery(queries.getJSONObject("returns").get("return_shipment_using_order").toString().replace("?", orderNumber));
            String returnShipmentId;
            if (rs.next()) {
                returnShipmentId = rs.getString("RETURN_SHIPMENT_ID");
                statement.executeUpdate(queries.getJSONObject("returns").get("delete_return_lineitem").toString().replace("?", returnShipmentId));
                statement.executeUpdate(queries.getJSONObject("returns").get("delete_return_shipment_label").toString().replace("?", returnShipmentId));
                statement.executeUpdate(queries.getJSONObject("returns").get("delete_return_pickup_info").toString().replace("?", returnShipmentId));
                statement.executeUpdate(queries.getJSONObject("returns").get("delete_return_shipment").toString().replace("?", returnShipmentId));
            }
        } catch (SQLException | JSONException e) {
            Assert.fail("delete return record failed : " + e.getMessage());
        }
    }

    /**
     * Find if the order number exists in database or not
     *
     * @param orderNumber order number to check
     * @return order exist return true
     */
    public boolean orderExistsByOrderNumber(String orderNumber) {
        try {
            setupConnection();
            JSONObject queries = Utils.getSqlQueries();
            ResultSet rs = statement.executeQuery(queries.getJSONObject("returns").get("select_order").toString().replace("?", orderNumber));
            if (rs.next()) {
                return true;
            }
        } catch (SQLException | JSONException e) {
            Assert.fail("orderExistsByOrderNumber(): " + e);
        }
        return false;
    }

    /**
     * Delete order record from database
     *
     * @param orderNumber order number to delete
     */
    public void deleteOrderRecord(String orderNumber) {
        try {
            setupConnection();
            JSONObject queries = Utils.getSqlQueries();
            statement.executeUpdate(queries.getJSONObject("returns").get("delete_order").toString().replace("?", orderNumber));
        } catch (SQLException | JSONException e) {
            Assert.fail("deleteOrderRecord(): " + e);
        }
    }

    /**
     * insert order number into database
     *
     * @param orderNumber order number to insert
     * @param email       email address to insert
     */
    public void insertOrderByOrderNumber(String orderNumber, String email) {
        try {
            String billingContId = getBillingContactId();
            String userId = getUserId(email);
            JSONObject queries = Utils.getSqlQueries();
            String setOrderNum = queries.getJSONObject("returns").get("insert_order").toString().replaceFirst("'\\?'", "'" + orderNumber + "'").replaceFirst("'\\?'", "'" + userId + "'").replaceFirst("'\\?'", "'" + billingContId + "'");
            PreparedStatement preparedStatement = connection.prepareStatement(setOrderNum);
            preparedStatement.executeUpdate();
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get user id from user table
     *
     * @param email user email address
     * @return userId
     */
    public String getUserId(String email) {
        setupConnection();
        JSONObject queries = Utils.getSqlQueries();
        try {
            ResultSet resultSet = statement.executeQuery(queries.getJSONObject("returns").get("user_details").toString().replace("?", email));
            if (resultSet.next()) {
                return resultSet.getString("USER_ID");
            }
        } catch (SQLException | JSONException e) {
            logger.error("Unable to get user ID for email: " + email);
        }
        return null;
    }

    /**
     * Get any billing contact Id from order table
     *
     * @return billing contact id
     */
    public String getBillingContactId() {
        setupConnection();
        JSONObject queries = Utils.getSqlQueries();
        try {
            ResultSet resultSet = statement.executeQuery(queries.getJSONObject("returns").get("first_order_details").toString());
            if (resultSet.next()) {
                return resultSet.getString("BILLING_CONTACT");
            }
        } catch (SQLException | JSONException e) {
            logger.error("Unable to get billing contact ID from DB: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update Return Status in DB
     *
     * @param returnStatus       value to change return status to
     * @param returnOrderDetails JSONObject with return order details
     */
    public void updateReturnStatus(String returnStatus, JSONObject returnOrderDetails) {
        setupConnection();
        JSONObject queries = Utils.getSqlQueries();
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        String locationId;
        try {
            switch (returnStatus) {
                case "intransit":
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_return_shipment_status_cd").toString());
                    preparedStatement.setString(1, "T");
                    preparedStatement.setString(2, returnOrderDetails.getString("order_number"));
                    preparedStatement.executeUpdate();
                    break;
                case "received":
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_return_shipment_status_cd").toString());
                    preparedStatement.setString(1, "D");
                    preparedStatement.setString(2, returnOrderDetails.getString("order_number"));
                    preparedStatement.executeUpdate();
                    break;
                case "incomplete":
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("get_return_shipment_details").toString());
                    preparedStatement.setString(1, returnOrderDetails.getString("order_number"));
                    resultSet = preparedStatement.executeQuery();
                    String timeStamp = null;
                    while (resultSet.next()) {
                        timeStamp = resultSet.getString("return_submitted_ts");
                    }
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_return_shipment_submitted_ts").toString());
                    preparedStatement.setString(1, timeStamp);
                    preparedStatement.setString(2, returnOrderDetails.getString("order_number"));
                    preparedStatement.executeUpdate();
                    break;
                case "incomplete_with_items_missing":
                    locationId = getLocationId(returnOrderDetails);
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_return_shipment_return_address_id").toString());
                    preparedStatement.setString(1, locationId);
                    preparedStatement.setString(2, returnOrderDetails.getString("order_number"));
                    preparedStatement.executeUpdate();
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_line_item_incomplete_with_items_missing").toString());
                    preparedStatement.setString(1, returnOrderDetails.getString("item_quantity"));
                    preparedStatement.setString(2, returnOrderDetails.getString("fill_division_cd"));
                    preparedStatement.setString(3, returnOrderDetails.getString("fill_store_nbr"));
                    preparedStatement.executeUpdate();
                    break;
                case "incomplete_with_items_not_received":
                    locationId = getLocationId(returnOrderDetails);
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_return_shipment_return_address_id").toString());
                    preparedStatement.setString(1, locationId);
                    preparedStatement.setString(2, returnOrderDetails.getString("order_number"));
                    preparedStatement.executeUpdate();
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_line_item_incomplete_with_items_not_received").toString());
                    preparedStatement.setString(1, returnOrderDetails.getString("upc_id"));
                    preparedStatement.setString(2, returnOrderDetails.getString("fill_division_cd"));
                    preparedStatement.setString(3, returnOrderDetails.getString("fill_store_nbr"));
                    preparedStatement.setString(4, returnOrderDetails.getString("$vendor_nm"));
                    preparedStatement.setString(5, returnOrderDetails.getString("item_desc"));
                    preparedStatement.executeUpdate();
                    break;
                case "complete":
                    locationId = getLocationId(returnOrderDetails);
                    preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("update_return_shipment_return_address_id").toString());
                    preparedStatement.setString(1, locationId);
                    preparedStatement.setString(2, returnOrderDetails.getString("order_number"));
                    preparedStatement.executeUpdate();
                    break;
            }
        } catch (SQLException | JSONException e) {
            logger.error("Unable to update return status: " + e.getMessage());
        }
    }

    /**
     * Get the location ID from location_address table
     *
     * @param returnOrderDetails order info
     * @return location Id
     */
    public String getLocationId(JSONObject returnOrderDetails) {
        String locationId = null;
        setupConnection();
        JSONObject queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("get_location_id").toString());
            preparedStatement.setString(1, returnOrderDetails.getString("fill_division_cd"));
            preparedStatement.setString(2, returnOrderDetails.getString("fill_store_nbr"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                locationId = resultSet.getString("location_address_id");
            }
            return locationId;
        } catch (SQLException | JSONException e) {
            logger.error("Unable to get location id: " + e.getMessage());
        }
        return null;
    }


    public Map getReturnInitiatedOrder(String orderNumber) {
        Map<String, String> returnOrderInfo = new HashMap<>();
        setupConnection();
        JSONObject queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("maximum_return_shipment_id").toString());
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            String shipmentId = null;
            while (resultSet.next()) {
                shipmentId = resultSet.getString("return_shipment_id");
            }
            preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("with_return_line_item_shipment_id").toString());
            preparedStatement.setString(1, shipmentId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                returnOrderInfo.put("upc", resultSet.getString("upc"));
                returnOrderInfo.put("itemDesc", resultSet.getString("item_desc"));
                returnOrderInfo.put("colorDesc", resultSet.getString("color_desc"));
                returnOrderInfo.put("sizeDesc", resultSet.getString("size_desc"));
                returnOrderInfo.put("quantity", resultSet.getString("quantity"));
                returnOrderInfo.put("vendorNm", resultSet.getString("vendor_nm"));
            }
            return returnOrderInfo;
        } catch (SQLException | JSONException e) {
            logger.error("Unable to get return initiated order: " + e.getMessage());
        }
        return null;
    }

    public String getStatusUpdatedDate(String returnStatus, String orderNumber) {
        String date = null;
        setupConnection();
        Map<String, String> statusCode = new HashMap<>();
        statusCode.put("RETURN STATUS Submitted", "S");
        statusCode.put("RETURN STATUS Intransit", "T");
        statusCode.put("RETURN STATUS Received", "D");
        statusCode.put("RETURN STATUS Incomplete", "I");
        JSONObject queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("returns").get("get_line_items_for_shipment").toString());
            preparedStatement.setString(1, orderNumber);
            preparedStatement.setString(2, statusCode.get(returnStatus));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DateFormat mcomFormat = new SimpleDateFormat("MM/dd/YYYY");
                DateFormat bcomFormat = new SimpleDateFormat("MMMM dd, YYYY");
                date = ((StepUtils.macys()) ? (mcomFormat.format(Date.valueOf(resultSet.getString("status_updated_ts").split(" ")[0]))) : (bcomFormat.format(Date.valueOf(resultSet.getString("status_updated_ts").split(" ")[0]))));
            }
            if (date == null) {
                Assert.fail("ERROR - ENV Database returned nil for order number #{order_number}");
            }
        } catch (SQLException | JSONException e) {
            logger.error("Unable to get date of status update for order : " + orderNumber);
        }
        return date;
    }

    /**
     * get user details corresponding to an order number
     *
     * @param orderNum order_number of an order
     * @return user details containing email and password associated to the order number given
     */
    public Map getUserDetails(String orderNum) {
        setupConnection();
        Map<String, String> userData = new HashMap<>();
        try {
            JSONObject queries = Utils.getSqlQueries();
            PreparedStatement ps = connection.prepareStatement(queries.getJSONObject("returns").get("return_order").toString());
            ps.setString(1, orderNum);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement userDetail = connection.prepareStatement(queries.getJSONObject("returns").get("user_detail").toString());
                userDetail.setString(1, rs.getString("USER_ID"));
                ResultSet us = userDetail.executeQuery();
                if (us.next()) {
                    userData.put("email", us.getString("EMAIL_ADDRESS"));
                    userData.put("password", us.getString("PASSWORD"));
                }
            }
        } catch (Exception e) {
            Assert.fail("Unable to found user details in db");
        }
        return userData;
    }

    /**
     * setup db connection
     */
    private void setupConnection() {
        try {
            if (statement == null) {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            }
        } catch (SQLException e) {
            logger.error("Error occurs while creating database connection : " + e.getMessage());
        }
    }
}
