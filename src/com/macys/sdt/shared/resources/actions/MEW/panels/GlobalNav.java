package com.macys.sdt.shared.resources.actions.MEW.panels;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class GlobalNav extends StepUtils {

    public static void openGlobalNav() {
        if (onPage("registry_home", "registry_manager")) {
            if (!Elements.elementPresent("home.registry_global_nav_visible")) {
                Clicks.click("home.global_nav_button");
                Assert.assertTrue("ERROR-ENV: Global Nav bar is not visible",
                        Wait.untilElementPresent("home.registry_global_nav_visible"));
            }
        } else {
            if (!Elements.elementPresent("home.global_nav_visible")) {
                Clicks.click("home.global_nav_button");
                Assert.assertTrue("ERROR-ENV: Global Nav bar is not visible",
                        Wait.untilElementPresent("home.global_nav_visible"));
            }
        }
    }

    public static void closeGlobalNav() {
        // sometimes the GN is in the process of closing (from a previous navigation)
        // but still mid-animation. Give it a moment.
        Utils.threadSleep(250, null);
        if (onPage("registry_home", "registry_manager")) {
            if (Elements.elementPresent("home.registry_global_nav_visible")) {
                Clicks.click("home.global_nav_button");
                Assert.assertTrue("ERROR-ENV: Global Nav bar is still visible",
                        Wait.untilElementNotPresent("home.registry_global_nav_visible"));
            }
        } else {
            if (Elements.elementPresent("home.global_nav_visible")) {
                Clicks.click("home.global_nav_button");
                Assert.assertTrue("ERROR-ENV: Global Nav bar is still visible",
                        Wait.untilElementNotPresent("home.global_nav_visible"));
            }
        }
    }

    public static void navigateOnGnByName(String gn_name) {
        Wait.untilElementPresent("home.nav_menu_list");
        for (String aGnName : gn_name.split(" or ")) {
            System.out.println("Navigating to " + aGnName + " element in nav menu...");
            Wait.untilElementPresent("home.current_nav");
            List<WebElement> elements = Elements.findElements("home.current_nav");
            for (WebElement el : elements) {
                if (el.getText().equalsIgnoreCase(aGnName)) {
                Clicks.click(el);
                    
                    try {
                        Wait.attributeChanged(el, "aria-expanded", "true");
                    } catch (StaleElementReferenceException e) {
                        // GN can be closed by click & cause this exception, nothing to worry about
                    }
                    return;
                }
            }
        }
        Assert.fail("Could not find \"" + gn_name + "\" on global nav");
    }

    public static void navigateToRecentlyViewedProduct() {
        Wait.untilElementPresent(Elements.element("product_display.recently_viewed_product_container"));
        List<WebElement> recently_viewed_prods = Elements.findElements(Elements.element("product_display.recently_viewed_products"));
        if (recently_viewed_prods == null || recently_viewed_prods.size() == 0) {
            Assert.fail("ERROR-DATA: Unable to find recently viewed products");
        }
        Clicks.click(recently_viewed_prods.get(0).findElement(By.xpath("a")));
    }
}
