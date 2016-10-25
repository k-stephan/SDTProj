package com.macys.sdt.shared.resources.actions.website.mcom.panels.shop_and_browse;


import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.utils.StepUtils;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class RecentlyViewed extends StepUtils {

    private static List<String> recentlyViewed;

    public static boolean isDisplayed() {
        scrollToLazyLoadElement("recently_viewed_items.thumbnail_wrapper");
        recentlyViewed = Elements.findElements("recently_viewed_items.thumbnail_wrapper").stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        return Elements.elementPresent("recently_viewed_items.thumbnail_wrapper");
    }

    public static boolean isEditVisible() {
        return Elements.elementPresent("recently_viewed_items.edit_button");
    }

    public static List<String> getRecentlyViewed() {
        return Elements.findElements("recently_viewed_items.thumbnail_wrapper").stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public static List<String> getOldRecentlyViewed() {
        return recentlyViewed;
    }

    public static void updateProducts() {
        recentlyViewed = getRecentlyViewed();
    }
}
