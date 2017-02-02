package com.macys.sdt.shared.steps.website;


import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.user.LoginCredentials;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.utils.Cookies;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import com.macys.sdt.shared.actions.website.mcom.pages.registry.CreateRegistry;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Registry extends StepUtils {

    private UserProfile regUser;
    private String promoCode, beforePrice;
    private boolean newRegistryEnabled = false;

    /**
     * Visits the web site then creates or logs into a registry
     *
     * @throws Throwable if any exception occurs
     */
    @Given("^I visit the web site as a registry user$")
    public void I_visit_the_web_site_as_a_registry_user() throws Throwable {
        regUser = TestUsers.getNewRegistryUser();
        // Get new co_registrant_fname & co_registrant_lname values if these names are matching with registrant first_name and last_name
        while (regUser.getRegistry().getCoRegistrantFirstName().equals(regUser.getUser().getProfileAddress().getFirstName()) && regUser.getRegistry().getCoRegistrantLastName().equals(regUser.getUser().getProfileAddress().getLastName())) {
            regUser.getRegistry().setCoRegistrantFirstName(TestUsers.generateRandomFirstName());
            regUser.getRegistry().setCoRegistrantLastName(TestUsers.generateRandomLastName());
        }
        pausePageHangWatchDog();
        sign_in_or_create_registry(regUser);
        resumePageHangWatchDog();
        new CheckoutSteps().iRemoveAllItemsInShoppingBag();
        new MyAccountSteps().iAddCheckoutEligibleAddressOnMyAddressBookPage();
        Navigate.visit("registry_home");
        Wait.forPageReady();
    }

    /**
     * Creates or logs into a registry
     *
     * @param user_details profile to create
     * @throws Throwable if any exception occurs
     */
    public void sign_in_or_create_registry(UserProfile user_details) throws Throwable {
        ProfileAddress userAddress = regUser.getUser().getProfileAddress();
        LoginCredentials credentials = regUser.getUser().getLoginCredentials();
        com.macys.sdt.framework.model.registry.Registry userRegistry = regUser.getRegistry();

        new PageNavigation().I_visit_the_web_site_as_a_guest_user();
        Clicks.click("home.goto_wedding_registry");
        CommonUtils.closeIECertError();
        Wait.forPageReady();
        if (safari()) {
            Wait.secondsUntilElementPresent("registry_home.goto_create_registry", 15);
        }
        new PageNavigation().iSelectCreateRegistry();
        pausePageHangWatchDog();
        Wait.secondsUntilElementPresent("registry_sign_in.existing_user_email", 5);
        newRegistryEnabled = onPage("new_registry_sign_in");
        if (newRegistryEnabled) {
            Wait.secondsUntilElementPresent("new_registry_sign_in.email", 10);
            TextBoxes.typeTextbox("new_registry_sign_in.email", userAddress.getEmail());
            TextBoxes.typeTextbox("new_registry_sign_in.password", credentials.getPassword());
            Clicks.javascriptClick("new_registry_sign_in.sign_in_button");
            Wait.forPageReady();
        } else {
            TextBoxes.typeTextbox("registry_sign_in.existing_user_email", userAddress.getEmail());
            TextBoxes.typeTextbox("registry_sign_in.existing_user_password", credentials.getPassword());
            Clicks.javascriptClick("registry_sign_in.existing_user_continue_button");
        }
        if (Cookies.getCookieValue("SMISCGCs").contains("regId") && !Cookies.getCookieValue("SMISCGCs").contains("regId1_92_null")) {
            Matcher m = Pattern.compile("regId1_92_(\\d+)").matcher(Cookies.getCookieValue("SMISCGCs"));
            userRegistry.setId(m.find() ? m.group(1) : null);
        }
        if (userRegistry.getId() != null) {
            System.out.println("User is already logged into registry");
        } else {
            Wait.forPageReady();
            CreateProfile.closeSecurityAlertPopUp();
            if (newRegistryEnabled) {
                Wait.untilElementPresent("new_registry_sign_in.error_message");
                Wait.untilElementPresent("new_create_registry.verify_page");
                if (onPage("new_registry_sign_in") && Elements.elementPresent("new_registry_sign_in.error_message")) {
                    if (prodEnv()) {
                        throw new Exceptions.ProductionException("Cannot create accounts on prod!");
                    }
                    Clicks.click("new_registry_sign_in.verify_page");
                    Wait.forPageReady();
                    CreateRegistry.createRegistryUser(regUser);
                    Wait.forPageReady();
                    if (safari()) {
                        Wait.secondsUntilElementPresent("registry_welcome.verify_page", 20);
                    }
                } else if (Wait.secondsUntilElementPresent("new_create_registry.verify_page", 8) && onPage("new_create_registry")) {
                    CreateRegistry.createRegistryUserForExistingUser(regUser);
                    Wait.forPageReady();
                } else if (!onPage("registry_manager")) {
                    Wait.secondsUntilElementPresent("registry_welcome.verify_page", (safari() ? 15 : 5));
                    shouldBeOnPage("registry_welcome");
                }
            } else {
                if (onPage("registry_sign_in") && Elements.elementPresent("registry_sign_in.error_message")) {
                    if (prodEnv()) {
                        throw new Exceptions.ProductionException("Cannot create accounts on prod!");
                    }
                    TextBoxes.typeTextbox("registry_sign_in.new_user_email", userAddress.getEmail());
                    TextBoxes.typeTextbox("registry_sign_in.new_user_email_verify", userAddress.getEmail());
                    TextBoxes.typeTextbox("registry_sign_in.new_user_password", credentials.getPassword());
                    TextBoxes.typeTextbox("registry_sign_in.new_user_password_verify", credentials.getPassword());
                    Clicks.click("registry_sign_in.new_user_continue_button");
                    CreateRegistry.fillRegistryUserDetails(user_details);
                } else if (onPage("create_registry")) {
                    CreateRegistry.fillRegistryUserDetailsForExistingUser(user_details);
                }
                Wait.forPageReady();
            }
        }
        if (safari()) {
            ShopAndBrowse shopAndBrowse = new ShopAndBrowse();
            try {
                shopAndBrowse.i_remove_all_items_from_the_shopping_bag();
            } catch (Exception e) {
            }
        }
        if (!onPage("registry_manager")) {
            Navigate.visit("registry_home");
            Wait.forPageReady();
        }
        Assert.assertFalse("ERROR - ENV : Registry Services are in Batch Mode!!", (macys() ? Elements.getText(By.id("generalErrorMsg")).contains("Your registry is not available online") : Elements.elementPresent(By.className("registry-batch-mode"))));
        Assert.assertFalse("ERROR - ENV : Registry Flex Template services are down!!", title().contains(macys() ? "Site Unavailable" : "oops"));
        shouldBeOnPage("registry_manager");
        resumePageHangWatchDog();
    }

    /**
     * Navigates to registry sign in page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to registry sign in page$")
    public void I_navigate_to_registry_sign_in_page() throws Throwable {
        Clicks.javascriptClick("registry_home.goto_create_registry");
        shouldBeOnPage("registry_sign_in", "new_registry_sign_in");
    }

    /**
     * Creates a registry. Assumes browser is on create registry page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I continue creating registry from create registry page$")
    public void I_continue_creating_registry_from_create_registry_page() throws Throwable {
        if (macys()) {
            CreateRegistry.createRegistryUserForExistingUser(TestUsers.getExistingRegistryUser());
        } else {
            CreateRegistry.fillRegistryUserDetailsForExistingUser(TestUsers.getExistingRegistryUser());
        }
    }

    /**
     * Navigates the browser back from login page to registry home page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I navigate back to registry home page from capture email page$")
    public void I_navigate_back_to_registry_home_page_from_capture_email_page() throws Throwable {
        Navigate.browserBack();
    }

    /**
     * Adds the current product to registry
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add the product to a registry$")
    public void I_add_the_product_to_a_registry() throws Throwable {
        Clicks.click("product_display.add_to_registry");
        Wait.secondsUntilElementPresent("add_to_registry_dialog.add_to_registry_dialog", 5);
        Assert.assertTrue("ERROR-ENV: Add to Registry Dialog is not present", Elements.elementPresent("add_to_registry_dialog.add_to_registry_dialog"));
    }

    /**
     * Verifies that the browser is on the registry manager page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be navigated to the registry manager page$")
    public void I_should_navigate_to_registry_manager_page() throws Throwable {
        shouldBeOnPage("registry_manager", "registry_welcome");
    }

    /**
     * Signs into existing user from sign in page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I sign in with existing profile on capture email page$")
    public void I_sign_in_with_existing_profile_and_from_capture_email_page() throws Throwable {
        TextBoxes.typeTextbox("registry_sign_in.existing_user_email", TestUsers.currentEmail);
        TextBoxes.typeTextbox("registry_sign_in.existing_user_password", TestUsers.currentPassword);
        Clicks.click("registry_sign_in.existing_user_continue_button");
        shouldBeOnPage("create_registry", "new_create_registry");
    }

    /**
     * Navigates to the registry home page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I navigate to registry home page$")
    public void I_navigate_to_registry_home_page() throws Throwable {
        Wait.forPageReady();
        Clicks.click("home.goto_wedding_registry");
        Wait.forPageReady();
        if (safari()) {
            Wait.secondsUntilElementPresent("registry_home.goto_find_registry", 30);
        }
        CommonUtils.closeIECertError();
        shouldBeOnPage("registry_home");
    }

    /**
     * Verifies that the browser is on a registry PDP
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should be redirected to registry PDP(?: page)?$")
    public void I_should_be_redirected_to_registry_PDP_page() throws Throwable {
        shouldBeOnPage("registry_pdp");
    }

    /**
     * Begins the process of creating a registry starting on the registry sign in page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I start to create a new registry from registry sign in page$")
    public void I_start_to_create_a_new_registry_from_registry_capture_email_page() throws Throwable {
        regUser = TestUsers.getNewRegistryUser();
        String email = regUser.getUser().getProfileAddress().getEmail();
        String password = regUser.getUser().getLoginCredentials().getPassword();
        if (onPage("new_registry_sign_in")) {
            // new registry experience
            if (macys()) {
                Clicks.click("new_registry_sign_in.create_registry_button");
            }
            shouldBeOnPage("new_create_registry");
        } else if (onPage("registry_sign_in")) {
            // old registry experience
            TextBoxes.typeTextbox("registry_sign_in.new_user_email", email);
            TextBoxes.typeTextbox("registry_sign_in.new_user_email_verify", email);
            TextBoxes.typeTextbox("registry_sign_in.new_user_password", password);
            TextBoxes.typeTextbox("registry_sign_in.new_user_password_verify", password);
            Clicks.click("registry_sign_in.new_user_continue_button");
            shouldBeOnPage("create_registry");
        } else {
            Assert.fail("User is currently not in Registry sign in Page");
        }
    }

    /**
     * Verifies that the registry has been created successfully
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see the registry created successfully$")
    public void I_should_see_the_registry_created_successfully() throws Throwable {
        Wait.untilElementPresent("create_registry.create_message");
        Wait.untilElementNotPresent("create_registry.create_message");
        if (safari()) {
            Wait.secondsUntilElementPresent("registry_manager.registry_id", 15);
        }
        Assert.assertFalse("ERROR - ENV : Registry Flex Template services are down!!", title().contains(macys() ? "Site Unavailable" : "oops"));
        shouldBeOnPage("registry_manager");
        CreateRegistry.verifyRegistryIsCreatedInDB(Elements.findElement("registry_manager.registry_id").getText());
    }

    /**
     * Click on the edit profile button on the registry manager page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I click on edit profile link on registry manager page$")
    public void I_click_on_edit_profile_link_on_registry_manager_page() throws Throwable {
        if (safari())
            Wait.untilElementPresent("registry_manager.edit_profile_button");
        if (!chrome() && macys())
            Clicks.javascriptClick("registry_manager.edit_profile_button");
        else
            Clicks.click("registry_manager.edit_profile_button");
    }

    /**
     * Verifies that the browser is on the update registry page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see update registry page$")
    public void I_should_see_update_registry_page() throws Throwable {
        if (safari()) {
            Wait.untilElementPresent("edit_registry.verify_page");
            Wait.forPageReady();
        }
        shouldBeOnPage("edit_registry");
    }

    @When("^I update the event location to \"([^\"]*)\" in update registry page$")
    public void I_update_the_event_location_in_update_registry_page(String location) throws Throwable {
        Wait.untilElementPresent("edit_registry.event_location");
        DropDowns.selectByText("edit_registry.event_location", location);
        Clicks.click("edit_registry.update_registry_button");
    }

    /**
     * Updates the co registrant first name in registry manager page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I update \"co_registrant_first_name\" in update registry page$")
    public void I_update_in_update_registry_page() throws Throwable {
        String updatedFirstName = TestUsers.generateRandomFirstName();
        if (regUser != null && regUser.getRegistry() != null) {
            regUser.getRegistry().setCoRegistrantFirstName(updatedFirstName);
        }
        Wait.untilElementPresent("edit_registry.co_registrant_first_name");
        TextBoxes.typeTextbox("edit_registry.co_registrant_first_name", updatedFirstName);
        Clicks.click("edit_registry.update_registry_button");
        Wait.untilElementPresent("create_registry.close_overlay_chat");
        if (Elements.elementPresent("create_registry.close_overlay_chat")) {
            Clicks.click("create_registry.close_overlay_chat");
        }
        Wait.forPageReady();
    }

    /**
     * Verifies that co registrant first name field has been updated
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see updated \"co_registrant_first_name\" in registry manager page$")
    public void I_should_see_updated_data_in_registry_manager_page() throws Throwable {
        Wait.secondsUntilElementPresent("registry_manager.registry_title", 5);
        String actualRegistryTitle = Elements.getText("registry_manager.registry_title");
        String expectedRegistryTitle = regUser.getUser().getProfileAddress().getFirstName() + " & " + regUser.getRegistry().getCoRegistrantFirstName() + "'S " + regUser.getRegistry().getEventType();
        if (!actualRegistryTitle.equalsIgnoreCase(expectedRegistryTitle)) {
            Assert.fail("Registry title is not updated properly!!");
        }
    }

    /**
     * Searches for an existing registry using currently saved registry data
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I search for the existing couple's registry$")
    public void i_search_for_the_existing_couple_s_registry() throws Throwable {
        String capturedFirstName = regUser.getUser().getProfileAddress().getFirstName();
        String capturedLastName = regUser.getUser().getProfileAddress().getLastName();

        TextBoxes.typeTextbox("find_registry.first_name", capturedFirstName);
        TextBoxes.typeTextbox("find_registry.last_name", capturedLastName);
        Clicks.click("find_registry.find_registry_button");
    }

    /**
     * Verifies that the correct registry has been found
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should find the couple's registry$")
    public void i_should_find_the_couple_s_registry() throws Throwable {
        String capturedFirstName = regUser.getUser().getProfileAddress().getFirstName();
        String capturedLastName = regUser.getUser().getProfileAddress().getLastName();
        String capturedName = capturedFirstName + " " + capturedLastName;
        Boolean found = false;
        Wait.forPageReady();
        if (safari()) {
            Wait.secondsUntilElementPresent("find_registry.find_registry_results", 10);
        }
        List<WebElement> elist = Elements.findElements("find_registry.find_registry_results");
        for (WebElement we : elist) {
            String str = we.getText();
            if (str.equalsIgnoreCase(capturedName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Assert.fail("Registry not found");
        }
    }

    /**
     * Verifies that the names on the registry are correct
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify the registrant & co registrant name details$")
    public void i_verify_the_registrant_co_registrant_name_details() throws Throwable {
        String first_name = regUser.getUser().getProfileAddress().getFirstName();
        String last_name = regUser.getUser().getProfileAddress().getLastName();
        String co_registrant_fn = regUser.getRegistry().getCoRegistrantFirstName();
        String co_registrant_ln = regUser.getRegistry().getCoRegistrantLastName();

        Wait.untilElementPresent("shopping_bag.registrant_name_details");
        String registrant_details = Elements.getText("shopping_bag.registrant_name_details").trim();

        try {
            if (macys()) {
                String registrant_co_registrant_name = first_name + " " + last_name + " & " + co_registrant_fn + " " + co_registrant_ln;
                Assert.assertTrue("Error: registrant and co-registrant name incorrect!!", registrant_details.toUpperCase().contains(registrant_co_registrant_name));
            } else {
                String[] split_name = registrant_details.split(" & ");

                String registrant_name = first_name + " " + last_name;
                String co_registrant_name = co_registrant_fn + " " + co_registrant_ln;

                Assert.assertTrue("Error: registrant name incorrect!!", split_name[0].trim().equalsIgnoreCase(registrant_name));
                Assert.assertTrue("Error: Co-registrant name incorrect!!", split_name[1].trim().equalsIgnoreCase(co_registrant_name));
            }
        } catch (Exception e) {
            Assert.fail("Registrant and Co Registrant name on shopping bag are not matching with Registrant data" + e);
        }
    }

    /**
     * Creates registry with given time to even and event type
     *
     * @param event_time "less than" or "more than"
     * @param event_type "WEDDING" or "COMMITMENT" or "ANNIVERSARY"
     * @throws Throwable if any exception occurs
     */
    @And("^I create a new wedding registry with event date as past date which is (less than|more than) 185 days and event type as \"(WEDDING|COMMITMENT||ANNIVERSARY)\" option$")
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
        regUser.getRegistry().setEventType(event_type);
        regUser.getRegistry().setEventMonth(month_name);
        regUser.getRegistry().setEventDay(day);
        regUser.getRegistry().setEventYear(year);
        sign_in_or_create_registry(regUser);
        // update the event date if the event date is not updated in update registry page.
        I_click_on_edit_profile_link_on_registry_manager_page();
        Wait.forPageReady();
        shouldBeOnPage("edit_registry");
        Wait.untilElementPresent("edit_registry.event_month");
        DropDowns.selectByText("edit_registry.event_month", month_name);
        DropDowns.selectByText("edit_registry.event_day", day);
        DropDowns.selectByText("edit_registry.event_year", year);
        Clicks.click("edit_registry.update_registry_button");
        Wait.untilElementPresent("create_registry.close_overlay_chat");
        Clicks.clickIfPresent("create_registry.close_overlay_chat");
        Wait.forPageReady();
    }

    /**
     * Saves the promo code on the registry manager page to a local variable
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I save promocode displayed on registry manager page$")
    public void I_save_promocode_displayed_on_registry_manager_page() throws Throwable {
        String promoBannerName = macys() ? "completion_banner" : "get_offer_now";
        Wait.untilElementPresent("registry_manager." + promoBannerName);
        if (Elements.elementPresent("registry_manager." + promoBannerName)) {
            if (bloomingdales()) {
                Clicks.click("registry_manager.get_offer_now");
            }
            promoCode = Elements.findElement("registry_manager.online_promo_code_no").getAttribute("innerHTML");
        } else {
            Assert.fail("Online promo code does not displayed");
        }
    }

    /**
     * Uses the saved promo code on the shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I apply registry promo code on the shopping bag page$")
    public void I_apply_registry_promo_code_on_the_shopping_bag_page() throws Throwable {
        beforePrice = Elements.getText("shopping_bag.order_total");
        beforePrice = beforePrice.replaceAll("[$.]", "").replaceAll("\\s+", "");
        TextBoxes.typeTextbox("shopping_bag.promo_code", promoCode);
        Clicks.click("shopping_bag.apply_promo_code_button");
        Wait.untilElementNotPresent("shopping_bag.apply_promo_code_button");
    }

    /**
     * Verifies that order total is updated on the shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see updated order total on the shopping bag page$")
    public void I_should_see_I_should_see_updated_order_total_on_the_shopping_bag_page() throws Throwable {
        pausePageHangWatchDog();
        Wait.secondsUntilElementPresent("shopping_bag.promo_text", 5);
        List<WebElement> discountTexts = Elements.findElements("shopping_bag.promo_text");
        String discount = "";
        if (discountTexts.stream().anyMatch(element -> element.getText().toLowerCase().contains("registry"))) {
            discount = discountTexts.stream()
                    .filter(element -> element.getText().toLowerCase().contains("registry"))
                    .map(element -> element.findElement(By.xpath("..")).findElement(Elements.element("shopping_bag.promo_discount")).getText())
                    .collect(Collectors.toList()).get(0);
        }
        Assert.assertFalse("Registry Promo Code is not applied on shopping bag page!!", discount.isEmpty());
        discount = discount.replaceAll("[-$.]", "").replaceAll("\\s+", "");
        String after_price = Elements.getText("shopping_bag.order_total");
        after_price = after_price.replaceAll("[$.]", "").replaceAll("\\s+", "");
        if (Integer.parseInt(after_price) <= (Integer.parseInt(beforePrice) - Integer.parseInt(discount))) {
            System.out.println("Registry Promotion is applied successfully");
        } else {
            Assert.fail("ERROR - APP : Applied registry promo code is not reflected in order summary!!");
        }
        resumePageHangWatchDog();
    }
}
