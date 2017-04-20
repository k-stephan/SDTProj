package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static Statement statement;
    public static Connection connection;

    /**
     * Unlink all user which are linked to USL ID
     *
     * @param uslId usl id
     */
    public static void removeUslIdFromAllUsers(String uslId) {
        if (statement == null)
            setupConnection();
        JSONObject sqlQueries = Utils.getSqlQueries();
        String selectQuery = sqlQueries.getJSONObject("user_service").getString("get_user_loyalty_info_using_usl");
        String deleteQuery = sqlQueries.getJSONObject("user_service").getString("delete_user_loyalty_info_using_usl");
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, uslId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, uslId);
                preparedStatement.executeUpdate();
            }
        } catch (JSONException | SQLException e) {
            logger.error("Failed to delete usl info: " + e);
        }
    }

    /**
     * To setup DB connection
     */
    private static void setupConnection() {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                logger.error("Error occur while creating database connection : " + e.getMessage());
            }
        }
    }
}
