package com.macys.sdt.shared.resources.actions.website.mcom.panels.shop_and_browse;

import com.macys.sdt.framework.interactions.Elements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.macys.sdt.framework.utils.StepUtils.*;

public class ProductThumbnail {

    public static JSONArray getProductThumbnailDetails() throws JSONException {
        JSONArray product_thumbnail_list = new JSONArray();
        JSONObject product_thumbnail = new JSONObject();
        List<WebElement> thumbnails = Elements.findElements(Elements.element("product_thumbnails.thumbnail_wrapper"));

        for (WebElement thumbnail : thumbnails) {
            product_thumbnail.put("product_id", thumbnail.getAttribute("id"));
            product_thumbnail.put("product_name", thumbnail.findElement(By.className("shortDescription")).getText());
            product_thumbnail.put("product_prices", thumbnail.findElement(By.className("prices")).getText().split("\n"));
            product_thumbnail_list.put(product_thumbnail);
        }

        return product_thumbnail_list;
    }

    public static Map<String, HashMap> getProdutThumnailWebElements() {
        HashMap<String, WebElement> thumbnailWebElements = new HashMap<>();
        HashMap<String, HashMap> productThumbnails = new HashMap<>();
        boolean color_swatches;
        boolean price_events;
        boolean batch_text;
        boolean customer_ratings;
        List<WebElement> thumbnails = Elements.findElements(Elements.element("product_thumbnails.thumbnail_wrapper"));

        for (WebElement thumbnail : thumbnails) {
            try {
                color_swatches = thumbnail.findElement(By.className("morecolorswrapper")).isDisplayed();
            } catch (NoSuchElementException e) {
                System.out.println("Color swatches are not available for Product ID " + thumbnail.getAttribute("id"));
                color_swatches = false;
            }

            try {
                price_events = thumbnail.findElement(By.id("priceEventsDiv")).isDisplayed();
            } catch (NoSuchElementException e) {
                System.out.println("Price events are not available for Product ID " + thumbnail.getAttribute("id"));
                price_events = false;
            }

            try {
                batch_text = thumbnail.findElement(By.className("badgeHeader")).isDisplayed();
            } catch (NoSuchElementException e) {
                System.out.println("Batch Texts are not available for Product ID " + thumbnail.getAttribute("id"));
                batch_text = false;
            }

            try {
                customer_ratings = thumbnail.findElement(By.className("pdpreviews")).isDisplayed();
            } catch (NoSuchElementException e) {
                System.out.println("Customer Ratings not available for Product ID " + thumbnail.getAttribute("id"));
                customer_ratings = false;
            }

            thumbnailWebElements.put("elm_product_id", thumbnail);
            thumbnailWebElements.put("elm_image_link", thumbnail.findElement(By.className("imageLink")));
            thumbnailWebElements.put("elm_color_swatches", color_swatches ? thumbnail.findElement(By.className("morecolorswrapper")) : null);
            thumbnailWebElements.put("elm_product_name", thumbnail.findElement(By.className("shortDescription")));
            thumbnailWebElements.put("elm_price_event", price_events ? thumbnail.findElement(By.id("priceEventsDiv")) : null);
            thumbnailWebElements.put("elm_prices", thumbnail.findElement(By.className("prices")));
            thumbnailWebElements.put("elm_batch_text", batch_text ? thumbnail.findElement(By.className("badgeHeader")) : null);
            thumbnailWebElements.put("elm_customer_ratings", customer_ratings ? thumbnail.findElement(By.className("pdpreviews")) : null);
            productThumbnails.put(thumbnail.getAttribute("id"), thumbnailWebElements);
        }

        return productThumbnails;
    }

    public static int productThumbnailColumns() {
        // no choice on bcom, always 3
        if (bloomingdales())
            return 3;

        WebElement gridView = Elements.findElement(Elements.element("search_result.grid_view"));
        return gridView.getAttribute("class").contains("three") ? 3 : 4;
    }
}
