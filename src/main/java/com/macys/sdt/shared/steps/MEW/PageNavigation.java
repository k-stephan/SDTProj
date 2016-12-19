package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.utils.Cookies;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.MEW.pages.ProductDisplay;
import com.macys.sdt.shared.actions.MEW.panels.GlobalNav;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PageNavigation extends StepUtils {

    @Then("^I should be in Search Landing page using mobile website$")
    public void I_should_be_in_Search_Landing_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("search_result");
    }

    @Then("^I should see the \"([^\"]*)\" Page$")
    public void I_should_see_the_page(String page) throws Throwable {
        shouldBeOnPage(page);
    }

    @Then("^I should be redirected to PDP page using mobile website$")
    public void I_should_be_redirected_to_PDP_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("product_display");
    }

    @Then("^I should be redirected to wishlist PDP using mobile website$")
    public void I_should_be_redirected_to_wishlist_PDP_using_mobile_website() throws Throwable {
        Assert.assertTrue("Not on MEW wishlist PDP", url().contains("/shop/product/?ID"));
    }

    @Then("^I should be redirected to ATB page using mobile website$")
    public void I_should_be_redirected_to_ATB_page_using_mobile_website() throws Throwable {
        Wait.forPageReady();
        shouldBeOnPage("add_to_bag");
    }

    @When("^I navigate to shopping bag page from add to bag page using mobile website$")
    public void I_navigate_to_shopping_bag_page_from_add_to_bag_page_using_mobile_website() throws Throwable {
        Assert.assertTrue("ERROR-ENV: Checkout button is not visible in add to bag panel", Elements.elementPresent("add_to_bag.checkout"));
        Clicks.click("add_to_bag.checkout");
        shouldBeOnPage("shopping_bag");
    }

    @When("^I navigate the global navigation menu as follows:$")
    public void I_navigate_the_global_navigation_menu_as_follows(List<String> table) throws Throwable {
        if (onPage("registry_home", "registry_manager")) {
            GlobalNav.openGlobalNav();
            table.forEach(GlobalNav::navigateOnGnByName);
        } else {
            table.forEach(gnName -> {
                GlobalNav.openGlobalNav();
                GlobalNav.navigateOnGnByName(gnName);
                closePopup();
            });
        }
        GlobalNav.closeGlobalNav();
        CommonUtils.closeStylistPopup();
    }

    @When("^I navigate back to pdp page from pick up store page$")
    public void I_navigate_back_to_pdp_page_from_pick_up_store_page() throws Throwable {
        Wait.untilElementPresent("change_pickup_store.back_button");
        Clicks.click("change_pickup_store.back_button");
        Assert.assertTrue("ERROR-ENV: Not navigated to the PDP", onPage("product_display"));
    }

    @Then("^I should be in mobile shopping bag$")
    public void I_should_be_in_mobile_shopping_bag() throws Throwable {
        shouldBeOnPage("shopping_bag");
    }

    @When("^I navigate to the sign-in page$")
    public void I_navigate_to_the_sign_in_page() throws Throwable {
        scrollToLazyLoadElement("home.goto_sign_in_link");
        Clicks.clickWhenPresent("home.goto_sign_in_link");
        Assert.assertTrue("ERROR-ENV: Not able to navigate to the sign_in page", Wait.untilElementPresent("sign_in.verify_page"));
        // shouldBeOnPage("sign_in");
    }

    @And("^I navigate to the create profile page$")
    public void I_navigate_to_the_create_profile_page() throws Throwable {
        GlobalNav.closeGlobalNav();
        Assert.assertTrue("ERROR-ENV: Create Account element is not visible", Elements.elementPresent(Elements.element("sign_in.create_account")));
        Clicks.click("sign_in.create_account");
        shouldBeOnPage("create_profile");
    }

    @And("^I select a recently viewed product using mobile website$")
    public void I_select_a_recently_viewed_product_using_mobile_website() throws Throwable {
        GlobalNav.navigateToRecentlyViewedProduct();
    }

    @Then("^I should be redirected to master PDP page in mobile website$")
    public void I_should_be_redirected_to_master_PDP_page_in_mobile_website() throws Throwable {
        shouldBeOnPage("product_display_master");
        Assert.assertTrue("ERROR-ENV: Unable to navigate product display page", ProductDisplay.isMasterMemberPage());
    }

    @And("^I select \"([^\"]*)\" from mobile registry home page$")
    public void I_select_from_mobile_registry_home_page(String mode) throws Throwable {
        GlobalNav.closeGlobalNav();
        if (onPage("registry_home")) {
            switch (mode) {
                case "create your registry":
                    Clicks.javascriptClick(Elements.element("registry_home.goto_create_registry"));
                    break;
            }
        } else {
            Assert.fail("User is currently not in Registry Home Page");
        }
    }

    @Then("^I should be navigated to the mobile my account page$")
    public void I_should_be_navigated_to_the_mobile_my_account_page() throws Throwable {
        shouldBeOnPage("my_account");
    }

    @And("^I navigate back to \"([^\"]*)\" page using mobile website$")
    public void I_navigate_back_to_page_using_mobile_website(String page_type) throws Throwable {
        switch (page_type.toLowerCase()) {
            case "home":
                Navigate.visit("home");
                break;
            case "my account":
                Navigate.visit("my_account");
                break;
            case "category browse":
                Navigate.browserBack();
               // Assert.assertTrue("ERROR-ENV: Not able to navigate to the " + page_type + ".", onPage("category_browse"));
                break;
            case "category splash":
                Navigate.browserBack();
             //   Assert.assertTrue("ERROR-ENV: Not able to navigate to the " + page_type + ".", onPage("category_splash"));
                break;
        }
    }

    @And("^I close the global navigation menu$")
    public void I_close_the_global_navigation_menu() throws Throwable {
        GlobalNav.closeGlobalNav();
    }

    @And("^I navigate to \"([^\"]*)\" category browse page using mobile website$")
    public void I_navigate_to_category_browse_page_using_mobile_website(String category_type) throws Throwable {
        switch (category_type) {
            case "women's Heel":
                Clicks.clickWhenPresent("category_splash.banner_link");
        }
    }

    @And("^I navigate back to home page using mobile website$")
    public void I_navigate_back_to_home_page_using_mobile_website() throws Throwable {
        if (Wait.untilElementPresent(Elements.element("home.header_image"))) {
            Clicks.click("home.header_image");
            shouldBeOnPage("home");
            System.out.print("Successfully navigated to the Home Page");
        } else {
            Navigate.visit("home");
        }
    }

    @And("^I navigate to \"([^\"]*)\" footer links using mobile website$")
    public void I_navigate_to_footer_links_using_mobile_website(String link_text) throws Throwable {
        if (Elements.elementPresent((Elements.element("home.footer")))) {
            switch (link_text) {
                case "customer service":
                    Clicks.javascriptClick(Elements.element("home.goto_contact_us"));
                    Navigate.switchWindow(1);
                    Navigate.switchWindowClose();
                    break;
                case "emails or texts":
                    Clicks.click(Elements.element("home.goto_email_text_signup"));
                    break;
                case "find a store":
                    Clicks.javascriptClick(Elements.element("home.find_a_store"));
                    break;
            }
        } else {
            Assert.fail("Footer is not visible");
        }
    }

    @Given("^I visit the mobile web site as a (guest|registered) user$")
    public void I_visit_the_mobile_web_site_as_a_registered_user(String registered) throws Throwable {
        Navigate.visit("home");
        pausePageHangWatchDog();
        // close popup
        Clicks.clickIfPresent("home.popup_close");

        closeMewTutorial();
        Thread.sleep(5000);
        if (registered.equals("registered")) {
            CommonUtils.signInOrCreateAccount();
            if (!prodEnv()) {
                new MyAccount().I_add_a_credit_card_from_my_account_page_using_mobile_website();
            }
            Navigate.visit("home");
            if (safari()) {
                new com.macys.sdt.shared.steps.website.ShopAndBrowse().i_remove_all_items_from_the_shopping_bag();
            }
        }
        Cookies.disableForeseeSurvey();
    }

    @Then("^I should be redirected to deals page using mobile website$")
    public void I_should_be_redirected_to_deals_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("deals_and_promotions");
    }

    @When("^I navigate to change country page using mobile website$")
    public void I_navigate_to_change_country_page_using_mobile_website() throws Throwable {
        if (Elements.elementPresent(Elements.element("home.change_country_link"))) {
            Clicks.javascriptClick(Elements.element("home.change_country_link"));
            Wait.untilElementPresent(Elements.element("change_country.header"));
        } else {
            Assert.fail("Change country link is not visible");
        }
    }

    @And("^I navigate to shopping bag page using mobile website$")
    public void I_navigate_to_shopping_bag_page_using_mobile_website() throws Throwable {
        try {
            Wait.untilElementPresent(Elements.element("header.my_bag"));
            Clicks.click(Elements.element("header.my_bag"));
        } catch (NoSuchElementException e) {
            System.out.print(e.getMessage());
            Assert.fail("Element is not visible on page");
        }
    }

    @Given("^I visit the mobile web site as a guest user in (domestic|iship|registry) mode$")
    public void I_visit_the_mobile_web_site_as_a_guest_user_in_mode(String mode) throws Throwable {
        I_visit_the_mobile_web_site_as_a_registered_user("guest");
        switch (mode.toLowerCase()) {
            case "domestic":
                break;
            case "registry":
                if (macys()) {
                    Navigate.visit("registry_home");
                } else {
                    GlobalNav.openGlobalNav();
                    GlobalNav.navigateOnGnByName("The Registry");
                    GlobalNav.navigateOnGnByName("Find");
                    GlobalNav.closeGlobalNav();
                }
                break;
            case "iship":
                I_navigate_to_change_country_page_using_mobile_website();
                Iship iship = new Iship();
                iship.I_change_country_to_using_mobile_website("India");
                iship.I_close_the_welcome_mat_if_it_s_visible_using_mobile_website();
                break;
        }

    }

    @Then("^I should be able to navigate using pagination functionality using mobile website$")
    public void I_should_be_able_to_navigate_using_pagination_functionality_using_mobile_website() throws Throwable {
        if (Wait.untilElementPresent(Elements.element("pagination.select_page_no"))) {
            String selectedValue = DropDowns.getSelectedValue(Elements.element("pagination.select_page_no"));
            Assert.assertNotNull("Not able to get page count", selectedValue);
            String pageCount = selectedValue.replace("page 1 of ", "");
            scrollToLazyLoadElement("pagination.next_page");
            Clicks.click("pagination.next_page");
            Assert.assertEquals("Not navigated to next page.", "page 2 of " + pageCount, DropDowns.getSelectedValue(Elements.element("pagination.select_page_no")));
            scrollToLazyLoadElement("pagination.previous_page");
            Clicks.click("pagination.previous_page");
            Assert.assertEquals("Not navigated to previous page.", "page 1 of " + pageCount, DropDowns.getSelectedValue(Elements.element("pagination.select_page_no")));
        }
    }

    @And("^I navigate to brand index page in (registry|iship|domestic) mode using mobile website$")
    public void I_navigate_to_brand_index_page_using_mobile_website(String mode) throws Throwable {
        switch (mode) {
            case "registry":
                scrollToLazyLoadElement("site_menu.site_menu_title");
                Clicks.click("site_menu.site_menu_title");
                Wait.untilElementPresent("site_menu.brand_index");
                Clicks.click("site_menu.brand_index");
                break;
            case "domestic":
            case "iship":
                GlobalNav.openGlobalNav();
                GlobalNav.navigateOnGnByName("ALL DESIGNERS");
                GlobalNav.closeGlobalNav();
        }
    }

    @When("^I navigate to dynamic landing page in (registry|iship|domestic) mode using mobile website$")
    public void I_navigate_to_dynamic_landing_page_using_mobile_website(String mode) throws Throwable {
        I_navigate_to_brand_index_page_using_mobile_website(mode);
        for (int i=0; i<5; i++) {
            if (mode.equals("registry")) {
                Clicks.clickRandomElement("brand_index.brand_alphabates");
            }
            Clicks.clickRandomElement("brand_index.brand_links", WebElement::isDisplayed);
            if (onPage("dynamic_landing")) {
                break;
            }
            Navigate.browserBack();
        }
        shouldBeOnPage("dynamic_landing");
    }
}
