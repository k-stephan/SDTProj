package com.macys.sdt.shared.utils;


import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ShoppingBag;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class CheckoutUtils extends StepUtils {

    public enum RCPage {
        SHIPPING("shipping", "responsive_checkout"),
        // shipping & payment is for signed in only
        SHIPPING_AND_PAYMENT("shipping & payment", "responsive_checkout_signed_in"),
        PAYMENT("payment", "responsive_payment_guest_section"),
        REVIEW("order review", "responsive_order_review_section"),
        CONFIRMATION("order confirmation", "responsive_order_summary");

        public final String name;
        public final String page;

        RCPage(String name, String page) {
            this.name = name;
            this.page = page;
        }

        public static RCPage fromString(String name) {
            for (RCPage page : RCPage.values())
                if (name.equalsIgnoreCase(page.name) || name.equalsIgnoreCase(page.page))
                    return page;

            Assert.fail("No checkout page found for: " + name);
            // this will never be reached, putting a non-null value so the lint checker knows this
            // method will never return null
            return RCPage.SHIPPING;
        }

        public String toString() {
            return page;
        }
    }

    private static boolean guestCheckoutSignInPageVerify = true;

    public static void selectGwp() {
        List<WebElement> selects = Elements.findElements(Elements.element("shopping_bag.gwp_selector"));
        for (WebElement s : selects) {
            Select select = new Select(s);
            select.selectByIndex(1);
        }
    }

    public static void selectGwpMEW() {
        while (Elements.elementPresent("shopping_bag.bag_error")) {
            try {
                Clicks.click(Elements.findElement(Elements.element("shopping_bag.bag_error")).findElement(By.xpath("../div/div")));
                Clicks.click(Elements.element("shopping_bag.apply"));
            } catch (Exception e) {
                Assert.fail("Failed to select gwp: " + e);
            }
        }
    }

    public static boolean gwpPresentMEW() {
        return Elements.elementPresent("shopping_bag.bag_error")
                && Elements.getText(Elements.element("shopping_bag.bag_error")).contains("You've qualified for a Bonus offer!");
    }

    public static void removeUnavailableItems() {
        List<WebElement> badProducts = Elements.findElements(Elements.element("shopping_bag.error_message"));
        if (badProducts.size() > 0) {
            badProducts.forEach(el -> {
                if (el.isDisplayed() && !el.getText().isEmpty())
                    Clicks.click(el.findElement(By.xpath("..//span[text()='Remove']")));
            });
        }
    }

    public void navigateToCheckout(boolean signIn, boolean iship) {
        Wait.forPageReady();
        if (signIn && !signedIn()) {
            CommonUtils.signInOrCreateAccount();
        }
        if (!onPage("shopping_bag"))
            Navigate.visit("shopping_bag");
        if (ie() && MainRunner.getWebDriver().getTitle().contains("Certificate Error:"))
            Navigate.execJavascript("document.getElementById('overridelink').click();");

        if (Elements.elementPresent("shopping_bag.gwp_selector"))
            selectGwp();

        if (MEW() && gwpPresentMEW())
            selectGwpMEW();

        removeUnavailableItems();
        ShoppingBag.removeBonusItemsFromBag();
        try {
            Wait.untilElementPresent("shopping_bag.continue_checkout_image");
            if (safari() || MEW())
                Clicks.javascriptClick("shopping_bag.continue_checkout_image");
            else
                Clicks.click("shopping_bag.continue_checkout_image");
            if (!signIn && !iship) {
                if (safari())
                    Wait.secondsUntilElementPresent("checkout_sign_in.continue_checkout_guest_button", 20);
                if (MEW()) {
                    Wait.secondsUntilElementPresent("checkout_sign_in.email", 20);
                    Clicks.click("checkout_sign_in.continue_checkout_guest_button");
                    Wait.forPageReady();
                }
                if (guestCheckoutSignInPageVerify) {
                    Assert.assertTrue("ERROR - ENV : Unable to navigate checkout sign in page", onPage("checkout_sign_in"));
                    guestCheckoutSignInPageVerify = false;
                }
            }

            if (ie() && MainRunner.getWebDriver().getTitle().contains("Certificate Error:"))
                Navigate.execJavascript("document.getElementById('overridelink').click();");
        } catch (org.openqa.selenium.ElementNotVisibleException e) {
            System.out.println("Unable to checkout. Cart may be empty. " + e);
        }
        if (Elements.elementPresent("checkout_sign_in.continue_checkout_guest_button")) {
            Clicks.click(Elements.element("checkout_sign_in.continue_checkout_guest_button"));
            if (!signIn) {
                if (safari())
                    Wait.secondsUntilElementPresent("responsive_checkout.continue_shipping_checkout_button", 20);
                if (onPage("responsive_checkout", "responsive_checkout_signed_in")) {
                    Wait.untilElementPresent("responsive_checkout.continue_shipping_checkout_button");
                    Assert.assertTrue("ERROR-ENV: Unable to navigate to responsive guest checkout page", onPage("responsive_checkout"));
                } else {
                    Assert.assertTrue("ERROR-ENV: Unable to navigate to responsive guest checkout page", onPage("responsive_checkout"));
                }
            }
        }
        // Commenting as this is failing for Safari
        /*if (safari() && signIn)
            Wait.forPageReady("place order")*/;
    }
}
