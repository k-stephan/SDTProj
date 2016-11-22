package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static Connection con = null;

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

    public Connection createConnection() {

        if (con == null) {
            DBUtils util = new DBUtils();
            DBConfig config = util.getConfig();

            try {
                Class.forName(config.getDriver());
                System.out.println("Connecting to database...");
                Assert.assertFalse("ERROR - ENV : Unable to fetch database details from REAPPS URL", (config.getDBUrl() == null || config.getUserName() == null || config.getPassword() == null));
                con = DriverManager.getConnection(config.getDBUrl(), config.getUserName(), config.getPassword());
                System.out.println("Connection complete");
            } catch (Exception e) {
                System.out.println("Error occure while craeting database connection" + e.getMessage());
            }
        }

        return con;

    }

}