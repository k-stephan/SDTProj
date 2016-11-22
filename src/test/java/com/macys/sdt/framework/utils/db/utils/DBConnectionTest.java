package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Created by M509108 on 11/21/2016.
 */
public class DBConnectionTest {

   /* @Test
    public void createConnection() throws Exception {
        DBConnection dbConnection;
        Connection con = null;
        try {
            dbConnection = new DBConnection();
            con = dbConnection.createConnection();
            Assert.assertFalse("connection is not open", con.isClosed());
        } catch (Exception e) {
            Assert.fail("Failed due to : " + e.getMessage());
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }*/

   /* @Test
    public void closeConnection() throws Exception {
        DBConnection dbConnection;
        Connection con = null;
        try {
            dbConnection = new DBConnection();
            con = dbConnection.createConnection();
            dbConnection.closeConnection();
            Assert.assertTrue("connection is not closed", con.isClosed());
        } catch (Exception e) {
            Assert.fail("Failed due to : " + e.getMessage());
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }
*/


}