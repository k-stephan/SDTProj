package com.macys.sdt.shared.steps.MEW;


import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

public class Wishlist extends StepUtils {

    /**
     * Deletes all wish lists the current user has from wish list page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I delete all lists in wishlist page using mobile website$")
    public void I_delete_all_lists_in_wishlist_page_using_mobile_website() throws Throwable {
        CreateProfile.closeSecurityAlertPopUp();
        if (Elements.elementPresent("my_account.my_list")) {
            Clicks.click("my_account.my_list");
            Wait.untilElementPresent("wish_list.list_item_count");
            String count = Elements.getText("wish_list.list_item_count");
            while (!count.equals("0")) {
                Clicks.click("wish_list.item_delete_link");
                String new_count = Elements.getText("wish_list.list_item_count");
                while (new_count.equals(count))
                    Navigate.browserRefresh();
                count = new_count;
            }
            return;
        }
        Wait.untilElementPresent("wish_list.wishlist_title");
        String title = Elements.findElement("wish_list.wishlist_title").getText();
        while (!title.contains("My List") && !(!(title.contains("Test's List")) || title.contains("My Wish List") || title.contains("My List"))) {
            Wait.secondsUntilElementPresentAndClick("wish_list.manage_list", 10);
            Wait.secondsUntilElementPresentAndClick("wish_list.udate_setting", 10);
            Wait.untilElementPresent("wish_list.select_delete_button");
            Wait.secondsUntilElementPresentAndClick("wish_list.select_delete_button", 10);
            Wait.secondsUntilElementPresentAndClick("wish_list.delete_confirm_button", 10);
            if (macys()) {
                Navigate.browserRefresh();
            }
            try {
                title = Elements.findElement(Elements.element("wish_list.wishlist_title")).getText();
            } catch (Exception ex) {
                System.err.println("I_delete_all_lists_in_wishlist_page:" + ex.getMessage());
                title = "";
            }
        }
    }

    /**
     * Adds a random product to bag from wishlist page and selects given button
     *
     * @param action "continue shopping", "checkout" or "close"
     * @throws Throwable if any exception occurs
     */
    @When("^I add product to my bag from wishlist page using mobile website and (continue shopping|checkout|close)$")
    public void I_add_product_to_my_bag_from_wishlist_page_using_mobile_website_and(String action) throws Throwable {
        Assert.assertTrue("ERROR-DATA: Unable to find available products in the wishlist", Wait.untilElementPresent("wish_list.add_to_bag_btn"));
        Clicks.clickRandomElement("wish_list.add_to_bag_btn");
        Wait.untilElementPresent("wish_list.add_to_bag_dialog");
        Assert.assertTrue("ERROR-ENV: Add to bag Dialog is not presented", Elements.elementPresent("wish_list.add_to_bag_dialog"));
        switch (action.toLowerCase()) {
            case "continue shopping":
                Clicks.clickIfPresent("wish_list.continue_shopping");
                break;
            case "checkout":
                Clicks.clickIfPresent("wish_list.checkout");
                break;
            case "close":
                Clicks.clickIfPresent("wish_list.overlay_close");
                break;
            default:
                Assert.fail("Invalid option found");
                break;
        }
    }

    /**
     * Selects a random product from wish list page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I navigate to a random product PDP from wish list page using mobile website$")
    public void I_navigate_to_a_random_product_PDP_from_wish_list_page_using_mobile_website() throws Throwable {
        Wait.untilElementPresent("wish_list.item_links");
        Clicks.clickRandomElement("wish_list.item_links");
    }
}
