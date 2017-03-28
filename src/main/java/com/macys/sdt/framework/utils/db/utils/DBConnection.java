package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    private static Connection con = null;

    /**
     * This method close the database connection.
     * Close the db connection esp when switching between different database.
     */
    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                con = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method create connection for db2 environment database based on REAPPS URL
     *
     * @return database connection to a test environment
     */
    public Connection createConnection() {

        if (con == null) {
            DBUtils util = new DBUtils();
            DBConfig config = util.getConfig();

            try {
                Class.forName(config.getDriver(null));
                logger.info("Connecting to database...");
                Assert.assertFalse("ERROR - ENV : Unable to fetch database details from REAPPS URL",
                        (config.getDBUrl() == null || config.getUserName() == null || config.getPassword() == null));
                con = DriverManager.getConnection(config.getDBUrl(), config.getUserName(), config.getPassword());
                logger.info("Database connection successful");
            } catch (Exception e) {
                logger.error("Error occurred while creating database connection " + e.getMessage());
            }
        } else {
            logger.info("Database connection already exists hence no new connection created");
        }

        return con;
    }

    /**
     * This method create connection for database based on inputs given
     *
     * @param databaseName : name of database : postgresql, db2 (db2 is by default)
     * @param dbUrl : url to access database
     * @param username : username to access database
     * @param password : password to access database
     * @return database connection
     */
    public Connection createConnection(String databaseName, String dbUrl, String username, String password) {

        if (con == null) {
            try {
                Class.forName(new DBConfig().getDriver(databaseName));
                logger.info("Connecting to database...");
                Assert.assertFalse("ERROR - DATA : valid value for database connection not given",
                        (dbUrl == null || username == null || password == null));
                con = DriverManager.getConnection(dbUrl, username, password);
                logger.info("Database connection successful");
            } catch (Exception e) {
                logger.error("Error occurred while creating database connection " + e.getMessage());
            }
        } else {
            logger.info("Database connection already exists hence no new connection created");
        }

        return con;
    }

}