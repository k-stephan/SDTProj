package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Wishlist extends StepUtils {

    @And("^I select wishlist link in header$")
    public void I_select_wishlist_link_in_header() throws Throwable {
        Wait.untilElementPresent("home.goto_wishlist");
        Clicks.click("home.goto_wishlist");
    }

    @Then("^I should see wishlist landing page as a (registered|guest) user$")
    public void I_should_see_wishlist_landing_page_as_a_user(String user) throws Throwable {
        if(safari())
            Wait.secondsUntilElementPresent("wish_list.wishlist_title", 20);
        shouldBeOnPage("wish_list");
        if(user.equals("registered"))
            Clicks.click("wish_list.default_wishlist");
    }

    @Then("^I navigate to a random product PDP from wish list page$")
    public void I_navigate_to_a_random_product_PDP_from_wish_list_page() throws Throwable {
        Clicks.clickRandomElement("wish_list.item_links");
    }

    @When("^I select wishlist link on the wishlist overlay in PDP page$")
    public void I_select_wishlist_link_on_the_wishlist_overlay_in_PDP_page() throws Throwable {
        Wait.secondsUntilElementPresentAndClick("product_display.wishlist_link", 5);
       // Assert.fail("Add to List overlay not visible");
    }

    @When("^I delete all lists in wishlist page$")
    public void I_delete_all_lists_in_wishlist_page() throws Throwable {
        Wait.secondsUntilElementPresentAndClick("home.goto_wishlist", 2);
        CreateProfile.closeSecurityAlertPopUp();
        Wait.untilElementPresent("wish_list.wishlist_title");
        String title = Elements.findElement("wish_list.wishlist_title").getText();
        while (!title.contains("My List (0)") && !(title.contains("My Temporary Wish List") || title.contains("My Wish List") || title.contains("My List"))) {
            Wait.secondsUntilElementPresentAndClick("wish_list.manage_list", 10);
            Wait.secondsUntilElementPresentAndClick("manage_wish_list.select_delete_button", 10);
            Wait.secondsUntilElementPresentAndClick("manage_wish_list.delete_confirm_message", 10);
            Wait.secondsUntilElementPresentAndClick("manage_wish_list.yes_confirmation", 10);
            if (macys()) {
                Navigate.browserRefresh();
            }
            try {
                title = Elements.findElement("wish_list.wishlist_title").getText();
            } catch (Exception ex) {
                System.err.println("I_delete_all_lists_in_wishlist_page:" + ex.getMessage());
                title = "";
            }
        }
    }

    @And("^I create a list \"([^\"]*)\" from wishlist page$")
    public void I_create_a_list_from_wishlist_page(String list_name) throws Throwable {
        Wait.secondsUntilElementPresentAndClick("wish_list.goto_create_list_link", 2);
        Wait.untilElementPresent("wish_list.list_name_text");
        if (!Elements.elementPresent("wish_list.list_name_text")){
            Clicks.clickIfPresent("wish_list.goto_create_list_link");
        }
        TextBoxes.typeTextNEnter("wish_list.list_name_text", list_name);
        if (macys()) {
            Navigate.browserRefresh();
        }
        Wait.untilElementPresent("wish_list.wishlist_title");
    }

    @And("^I add the product to wishlist")
    public void I_add_product_to_wishlist() throws Throwable {
        Wait.secondsUntilElementPresentAndClick("product_display.add_to_wishlist_image", 2);
        if (bloomingdales())
            Wait.secondsUntilElementPresentAndClick("quick_view.default_wishlist", 5);
        else
            Wait.untilElementPresent("product_display.wishlist_overlay");
    }

    @Then("^I should see \"([^\"]*)\" in product line items in wishlist page$")
    public void i_should_see_in_product_line_items_in_wishlist_page(String product) throws Throwable {
        pausePageHangWatchDog();
        if (!Wait.secondsUntilElementPresent("wish_list.item_links", 5))
            Navigate.browserRefresh();
        List<WebElement> plist = Elements.findElements("wish_list.item_links");
        Boolean found = plist.stream().anyMatch(link -> link.getText().equalsIgnoreCase(product));
        Assert.assertTrue("Product is not added to wishlist", found);
        resumePageHangWatchDog();
    }

    @When("^I select a \"([^\"]*)\" product on wishlist page$")
    public void i_select_a_product_on_wishlist_page(String product) throws Throwable {
        List<WebElement> plist = Elements.findElements("wish_list.item_links");
        if (plist.stream().anyMatch(link -> link.getText().equalsIgnoreCase(product)))
            Clicks.click(plist.stream().filter(link -> link.getText().equalsIgnoreCase(product)).findFirst().get());
    }

    @When("^I add product to my bag from wishlist page and (continue shopping|checkout|close)$")
    public void I_add_product_to_my_bag_from_wishlist_page_and(String action) throws Throwable {
        Assert.assertTrue("ERROR-DATA: Unable to find available products in the wishlist", Elements.elementPresent("wish_list.add_to_bag_btn"));
        Clicks.clickRandomElement("wish_list.add_to_bag_btn");
        Wait.untilElementPresent("wish_list.add_to_bag_dialog");
        Assert.assertTrue("ERROR - ENV : Add to bag Dialog is not presented", Elements.elementPresent("wish_list.add_to_bag_dialog"));
        action = action.equals("close") ? "overlay close" : action;
        action = action.replaceAll(" ", "_");
        Clicks.clickIfPresent("wish_list." + action);
    }
}
