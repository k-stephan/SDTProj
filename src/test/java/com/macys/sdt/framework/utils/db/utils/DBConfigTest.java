package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.Test;

public class DBConfigTest {

    private DBConfig dbConfig;

    public DBConfigTest() {
        dbConfig = new DBConfig("qa0codemacys", "11.168.114.93", "BLX01", "db2Schema");
    }

    @Test
    public void testGetHost() throws Exception {
        String host = "11.168.112.153";
        dbConfig.setHost(host);
        Assert.assertEquals(host, dbConfig.getHost());
    }

    @Test
    public void testGetPort() throws Exception {
        String port = "60000";
        dbConfig.setPort(port);
        Assert.assertEquals(port, dbConfig.getPort());
    }

    @Test
    public void testGetDbName() throws Exception {
        String dbName = "MLQ10";
        dbConfig.setDbName(dbName);
        Assert.assertEquals(dbName, dbConfig.getDbName());
    }

    @Test
    public void testGetUserName() throws Exception {
        String userName = "mcyapp";
        dbConfig.setUserName(userName);
        Assert.assertEquals(userName, dbConfig.getUserName());
    }

    @Test
    public void testGetPassword() throws Exception {
        String password = "dbacce5s";
        dbConfig.setPassword(password);
        Assert.assertEquals(password, dbConfig.getPassword());
    }

    @Test
    public void testGetEnvName() throws Exception {
        String envName = "qa0bloomingdales";
        dbConfig.setEnvName(envName);
        Assert.assertEquals(envName, dbConfig.getEnvName());
    }

    @Test
    public void testGetDBUrl() throws Exception {
        dbConfig.setPort("60020");
        dbConfig.setDBUrl(null);
        Assert.assertEquals("jdbc:db2://11.168.114.93:60020/BLX01", dbConfig.getDBUrl());
        String dBUrl = "jdbc:db2://11.168.112.101:60000/dbName";
        dbConfig.setDBUrl(dBUrl);
        Assert.assertEquals(dBUrl, dbConfig.getDBUrl());
    }

    @Test
    public void testGetSchema() throws Exception {
        String schema = "dbSchema";
        dbConfig.setSchema(schema);
        Assert.assertEquals(schema, dbConfig.getSchema());
    }

    @Test
    public void testGetDbType() throws Exception {
        String dbType = "db2";
        dbConfig.setDbType(dbType);
        Assert.assertEquals(dbType, dbConfig.getDbType());
    }

    @Test
    public void testGenerateDBUrl() throws Exception {
        String dbName = "MLQ10";
        String host = "11.168.112.153";
        String port = "60000";

        DBConfig config = new DBConfig();
        config.setDbName(dbName);
        config.setHost(host);
        config.setPort(port);

        Class.forName(config.getDriver());
        String str = "jdbc:db2://" + host + ":" + port + "/" + dbName;
        Assert.assertEquals(str, config.generateDBUrl());
    }
}