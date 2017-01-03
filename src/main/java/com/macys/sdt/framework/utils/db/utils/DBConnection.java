package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static Connection con = null;

    /**
     * This method close the database connection
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
                System.out.println("Connecting to database...");
                Assert.assertFalse("ERROR - ENV : Unable to fetch database details from REAPPS URL",
                        (config.getDBUrl() == null || config.getUserName() == null || config.getPassword() == null));
                con = DriverManager.getConnection(config.getDBUrl(), config.getUserName(), config.getPassword());
                System.out.println("Connection complete");
            } catch (Exception e) {
                System.out.println("Error occurred while creating database connection" + e.getMessage());
            }
        } else {
            System.out.println("INFO : Database connection already exists hence no new connection created");
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
                System.out.println("Connecting to database...");
                Assert.assertFalse("ERROR : valid value for database connection not given",
                        (dbUrl == null || username == null || password == null));
                con = DriverManager.getConnection(dbUrl, username, password);
                System.out.println("Connection complete");
            } catch (Exception e) {
                System.out.println("Error occurred while creating database connection" + e.getMessage());
            }
        } else {
            System.out.println("INFO : Database connection already exists hence no new connection created");
        }

        return con;
    }

}