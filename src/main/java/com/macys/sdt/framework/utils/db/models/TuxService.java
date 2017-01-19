package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.db.utils.DBUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Collection of TUX related DB methods
 *
 */
public class TuxService {

    /**
     * Check existence of marketplace_id in marketplace_attrib table
     *
     * @param mkpReservationId marketplace reservation Id
     * @return true is marketplace_id exists in marketplace_attrib table, else false
     */
    public static Boolean isMkpReservationExists(String mkpReservationId) {
        Connection connection = DBUtils.setupDBConnection();
        String selectQuery = "select * from marketplace_attrib where marketplace_id=" + mkpReservationId;
        try {
            ResultSet resultSet = connection.prepareStatement(selectQuery).executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Failed to check mkpReservationId " + mkpReservationId + " existence in DB: " + e);
        }
        return false;
    }

    /**
     * Delete records of given marketplace_id from marketplace_attrib table and cart_item table
     *
     * @param mkpReservationId marketplace reservation Id
     */
    public static void deleteMkpReservationRecord(String mkpReservationId) {
        Connection connection = DBUtils.setupDBConnection();
        String deleteAttribute = "delete from marketplace_attrib where marketplace_id=" + mkpReservationId;
        String deleteCartItem = "delete from cart_item where marketplace_id=" + mkpReservationId;
        try {
            connection.prepareStatement(deleteAttribute).executeUpdate();
            connection.prepareStatement(deleteCartItem).executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete mkpReservation records for " + mkpReservationId +" from DB: " + e);
        }
    }
}
