package com.macys.sdt.framework.resources.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * This class represents a product and contains all the information about that product
 */
public class Product {
    public String name, currency, description, categoryName, categoryPageType, colorName,
            sizeName, colorNormalName, orderMethod, brand, type, department;
    public String[] primaryImageColors, colorOptions, sizeOptions, promoDescriptions, quantities;
    public int id, quantity, orderLimit, categoryId, canvasId, storeLocationNum;
    public int[] upcs, memberProducts;
    public long upc;
    public Product[] productRecommendations;
    public double individualPrice, salePrice, totalPrice;
    public double[] promoPrices;
    public boolean registryItem, normalItem, parentProductId, available, orderable, masterAvailable, bopsAvailable,
            prodAvailable, bopsEligible, bigTicket, fitPredictorEligible, categoryBreadcrumb, beautyItem, giftCard,
            electronicGiftCard, masterProduct, ishipEligible, clickToCall, availableInStore, sddEligible, hasColor,
            sddAvailable, hasWarranty, giftWrappable, giftMessageable;
    // attributes with unknown types:
    // product_images, color_swatch_images, promotions, default_image

    public Product(int id) {
        this.id = id;
    }

    public Product(JSONObject product) {
        Iterator keys = product.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            String varName = translate(key);
            try {
                Field f = Product.class.getField(varName);
                f.set(this, product.get(key));
            } catch (NoSuchFieldException e) {
                System.out.println("No field for " + key + " in Product class");
            } catch (JSONException | IllegalAccessException e) {
                System.err.println("Failed to set product property " + key + ": " + e);
            }
        }
    }

    private String translate(String attr) {
        switch (attr) {
            case "category_name":
                return "categoryName";
            case "category_page_type":
                return "categoryPageType";
            case "promo_description":
                return "promoDescription";
            case "color_normal_name":
                return "colorNormalName";
            case "order_method":
                return "orderMethod";
            case "with_brand":
                return "brand";
            case "product_type":
                return "type";
            case "order_limit":
                return "orderLimit";
            case "category_id":
                return "categoryId";
            case "canvas_id":
                return "canvasId";
            case "store_location_nbr":
                return "storeLocationNum";
            case "individual_price":
                return "individualPrice";
            case "sale_price":
                return "salePrice";
            case "promo_price":
                return "promoPrice";
            case "total_price":
            case "price":
                return "totalPrice";
            case "registry_item":
            case "registrable":
                return "registryItem";
            case "parent_product_id":
                return "parentProductId";
            case "master_available":
                return "masterAvailable";
            case "available_bops":
                return "bopsAvailable";
            case "prod_available":
                return "prodAvailable";
            case "eligible_bops":
            case "bops_available":
                return "bopsEligible";
            case "big_ticket":
                return "bigTicket";
            case "fit_predictor_eligible":
                return "fitPredictorEligible";
            case "category_breadcrumb":
                return "categoryBreadcrumb";
            case "beauty_item":
                return "beautyItem";
            case "gift_card":
                return "giftCard";
            case "electronic_gift_card":
                return "electronicGiftCard";
            case "iship_eligible":
                return "ishipEligible";
            case "click_to_call":
                return "clickToCall";
            case "available_in_store":
                return "availableInStore";
            case "available_sdd":
                return "sddAvailable";
            case "eligible_sdd":
                return "sddEligible";
            case "product_warranty":
                return "hasWarranty";
            case "gift_wrappable":
                return "giftWrappable";
            case "gift_messageable":
                return "giftMessageable";
            case "with_color_swatch":
            case "with_color":
                return "hasColor";
            case "master_product":
                return "masterProduct";
            default:
                return attr;
        }
    }
}
