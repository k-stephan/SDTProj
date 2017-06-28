package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Tests for DBUtils
 */
public class DBUtilsTest {

    @BeforeClass
    public static void setup() {
        DBConnectionTest.setup();
    }

    @Test
    public void testGetCustomDate() {
        try {
            Assert.assertNotNull("Date generated is null", DBUtils.getCustomDate());
        } catch (Exception e) {
            Assert.fail("Failed due to : " + e.getMessage());
        }
    }

    @Test
    public void testSetupDBConnection() throws SQLException {
        Connection con = null;
        try {
            con = DBUtils.setupDBConnection();
            Assert.assertNotNull(con);
            Assert.assertFalse(con.isClosed());
            new DBConnection().closeConnection();
        } catch (Exception e) {
            Assert.fail("Failed due to : " + e.getMessage());
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }

    @Test
    public void testGetConfig() {
        try {
            Assert.assertNotNull(new DBUtils().getConfig());
        } catch (Exception e) {
            Assert.fail("Failed due to : " + e.getMessage());
        }
    }

}
