package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Collection of TUX related DB methods
 *
 */
public class TuxService {

    private static final Logger logger = LoggerFactory.getLogger(TuxService.class);

    /**
     * Check existence of marketplace_id in marketplace_attrib table
     *
     * @param mkpReservationId marketplace reservation Id
     * @return true is marketplace_id exists in marketplace_attrib table, else false
     */
    public static Boolean isMkpReservationExists(String mkpReservationId) {
        Connection connection = DBUtils.setupDBConnection();
        String selectQuery = String.format("select * from marketplace_attrib where marketplace_id=%s", mkpReservationId);
        try {
            ResultSet resultSet = connection.prepareStatement(selectQuery).executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error("Failed to check mkpReservationId " + mkpReservationId + " existence in DB: " + e.getMessage());
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
        String deleteAttribute = String.format("delete from marketplace_attrib where marketplace_id=%s", mkpReservationId);
        String deleteCartItem = String.format("delete from cart_item where marketplace_id=%s", mkpReservationId);
        try {
            connection.prepareStatement(deleteAttribute).executeUpdate();
            connection.prepareStatement(deleteCartItem).executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to delete mkpReservation records for " + mkpReservationId +" from DB: " + e.getMessage());
        }
    }
}
