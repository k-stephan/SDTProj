package com.macys.sdt.shared.steps.website;


import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.framework.utils.rest.services.GiftCardService;
import com.macys.sdt.shared.actions.website.bcom.pages.CheckoutPageBcom;
import com.macys.sdt.shared.actions.website.bcom.pages.PaypalLogin;
import com.macys.sdt.shared.actions.website.mcom.pages.checkout.Checkout;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.ShoppingBag;
import com.macys.sdt.shared.utils.CheckoutUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;

import static com.macys.sdt.framework.interactions.Elements.element;
import static com.macys.sdt.framework.utils.TestUsers.getValid3DSecureCard;

public class CheckoutSteps extends StepUtils {

    public static Map<String, Object> MB_MONEY_ESTIMATED_DATA;
    public String cardType = null;
    String beforePrice;
    public static HashMap shippingDetails = null;
    public static String selectedShipping = null;

    @And("^I change the cookie to \"([^\"]*)\"$")
    public static void i_change_the_cookie_value(String checkoutType) throws Throwable {
        if (checkoutType.contains("responsive")) {
            Cookies.forceRc();
        } else {
            Cookies.forceNonRc();
        }
    }

    /**
     * This is the master step for going through the checkout flow
     * <p>
     * Options for Responsive pages:<br>
     * <code>shipping, payment, order review, order confirmation</code><br>
     * Options for international and legacy checkout pages:<br>
     * <code>shipping and payment, order review, order confirmation</code><br>
     * Options for user type:<br>
     * <code>guest, signed in</code><br>
     * For international or bops users include "iship" and "bops" respectively in the user type<br>
     * Any address parameters should be in the "address" field. supported parameters:<br>
     * <code>paypal_address</code>
     * </p>
     *
     * @param pageName name of the page you want to end up on
     * @param userType either signed in or guest user
     * @param country  country the shipping address should be from (default US)
     * @param address  Any other custom address requirements
     * @throws Throwable if any exception occurs
     */
    @And("^I checkout until I reach the (shipping|payment|shipping (?:and|&) payment|order review|order confirmation) page as an? \"([^\"]*)\" user(?: from \"([^\"]*)\")?(?: with \"([^\"]*)\")?$")
    public void I_checkout_until_I_reach_the_page_as_a_user(String pageName, String userType, String country, String address) throws Throwable {
        userType = userType.toLowerCase();
        boolean bops = userType.contains("bops");
        boolean signIn = userType.contains("signed in") || signedIn();
        boolean responsive;
        HashMap<String, String> opts = new HashMap<>();
        if (address != null && address.equals("paypal_address")) {
            opts.put("paypal_eligible", "true");
        }
        if (country == null) {
            country = "United States";
        }
        boolean iship = userType.contains("iship") || !country.equalsIgnoreCase("United States");
        opts.put("country", country);
        opts.put("checkout_eligible", "true");

        if (!(onPage("responsive_checkout", "checkout"))) {
            new CheckoutUtils().navigateToCheckout(signIn, iship);
        }

        responsive = onPage("responsive_checkout", "responsive_checkout_signed_in");

        //checkout can take some time, page hang watchdog can safely be paused
        pausePageHangWatchDog();
        if (iship) {
            new Checkout().ishipCheckout(pageName, opts);
        } else if (responsive) {
            // responsive checkout is identical across mcom and bcom
            CheckoutUtils.RCPage page = CheckoutUtils.RCPage.fromString(pageName);
            if (signIn) {
                new Checkout().rcSignedIn(page, opts, bops);
            } else {
                new Checkout().rcGuest(page, opts, bops);
            }
        } else if (signIn) {
            if (macys() || bops) {
                // bops is also the same across both platforms
                new Checkout().signInCheckout(pageName, opts, bops);
            } else {
                new CheckoutPageBcom().signInCheckout(pageName, opts);
            }
        } else {
            new CheckoutPageBcom().guestCheckout(pageName, opts, bops);
        }
        resumePageHangWatchDog();
    }

    /**
     * Clicks "continue" on the responsive guest shipping page
     */
    @And("^I select continue button on guest shipping page$")
    public void I_select_continue_button_on_guest_shipping_page() {
        if (macys()) {
            Clicks.click("responsive_checkout.continue_shipping_checkout_button");
        } else {
            if (!Clicks.clickIfPresent("responsive_checkout.continue_shipping_checkout_button")) {
                Clicks.click("shipping_guest.continue_checkout");
            }
        }
    }

    /**
     * Fills in all shipping data on any responsive checkout page
     */
    @When("^I enter shipping address on guest shipping page$")
    public void I_enter_shipping_address_on_guest_shipping_page() {
        HashMap<String, String> opts = new HashMap<>();
        pausePageHangWatchDog();
        opts.put("checkout_eligible", "true");
        if (macys()) {
            new Checkout().fillShippingData(true, false, opts);
        } else {
            new CheckoutPageBcom().fillGuestShippingData(opts, false);
        }
        resumePageHangWatchDog();
    }

    /**
     * Fills in all payment data on any responsive checkout page
     */
    @And("^I fill in payment information on guest payment page$")
    public void I_fill_in_payment_information_on_guest_payment_page() {
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        new Checkout().fillPaymentData(true, false, opts);
    }

