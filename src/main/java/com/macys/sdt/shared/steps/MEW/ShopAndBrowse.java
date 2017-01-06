package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.shared.actions.MEW.pages.Browse;
import com.macys.sdt.shared.actions.MEW.pages.ProductDisplay;
import com.macys.sdt.shared.actions.MEW.panels.MEWLeftFacet;
import com.macys.sdt.shared.actions.MEW.panels.RecentlyViewed;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.macys.sdt.shared.utils.CommonUtils.selectRandomProductMEW;

public class ShopAndBrowse extends StepUtils {

    public static Product recentProduct;

    @When("^I directly add an available and orderable product \"([^\"]*)\" to my bag in mobile site$")
    public void I_directly_add_an_available_and_orderable_product_to_my_bag(String avilable_prodcut_id) throws Throwable {
        CommonUtils.navigateDirectlyToProduct(avilable_prodcut_id);
        I_add_product_to_my_bag_from_standard_PDP_page();

        if (onPage("add_to_bag"))
            Clicks.click("add_to_bag.checkout");
        else if (Elements.elementPresent("add_to_bag_dialog.add_to_bag_checkout"))
            Clicks.click("add_to_bag_dialog.add_to_bag_checkout");
        else
            Clicks.click("product_display.member_atb_checkout");

        System.out.println("Sucessfuly added product");
    }

