package com.macys.sdt.framework.model;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ProductTest {

    @Test
    public void testProduct() throws Exception {
        String productName = "Some Product Name";
        int productId = 987654;
        int productQty = 9;

        JSONObject productData = new JSONObject();
        productData.put("name", productName);
        productData.put("id", productId);
        productData.put("quantity", productQty);
        productData.put("registry_item", true);
        productData.put("available", true);
        productData.put("master_available", false);
        productData.put("bops_available", true);
        productData.put("prod_available", true);
        productData.put("big_ticket", true);
        productData.put("fit_predictor_eligible", true);
        productData.put("beauty_item", false);
        productData.put("gift_card", false);
        productData.put("electronic_gift_card", false);
        productData.put("master_product", false);
        productData.put("iship_eligible", true);
        productData.put("click_to_call", false);
        productData.put("available_in_store", true);
        productData.put("available_sdd", true);
        productData.put("with_color_swatch", false);
        productData.put("eligible_sdd", true);
        productData.put("product_warranty", false);
        productData.put("gift_wrappable", true);
        productData.put("gift_messageable", true);
        productData.put("available_bops", false);
        productData.put("invalidField", "value");

        Product product = new Product(productData);
        Assert.assertNotNull(product);
        Assert.assertEquals(productName, product.name);
        Assert.assertEquals(productId, product.id);
        Assert.assertEquals(productQty, product.quantity);
        Assert.assertTrue(product.registryItem);
        Assert.assertTrue(product.available);
        Assert.assertFalse(product.masterAvailable);
        Assert.assertTrue(product.bopsEligible);
        Assert.assertFalse(product.bopsAvailable);
        Assert.assertTrue(product.prodAvailable);
        Assert.assertTrue(product.bigTicket);
        Assert.assertTrue(product.fitPredictorEligible);
        Assert.assertFalse(product.beautyItem);
        Assert.assertFalse(product.giftCard);
        Assert.assertFalse(product.electronicGiftCard);
        Assert.assertFalse(product.masterProduct);
        Assert.assertTrue(product.ishipEligible);
        Assert.assertFalse(product.clickToCall);
        Assert.assertTrue(product.availableInStore);
        Assert.assertTrue(product.sddEligible);
        Assert.assertFalse(product.hasColor);
        Assert.assertTrue(product.sddAvailable);
        Assert.assertFalse(product.hasWarranty);
        Assert.assertTrue(product.giftWrappable);
        Assert.assertTrue(product.giftMessageable);

        int anotherProductId = 12345;
        Product anotherProduct = new Product(anotherProductId);
        Assert.assertNotNull(anotherProduct);
        Assert.assertEquals(anotherProductId, anotherProduct.id);
    }
}