package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.*;

/**
 * Created by M509108 on 11/21/2016.
 */
public class DBConfigTest {

   /* @Test
    public void generateDBUrl() throws Exception {
        try {
            DBUtils util = new DBUtils();
            DBConfig config = util.getConfig();

            Class.forName(config.getDriver());
            String str = "jdbc:db2://" + config.getHost() + ":" +  config.getPort() + "/" + config.getDbName();
            Assert.assertEquals(str, config.generateDBUrl());
        } catch (Exception e) {
            Assert.fail("fail due to : " + e.getMessage());
        }
    }*/

}