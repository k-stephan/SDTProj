package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.framework.utils.db.models.PromotionService;
import com.macys.sdt.shared.actions.website.bcom.pages.RegistryBVR;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.MyWallet;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.CategorySplash;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ProductDisplay;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.SearchResults;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ShoppingBag;
import com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.ChangePickupStoreDialog;
import com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.RecentlyViewed;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.macys.sdt.shared.utils.CommonUtils.retryAction;
import static com.macys.sdt.shared.utils.CommonUtils.selectRandomProduct;

public class ShopAndBrowse extends StepUtils {

    public static String brandSearch;
    public static String promotionalProdId;
    public static String promotionCode;
    public static Product recentProduct;
    public static String promoCode;

    /**
     * Selects given distance in bops store dialog
     *
     * @param distance exact text of distance to select
     * @throws Throwable if any exception occurs
     */
    @When("^I select \"([^\"]*)\" in bops change store dialog$")
    public static void I_select_miles_in_bops_change_store_dialog(String distance) throws Throwable {
        DropDowns.selectByText("change_pickup_store_dialog.search_distance", distance);
    }

    /**
     * Selects save and closes the bops change store dialog
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I close the bops change store dialog$")
    public static void I_save_close_the_bops_change_store_dialog() throws Throwable {
        //Clicks.click("change_pickup_store_dialog.save");
        if (bloomingdales()) {
            if (Elements.elementPresent("change_pickup_store_dialog.close")) {
                Clicks.click("change_pickup_store_dialog.close");
            }
        } else {
            Clicks.clickIfPresent("change_pickup_store_dialog.overlay_close_button");
        }

    }

    /**
     * Adds product to registry from PDP
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add registry product to BVR page from standard PDP(?: Page)?")
    public void I_add_registry_product_to_BVR_page_from_standard_PDP_Page() throws Throwable {
        pausePageHangWatchDog();
        Assert.assertFalse("ERROR - DATA : Product ( " + String.valueOf(recentProduct.id) + " ) is unavailable on product display page!!", !Elements.elementPresent("product_display.add_to_registry") && Elements.elementPresent("product_display.availability_error"));
        if (macys()) {
            int retries = 5;
            for (int count = 0; count < retries && !Elements.elementPresent("add_to_registry_dialog.add_to_bag_view_registry"); count++) {
                ProductDisplay.selectRandomColor();
                ProductDisplay.selectRandomSize();
                if (ProductDisplay.isMasterMemberPage()) {
                    Clicks.clickRandomElement("product_display.add_to_registry");
                } else {
                    Clicks.click("product_display.add_to_registry");
                }
                Wait.secondsUntilElementPresent("add_to_registry_dialog.add_to_bag_view_registry", 5);
                Clicks.clickIfPresent("product_display.technical_error");
                if (isErrorPaneVisible()) {
                    Clicks.click("home.popup_close");
                }
                if (macys() && Elements.anyPresent(By.className("close-grey-tiny"))) {
                    Assert.assertTrue("ERROR - DATA : Product is not eligible to add to registry !!", Elements.findElement(By.className("close-grey-tiny")).getText().contains("This item is not registrable"));
                }
            }
            try {
                Wait.forPageReady();
                Wait.untilElementPresent("add_to_registry_dialog.add_to_bag_view_registry");
                Clicks.click("add_to_registry_dialog.add_to_bag_view_registry");
            } catch (NoSuchElementException e) {
                Assert.fail("Unable to add product to registry" + e);
            }
        } else {
            Clicks.clickIfPresent("header_and_footer.close_new_offer_popup");

            ProductDisplay.selectRandomColor();
            ProductDisplay.selectRandomSize();
            Clicks.javascriptClick("product_display.add_to_registry");
            Wait.secondsUntilElementPresent("product_display.add_to_registry_dialog", 10);
            if (Elements.elementPresent("product_display.add_to_registry_dialog")) {
                if (macys()) {
                    Clicks.click("product_display.add_to_bag_view_registry");
                } else {
                    Elements.findElements("product_display.add_to_bag_view_registry").forEach(item -> {
                        try {
                            if (item.isDisplayed()) {
                                Clicks.click(item);
                            }
                        } catch (Exception e) {
                            // exception will be thrown on BCOM non-gift items. No need to take action.
                        }
                    });
                }
            } else {
                Assert.fail("Unable to add product to registry");
            }
        }
        resumePageHangWatchDog();
    }

    /**
     * Adds product to bag from PDP
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add product to my bag from standard PDP(?: Page)?$")
    public void I_add_product_to_my_bag_from_standard_PDP_Page() throws Throwable {
        boolean addedToBag = false;
        Assert.assertFalse("ERROR - DATA : Product ( " + (recentProduct == null ? "" : String.valueOf(recentProduct.id)) + " ) is unavailable on product display page!!", !Elements.elementPresent("product_display.add_to_bag_button") && Elements.elementPresent("product_display.availability_error"));
        try {
            int retries = 5;
            pausePageHangWatchDog();
            for (int count = 0; count < retries && !addedToBag; count++) {
                try {
                    ProductDisplay.selectRandomColor();
                    ProductDisplay.selectRandomSize();
                    Clicks.click("product_display.add_to_bag_button");
                    if (!Elements.elementPresent("add_to_bag_dialog.add_to_bag_dialog")) {
                        Clicks.clickIfPresent("product_display.add_to_bag_button");
                    }

                    addedToBag = ProductDisplay.addedToBag();
                    if (MainRunner.debugMode) {
                        System.out.println("IsProductAddedToBag:" + addedToBag + ", Add to bag retry count:" + (count + 1));
                    }
                } catch (Exception e) {
                    System.err.println("Exception while adding product:" + e.getMessage());
                }
            }
            Wait.untilElementPresent("add_to_bag_dialog.add_to_bag_dialog");
            if (!Elements.elementPresent("add_to_bag_dialog.add_to_bag_dialog")) {
                Clicks.clickIfPresent("product_display.technical_error");
            }
            if (isErrorPaneVisible()) {
                Clicks.click("home.popup_close");
            }
            resumePageHangWatchDog();
        } catch (IllegalArgumentException | NoSuchElementException e) {
            System.err.println("Error while adding to bag: " + e);
        } finally {
            if (!addedToBag) {
                Wait.untilElementNotPresent("product_display.add_to_bag_button");
                if (macys()) {
                    Assert.assertFalse("ERROR - DATA : Given item is unavailable!!", Elements.elementPresent(By.className("css-tooltip")) && Elements.getText(By.className("css-tooltip")).contains("this item is unavailable"));
                }
                Assert.assertTrue("Unable to add product to bag", ProductDisplay.addedToBag());
            }

        }
    }

    /**
     * Adds given quantity of the current product to bag
     *
     * @param quantity how many to add
     * @throws Throwable if any exception occurs
     */
    @And("^I add \"([^\"]*)\" quantity to my bag from standard PDP Page$")
    public void iAddQuantityToMyBagFromStandardPDP(String quantity) throws Throwable {
        shouldBeOnPage("product_display");
        ProductDisplay.selectQuantity(quantity);
        I_add_product_to_my_bag_from_standard_PDP_Page();
    }

