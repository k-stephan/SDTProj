package com.macys.sdt.shared.utils.db.models;

import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.sql.*;
import java.util.*;

public class OrderServices {
    public Statement statement;
    public Connection connection;
    public JSONObject queries;

    /**
     * Method to get orderDetails from site DB(table:ORDER) for a given order number
     * @return orderDetails in hashMap format
     **/
    public HashMap getOrderDetails(String orderNumber) throws Throwable
    {
        setupConnection();
        HashMap<String, String> orderDetails = new HashMap<>();
        queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service").getString("order_details").toString().replaceFirst("'\\?'", "'" + orderNumber + "'"));
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    /**
     * Method to get shipping method code from site DB(TABLE:Shipment) for a given order number
     * @return shippingMethodCode
     **/
    public List getShipMethodCode(String orderNumber) throws Throwable {
        setupConnection();
        List shipMethod = new ArrayList();
        queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service").getString("order_shipment_details").toString().replaceFirst("'\\?'", "'" + orderNumber + "'"));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                shipMethod.add(resultSet.getString("SHIP_METHOD_CODE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shipMethod;
    }

    /**
     * Method to get prepareOrder request from site DB(TABLE:Orderlog) for a given order number
     * @return prepareOrderReq in List Element format
     **/
    public List<Element> getPrepareOrderRequest(String orderNumber) throws Throwable {
        setupConnection();
        List<Element> preapreOrderReq = new ArrayList();
        queries = Utils.getSqlQueries();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queries.getJSONObject("order_service").getString("prepare_order_details").toString().replaceFirst("'\\?'", "'" + orderNumber + "'"));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String xmlData = resultSet.getString("XML_DATA");
                Element prepareOrderXml = getXmlElements(xmlData);
                preapreOrderReq.add(prepareOrderXml);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preapreOrderReq ;
    }

    /**
     * Method to parse the XML into TagElements
     *
     * @return documentElement
     **/

    public Element getXmlElements(String xmlData) throws Exception
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource src = new InputSource();
        src.setCharacterStream(new StringReader(xmlData));
        Document doc = builder.parse(src);
        return doc.getDocumentElement();
    }

    /*
        To setup DB connection
     */
    private void setupConnection() {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                System.out.println("Error occurs while creating database connection" + e.getMessage());
            }
        }
    }


}