    /**
     * Click the "continue" button on any guest payment page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select continue button on guest payment page$")
    public void I_select_continue_button_on_guest_payment_page() throws Throwable {
        // run this step only on Batch Mode enabled QA environment
        // We can place orders even though site is in batch mode. Batch mode and product unavailability are two different things, user con't place order if the product is unavaibale.
        //        if (MainRunner.batchMode) {
        if (onPage("responsive_checkout")) {
            Clicks.clickWhenPresent("responsive_checkout.continue_payment_checkout_button");
            Wait.untilElementPresent("responsive_checkout.place_order");
        } else {
            Clicks.click("payment_guest.continue_checkout_button");
            Wait.untilElementPresent("order_review.place_order_button");
        }
        //        } else {
        //            System.err.println("Environment not in batch mode, unable to navigate to payment panel due to product unavailability.");
        //        }
    }

    /**
     * Click the "checkout" button on the add to bag overlay
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I checkout on add to bag overlay$")
    public void I_checkout_on_add_to_bag_overlay() throws Throwable {
        try {
            Clicks.click("add_to_bag_dialog.add_to_bag_checkout");
        } catch (NoSuchElementException e) {
            if (Elements.elementPresent("add_to_bag_dialog.master_add_to_bag_checkout")) {
                Clicks.click("add_to_bag_dialog.master_add_to_bag_checkout");
            } else {
                Navigate.visit("shopping_bag");
            }
        }
        Wait.forPageReady("shopping_bag");
        ShoppingBag.removeBonusItemsFromBag();
        Wait.forPageReady();
    }

    /**
     * Adds credit card data during checkout
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add a credit card during checkout$")
    public void I_add_a_credit_card_during_checkout() throws Throwable {
        HashMap<String, String> opts = new HashMap<>();
        pausePageHangWatchDog();
        opts.put("checkout_eligible", "true");
        boolean responsive = onPage("responsive_checkout");
        boolean iship = onPage("iship_checkout");
        if (responsive || macys()) {
            if (!responsive && !iship && !onPage("shipping_payment_signed_in")) {
                Assert.fail("Tried to add credit card while not on checkout page");
            }

            if (!responsive && !iship) {
                Clicks.click("shipping_payment_signed_in.add_credit_card_button");
            }
            new Checkout().fillPaymentData(responsive, iship, opts);
        } else {
            new CheckoutPageBcom().fillGuestPaymentData(null, false);
        }
        resumePageHangWatchDog();
    }

    /**
     * Fills out sign in information on the checkout sign in screen
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I sign in during checkout$")
    public void I_sign_in_during_checkout() throws Throwable {
        if (!onPage("shipping_payment_signed_in")) {
            Navigate.visit("shopping_bag");
            Clicks.click("shopping_bag.continue_checkout_image");
        }
        if (prodEnv()) {
            UserProfile customer = TestUsers.getProdCustomer();
            TextBoxes.typeTextbox("checkout_sign_in.email", customer.getUser().getProfileAddress().getEmail());
            TextBoxes.typeTextbox("checkout_sign_in.password", customer.getUser().getLoginCredentials().getPassword());
        } else {
            UserProfile customer = TestUsers.getCustomer(null);
            TextBoxes.typeTextbox("checkout_sign_in.email", customer.getUser().getProfileAddress().getEmail());
            TextBoxes.typeTextbox("checkout_sign_in.password", customer.getUser().getLoginCredentials().getPassword());
        }
        Clicks.click("checkout_sign_in.checkout_button");
    }

    /**
     * Selects bops option instead of shipping if available
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select pick up option for bops item$")
    public void I_select_pick_up_option_for_bops_item() throws Throwable {
        if (!onPage("shopping_bag")) {
            Navigate.visit("shopping_bag");
        }
        Wait.forPageReady();
        Wait.untilElementPresent("shopping_bag.pick_it_up_button");
        if (!MEW()) {
            Assert.assertTrue("ERROR-DATA: Selected Product is not a BOPS Eligible one", Elements.elementPresent("shopping_bag.pick_it_up_button"));
        }
        Clicks.javascriptClick("shopping_bag.pick_it_up_button");
        if (MEW()) {
            Clicks.click("shopping_bag.pick_up_in_store_apply");
        }
    }

    /**
     * Adds the specified type of shipping address on the given checkout shipping page
     *
     * @param eligibility sdd or paypal eligible or ineligible
     * @throws Throwable if any exception occurs
     */
    @And("^I enter (sdd_eligible|paypal_eligible|sdd_ineligible) address on shipping page for (?:guest|signed in|responsive guest|responsive signed in) user$")
    public void I_enter_sdd_eligible_address_on_shipping_page_for_signed_in_user(String eligibility) throws Throwable {
        boolean sdd = eligibility.equals("sdd_eligible");
        boolean paypal = eligibility.equals("paypal_eligible");
        HashMap<String, String> opts = new HashMap<>();
        if (sdd || paypal) {
            opts.put(eligibility, "true");
        }
        opts.put("site", TestUsers.getSiteType());
        opts.put("checkout_eligible", "true");
        if (signedIn()) {
            new CheckoutPageBcom().editShippingAddress(opts);
        } else {
            if (macys()) {
                new Checkout().fillShippingData(true, false, opts);
            } else {
                new CheckoutPageBcom().fillGuestShippingData(opts, false);
            }
        }
    }

    /**
     * Selects a second day shipping option if available
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select sdd_shipping in shipping methods$")
    public void I_select_sdd_shipping_in_shipping_methods() throws Throwable {
        if (signedIn()) {
            // Note: Pending - The below code is  for non-responsive signed in flow need to add the steps for responsive signed in flow
            if (onPage("responsive_checkout_signed_in")) {
                Clicks.clickWhenPresent("responsive_checkout_signed_in.change_shipping_method");
                Assert.assertTrue("ERROR-DATA: Added Product or Address is not a sdd eligible one", Wait.untilElementPresent("responsive_checkout_signed_in.sdd_shipping"));
                Clicks.click("responsive_checkout_signed_in.sdd_shipping");
                Clicks.click("responsive_checkout_signed_in.save_shipping_method");
            } else {
                int count = 0;
                while (!Elements.elementPresent("shipping_payment_signed_in.sdd_shipping") && count++ < 10) {
                    Clicks.clickRandomElement("shipping_payment_signed_in.shipping_address_tiles");
                }
                Assert.assertTrue("ERROR-DATA: Added Product or Address is not a sdd eligible one", Elements.elementPresent("shipping_payment_signed_in.sdd_shipping"));
                Clicks.click("shipping_payment_signed_in.sdd_shipping");
                if (bloomingdales()) {
                    Wait.untilElementPresent("shipping_payment_signed_in.highlighted_sdd_address");
                }
            }
        } else {
            String page = onPage("responsive_checkout") ? "responsive_checkout" : "shipping_guest";
            Assert.assertTrue("ERROR-DATA: Added Product or Address is not a sdd eligible one", Elements.elementPresent(page + ".sdd_shipping"));
            Clicks.click(page + ".sdd_shipping");
        }
    }

    /**
     * Adds the given promo code to the shopping cart
     *
     * @param validity   valid or invalid expected
     * @param promo_code promo code to add
     * @throws Throwable if any exception occurs
     */
    @And("^I apply (valid|invalid) promo code \"([^\"]*)\" using website$")
    public void I_apply_promo_code_using_website(String validity, String promo_code) throws Throwable {
        try {
            Assert.assertTrue("ERROR: promo code field is not present", Wait.untilElementPresent("shopping_bag.promo_code"));
            if (prodEnv() || validity.equals("invalid")) {
                TextBoxes.typeTextbox("shopping_bag.promo_code", promo_code);
            } else {
                TextBoxes.typeTextbox("shopping_bag.promo_code", promo_code);
            }
            Clicks.click("shopping_bag.apply_promo_code_button");
        } catch (NoSuchElementException e) {
            Assert.fail("ElementTest is not visible on page: " + e);
        }

        if (validity.equals("valid")) {
            Assert.assertTrue("ERROR-DATA: Not a valid promo code", Wait.untilElementPresent("shopping_bag.promo_code_remove"));
        } else {
            Assert.assertTrue("ERROR-DATA: Not an invalid promo code", Wait.untilElementPresent("shopping_bag.error_message"));
        }
    }

