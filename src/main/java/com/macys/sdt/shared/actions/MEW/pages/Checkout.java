package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.shared.utils.CheckoutUtils;
import com.macys.sdt.shared.utils.CheckoutUtils.RCPage;
import com.macys.sdt.shared.utils.CommonUtils;
import org.junit.Assert;

import java.util.HashMap;

public class Checkout extends StepUtils {
    private CheckoutUtils m_checkout = new CheckoutUtils();

    /**
     * Fill shipping details during checkout
     *
     * @param responsive true if responsive checkout flow, else false
     * @param bops true if bops order, else false
     * @param opts any other custom shipping address requirements
     */
    public void fillShippingData(boolean responsive, boolean bops, HashMap<String, String> opts) {
        boolean iship = opts != null && opts.get("country") != null && !opts.get("country").equalsIgnoreCase("United States");
        String page = responsive ? "responsive_checkout" : (iship ? "iship_checkout" : "shipping_payment_signed_in");
        String type = bops ? ".pickup" : ".shipping";

        ProfileAddress address = new ProfileAddress();
        TestUsers.getRandomValidAddress(opts, address);
        String phoneNum = TestUsers.generateRandomPhoneNumber();

        if (safari())
            Wait.untilElementPresentWithRefresh(Elements.element(page + type + "_first_name"));
        if (MEW())
            Wait.secondsUntilElementPresent(page + type + "_first_name", 5);
        TextBoxes.typeTextbox(page + type + "_first_name", TestUsers.generateRandomFirstName());
        TextBoxes.typeTextbox(page + type + "_last_name", TestUsers.generateRandomLastName());
        if (bops) {
            TextBoxes.typeTextbox(page + ".pickup_email_address", TestUsers.generateRandomEmail(5));
        } else {
            try {
                TextBoxes.typeTextbox(page + ".shipping_address_line_1", address.getAddressLine1());
                if (address.getAddressLine2() != null)
                    TextBoxes.typeTextbox(page + ".shipping_address_line_2", address.getAddressLine2());
                TextBoxes.typeTextbox(page + ".shipping_address_city", address.getCity());
                if (iship) {
                    if (address.getProvince() != null) {
                        DropDowns.selectByText(page + ".shipping_address_province", address.getProvince());
                    }
                    TextBoxes.typeTextbox(page + ".shipping_email_address", TestUsers.generateRandomEmail(5));
                } else {
                    String state = responsive ? address.getState() :
                            AbbreviationHelper.translateStateAbbreviation(address.getState());
                    DropDowns.selectByText(page + ".shipping_address_state", state);
                }
                TextBoxes.typeTextbox(page + ".shipping_address_" + (iship ? "postal_code" : "zip_code"), address.getZipCode().toString());
            } catch (Exception e) {
                Assert.fail("Failed to get new address: " + e);
            }
        }
        if (responsive || iship) {
            TextBoxes.typeTextbox(page + type + "_phone_number", phoneNum);
        } else {
            TextBoxes.typeTextbox(page + type + "_phone_area_code", phoneNum.substring(0, 3));
            TextBoxes.typeTextbox(page + type + "_phone_exchange", phoneNum.substring(3, 6));
            TextBoxes.typeTextbox(page + type + "_phone_subscriber", phoneNum.substring(6));
        }
    }

    /**
     * Method for going through the checkout flow in iship mode
     * <p>
     * Options for page:<br>
     * <code>shipping and payment, order review, order confirmation</code><br>
     * </p>
     *
     * @param pageName name of the page you want to end up on
     * @param opts any other custom address requirements
     * @throws Exception if any exception occurs
     */
    public void ishipCheckout(String pageName, HashMap<String, String> opts) throws Exception {
        String page = "iship_checkout";
        switch (pageName.toLowerCase()) {
            case "shipping & payment":
                if (!onPage(page))
                    m_checkout.navigateToCheckout(false, true);
                break;
            case "order review":
                if (!Elements.elementPresent(page + ".shipping_first_name")) {
                    ishipCheckout("shipping & payment", opts);
                }
                switchToFrame(page + ".shipping_iFrame");
                if (Elements.getElementAttribute(page + ".shipping_first_name", "value").equals("")) {
                    fillShippingData(false, false, opts);
                }
                Clicks.click(page + ".card_payment_method");
                switchToFrame(page + ".payment_iFrame");
                if (Elements.getElementAttribute(page + ".card_number", "value").equals("")) {
                    fillPaymentData(false, true, opts);
                }
                break;
            case "order confirmation":
                if (!Elements.elementPresent(page + ".submit_order_button")) {
                    ishipCheckout("order review", opts);
                }
                switchToFrame("default");
                switchToFrame(page + ".shipping_iFrame");
                if (prodEnv()) {
                    System.out.println("Cannot place orders on prod!!!");
                } else {
                    Clicks.click(page + ".submit_order_button");
                }
                break;
        }
        switchToFrame("default");
    }

