package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.sql.*;
import java.util.*;

public class OrderServices {

    private static final Logger logger = LoggerFactory.getLogger(OrderServices.class);

    public Statement statement;
    public Connection connection;
    public JSONObject queries;

    public OrderServices() {
        setupConnection();
        queries = Utils.getSqlQueries();
    }

    /**
     * Method to get orderDetails from site DB(table:ORDER) for a given order number
     *
     * @param orderNumber order number to look up
     * @return orderDetails in hashMap format
     **/
    public HashMap getOrderDetails(String orderNumber) {
        HashMap<String, String> orderDetails = new HashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service")
                    .getString("order_details").replaceFirst("'\\?'", "'" + orderNumber + "'"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                orderDetails.put("USER_ID", resultSet.getString("USER_ID"));
                orderDetails.put("TAX", resultSet.getString("TAX"));
                orderDetails.put("ORDER_STATUS", resultSet.getString("ORDER_STATUS"));
                orderDetails.put("RESERVATION_ID", resultSet.getString("RESERVATION_ID"));
                orderDetails.put("BASE_FEE", resultSet.getString("BASE_FEE"));
                orderDetails.put("ADJUSTED_BASE_FEE", resultSet.getString("ADJUSTED_BASE_FEE"));
                orderDetails.put("RESERVATION_STATUS", resultSet.getString("RESERVATION_STATUS"));
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving order details due to : " + e.getMessage());
        }
        return orderDetails;
    }