    /**
     * Removes the firstpromo code from the shopping cart
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I remove the promo code$")
    public void I_remove_the_promo_code() throws Throwable {
        if (Elements.elementPresent("shopping_bag.promo_code_remove")) {
            Clicks.click("shopping_bag.promo_code_remove");
        } else {
            System.out.println("No promo codes to remove.");
        }
    }

    /**
     * Selects the "checkout with paypal" button on the shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select checkout with paypal$")
    public void I_select_checkout_with_paypal() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("I_select_checkout_with_paypal()");
        }

        if (!onPage("shopping_bag")) {
            Navigate.visit("shopping_bag");
        }
        Wait.untilElementPresent("shopping_bag.checkout_with_paypal");
        Clicks.click("shopping_bag.checkout_with_paypal");
    }

    /**
     * Verifies that the browser is on the paypal login page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see paypal login page$")
    public void I_should_see_paypal_login_page() throws Throwable {
        Wait.forPageReady("PayPal");
        Thread.sleep(5000);
        if (!Elements.elementPresent("paypal_login.login_iframe")) {
            Assert.fail("Not on paypal login page");
        }
    }

    /**
     * Logs in to a stored paypal account
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I login into paypal account$")
    public void I_login_into_paypal_account() throws Throwable {
        new PaypalLogin().login();
    }

    /**
     * Clicks "continue" on the paypal login page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I checkout from paypal review page$")
    public void I_checkout_from_paypal_review_page() throws Throwable {
        Clicks.click("paypal_login.continue");
        Wait.secondsUntilElementNotPresent("paypal_login.continue", (safari() ? 15 : 5));
        if (safari() || ie()) {
            Thread.sleep(5000);
        }
    }

    /**
     * Clicks the "place order" button on the order review page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I place an order$")
    public void I_place_an_order() throws Throwable {
        pausePageHangWatchDog();
        Boolean responsive = !onPage("order_review");
        Wait.untilElementPresent((responsive ? "responsive_order_summary" : "order_review") + ".place_order");
        new Checkout().selectPlaceOrderButton();
        Wait.secondsUntilElementNotPresent((responsive ? "responsive_order_summary" : "order_review") + ".place_order", 10);
        Wait.secondsUntilElementNotPresent((responsive ? "responsive_order_summary" : "order_review") + ".mask", 10);
        Wait.secondsUntilElementPresent((responsive ? "responsive_order_confirmation" : "order_confirmation") + ".verify_page", 20);
        Assert.assertTrue("Order not placed successfully!!", onPage("responsive_order_confirmation"));
        resumePageHangWatchDog();
    }

    /**
     * Selects the checkbox to make this order a gift and adds a message
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select gift options on shipping page$")
    public void i_select_gift_options_on_shipping_page() throws Throwable {
        pausePageHangWatchDog();
        if (onPage("responsive_checkout")) {
            Clicks.selectCheckbox("responsive_shipping_options.gift_option");
            Clicks.selectCheckbox("responsive_shipping_options.gift_message");
            TextBoxes.typeTextbox("responsive_shipping_options.gift_message_field1", "test message");
            //            Clicks.selectCheckbox("responsive_shipping_options.gift_box");
        } else {
            Clicks.click("shipping_guest.gift_option");
            Clicks.selectCheckbox("shipping_guest.gift_message");
            TextBoxes.typeTextbox("shipping_guest.gift_message_field1", "test message");
            Clicks.selectCheckbox("shipping_guest.gift_box");
        }
        resumePageHangWatchDog();
    }

    /**
     * Enters random payment details on any checkout payment page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I enter payment details on payment page$")
    public void i_enter_payment_details_on_payment_page() throws Throwable {
        boolean responsive = onPage("responsive_checkout");
        boolean iship = onPage("iship_checkout");
        if (macys()) {
            new Checkout().fillCreditCardData(responsive, iship);
        } else {
            new CheckoutPageBcom().fillGuestCardDetails(responsive, iship);
        }
    }

    /**
     * Checks the "use my shipping address" checkbox on the payment page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select use my shipping address checkbox on payment page$")
    public void i_select_use_my_shipping_address_checkbox_on_payment_page() throws Throwable {
        if (macys()) {
            if (Elements.elementPresent("responsive_payment_guest_section.use_shipping_address")) {
                Clicks.selectCheckbox("responsive_payment_guest_section.use_shipping_address");
            }
        } else {
            if (Elements.elementPresent("payment_guest.use_shipping_address")) {
                Clicks.selectCheckbox("payment_guest.use_shipping_address");
            } else {
                Clicks.selectCheckbox("responsive_payment_guest_section.use_shipping_address");
            }
        }
    }

    /**
     * Fills in contact details on the payment page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I enter contact details on payment page$")
    public void i_enter_contact_details_on_payment_page() throws Throwable {
        boolean responsive = onPage("responsive_checkout") || onPage("responsive_checkout_signed_in");
        String page = responsive ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout")
                : (!signedIn() ? "payment_guest" : "shipping_payment_signed_in");
        new Checkout().fillContactDetails(responsive, page, null);
    }

    /**
     * Selects the option to pay for entire order with USL and clicks place order
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I pay whole transaction amount with usl on payment page and place order$")
    public void I_use_usl_as_payment_to_perform_the_transaction() throws Throwable {
        pausePageHangWatchDog();
        CheckoutUtils.RCPage page = signedIn() ? CheckoutUtils.RCPage.SHIPPING_AND_PAYMENT : CheckoutUtils.RCPage.REVIEW;
        Checkout.addUslAsPayment();
        String plenti_element;
        if (MEW()) {
            plenti_element = "responsive_order_summary.gift_plenti_reward_cards";
        } else {
            plenti_element = page + ".gift_plenti_reward_cards";
        }
        Wait.untilElementPresent(plenti_element);
        TestUsers.getUSLInformation().setRedeemedPlentiPoints(Elements.getText(plenti_element).replaceAll("\\D+", ""));
        i_enter_billing_details_on_payment_page();
        i_enter_contact_details_on_payment_page();
        if (signedIn()) {
            i_save_information_on_responsive_checkout_signin();
            Navigate.browserRefresh();
        } else if (signedIn()) {
            Clicks.click("shipping_payment_signed_in.continue_checkout_button");
        } else {
            I_select_continue_button_on_guest_payment_page();
        }
        I_place_an_order();
        resumePageHangWatchDog();
    }

    /**
     * Applies an offer from My Wallet to the current cart
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I apply one offer from wallet offers on shopping bag page$")
    public void I_apply_one_offer_from_wallet_offers_on_shopping_bag_page() throws Throwable {
        beforePrice = Elements.getText("shopping_bag.subtotal_price");
        beforePrice = beforePrice.replaceAll("[$.]", "").replaceAll("\\s+", "");
        Clicks.click("shopping_bag.see_all_my_offers");
        if (Wait.untilElementPresent("shopping_bag.offers_overlay")) {
            Elements.getRandomElement("shopping_bag.offers");
            Clicks.click("shopping_bag.apply_promotion");
        }
    }

    @Then("^I should see price adjusted as per applied offer$")
    public void I_should_see_price_adjusted_as_per_applied_offer() throws Throwable {
        String discount = Elements.getText("shopping_bag.promo_discount");
        discount = discount.replaceAll("[-$.]", "").replaceAll("\\s+", "");
        String after_price = Elements.getText("shopping_bag.subtotal_price");
        after_price = after_price.replaceAll("[$.]", "").replaceAll("\\s+", "");
        if (Integer.parseInt(after_price) == (Integer.parseInt(beforePrice) - Integer.parseInt(discount))) {
            System.out.println("Promotion from wallet offers is applied successfully");
        } else {
            Assert.fail("Unable to apply promotion from wallet offers.");
        }
    }

    /**
     * Fills in a billing address on the checkout payment page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I enter billing details on payment page$")
    public void i_enter_billing_details_on_payment_page() throws Throwable {
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        if (macys()) {
            new Checkout().fillBillingAddress(opts);
        } else {
            new CheckoutPageBcom().fillBillingAddress(opts);
        }

    }

    /**
     * TODO: duplicate?
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select billing address same as shipping address on payment page and save it$")
    public void i_select_billing_address_same_as_shipping_address_on_checkout_page() throws Throwable {
        this.i_select_use_my_shipping_address_checkbox_on_payment_page();
        this.i_save_information_on_responsive_checkout_signin();
    }

    /**
     * Saves current shipping information on the signed in checkout page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^i_save_information_on_responsive_checkout_signin")
    public void i_save_information_on_responsive_checkout_signin() throws Throwable {
        Clicks.clickIfPresent("responsive_checkout_signed_in.save_button");
    }

    /**
     * Attempts to merge old products into current cart
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify the functionality of merge bag$")
    public void I_verify_the_functionality_of_merge_bag() throws Throwable {
        if (Wait.untilElementPresent("shopping_bag.add_to_current_bag")) {
            Clicks.click("shopping_bag.add_to_current_bag");
        } else {
            Assert.fail("The add to current bag button is not visible.");
        }
        ShoppingBag.removeBonusItemsFromBag();
        Navigate.browserRefresh();
        List<Product> bagProductsList = ShoppingBag.getAllProductDetails();
        int quantity = 0;
        if (bagProductsList.size() == 1) {
            for (Product product : bagProductsList) {
                quantity += product.quantity;
            }
        }
        if ((bagProductsList.size() >= 2 || quantity >= 2) && !Elements.elementPresent("shopping_bag.add_to_current_bag")) {
            System.out.println("The merge bag scenario is successfull");
        } else {
            Assert.fail("The merge bag scenario failed.");
        }
    }

    /**
     * Selects paypal option on checkout payment page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I select checkout with paypal on payment page$")
    public void i_select_checkout_with_paypal_on_payment_page() throws Throwable {
        String page = (signedIn() ? (onPage("shipping_payment_signed_in") ? "shipping_payment_signed_in" : "responsive_checkout_signed_in") : "responsive_checkout");
        Wait.secondsUntilElementPresent(page + ".paypal_radio_button", 10);
        Clicks.click(page + ".paypal_radio_button");
        i_enter_contact_details_on_payment_page();
        Wait.untilElementPresent(page + ".continue_with_paypal");
        Clicks.click(page + ".continue_with_paypal");
    }

    /**
     * Clicks the store availability button on shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I click store pickup availability link on bag page$")
    public void i_select_store_pickup_availability_link_on_bag_page() throws Throwable {
        if (!onPage("shopping_bag")) {
            Navigate.visit("shopping_bag");
        }
        Wait.secondsUntilElementPresentAndClick("shopping_bag.bops_change_store_button", 5);
    }

    /**
     * Selects a store in the change bops store dialogue
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I select bops store button in change pickup store dialog$")
    public void i_select_bops_store_button_in_change_pickup_store_dialog() throws Throwable {
        if (macys()) {
            Wait.forLoading(By.id("loading_mask"));
        } else {
            Wait.secondsUntilElementNotPresent(By.className("loading"), 50);
        }

        try {
            Wait.secondsUntilElementPresent("change_pickup_store_dialog.search_results_section", 100);
        } catch (Exception e) {
            Assert.fail("Element is not present" + element("change_pickup_store_dialog.search_results_section"));
        }
        if (Elements.elementPresent("change_pickup_store_dialog.select_store_button")) {
            Clicks.click("change_pickup_store_dialog.select_store_button");
        } else {
            Assert.fail("No store for that zipcode");
        }
    }

    /**
     * Saves and closes the change bops store dialogue
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I save & close the bops change store dialog$")
    public void I_save_close_the_bops_change_store_dialog() throws Throwable {
        Clicks.click("change_pickup_store_dialog.save");
    }

    /**
     * Verifies that the given shipping methods are available
     *
     * @param shippingMethods List of expected shipping methods
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see below shipping options:$")
    public void I_should_see_below_shipping_options(Map<String, Boolean> shippingMethods) throws Throwable {
        String page = signedIn() ? (onPage("responsive_checkout_signed_in") ? "responsive_checkout_signed_in" : "shipping_payment_signed_in") : (onPage("responsive_checkout") ? "responsive_checkout" : "shipping_guest");
        if (page.equals("responsive_checkout_signed_in")) {
            Clicks.click(page + ".change_shipping_method");
        }
        Utils.threadSleep(2000, null);
        // Note: Pending - Need to write code for responsive checkout signed in user flow to click on change button and then verify the shipping options.
        for (Map.Entry<String, Boolean> entry : shippingMethods.entrySet()) {
            if (Elements.elementPresent(page + "." + (signedIn() && macys() ? "signin_" : "") + entry.getKey()) != entry.getValue()) {
                String visible = "be visible";
                if (entry.getValue().equals(false)) {
                    visible = "not be visible";
                }
                Assert.fail("Expected element " + entry.getKey() + " should " + visible);
            }
        }
    }

    /**
     * Verifies that the shipping note has the given text
     *
     * @param display  shipping method (same day delivery or normal)
     * @param pageName which checkout page
     * @param message  expected message
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see following delivery ship note for (same day delivery|normal) shipping method on \"([^\"]*)\" page:$")
    public void I_should_see_following_delivery_ship_note_for_same_day_delivery_shipping_method_on_page(String display, String pageName, List<String> message) throws Throwable {
        String page = new Checkout().getPageName(pageName);
        if (!page.equals("responsive_order_review_section")) {
            Assert.assertTrue("Expected delivery ship note not displayed!!", (display.contains("normal") ? Elements.getText(page + ".normal_shipping_note") : Elements.getText(page + ".sdd_shipping_note")).contains(message.get(message.size() - 1)));
        }
        if (page.equals("responsive_checkout_signed_in") && Elements.elementPresent("responsive_checkout_signed_in.save_shipping_method")) {
            Clicks.click("responsive_checkout_signed_in.save_shipping_method");
        }
    }

    /**
     * Verifies the display of the schedule delivery alert
     *
     * @param message expected message
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see following alert message to schedule delivery on order confirmation page:$")
    public void I_should_see_following_alert_message_to_schedule_delivery_on_order_confirmation_page(List<String> message) throws Throwable {
        String page = onPage("responsive_order_confirmation") ? "responsive_order_confirmation" : "order_confirmation";

        message.stream().filter(msg -> !Elements.getText(page + ".alert_message").contains(msg))
                .forEach(msg -> Assert.fail("Expected alert message to schedule delivery: " + message + " is not displayed."));
    }

    /**
     * Verifies that the estimated delivery date is present on the given page
     *
     * @param pageName checkout page to check on
     * @param message  expected delivery message
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see following estimated delivery message on \"([^\"]*)\" page:$")
    public void I_should_see_following_estimated_delivery_message_on_page(String pageName, List<Map<String, String>> message) throws Throwable {
        String page = new Checkout().getPageName(pageName);
        String actualShippingText, expectedShippingText;
        if (page.equals("responsive_order_review_section") && bloomingdales()) {
            expectedShippingText = message.get(1).get("sdd_estimated_delivery");
            actualShippingText = Elements.findElement(page + ".shipping_summary").findElements(By.tagName("p")).get(Elements.findElement(page + ".shipping_summary").findElements(By.tagName("p")).size() - 1).getText().replace("–", "-");
        } else {
            expectedShippingText = message.get(0).get("sdd_estimated_delivery").replaceAll("[^A-Za-z0-9 ]", "");
            actualShippingText = Elements.getText(page + (signedIn() && macys() ? ".shipping_methods_set" : ".estimated_delivery")).replaceAll("[^A-Za-z0-9 ]", "");
        }
        Assert.assertTrue("Expected delivery message is not displayed!!", actualShippingText.contains(expectedShippingText));
    }

    /**
     * Verifies that the order number is visible on the order confirmation page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see order number on order receipt page$")
    public void I_should_see_order_number_on_order_receipt_page() throws Throwable {
        switchToFrame("iship_checkout.shipping_iFrame");
        if (Wait.untilElementPresent("order_confirmation.iship_order_number")) {
            System.out.println("The iship order was placed successfully.");
        } else {
            Assert.fail("Unable to place the order");
        }
    }

    /**
     * Adds a 3DSecure credit card as your payment information on the checkout payment page
     *
     * @param cardType what type of card (visa, master card, etc)
     * @throws Throwable if any exception occurs
     */
    @And("^I add 3d secure \"([^\"]*)\" as my card type and checkout$")
    public void I_add_3d_card_as_the_cardtype(String cardType) throws Throwable {
        boolean responsive = onPage("responsive_checkout");
        boolean iship = onPage("iship_checkout");
        this.cardType = cardType;
        pausePageHangWatchDog();
        new Checkout().add3DSecureCard(cardType, responsive, iship);
        if (!signedIn()) {
            i_enter_billing_details_on_payment_page();
            i_enter_contact_details_on_payment_page();
            I_select_continue_button_on_guest_payment_page();
        }
        resumePageHangWatchDog();
    }

