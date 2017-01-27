package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Cookies;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.db.models.CampaignService;
import com.macys.sdt.shared.actions.website.mcom.pages.home.Home;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.CategorySplash;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;
import java.util.Random;


public class PageNavigation extends StepUtils {

    private com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.ShopAndBrowse shopAndBrowse;

    /**
     * Navigates to the home page, clears cookies, and closes all popups that appear
     *
     * @throws Throwable if any exception occurs
     */
    @Given("^I visit the web site as a guest user$")
    public void I_visit_the_web_site_as_a_guest_user() throws Throwable {
        Navigate.visit("home");
        MyAccountSteps.iClearAllTheCookies();
        Clicks.clickIfPresent("home.popup_close");
        Wait.forPageReady();
        closeBcomLoyaltyPromotionVideoOverlay();
        closeBcomPopup();
        Cookies.disableForeseeSurvey();
    }

    /**
     * Follows the same steps as "I visit the web site as a guest user" and then logs into an account or creates one
     *
     * @throws Throwable if any exception occurs
     */
    @Given("^I visit the web site as a registered user$")
    public void I_visit_the_web_site_as_a_registered_user() throws Throwable {
        I_visit_the_web_site_as_a_guest_user();
        CommonUtils.signInOrCreateAccount();
        if (!CommonUtils.isNewProfileCreated) {
            new CheckoutSteps().iRemoveAllItemsInShoppingBag();
            new MyAccountSteps().iNavigateToMyAccountPage();
        }
        CreateProfile.closeSecurityAlertPopUp();
        Cookies.disableForeseeSurvey();
    }

    /**
     * Navigates to the home page
     *
     * @throws Throwable if any exception occurs
     */
    @Given("^I goto Home page$")
    public void i_goto_home_page() throws Throwable {
        Navigate.visit(MainRunner.url);
    }

    /**
     * Checks that the browser is on the given page
     *
     * @param page name of expected page
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see the \"([^\"]*)\" page$")
    public void I_should_see_the_page(String page) throws Throwable {
        shouldBeOnPage(page.replace(" ", "_"));
    }

    /**
     * Mouses over the given category in the top navigation bar
     *
     * @param menu category to mouse over
     * @throws Throwable if any exception occurs
     */
    @When("^I mouse over \"([^\"]*)\" category from top navigation$")
    public void I_mouse_over_category_from_top_navigation(String menu) throws Throwable {
        if (tablet()) {
            new Home().selectMainCategory(menu);
        }
        CreateProfile.closeSecurityAlertPopUp();
        // We should not visit site home page here, as we are using this step for all pages like registry home, other browser pages.
//        Navigate.visit(MainRunner.url);
//        Wait.forPageReady();
//        closeBcomLoyaltyPromotionVideoOverlay();
//        closeBcomPopup();
        By menuEl = Elements.paramElement("home.flyout_category", menu.toUpperCase());
        Clicks.hoverForSelection(menuEl);
    }

    /**
     * Mouses over a random category in the top navigation bar
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I mouse over random category from top navigation$")
    public void I_mouse_over_random_category_from_top_navigation() throws Throwable {
        List<String> categories = new Home().getAllMainCategoryNames();
        I_mouse_over_category_from_top_navigation(categories.get(new Random().nextInt(categories.size())));
    }

    /**
     * Selects the given subcategory from a visible flyout
     * <p>
     * This step requires a visible flyout BEFORE calling it
     * </p>
     * @param subCategory subcategory to select
     */
    @When("^I select \"([^\"]*)\" subcategory from flyout menu")
    public void iSelectSubcategoryFromFlyoutMenu(String subCategory) {
        if (tablet()) {
            new Home().selectSubCategory(subCategory);
        }
        // Adding extra space in link text for edge browser
        subCategory = (edge() ? (subCategory + " ") : subCategory);
        if (!Wait.untilElementPresent(By.linkText(subCategory))) {
            Assert.fail("Could not click fly-out \"" + subCategory + "\"");
        }
        if (ie()) {
            Clicks.javascriptClick(By.linkText(subCategory));
        } else {
            Clicks.click(By.linkText(subCategory));
        }
    }