    /**
     * Searches for given text in the top search bar
     *
     * @param value text to search for
     * @throws Throwable if any exception occurs
     */
    @When("^I search for \"([^\"]*)\"$")
    public void I_search_for(String value) throws Throwable {
        TextBoxes.typeTextNEnter("home.search_field", value);
        Wait.forPageReady();
    }

    /**
     * Selects the given social icon if present
     *
     * @param social_icon icon to check for (eg: pinterest|email|facebook etc)
     * @throws Throwable if any exception occurs
     */
    @And("^I select \"([^\"]*)\" social icon on PDP Page$")
    public void I_select_social_icon_on_PDP_Page(String social_icon) throws Throwable {
        Navigate.browserRefresh();
        if (onPage("radical_pdp") && !social_icon.matches("pinterest|email")) {
            // radical pdp only has pinterest icon, can't expect others to be there
            System.err.println("Can't click" + social_icon + " on radical pdp, does not exist");
            return;
        }
        if (!Clicks.clickWhenPresent("product_display.social_icon_" + social_icon.toLowerCase())) {
            Assert.fail(social_icon + " is not Available");
        }

        if (ie() && MainRunner.getWebDriver().getTitle().contains("Certificate Error:")) {
            Set<String> windowSet = MainRunner.getWebDriver().getWindowHandles();
            Assert.assertTrue("The popup window has not opened.", windowSet.size() > 1);
            MainRunner.getWebDriver().switchTo().window((String) windowSet.toArray()[1]);
            MainRunner.getWebDriver().get("javascript:document.getElementById('overridelink').click();");
        }
    }

    /**
     * Changes active country to given country
     *
     * @param country full name of country as it is in the country selection drop down
     * @throws Throwable if any exception occurs
     */
    @When("^I change country to \"([^\"]*)\"$")
    public void I_change_country_to(String country) throws Throwable {
        boolean newDropDownEnabled;
        if (Wait.secondsUntilElementPresent("international_shipping.country", (safari() ? 20 : 5))) {
            Assert.assertTrue("Not on international context page.", Wait.secondsUntilElementPresent("international_shipping.country", (safari() ? 20 : 5)));
            newDropDownEnabled = false;
        } else {
            Assert.assertTrue("Not on international context page.", Wait.secondsUntilElementPresent("international_shipping.country_options", (safari() ? 20 : 5)));
            newDropDownEnabled = true;
        }
        List<String> values;
        Random random = new Random();
        values = (newDropDownEnabled ? DropDowns.getAllCustomValues("international_shipping.country_options", "international_shipping.country_list") : DropDowns.getAllValues("international_shipping.country"));
        String selectCountry = (country.equals("a random country") ? values.get(random.nextInt(values.size())) : country);
        if (newDropDownEnabled) {
            DropDowns.selectCustomText("international_shipping.country_options", "international_shipping.country_list", selectCountry);
        } else {
            DropDowns.selectByText("international_shipping.country", selectCountry);
        }
        Clicks.click("international_shipping.save_continue");
        Wait.forPageReady();
        Clicks.clickIfPresent("home.close_overlay_country");
        Navigate.visit("home");
        Wait.forPageReady();
        closeBcomLoyaltyPromotionVideoOverlay();
    }

    /**
     * Closes the iship welcome mat
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I close the welcome mat if it's visible$")
    public void I_close_the_welcome_mat_if_it_s_visible() throws Throwable {
        try {
            Wait.untilElementPresent("welcome_mat_dialog.container");
            Clicks.click("welcome_mat_dialog.close");
            Wait.untilElementNotPresent("welcome_mat_dialog.container");
        } catch (Exception e) {
            System.out.println("Welcome mat is not being displayed");
        }
    }

    /**
     * Selects a customer top rated product from category page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select a customer top rated product$")
    public void I_select_a_customer_top_rated_product() throws Throwable {
        if (!Elements.elementPresent("category_splash.top_rated")) {
            CategorySplash.selectCustomerTopRatedProduct();
        } else {
            Assert.fail("ERROR - ENV: shop customers' top rated panel is not visible..... ");
        }
    }

    /**
     * Selects a product with the given image title
     *
     * @param product name of product to select
     * @throws Throwable if any exception occurs
     */
    @When("^I select a \"([^\"]*)\" product$")
    public void I_select_a_product(String product) throws Throwable {
        if (MainRunner.useSauceLabs) {
            Clicks.hover(By.xpath("//img[@title='" + product + "']"));
        }
        Clicks.click(By.xpath("//img[@title='" + product + "']"));
    }

    /**
     * Searches using the given data on the given page
     *
     * @param searchInput    search text
     * @param searchCriteria zipcode|city
     * @param page           page you're searching on - store locations|events search
     * @throws Throwable if any exception occurs
     */
    @When("^I search using \"([^\"]*)\" as \"(zipcode|city)\" in \"(store locations|events search)\" page$")
    public void I_search_using_as_in_page(String searchInput, String searchCriteria, String page) throws Throwable {
        switch (page.toLowerCase()) {
            case "store locations":
                TextBoxes.typeTextbox("stores.search_box", searchInput);
                Clicks.click("stores.search_button");
                break;
            case "events search":
                if (searchCriteria.equalsIgnoreCase("zipcode")) {
                    TextBoxes.typeTextbox("events.zip_code", searchInput);
                } else {
                    TextBoxes.typeTextbox("events.city", searchInput);
                }
                Clicks.click("events.search_button");
                break;
        }
    }

    /**
     * Selects the given text in search result sort by dropdown menu
     *
     * @param toSelect text to select
     * @throws Throwable if any exception occurs
     */
    @And("^I select \"([^\"]*)\" in sort by drop down$")
    public void I_select_in_sort_by_drop_down(String toSelect) throws Throwable {
        Wait.forPageReady();
        if (macys()) {
            DropDowns.selectByText("search_result.sort_by_select", toSelect);
        } else {
            Clicks.clickWhenPresent("search_result.sort_by_select");
            Wait.forLoading("search_result.sort_by_option");
            Clicks.clickElementByText("search_result.sort_by_option", toSelect);
        }
        Wait.forLoading(By.id("loading_mask"));
    }

