package com.macys.sdt.shared.actions.MEW.panels;


import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.utils.StepUtils;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class RecentlyViewed extends StepUtils {

    private static List<String> recentlyViewed;

    /**
     * Checks if recently viewed products are displayed and store the displayed products in recentlyViewed variable
     *
     * @return true if recently viewed products are displayed else false
     */
    public static boolean isDisplayed() {
        scrollToLazyLoadElement("recently_viewed_items.thumbnail_wrapper");
        recentlyViewed = Elements.findElements("recently_viewed_items.thumbnail_wrapper").stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        return Elements.elementPresent("recently_viewed_items.thumbnail_wrapper");
    }

    /**
     * Checks if "Edit" button is present on recently viewed panel
     *
     * @return true if "Edit" button is present on recently viewed panel else false
     */
    public static boolean isEditVisible() {
        return Elements.elementPresent("recently_viewed_items.edit_button");
    }

    /**
     * Gets the list of product from recently viewed panel
     *
     * @return list of product from recently viewed panel
     */
    public static List<String> getRecentlyViewed() {
        return Elements.findElements("b-product-recently-viewed-img-wrapper").stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of product stored in recentlyViewed variable
     *
     * @return list of product stored in recentlyViewed variable
     */
    public static List<String> getOldRecentlyViewed() {
        return recentlyViewed;
    }

    /**
     * Updates the stored list of product from recently viewed panel
     */
    public static void updateProducts() {
        recentlyViewed = getRecentlyViewed();
    }
}