    /**
     * Fill payment details during checkout
     *
     * @param responsive true if responsive checkout flow, else false
     * @param iship true if bops order, else false
     * @param opts any other custom billing address requirements
     */
    public void fillPaymentData(boolean responsive, boolean iship, HashMap<String, String> opts) {
        String page = responsive ? "responsive_checkout" : (iship ? "iship_checkout" : "shipping_payment_signed_in");
        if (responsive || iship) {
            Wait.untilElementPresent(page + ".card_number");
        }

        ProfileAddress address =  new ProfileAddress();
        TestUsers.getRandomValidAddress(opts, address);
        CreditCard visaCard = TestUsers.getValidVisaCreditCard();

        TextBoxes.typeTextbox(page + ".card_number", visaCard.getCardNumber());
        DropDowns.selectByText(page + ".expiry_month", (bloomingdales() && iship) ? visaCard.getExpiryMonthIndex().replaceFirst("^0","") :
                (visaCard.getExpiryMonthIndex() + (responsive || iship ? "" : " - " + visaCard.getExpiryMonth())));

        DropDowns.selectByText(page + ".expiry_year", visaCard.getExpiryYear());

        if (!iship) {
            DropDowns.selectByText(page + ".card_type", "Visa");
            if (!Elements.elementPresent(page + ".first_name"))
                Clicks.click(page + ".use_shipping_address");
            TextBoxes.typeTextbox(page + ".first_name", TestUsers.generateRandomFirstName());
            TextBoxes.typeTextbox(page + ".last_name", TestUsers.generateRandomLastName());
            TextBoxes.typeTextbox(page + ".address_line_1", address.getAddressLine1());
            // address line 2
            String addr2 = address.getAddressLine2();
            if (addr2 != null && !addr2.equals(""))
                TextBoxes.typeTextbox(page + ".address_line_2", address.getAddressLine2());
            TextBoxes.typeTextbox(page + ".address_city", address.getCity());
            String state = address.getState();
            if (responsive) {
                TextBoxes.typeTextbox(page + ".phone_number", address.getBestPhone());
            } else {
                state = AbbreviationHelper.translateStateAbbreviation(state);
                TextBoxes.typeTextbox(page + ".card_phone_area_code", address.getPhoneAreaCode());
                TextBoxes.typeTextbox(page + ".card_phone_exchange", address.getPhoneExchange());
                TextBoxes.typeTextbox(page + ".card_phone_subscriber", address.getPhoneSubscriber());
            }
            DropDowns.selectByText(page + ".address_state", state);
            TextBoxes.typeTextbox(page + ".address_zip_code", address.getZipCode().toString());
            TextBoxes.typeTextbox(page + ".payment_email", address.getEmail());
        }

        TextBoxes.typeTextbox(page + ".security_code", visaCard.getSecurityCode());
    }

    /**
     * Method for going through the rc checkout flow as a signed in user
     * <p>
     * Options for page:<br>
     * <code>shipping and payment, order review, order confirmation</code><br>
     * </p>
     *
     * @param pageName name of the page you want to end up on
     * @param opts unused
     * @param bops true if bops order, else false
     * @throws Exception if any exception occurs
     */
    public void rcSignedIn(String pageName, HashMap<String, String> opts, boolean bops) throws Exception {
        String page = "responsive_checkout_signed_in";
        switch (pageName.toLowerCase()) {
            case "shipping & payment":
                // force signed in rc - might not have gotten rc experimentation cookie
                Cookies.forceRc();
                if (!onPage(page))
                    m_checkout.navigateToCheckout(true, false);
                break;
            case "order review":
                if (!Elements.elementPresent(page + ".place_order"))
                    rcSignedIn("shipping & payment", opts, false);

                if (!Elements.elementPresent(page + ".change_shipping_address") && Elements.elementPresent(page + ".add_shipping_address_button"))  {
                    Clicks.click(page + ".add_shipping_address_button");
                    CreateProfileMEW.addNewAddress();
                    if (onPage("my_address_book")) {
                        m_checkout.navigateToCheckout(true, false);
                    }
                    Wait.untilElementPresent(page + ".change_shipping_address");
                }

                if (bops) {
                    Clicks.clickIfPresent(page + ".pick_up_save");
                }

                // As of 2/3/16, adding addresses and credit cards redirects to my account.
                // For now, assume we have an address & card since we're signed in.
                TextBoxes.typeTextbox(page + ".security_code", "204");

                break;
            case "order confirmation":
                if (!Elements.elementPresent(page + ".place_order")
                        || Elements.getElementAttribute(page + ".place_order", "aria-disabled").equals("true")) {
                    rcSignedIn("order review", opts, false);
                }
                if (!prodEnv()) {
                    Clicks.click(page + ".place_order");
                } else {
                    System.out.println("Cannot place orders on prod!!!");
                }
                break;
        }
    }

