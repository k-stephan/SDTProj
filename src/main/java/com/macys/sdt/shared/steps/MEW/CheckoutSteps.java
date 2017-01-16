package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.shared.actions.MEW.pages.Checkout;
import com.macys.sdt.shared.actions.MEW.pages.CreateProfileMEW;
import com.macys.sdt.shared.actions.website.bcom.pages.PaypalLogin;
import com.macys.sdt.shared.utils.CheckoutUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

public class CheckoutSteps extends StepUtils {

    @And("^I checkout until I reach the (shipping|payment|shipping\\s*&\\s*payment|order\\s*review|order\\s*confirmation) page using mobile website as an? \"([^\"]*)\" user(?: from \"([^\"]*)\")?$")
    public void I_checkout_until_I_reach_the_page_using_mobile_website_as_a_user(String pageName, String userType, String country) throws Throwable {
        boolean bops = userType.contains("bops");
        boolean signIn = userType.contains("signed in");
        HashMap<String, String> opts = new HashMap<>();
        if (country == null)
            country = "United States";

        boolean iship = userType.contains("iship") || !country.equalsIgnoreCase("United States");
        opts.put("country", country);
     //   if (signIn && userType.contains("responsive"))
        //    Cookies.forceRc();

        if (!((onPage("responsive_checkout") || onPage("shipping_payment_signed_in")))&& !iship)
            new CheckoutUtils().navigateToCheckout(signIn, false);

        if (iship) {
            new Checkout().ishipCheckout(pageName, opts);
        } else if (signIn) {
            if (onPage("responsive_checkout_signed_in"))
                new Checkout().rcSignedIn(pageName, opts, bops);
            else
                new Checkout().signInCheckout(pageName, opts, bops);
        } else {
            if (onPage("responsive_checkout")) {
                opts.put("checkout_eligible", "true");
                new Checkout().rcGuest(CheckoutUtils.RCPage.fromString(pageName), opts, bops);
            } else
                Assert.fail("Not on a valid checkout page!");
        }
    }

    @And("^I checkout on batch mode until I reach the (shipping|payment|shipping\\s*&\\s*payment|order\\s*review|order\\s*confirmation) page using mobile website as an? \"([^\"]*)\" user(?: from \"([^\"]*)\")?$")
    public void I_checkout_on_batch_mode_until_I_reach_the_page_using_mobile_website_as_a_user(String pageName, String userType, String country) throws Throwable {
        // run this step only on Batch Mode enabled QA environment
        if (MainRunner.batchMode) {
            I_checkout_until_I_reach_the_page_using_mobile_website_as_a_user(pageName, userType, country);
        } else {
            System.err.println("Environment not in batch mode, unable to checkout due to product unavailability.");
        }
    }