    /**
     * Enters 3DSecure login info on checkout payment page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I enter the 3d secure credentials$")
    public void I_enter_the_3d_secure_credentials() throws Throwable {
        pausePageHangWatchDog();
        boolean responsive = onPage("responsive_checkout, responsive_checkout_signed_in".split(", "));
        String page = responsive ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout")
                : "order_review";
        if (responsive) {
            new Checkout().selectPlaceOrderButton();
        }
        if (!Wait.secondsUntilElementPresent(page + ".3d_secure_reviewIframe", (safari() ? 20 : 10))) {
            Assert.fail("3d_secure_Overlay is not present");
        } else {
            switchToFrame(page + ".3d_secure_reviewIframe");
            switchToFrame(page + ".3d_secure_authIframe");
            if (safari()) {
                Wait.secondsUntilElementPresent(page + ".3d_secure_password", 10);
            }
            CreditCard card = getValid3DSecureCard(cardType);
            if (card == null) {
                Assert.fail("Unable to get valid 3DSecure card");
            }
            TextBoxes.typeTextbox(page + ".3d_secure_password", card.getSecurePassword());
            Clicks.click(page + ".3d_secure_submit");
            switchToFrame("default");
            switchToFrame("default");
            Wait.forPageReady();
            if (!responsive) {
                Wait.untilElementPresent("order_review.verify_page");
                Clicks.click("order_review.verify_page");
            }
        }
        resumePageHangWatchDog();
    }

    /**
     * If the current cart doesn't meet the MBMoney threshold, add more products
     */
    @When("^I update the bag to meet the mbMoney earn threshold if the bag not met threshold$")
    public void i_update_the_bag_to_meet_the_mbMoney_earn_threshold_if_the_bag_not_met_threshold() {
        pausePageHangWatchDog();
        if (!onPage("shopping_bag")) {
            Navigate.visit("shopping_bag");
        }
        ShoppingBag.removeBonusItemsFromBag();
        Navigate.browserRefresh();
        Map<String, Object> mbmoneyEstimatedData = ShoppingBag.getMbmoneyEarnInformationBasedOnBagTotal(ShoppingBag.getAllProductDetails(), ShoppingBag.subtotal());

        if (mbmoneyEstimatedData.get("estimatedEarnAmount").equals(0)) {
            Assert.assertFalse("All items in bag are not eligible for mbMoney!!", ((List) mbmoneyEstimatedData.get("mbmoneyEligibleItems")).isEmpty());
            Object check = mbmoneyEstimatedData.get("mbmoneyEligibleItems");
            if (check instanceof List) {
                List eligibleItems = (List) check;
                int randomIndex = 0;
                if (eligibleItems.size() > 1) {
                    Random rand = new Random();
                    randomIndex = rand.nextInt(eligibleItems.size());
                }
                Product randomEligibleItem = (Product) eligibleItems.get(((randomIndex > 0) ? (randomIndex - 1) : randomIndex));
                List<String> quantityOptions = ShoppingBag.quantityOptions(randomEligibleItem);
                ShoppingBag.updateQuantity(randomEligibleItem, quantityOptions.get(quantityOptions.size() - 1));
            }
            System.out.println("Updated the product quantity to meet mbMoney threshold!!");
        }
    }