    /**
     * Method for going through the checkout flow as a signed in user
     * <p>
     * Options for page:<br>
     * <code>shipping and payment, order review, order confirmation</code><br>
     * </p>
     *
     * @param pageName name of the page you want to end up on
     * @param opts unused
     * @param bops true if bops order, else false
     * @throws Exception if any exception occurs
     */
    public void signInCheckout(String pageName, HashMap<String, String> opts, boolean bops) throws Exception {
        // check if we ended up in rc
        if (onPage("responsive_checkout_signed_in")) {
            rcSignedIn(pageName, opts, bops);
            return;
        }

        switch (pageName.toLowerCase()) {
            case "shipping & payment":
                if (!onPage("shipping_payment_signed_in"))
                    m_checkout.navigateToCheckout(true, false);
                break;
            case "order review":
                if (!onPage("shipping_payment_signed_in"))
                    signInCheckout("shipping & payment", opts, true);

                if (!Elements.elementPresent("shipping_payment_signed_in.shipping_address_radio_button")) {
                    Clicks.click("shipping_payment_signed_in.add_shipping_address_button");
                    CreateProfileMEW.addNewAddress();
                }

                if (!Elements.elementPresent("shipping_payment_signed_in.credit_card_radio_button")) {
                    Clicks.click("shipping_payment_signed_in.add_credit_card_button");
                    CommonUtils.addCreditCardFromBWallet(null, null);
                }

                if (Elements.elementPresent("shipping_payment_signed_in.usl_pin")) {
                    TextBoxes.typeTextbox("shipping_payment_signed_in.usl_pin", "1234");
                    Clicks.click("shipping_payment_signed_in.usl_apply");
                    Wait.untilElementPresent("shipping_payment_signed_in.use_my_billing_address");
                    Clicks.selectCheckbox("shipping_payment_signed_in.use_my_billing_address");
                    String phone_no = TestUsers.generateRandomPhoneNumber();
                    TextBoxes.typeTextbox("shipping_payment_signed_in.bill_phone_area_code", phone_no.substring(0, 3));
                    TextBoxes.typeTextbox("shipping_payment_signed_in.bill_phone_exchange", phone_no.substring(3, 6));
                    TextBoxes.typeTextbox("shipping_payment_signed_in.bill_phone_subscriber", phone_no.substring(6));
                } else {
                    TextBoxes.typeTextbox("shipping_payment_signed_in.security_code", "234");
                }
                Clicks.click("shipping_payment_signed_in.continue_checkout");
                Wait.untilElementPresent("order_review_signed_in.place_order");
                break;
            case "order confirmation":
                if (!onPage("order_review_signed_in"))
                    signInCheckout("order review", opts, true);
                Assert.assertTrue("ERROR-ENV: Not able to navigate to the order review page", onPage("order_review_signed_in"));
                if (prodEnv())
                    System.out.println("Cannot place orders on prod!");
                else
                    Clicks.click("order_review_signed_in.place_order");
        }
    }

    /**
     * Method for going through the rc checkout flow as a guest user
     * <p>
     * Options for page:<br>
     * <code>RCPage.SHIPPING, RCPage.PAYMENT, RCPage.REVIEW, RCPage.CONFIRMATION</code><br>
     * </p>
     *
     * @param page RCPage you want to end up on
     * @param opts any other custom address requirements
     * @param bops true if bops order, else false
     * @throws Exception if any exception occurs
     */
    public void rcGuest(RCPage page, HashMap<String, String> opts, boolean bops) throws Exception {
        switch (page) {
            case SHIPPING:
                if (!onPage(page.toString()))
                    m_checkout.navigateToCheckout(false, false);
                break;
            case PAYMENT:
                if (Elements.elementPresent(page + ".card_number")) {
                    break;
                }
                page = RCPage.SHIPPING;
                if (!Elements.elementPresent(page + (bops ? ".pickup_first_name" : ".shipping_first_name"))) {
                    rcGuest(RCPage.SHIPPING, opts, bops);
                }

                if (Elements.getElementAttribute(page + (bops ? ".pickup" : ".shipping") + "_first_name", "value").equals("")) {
                    fillShippingData(true, bops, opts);
                }
                Clicks.click(page + ".continue_shipping_checkout_button");
                // some emulator devices fail here, no idea why
                if (MEW() && !Wait.untilElementPresent(page + ".card_number")) {
                    Clicks.click(page + ".continue_shipping_checkout_button");
                }
                Wait.secondsUntilElementPresent(page + ".card_number", 15);
                break;
            case REVIEW:
                //
                page = RCPage.PAYMENT;
                if (!Wait.untilElementPresent(page + ".card_number")) {
                    rcGuest(RCPage.PAYMENT, opts, bops);
                }

                if (Elements.elementPresent(page + ".card_number")
                        && Elements.getElementAttribute(page + ".card_number", "value").equals("")) {
                    new Checkout().fillPaymentData(true, false, opts);
                }
                Clicks.click(page + ".continue_payment_checkout_button");
                // that click brings us to the review page
                page = RCPage.REVIEW;
                if (!Wait.secondsUntilElementPresent(page + ".edit_payment_info", 5))
                    Assert.fail("Didn't make it to order review");
                break;
            case CONFIRMATION:
                if (!Elements.elementPresent(page + ".edit_shipping_info") && !Elements.elementPresent(page + ".edit_payment_info")) {
                    rcGuest(RCPage.REVIEW, opts, bops);
                }
                if (!prodEnv()) {
                    Clicks.click(page + ".place_order");
                } else {
                    System.out.println("Cannot place orders on prod!!!");
                }
                break;
        }
    }
}