    /**
     * Changes the number of items per page to the given number
     *
     * @param numItems number of items to show
     * @throws Throwable if any exception occurs
     */
    @And("^I filter the result set to show \"([^\"]*)\" items$")
    public void I_filter_the_result_set_to_show_items(String numItems) throws Throwable {
        Clicks.click(SearchResults.showItemsPerPage(numItems));
        Wait.forLoading(By.id("loading_mask"));
    }

    /**
     * Changes the number of columns to the given number, either 3 or 4
     *
     * @param number number of columns
     * @throws Throwable if any exception occurs
     */
    @And("^I select \"([^\"]*)\" Column Grid icon$")
    public void I_select_Column_Grid_icon(String number) throws Throwable {
        By el = SearchResults.selectGridColumns(number);
        //hoverForSelection(el);
        try {
            Wait.untilElementPresent(el);
            Clicks.click(el);
            Wait.forLoading(By.id("loading_mask"));
        } catch (Exception e) {
            // low resolution display does not have the icon
        }
    }

    /**
     * Searches nearby stores for stock of the current product
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I search for a product in a nearby store$")
    public void I_search_for_a_product_in_a_nearby_store() throws Throwable {
        Assert.assertTrue("Unable to locate product in store", Elements.elementPresent("product_display.find_store_link"));
        Clicks.click("product_display.find_store_link");
        Assert.assertTrue("ERROR: Find in store overlay is not visible ", Elements.findElement("product_display.find_in_store_overlay").isDisplayed());
        if (macys()) {
            DropDowns.selectByText("change_pickup_store_dialog.search_distance", "100 miles");
        } else {
            DropDowns.selectCustomText("change_pickup_store_dialog.search_distance",
                    "change_pickup_store_dialog.search_distance_values", "100 Miles");
        }
        Wait.forLoading("home.loading");
        String zip_code = (macys() ? "22102" : "10022");
        TextBoxes.typeTextbox("change_pickup_store_dialog.address_zip_code", zip_code);
        Clicks.click("change_pickup_store_dialog.search_button");
    }

    /**
     * Enters the given invalid email and password into the current sign in page (registry or normal)
     *
     * @param email    email address
     * @param password invalid password
     * @throws Throwable if any exception occurs
     */
    @And("^I enter invalid \"([^\"]*)\" and \"([^\"]*)\"$")
    public void I_enter_invalid_and_in_the_fields(String email, String password) throws Throwable {
        if (onPage("registry_home")) {
            Clicks.click("registry_home.sign_in_email");
            TextBoxes.typeTextbox("registry_home.sign_in_email", email);
            Clicks.click("registry_home.fake_password");
            TextBoxes.typeTextbox("registry_home.sign_in_password", password);
            Clicks.click("registry_home.sign_in_button");
        } else {
            TextBoxes.typeTextbox("sign_in.email", email);
            TextBoxes.typeTextbox("sign_in.password", password);
            Clicks.click("sign_in.verify_page");
        }
    }

    /**
     * Closes the in-store Availability pop up
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I close the instore Availability popUp$")
    public void I_close_the_instore_Availability_popUp() throws Throwable {
        Clicks.click("change_pickup_store_dialog.close");
    }

    /**
     * Selects a color and size for the current product
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select product related attributes from PDP$")
    public void I_select_product_related_attributes_from_PDP() throws Throwable {
        ProductDisplay.selectRandomColor();
        ProductDisplay.selectRandomSize();
    }

    /**
     * Adds a random offer to my wallet from promotions page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I click on 'add to wallet' button for any offer in deals and promotions page$")
    public void I_click_on_add_to_wallet_button_for_any_offer_in_deals_and_promotions_page() throws Throwable {
        if (Elements.elementPresent("my_offers.add_to_wallet")) {
            Clicks.click("my_offers.add_to_wallet");
        } else {
            System.out.println("No offers present that can be added to wallet");
        }
    }

    /**
     * Closes the details and exclusions overlay on my offers page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select 'X' on details and exclusions overlay$")
    public void I_select_X_on_details_and_exclusions_overlay() throws Throwable {
        if (Elements.elementPresent("my_offers.details_and_exclusions_close")) {
            Clicks.click("my_offers.details_and_exclusions_close");
        } else {
            System.out.println("No details window was opened");

        }
    }

    /**
     * Opens a coupon on the my offers page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I open the coupon window$")
    public void I_open_the_coupon_window() throws Throwable {
        if (!Clicks.clickIfPresent("my_offers.get_savings_pass")) {
            System.out.println("No coupons present.");
        }
    }

    /**
     * Clicks the "Shop now" button
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I click on SHOP NOW button$")
    public void I_click_on_SHOP_NOW_button() throws Throwable {
        Clicks.click("my_offers.shop_now");
    }

    /**
     * Selects a random product with the given attributes
     *
     * @param prod_type member|master|member_alternate_image|master_alternate_image
     * @param hasRating present if you want a product with customer ratings
     * @throws Throwable if any exception occurs
     */
    @When("^I select a random (member|master|member_alternate_image|master_alternate_image) product(?: with (customer ratings))?$")
    public void I_select_a_random_product(String prod_type, String hasRating) throws Throwable {
        Wait.untilElementPresent("product_display.loader_completed");
        boolean found = false;
        int i = 0, max = 5;

        while (!found && i++ < max) {
            selectRandomProduct(hasRating != null, prod_type.toLowerCase().contains("master"));
            switch (prod_type.toLowerCase()) {
                case "member":
                    found = !ProductDisplay.isMasterMemberPage();
                    break;
                case "master":
                    found = ProductDisplay.isMasterMemberPage();
                    break;
                case "member_alternate_image":
                    found = Elements.elementPresent("product_display.alt_image_container")
                            && !ProductDisplay.isMasterMemberPage();
                    break;
                case "master_alternate_image":
                    found = Elements.elementPresent("product_display.alt_image_container")
                            && ProductDisplay.isMasterMemberPage();
                    break;
            }
            if (MEW()) {
                found &= onPage("product_display");
                if (!found) {
                    Navigate.browserBack();
                }
            } else if (!found) {
                Navigate.browserBack();
            }
        }
        if (!found) {
            Assert.fail("Failed to find " + prod_type + " product after " + max + " tries.");
        }

        if (chrome()) {
            MainRunner.getWebDriver().navigate().refresh();
            Wait.forPageReady();
        }
    }