    /**
     * Clicks the "create your registry" button on registry home page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select \"Create Your Registry\"$")
    public void iSelectCreateRegistry() throws Throwable {
        if (chrome() && macys() && !Elements.elementPresent("registry_home.goto_create_registry")) {
            Clicks.clickArea("alt", "create your registry. couples start here!");
        } else if (edge() || firefox()) {
            Clicks.javascriptClick("registry_home.goto_create_registry");
        } else {
            Clicks.click("registry_home.goto_create_registry");
        }
        Wait.forPageReady();
    }

    /**
     * Visits the deals and promotions page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I visit the deals and promotions page$")
    public void I_visit_the_deals_and_promotions_page() throws Throwable {
        i_goto_home_page();
        Clicks.click("my_account.goto_deals_promotions");
        CreateProfile.closeSecurityAlertPopUp();
    }

    /**
     * Navigates to the given sign in page
     *
     * @param mode "site" or "registry" (case insensitive)
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to signin page of \"([^\"]*)\" mode$")
    public void I_navigate_to_signin_page_of_mode(String mode) throws Throwable {
        switch (mode.toLowerCase()) {
            case "site":
                Clicks.click("home.goto_sign_in_link");
                CommonUtils.closeIECertError();
                break;
            case "registry":
                Clicks.click("home.goto_wedding_registry");
                Clicks.click("registry_home.manage_box");
                Wait.untilElementPresent("registry_home.sign_in_email");
                break;
        }
    }

    /**
     * Navigates to the given footer link
     *
     * @param footer_link link text of footer link
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to the \"([^\"]*)\" page from footer$")
    public void I_navigate_to_the_page_from_footer(String footer_link) throws Throwable {
        switch (footer_link.toLowerCase()) {
            case "ways to shop":
                if (Elements.elementPresent("home.goto_ways_to_shop")) {
                    Clicks.click("home.goto_ways_to_shop");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
            case "stores":
                if (Elements.elementPresent("home.goto_our_stores")) {
                    Clicks.click("home.goto_our_stores");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
            case "outlet":
                if (Elements.elementPresent("home.goto_outlets")) {
                    Clicks.click("home.goto_outlets");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
            case "order status":
                if (Elements.elementPresent("home.goto_order_status")) {
                    Clicks.click("home.goto_order_status");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
            case "pay bill":
                if (Elements.elementPresent("home.goto_credit_pay_bill_online")) {
                    Clicks.click("home.goto_credit_pay_bill_online");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
            case "apply & learn more":
                if (Elements.elementPresent("home.goto_credit_apply_now")) {
                    Clicks.click("home.goto_credit_apply_now");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
            case "credit services":
                if (Elements.elementPresent("home.goto_credit_services")) {
                    Clicks.click("home.goto_credit_services");
                } else {
                    Assert.fail(footer_link + " is not found in the footer section");
                }
                break;
        }
    }

    /**
     * Navigates to the shopping pag page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to shopping bag page$")
    public void I_navigate_to_shopping_bag_page() throws Throwable {
        if (safari()) {
            Wait.secondsUntilElementPresent("header.quick_bag", 15);
        }
        Clicks.click("header.quick_bag");
    }

    /**
     * Navigates to the given FOB
     *
     * @param fob FOB to select
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to \"([^\"]*)\" FOB on the left navigation$")
    public void I_navigate_to_FOB_on_the_left_navigation(String fob) throws Throwable {
        if (Elements.elementPresent("category_splash.brand_links")) {
            int link_size = Elements.findElements("category_splash.brand_links").size();
            for (int i = 0; i < link_size; i++) {
                boolean is_link = Elements.findElements("category_splash.brand_links").get(i).getText().equalsIgnoreCase(fob);
                if (is_link) {
                    Clicks.click(Elements.findElements("category_splash.brand_links").get(i));
                    break;
                }
            }
        } else {
            System.out.println("Unable to find left navigation links");
        }
    }

    /**
     * Verifies the browser is on the PDP
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be redirected to PDP(?: page)?$")
    public void I_should_be_redirected_to_PDP_page() throws Throwable {
        if (safari()) {
            Wait.secondsUntilElementPresent("product_display.verify_page", 20);
        }
        shouldBeOnPage("product_display");
    }

    /**
     * Verifies the browser is on the add to bag page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be redirected to ATB page$")
    public void I_should_be_redirected_to_ATB_page() throws Throwable {
        if (!onPage("add_to_bag")) {
            if (!Elements.elementPresent("product_display.add_to_bag_dialog") &&
                    !Elements.elementPresent("product_display.master_add_to_bag_dialog")) {
                Assert.fail("Not on ATB page OR panel");
            }
        }
    }

    /**
     * Verifies the browser is on the domestic home page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be navigated to domestic home page as a sign in user$")
    public void I_should_be_navigated_to_domestic_home_page_as_a_sign_in_user() throws Throwable {
        if (!onPage("home")) {
            Assert.fail("Not navigated to the domestic home page");
        }
    }

    /**
     * Verifies the browser is on the search results
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be in Search Landing page$")
    public void I_should_be_in_Search_Landing_page() throws Throwable {
        shouldBeOnPage("search_result");
    }

    /**
     * Navigates to the change country page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to international context page$")
    public void I_navigate_to_international_context_page() throws Throwable {
        if (safari()) {
            Wait.secondsUntilElementPresent("header_and_footer.goto_change_country", 10);
        }
        Clicks.click("header_and_footer.goto_change_country");
    }

    /**
     * Clicks the find out more link on header panel
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I click find out more link on header panel$")
    public void I_click_find_out_more_link_on_header_panel() throws Throwable {
        Wait.untilElementPresent("home.find_out_more");
        Clicks.click("home.find_out_more");
    }

    /**
     * Navigates to the given category page
     *
     * @param arg1 category to visit
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to \"([^\"]*)\" category page$")
    public void I_navigate_to_category_page(String arg1) throws Throwable {
        new Home().selectMainCategory(arg1);
        //   shouldBeOnPage("category_splash");
    }

    /**
     * Selects the given featured category
     *
     * @param category category to select
     * @throws Throwable if any exception occurs
     */
    @And("^I click on thumbnail \"([^\"]*)\" on featured categories$")
    public void I_click_on_thumbnail_on_featured_categories(String category) throws Throwable {
        if (!Elements.elementPresent("category_splash.featured_categories")) {
            Clicks.click(CategorySplash.selectFeaturedCategory(category));
        } else {
            Assert.fail("ERROR - ENV: featured categories panel is not visible..... ");
        }
    }

