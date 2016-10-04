package com.macys.sdt.framework.utils.db.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static Connection con = null;

    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
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
                con = DriverManager.getConnection(config.getDBUrl(), config.getUserName(), config.getPassword());
                System.out.println("Connection complete");
            } catch (Exception e) {
                System.out.println("Error occure while craeting database connection" + e.getMessage());
            }
        }

        return con;

    }

}