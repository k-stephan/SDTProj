package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.MEW.pages.CreateProfileMEW;
import com.macys.sdt.shared.actions.MEW.pages.MyOffers;
import com.macys.sdt.shared.actions.MEW.pages.MyWallet;
import com.macys.sdt.shared.actions.MEW.pages.LoyaltyEnrollment;
import com.macys.sdt.shared.actions.MEW.panels.GlobalNav;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyAccount extends StepUtils {
    @And("^I create a new profile in mobile site$")
    public void I_create_a_new_profile_in_mobile_site() throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("Cannot make accounts on prod!");

        TestUsers.clearCustomer();
        CreateProfileMEW.createProfile(TestUsers.getCustomer(null));
        if (!onPage("my_account")) {
            Assert.fail("New Profile is not created");
        }
        CreateProfileMEW.closeSecurityAlertPopUp();
        TestUsers.currentEmail = TestUsers.getCustomerInformation().getUser().getProfileAddress().getEmail();
        TestUsers.currentPassword = TestUsers.getCustomerInformation().getUser().getLoginCredentials().getPassword();
        if (Elements.elementPresent("my_account.one_time_add_card_overlay")) {
            com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile.closeSecurityAlertPopUp();
            Clicks.javascriptClick(Elements.element("my_account.add_card_overlay_no_thanks_button"));
        }
        I_add_a_credit_card_from_my_account_page_using_mobile_website();
        if (onPage("my_account")) {
            Navigate.visit("my_account");
        } else {
            Navigate.browserBack();
        }
    }

    @And("^I sign out from my current mobile site profile$")
    public void I_sign_out_from_my_current_mobile_site_profile() throws Throwable {
        Assert.assertTrue("ERROR-ENV: Not able to navigate to the sign_in page", Elements.elementPresent(Elements.element("my_account.goto_sign_out_link")));
        Clicks.click(Elements.element("my_account.goto_sign_out_link"));
        Wait.untilElementNotPresent("my_account.goto_sign_out_link");
        // sign out doesn't like to stick
        if (Elements.elementPresent("my_account.goto_sign_out_link")) {
            Clicks.click(Elements.element("my_account.goto_sign_out_link"));
            Assert.assertTrue("ERROR-ENV: sign out link is still visible", Elements.elementPresent(Elements.element("home.goto_sign_in_link")));
        }
    }

    @When("^I sign in to my existing profile using mobile website$")
    public void I_sign_in_to_my_existing_profile_using_mobile_website() throws Throwable {
        CommonUtils.signInOrCreateAccount();
        Navigate.visit("home");
    }

    @And("^I navigate to my profile page using mobile website$")
    public void I_navigate_to_my_profile_page_using_mobile_website() throws Throwable {
        Clicks.click("my_account.my_profile");
        pausePageHangWatchDog();
        if (!onPage("my_profile")) {
            Assert.fail("Not navigated to the my profile page");
        }
        resumePageHangWatchDog();
    }

    @And("^I add a credit card from my wallet page using mobile website$")
    public void I_add_a_credit_card_from_my_account_page_using_mobile_website() throws Throwable {
        if (!onPage("my_account")) {
            GlobalNav.openGlobalNav();
            GlobalNav.navigateOnGnByName("My Account");
            GlobalNav.closeGlobalNav();
        }
        I_navigate_to_the_wallet_page_using_mobile_website();
        MyWallet.deleteCreditCard();
        CommonUtils.addCreditCardFromBWallet(null, null);
    }

    @Then("^I should be redirected to store page using mobile website$")
    public void I_should_be_redirected_to_store_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("stores");
    }

    @When("^I search for \"([^\"]*)\" as a \"([^\"]*)\" in stores page using mobile website$")
    public void I_search_using_as_a_in_stores_page_using_mobile_website(String search_input, String search_criteria) throws Throwable {
        switch (search_criteria) {
            case "zipcode":
                TextBoxes.typeTextbox(Elements.element("stores.search_box"), search_input);
                Clicks.click("stores.search_near_me");
                break;
        }
    }

    @When("^I type \"([^\"]*)\" into the search bar of the stores page using mobile website$")
    public void I_type_into_the_search_bar_of_stores_page_using_mobile_website(String search_input) {
        TextBoxes.typeTextbox(Elements.element("stores.search_box"), search_input);
    }

    @Then("^I should see auto-complete suggestion store names$")
    public void I_should_see_auto_complete_suggestion_store_names() throws Throwable {
        if (Elements.elementPresent(Elements.element("stores.autocomplete_container"))) {
            System.out.print("Autocomplete suggestion store names displayed");
        } else {
            Assert.fail("Autocomplete suggestion store names not displayed");
        }
    }

    @When("^I select a auto-complete suggestion store name$")
    public void I_select_a_auto_complete_suggestion_store_name() throws Throwable {
        if (Wait.untilElementPresent(Elements.element("stores.autocomplete_text"))) {
            Clicks.clickRandomElement("stores.autocomplete_text");
            Wait.untilElementPresent(Elements.element("stores.store_list"));
            System.out.print("All the stores loaded successfully");
        } else {
            Assert.fail("Autocomplete suggestion store names not displayed");
        }
    }

    @And("^I select \"([^\"]*)\" store name$")
    public void I_select_store_name(String store_name) throws Throwable {
        if (Wait.untilElementPresent(Elements.element("stores.store_list"))) {
            store_name = '"' + store_name + '"';
            Clicks.click(Elements.paramElement("stores.select_store", store_name));
        } else {
            Assert.fail("Facet container not loaded");
        }
    }

    @Then("^I should be redirected to store details panel using mobile website$")
    public void I_should_be_redirected_to_store_details_panel_using_mobile_website() throws Throwable {
        if (Elements.elementPresent(Elements.element("stores.store_address"))) {
            System.out.print("Store Details panel loaded successfully");
        } else {
            Assert.fail("Store Details panel not loaded");
        }
    }

    @When("^I select \"([^\"]*)\" from store details page using mobile website$")
    public void I_select_from_store_details_page_using_mobile_website(String element) throws Throwable {
        switch (element) {
            case "Directions":
                Clicks.click("stores.directions");
                Navigate.switchWindow(1);
                Navigate.switchWindowClose();
                break;
        }
    }

    @And("^I navigate to the wallet page using mobile website$")
    public void I_navigate_to_the_wallet_page_using_mobile_website() throws Throwable {
        Clicks.clickIfPresent("my_account.add_card_overlay_no_thanks_button");
        if (Wait.untilElementPresent("my_account.mywallet_link")) {
            Clicks.click("my_account.mywallet_link");
            Wait.forPageReady();
            shouldBeOnPage("oc_my_wallet");
        }
    }

    @When("^I select a random \"([^\"]*)\" from deals & promotions page using mobile website$")
    public void I_select_a_random_from_deals_promotions_page_using_mobile_website(String deal) throws Throwable {
        boolean expected = false;
        List<WebElement> offer = Elements.findElements("offer_details.offers_container");
        int size = offer.size();
        if (size > 0) {
            for (WebElement anOffer : offer) {
                Clicks.click(anOffer);
                switch (deal) {
                    case "offer":
                        if (Wait.untilElementPresent("offer_details.shop_now") && !Elements.findElement("offer_details.offer_promocode").getText().contains("no promo code")) {
                            expected = true;
                        }
                        break;
                    case "coupon":
                        if (Wait.untilElementPresent("offer_details.add_to_wallet")) {
                            expected = true;
                        }
                }
                if (expected) {
                    break;
                } else {
                    Clicks.clickWhenPresent("offer_details.back");
                }
            }
        } else{
            Assert.fail("ERROR-DATA: Cannot find offers and coupons");
        }
    }

    @And("^I select \"([^\"]*)\" link from offers and details panel using mobile website$")
    public void I_select_link_from_offers_and_details_panel_using_mobile_website(String offer_link) throws Throwable {
        switch (offer_link) {
            case "shop now":
                if (Wait.untilElementPresent("deals_and_promotions.shop_now")) {
                    Clicks.click("offer_details.shop_now");
                    Clicks.clickIfPresent("offer_details.back");
                } else {
                    Assert.fail("ERROR-DATA: shop now button is not visible");
                }
                break;
            case "Add To Wallet":
                if (Wait.untilElementPresent("deals_and_promotions.add_to_wallet")) {
                    Clicks.click("offer_details.add_to_wallet");
                    Clicks.click("offer_details.back");
                } else {
                    Assert.fail("ERROR-DATA: add to wallet button is not visible");
                }
        }
    }

    @And("^I add fully_enrolled_usl id on my account page using mobile website$")
    public void I_add_fully_enrolled_usl_id_on_my_account_page_using_mobile_website() throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("I_add_fully_enrolled_usl_id_on_my_account_page_using_mobile_website()");

        String plenti_id = TestUsers.getEnrolledUslId().getPlentiId();
        TextBoxes.typeTextbox("my_account.usl_id", plenti_id);
        Clicks.click("my_account.apply_usl_id_button");
        Wait.untilElementPresent("my_account.go_to_my_XXXXXX");
        Assert.assertTrue("ERROR-ENV: Unable to add usl id to profile", Elements.elementPresent("my_account.go_to_my_XXXXXX"));
    }

    @And("^I remove USL ID from shipping and payment page using mobile website$")
    public void I_remove_USL_ID_from_shipping_and_payment_page_using_mobile_website() throws Throwable {
        Wait.untilElementPresent("shipping_payment_signed_in.remove_usl_button");
        Assert.assertTrue("ERROR-ENV: No USL ID added to the profile", Elements.elementPresent("shipping_payment_signed_in.remove_usl_button"));
        Clicks.click("shipping_payment_signed_in.remove_usl_button");
        Wait.untilElementPresent("shipping_payment_signed_in.lookUpLoyaltyId");
        Assert.assertTrue("ERROR-ENV: Unable to remove USL ID from shipping and payment page", Elements.elementPresent("shipping_payment_signed_in.lookup_link"));

    }


    @And("^I add a valid offer to my wallet using mobile website$")
    public void I_add_a_valid_offer_to_my_wallet_using_mobile_website() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.available_offers")) {
            MyOffers.deleteOffers();
            MyOffers.addOffers();
        } else {
            MyOffers.addOffers();
        }
    }

    @And("^I remove a valid offer to my wallet using mobile website$")
    public void I_remove_a_valid_offer_to_my_wallet_using_mobile_website() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.available_offers")) {
            MyOffers.deleteOffers();
        } else {
            MyOffers.addOffers();
            MyOffers.deleteOffers();
        }
    }

    @And("^I lookup plenti id using valid usl phone number on payment page using mobile website$")
    public void I_lookup_plenti_id_using_valid_usl_phone_number_on_payment_page_using_mobile_website() throws Throwable {
        String phone_no = TestUsers.getEnrolledUslId().getUslPhone();
        Wait.untilElementPresent("shipping_payment_signed_in.lookup_link");
        Clicks.click("shipping_payment_signed_in.lookup_link");
        TextBoxes.typeTextbox("shipping_payment_signed_in.usl_phone_number", phone_no);
        Clicks.click("shipping_payment_signed_in.usl_search_phone");
        Wait.untilElementPresent("shipping_payment_signed_in.remove_usl_button");
        Assert.assertTrue("ERROR-ENV: Unable to lookup plenti id usign phone number", Elements.elementPresent("shipping_payment_signed_in.remove_usl_button"));
    }

    @When("^I sign in to my same profile using mobile website$")
    public void I_sign_in_to_same_profile_using_mobile_website() throws Throwable {
        if (!prodEnv()) {
            Clicks.click("home.goto_sign_in_link");
            TextBoxes.typeTextbox(Elements.element("sign_in.email"), TestUsers.currentEmail);
            TextBoxes.typeTextbox(Elements.element("sign_in.password"), TestUsers.currentPassword);
            Clicks.click(Elements.element("sign_in.verify_page"));
            Assert.assertTrue("ERROR-ENV: Unable to sign in to the application", Elements.elementPresent(Elements.element("footer.goto_sign_out_link")));
        }
    }

    @When("^I create a new profile in mobile site without closing the add card overlay$")
    public void I_create_a_new_profile_in_mobile_site_without_closing_the_add_card_overlay() throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("Cannot make accounts on prod!");

        TestUsers.clearCustomer();
        CreateProfileMEW.createProfile(TestUsers.getCustomer(null));
        if (!onPage("my_account")) {
            Assert.fail("New Profile is not created");
        }
        CreateProfileMEW.closeSecurityAlertPopUp();
        Wait.untilElementPresent("my_account.one_time_add_card_overlay");
    }

    @Then("^I verify the My Account Pages are rendered properly using mobile website$")
    public void I_verify_the_My_Account_Pages_are_rendered_properly_using_mobile_website(List<String> pageNames) throws Throwable {
        shouldBeOnPage("my_account");
        for (String pageName : pageNames) {
            I_navigate_to_page_from_my_account_page_using_mobile_website(pageName);
            shouldBeOnPage(pageName.replace(" ", "_"));
            Navigate.visit("my_account");
        }
    }

    @And("^I navigate to \"([^\"]*)\" page from my account page using mobile website$")
    public void I_navigate_to_page_from_my_account_page_using_mobile_website(String pageName) throws Throwable {
        shouldBeOnPage("my_account");
        switch (pageName.toLowerCase()) {
            case "my profile":
                Clicks.click("my_account.my_profile");
                break;
            case "my preferences":
                Clicks.click("my_account.my_preferences");
                break;
            case "my address book":
                Clicks.click("my_account.my_address_book");
                break;
            case "oc my wallet":
                Clicks.click("my_account.mywallet_link");
                break;
            case "wish list":
                if (macys()) {
                    Clicks.click("my_account.my_lists");
                    Clicks.click("my_account.my_list");
                } else {
                    GlobalNav.openGlobalNav();
                    GlobalNav.navigateOnGnByName("MENU");
                    GlobalNav.navigateOnGnByName("WISH LIST");
                    GlobalNav.closeGlobalNav();
                }
                break;
            case "order status":
                if (macys()) {
                    Clicks.click("my_account.my_order");
                    Clicks.click("my_account.view_order_history_link");
                } else {
                    Clicks.click("site_menu.site_menu_title");
                    Clicks.click("site_menu.order_status");
                }
                break;
            case "furniture mattress status":
                Clicks.click("my_account.my_order");
                Clicks.click("my_account.furniture_mattress_status");
                break;
            case "gift card balance":
                Clicks.click("my_account.gift_card_balance");
                break;
        }
    }

    @Then("^I should see below credit services links in my account page:$")
    public void iShouldSeeBelowCreditServicesLinksInMobileMyaccountPage(List<String> credit_links) throws Throwable {
        shouldBeOnPage("my_account");
        for (String credit_link : credit_links) {
            Assert.assertTrue(credit_link + " not present on my account page.", Elements.elementPresent("my_account." + credit_link));
        }
    }

    @And("^I should be navigated to below respective credit services pages using mobile website:$")
    public void iShouldBeNavigatedToBelowRespectiveCreditServicesPagesUsingMobileWebsite(List<HashMap<String, String>> list) throws Throwable {
        for (Map set : list) {
            shouldBeOnPage("my_account");
            Clicks.click("my_account." + set.get("credit_link"));
            shouldBeOnPage(set.get("landing_page").toString());
            Navigate.visit("my_account");
        }
    }

    @Then("^I enroll in to the loyalty program using mobile website as a signed in user$")
    public void I_enroll_in_to_the_loyalty_program_using_mobile_website_as_a_user() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("iEnrollInToTheLoyaltyProgramAsAUserUsingMobileWebsite()");
        }
        new LoyaltyEnrollment().signedInUserLoyaltyEnrollment(TestUsers.getCustomer(null));
        shouldBeOnPage("loyalty_enrollment_confirmation");
    }

    @Then("^I (should|should not) see one time add card overlay and its components using mobile website$")
    public void iShouldSeeOneTimeAddCardOverlayAndItsComponentsUsingMobileWebsite(String condition) throws Throwable {
        String add_card_elements[] = {"one_time_add_card_overlay", "add_card_overlay_add_card_button", "add_card_overlay_close_button"};
        Wait.untilElementPresent("my_account.one_time_add_card_overlay");
        if (condition.equals("should")) {
            for (String element : add_card_elements) {
                Assert.assertTrue(element + " element is not displayed on my account page!!", Elements.elementPresent("my_account." + element));
            }
        } else {
            Assert.assertFalse("Add credit card overlay is displayed on my account page", Elements.elementPresent("my_account.one_time_add_card_overlay"));
        }
    }
}