    @And("^I select the bwallet offer in shopping bag using mobile website$")
    public void I_select_the_bwallet_offer_in_shopping_bag() throws Throwable {
        Wait.forPageReady();
        Clicks.clickIfPresent("shopping_bag.apply_offer");
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

    @And("^I add product to my bag from standard PDP page$")
    public void I_add_product_to_my_bag_from_standard_PDP_page() throws Throwable {
        boolean addedToBag = false;
        Assert.assertFalse("ERROR - DATA : Product ( "+ (recentProduct == null ? "" : String.valueOf(recentProduct.id)) + " ) is unavailable on product display page!!", !Elements.elementPresent("product_display.add_to_bag_button") && Elements.elementPresent("product_display.availability_error"));
        try {
            int retries = 5;
            pausePageHangWatchDog();
            for (int count = 0; count < retries && !addedToBag; count++) {
                try {
                    com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ProductDisplay.selectRandomColor();
                    com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ProductDisplay.selectRandomSize();
                    Clicks.click("product_display.add_to_bag_button");
                    if (!Elements.elementPresent("add_to_bag_dialog.add_to_bag_dialog"))
                        Clicks.clickIfPresent("product_display.add_to_bag_button");

                    addedToBag = ProductDisplay.addedToBag();
                    if (MainRunner.debugMode) {
                        System.out.println("IsProductAddedToBag:" + addedToBag + ", Add to bag retry count:" + (count + 1));
                    }
                } catch (Exception e) {
                    System.err.println("Exception while adding product:" + e.getMessage());
                }
            }
            Wait.untilElementPresent("add_to_bag_dialog.add_to_bag_dialog");
            if (!Elements.elementPresent("add_to_bag_dialog.add_to_bag_dialog"))
                Clicks.clickIfPresent("product_display.technical_error");
            if (isErrorPaneVisible())
                Clicks.click("home.popup_close");
            resumePageHangWatchDog();
        } catch (IllegalArgumentException | NoSuchElementException e) {
            System.err.println("Error while adding to bag: " + e);
        } finally {
            if (!addedToBag) {
                Wait.untilElementNotPresent("product_display.add_to_bag_button");
                if (macys())
                    Assert.assertFalse("ERROR - DATA : Given item is unavailable!!", Elements.elementPresent(By.className("css-tooltip")) && Elements.getText(By.className("css-tooltip")).contains("this item is unavailable"));
                Assert.assertTrue("Unable to add product to bag", ProductDisplay.addedToBag());
            }

        }
    }
    @When("^I select a random (member|master|member_alternate_image|master_alternate_image) product using mobile website(?: with (customer ratings))?$")
    public void I_select_a_random_product_using_mobile_website(String prod_type, String hasRating) throws Throwable {
        boolean found = false;
        int i = 0, max = 20;

        while (!found && i++ < max) {
            selectRandomProductMEW(hasRating != null, prod_type.toLowerCase().contains("master"));
            switch (prod_type.toLowerCase()) {
                case "member":
                    found = !ProductDisplay.isMasterMemberPage();
                    break;
                case "master":
                    found = ProductDisplay.isMasterMemberPage();
                    break;
                case "member_alternate_image":
                    found = Wait.untilElementPresent("product_display.alt_images")
                            && !ProductDisplay.isMasterMemberPage();
                    break;
                case "master_alternate_image":
                    found = Wait.untilElementPresent("product_display.alt_image")
                            && ProductDisplay.isMasterMemberPage();
                    break;
            }
            if (!found) {
                Navigate.browserBack();
            }
            if (!onPage("product_display", "registry_pdp")) {
                found = false;
            }
        }
        if (!found) {
            Assert.fail("Failed to find " + prod_type + " product after " + max + " tries.");
        }
    }

    @When("^I click on \"(left|right)\" arrow key inside Recently Viewed panel using mobile website$")
    public void I_click_on_arrow_key_inside_Recently_Viewed_panel(String arrow) throws Throwable {
        RecentlyViewed.updateProducts();
        if (!Clicks.clickIfPresent("recently_viewed_items.scroll_" + arrow))
            Assert.fail("ERROR - APP: Cannot scroll to the " + arrow);
        // wait for the scroll animation - can't find it programmatically but it always takes the same amount of time
        Utils.threadSleep(2000, null);
    }

    @When("^I view (\\d+) random (member|master|member_alternate_image|master_alternate_image) products using mobile website$")
    public void I_view_random_member_products(int count,String prod_type) throws Throwable {
        int i = 0;

        if (!onPage("category_browse", "search_result")) {
            Assert.fail("No products to select");;
        }

        while(i < count){
            I_select_a_random_product_using_mobile_website(prod_type,null);
            Navigate.browserBack();
            i++;
            System.out.println("Navigate to "+i+" product");
        }
    }

    @When("^I select a predefined orderable random member product using mobile website$")
    public void I_select_a_predefined_orderable_random_member_product_using_mobile_website() throws Throwable {
        /*
              78600: Lenox Eternal Gold 5-Piece Place Setting
              22805: Lenox Eternal Gold 4-Piece Hostess Set
              1494:  Villeroy & Boch Dinnerware, French Garden Dinner Plate
              86800: Lenox Vintage Jewel 5 Piece Place Setting
              1276:  Lenox Federal Platinum All Purpose Bowl
         */
        String[] predefined_product_ids = {"78600", "22804", "1494", "86800", "1275"};
        boolean searchResults = Elements.elementPresent("search_result.search_results_thumbnail_wrapper");
        String selector = (searchResults ? "search_result" : "category_browse") // page name
                + "." + "product_thumbnail_list";
        List<WebElement> elements = Elements.findElements(Elements.element(selector));
        WebElement selected = null;

        if (elements == null || elements.isEmpty()) {
            throw new NoSuchElementException("No elements found with selector: " + selector);
        }

        for (WebElement el: elements) {
            String id = el.getAttribute("data-product_id");
            if (id != null && ArrayUtils.contains(predefined_product_ids, id) || MainRunner.batchMode) {
                selected = el.findElement(By.xpath(".//div/img"));
                break;
            }
        }

        if (selected == null) {
            Assert.fail("Failed to find  a predefined orderable random member product");
        }
        Clicks.click(selected);
    }

    @When("^I select a predefined orderable random master product using mobile website$")
    public void I_select_a_predefined_orderable_random_master_product_using_mobile_website() throws Throwable {
        String[] predefined_product_ids = {"78600", "22805", "1494", "86800", "1275"};
        boolean searchResults = Elements.elementPresent("search_result.search_results_thumbnail_wrapper");
        String selector = (searchResults ? "search_result" : "category_browse") // page name
                + "." + "product_thumbnail_list";
        List<WebElement> elements = Elements.findElements(Elements.element(selector));
        WebElement selected = null;

        if (elements == null || elements.isEmpty()) {
            throw new NoSuchElementException("No elements found with selector: " + selector);
        }

        for (WebElement el: elements) {
            String id = el.getAttribute("data-product_id");
            if (id != null && ArrayUtils.contains(predefined_product_ids, id)) {
                selected = el.findElement(By.xpath(".//div/img"));
                break;
            }
        }

        if (selected == null) {
            Assert.fail("Failed to find  a predefined orderable random member product");
        }
        Clicks.click(selected);
    }

    @And("^I select \"([^\"]*)\" from \"([^\"]*)\" page using mobile website$")
    public void I_select_from_page_using_mobile_website(String toSelect, String page_type) throws Throwable {
        switch (page_type) {
            case "category browse":
                new Browse().sortBy(toSelect);
                break;
            default:
                System.out.println("unsupported page");
                break;
        }
    }

    @And("^I select \"([^\"]*)\" facet on left nav using mobile website$")
    public void I_select_facet_on_left_nav_using_mobile_website(String facet_name) throws Throwable {
        Wait.forPageReady();
        // We have no better way to wait for this animation
        Utils.threadSleep(1000, null);
        //mew bcom and mcom have too different facet selection
        if (macys()) {
            String element = onPage("category_browse") ? "category_browse.sort_by" : "search_result.filter_by_select";
            if (!Clicks.clickWhenPresent(element))
                Assert.fail("Unable to find facets on current page");
        }else{
            boolean isCollapsed = Elements.findElement(By.cssSelector("#b-j-show-facets-btn")).getText().equalsIgnoreCase("show more");
            if (isCollapsed) {
                Clicks.click("left_facet.show_more_facets");
            }
        }
        MEWLeftFacet.selectFacetOnLeftNav(facet_name);
    }

    @And("^I select \"([^\"]*)\" sub facet on left nav using mobile website$")
    public void I_select_sub_facet_on_left_nav_using_mobile_website(String sub_facet_name) throws Throwable {
        MEWLeftFacet.selectSubFacetOnLeftNav(sub_facet_name);
    }

    @And("^I confirm selected facets using mobile website$")
    public void I_confirm_selected_facets_using_mobile_website() throws Throwable {
        MEWLeftFacet.confirmFacets();
    }

    @And("^I input \"([^\"]*)\" as zip code in bops facet and select a store$")
    public void I_input_as_zip_code_in_bops_facet_and_select_a_store(String zipcode) throws Throwable {
        if (Wait.untilElementPresent("left_facet.bops_facet_value")) {
            Clicks.randomJavascriptClick("left_facet.bops_facet_value");
            return;
        }
        Assert.assertTrue("ERROR-ENV: zip code text box not present", Elements.elementPresent("left_facet.bops_zip_code"));
        TextBoxes.typeTextbox("left_facet.bops_zip_code", zipcode);
        DropDowns.selectByValue("change_pickup_store_dialog.search_distance", "100");
        Clicks.javascriptClick("left_facet.bops_search");
        Wait.secondsUntilElementPresent(("left_facet.bops_facet_value"), 50);
        if (Elements.elementPresent("left_facet.bops_error"))
            Assert.fail("Unable to find stores");

        if (Elements.elementPresent("left_facet.bops_facet_value"))
            Clicks.randomJavascriptClick("left_facet.bops_facet_value");

    }

    @And("^I registry filter as follows:$")
    public void I_registry_filter_as_follows(List<String> tbl) throws Throwable {
        Clicks.clickIfPresent("header.close_browser_upgrade");
        tbl.forEach(gnName -> Clicks.click(Elements.paramElement("category_browse.registry_filter_by", gnName)));
    }

    @Then("^I should be on mobile browse page$")
    public void I_should_be_on_mobile_browse_page() throws Throwable {
        shouldBeOnPage("category_browse");
    }

    @When("^I select \"([^\"]*)\" miles in bops facet change store dialog using mobile website$")
    public static void I_select_miles_in_bops_facet_change_store_dialog_using_mobile_website(String distance) throws Throwable {
        DropDowns.selectByValue("change_pickup_store_dialog.search_distance", distance);
    }

    @Then("^I should see sort by functionality with below options using mobile website:$")
    public void I_should_see_sort_by_functionality_with_below_options_using_mobile_website(List<String> sortByOptions) throws Throwable {
        Wait.forPageReady();
        Boolean isCategoryBrowse = onPage("category_browse");
        String pageName = isCategoryBrowse ? "category_browse" : "search_result";
        String productCount = Elements.getText(pageName + ".total_products");
        if (macys()) {
            Clicks.click(isCategoryBrowse ? "category_browse.sort_by" : "search_result.filter_by_select");
        }
        Wait.secondsUntilElementPresent(pageName + ".sort_by_select", 10);
        List<String> sortByOptionsUI = DropDowns.getAllValues(pageName + ".sort_by_select");
        Assert.assertTrue("Sort by options are not displayed correctly!!", (sortByOptionsUI.size() == sortByOptions.size()));
        for (String option : sortByOptions) {
            Assert.assertTrue("Sort by (" + option + ") option is not displayed in page!!", CommonUtils.matchSimilarSortBy(sortByOptionsUI, option));
        }
        DropDowns.selectByText(pageName + ".sort_by_select", sortByOptionsUI.get(new Random().nextInt(sortByOptionsUI.size())));
        if (macys()) {
            Clicks.click("left_facet.apply");
        }
        Wait.forLoading(By.id("mb-j-spinner"));
        Wait.forPageReady();
        Wait.untilElementPresent(pageName + ".total_products");
        Assert.assertEquals("Sort by functionality is not working properly!!", productCount, Elements.getText(pageName + ".total_products"));
    }

    @And("^I verify sort by functionality in registry mode using mobile website:$")
    public void I_verify_sort_by_functionality_in_registry_mode_using_mobile_website(List<String> sortByOptions) throws Throwable {
        Wait.forPageReady();
        String page = (onPage("search_result") ? "search_result" : "category_browse");
        String productCount = Elements.getText(page + ".total_products");
        Wait.secondsUntilElementPresent(page + ".sort_by_button", 10);
        List<String> sortByOptionsUI = Elements.findElements(page + ".sort_by_button").stream().map(WebElement::getText).collect(Collectors.toList());
        Assert.assertTrue("Sort by options are not displayed correctly!!", (sortByOptionsUI.size() == sortByOptions.size()));
        for (String option : sortByOptionsUI) {
            Assert.assertTrue("Sort by (" + option + ") option is not displayed in page!!", CommonUtils.matchSimilarSortBy(sortByOptionsUI, option));
        }
        Clicks.clickRandomElement(page + ".sort_by_button");
        Wait.forPageReady();
        Assert.assertEquals("Sort by functionality is not working properly!!", productCount, Elements.getText(page + ".total_products"));
    }

    @And("^I navigate to random browse page from site menu using mobile website$")
    public void I_navigate_to_random_browse_page_from_site_menu_using_mobile_website() throws Throwable {
        scrollToLazyLoadElement("site_menu.site_menu_title");
        Clicks.click("site_menu.site_menu_title");
        Wait.untilElementPresent("site_menu.shop_all_products");
        Clicks.click("site_menu.shop_all_products");
        Wait.untilElementPresent("site_menu.category_names");
        Clicks.clickRandomElement("site_menu.category_names");
        shouldBeOnPage("category_browse");
    }

    @Then("^I should see recommendation panel on pdp page using mobile website$")
    public void I_should_see_recommendation_panel_on_pdp_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("product_display");
        Elements.elementShouldBePresent("recommendations.recommendations_products");
    }

