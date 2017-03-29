package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.sql.Connection;
import java.util.HashMap;

/**
 * Tests for DBConnection
 */
public class DBConnectionTest {

    @BeforeClass
    public static void setup() {
        RunConfig.url = "http://www.qa0codemacys.fds.com";
        RunConfig.project = "framework";
        RunConfig.projectResourceDir = "src/test/java/com/macys/sdt/framework";
        HashMap<String, String> env = new HashMap<>();
        env.put("dbUnitTest", "true");
        env.put("dbUnitTestConfig", "{\"dbInfo\":{\"dbName\":\"mcyprdst\",\"dbSchema\":\"DB2MCYS\",\"dbUsername\":\"mcyapp\",\"dbPassword\":\"dbacce5s\",\"dbPortNo\":\"60020\",\"dbHost\":\"ibm80p49\",\"dbUrl\":\"jdbc:db2://ibm80p49:60020/MCYPRDST\"}}");
        TestUtils.setEnv(env);
    }

   @Test
    public void testCreateAndCloseConnection() throws Exception {
        DBConnection dbConnection;
        Connection con = null;
        try {
            dbConnection = new DBConnection();
            con = dbConnection.createConnection();
            Assert.assertFalse("connection is not open", con.isClosed());
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

}