package com.macys.sdt.shared.resources.actions.website.mcom.pages.shop_and_browse;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CategorySplash {

    public static By selectFeaturedCategory(String category) {
        return By.xpath("//img[@alt='" + category + "']");
    }

    public static void selectCustomerTopRatedProduct() {
        WebElement itemElement = Elements.findElement(Elements.element("category_splash.splash_reviews")).findElements(By.id("splashReview")).get(0);
        Clicks.click(itemElement.findElement(By.id("reviewProductImage")));
    }
}

