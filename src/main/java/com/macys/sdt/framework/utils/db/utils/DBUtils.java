package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.utils.Utils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.Date;

public class DBUtils {

    public DBUtils() {

    }

    /**
     * Returns the custom date and if the custom date is empty it will return current date
     *
     * @return custom date
     */
    public static Date getCustomDate() {

        Date customDate = null;
        Connection con = setupDBConnection();

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(Utils.getSqlQueries().get("custom_date").toString());
            if (rs.next()) {
                customDate = rs.getTimestamp("timestamp_value");
            }
            Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
            customDate = customDate == null ? currentTimeStamp : customDate;
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }

        return customDate;

    }

    /**
     * Method to setup DB connection
     *
     * @return DB connection object
     */
    public static Connection setupDBConnection() {
        DBConnection dbConnection = new DBConnection();
        return dbConnection.createConnection();
    }

    /**
     * Attempts to retrieve the database configuration from Odin
     *
     * @return DBConfig object with config details
     */
    public DBConfig getConfig() {

        String json = null;

        try {
            json = EnvironmentDetails.getJSONString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return createConfigFromJSON(json);

    }

    private DBConfig createConfigFromJSON(String json) {

        DBConfig dbconfig = new DBConfig();

        try {
            JSONObject jsonObject = new JSONObject(json);
            String eName;
            try {
                eName = jsonObject.getString("envName");
            } catch (JSONException e) {
                eName = "Unknown";
            }
            JSONObject dbInfo = (JSONObject) jsonObject.get("dbInfo");

            dbconfig.setPort(dbInfo.getString("dbPortNo"));
            dbconfig.setDBUrl(dbInfo.getString("dbUrl"));
            dbconfig.setUserName(dbInfo.getString("dbUsername"));
            dbconfig.setPassword(dbInfo.getString("dbPassword"));
            dbconfig.setHost(dbInfo.getString("dbHost"));
            dbconfig.setDbName((String) dbInfo.get("dbName"));
            dbconfig.setSchema(dbInfo.getString("dbSchema"));
            dbconfig.setEnvName(eName);
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }

        return dbconfig;

    }

}