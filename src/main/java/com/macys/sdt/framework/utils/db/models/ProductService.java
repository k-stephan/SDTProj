package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductService extends StepUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    /**
     * Method to get department ids for the products
     *
     * @param productIds to get department id
     * @return department id for the product
     */
    public static List<Map<String, String>> getDepartmentIdForProduct(String[] productIds) {
        List<Map<String, String>> productDepartment = new ArrayList<>();
        try {
            JSONObject productQueries = (JSONObject) Utils.getSqlQueries().get("product");
            String sqlQuery = productQueries.get("product_department").toString();
            Connection con = DBUtils.setupDBConnection();
            StringBuilder builder = new StringBuilder();

            for (String productId : productIds) {
                builder.append("?,");
            }
            sqlQuery = sqlQuery.replace("?", builder.deleteCharAt(builder.length() - 1).toString());

            PreparedStatement preparedStatement = con.prepareStatement(sqlQuery);
            int index = 1;
            for (String productId : productIds) {
                preparedStatement.setInt(index++, Integer.parseInt(productId));
            }
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Map<String, String> prdDept = new HashMap<>();

                prdDept.put("productId", rs.getString("product_id"));
                prdDept.put("departmentId", rs.getString("department_id"));

                productDepartment.add(prdDept);
            }
            return productDepartment;
        } catch (SQLException | JSONException e) {
            logger.error("Unable to get department id for product(s) due to : " + e.getMessage());
        }
        return null;
    }

}