    @When("^I enter shipping address on guest shipping page using mobile website$")
    public void I_enter_shipping_address_on_guest_shipping_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("responsive_checkout");
        new Checkout().fillShippingData(true, false, null);
    }

    @And("^I select continue button on guest shipping page using mobile website$")
    public void I_select_continue_button_on_guest_shipping_page_using_mobile_website() throws Throwable {
        Clicks.click("responsive_checkout.continue_shipping_checkout_button");
        // some emulator devices fail here, no idea why
        if (MEW() && Elements.elementPresent("responsive_checkout.continue_shipping_checkout_button")) {
            Utils.threadSleep(1000, null);
            Clicks.click("responsive_checkout.continue_shipping_checkout_button");
        }
    }

    @And("^I fill in payment information on guest payment page using mobile website$")
    public void I_fill_in_payment_information_on_guest_payment_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("responsive_checkout");
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        new Checkout().fillPaymentData(true, false, opts);
    }

    @And("^I select continue button on guest payment page using mobile website$")
    public void I_select_continue_button_on_guest_payment_page_using_mobile_website() throws Throwable {
        // run this step only on Batch Mode enabled QA environment
        if (MainRunner.batchMode) {
            if (onPage("responsive_checkout")) {
                Clicks.click("responsive_checkout.continue_payment_checkout_button");
            } else {
                Clicks.click("payment_guest.continue_checkout_button");
            }
        } else {
            System.err.println("Environment not in batch mode, unable to checkout due to product unavailability.");
        }
    }

    @When("^I sign in during checkout using mobile website$")
    public void I_sign_in_during_checkout_using_mobile_website() throws Throwable {
        User user = prodEnv() ? TestUsers.getProdCustomer().getUser() : TestUsers.getCustomer(null).getUser();
        if (!onPage("shipping_payment_signed_in")) {
            Navigate.visit("shopping_bag");
            Clicks.click("shopping_bag.continue_checkout_image");
        }
        if (onPage("checkout_sign_in")) {
            TextBoxes.typeTextbox("checkout_sign_in.email", user.getProfileAddress().getEmail());
            TextBoxes.typeTextbox("checkout_sign_in.password", user.getLoginCredentials().getPassword());

            Assert.assertTrue("ERROR-ENV: CheckoutSteps button is not visible", Elements.elementPresent("checkout_sign_in.checkout_button"));
            Clicks.click("checkout_sign_in.checkout_button");
        } else {
            System.out.println("Already signed in");
        }
    }

    @And("^I enter (sdd_eligible|sdd_ineligible) address on shipping page using mobile website for (guest|signed in) user$")
    public void I_enter_sdd_eligible_address_on_shipping_page_using_mobile_website_for_signed_in_user(String sdd_eligible, String signedIn) throws Throwable {
        boolean sdd = sdd_eligible.equals("sdd_eligible");
        HashMap<String, String> opts = new HashMap<>();
        opts.put(sdd_eligible, Boolean.toString(sdd));

        boolean signIn = signedIn.equals("signed in");
        String page = signIn ? "responsive_checkout_signed_in" : "responsive_checkout";
        if (prodEnv()) {
            page = "shipping_payment_signed_in";
        }

        if (signIn) {
            if (!Elements.elementPresent(page + ".select_sdd")) {
                if (prodEnv()) {
                    if (Elements.elementPresent(page + ".address_edit_link")) {
                        Clicks.click(page + ".address_edit_link");
                        Clicks.click(page + ".address_delete_button");
                        Clicks.click(page + ".confirm_delete");
                    }
                    Clicks.click(Elements.element(page + ".add_shipping_address_button"));
                    CreateProfileMEW.addNewAddress();
                } else {
                    if (onPage("responsive_checkout_signed_in")) {
                        if (Elements.elementPresent(page + ".change_shipping_address"))  {
                            Clicks.click(page + ".change_shipping_address");
                            Clicks.click(page + ".address_edit_link");
                        } else {
                            Clicks.click(page + ".add_shipping_address_button");
                        }
                    } else if (onPage("shipping_payment_signed_in")) {
                        page = "shipping_payment_signed_in";
                        Clicks.click(page + ".address_edit_link");
                    }
                    CreateProfileMEW.removeAddress();
            //        click(page + ".add_shipping_address_button");
                    CreateProfileMEW.addNewAddress();
                  //  untilElementPresent(page + ".address_edit_link");
                    Wait.untilElementPresent(page + ".message");
                    new CheckoutUtils().navigateToCheckout(signIn, false);
//                    visit("responsive_checkout_signed_in");
                }
            } else {
                Assert.fail("DATA ERROR : SDD address not added.");
            }
        } else {
            if (bloomingdales()) {
                new Checkout().fillShippingData(true, false, opts);
            } else {
                new com.macys.sdt.shared.actions.website.mcom.pages.checkout.Checkout().fillShippingData(true, false, opts);
            }
        }
    }

    @When("^I select sdd_shipping in shipping methods using mobile website$")
    public void I_select_sdd_shipping_in_shipping_methods_using_mobile_website() throws Throwable {
        int i = 0;
        if(onPage("shipping_payment_signed_in")){
            int num_tiles = Elements.findElements("shipping_payment_signed_in.shipping_address_tiles").size();
            while (!Elements.elementPresent("shipping_payment_signed_in.select_sdd") && i++ < num_tiles)
                Clicks.clickRandomElement("shipping_payment_signed_in.shipping_address_tiles");

            if (Elements.elementPresent("shipping_payment_signed_in.select_sdd")) {
                Clicks.click(Elements.element("shipping_payment_signed_in.select_sdd"));
            } else {
                Assert.fail("ERROR-DATA: Doesn't display sdd shipping method");
            }
        } else {
            Clicks.click("responsive_checkout_signed_in.change_shipping_method");
            if (Elements.elementPresent("responsive_checkout_signed_in.sdd_shipping")) {
                Clicks.click(Elements.element("responsive_checkout_signed_in.sdd_shipping"));
            } else {
                Assert.fail("ERROR-DATA: Doesn't display sdd shipping method");

            }
        }
    }

    @And("^I select pick up option for bops item using mobile website$")
    public void I_select_pick_up_option_for_bops_item_using_mobile_website() throws Throwable {
        Clicks.click("shopping_bag.bops_available");
        if (Elements.elementPresent("shopping_bag.bops_stores")) {
            Clicks.click("shopping_bag.bops_stores");
            Clicks.click("shopping_bag.select_bops");
            Clicks.click("shopping_bag.apply");
            Wait.untilElementPresent("shopping_bag.bag_items");
        } else {
            Assert.fail("ERROR-DATA: BOPS stores not available");
        }


    }

    @When("^I place an Order using mobile site$")
    public void I_place_an_order() throws Throwable {
        pausePageHangWatchDog();
        Boolean responsive = !onPage("order_review");
        Wait.untilElementPresent((responsive ? "responsive_order_summary" : "order_review") + ".place_order");
        new com.macys.sdt.shared.actions.website.mcom.pages.checkout.Checkout().selectPlaceOrderButton();
        Wait.secondsUntilElementNotPresent((responsive ? "responsive_order_summary" : "order_review") + ".place_order", 10);
        Wait.secondsUntilElementNotPresent((responsive ? "responsive_order_summary" : "order_review") + ".mask", 10);
        Wait.secondsUntilElementPresent((responsive ? "responsive_order_confirmation" : "order_confirmation") + ".verify_page", 20);
        Assert.assertTrue("Order not placed successfully!!", onPage("responsive_order_confirmation"));
        resumePageHangWatchDog();
        System.out.println("sucessfuly placed an order");
    }

    @Then("^I verify the promo code validation error message appeared in mobile website$")
    public void I_verify_the_promo_code_validation_error_message_appeared_in_mobile_website() throws Throwable {
        try {
            Elements.elementPresent(Elements.element("shopping_bag.promo_error"));
        } catch (NoSuchElementException e) {
            Assert.fail("Error message is not present on page");
        }
    }

    @And("^I select checkout with paypal in mobile site$")
    public void I_select_checkout_with_paypal() throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("I_select_checkout_with_paypal()");

        if (!onPage("shopping_bag"))
            Navigate.visit("shopping_bag");
        Wait.untilElementPresent("shopping_bag.checkout_with_paypal");
        Clicks.click("shopping_bag.checkout_with_paypal");
    }

    @When("^I login into Paypal account using mobile site$")
    public void I_login_into_paypal_account() throws Throwable {
        new PaypalLogin().login();
    }

    @And("^I checkout from Paypal review page using mobile site$")
    public void I_checkout_from_paypal_review_page() throws Throwable {
        Clicks.click("paypal_login.continue");
        Wait.secondsUntilElementNotPresent("paypal_login.continue", (safari() ? 15 : 5));
        if (safari() || ie())
            Thread.sleep(5000);
    }


    @When("^I remove the promo code using mobile website$")
    public void I_remove_the_promo_code_using_mobile_website() throws Throwable {
        try {
            Clicks.click(Elements.element("shopping_bag.promocode_remove"));
            Wait.forPageReady();
        } catch (NoSuchElementException e) {
            Assert.fail("Element is not present on page");
        }
    }

    @And("^I apply (valid|invalid) promo code \"([^\"]*)\" using mobile website$")
    public void I_apply_promo_code_using_mobile_website(String validity, String promo_code) throws Throwable {
        try {
            Assert.assertTrue("ERROR: promo code field is not present", Wait.untilElementPresent(Elements.element("shopping_bag.promocode_area")));
            Clicks.click(Elements.element("shopping_bag.promocode_area"));
            if (prodEnv() || (validity.equals("invalid"))) {
                TextBoxes.typeTextbox(Elements.element("shopping_bag.text_promocode"), promo_code);
            } else {
                TextBoxes.typeTextbox(Elements.element("shopping_bag.text_promocode"), promo_code);
            }
            Clicks.click(Elements.element("shopping_bag.btn_promocode_apply"));
        } catch (NoSuchElementException e) {
            Assert.fail("Element is not visible on page: " + e);
        }

        if (validity.equals("valid"))
            Assert.assertTrue("ERROR-DATA: Not a valid promo code", Wait.untilElementPresent(Elements.element("shopping_bag.promocode_remove")));
        else
            Assert.assertTrue("ERROR-DATA: Not an invalid promo code", Wait.untilElementPresent(Elements.element("shopping_bag.promo_error")));
    }

    @And("^I add fully_enrolled_usl id on shopping bag page using mobile website$")
    public void I_add_fully_enrolled_usl_id_on_shopping_bag_page_using_mobile_website() throws Throwable {
        if (Elements.getText("shopping_bag.plenti_id").contains("Add your Plenti # to earn points on qualifying purchases")) {
            String plenti_id = TestUsers.getEnrolledUslId().getPlentiId();
            Clicks.click("shopping_bag.plenti_container");
            Clicks.click("shopping_bag.have_plenti_no");
            TextBoxes.typeTextNEnter("shopping_bag.plenti_id_textbox", plenti_id);
            Assert.assertTrue("ERROR-ENV: Unable to add plenti id to shopping bag", Wait.untilElementPresent("shopping_bag.plenti_apply"));
            Clicks.click("shopping_bag.plenti_apply");
        }
    }

    @Then("^I verify the functionality of merge bag using mobile website$")
    public void I_verify_the_functionality_of_merge_bag_using_mobile_website() throws Throwable {
        shouldBeOnPage("merged_bag");
        if (macys()) {
            String expected_message = "One or more items from your previous shopping session have been added to your Shopping Bag.";
            Assert.assertEquals(expected_message, Elements.getText("merged_bag.merged_message"));
        }
    }

    @And("^I add shipping address if not present on shipping page using mobile website for signed in user$")
    public void I_add_shipping_address_if_not_present_on_shipping_page_using_mobile_website_for_signed_in_user() throws Throwable {
        if (!Elements.elementPresent("responsive_checkout_signed_in.change_shipping_address"))  {
            Clicks.click("responsive_checkout_signed_in.add_shipping_address_button");
            CreateProfileMEW.addNewAddress();
            if (onPage("my_address_book")) {
                new CheckoutUtils().navigateToCheckout(true, false);
            }
            Wait.untilElementPresent("responsive_checkout_signed_in.change_shipping_address");
        }
    }

    @When("^I remove (registry|normal) item from mobile shopping bag page$")
    public void I_remove__item_from_mobile_shopping_bag_page(String itemType) throws Throwable {
        shouldBeOnPage("shopping_bag");
        List <WebElement> items = Elements.findElements("shopping_bag.line_items");
        if (items == null || items.size() == 0) {
            Assert.fail("No products in shopping bag");
        } else {
            for (int i = 0; i < items.size(); i++) {
                boolean registryItem = !items.get(i).findElements(Elements.element("shopping_bag.registrant_name_details")).isEmpty();
                if (registryItem == (itemType.equals("registry"))) {
                    Clicks.click(Elements.findElements("shopping_bag.remove_item").get(i));
                    break;
                }
            }
        }
    }

    @Then("^I should see only (registry|normal) item is present in mobile shopping bag page$")
    public void I_should_see_only_item_is_present_in_mobile_shopping_bag_page(String itemType) throws Throwable {
        Navigate.browserRefresh();
        shouldBeOnPage("shopping_bag");
        List <WebElement> items = Elements.findElements("shopping_bag.line_items");
        if (items == null || items.size() == 0) {
            Assert.fail("No products in shopping bag");
        } else {
            boolean registryItem = itemType.equals("registry");
            items.forEach(el ->
                    Assert.assertEquals((registryItem ? "Normal" : "Registry") + " item is present in shopping bag page",
                            registryItem, !el.findElements(Elements.element("shopping_bag.registrant_name_details")).isEmpty()));
        }
    }

    @Then("^I should see loyalty points section on mobile order conformation page$")
    public void I_should_see_loyalty_points_section_on_mobile_order_conformation_page() throws Throwable{
        shouldBeOnPage("responsive_order_confirmation");
        Assert.assertTrue("Loyalty points section not displayed on order confirmation page",
                Wait.untilElementPresent("responsive_order_confirmation.loyalty_points_section"));
    }
}
