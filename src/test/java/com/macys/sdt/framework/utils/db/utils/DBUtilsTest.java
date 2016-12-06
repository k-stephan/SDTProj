package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.SQLException;


public class DBUtilsTest {

    @BeforeClass
    public static void setup() {
        MainRunner.url = "http://mcom-bops16s.c4d.devops.fds.com/";
        MainRunner.browser = "firefox";
    }

    //@Test
    public void getCustomDate() {
        try {
            Assert.assertNotNull("Date generated is null", DBUtils.getCustomDate());
        } catch (Exception e)   {
            Assert.fail("Failed due to : " + e.getMessage());
        }
    }

    //@Test
    public void setupDBConnection() throws SQLException {
        Connection con = null;
        try {
            con = DBUtils.setupDBConnection();
            Assert.assertNotNull(con);
            Assert.assertFalse(con.isClosed());
        } catch (Exception e) {
            Assert.fail("Failed due to : " + e.getMessage());
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }

    //@Test
    public void getConfig() {
        try {
            Assert.assertNotNull(new DBUtils().getConfig());
        } catch (Exception e)   {
            Assert.fail("Failed due to : " + e.getMessage());
        }
    }

}