    /**
     * Selects a random alternate image on pdp
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select a random alternative image$")
    public void I_select_a_random_alternative_image() throws Throwable {
        Assert.assertTrue("Unable to locate any alternative images", Elements.elementPresent("product_display.alt_images"));
        Clicks.clickRandomElement("product_display.alt_images");
    }

    /**
     * Selects all attributes for bops on pdp and adds product to bag
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I add bops available product to my bag from standard PDP Page$")
    public void I_add_bops_available_product_to_my_bag_from_standard_PDP_Page() throws Throwable {
        List<WebElement> colors = Elements.findElements("product_display.select_default_color");
        List<WebElement> sizes = Elements.findElements("product_display.select_default_size");
        if (colors.size() > 0) {
            for (WebElement color : colors) {
                Clicks.click(color);
                // sizes may change per color, need to refresh
                sizes = Elements.findElements("product_display.select_default_size");
                for (int i = 0; i < sizes.size(); i++) {
                    Clicks.click(sizes.get(i));
                    if (ProductDisplay.bopsEligible()) {
                        //There are two element with same id. One for tablet and another for mobile. so had to filter out the displayed element and click in below step
                        Clicks.clickRandomElement("product_display.add_to_bag_button", WebElement::isDisplayed);
                        Wait.untilElementNotPresent("product_display.add_to_bag_button");
                        if (ProductDisplay.addedToBag()) {
                            return;
                        }
                    }
                    // we get stale references if we don't update every time
                    sizes = Elements.findElements("product_display.select_default_size");
                }
            }
        } else if (sizes.size() > 0) {
            for (int i = 0; i < sizes.size(); i++) {
                Clicks.click(sizes.get(i));
                if (ProductDisplay.bopsEligible()) {
                    Clicks.click("product_display.add_to_bag_button");
                    Wait.untilElementNotPresent("product_display.add_to_bag_button");
                    if (ProductDisplay.addedToBag()) {
                        return;
                    }
                }
                // we get stale references if we don't update every time
                sizes = Elements.findElements("product_display.select_default_size");
            }
        }

        Assert.fail("Could not add bops available product to bag");
    }

    /**
     * Types the given text in the top search bar
     *
     * @param searchTerm text to type in search bar
     * @throws Throwable if any exception occurs
     */
    @When("^I type \"([^\"]*)\" in search box$")
    public void I_type_in_search_box(String searchTerm) throws Throwable {
        TextBoxes.typeTextbox("home.search_field", searchTerm);
    }

    /**
     * Selects the given text in autocomplete suggestions
     *
     * @param select text to select
     * @throws Throwable if any exception occurs
     */
    @And("^I select \"([^\"]*)\" from autocomplete suggestions$")
    public void I_select_from_autocomplete_suggestions(String select) throws Throwable {
        try {
            Wait.untilElementPresent("header.suggestions");
            Clicks.clickElementByText("header.suggestions", select);
        } catch (Exception e) {
            Assert.fail("Element not present " + e);
        }
    }


    /**
     * Types in the search bar and selects from the suggestions as given
     *
     * @param searchTerm what to search for
     * @param select     what to select
     * @throws Throwable if any exception occurs
     */
    @When("^I type \"([^\"]*)\" in search box and select \"([^\"]*)\" from autocomplete suggestions$")
    public void I_type_in_search_box_and_select_from_autocomplete_suggestions(String searchTerm, String select) throws Throwable {
        try {
            TextBoxes.typeTextbox("home.search_field", searchTerm);
            Wait.untilElementPresent("header.suggestions");
            Clicks.clickElementByText("header.suggestions", select);
        } catch (Exception e) {
            Assert.fail("Element not present " + e);
        }
    }

    /**
     * Verifies that the given option is visible in autocomplete suggestions
     *
     * @param select expected option
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see \"([^\"]*)\" in autocomplete suggestions$")
    public void I_should_see_autocomplete_suggestions(String select) throws Throwable {
        Wait.untilElementPresent("header.suggestions");
        List<WebElement> list = Elements.findElements("header.suggestions");
        if (list == null || list.size() == 0) {
            Assert.fail("Auto completion has no results");
        } else {
            list.forEach(el ->
                    Assert.assertTrue("Search word not found in auto complete",
                            el.getText().toLowerCase().contains(select.toLowerCase())));
        }
    }

    /**
     * Verifies that autocomplete suggestions are not currently visible
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should not see autocomplete suggestions$")
    public void I_should_see_autocomplete_suggestions() throws Throwable {
        if (Elements.elementPresent("header.suggestions")) {
            Assert.fail("Wedding registry should not have autocomplete results");
        }
    }


    /**
     * Searches for the given text and expects the given recommendation panel to be present on search results page
     *
     * @param keyword        what to search for
     * @param panel_position vertical|horizontal|no_panel
     * @throws Throwable if any exception occurs
     */
    @When("^I search for \"([^\"]*)\" on home page for \"(vertical|horizontal|no_panel)\" panel$")
    public void I_search_for_on_home_page_for_panel(String keyword, String panel_position) throws Throwable {
        switch (panel_position) {
            case "vertical":
                Cookies.addSegment("220");
                break;
            case "horizontal":
                Cookies.addSegment("221");
                break;
            case "no_panel":
                Cookies.addSegment("222");
                break;
        }
        I_search_for(keyword);
        Wait.untilElementPresent("search_result.verify_page");
    }

    /**
     * Selects a random product from given recommendation panel type
     *
     * @param panel vertical|horizontal
     * @throws Throwable if any exception occurs
     */
    @When("^I select a random product from \"(vertical|horizontal)\" recommendation panel$")
    public void I_select_a_random_product_from_recommendation_panel(String panel) throws Throwable {
        if (onPage("product_display")) {
            if (panel.equals("vertical")) {
                Clicks.clickRandomElement("recommendations.vertical_recommendations");
            } else {
                Clicks.clickRandomElement("recommendations.horizontal_recommendations");
            }
        } else {
            try {
                Wait.secondsUntilElementPresent("recommendations.horizontal_recommendations", 5);
                Clicks.clickRandomElement("recommendations.horizontal_recommendations");
            } catch (Exception e) {
                Clicks.clickRandomElement("recommendations.vertical_recommendations");
            }
        }
    }