    /**
     * Verifies that estimated earned mMoney shows up on shopping bag page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see estimated mMoney earned section in the shopping bag page$")
    public void I_should_see_estimated_mMoney_earned_section_in_the_shopping_bag_page() throws Throwable {
        MB_MONEY_ESTIMATED_DATA = ShoppingBag.getMbmoneyEarnInformationBasedOnBagTotal(ShoppingBag.getAllProductDetails(), ShoppingBag.subtotal());
        Map<String, String> actualMmoneyEarnInfo = ShoppingBag.getMmoneyEstimateEarnInformation();
        if (!Elements.elementPresent("shopping_bag.mmoney_earn_logo") &&
                actualMmoneyEarnInfo.get("mmoneyEstimatedEarnAmount").equals(MB_MONEY_ESTIMATED_DATA.get("estimatedEarnAmount")) &&
                actualMmoneyEarnInfo.get("mmoneyNextSpentAmount").equals(MB_MONEY_ESTIMATED_DATA.get("nextSpentAmount")) &&
                actualMmoneyEarnInfo.get("mmoneyNextGetAmount").equals(MB_MONEY_ESTIMATED_DATA.get("nextGetAmount"))) {
            Assert.fail("mMoney earn information is not displaying properly on shopping bag page");
        } else {
            System.out.println("Estimated earn section is displayed on shopping bag page");
        }
    }

    /**
     * Saves estimated mMoney or bMoney earned to a variable
     *
     * @param campaignType mMoney or bMoney
     * @throws Throwable if any exception occurs
     */
    @When("^I save the (mMoney|bMoney) eligible amount information in bag$")
    public void I_save_the_mMoney_eligible_amount_information_in_bag(String campaignType) throws Throwable {
        MB_MONEY_ESTIMATED_DATA = ShoppingBag.getMbmoneyEarnInformationBasedOnBagTotal(ShoppingBag.getAllProductDetails(), ShoppingBag.subtotal());
        System.out.println("Saved " + campaignType + "eligible details on bag page");
    }

