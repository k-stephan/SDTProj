package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ProductDisplay;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Random;

import static com.macys.sdt.shared.utils.CommonUtils.quickViewRandomProduct;

public class QuickView extends StepUtils {

    @When("^I select a random product in a quickview dialog$")
    public void I_select_a_random_product_in_a_quickview_dialog() throws Throwable {
        if (macys()) {
            // tablet resolution
            if (Elements.elementPresent("search_result.product_thumbnail_quickview_tablet")) {
                Clicks.clickRandomElement("search_result.product_thumbnail_quickview_tablet");
            } else {
                WebElement thumbnail = Elements.getRandomElement("search_result.product_thumbnail");
                Clicks.hoverForSelection(thumbnail);
                if (!Wait.untilElementPresent("search_result.product_thumbnail_quickview")) {
                    Clicks.click(thumbnail);
                    return;
                }

                Clicks.click("search_result.product_thumbnail_quickview");
                Clicks.clickIfPresent("quick_view.survey_close_button");
                Wait.untilElementPresent("quick_view.quick_view_product_dialog");
            }
        } else {
            Clicks.randomJavascriptClick("search_result.product_thumbnail_quickview");
            Assert.assertTrue("ERROR-ENV: Quick view Dialog is not present", Wait.untilElementPresent("quick_view.quick_view_product_dialog"));
        }
    }

    @When("^I quick view a random (member|master|member_alternate_image|master_alternate_image) product(?: with (customer ratings))?$")
    public void I_quick_view_a_random_product(String prod_type, String hasRating) throws Throwable {
        boolean found = false;
        int i = 0, max = 5;

        while (!found && i++ < max) {
            quickViewRandomProduct(hasRating != null, prod_type.toLowerCase().contains("master"));
            switch (prod_type.toLowerCase()) {
                case "member":
                    found = ProductDisplay.isMasterMemberQuickViewDialog();
                    break;
                case "master":
                    found = !ProductDisplay.isMasterMemberQuickViewDialog();
                    break;
                case "member_alternate_image":
                    found = Elements.elementPresent("quick_view.quick_view_alt_images")
                            && ProductDisplay.isMasterMemberQuickViewDialog();
                    break;
                case "master_alternate_image":
                    found = Elements.elementPresent("quick_view.quick_view_alt_images")
                            && !ProductDisplay.isMasterMemberQuickViewDialog();
                    break;
            }
            if (!found) {
                Clicks.clickIfPresent("quick_view.quick_view_close_dialog");
            }
        }
        if (!found) {
            Assert.fail("Failed to find " + prod_type + " product after " + max + " tries.");
        }
    }

    @Then("^I select 'see full product details' link from the quickview dialog$")
    public void I_select_see_full_product_details_link_from_the_quickview_dialog() throws Throwable {
        Assert.assertTrue("ERROR-ENV: Unable to find see full product info link", Elements.elementPresent("quick_view.quick_view_see_full_details"));
        Wait.secondsUntilElementPresentAndClick("quick_view.quick_view_see_full_details", 30);
        new PageNavigation().I_should_be_redirected_to_PDP_page();
    }

    @When("^I close the quickview dialog$")
    public void I_close_the_quickview_dialog() throws Throwable {
        Wait.secondsUntilElementPresentAndClick("quick_view.quick_view_close_dialog", 2);
    }

    @And("^I add the item to the bag from quick view$")
    public void I_add_the_item_to_the_bag_from_quick_view() throws Throwable {
        if (bloomingdales()) {
            if (Elements.elementPresent("quick_view.quick_view_product_size_list")) {
                List<WebElement> sizes = Elements.findElement("quick_view.quick_view_product_size_list").findElements(By.xpath("li"));
                WebElement prodSize = sizes.get(new Random().nextInt(sizes.size()));
                Clicks.click(prodSize.findElement(By.xpath("span")));
            }
        } else {
            if (Elements.elementPresent("quick_view.quick_view_product_size")) {
                DropDowns.selectByIndex("quick_view.quick_view_size_dropdown", 1);
            }
        }
        Clicks.click("quick_view.quick_view_product_add_to_bag");
        Wait.untilElementNotPresent("quick_view.quick_view_product_add_to_bag");
    }

    @And("^I add the item to wishlist from QV$")
    public void I_add_the_product_to_wishlist_from_QV() throws Throwable {
        if (macys()) {
            DropDowns.selectByIndex("quick_view.quick_view_size_dropdown", 1);
            if (Elements.elementPresent("quick_view.selected_color")) {
                WebElement color = Elements.findElement("quick_view.selected_color");
                while (color.getAttribute("class").contains("disabledOption")) {
                    Clicks.clickRandomElement("quick_view.colorway_swatch");
                    color = Elements.findElement("quick_view.selected_color");
                }
            }
        } else {
            if (Elements.findElement("quick_view.quick_view_product_size_list").isDisplayed()) {
                Clicks.clickRandomElement("quick_view.quick_view_product_size");
            }
        }

        Clicks.click("quick_view.quick_view_product_add_to_wishlist");
        if (bloomingdales() && Elements.elementPresent("quick_view.default_wishlist")) {
            Clicks.click("quick_view.default_wishlist");
        }

        if (bloomingdales()) {
            Wait.untilElementPresent("quick_view.quick_view_add_to_wishlist_overlay");
            if (Elements.findElement("quick_view.quick_view_add_to_wishlist_overlay").getText().contains("Sorry"))
                Clicks.click("quick_view.quick_view_product_add_to_wishlist");
        }
    }

    @And("^I navigate to shopping bag page from quick view dialog$")
    public void I_navigate_to_shopping_bag_page_from_quick_view_dialog() throws Throwable {
        Clicks.click("quick_view.checkout_now");
        if (onPage("add_to_bag"))
            Clicks.click("add_to_bag.checkout");
        shouldBeOnPage("shopping_bag");
    }
}