    /**
     * Selects the given link in the header
     *
     * @param link link text of header link
     * @throws Throwable if any exception occurs
     */
    @When("^I click on \"([^\"]*)\" link in the header$")
    public void I_click_on_link_in_the_header(String link) throws Throwable {
        switch (link.toLowerCase()) {
            case "sign in":
                Clicks.click("home.goto_guest_sign_in");
                CommonUtils.closeIECertError();
                break;
            case "my account":
                Clicks.click("home.goto_guest_my_account");
                CommonUtils.closeIECertError();
                break;
            case "stores":
                Clicks.click("home.stores");
                CommonUtils.closeIECertError();
                break;
            default:
                Clicks.click(By.linkText(link));
                break;
        }
    }

    /**
     * Navigates the browser back one page and expects the given page to be visible
     *
     * @param page_type expected page
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate back to \"([^\"]*)\" page$")
    public void I_navigate_back_to_page(String page_type) throws Throwable {
        if (page_type.equalsIgnoreCase("home")) {
            Navigate.visit("home");
        } else {
            Navigate.browserBack();
            if (safari() || ie()) {
                if (page_type.equals("OH")) {
                    Clicks.click("home.goto_order_status");
                }
            }
            page_type = page_type.replace(" ", "_");
            if (Elements.element(page_type + ".url") != null) {
                shouldBeOnPage(page_type);
            }
        }
    }

    /**
     * Navigates to the given category and subcategory
     *
     * @param subcategory subcategory to visit
     * @param category    category to visit
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to the \"([^\"]*)\" browse page under \"([^\"]*)\"$")
    public void I_navigate_to_the_browse_page_under(String subcategory, String category) throws Throwable {
        Home homePage = new Home();
        homePage.selectMainCategory(category);
        homePage.selectSubCategory(subcategory);
    }

    /**
     * Navigates to the given footer link
     *
     * @param link link text
     * @throws Throwable if any exception occurs
     */
    @When("^I Navigate to \"([^\"]*)\" footer links$")
    public void I_Navigate_to_footer_links(String link) throws Throwable {
        Clicks.click(By.linkText(link));
        if (link.equals("catalogs")) {
            Clicks.clickWhenPresent("home.verify_page");
        }
    }