    /**
     * Verifies the display of the estimated mMoney earned on the responsive checkout page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see the same estimated mMoney amount is displayed on RC page till place order$")
    public void I_should_see_the_same_estimated_mMoney_amount_is_displayed_on_RC_page_till_place_order() throws Throwable {
        if (!Checkout.getMMoneyInformation().get("mmoneyEstimatedEarnAmount").equals(MB_MONEY_ESTIMATED_DATA.get("estimatedEarnAmount").toString())) {
            Assert.fail("Earn amount not displayed on responsive shipping page");
        }
        I_enter_shipping_address_on_guest_shipping_page();
        I_select_continue_button_on_guest_shipping_page();
        if (!Checkout.getMMoneyInformation().get("mmoneyEstimatedEarnAmount").equals(MB_MONEY_ESTIMATED_DATA.get("estimatedEarnAmount").toString())) {
            Assert.fail("Earn amount not displayed on responsive payment page");
        }
        I_fill_in_payment_information_on_guest_payment_page();
        I_select_continue_button_on_guest_payment_page();
        if (!Checkout.getMMoneyInformation().get("mmoneyEstimatedEarnAmount").equals(MB_MONEY_ESTIMATED_DATA.get("estimatedEarnAmount").toString())) {
            Assert.fail("Earn amount not displayed on responsive order review page");
        }
    }

    /**
     * Verifies the display of mMoney or bMoney on the given page
     *
     * @param condition should or should not
     * @param page      which page you're ong (shipping and payment or order review)
     * @throws Throwable if any exception occurs
     */
    @Then("^I (should|should not) see bMoney earn information is displayed on (shipping & payment|order review) page$")
    public void I_should_see_bMoney_earn_information_is_displayed_on_shipping_and_payment_page(String condition, String page) throws Throwable {
        pausePageHangWatchDog();
        CheckoutUtils.RCPage pageName = page.equals("shipping & payment") ? CheckoutUtils.RCPage.SHIPPING_AND_PAYMENT : CheckoutUtils.RCPage.REVIEW;
        if (condition.equals("should")) {
            if (!Checkout.getEstimatedBMoneyInformation(pageName).get("bmoneyEstimatedEarnAmount").equals(MB_MONEY_ESTIMATED_DATA.get("estimatedEarnAmount").toString())) {
                Assert.fail("Earn amount is not displayed on " + page + " page");
            }
        } else {
            if (!Elements.elementPresent(page + ".bmoney_earn_amount_text")) {
                Assert.fail("Earn amount is displayed on " + page + " page");
            }
        }
        resumePageHangWatchDog();
        System.out.println("Estimated earn amount is" + (condition.equals("should") ? "" : " not") + " displayed on " + page + " page");
    }

