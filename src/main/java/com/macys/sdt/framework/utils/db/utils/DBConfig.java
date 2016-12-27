package com.macys.sdt.framework.utils.db.utils;

public class DBConfig {

    private String envName;
    private String host;
    private String port;
    private String dbName;
    private String schema;
    private String userName;
    private String password;
    private String dbType;
    private String dbUrl;

    public DBConfig() {

    }

    public DBConfig(String envName, String host, String dbName, String schema) {
        this.envName = envName;
        this.host = host;
        this.dbName = dbName;
        this.schema = schema;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDriver() {
        return "com.ibm.db2.jcc.DB2Driver";
    }

    /**
     * get database driver path depending on database. If no match for database, db2 driver path returns
     *
     * @param databaseName name of database
     * @return database driver path
     */
    public String getDriver(String databaseName) {
        switch (databaseName) {
            case "postgresql":
                return "org.postgresql.Driver";
            default:
                return "com.ibm.db2.jcc.DB2Driver";
        }
    }

    public String getDBUrl() {
        if (dbUrl != null) {
            return dbUrl;
        } else {
            return generateDBUrl();
        }
    }

    public void setDBUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String generateDBUrl() {
        //jdbc:db2://11.168.114.41:60000/MLQ10
        StringBuffer sb = new StringBuffer();
        sb.append("jdbc:db2://");
        sb.append(getHost());
        sb.append(":");
        sb.append(getPort());
        sb.append("/");
        sb.append(getDbName());

        return sb.toString();
    }

}