    /**
     * Clicks the given store from store search results
     *
     * @param link store to click
     * @throws Throwable if any exception occurs
     */
    @And("^I click \"([^\"]*)\" link in a store from store results$")
    public void I_click_link_in_a_store_from_store_results(String link) throws Throwable {
        if (link.equalsIgnoreCase("more arrow")) {
            Clicks.clickWhenPresent("stores.more");
        } else {
            Assert.fail("This method does not yet support " + link);
        }
    }

    /**
     * Clicks the given link on map popup
     * <p>
     * The only supported option is "directions"
     * </p>
     *
     * @param link text of link to click on
     * @throws Throwable if any exception occurs
     */
    @And("^I click \"([^\"]*)\" link from a map popup$")
    public void I_click_link_from_a_map_popup(String link) throws Throwable {
        if (link.equalsIgnoreCase("directions")) {
            Clicks.click("stores.directions");
            Navigate.switchWindow(1);
            Navigate.switchWindowClose();
        }
    }

    /**
     * Navigates to the create profile page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to create profile page$")
    public void I_navigate_to_create_profile_page() throws Throwable {
        Clicks.click("sign_in.create_profile");
        if (safari()) {
            Wait.secondsUntilElementPresent("create_profile.verify_page", 15);
        }
        if (!onPage("create_profile")) {
            Assert.fail("Not navigated to the create profile page");
        }

    }

    /**
     * Selects "continue checkout" on add to bag page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to shopping bag page from add to bag page$")
    public void I_navigate_to_shopping_bag_page_from_add_to_bag_page() throws Throwable {
        CreateProfile.closeSecurityAlertPopUp();
        if (Elements.elementPresent("add_to_bag_dialog.add_to_bag_dialog")) {
            Clicks.javascriptClick("add_to_bag_dialog.add_to_bag_checkout");
        } else if (Elements.elementPresent("add_to_bag_dialog.master_add_to_bag_dialog")) {
            Clicks.click("add_to_bag_dialog.master_add_to_bag_checkout");
        } else {
            Clicks.click("add_to_bag.checkout");
        }
    }

    /**
     * Verifies that the browser is on the my account page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be navigated to My Account Page$")
    public void I_should_be_navigated_to_My_Account_Page() throws Throwable {
        shouldBeOnPage("my_account");
    }

    /**
     * Verifies that the browser is on the USL loyalty home page
     *
     * @throws Throwable if any exception occurs
     */
    @Given("^I am on the USL loyalty home page$")
    public void I_am_on_the_USL_loyalty_home_page() throws Throwable {
        // Now we have new USL home page in qa environment which is pointing to production, So we are directly visit USL sign in page instead of USL home.
        if (!signedIn()) {
            Navigate.visit("usl_sign_in");
        } else {
            Clicks.hoverForSelection("my_account.goto_my_account");
            Wait.untilElementPresent("my_account.goto_my_plenti");
            Clicks.click("my_account.goto_my_plenti");
        }
        if (!onPage("usl_home, usl_sign_in".split(", "))) {
            Assert.fail("Not navigated to USL home page");
        }
    }