    /**
     * Adds a product with the given attributes to bag from BVR page
     *
     * @param productTrue  Properties expected to be true separated by either a space or the word "and"
     * @param productFalse Properties expected to be false separated by either a space or the word "and"
     * @throws Throwable if any exception occurs
     */
    @When("^I add \"([^\"]*)\" product to my bag(?: that is not(?: an?)? \"(.*?)\")? from BVR page$")
    public void i_add_product_to_my_bag_from_BVR_page(String productTrue, String productFalse) throws Throwable {
        iNavigateToPdp(productTrue, productFalse);
        I_add_registry_product_to_BVR_page_from_standard_PDP_Page();

        if (Elements.elementPresent("home.continue_shopping")) {
            Clicks.click("home.continue_shopping");
            I_add_registry_product_to_BVR_page_from_standard_PDP_Page();
        }
        Wait.forPageReady();
        if (safari()) {
            Wait.secondsUntilElementPresent("registry_bvr.bvr_prod_list", 10);
        }
        int rad_qut;
        ArrayList<Object> products = RegistryBVR.getProducts();
        pausePageHangWatchDog();
        if (products.size() > 0) {
            rad_qut = new Random().nextInt(products.size());
            RegistryBVR.selectProdQuantity(rad_qut, "1");
            RegistryBVR.registryAddToBag();
        } else {
            Assert.fail("The products list is empty");
        }
        resumePageHangWatchDog();
    }

    /**
     * Adds the given product ID to bag
     *
     * @param availableProductId product id to add to bag
     * @throws Throwable if any exception occurs
     */
    @When("^I directly add an available and orderable product \"([^\"]*)\" to my bag$")
    public void I_directly_add_an_available_and_orderable_product_to_my_bag(String availableProductId) throws Throwable {
        CommonUtils.navigateDirectlyToProduct(availableProductId);
        I_add_product_to_my_bag_from_standard_PDP_Page();

        if (onPage("add_to_bag")) {
            Clicks.click("add_to_bag.checkout");
        } else if (Elements.elementPresent("add_to_bag_dialog.add_to_bag_checkout")) {
            Clicks.click("add_to_bag_dialog.add_to_bag_checkout");
        } else {
            Clicks.click("product_display.member_atb_checkout");
        }
    }

    /**
     * Goes back to the previous breadcrumb
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select previous page category from breadcrumb$")
    public void I_select_previous_page_category_from_breadcrumb() throws Throwable {
        List<WebElement> breadcrumbs = Elements.findElements("category_splash.breadcrumbs");
        Clicks.click(breadcrumbs.get(breadcrumbs.size() - 2));
    }

    /**
     * Clicks a random ad on the home page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select random asset from home page$")
    public void I_select_random_asset_from_home_page() throws Throwable {
        if (Elements.elementPresent("home.row_one_point_one")) {
            Clicks.click(Elements.findElement("home.row_one_point_one").findElement(By.tagName("img")));
        }
    }

    /**
     * Clicks the given asset on home page
     *
     * @param assetNumber first|second|third|fourth|fifth
     * @throws Throwable if any exception occurs
     */
    @When("^I select (first|second|third|fourth|fifth) asset from home page$")
    public void I_select_specific_asset_from_home_page(String assetNumber) throws Throwable {
        StringBuilder elementTag = new StringBuilder("home.row_one_point_");
        switch (assetNumber) {
            case "first":
                elementTag.append("one");
                break;
            case "second":
                elementTag.append("two");
                break;
            case "third":
                elementTag.append("three");
                break;
            case "fourth":
                elementTag.append("four");
                break;
            case "fifth":
                elementTag.append("five");
                break;
        }

        if (Elements.elementPresent(elementTag.toString())) {
            Clicks.click(Elements.findElement(elementTag.toString()).findElement(By.tagName("img")));
        }
    }

    /**
     * Clicks the info and exclusions link
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I click on INFO and EXCLUSIONS link$")
    public void I_click_on_INFO_and_EXCLUSIONS_link() throws Throwable {
        Clicks.click("home.nav_banner");
        Navigate.switchWindow(1);
        Navigate.switchWindowClose();
    }

    /**
     * Selects the footer ad banner
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select footer Ad banner$")
    public void I_select_footer_Ad_banner() throws Throwable {
        Clicks.click("footer.goto_footer_banner");
        Navigate.switchWindow(1);
        Navigate.switchWindowClose();
    }

    /**
     * Selects the given brand on the category splash page
     *
     * @param brand brand to select
     * @throws Throwable if any exception occurs
     */
    @And("^I move to \"([^\"]*)\" from the available brands$")
    public void I_move_to_from_the_available_brands(String brand) throws Throwable {
        if (Elements.elementPresent("category_splash.brand_lists")) {
            for (WebElement brand_lists : Elements.findElements("category_splash.brand_lists")) {
                boolean is_name_link = false;
                for (WebElement brand_name : brand_lists.findElements(Elements.element("category_splash.brand_links_names"))) {
                    is_name_link = brand_name.getText().equalsIgnoreCase(brand);
                    if (is_name_link) {
                        Clicks.click(brand_name);
                        break;
                    }
                }
                if (is_name_link) {
                    break;
                }
            }
        } else {
            System.out.println("Unable to find left navigation link names");
        }
    }

