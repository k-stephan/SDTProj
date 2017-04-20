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

    /**
     * Method to get orderDetails from site DB(table:ORDER) for a given order number
     *
     * @param orderNumber order number to look up
     * @return orderDetails in hashMap format
     **/
    public HashMap getOrderDetails(String orderNumber) {
        setupConnection();
        HashMap<String, String> orderDetails = new HashMap<>();
        queries = Utils.getSqlQueries();
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
                orderDetails.put("BASE_FEE", resultSet.getString("ADJUSTED_BASE_FEE"));
                orderDetails.put("RESERVATION_STATUS", resultSet.getString("RESERVATION_STATUS"));
            }
        } catch (SQLException | JSONException e) {
            logger.warn("Error in retrieving order details due to : " + e.getMessage());
        }
        return orderDetails;
    }

    /**
     * Method to get shipping method code from site DB(TABLE:Shipment) for a given order number
     *
     * @param orderNumber order number to get info for
     * @return shippingMethodCode
     **/
    public List<String> getShipMethodCode(String orderNumber) {
        setupConnection();
        List<String> shipMethod = new ArrayList<>();
        queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    queries.getJSONObject("order_service").getString("order_shipment_details").replaceFirst("'\\?'", "'" + orderNumber + "'"));
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
        setupConnection();
        List<Element> prepareOrderReq = new ArrayList<>();
        queries = Utils.getSqlQueries();
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
        return prepareOrderReq ;
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

