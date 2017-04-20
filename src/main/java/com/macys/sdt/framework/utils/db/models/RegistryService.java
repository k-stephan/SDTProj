package com.macys.sdt.framework.utils.db.models;


import com.macys.sdt.framework.utils.db.utils.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    public static boolean registryExists(String id) {

        boolean exists = false;

        try {
            DBConnection dbConnection = new DBConnection();
            Connection con = dbConnection.createConnection();

            Statement st = con.createStatement();
            String sqlQuery = String.format("Select * from user_registry_info where registry_id = %s", id);
            ResultSet rs = st.executeQuery(sqlQuery);

            if (rs.next()) {
                exists = true;
                logger.info("*******The registry  is successfully created in DB for USER with registry id  " + rs.getString("REGISTRY_ID"));
            }
        } catch (SQLException e) {
            logger.error("error in retrieving registry : " + e.getMessage());
        }

        return exists;
    }

}