    /**
     * Verifies the browser is on the order review page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be on order review page$")
    public void I_should_be_on_order_review_page() throws Throwable {
        Wait.secondsUntilElementPresent("order_review.verify_page", 15);
        shouldBeOnPage("order_review, responsive_order_review_section".split(", "));
    }

    /**
     * Visits the web site then goes to the specified mode
     *
     * @param mode_name domestic|iship|registry
     * @throws Throwable if any exception occurs
     */
    @Given("^I visit the web site as a guest user in \"(domestic|iship|registry)\" mode$")
    public void I_visit_web_site_as_a_guest_user_in_mode(String mode_name) throws Throwable {
        I_visit_the_web_site_as_a_guest_user();
        switch (mode_name.toLowerCase()) {
            case "domestic":
                break;
            case "iship":
                I_navigate_to_international_context_page();
                new ShopAndBrowse().I_change_country_to("a random country");
                break;
            case "registry":
                new Registry().I_navigate_to_registry_home_page();
                break;
        }
        System.out.println("User visited web site in " + mode_name);
    }

    /**
     * Navigates to the given page in the given site mode
     *
     * @param page_name "search results" or "dynamic landing" or "browse" or "brand index"
     * @param mode_name "domestic" or "iship" or "registry"
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to (search results|dynamic landing|browse|brand index) page in \"(domestic|iship|registry)\" mode$")
    public void I_navigate_to_page_in_mode(String page_name, String mode_name) throws Throwable {
        shopAndBrowse = new com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.ShopAndBrowse();
        switch (page_name.toLowerCase()) {
            case "search results":
                String searchItemText = mode_name.equalsIgnoreCase("registry") ? "sheets" : "shoes";
                shopAndBrowse.searchForAnItem(searchItemText);
                Wait.forPageReady();
                break;
            case "dynamic landing":
                shopAndBrowse.navigateToRandomCategoryWithPopularSearchLink(8);
                break;
            case "browse":
                shopAndBrowse.navigateToRandomCategoryBrowsePage(8);
                break;
            case "brand index":
                Home home = new Home();
                home.selectMainCategory(macys() || mode_name.equalsIgnoreCase("registry") ? "BRANDS" : "DESIGNERS");
                break;
        }
        closeAlert();
    }

    /**
     * Verifies that the given sort by options are visible in the sort by drop down
     *
     * @param sortByList list of expected sort by options
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see sort by functionality with below options:$")
    public void I_should_see_sort_by_functionality_with_below_options(List<String> sortByList) throws Throwable {
        shopAndBrowse = new com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.ShopAndBrowse();
        List<String> sortByOptions;
        if (safari()) {
            Wait.secondsUntilElementPresent("pagination.sort_by", 15);
        }
        if (bloomingdales()) {
            String defaultText = Elements.getText("pagination.sort_by");
            sortByOptions = DropDowns.getAllCustomValues("pagination.sort_by", "pagination.sort_by_options");
            sortByOptions.add(0, defaultText);
        } else {
            sortByOptions = DropDowns.getAllValues("pagination.sort_by");
        }
        if (!(sortByOptions.size() == sortByList.size())) {
            Assert.fail("sort by list options are not displayed correctly!!");
        }

        boolean foundMatch;
        for (String option : sortByList) {
            //Observed that, In website, Sort by options values are varying for products and thus failing assertions
            //Example: Feature file has "Price: Low to High", where as in Website, it is listed as "Price: (low to high)"
            foundMatch = CommonUtils.matchSimilarSortBy(sortByOptions, option);
            if (!foundMatch) {
                Assert.fail("sort by (" + option + ") option is not displayed in page!!");
            }
        }
        int productCount = shopAndBrowse.getProductCount();
        if (bloomingdales()) {
            sortByOptions.remove(0);
        }
        shopAndBrowse.sortByValue(sortByOptions.get(new Random().nextInt(sortByOptions.size())));
        if (!(productCount == shopAndBrowse.getProductCount())) {
            Assert.fail("Sort by functionality is not working properly!!");
        }
    }

    /**
     * Verifies that the search page navigation buttons work
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be able to navigate using pagination functionality$")
    public void I_should_be_able_to_navigate_using_pagination_functionality() throws Throwable {
        shopAndBrowse = new com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.ShopAndBrowse();
        int pageCount = shopAndBrowse.getPageCount();
        if (pageCount > 1) {
            int currentPageNumber = shopAndBrowse.getCurrentPageNumber();
            shopAndBrowse.gotoPageNumber(currentPageNumber + 1);
            if (shopAndBrowse.getCurrentPageNumber() != (currentPageNumber + 1)) {
                Assert.fail("Pagination functionality is not working properly!!");
            }
            currentPageNumber = shopAndBrowse.getCurrentPageNumber();
            shopAndBrowse.gotoPageNumber(currentPageNumber - 1);
            if (shopAndBrowse.getCurrentPageNumber() != (currentPageNumber - 1)) {
                Assert.fail("Pagination functionality is not working properly!!");
            }
        }
    }

    /**
     * Verifies that the browser is on the brand index page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be navigated to brand index page$")
    public void I_should_be_navigated_to_brand_index_page() throws Throwable {
        if (!onPage("designer_static")) {
            Assert.fail("User is not navigated to brand index page!!");
        }
    }

    /**
     * Navigates to the order details page using footer link
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to order details page from footer$")
    public void I_navigate_to_order_details_page() throws Throwable {
        if (!Elements.elementPresent("footer.goto_order_status")) {
            Navigate.visit("home");
            Wait.forPageReady();
            Wait.secondsUntilElementPresent("footer.goto_order_status", safari() ? 20 : 10);
        }
        Clicks.click("footer.goto_order_status");
        if (safari()) {
            Wait.secondsUntilElementPresent("order_status.verify_page", 10);
        }
        Assert.assertTrue("Not Navigate to Order Details Page", onPage("order_status"));
    }

    /**
     * Visits the web site and starts a loyalty campaign based on given data
     *
     * @param userType     "guest" or "registered"
     * @param moneyType    "mMoney", "bMoney", or "ICWMoney"
     * @param campaignType "earn" or "burn"
     * @throws Throwable if any exception occurs
     */
    @Given("^I visit the web site as a (guest|registered) user in (mMoney|bMoney|ICWMoney) (earn|burn) period$")
    public void I_visit_the_web_site_as_a_guest_user_in_mMoney_earn_period(String userType, String moneyType, String campaignType) throws Throwable {
        pausePageHangWatchDog();
        CampaignService.setCampaignName(moneyType);
        Map<String, Boolean> mbMoneyCampaignDetails = CampaignService.getAllCampaignPeriods();
        if (campaignType.equals("earn") && mbMoneyCampaignDetails.get("earn").equals(false)) {
            CampaignService.updateCampaignDetailsInDatabase(moneyType.contains("ICW") ? "ICWMEarn" : (macys() ? "MEarn" : "BEarn"));
        } else if (campaignType.equals("burn") && mbMoneyCampaignDetails.get("redeem").equals(false)) {
            CampaignService.updateCampaignDetailsInDatabase(moneyType.contains("ICW") ? "ICWMRedeem" : (macys() ? "MRedeem" : "BRedeem"));
        } else if (mbMoneyCampaignDetails.get(campaignType).equals(false)) {
            CampaignService.updateCampaignDetailsInDatabase(campaignType);
        }
        CampaignService.clearAllMbmoneyRelatedCaches();
        resumePageHangWatchDog();
        if (userType.equals("guest")) {
            I_visit_the_web_site_as_a_guest_user();
        } else {
            new MyAccountSteps().iVisitTheWebSiteAsARegisteredUserWithCheckoutEligibleAddress();
        }
    }


    /**
     * Selects a random recently viewed product on PDP
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select a recently viewed product in product display page$")
    public void I_select_a_recently_viewed_product_in_product_display_page() throws Throwable {
        Clicks.clickRandomElement("recently_viewed_items.item");
    }

    /**
     * Navigates to a random product
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to a random product$")
    public void iNavigateToARandomProduct() throws Throwable {
        CommonUtils.navigateToRandomProduct();
    }

    /**
     * Navigates to a random category splash page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to random category splash page$")
    public void I_navigate_to_random_category_splash_page() throws Throwable {
        CommonUtils.navigateToRandomCategory();
    }
}