    /**
     * Selects an available store for bops item and selects pick up
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select pick up option for bops item after selecting available store$")
    public void I_select_pick_up_option_for_bops_item_after_selecting_available_store() throws Throwable {
        if (MEW()) {
            i_select_store_pickup_availability_link_on_bag_page();
            ShopAndBrowse.I_select_miles_in_bops_change_store_dialog("100 miles");
            Facets.I_search_for_zipcode_in_bops_facet(macys() ? "22102" : "10022");
            i_select_bops_store_button_in_change_pickup_store_dialog();
            I_select_pick_up_option_for_bops_item();
            System.out.println("Selected pick up option for bops item after selecting available store in mobile website");
        } else {
            i_select_store_pickup_availability_link_on_bag_page();
            ShopAndBrowse.I_select_miles_in_bops_change_store_dialog(macys() ? "100 miles" : "100 Miles");
            Facets.I_search_for_zipcode_in_bops_facet(macys() ? "22102" : "10022");
            i_select_bops_store_button_in_change_pickup_store_dialog();
            ShopAndBrowse.I_save_close_the_bops_change_store_dialog();
            I_select_pick_up_option_for_bops_item();
            System.out.println("Selected pick up option for bops item after selecting available store");
        }
    }

    /**
     * Verifies that the correct shipping option is displayed on shopping bag page
     *
     * @param shippingMethod what shipping options should be displayed
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see (bops shipping|normal shipping|bops shipping and normal shipping) in order summary on shopping bag page$")
    public void I_should_see_bops_shipping_in_order_summary_on_shopping_bag_page(String shippingMethod) throws Throwable {
        switch (shippingMethod) {
            case "bops shipping":
                Wait.untilElementPresent("shopping_bag.bops_shipping");
                if (!Elements.elementPresent("shopping_bag.bops_shipping")) {
                    Assert.fail("Bops shipping is not displaying in order summary section on shopping bag page");
                }
                break;
            case "normal shipping":
                if (!Elements.elementPresent("shopping_bag.normal_shipping")) {
                    Assert.fail("Normal shipping is not displaying in order summary section on shopping bag page");
                }
                break;
            default:
                if (!Elements.elementPresent("shopping_bag.bops_shipping") && !Elements.elementPresent("shopping_bag.normal_shipping")) {
                    Assert.fail("Bops shipping and normal shipping are not displaying in order summary section on shopping bag page");
                }
                break;
        }
        System.out.println(shippingMethod + " is displaying in order summary on shopping bag page");
    }

    /**
     * Verifies that the correct shipping option is displayed in checkout order summary
     *
     * @param shippingMethod what shipping options should be displayed
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see (bops shipping|normal shipping|bops shipping and normal shipping) in order summary section$")
    public void I_should_see_bops_shipping_in_order_summary_section(String shippingMethod) throws Throwable {
        String page;
        if (signedIn()) {
            page = onPage("responsive_checkout_signed_in") ? "responsive_checkout_signed_in" : onPage("shipping_payment_signed_in") ? "shipping_payment_signed_in" : "order_review";
        } else {
            page = onPage("responsive_checkout") ? "responsive_checkout" : onPage("shipping_guest") ? "shipping_guest" : "order_review";
        }
        switch (shippingMethod) {
            case "bops shipping":
                Wait.untilElementPresent(page + ".bops_shipping");
                if (!Elements.elementPresent(page + ".bops_shipping")) {
                    Assert.fail("Bops shipping section is not displaying on " + page + " page");
                }
                break;
            case "normal shipping":
                if (!Elements.elementPresent(page + ".normal_shipping")) {
                    Assert.fail("Normal shipping section is not displaying on " + page + " page");
                }
                break;
            default:
                if (!Elements.elementPresent(page + ".bops_shipping") && !Elements.elementPresent(page + ".normal_shipping")) {
                    Assert.fail("Bops shipping and normal shipping sections are not displaying on " + page + " page");
                }
                break;
        }
        System.out.println(shippingMethod + " is displaying in order summary section");
    }

    /**
     * Verifies that the correct shipping option is displayed on the order confirmation page
     *
     * @param shippingMethod what shipping options should be displayed
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see (bops shipping|normal shipping|bops shipping and normal shipping) section on order confirmation page$")
    public void I_should_see_bops_shipping_section_on_order_confirmation_page(String shippingMethod) throws Throwable {
        String page = onPage("responsive_order_confirmation") ? "responsive_order_confirmation" : "order_confirmation";
        switch (shippingMethod) {
            case "bops shipping":
                if (!Elements.elementPresent(page + ".bops_shipping_section")) {
                    Assert.fail("Bops shipping section is not displaying on order confirmation page");
                }
                break;
            case "normal shipping":
                if (!Elements.elementPresent(page + ".normal_shipping_section")) {
                    Assert.fail("Normal shipping section is not displaying on order confirmation page");
                }
                break;
            default:
                if (!Elements.elementPresent(page + ".bops_shipping_section") && !Elements.elementPresent(page + ".normal_shipping_section")) {
                    Assert.fail("Bops shipping and normal shipping sections are not displaying on order confirmation page");
                }
                break;
        }
        System.out.println(shippingMethod + " section is displaying on order confirmation page");
    }

    /**
     * Verifies the display of loyalty points earned on the order confirmation page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see loyalty points section on order conformation page$")
    public void I_should_see_loyalty_points_section_on_order_conformation_page() throws Throwable {
        String pageName = onPage("responsive_order_confirmation") ? "responsive_order_confirmation" : "order_confirmation";
        shouldBeOnPage(pageName);
        if (bloomingdales()) {
            if (!(Elements.elementPresent(pageName + ".loyalty_pending_points_section") && Elements.elementPresent(pageName + ".loyalty_total_points_section"))) {
                Assert.fail("Loyalty points section is not displayed on order confirmation page!!");
            }
        }
        System.out.println("Loyalty points section displayed on order confirmation page!!");
    }

    /**
     * Removes one item of the given type from the shopping bag
     *
     * @param itemType registry or normal
     * @throws Throwable if any exception occurs
     */
    @When("^I remove (registry|normal) item from shopping bag page$")
    public void iRemoveItemFromShoppingBagPage(String itemType) throws Throwable {
        shouldBeOnPage("shopping_bag");
        ShoppingBag.removeBonusItemsFromBag();
        Navigate.browserRefresh();
        List<Product> bagItems = ShoppingBag.getAllProductDetails();
        ShoppingBag.removeItem(ShoppingBag.findItemIndexByType(bagItems, itemType));
    }