    /**
     * Method to get orderContextInfo from site DB(table:ORDER_CONTEXT_INFO) for a given order number
     *
     * @param orderNumber order number to look up for order context info
     * @return order context info details in hashMap format
     */
    public HashMap<String, String> getOrderContextInfo(String orderNumber) {
        HashMap<String, String> orderContextInfo = new HashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service")
                    .getString("order_context_info"));
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                orderContextInfo.put("DEVICE_INFO", resultSet.getString("DEVICE_INFO"));
                orderContextInfo.put("CLIENT_ID", resultSet.getString("CLIENT_ID"));
                orderContextInfo.put("SUB_CLIENT_ID", resultSet.getString("SUB_CLIENT_ID"));
            }
        } catch (SQLException e) {
            logger.warn("Error in retrieving order context info due to : " + e.getMessage());
        }
        return orderContextInfo;
    }

    /**
     * Method to get order attributes from site DB(table:ORDER_ATTRIBUTES) for a given order number
     *
     * @param orderNumber order number to look up for order attributes
     * @return order attributes in hashMap format
     */
    public HashMap<String, String> getAllOrderAttributes(String orderNumber) {
        HashMap<String, String> orderAttributes = new HashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service")
                    .getString("order_attributes"));
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                orderAttributes.put(resultSet.getObject(1).toString(), resultSet.getObject(2).toString());
            }
        } catch (SQLException e) {
            logger.warn("Error in retrieving order attributes due to : " + e.getMessage());
        }
        return orderAttributes;
    }

    /**
     * Method to get EPS authorization request from site DB(table:EPS_LOG) for a given order number
     *
     * @param orderNumber order number to look up for EPS authorization request
     * @param paymentType mode of payment for the order (paypal/creditcard)
     * @return EPS authorization request in Element format
     */
    public Element getEpsAuthorizationRequest(String orderNumber, String paymentType) {
        Element epsAuthorizationRequest = null;
        String serviceName = null;
        switch (paymentType.toLowerCase()) {
            case "paypal":
                serviceName = "eps_paypal_authorization_request";
                break;
            case "creditcard":
            case "credit card":
                serviceName = "eps_authorization_request";
                break;
            default:
                throw new IllegalArgumentException("Unexpected payment type given: " + paymentType);
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service").getString(serviceName));
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String xmlData = resultSet.getString("XML_DATA");
                epsAuthorizationRequest = getXmlElements(xmlData);
            }
        } catch (SQLException e) {
            logger.warn("Error in retrieving eps authorization request due to : " + e.getMessage());
        }
        return epsAuthorizationRequest;
    }

    /**
     * Method to get EPS authorization response from site DB(table:EPS_LOG) for a given order number
     *
     * @param orderNumber order number to look up for EPS authorization response
     * @param paymentType mode of payment for the order (paypal/creditcard)
     * @return EPS authorization response in Element format
     */
    public Element getEpsAuthorizationResponse(String orderNumber, String paymentType) {
        Element epsAuthorizationResponse = null;
        String serviceName = null;
        switch (paymentType.toLowerCase()) {
            case "paypal":
                serviceName = "eps_paypal_authorization_response";
                break;
            case "creditcard":
            case "credit card":
                serviceName = "eps_authorization_response";
                break;
            default:
                throw new IllegalArgumentException("Unexpected payment type given: " + paymentType);
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service").getString(serviceName));
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                epsAuthorizationResponse = getXmlElements(resultSet.getString("XML_DATA"));
            }
        } catch (SQLException e) {
            logger.warn("Error in retrieving eps authorization response due to : " + e.getMessage());
        }
        return epsAuthorizationResponse;
    }

    /**
     * Method to get all the shipments of an order from site DB(TABLE:SHIPMENT) for a given order number
     *
     * @param orderNumber order number to get shipments
     * @return List of shipments in HashMap format
     **/
    public List<HashMap> getShipments(String orderNumber) {
        List<HashMap> shipments = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    queries.getJSONObject("order_service").getString("order_shipment_details"));
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                HashMap<String, String> shipment = new HashMap<>();
                shipment.put("SHIPMENT_ID", resultSet.getString("SHIPMENT_ID"));
                shipment.put("SHIP_METHOD_CODE", resultSet.getString("SHIP_METHOD_CODE"));
                shipment.put("SHIP_CONTACT_ID", resultSet.getString("SHIP_CONTACT_ID"));
                shipment.put("REGISTRY_ID", resultSet.getString("REGISTRY_ID"));
                shipment.put("REGISTRY_ADDR_USED_F", resultSet.getString("REGISTRY_ADDR_USED_F"));
                shipments.add(shipment);
            }
        } catch (SQLException e) {
            logger.warn("Error in retrieving shipments due to : " + e.getMessage());
        }
        return shipments;
    }

    /**
     * Method to get shipping method code from site DB(TABLE:Shipment) for a given order number
     *
     * @param orderNumber order number to get info for
     * @return shippingMethodCode
     **/
    public List<String> getShipMethodCode(String orderNumber) {
        List<String> shipMethod = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service").getString("order_shipment_details"));
            preparedStatement.setString(1, orderNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                shipMethod.add(resultSet.getString("SHIP_METHOD_CODE"));
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving ship method code due to : " + e.getMessage());
        }
        return shipMethod;
    }

    /**
     * Method to get prepareOrder request from site DB(TABLE:Orderlog) for a given order number
     *
     * @param orderNumber order number to get info for
     * @return prepareOrderReq in List Element format
     **/
    public List<Element> getPrepareOrderRequest(String orderNumber) {
        List<Element> prepareOrderReq = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    queries.getJSONObject("order_service").getString("prepare_order_details").replaceFirst("'\\?'", "'" + orderNumber + "'"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String xmlData = resultSet.getString("XML_DATA");
                Element prepareOrderXml = getXmlElements(xmlData);
                prepareOrderReq.add(prepareOrderXml);
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving prepare Order due to : " + e.getMessage());
        }
        return prepareOrderReq;
    }

    /**
     * Method to get status message from site DB(TABLE:order_confirmation_message) for a given order number
     *
     * @param orderNumber order number to get info for
     * @return status in String format
     */
    public String getOrderConfirmationMessage(String orderNumber) {
        String status = null;

        try {
            String query = queries.getJSONObject("order_service")
                    .getString("order_confirmation_message_status")
                    .replaceFirst("'\\?'", "'" + orderNumber + "'");
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                status = resultSet.getString("status");
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving order confirmation message status code due to : " + e.getMessage());
        }

        return status;
    }

    /**
     *
     */
    public Map<String, String> getBTSalescheck(String orderNumber) {
        HashMap<String, String> btSalescheck = new HashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service")
                    .getString("bt_salescheck").replaceFirst("'\\?'", "'" + orderNumber + "'"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                btSalescheck.put("BT_SALESCHECK_NBR", resultSet.getString("BT_SALESCHECK_NBR"));
                btSalescheck.put("DELIVERY_FEE", resultSet.getString("DELIVERY_FEE"));
                btSalescheck.put("TAX_AMT", resultSet.getString("TAX_AMT"));
                btSalescheck.put("TOTAL_AMT", resultSet.getString("TOTAL_AMT"));
                btSalescheck.put("RECYCLE_FEE", resultSet.getString("RECYCLE_FEE"));
                btSalescheck.put("BED_REMOVAL_FEE", resultSet.getString("BED_REMOVAL_FEE"));
                btSalescheck.put("WORRY_NO_MORE_FEE", resultSet.getString("WORRY_NO_MORE_FEE"));
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving big ticket sales check details due to : " + e.getMessage());
        }
        return btSalescheck;
    }

    /**
     *
     */
    public Map<String, String> getBTCreditcardPayment(String orderNumber) {
        HashMap<String, String> btSalescheck = new HashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service")
                    .getString("bt_credit_card_payment").replaceFirst("'\\?'", "'" + orderNumber + "'"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                btSalescheck.put("ORDER_NUMBER", resultSet.getString("ORDER_NUMBER"));
                btSalescheck.put("BT_SALESCHECK_NBR", resultSet.getString("BT_SALESCHECK_NBR"));
                btSalescheck.put("AMOUNT_CHARGED", resultSet.getString("AMOUNT_CHARGED"));
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving big ticket order credit card payment due to : " + e.getMessage());
        }
        return btSalescheck;
    }

    /**
     * Method to parse the XML into TagElements
     *
     * @param xmlData Data to look for
     * @return documentElement
     **/

    public Element getXmlElements(String xmlData) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(xmlData));
            Document doc = builder.parse(src);
            return doc.getDocumentElement();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets up a database connection
     */
    private void setupConnection() {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                logger.error("Error occurs while creating database connection : " + e.getMessage());
            }
        }
    }


}
