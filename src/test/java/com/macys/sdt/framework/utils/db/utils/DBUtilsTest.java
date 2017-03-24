package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Tests for DBUtils
 */
public class DBUtilsTest {

    @BeforeClass
    public static void setup() {
        RunConfig.url = "http://www.qa0codemacys.fds.com";
        RunConfig.project = "framework";
        RunConfig.projectDir = "src/test/java/com/macys/sdt/framework";
        HashMap<String, String> env = new HashMap<>();
        env.put("dbUnitTest", "true");
        env.put("dbUnitTestConfig", "{\"dbInfo\":{\"dbName\":\"mcyprdst\",\"dbSchema\":\"DB2MCYS\",\"dbUsername\":\"mcyapp\",\"dbPassword\":\"dbacce5s\",\"dbPortNo\":\"60020\",\"dbHost\":\"ibm80p49\",\"dbUrl\":\"jdbc:db2://ibm80p49:60020/MCYPRDST\"}}");
        TestUtils.setEnv(env);
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
