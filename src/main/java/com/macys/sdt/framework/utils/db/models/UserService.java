package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mpamujula on 19-12-2016.
 */
public class UserService {

    public static Statement statement;
    public static Connection connection;

    /**
     * Unlink all user which are linked to USL ID
     *
     * @param  uslId usl id
     */
    public static void removeUslIdFromAllUsers(String uslId){
        if (statement == null)
            setupConnection();
        JSONObject sqlQueries = Utils.getSqlQueries();
        String selectQuery = sqlQueries.getJSONObject("user_service").getString("get_user_loyalty_info_using_usl");
        String deleteQuery = sqlQueries.getJSONObject("user_service").getString("delete_user_loyalty_info_using_usl");
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, uslId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, uslId);
                preparedStatement.executeUpdate();
            }
        } catch (JSONException | SQLException e) {
            System.err.println("Failed to delete usl info: " + e);
        }
    }

    /*
        To setup DB connection
     */
    private static void setupConnection() {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                System.out.println("Error occure while craeting database connection" + e.getMessage());
            }
        }
    }
}