    @When("I select first recommended product from pdp page using mobile website")
    public void I_select_recommended_product_from_pdp_page_using_mobile_website() throws Throwable {
        if (Elements.anyPresent("product_display.recommended_product")) {
            Clicks.javascriptClick("product_display.recommended_product");
            shouldBeOnPage("product_display");
        } else {
            Assert.fail("Unable to click recommended product from horizontal pros panel");
        }
    }

    @When("^I select random asset from mew home page$")
    public void I_select_random_asset_from_mew_home_page() throws Throwable {
        if (Elements.elementPresent("home.home_asset")) {
            Clicks.click(Elements.findElement("home.home_asset"));
        } else {
            Assert.fail("Unable to find home page assets");
        }
    }

    @When("^I click show more to expand facet panel using mobile website$")
    public void I_click_show_more_to_expand_facet_panel_using_mobile_website() throws Throwable {
        //expand show more button from browse/search result page
        Clicks.clickIfPresent("left_facet.show_more_facets");
        Wait.until(() -> Elements.getText("left_facet.show_more_facets").equalsIgnoreCase("show less"));
    }

    @And("^I navigate to registry \"([^\"]*)\" browse page from site menu using mobile website$")
    public void I_navigate_to_registry_browse_page_from_site_menu_using_mobile_website(String pageType) throws Throwable {

        try {
            scrollToLazyLoadElement("site_menu.site_menu_title");
            Clicks.click("site_menu.site_menu_title");
            Wait.untilElementPresent("site_menu.shop_all_products");
            Clicks.click("site_menu.shop_all_products");
            Wait.untilElementPresent("site_menu.category_names");
            Clicks.clickElementByText("site_menu.category_names", pageType);
        } catch (Exception e) {
            Assert.fail("Element not present " + e);
        }
        shouldBeOnPage("category_browse");
    }

    @And("^I add \"([^\"]*)\" quantity to my bag from mobile standard PDP Page$")
    public void I_add_quantity_to_my_bag_from_mobile_standard_pdp_page(String quantity) throws Throwable {
        shouldBeOnPage("product_display");
        ProductDisplay.selectQuantity(quantity);
    }

    @And("^I select \"([^\"]*)\" in sort by drop down on designer page using mobile website$")
    public void I_select_in_sort_by_drop_down_on_designer_page_using_mobile_website(String toSelect) throws Throwable {
        Wait.forPageReady();
        Clicks.clickWhenPresent("brand_index.sort_by_select");
        Wait.forLoading("brand_index.sort_by_option");
        DropDowns.selectByText("brand_index.sort_by_option", toSelect);
    }

}
