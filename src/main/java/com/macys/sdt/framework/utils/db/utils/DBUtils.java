package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.*;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;

public class DBUtils {

    private static final Logger logger = LoggerFactory.getLogger(DBUtils.class);

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
            logger.warn("not able to retrieve custom date due to : " + e.getMessage());
        }

        return customDate;
    }

    /**
     * Method to setup DB connection for db2 database of test environment based on REAPPS URL
     *
     * @return DB connection object
     */
    public static Connection setupDBConnection() {
        return new DBConnection().createConnection();
    }

    /**
     * This method setup database connection based on input given
     *
     * @param databaseName : name of database : postgresql, db2 (db2 is by default)
     * @param dbUrl : url to access database
     * @param username : username to access database
     * @param password : password to access database
     * @return database connection object
     */
    public static Connection setupDBConnection(String databaseName, String dbUrl, String username, String password) {
        return new DBConnection().createConnection(databaseName, dbUrl, username, password);
    }

    /**
     * Method to remove DB connection.
     * Close the db connection esp when switching between different database.
     */
    public static void closeDBConnection() {
        new DBConnection().closeConnection();
    }


    /**
     * Attempts to retrieve the database configuration from Odin
     *
     * @return DBConfig object with config details
     */
    public DBConfig getConfig() {

        String json;

        // This is for testing DBUtils and DBConnection
        if (RunConfig.booleanParam("dbUnitTest")) {
            json = RunConfig.getEnvVar("dbUnitTestConfig");
            if (json == null) {
                return null;
            }
            return createConfigFromJSON(new JSONObject(json));
        }

        return createConfigFromJSON(EnvironmentDetails.getServicesJson());

    }

    private DBConfig createConfigFromJSON(JSONObject json) {

        DBConfig dbconfig = new DBConfig();

        try {
            String eName;
            if (EnvironmentDetails.isZeus()) {
                return getDetailsFromZeus(json);
            }

            try {
                eName = json.getString("envName");
            } catch (JSONException e) {
                eName = "Unknown";
            }
            JSONObject dbInfo = (JSONObject) json.get("dbInfo");

            dbconfig.setPort(dbInfo.getString("dbPortNo"));
            dbconfig.setDBUrl(dbInfo.getString("dbUrl"));
            dbconfig.setUserName(dbInfo.getString("dbUsername"));
            dbconfig.setPassword(dbInfo.getString("dbPassword"));
            dbconfig.setHost(dbInfo.getString("dbHost"));
            dbconfig.setDbName((String) dbInfo.get("dbName"));
            dbconfig.setSchema(dbInfo.getString("dbSchema"));
            dbconfig.setEnvName(eName);
        } catch (ParseException | JSONException e) {
            logger.warn("error in create config from JSON due to : " + e.getMessage());
        }

        return dbconfig;
    }

    private DBConfig getDetailsFromZeus(JSONObject details) {
        JSONArray components = details.getJSONObject("component").getJSONArray("components");
        DBConfig config = new DBConfig();

        JSONObject obj = Utils.findObjectWithProperty(components, "name", "sitedb");
        JSONArray offerings = obj == null ? null : obj.getJSONObject("ie").getJSONArray("offerings");

        if (offerings != null) {
            JSONObject siteDB = Utils.findObjectWithProperty(offerings, "name", "sitedb");
            if (siteDB != null) {
                config.setPort(siteDB.getString("port"));
                config.setUserName(siteDB.getString("username"));
                config.setPassword(siteDB.getString("password"));
                config.setHost(siteDB.getString("host"));
                config.setDbName(siteDB.getString("dbname"));
                config.setSchema(siteDB.getString("schema"));
                config.setHost(EnvironmentDetails.getEnv(EnvironmentDetails.getEnvUrl()));
            }
        }
        return config;
    }

}