    /**
     * Scrolls to the footer panel
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I scroll 'down' the page until reach bottom of footer panel$")
    public void I_scroll_down_the_page_until_reach_bottom_of_footer_panel() throws Throwable {
        Clicks.hoverForSelection("search_result.footer_bottom");
    }

    /**
     * Clicks the "back to top" button on search result page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select 'back to top' button$")
    public void I_select_back_to_top_button() throws Throwable {
        Clicks.click("search_result.back_to_top_button");
    }

    /**
     * Selects a random color for current product
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select random color swatch for the given product$")
    public void I_select_random_color_swatch_for_the_given_product() throws Throwable {
        ProductDisplay.selectRandomColor();
    }

    /**
     * Adds a valid or invalid offer to wallet
     *
     * @param validity invalid|valid
     * @throws Throwable if any exception occurs
     */
    @And("^I add an? (invalid|valid) offer to wallet$")
    public void I_add_an_invalid_offer_to_wallet(String validity) throws Throwable {
        switch (validity) {
            case "invalid":
                String invalid_offer = "ABC25";
                TextBoxes.typeTextbox("add_offer_dialog.offer_text", invalid_offer);
                Clicks.click("add_offer_dialog.save_btn");
                try {
                    assert Elements.elementPresent("add_offer_dialog.error_message");
                    Clicks.click("add_offer_dialog.close_btn");
                } catch (NoSuchElementException e) {
                    Assert.fail("Invalid offer error message did not display");
                }
                break;
            case "valid":
                try {
                    if (prodEnv()) {
                        JSONObject promoObject = TestUsers.getValidPromotion();
                        if (promoObject != null) {
                            String validPromo = promoObject.getString("promo_code");
                            TextBoxes.typeTextbox("add_offer_dialog.offer_text", validPromo);
                            Clicks.click("add_offer_dialog.save_btn");
                            if (Elements.elementPresent("add_offer_dialog.save_btn")) {
                                System.out.println("Unable to add promo code to wallet");
                                Clicks.click("add_offer_dialog.close_btn");
                            }
                        }
                    } else {
                        new MyWalletSteps().I_saved_omnichannel_offer_having_more_than_one_promo_code_in_wallet();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Assert.fail("getValidPromotion method returned null");
                }
                break;

        }
    }

    /**
     * Clicks the given button on my bwallet page
     *
     * @param button element name (should match element ID in page JSON file
     * @throws Throwable if any exception occurs
     */
    @And("^I click \"([^\"]*)\" button on my bwallet page$")
    public void I_click_link_on_my_account_page(String button) throws Throwable {
        switch (button) {
            case "add credit card":
                Clicks.click("my_bwallet.add_credit_card_btn");
                break;
            case "view more details":
                Clicks.click("my_bwallet.view_more_details_btn");
                break;
            default:
                Clicks.click("my_bwallet." + button.replace(" ", "_"));
        }
    }

    /**
     * Clicks save card without filling in any details
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I submit credit card without filling the details$")
    public void I_submit_credit_card_without_filling_the_details() throws Throwable {
        Clicks.click("credit_card_dialog.save_card");
    }

    /**
     * Adds a random saved credit card to bwallet
     * <p>
     * Data from "valid_cards.json" or credit card service if available
     * </p>
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add a credit card from my bwallet page$")
    public void I_add_a_credit_card_from_my_account_page() throws Throwable {
        CommonUtils.addCreditCardFromBWallet(null, null);
    }

    /**
     * Verifies the display of at least one offer on shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select the bwallet offer in shopping bag$")
    public void I_select_the_bwallet_offer_in_shopping_bag() throws Throwable {
        Wait.forPageReady();
        int size = Elements.findElements("shopping_bag.bwallet_offers").size();
        boolean displayed = false;
        for (int i = 0; i < size; i++) {
            if (Elements.findElements("shopping_bag.bwallet_offers").get(i).isDisplayed()) {
                Clicks.click(Elements.findElements("shopping_bag.bwallet_offers").get(i));
                displayed = true;
                break;
            }
        }
        if (!displayed) {
            Assert.fail("The offers are not eligible for the products in the shopping bag");
        }
    }

    /**
     * Views the given number of random products
     *
     * @param count number of products to view
     * @throws Throwable if any exception occurs
     */
    @When("^I view (\\d+) random products$")
    public void I_view_random_products(int count) throws Throwable {
        if (!onPage("category_browse", "search_result", "product_display")) {
            CommonUtils.navigateToRandomSubCategory();
        }
        ArrayList<String> visitedProducts = new ArrayList<>();
        while (count-- > 0) {
            I_select_a_random_product("member", null);
            while (visitedProducts.contains(Elements.getText("product_display.product_title"))) {
                retryAction(() -> {
                    Navigate.browserBack();
                    return !onPage("product_display");
                }, 0, "Could not navigate back to category browse page");
                I_select_a_random_product("member", null);
            }
            visitedProducts.add(Elements.getText("product_display.product_title"));
            retryAction(() -> {
                Navigate.browserBack();
                return !onPage("product_display");
            }, 0, "Could not navigate back to category browse page");
        }
    }

    /**
     * CLicks the given arrow on the recently viewed items panel
     *
     * @param arrow left|right
     * @throws Throwable if any exception occurs
     */
    @When("^I click on \"(left|right)\" arrow key inside Recently Viewed panel$")
    public void I_click_on_arrow_key_inside_Recently_Viewed_panel(String arrow) throws Throwable {
        RecentlyViewed.updateProducts();
        if (!Clicks.clickIfPresent("recently_viewed_items.scroll_" + arrow)) {
            Assert.fail("ERROR - APP: Cannot scroll to the " + arrow);
        }
        // wait for the scroll animation - can't find it programmatically but it always takes the same amount of time
        Utils.threadSleep(2000, null);
    }

    /**
     * Selects an item from recently viewed items panel
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select a recently viewed product$")
    public void I_select_a_recently_viewed_product() throws Throwable {
        scrollToLazyLoadElement("recently_viewed_items.item");
        // IE does not support recently viewed products
        if (ie()) {
            throw new Exceptions.SkipException("I_select_a_recently_viewed_product(): IE doesn't support RVI");
        }

        Clicks.clickRandomElement("recently_viewed_items.item");
    }

    /**
     * Adds a product to bag and selects the given option
     *
     * @param option checkout|continue shopping
     * @throws Throwable if any exception occurs
     */
    @When("^I add member product from PDP and select \"(checkout|continue shopping)\"$")
    public void I_add_member_product_from_PDP_and_select(String option) throws Throwable {
        ProductDisplay.addRandomMemberProductOnMasterPDP();
        if (option.equalsIgnoreCase("checkout")) {
            Clicks.clickWhenPresent("add_to_bag_dialog.master_add_to_bag_checkout");
        } else if (option.equalsIgnoreCase("continue shopping")) {
            Clicks.click("add_to_bag_dialog.master_add_to_bag_continue_shopping");
        }
    }

    /**
     * Refreshes the page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I refresh current page$")
    public void I_refresh_current_page() throws Throwable {
        Navigate.browserRefresh();
    }

    /**
     * Switches the website to domestic mode
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I go to US site$")
    public void I_go_to_US_site() throws Throwable {
        if (Elements.findElement("footer.goto_us_site").isDisplayed()) {
            Clicks.click("footer.goto_us_site");
            Wait.untilElementPresent("footer.us_flag");
            System.out.println("Changed current mode to domestic");
        } else {
            System.out.println("Already in domestic mode");
        }
    }

    /**
     * Empties the shopping bag
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I remove all items from the shopping bag$")
    public void i_remove_all_items_from_the_shopping_bag() throws Throwable {
        Navigate.visit("shopping_bag");
        ShoppingBag.emptyCurrentShoppingBag();
    }

    /**
     * Logs out from current user
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I logout$")
    public void i_logout() throws Throwable {
        Clicks.clickIfPresent("home.goto_sign_out_link");
    }

    /**
     * Verifies that ATB overlay is present
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be redirected to ATB overlay$")
    public void I_should_be_redirected_to_ATB_overlay() throws Throwable {
        Elements.elementShouldBePresent("panel.add_to_bag_dialog.add_to_bag_dialog");
    }

    /**
     * Verifies that there is a popup present with expected coupon code
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I should see a popup window with coupon code$")
    public void I_should_see_a_popup_window_with_coupon_code() throws Throwable {
        try {
            Wait.secondsUntilElementPresent("my_offers.get_savings_pass", 5);
            int windex = Navigate.findIndexOfWindow("/cms/deals");
            String winHandleBefore = MainRunner.getWebDriver().getWindowHandle();
            if (windex > 0) {
                WebDriver newPage = Navigate.switchWindow(windex);
                if (newPage.getTitle() != null) {
                    newPage.close();
                    MainRunner.getWebDriver().switchTo().window(winHandleBefore);
                } else {
                    Assert.fail("Popup window did not appear after clicking deal");
                }
            }
        } catch (TimeoutException e) {
            System.out.println("No savings passes present");
        }
    }

    /**
     * Verifies the display of member products on master pdp
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I should see member products listed$")
    public void I_should_see_member_products_listed() throws Throwable {
        Elements.elementShouldBePresent("product_display.member_products");
    }

    /**
     * Verifies the given recommendation panel type on the given page or overlay
     *
     * @param panel_position vertical|horizontal
     * @param page           pdp|zsr|AddToBag|shopping_bag|my_account|wishlist|order_confirmation
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see \"(vertical|horizontal)\" recommendation panel on (pdp|zsr|AddToBag|shopping_bag|my_account|wishlist|order_confirmation) (?:overlay|page)$")
    public void I_should_see_recommendation_panel_on_zsr_page(String panel_position, String page) throws Throwable {
        switch (page.toLowerCase()) {
            case "pdp":
                shouldBeOnPage("product_display");
                break;
            case "AddToBag":
                shouldBeOnPage("add_to_bag");
                break;
            case "shopping_bag":
                shouldBeOnPage("shopping_bag");
                break;
            case "my_account":
            case "my account":
                shouldBeOnPage("my_account");
                break;
            case "wishlist":
                shouldBeOnPage("wish_list");
                break;
            case "order_confirmation":
            case "order confirmation":
                shouldBeOnPage("order_confirmation");
                break;
            case "zsr":
                shouldBeOnPage("search_result");
                break;
        }
        Elements.elementShouldBePresent("recommendations." + panel_position + "_recommendations");
    }

    /**
     * Verifies the expected title of a popup window for social icons
     *
     * @param socialIconPopUp expected title
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see \"([^\"]*)\" title in the popup window$")
    public void I_should_see_title_in_the_popup_window(String socialIconPopUp) throws Throwable {
        if (onPage("radical_pdp") && !socialIconPopUp.matches("pinterest|email")) {
            // radical pdp only has pinterest icon, can't expect others to be there
            System.err.println("Can't Check for " + socialIconPopUp + " on radical pdp, does not exist");
            return;
        }
        int windowIndex = 0;
        switch (socialIconPopUp.toLowerCase()) {
            case "facebook":
                windowIndex = Navigate.findIndexOfWindow("Facebook");
                break;
            case "twitter":
                windowIndex = Navigate.findIndexOfWindow("Share a link on Twitter");
                break;
            case "pinterest":
                windowIndex = Navigate.findIndexOfWindow("catalog of ideas");
                // normally would use clickIfPresent, but this one takes a second to pop up
                // & the page doesn't change to a loading state
                if (Wait.untilElementPresent("product_display.close_popup_panel")) {
                    Clicks.click("product_display.close_popup_panel");
                }
                break;
            case "email":
                // cheat a little here because email doesn't pop up in another window
                windowIndex = Wait.untilElementPresent("product_display.email_panel") ? 1 : 0;
                Clicks.clickIfPresent("product_display.close_popup_panel");
                break;
            case "google_plus":
                windowIndex = Navigate.findIndexOfWindow("Google+");
                break;
        }
        if (windowIndex <= 0) {
            Assert.fail(socialIconPopUp + " is not displayed");
        }

        // don't have a window to close in email case
        if (!socialIconPopUp.equalsIgnoreCase("email")) {
            Navigate.switchWindow(1);
            Navigate.switchWindowClose();
            // close olapic template banner if present
            Clicks.clickIfPresent("product_display.social_banner_close");
        }
    }

    /**
     * Verifies that the promo code error message appeared on shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify the promo code validation error message appeared$")
    public void I_verify_the_promo_code_validation_error_message_appeared() throws Throwable {
        Wait.untilElementPresent("shopping_bag.error_message");
    }

    /**
     * Navigates to shopping bag page and makes sure it's empty
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to empty shopping bag page$")
    public void I_navigate_to_empty_shopping_bag_page() throws Throwable {
        String productsCount = Elements.getText("home.shopping_bag_item_count");
        productsCount = productsCount.replaceAll("\\(|\\)", "");
        Wait.forPageReady();
        if (productsCount.equals("0")) {
            Clicks.click("home.goto_shopping_bag");
        } else {
            i_remove_all_items_from_the_shopping_bag();
            Navigate.browserRefresh();
        }
    }

    /**
     * Verifies that recommendation panel is not displayed
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify recommendation panel is not displayed$")
    public void I_verify_recommendation_panel_is_not_displayed() throws Throwable {
        if (Elements.elementPresent("shopping_bag.vertical_recommendations_panel")) {
            Assert.fail("Recommendation Panel is Displaying for Empty Shopping Bag");
        }
    }

    /**
     * Selects "add a new card" on my wallet page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I click ADD a NEW CARD button$")
    public void I_click_ADD_a_NEW_CARD_button() throws Throwable {
        if (!Clicks.clickIfPresent("oc_my_wallet.add_credit_card")) {
            Assert.fail("Add a New CARD button is not displaying");
        }
    }

    /**
     * Adds a new credit card on my wallet page and makes it the default card
     *<p>
     *     Card data comes from "valid_cards.json" or credit card service when available
     *</p>
     * @throws Throwable if any exception occurs
     */
    @And("^I add a credit card to My Wallet as default card on My Wallet page$")
    public void I_add_a_credit_card_to_My_Wallet_as_default_card_on_My_Wallet_page() throws Throwable {
        MyWallet.addCard();
    }

    /**
     * Selects delete a random offer from my wallet and waits for the confirmation overlay to show up
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I click delete on random offer and wait for confirmation overlay$")
    public void I_click_delete_on_random_offer_and_wait_for_confirmation_overlay() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.delete_offers")) {
            Clicks.clickRandomElement("oc_my_wallet.delete_offers");
            Wait.untilElementPresent("oc_my_wallet.yes_delete_offer");
        } else {
            Assert.fail("No offers to delete");
        }
    }

    @And("^I add a valid offer$")
    public void I_add_a_valid_offer() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.special_redemption_codes")) {
            promoCode = Elements.findElement("oc_my_wallet.special_redemption_codes").getText();
        } else {
            promoCode = "X1A001IEZB17";
            TextBoxes.typeTextbox("add_offer_dialog.promo_code", promoCode);
            Clicks.click("add_offer_dialog.save_offer");
        }
    }

    /**
     * Confirms removal of an offer
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I confirm offer remove$")
    public void I_confirm_offer_remove() throws Throwable {
        if (!Clicks.clickIfPresent("oc_my_wallet.yes_delete_offer")) {
            Assert.fail("Confirm deletion of offer dialog is not displaying");
        }
    }

    /**
     * Adds a random product to bag
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add a random product to bag$")
    public void iAddARandomProductToBag() throws Throwable {
        CommonUtils.navigateToRandomProduct();
        I_add_product_to_my_bag_from_standard_PDP_Page();
    }

    /**
     * Selects a random available bops store in bops dialog and saves
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select available bops store and save details$")
    public void I_select_available_bops_store_and_save_details() throws Throwable {
        Assert.assertTrue("Unable to locate any available stores", Elements.elementPresent("change_pickup_store_dialog.search_results_section"));
        if (Elements.elementPresent("change_pickup_store_dialog.address_city")) {
            //old form
            if (Elements.elementPresent("change_pickup_store_dialog.select_store_span")) {
                Clicks.click("change_pickup_store_dialog.select_store_span");
                Clicks.click("change_pickup_store_dialog.save");
                Wait.untilElementNotPresent("product_display.find_in_store_overlay");
            } else {
                I_close_the_instore_Availability_popUp();
                Wait.forPageReady();
                Assert.fail("ERROR-DATA: Unable to locate any available bops stores");
            }
        } else {
            ChangePickupStoreDialog.selectFirstStore();
            Clicks.click("change_pickup_store_dialog.save");
        }
    }

    /**
     * Clicks "write a review" on pdp
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I click write a review on PDP$")
    public void iClickWriteAReview() throws Throwable {
        Clicks.clickLazyElement("product_display.write_a_review");
    }

    /**
     * Verifies that recommendation panel scroll correctly
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I should see scrolling for recommendation panel$")
    public void iShouldSeeScrollingForRecommendationPanel() throws Throwable {
        try {
            if (Elements.findElements("recommendations.vertical_recommendations").size() > 2) {
                Elements.elementShouldBePresent("recommendations.scroll_forward");
            }
        } catch (Exception e) {
            if (Elements.findElements("recommendations.horizontal_recommendations").size() > 2) {
                Elements.elementShouldBePresent("recommendations.scroll_forward");
            }
        }
    }

    /**
     * Navigates to a product with the given attributes
     *
     * @param productTrue  Properties expected to be true separated by either a space or the word "and"
     * @param productFalse Properties expected to be false separated by either a space or the word "and"
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to \"(.*)\"(?: that is not(?: an?)? \"(.*)\")? product PDP page$")
    public void iNavigateToPdp(String productTrue, String productFalse) throws Throwable {
        if (prodEnv()) {
            recentProduct = CommonUtils.navigateToRandomProduct(productTrue, productFalse);
        } else {
            //All the none production related promotional products need to be get from siteDB with their respective attributes
            switch (productTrue) {
                case "orderable and promo_code_eligible":
                    String[] productsWithPromoCode = new PromotionService().getPromoCodeForPromotionalProducts();
                    if (productsWithPromoCode == null) {
                        Assert.fail("ERROR-DATA: Promo-Code eligible promotions are not available in database!!");
                    }
                    promotionalProdId = productsWithPromoCode[1];
                    promotionCode = productsWithPromoCode[0];
                    CommonUtils.navigateDirectlyToProduct(promotionalProdId);
                    recentProduct = new Product(Integer.valueOf(promotionalProdId));
                    break;
                case "orderable and pwp":
                    //TODO
                    break;
                default:
                    recentProduct = CommonUtils.navigateToRandomProduct(productTrue, productFalse);
            }
        }
    }

    /**
     * Selects a random size for current product
     */
    @When("^I select available size of the product$")
    public void iSelectAvailableSize() {
        ProductDisplay.selectRandomSize();
    }

    /**
     * Selects the given alternate image on pdp
     *
     * @param alt_image first|second
     * @throws Throwable if any exception occurs
     */
    @And("^I select (first|second) alternative image on PDP(?: Page)?$")
    public void I_select_alternative_image(String alt_image) throws Throwable {
        Wait.untilElementPresent("product_display.alt_images");
        switch (alt_image) {
            case "first":
                if (Elements.elementPresent(By.cssSelector("#altImage_0"))) {
                    Clicks.click(By.cssSelector("#altImage_0"));
                } else {
                    Assert.fail("There is no alternative image available for selected product");
                }
                break;
            case "second":
                if (Elements.elementPresent(By.cssSelector("#altImage_1"))) {
                    Clicks.click(By.cssSelector("#altImage_1"));
                } else {
                    Assert.fail("More than one alternative images are not available for selected product");
                }
                break;
        }
    }

    /**
     * Selects a predefined orderable product
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select a predefined orderable random product$")
    public void I_select_a_predefined_orderable_random_product() throws Throwable {
        String selector = ("category_browse.product_thumbnails_container");
        String[] predefined_product_ids = {"78600", "22804", "22805", "86800"};
        List<WebElement> elements = Elements.findElements(selector);

        for (WebElement el : elements) {
            String id = el.getAttribute("id");
            if (id != null && ArrayUtils.contains(predefined_product_ids, id)) {
                Clicks.click(el);
                break;
            }
        }
    }

    /**
     * Replaces current product ID in url with given ID
     *
     * @param productID ID to navigate to
     * @throws Throwable if any exception occurs
     */
    @When("^I replace product ID with available \"([^\"]*)\" product ID$")
    public void iReplaceProductIDWithAvailableProductID(int productID) throws Throwable {
        shouldBeOnPage("product_display", "registry_pdp");
        try {
            String url = MainRunner.getCurrentUrl();
            String pattern = "ID=(.*)&";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(url);
            m.find();
            String new_url = url.replace(m.group(), "ID=" + productID + "&");
            Navigate.visit(new_url);
        } catch (Exception e) {
            Assert.fail("ERROR - DATA: Unable to append predefined prodID to URL" + e);
        }
    }
}
