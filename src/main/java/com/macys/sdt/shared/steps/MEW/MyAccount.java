package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.shared.actions.MEW.pages.*;
import com.macys.sdt.shared.actions.MEW.panels.GlobalNav;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyAccount extends StepUtils {

    /**
     * Creates a new profile
     *
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Signs out from currently signed in profile
     *
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Signs in to existing profile or creates a new one
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I sign in to my existing profile using mobile website$")
    public void I_sign_in_to_my_existing_profile_using_mobile_website() throws Throwable {
        CommonUtils.signInOrCreateAccount();
        Navigate.visit("home");
    }

    /**
     * Navigates to the my profile page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to my profile page using mobile website$")
    public void I_navigate_to_my_profile_page_using_mobile_website() throws Throwable {
        Clicks.click("my_account.my_profile");
        shouldBeOnPage("my_profile");
        resumePageHangWatchDog();
    }

    /**
     * Adds a credit card to my wallet on my wallet page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add a credit card from my wallet page using mobile website$")
    public void I_add_a_credit_card_from_my_account_page_using_mobile_website() throws Throwable {
        Wait.forPageReady();
        if ((!onPage("my_account")) && (!onPage("my_bwallet"))) {
            GlobalNav.openGlobalNav();
            GlobalNav.navigateOnGnByName("My Account");
            GlobalNav.closeGlobalNav();
        }
        I_navigate_to_the_wallet_page_using_mobile_website();
        //Check before attempt to delete a CC
        if (Elements.elementPresent("oc_my_wallet.cc_container"))
            MyWallet.deleteCreditCard();

        CommonUtils.addCreditCardFromBWallet(null, null);
    }

    /**
     * Verifies that you are on store page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be redirected to store page using mobile website$")
    public void I_should_be_redirected_to_store_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("stores");
    }

    /**
     * Searches for store, by given search input and criteria in stores page
     *
     * @param search_input to search for
     * @param search_criteria to search by
     * @throws Throwable if any exception occurs
     */
    @When("^I search for \"([^\"]*)\" as a \"([^\"]*)\" in stores page using mobile website$")
    public void I_search_using_as_a_in_stores_page_using_mobile_website(String search_input, String search_criteria) throws Throwable {
        switch (search_criteria) {
            case "zipcode":
                TextBoxes.typeTextbox(Elements.element("stores.search_box"), search_input);
                Clicks.click("stores.search_near_me");
                break;
        }
    }

    /**
     * Type given text into the search box of the stores pag
     *
     * @param search_input text to type in search box
     */
    @When("^I type \"([^\"]*)\" into the search bar of the stores page using mobile website$")
    public void I_type_into_the_search_bar_of_stores_page_using_mobile_website(String search_input) {
        TextBoxes.typeTextbox(Elements.element("stores.search_box"), search_input);
    }

    /**
     * Verifies store auto complete suggestions are visible
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see auto-complete suggestion store names$")
    public void I_should_see_auto_complete_suggestion_store_names() throws Throwable {
        if (Elements.elementPresent(Elements.element("stores.autocomplete_container"))) {
            System.out.print("Autocomplete suggestion store names displayed");
        } else {
            Assert.fail("Autocomplete suggestion store names not displayed");
        }
    }

    /**
     * Selects random option from store auto complete suggestions
     *
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Selects given store from stores list
     *
     * @param store_name store to select
     * @throws Throwable if any exception occurs
     */
    @And("^I select \"([^\"]*)\" store name$")
    public void I_select_store_name(String store_name) throws Throwable {
        if (Wait.untilElementPresent(Elements.element("stores.store_list"))) {
            store_name = '"' + store_name + '"';
            Clicks.click(Elements.paramElement("stores.select_store", store_name));
        } else {
            Assert.fail("Facet container not loaded");
        }
    }

    /**
     * Verifies store details panel is visible
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be redirected to store details panel using mobile website$")
    public void I_should_be_redirected_to_store_details_panel_using_mobile_website() throws Throwable {
        if (Elements.elementPresent(Elements.element("stores.store_address"))) {
            System.out.print("Store Details panel loaded successfully");
        } else {
            Assert.fail("Store Details panel not loaded");
        }
    }

    /**
     * Selects given element on stores details page
     *
     * @param element element to select
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Navigates to the my wallet page from the my account page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to the wallet page using mobile website$")
    public void I_navigate_to_the_wallet_page_using_mobile_website() throws Throwable {
        Clicks.clickIfPresent("my_account.add_card_overlay_no_thanks_button");
        if (Wait.untilElementPresent("my_account.mywallet_link")) {
            Clicks.click("my_account.mywallet_link");
            Wait.forPageReady();
            shouldBeOnPage("oc_my_wallet");
        }
    }

    /**
     * Selects a random deal of given type from deals and promotion page
     *
     * @param deal offer or coupon
     * @throws Throwable if any exception occurs
     */
    @When("^I select a random \"([^\"]*)\" from deals & promotions page using mobile website$")
    public void I_select_a_random_from_deals_promotions_page_using_mobile_website(String deal) throws Throwable {
        boolean expected = false;
        List<WebElement> offer = Elements.findElements("offer_details.offers_container");
        int size = offer.size();
        if (size > 0 && (expected == false)) {
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

    /**
     * Selects given link from offers and details panel
     *
     * @param offer_link shop now or Add To Wallet
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Adds a fully enrolled usl ID to current profile on my account page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add fully_enrolled_usl id on my account page using mobile website$")
    public void I_add_fully_enrolled_usl_id_on_my_account_page_using_mobile_website() throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("I_add_fully_enrolled_usl_id_on_my_account_page_using_mobile_website()");

        Clicks.click("my_account.plenti_id");
        String plenti_id = TestUsers.getEnrolledUslId().getPlentiId();
        TextBoxes.typeTextbox("my_account.usl_id", plenti_id);
        Clicks.click("my_account.apply_usl_id_button");
        Wait.untilElementPresent("my_account.my_plenti_label");
        Assert.assertTrue("ERROR-ENV: Unable to add usl id to profile", Elements.elementPresent("my_account.my_plenti_label"));
    }

    /**
     * Removes USL ID on checkout shipping and payment page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I remove USL ID from shipping and payment page using mobile website$")
    public void I_remove_USL_ID_from_shipping_and_payment_page_using_mobile_website() throws Throwable {
        Wait.untilElementPresent("shipping_payment_signed_in.remove_usl_button");
        Assert.assertTrue("ERROR-ENV: No USL ID added to the profile", Elements.elementPresent("shipping_payment_signed_in.remove_usl_button"));
        Clicks.click("shipping_payment_signed_in.remove_usl_button");
        Wait.untilElementPresent("shipping_payment_signed_in.lookUpLoyaltyId");
        Assert.assertTrue("ERROR-ENV: Unable to remove USL ID from shipping and payment page", Elements.elementPresent("shipping_payment_signed_in.lookup_link"));

    }

    /**
     * Remove all existing offers from wallet and adds a valid offer to wallet
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add a valid offer to my wallet using mobile website$")
    public void I_add_a_valid_offer_to_my_wallet_using_mobile_website() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.available_offers")) {
            MyOffers.deleteOffers();
            MyOffers.addOffers();
        } else {
            MyOffers.addOffers();
        }
    }

    /**
     * Navigates to loyalty landing page as a given user type
     *
     * @param user_type guest or signed_in
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to the loyalty landing page as a \"([^\"]*)\" user using mobile website$")
    public void iNavigateToTheLoyaltyLandingPageAsAUser(String user_type) throws Throwable {
        // Before landing to the Loyalty enrollment page check whether the loyalty account already associated to the signed in account
        if (signedIn() && Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            System.out.println("--> User is already enrolled in Loyalty!!");
        } else {
            Clicks.click("home.become_guest_loyallist");
            switch (user_type.toLowerCase()) {
                case "guest":
                    shouldBeOnPage("loyalty_home");
                    break;
                case "signed_in":
                    shouldBeOnPage("loyalty_enrollment");
                    break;
            }
        }
    }

    /**
     * Navigates to loyalty enrollment page from loyalty home page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to the loyalty enrollment page using mobile website$")
    public void iNavigateToTheLoyaltyEnrollmentPage() throws Throwable {
        Clicks.click("loyalty_home.create_profile_enroll_button");
        shouldBeOnPage("loyalty_enrollment");
    }

    /**
     * Navigates to the loyalist account association page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to the loyallist account association page using mobile website$")
    public void iNavigateToTheLoyallistAccountAssociationPage() throws Throwable {
        // Before landing to the Loyalty association page check whether the loyalty account already associated to the signed in account
        if (Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            Clicks.click("my_account.view_my_loyalllist_account");
            shouldBeOnPage("loyallist_account_summary");
            Clicks.click("loyallist_account_summary.remove_button");
            Wait.untilElementPresent("loyallist_account_summary.lty_account_panel");
            Clicks.click("loyallist_account_summary.remove_confirmation_btn");
        } else {
            Clicks.clickWhenPresent("my_account.goto_my_loyallist");
        }
        Wait.untilElementPresent("loyalty_association.verify_page");
        shouldBeOnPage("loyalty_association");
    }

    /**
     * Clicks on add offer on wallet page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I click on add offer on wallet page using mobile website$")
    public void iClickOnAddOfferOnWalletPage() throws Throwable {
        Clicks.click((macys() ? "oc_my_wallet" : "my_bwallet")+".add_offer_btn");
    }

    /**
     * Visits the website as a given user type and DOES NOT add new credit card to profile
     *
     * @param registered user type guest or registered
     * @throws Throwable if any exception occurs
     */
    @Given("^I visit the mobile web site as a (guest|registered) user without add CC$")
    public void I_visit_the_mobile_web_site_as_a_registered_user(String registered) throws Throwable {
        Navigate.visit("home");
        pausePageHangWatchDog();
        // close popup
        Clicks.clickIfPresent("home.popup_close");

        closeMewTutorial();
        Thread.sleep(5000);
        if (registered.equals("registered")) {
            CommonUtils.signInOrCreateAccount();
            // close CC popup
            Clicks.clickIfPresent("my_account.add_card_overlay_close_button");
        }
        Navigate.visit("home");
        Cookies.disableForeseeSurvey();
    }

    /**
     * Verifies a loyallist number can be associated with a user account
     *
     * @param loyallist_type type of loyallist ID to use from "loyalty.json" data file
     * @throws Throwable if any exception occurs
     */
    @And("^I should be able to associate my account by loyallist number using \"([^\"]*)\" details on mobile website$")
    public void iShouldBeAbleToAssociateMyAccountByLoyallistNumberUsingDetails(String loyallist_type) throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("iShouldBeAbleToAssociateMyAccountByLoyallistNumberUsingDetails()");

        LoyallistAssociation.loyaltyAssociation(TestUsers.getLoyallistInformation(loyallist_type));
        Wait.untilElementPresent("loyallist_account_summary.verify_page");
        shouldBeOnPage("loyallist_account_summary");
    }

    /**
     * Enrolls in the loyalty program as given user type
     *
     * @param user_type guest or signed_in
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be able to enroll in to the loyalty program as a \"([^\"]*)\" user using mobile website$")
    public void iShouldBeAbleToEnrollInToTheLoyaltyProgramAsAUser(String user_type) throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("iShouldBeAbleToEnrollInToTheLoyaltyProgramAsAUser()");
        String pageName = null;
        if (signedIn() && Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            Clicks.click("my_account.view_my_loyalllist_account");
            Wait.forPageReady();
            System.out.println("--> User is already enrolled in Loyalty, navigating to loyalty_enrollment_confirmation!!");
            pageName = "loyallist_account_summary";
        } else {
            LoyaltyEnrollment enrollmentPage = new LoyaltyEnrollment();
            switch (user_type.toLowerCase()) {
                case "guest":
                    enrollmentPage.guestUserLoyaltyEnrollmentMobileWebsite(TestUsers.getCustomer(null));
                    break;
                case "signed_in":
                    enrollmentPage.signedInUserLoyaltyEnrollment(TestUsers.getCustomer(null));
                    break;
            }
            pageName = "loyalty_enrollment_confirmation";
        }
        if (!Elements.elementPresent(pageName + ".loyalty_number")) {
            Assert.fail("Loyalty Enrollment Confirmation Page Not Loaded Properly");
        } else {
            System.out.println("Loyalty Enrollment Confirmation Page Loaded Successfully!!!");
        }
    }

    /**
     * Remove all existing offers from wallet and adds the given offer to wallet
     *
     * @param code offer code to add
     * @throws Throwable if any exception occurs
     */
    @And("^I add a offer \"([^\"]*)\" to my wallet using mobile website$")
    public void I_add_a_offer_to_my_wallet_using_mobile_website(String code) throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.available_offers")) {
            MyOffers.deleteOffers();
            MyOffers.addValidOffers(code);
        } else {
            MyOffers.addValidOffers(code);
        }
    }

    /**
     * Add an offer to wallet if no offer is present and then removes all offers from wallet
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I remove a valid offer to my wallet using mobile website$")
    public void I_remove_a_valid_offer_to_my_wallet_using_mobile_website() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.available_offers")) {
            MyOffers.deleteOffers();
        } else {
            MyOffers.addOffers();
            MyOffers.deleteOffers();
        }
    }

    /**
     * Looks up a plenti ID using valid usl phone number on payment page
     *
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Signs in to current existing profile
     *
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Creates a new profile and DOES NOT close the add credit card dialog
     *
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Verifies the display of the my account pages
     *
     * @param pageNames list of pages to verify
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify the My Account Pages are rendered properly using mobile website$")
    public void I_verify_the_My_Account_Pages_are_rendered_properly_using_mobile_website(List<String> pageNames) throws Throwable {
        shouldBeOnPage("my_account");
        for (String pageName : pageNames) {
            I_navigate_to_page_from_my_account_page_using_mobile_website(pageName);
            shouldBeOnPage(pageName.replace(" ", "_"));
            Navigate.visit("my_account");
        }
    }

    /**
     * Navigates to given page from my account page
     *
     * <p>
     * Options for page name:<br>
     * <code>my profile, my preferences, my address book, oc my wallet, wish list, order status, furniture mattress status, gift card balance</code><br>
     * </p>
     *
     * @param pageName target page name
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Verifies that the given credit services links displayed on my account page
     *
     * @param credit_links list of credit services links
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see below credit services links in my account page:$")
    public void iShouldSeeBelowCreditServicesLinksInMobileMyaccountPage(List<String> credit_links) throws Throwable {
        shouldBeOnPage("my_account");
        for (String credit_link : credit_links) {
            Assert.assertTrue(credit_link + " not present on my account page.", Elements.elementPresent("my_account." + credit_link));
        }
    }

    /**
     * Verifies that the given credit services links navigates to given respective pages
     *
     * @param list of link and respective page
     * @throws Throwable if any exception occurs
     */
    @And("^I should be navigated to below respective credit services pages using mobile website:$")
    public void iShouldBeNavigatedToBelowRespectiveCreditServicesPagesUsingMobileWebsite(List<HashMap<String, String>> list) throws Throwable {
        for (Map set : list) {
            shouldBeOnPage("my_account");
            Clicks.click("my_account." + set.get("credit_link"));
            shouldBeOnPage(set.get("landing_page").toString());
            Navigate.visit("my_account");
        }
    }

    /**
     * Enrolls in to the loyalty program as a signed in user
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I enroll in to the loyalty program using mobile website as a signed in user$")
    public void I_enroll_in_to_the_loyalty_program_using_mobile_website_as_a_user() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("iEnrollInToTheLoyaltyProgramAsAUserUsingMobileWebsite()");
        }
        new LoyaltyEnrollment().signedInUserLoyaltyEnrollment(TestUsers.getCustomer(null));
        shouldBeOnPage("loyalty_enrollment_confirmation");
    }

    /**
     * Verifies that the add credit card overlay and its components are displayed or not
     *
     * @param condition should or should not
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Navigates to order details page from order status page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to order details page using mobile website$")
    public void I_navigate_to_order_details_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("order_status");
        Wait.untilElementPresent("order_status.order_details");
        Clicks.click("order_status.order_details");
    }

    /**
     * Verifies that an order can be cancelled from order details page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify the ability to cancel the order in order details page using mobile website$")
    public void I_verify_the_ability_to_cancel_the_order_in_order_details_page_using_mobile_website() throws Throwable {
        Wait.untilElementPresent("order_details.cancel_order_button");
        Clicks.click("order_details.cancel_order_button");
        Wait.untilElementPresent("order_details.order_cancel_yes_button");
        Clicks.click("order_details.order_cancel_yes_button");
        Wait.untilElementNotPresent("order_details.order_cancel_yes_button");
        String cancelText = macys() ? "canceled" : "CANCELLED";
        if (!Elements.getText("order_details.order_status_text").contains(cancelText) &&
                !Elements.getText("order_details.order_total_amount").replace("$", "").equals("0.00")) {
            Assert.fail("Order not cancelled successfully");
        }
    }

    /**
     * Navigates to order return confirmation page using given order data
     * <p>
     * Order details come from "return_orders.json" resource file in shared data
     * </p>
     *
     * @param orderType "submitted", "intransit", or "transit"
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to order details page for \"([^\"]*)\" order using mobile website$")
    public void I_navigate_to_order_details_page_for_order_using_mobile_website(String orderType) throws Throwable {
        String orderNum = Utils.getOrderNumber(orderType);
        Boolean orderFound = false;
        for (String dataRange : DropDowns.getAllValues("order_status.order_date_range")) {
            DropDowns.selectByText("order_status.order_date_range", dataRange);
            Wait.forPageReady();
            for (WebElement order : Elements.findElements("order_status.order_number")) {
                if (order.getText().equals(orderNum)) {
                    Clicks.click(order);
                    orderFound = true;
                    break;
                }
            }
            if (orderFound) {
                break;
            }
        }
        Assert.assertTrue("Order " + orderNum + " not found in data range", orderFound);
    }

    /**
     * Verifies the details of order with the given status on the order details page
     *
     * @param orderType status to check
     * @throws Throwable if any exception occurs
     */
    @And("^I verify order details in OD page for \"([^\"]*)\" using mobile website$")
    public void I_verify_order_details_in_OD_page_for_using_mobile_website(String orderType) throws Throwable {
        shouldBeOnPage("order_details");
        Elements.elementShouldBePresent("order_details.header_status");
        Assert.assertTrue("Header staus is not displaying", Elements.getText("order_details.header_status").contains(orderType.toUpperCase()));
        Elements.elementShouldBePresent("order_details.shipping_address_container");
        for (WebElement items : Elements.findElements("order_details.shipping_address_container")) {
            Elements.elementShouldBePresent(items.findElement(Elements.element("order_details.shipping_address")));
            Elements.elementShouldBePresent(items.findElement(Elements.element("order_details.shipping_method")));
            Elements.elementShouldBePresent(items.findElement(Elements.element("order_details.shipping_status")));
        }
        Elements.elementShouldBePresent("order_details.item_container");
        for (WebElement itemList : Elements.findElements("order_details.item_container")) {
            Elements.elementShouldBePresent(itemList.findElement(Elements.element("order_details.item_description")));
            Elements.elementShouldBePresent(itemList.findElement(Elements.element("order_details.gift_box")));
            Elements.elementShouldBePresent(itemList.findElement(Elements.element("order_details.item_qty")));
            Elements.elementShouldBePresent(itemList.findElement(Elements.element("order_details.total")));
        }
        Elements.elementShouldBePresent("order_details.order_total_details");
    }

    /**
     * Navigates to loyalty enrollment page as a registered user
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate to the loyallist enrollment page as a registered user using mobile website$")
    public void I_navigate_to_the_loyallist_enrollment_page_as_a_registered_user_using_mobil_website() throws Throwable {
        // Before landing to the Loyalty enrollment page check whether the loyalty account already associated to the signed in account
        if (signedIn() && Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            System.out.println("--> User is already enrolled in Loyalty!!");
        } else {
            Clicks.click("my_account.become_a_loyallist");
            shouldBeOnPage("loyalty_enrollment");
        }
    }
}
