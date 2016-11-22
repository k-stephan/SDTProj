package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.*;

/**
 * Created by M509108 on 11/21/2016.
 */
public class DBUtilsTest {

    @Test
    public void getCustomDate() throws Exception {
        try {
            Assert.assertNotNull("Date generated is null", DBUtils.getCustomDate());
        } catch (Exception e)   {
            Assert.fail("Failed due to : " + e.getMessage());
        }
    }

    @Test
    public void setupDBConnection() throws Exception {
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

    @Test
    public void getConfig() throws Exception {
        try {
            Assert.assertNotNull(new DBUtils().getConfig());
        } catch (Exception e)   {
            Assert.fail("Failed due to : " + e.getMessage());
        }
    }

}