    /**
     * Removes all items from the shopping bag
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I remove all items in shopping bag$")
    public void iRemoveAllItemsInShoppingBag() throws Throwable {
        ShoppingBag.emptySavedShoppingBag();
        ShoppingBag.emptyCurrentShoppingBag();
    }

    /**
     * Verifies that an item was removed from the shopping bag
     *
     * @param itemType registry or normal
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see (registry|normal) item is removed from shopping bag page$")
    public void iShouldSeeItemIsRemovedFromShoppingBagPage(String itemType) throws Throwable {
        List<Product> bagItems = ShoppingBag.getAllProductDetails();
        if (ShoppingBag.findItemIndexByType(bagItems, itemType) == -1) {
            System.out.println(itemType + " Item is removed from bag!!");
        } else {
            Assert.fail(itemType + " Item is not removed from bag!!");
        }
    }

    /**
     * Checks that shopping bag only contains given type of item
     *
     * @param itemType registry or normal
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see only (registry|normal) item is present in shopping bag page$")
    public void iShouldSeeOnlyItemIsPresentInShoppingBagPage(String itemType) throws Throwable {
        List<Product> bagItems = ShoppingBag.getAllProductDetails();
        if (ShoppingBag.findItemIndexByType(bagItems, itemType) == -1) {
            Assert.fail(itemType + " Item is not present in shopping bag!!");
        } else {
            System.out.println(itemType + " Item is present in shopping bag!!");
        }
    }

    /**
     * Follow standard checkout flow but only if batch mode is enabled on environment
     * <p>
     * If batch mode is enabled, include the environment variable "batch_mode" with value "true"
     * <p>
     * Options for Responsive pages:<br>
     * <code>shipping, payment, order review, order confirmation</code><br>
     * Options for international and legacy checkout pages:<br>
     * <code>shipping and payment, order review, order confirmation</code><br>
     * Options for user type:<br>
     * <code>guest, signed in</code><br>
     * For international or bops users include "iship" and "bops" respectively in the user type<br>
     * Any address parameters should be in the "address" field. supported parameters:<br>
     * <code>paypal_address</code>
     * </p>
     *
     * @param pageName name of the page you want to end up on
     * @param userType either signed in or guest user
     * @param country  country the shipping address should be from (default US)
     * @param address  Any other custom address requirements
     * @throws Throwable if any exception occurs
     * @throws Throwable if any exception occurs
     */
    @And("^I checkout on batch mode until I reach the (shipping|payment|shipping (?:and|&) payment|order review|order confirmation) page as an? \"([^\"]*)\" user(?: from \"([^\"]*)\")?(?: with \"([^\"]*)\")?$")
    public void I_checkout_on_batch_mode_until_I_reach_the_page_as_a_user(String pageName, String userType, String country, String address) throws Throwable {
        // run this step only on Batch Mode enabled QA environment
        if (MainRunner.batchMode) {
            I_checkout_until_I_reach_the_page_as_a_user(pageName, userType, country, address);
        } else {
            System.err.println("Environment not in batch mode, unable to checkout due to product unavailability.");
        }
    }

    /**
     * Selects the given shipping method on any checkout shipping page
     *
     * @param shippingMethod method to select (premium, express, or nohurry)
     * @throws Throwable if any exception occurs
     */
    @When("^I select (premium|express|nohurry) in shipping methods$")
    public void I_select_method_in_shipping_methods(String shippingMethod) throws Throwable {
        String page = signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout";
        String type = signedIn() ? "signin_" : "";
        Wait.forPageReady();
        if (signedIn()) {
            Wait.secondsUntilElementPresent(page + ".change_shipping_method", 5);
            Clicks.javascriptClick(page + ".change_shipping_method");
        }
        Assert.assertTrue("ERROR-DATA: Shipping method is not displayed as product is not eligible for that method",
                Wait.secondsUntilElementPresent(page + "." + type + shippingMethod + "_shipping", 5));
        Clicks.javascriptClick(page + "." + type + shippingMethod + "_shipping");
        pausePageHangWatchDog();
        shippingDetails = ((new Checkout().getShippingMethods())).get(shippingMethod);
        resumePageHangWatchDog();
        if (signedIn()) {
            Clicks.click("responsive_checkout_signed_in.save_shipping_method");
        }
    }

    /**
     * Verifies the display of the shipping details in shipping summary on checkout order review page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify the selected shipping method details in shipping summary section$")
    public void iVerifyTheSelectedShippingMethodDetailsInShippingSummarySection() throws Throwable {
        String page = signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout";
        String shippingElement = signedIn() ? "shipping_methods_set" : "shipping_method_summary";
        Assert.assertTrue((Elements.getText(page + "." + shippingElement)).contains(((ArrayList) shippingDetails.get("shippingMethodDetails")).get(0).toString()));
        for (int i = 0; i < ((ArrayList) shippingDetails.get("shippingMethodDetails")).size(); i++) {
            String shippingText = ((ArrayList) shippingDetails.get("shippingMethodDetails")).get(i).toString();
            if (shippingText.contains("Transit time")) {
                Assert.assertTrue((Elements.getText(page + "." + shippingElement)).contains(shippingText.replace("Transit time: ", "")));
            }
        }
    }

    /**
     * Verifies the shipping fee displays correctly in shipping summary on checkout order review page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I verify shipping fee in order summary section$")
    public void I_Verify_Shipping_Fee_In_Order_Summary_Section() throws Throwable {
        String page = signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout";
        String order_summary_shipping_fee = (Elements.getText(element(page + "." + "shipping_fee")));
        Assert.assertTrue(shippingDetails.get("shippingMethodDetails").toString().contains(order_summary_shipping_fee));
    }

    /**
     * Adds a gift card of the given type to payment
     *
     * @param cardType type of card to use (EGC, VGC, or VRC)
     * @throws Throwable if any exception occurs
     */
    @And("^I add valid (EGC|VGC|VRC) gift card as payment option on payment page$")
    public void iAddValidGiftCardAsPaymentOptionOnPaymentPage(String cardType) throws Throwable {
        GiftCard giftCard = GiftCardService.getValidGiftCardDetails(GiftCard.CardType.fromString(cardType));
        if (giftCard == null) {
            Assert.fail("Unable to get valid gift card to add as payment");
        }
        CheckoutUtils.RCPage page = CheckoutUtils.RCPage.PAYMENT;
        Wait.untilElementPresent(page + ".add_gift_card_button");
        Clicks.click(page + ".add_gift_card_button");
        Wait.untilElementPresent(page + ".gift_card_number");
        TextBoxes.typeTextbox(page + ".gift_card_number", giftCard.getCardNumber());
        TextBoxes.typeTextbox(page + ".gift_card_cid_number", giftCard.getEcid());
        // Need to enter capcha manually
        //        TextBoxes.typeTextbox(page + ".gift_card_capcha", giftCard.getEcid());
        Clicks.click(page + ".gift_card_apply_button");
        Wait.untilElementNotPresent(page + ".gift_card_apply_button");
    }

    /**
     * Verifies that the order has been placed
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^Order should be placed successfully$")
    public void I_should_be_on_order_confirmation_page() throws Throwable {
        pausePageHangWatchDog();
        String page = onPage("responsive_order_confirmation") ? "responsive_order_confirmation" : "order_confirmation";
        if (safari() || ie()) {
            Wait.secondsUntilElementPresent(page + ".order_confirmation_message", 20);
        }
        if (!Elements.elementPresent(page + ".order_confirmation_message")) {
            Assert.fail("Order not placed successfully");
        }
        resumePageHangWatchDog();
    }
}