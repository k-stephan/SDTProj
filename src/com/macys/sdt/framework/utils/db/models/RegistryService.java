package com.macys.sdt.framework.utils.db.models;


import com.macys.sdt.framework.utils.db.utils.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegistryService {

    public static boolean registryExists(String id) {

        boolean exists = false;

        try {
            DBConnection dbConnection = new DBConnection();
            Connection con = dbConnection.createConnection();

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("Select * from user_registry_info where registry_id = " + id);

            if (rs.next()) {
                exists = true;
                System.out.println("*******The registry  is successfully created in DB for USER with registry id  " + rs.getString("REGISTRY_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;

    }

}