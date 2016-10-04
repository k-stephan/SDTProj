package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONException;
import org.junit.Assert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class PromotionService extends StepUtils {

    public Statement statement;
    public Date customDate;

    /**
     * To get wallet eligible promo code from database
     *
     * @return null returns if wallet eligible promo code not available in DB.
     */
    public String getWalletEligiblePromoCode() {
        String walletEligiblePromoCode = null;
        setupConnection();
        if (customDate == null) {
            customDate = DBUtils.getCustomDate();
        }
        try {
            String walletPromoQuery = Utils.getSqlQueries().get("wallet_promo").toString().replaceAll(">= \\?", ">= '" + customDate.toString() + "'");
            ResultSet resultSet = statement.executeQuery(walletPromoQuery);
            while (resultSet.next()) {
                if (!resultSet.getString("PROMOTION_CODE").contains("_") && !resultSet.getString("PROMOTION_CODE").contains("SHINE")) {
                    walletEligiblePromoCode = resultSet.getString("PROMOTION_CODE");
                    break;
                }
            }
        } catch (SQLException | JSONException e) {
            Assert.fail("Unable to add promo code to wallet: " + e);
        }
        return walletEligiblePromoCode;
    }

    /**
     * To get promo code eligible products from database
     *
     * @return null returns if promo code eligible products not available in DB.
     */
    public String[] getPromoCodeForPromotionalProducts() {
        String[] promotionalArray = null;
        setupConnection();
        if (customDate == null) {
            customDate = DBUtils.getCustomDate();
        }
        try {
            String promotionalPromoCodeQuery = Utils.getSqlQueries().get("promotional_products_with_promo_code").toString().replaceAll(">= \\?", ">= '" + customDate.toString() + "'").replaceAll("<= \\?", "<= '" + customDate.toString() + "'");
            ResultSet resultSet = statement.executeQuery(promotionalPromoCodeQuery);
            while (resultSet.next()) {
                if (!resultSet.getString("PROMOTION_CODE").contains("_")) {
                    String promotionalPromoCode = resultSet.getString("PROMOTION_CODE");
                    String promotionalProduct = resultSet.getString("PROD_ID");
                    promotionalArray = new String[]{promotionalPromoCode, promotionalProduct};
                    break;
                }
            }
        } catch (SQLException | JSONException e) {
            Assert.fail("Unable to add promo code to wallet: " + e);
        }
        return promotionalArray;
    }

    /**
     * Initializes the DB connection
     **/
    private void setupConnection() {
        if (statement == null) {
            try {
                Connection connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                System.out.println("Error occurs while creating database connection" + e.getMessage());
            }
        }
    }
}
