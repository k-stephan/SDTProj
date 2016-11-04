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

            String eName = (String) jsonObject.get("envName");
            JSONObject dbInfo = (JSONObject) jsonObject.get("dbInfo");
            String dbName = (String) dbInfo.get("dbName");
            String dbSchema = (String) dbInfo.get("dbSchema");
            String dbUsername = (String) dbInfo.get("dbUsername");
            String dbPassword = (String) dbInfo.get("dbPassword");
            String dbPortNo = (String) dbInfo.get("dbPortNo");
            String dbHost = (String) dbInfo.get("dbHost");
            String dbUrl = (String) dbInfo.get("dbUrl");

            dbconfig.setPort(dbPortNo);
            dbconfig.setDBUrl(dbUrl);
            dbconfig.setUserName(dbUsername);
            dbconfig.setPassword(dbPassword);
            dbconfig.setHost(dbHost);
            dbconfig.setDbName(dbName);
            dbconfig.setSchema(dbSchema);
            dbconfig.setEnvName(eName);
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }

        return dbconfig;

    }

}