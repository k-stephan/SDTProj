package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.LoginCredentials;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.MEW.pages.CreateRegistry;
import com.macys.sdt.shared.actions.MEW.panels.GlobalNav;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import com.macys.sdt.shared.steps.website.ShopAndBrowse;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Registry extends StepUtils {

    private UserProfile regUser;
    private String promoCode;

    @And("^I add the product to a registry using mobile website$")
    public void I_add_the_product_to_a_registry_using_mobile_website() throws Throwable {
        // There are two element with same id. One for tablet and another for mobile.
        // so had to filter out the displayed element and click in below step
        Clicks.clickRandomElement("product_display.add_to_registry", WebElement::isDisplayed);
        Assert.assertTrue("ERROR-DATA: Unable to add product in registry", Wait.untilElementPresent
                ("add_to_registry_overlay.add_to_registry_overlay"));
    }

    @Given("^I visit the mobile web site as a registry user$")
    public void I_visit_the_mobile_web_site_as_a_registry_user() throws Throwable {
        regUser = TestUsers.getNewRegistryUser();
        sign_in_or_create_registry(regUser);
    }

    public void sign_in_or_create_registry(UserProfile user_details) throws Throwable {
        Navigate.visit("home");
        closeMewTutorial();
        if (bloomingdales()) {
            GlobalNav.openGlobalNav();
            GlobalNav.navigateOnGnByName("The Registry");
            GlobalNav.navigateOnGnByName("Create");
            GlobalNav.closeGlobalNav();
        } else {
            if (firefox()) {
                Navigate.visit(MainRunner.url + "/wgl/registry/signin?cm_sp=reg_homepage-_-row1-_-create");
                System.out.println(MainRunner.url);
            } else {
                GlobalNav.openGlobalNav();
                GlobalNav.navigateOnGnByName("Registry or Wedding Registry");
                GlobalNav.closeGlobalNav();
                Clicks.click("registry_home.goto_create_registry");
            }
        }
        ProfileAddress address = regUser.getUser().getProfileAddress();
        LoginCredentials credentials = regUser.getUser().getLoginCredentials();

        Wait.secondsUntilElementPresent("registry_sign_in.existing_user_email", 5);
        boolean newRegistryEnabled = onPage("new_registry_sign_in");
        if (newRegistryEnabled) {
            Wait.secondsUntilElementPresent("new_registry_sign_in.email", 10);
            TextBoxes.typeTextbox("new_registry_sign_in.email", address.getEmail());
            TextBoxes.typeTextbox("new_registry_sign_in.password", credentials.getPassword());
            Clicks.javascriptClick("new_registry_sign_in.sign_in_button");
        } else {
            TextBoxes.typeTextbox("registry_sign_in.existing_user_email", address.getEmail());
            TextBoxes.typeTextbox("registry_sign_in.existing_user_password", credentials.getPassword());
            Clicks.click("registry_sign_in.existing_user_continue_button");
        }
        CreateProfile.closeSecurityAlertPopUp();
        if (newRegistryEnabled) {
            Wait.untilElementPresent("new_registry_sign_in.error_message");
            if (onPage("new_registry_sign_in") && Elements.elementPresent("new_registry_sign_in.error_message")) {
                if (prodEnv())
                    throw new Exceptions.ProductionException("Cannot create accounts on prod!");
                Clicks.javascriptClick("new_registry_sign_in.create_registry_button");
                Wait.forPageReady();
                CreateRegistry.createRegistryUser(regUser);
                Wait.forPageReady();
                if (safari())
                    Wait.secondsUntilElementPresent("registry_welcome.verify_page", 20);
            } else if (onPage("new_create_registry")) {
                CreateRegistry.createRegistryUserForExistingUser(regUser);
                Wait.forPageReady();
            } else if (!onPage("registry_manager")) {
                Wait.secondsUntilElementPresent("registry_welcome.verify_page", (safari() ? 15 : 2));
                shouldBeOnPage("registry_welcome");
            }
        } else {
            if (onPage("registry_sign_in") && Elements.elementPresent("registry_sign_in.error_message")) {
                if (prodEnv())
                    throw new Exceptions.ProductionException("Cannot create accounts on prod!");
                TextBoxes.typeTextbox("registry_sign_in.new_user_email", address.getEmail());
                TextBoxes.typeTextbox("registry_sign_in.new_user_email_verify", address.getEmail());
                TextBoxes.typeTextbox("registry_sign_in.new_user_password", credentials.getPassword());
                TextBoxes.typeTextbox("registry_sign_in.new_user_password_verify", credentials.getPassword());
                Clicks.click("registry_sign_in.new_user_continue_button");
                CreateRegistry.fillRegistryUserDetails(user_details);
            }
            Wait.forPageReady();
            if (onPage("create_registry"))
                CreateRegistry.fillRegistryUserDetailsForExistingUser(user_details);
        }
        if (safari()) {
            com.macys.sdt.shared.steps.website.ShopAndBrowse shopAndBrowse = new ShopAndBrowse();
            try {
                shopAndBrowse.i_remove_all_items_from_the_shopping_bag();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Navigate.visit("home");
    }

    @And("^I start to create a new registry from mobile registry capture email page$")
    public void I_start_to_create_a_new_registry_from_mobile_registry_capture_email_page() throws Throwable {
        TestUsers.clearCustomer();
        UserProfile regUser = TestUsers.getNewRegistryUser();
        if (prodEnv()) {
            TextBoxes.typeTextbox("registry_sign_in.existing_user_email", regUser.getUser().getProfileAddress().getEmail());
            TextBoxes.typeTextbox("registry_sign_in.existing_user_password", regUser.getUser().getLoginCredentials().getPassword());
            Clicks.click("registry_sign_in.existing_user_continue_button");
            shouldBeOnPage("registry_manager");
        } else {
            if (onPage("registry_sign_in")) {
                TextBoxes.typeTextbox("registry_sign_in.new_user_email", regUser.getUser().getProfileAddress().getEmail());
                TextBoxes.typeTextbox("registry_sign_in.new_user_email_verify", regUser.getUser().getProfileAddress().getEmail());
                TextBoxes.typeTextbox("registry_sign_in.new_user_password", regUser.getUser().getLoginCredentials().getPassword());
                TextBoxes.typeTextbox("registry_sign_in.new_user_password_verify", regUser.getUser().getLoginCredentials().getPassword());
                Clicks.click("registry_sign_in.new_user_continue_button");
                shouldBeOnPage("create_registry");
            } else if (onPage("new_registry_sign_in")) {
                TextBoxes.typeTextbox("new_registry_sign_in.email", regUser.getUser().getProfileAddress().getEmail());
                TextBoxes.typeTextbox("new_registry_sign_in.password", regUser.getUser().getLoginCredentials().getPassword());
                Clicks.click("new_registry_sign_in.sign_in_button");
                if (onPage("new_registry_sign_in")) {
                    Clicks.click("new_registry_sign_in.create_registry_button");
                }
            } else {
                Assert.fail("User is currently not in Registry Capture Email Page");
            }
        }
    }

    @And("^I create a new registry using mobile website$")
    public void I_create_a_new_registry_using_mobile_website() throws Throwable {
        if (prodEnv()) {
            System.out.print("In Production we cannot create new Registries");
        } else {
            if (onPage("create_registry")) {
                CreateRegistry.fillRegistryUserDetails(TestUsers.getNewRegistryUser());
            } else if (onPage("new_create_registry")) {
                CreateRegistry.createRegistryUser(TestUsers.getNewRegistryUser());
            } else {
                Assert.fail("User is currently not in Create Registry Page");
            }
        }
    }

    @Then("^I should be navigated to the mobile registry manager page$")
    public void I_should_be_navigated_to_the_mobile_registry_manager_page() throws Throwable {
        if (onPage("registry_manager")) {
            if (Elements.elementPresent("registry_manager.registry_header")) {
                System.out.print("User successfully navigated to Registry Manager Page");
            }
        } else {
            Assert.fail("User is currently not in Registry Manager Page");
        }
    }

    @Then("^I should be navigated to the mobile registry welcome page$")
    public void I_should_be_navigated_to_the_mobile_registry_welcome_page() throws Throwable {
        if (onPage("registry_welcome")) {
            System.out.print("User successfully navigated to Registry welcome Page");
        } else {
            Assert.fail("User is currently not in Registry welcome Page");
        }
    }

    @And("^I sign in with existing profile on mobile capture email page$")
    public void I_sign_in_with_existing_profile_on_mobile_capture_email_page() throws Throwable {
        TextBoxes.typeTextbox("registry_sign_in.existing_user_email", TestUsers.currentEmail);
        TextBoxes.typeTextbox("registry_sign_in.existing_user_password", TestUsers.currentPassword);
        Clicks.click("registry_sign_in.existing_user_continue_button");
        Wait.secondsUntilElementPresent("new_create_registry.continue_button", 10);
        shouldBeOnPage("new_create_registry", "create_registry");
    }

    @And("^I continue creating registry from mobile create registry page$")
    public void I_continue_creating_registry_from_mobile_create_registry_page() throws Throwable {
        if (onPage("create_registry")) {
            CreateRegistry.fillRegistryUserDetailsForExistingUser(TestUsers.getExistingRegistryUser());
        } else if (onPage("new_create_registry")) {
            CreateRegistry.createRegistryUserForExistingUser(TestUsers.getExistingRegistryUser());
        } else {
            Assert.fail("User is currently not in Create Registry Page");
        }
        Wait.forPageReady();
        shouldBeOnPage("registry_welcome");
    }

    @When("^I navigate to wedding registry page$")
    public void I_navigate_to_wedding_registry_page() throws Throwable {
        GlobalNav.openGlobalNav();
        GlobalNav.navigateOnGnByName("Registry or Wedding Registry");
        GlobalNav.closeGlobalNav();
        Wait.secondsUntilElementPresent(Elements.element("registry_home.create_registry"), MainRunner.timeout);
        Assert.assertTrue("ERROR-ENV: Unable to navigate wedding registry page", Elements.elementPresent(Elements.element("registry_home.create_registry")));
    }

    @And("^I create a new wedding registry with event date as past date which is (less than|more than) 185 days and event type as \"(WEDDING|COMMITMENT||ANNIVERSARY)\" option on mobile site$")
    public void i_create_a_new_wedding_registry_with_event_date_as_past_date_which_is_less_than_185_days_and_event_type_as_wedding_option(String event_time, String event_type) throws Throwable {
        Calendar cal = Calendar.getInstance();
        Date modified_date;
        Random random = new Random();
        if (event_time.equals("less than")) {
            modified_date = DateUtils.addDays(new Date(), -(random.nextInt((185 - 1) + 1) + 1));
        } else {
            modified_date = DateUtils.addDays(new Date(), random.nextInt((185 - 1) + 1) + 1);
        }
        cal.setTime(modified_date);
        String year = Integer.toString(cal.get(Calendar.YEAR));
        int month = cal.get(Calendar.MONTH);
        String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        String month_name = WordUtils.capitalizeFully(Month.of(month + 1).name());
        regUser = TestUsers.getNewRegistryUser();
        com.macys.sdt.framework.model.Registry registry = regUser.getRegistry();
        registry.setEventType(event_type);
        registry.setEventMonth(month_name);
        registry.setEventDay(day);
        registry.setEventYear(year);
        sign_in_or_create_registry(regUser);
        Wait.forPageReady();
    }

    @And("^I navigate to the mobile registry manager page$")
    public void I_navigate_to_mobile_registry_manager_page() throws Throwable {
        GlobalNav.openGlobalNav();
        GlobalNav.navigateOnGnByName("Registry or Wedding Registry");
        shouldBeOnPage("registry_manager");
    }

    @And("^I save promocode displayed on mobile registry manager page$")
    public void I_save_promocode_displayed_on_registry_manager_page() throws Throwable {
        Wait.untilElementPresent(Elements.element("registry_manager.registry_promocode"));
        if (Elements.elementPresent("registry_manager.registry_promocode")) {
            promoCode = Elements.getText(Elements.element("registry_manager.registry_promocode"));
        } else {
            Assert.fail("Online promo code does not displayed");
        }
    }

    @And("^I apply registry promo code on mobile shopping bag page$")
    public void I_apply_registry_promo_code_on_the_shopping_bag_page() throws Throwable {
        Assert.assertTrue("ERROR: promo code field is not present", Wait.untilElementPresent(Elements.element("shopping_bag.promocode_area")));
        Clicks.click(Elements.element("shopping_bag.promocode_area"));
        TextBoxes.typeTextbox(Elements.element("shopping_bag.text_promocode"), promoCode);
        Clicks.click(Elements.element("shopping_bag.btn_promocode_apply"));
    }

    @Then("^I should see registry promocode is applied on mobile shopping bag page$")
    public void I_should_see_registry_promocode_applied_on_mobile_shopping_bag_page() throws Throwable {
        Wait.untilElementPresent("shopping_bag.promo_text");
        String promoText = (macys() ? "Save 20% on most remaining registry gifts!" :
                "Completion Promotion - 10% off registry completion purchases");
        Assert.assertTrue("Registry Promo Code is not applied on shopping bag page!!",
                Elements.getText("shopping_bag.promo_text").contentEquals(promoText));
    }

    @Then("^I verify the registrant & co registrant name on mobile shopping bag page$")
    public void I_verify_the_registrant_co_registrant_name_on_mobile_shopping_bag_page() throws Throwable {
        String first_name = regUser.getUser().getProfileAddress().getFirstName();
        String last_name = regUser.getUser().getProfileAddress().getLastName();
        String co_registrant_fn = regUser.getRegistry().getCoRegistrantFirstName();
        String co_registrant_ln = regUser.getRegistry().getCoRegistrantLastName();
        Wait.untilElementPresent("shopping_bag.registrant_name_details");
        String registrant_details = Elements.getText("shopping_bag.registrant_name_details");
        String registrant_co_registrant_name = first_name + " " + last_name + " & " + co_registrant_fn + " " + co_registrant_ln;
        Assert.assertTrue("Error: Registrant & Co-registrant name incorrect", registrant_details.toUpperCase().contains(registrant_co_registrant_name));
    }

    @When("^I search for the existing couple's registry using mobile site$")
    public void i_search_for_the_existing_couple_s_registry() throws Throwable {
        String capturedFirstName = regUser.getUser().getProfileAddress().getFirstName();
        String capturedLastName = regUser.getUser().getProfileAddress().getLastName();
        String page = (onPage("registry_home") ? "registry_home" : "registry_search");
        TextBoxes.typeTextbox(page + ".search_first_name", capturedFirstName);
        TextBoxes.typeTextbox(page + ".search_last_name", capturedLastName);
        Clicks.click(page + ".search_registry_button");
    }

    @Then("^I should find the couple's registry using mobile site$")
    public void i_should_find_the_couple_s_registry() throws Throwable {
        String firstName = regUser.getUser().getProfileAddress().getFirstName();
        String lastName = regUser.getUser().getProfileAddress().getLastName();
        String registrantName = firstName + " " + lastName;
        Boolean found = false;
        shouldBeOnPage("registry_search");
        List<WebElement> registrantNameElements = Elements.findElements("registry_search.registrant_name");
        for (WebElement we : registrantNameElements) {
            String str = we.getText();
            if (str.equalsIgnoreCase(registrantName)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(registrantName + " registry not found.", found);
    }

    @When("^I click on edit profile link on mobile registry manager page$")
    public void I_click_on_edit_profile_link_on_mobile_registry_manager_page() throws Throwable {
        shouldBeOnPage("registry_manager");
        Clicks.click("registry_manager.edit_our_registry");
    }

    @When("^I update co_registrant_first_name in mobile update registry page$")
    public void I_update_co_registrant_first_name_in_mobile_update_registry_page() throws Throwable {
        shouldBeOnPage("edit_registry");
        String updatedFirstName = TestUsers.generateRandomFirstName();
        if (regUser != null && regUser.getRegistry() != null) {
            regUser.getRegistry().setCoRegistrantFirstName(updatedFirstName);
        }
        if (macys()) {
            Clicks.click("edit_registry.contact_information");
        }
        Wait.untilElementPresent("edit_registry.co_registrant_first_name");
        TextBoxes.typeTextbox("edit_registry.co_registrant_first_name", updatedFirstName);
        Clicks.click("edit_registry.update_button");
        Wait.forPageReady();
    }

    @Then("^I should see updated co_registrant_first_name in mobile registry manager page$")
    public void I_should_see_updated_co_registrant_first_name_in_mobile_registry_manager_page() throws Throwable {
        Wait.untilElementPresent("registry_manager.registry_header");
        shouldBeOnPage("registry_manager");
        String actualRegistryHeader = Elements.getText("registry_manager.registry_header");
        String expectedRegistryHeader;
        if (macys()) {
            expectedRegistryHeader = regUser.getUser().getProfileAddress().getFirstName() + " " +
                    regUser.getUser().getProfileAddress().getLastName() + " & " +
                    regUser.getRegistry().getCoRegistrantFirstName() + " " + regUser.getRegistry().getCoRegistrantLastName();
        } else {
            expectedRegistryHeader = regUser.getUser().getProfileAddress().getFirstName() + " & " +
                    regUser.getRegistry().getCoRegistrantFirstName() + "'S WEDDING";
        }
        Assert.assertTrue("Registry title is not updated properly!!", actualRegistryHeader.equalsIgnoreCase(expectedRegistryHeader));
    }

    @And("^I add product to bag from GVR page using mobile website and select checkout$")
    public void iAddProductToBagFromGVRPageUsingMobileWebsite() throws Throwable {
        I_navigate_to_wedding_registry_page();
        i_search_for_the_existing_couple_s_registry();
        i_should_find_the_couple_s_registry();
        String capturedName = regUser.getUser().getProfileAddress().getFirstName() + " " +
                regUser.getUser().getProfileAddress().getLastName();
        for (WebElement result : Elements.findElements("registry_search.registrant_name")) {
            if (result.getText().equalsIgnoreCase(capturedName)) {
                Clicks.click(result);
                break;
            }
        }
        shouldBeOnPage("registry_gvr");
        Wait.untilElementPresent("registry_gvr.add_to_bag");
        Clicks.click("registry_gvr.add_to_bag");
        Wait.untilElementPresent("registry_gvr.atb_checkout");
        Clicks.click("registry_gvr.atb_checkout");
    }
}
