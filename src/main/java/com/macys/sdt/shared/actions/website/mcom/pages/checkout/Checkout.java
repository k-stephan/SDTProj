package com.macys.sdt.shared.actions.website.mcom.pages.checkout;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.website.bcom.pages.CheckoutPageBcom;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.MyWallet;
import com.macys.sdt.shared.actions.website.mcom.panels.checkout.UslPayment;
import com.macys.sdt.shared.utils.CheckoutUtils;
import com.macys.sdt.shared.utils.CheckoutUtils.RCPage;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;
import java.util.ArrayList;

import java.util.HashMap;

public class Checkout extends StepUtils {

    CreditCard creditCard;
    private CheckoutUtils checkoutUtils = new CheckoutUtils();

    //    public void addCreditCardOnRCSignedIn

    public static void addUslAsPayment() {
        new UslPayment().addUslAsPayment();
    }

    /**
     * Method to get mMoney earn information
     *
     * @return mMoney earn information
     */
    public static HashMap<String, String> getMMoneyInformation() {
        HashMap<String, String> mMoneyEstimatedEarnInfo = new HashMap<>();

        mMoneyEstimatedEarnInfo.put("mmoneyEstimatedEarnText", Elements.getText("responsive_checkout.mmoney_estimated_earn_text"));
        mMoneyEstimatedEarnInfo.put("mmoneyEstimatedEarnAmount", Elements.getText("responsive_checkout.mmoney_estimated_earn_amount").replaceAll("[^0-9]", ""));

        return mMoneyEstimatedEarnInfo;
    }

    /**
     * Method to get bMoney earn information
     *
     * @param pageName current page
     * @return bMoney earn information
     */
    public static HashMap<String, String> getEstimatedBMoneyInformation(RCPage pageName) {
        HashMap<String, String> bMoneyEstimatedEarnInfo = new HashMap<>();

        bMoneyEstimatedEarnInfo.put("bmoneyEstimatedEarnDesc", Elements.getText(pageName + ".bmoney_earn_desc"));
        bMoneyEstimatedEarnInfo.put("bmoneyEstimatedEarnAmount", Elements.getText(pageName + ".bmoney_earn_amount").split("\\.")[0].replaceAll("[^0-9]", ""));

        return bMoneyEstimatedEarnInfo;
    }

    public void fillShippingData(boolean responsive, boolean bops, HashMap<String, String> opts) {
        boolean iship = opts != null && opts.get("country") != null && !opts.get("country").equalsIgnoreCase("United States");
        String page = (signedIn() ? (onPage("shipping_payment_signed_in") ? "shipping_payment_signed_in" : "responsive_checkout_signed_in") : (iship ? "iship_checkout" : "responsive_checkout"));
        String type = bops ? ".pickup" : ".shipping";

        ProfileAddress address = TestUsers.getRandomValidAddress(opts);
        String phoneNum = TestUsers.generateRandomPhoneNumber();

        Wait.forPageReady();
        if (macys() || bops) {
            if (safari()) {
                Wait.untilElementPresentWithRefresh(Elements.element(page + type + "_first_name"));
            }
            if (signedIn() && !bops) {
                fillResponsiveBCAddressInfo(opts, "shipping", RCPage.fromString(page), responsive);
            } else {
                TextBoxes.typeTextbox(page + type + "_first_name", TestUsers.generateRandomFirstName());
                TextBoxes.typeTextbox(page + type + "_last_name", TestUsers.generateRandomLastName());
                if (bops) {
                    TextBoxes.typeTextbox(page + ".pickup_email_address", TestUsers.generateRandomEmail(5));
                } else {
                    try {
                        TextBoxes.typeTextbox(page + ".shipping_address_line_1", address.getAddressLine1());
                        if (address.getAddressLine2() != null && !address.getAddressLine2().isEmpty()) {
                            TextBoxes.typeTextbox(page + ".shipping_address_line_2", address.getAddressLine2());
                        }
                        TextBoxes.typeTextbox(page + ".shipping_address_city", address.getCity());
                        if (iship) {
                            if (Elements.elementPresent(page + ".shipping_address_province") && address.getProvince() != null) {
                                DropDowns.selectByText(page + ".shipping_address_province", address.getProvince());
                            }
                            TextBoxes.typeTextbox(page + ".shipping_email_address", TestUsers.generateRandomEmail(5));
                        } else {
                            String state = responsive ? address.getState() : StatesUtils.translateAbbreviation(address.getState());
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
        } else {
            if (iship) {
                switchToFrame(page + ".shipping_iFrame");
                TextBoxes.typeTextbox(page + type + "_first_name", TestUsers.generateRandomFirstName());
                TextBoxes.typeTextbox(page + type + "_last_name", TestUsers.generateRandomLastName());
                if (Elements.elementPresent(page + ".shipping_address_province") && address.getProvince() != null) {
                    DropDowns.selectByText(page + ".shipping_address_province", address.getProvince());
                }
                TextBoxes.typeTextbox(page + ".shipping_email_address", TestUsers.generateRandomEmail(5));
                TextBoxes.typeTextbox(page + ".shipping_phone_number", TestUsers.generateRandomPhoneNumber());
                TextBoxes.typeTextbox(page + ".shipping_address_postal_code", address.getZipCode().toString());

                TextBoxes.typeTextbox(page + ".shipping_address_line_1", address.getAddressLine1());
                TextBoxes.typeTextbox(page + ".shipping_address_city", address.getCity());

            } else {
                try {
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_first_name", TestUsers.generateRandomFirstName());
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_last_name", TestUsers.generateRandomLastName());
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_address_line_1", address.getAddressLine1());
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_address_city", address.getCity());
                    String state = address.getState();
                    DropDowns.selectByText("shipping_payment_signed_in.shipping_address_state", state);
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_address_zip_code", address.getZipCode().toString());
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_phone_area_code", address.getPhoneAreaCode());
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_phone_exchange", TestUsers.generateRandomPhoneAreaCodeExchange());
                    TextBoxes.typeTextbox("shipping_payment_signed_in.shipping_phone_subscriber", TestUsers.generateRandomPhoneSubscriber());
                    Clicks.selectCheckbox("shipping_payment_signed_in.set_as_a_default_address");
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail("Failed to get new address: " + e);
                }
            }
        }
    }

    public void fillCreditCardData(boolean responsive, boolean iship) {
        String page = responsive ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout") : (iship ? "iship_checkout" : "shipping_payment_signed_in");
        if(creditCard == null)
            creditCard = TestUsers.getValidVisaCreditCard();
        if(page.equals("shipping_payment_signed_in") || page.equals("responsive_checkout_signed_in"))
            Clicks.clickIfPresent(page + ".add_credit_card_button");
        if(Wait.untilElementPresent(page + ".card_number"))
            TextBoxes.typeTextbox(page + ".card_number", creditCard.getCardNumber());
        if (iship) {
            DropDowns.selectByText(page + ".expiry_month", String.valueOf(Integer.parseInt(creditCard.getExpiryMonthIndex())));
            DropDowns.selectByText(page + ".expiry_year", creditCard.getExpiryYear());
        } else {
            if (!responsive || macys()) {
                DropDowns.selectByText(page + ".expiry_month", (responsive && signedIn()) ? creditCard.getExpiryMonth() : (creditCard.getExpiryMonthIndex() + (responsive ? "" : " - " + creditCard.getExpiryMonth())));
                DropDowns.selectByText(page + ".card_type", creditCard.getCardType().name);
                DropDowns.selectByText(page + ".expiry_year", creditCard.getExpiryYear());
            } else {
                if(signedIn()){
                    MyWallet.addCard(creditCard);
                    Navigate.visit(page);
                    Wait.forPageReady();
                } else {
                    DropDowns.selectCustomText(page + ".expiry_month_list", page + ".expiry_month_options",
                            signedIn() ? creditCard.getExpiryMonth() : creditCard.getExpiryMonthIndex());
                    DropDowns.selectCustomText(page + ".card_type_list", page + ".card_type_options", creditCard.getCardType().name);
                    DropDowns.selectCustomText(page + ".expiry_year_list", page + ".expiry_year_options", creditCard.getExpiryYear());
                }
            }
        }
        if (!signedIn()) {
            TextBoxes.typeTextbox(page + ".security_code", creditCard.getSecurityCode());
        }
    }

    public void fillContactDetails(boolean responsive, String page, ProfileAddress address) {
        if (address == null) {
            address = TestUsers.getRandomValidAddress(null);
        }
        if (responsive) {
            Wait.untilElementPresent(page + ".phone_number");
            TextBoxes.typeTextbox(page + ".phone_number", address.getBestPhone());
            if (!(signedIn() && Elements.elementPresent(page + ".update_paypal_profile_email"))) {
                TextBoxes.typeTextbox(page + ".payment_email", address.getEmail());
            }
        } else {
            TextBoxes.typeTextbox(page + ".area_code", address.getPhoneAreaCode());
            TextBoxes.typeTextbox(page + ".exchange", address.getPhoneExchange());
            TextBoxes.typeTextbox(page + ".subscriber", TestUsers.generateRandomPhoneSubscriber());
            TextBoxes.typeTextbox(page + ".email", TestUsers.generateRandomEmail(5));
        }
    }

    public void fillPaymentData(boolean responsive, boolean iship, HashMap<String, String> opts) {
        String page = responsive ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout") : (iship ? "iship_checkout" : "shipping_payment_signed_in");
        if (responsive || iship) {
            Wait.untilElementPresent(page + ".card_number");
        }

        ProfileAddress address = TestUsers.getRandomValidAddress(opts);

        fillCreditCardData(responsive, iship);

        if (!iship && !(bloomingdales() && signedIn()))
            fillResponsiveBCAddressInfo(opts, "payment", RCPage.fromString(page), responsive);

        if (!responsive && !iship) {
            String number = TestUsers.generateRandomPhoneNumber();
            TextBoxes.typeTextbox(page + ".card_phone_area_code", number.substring(0, 3));
            TextBoxes.typeTextbox(page + ".card_phone_exchange", number.substring(3, 6));
            TextBoxes.typeTextbox(page + ".card_phone_subscriber", number.substring(6));
            TextBoxes.typeTextbox(page + ".payment_email", TestUsers.generateRandomEmail(5));
            Clicks.click(page + ".save_card");
            Wait.secondsUntilElementNotPresent(page + ".card_phone_area_code", 5);
            fillContactDetails(false, page.toString(), address);
        }
        if (!signedIn() && !iship) {
            TextBoxes.typeTextbox(page + ".security_code", creditCard.getSecurityCode());
            TextBoxes.typeTextbox(page + ".payment_email", address.getEmail());
        }
        Clicks.clickIfPresent(page + ".save_credit_card_button");
    }

    public void ishipCheckout(String pageName, HashMap<String, String> opts) throws Exception {
        String page = "iship_checkout";
        switch (pageName.toLowerCase()) {
            case "shipping & payment":
                if (!onPage(page)) {
                    checkoutUtils.navigateToCheckout(false, true);
                }
                break;
            case "order review":
                if (!Elements.elementPresent(page + ".shipping_first_name")) {
                    ishipCheckout("shipping & payment", opts);
                }
                switchToFrame(page + ".shipping_iFrame");
                if (Elements.getElementAttribute(page + ".shipping_first_name", "value").equals("")) {
                    fillShippingData(false, false, opts);
                }
                Wait.secondsUntilElementPresent(page + ".card_payment_method", 10);
                Clicks.click(page + ".card_payment_method");
                switchToFrame(page + ".payment_iFrame");
                if (safari()) {
                    int cnt = 0;
                    while (!Elements.elementPresent(page + ".card_number") && cnt++ < 3) {
                        Wait.secondsUntilElementPresent(page + ".card_number", 1);
                        if (Elements.elementPresent(page + ".card_number")) {
                            break;
                        }
                        try {
                            Navigate.browserRefresh();
                            Wait.forPageReady();
                            switchToFrame(page + ".shipping_iFrame");
                            Wait.untilElementPresent(page + ".shipping_first_name");
                            if (Elements.getElementAttribute(page + ".shipping_first_name", "value").equals("")) {
                                fillShippingData(false, false, opts);
                            }
                            Clicks.click(page + ".card_payment_method");
                            switchToFrame(page + ".payment_iFrame");
                        } catch (Exception e) {
                            System.err.println("Error while adding iship address: " + e);
                        }
                    }
                }
                if (Elements.getElementAttribute(page + ".card_number", "value").equals("")) {
                    fillPaymentData(false, true, opts);
                }
                break;
            case "order confirmation":
                if (!Elements.elementPresent(page + ".submit_order_button")) {
                    ishipCheckout("order review", opts);
                }
                switchToFrame(page + ".shipping_iFrame");
                if (prodEnv()) {
                    System.err.println("Cannot place orders on prod!!!");
                } else {
                    Clicks.click(page + ".submit_order_button");
                    Wait.untilElementNotPresent(page + ".submit_order_button");
                    switchToFrame(page + ".shipping_iFrame");
                }
                break;
        }
        switchToFrame("default");
    }

    public void rcSignedIn(RCPage page, HashMap<String, String> opts, boolean bops) throws Exception {
        pausePageHangWatchDog();
        switch (page) {
            case SHIPPING_AND_PAYMENT:
                Clicks.clickIfPresent(page + ".add_new_address");
                boolean registry_address = Elements.elementPresent(By.className("rc-registry-address-note"));
                boolean isAddressAdded = Elements.elementPresent(page + (bops ? ".pickup_first_name" : ".shipping_first_name"));
                if (!registry_address && isAddressAdded) {
                    Wait.secondsUntilElementPresent(page + (bops ? ".pickup" : ".shipping") + "_first_name", 10);
                    if (Elements.getElementAttribute(page + (bops ? ".pickup" : ".shipping") + "_first_name", "value").equals("")) {
                        if (macys()) {
                            fillShippingData(true, bops, opts);
                        } else {
                            new CheckoutPageBcom().fillGuestShippingData(opts, bops);
                        }
                        Clicks.clickIfPresent(page + ".save_new_address");

                        // enter shipping details when bag has mixed items (Bops + shipped)
                        if (Elements.elementPresent(page + ".shipping_first_name") && Elements.getElementAttribute(page + ".shipping_first_name", "value").isEmpty()) {
                            if (macys()) {
                                fillShippingData(true, false, opts);
                            } else {
                                new CheckoutPageBcom().fillGuestShippingData(opts, false);
                            }
                        }
                        Clicks.clickIfPresent(page + ".pick_up_save");
                    }
                }
                Wait.forPageReady();
                break;
            case REVIEW:
                if (!Elements.elementPresent(RCPage.REVIEW + ".place_order") ||
                        Elements.getElementAttribute(RCPage.REVIEW + ".place_order", "disabled").matches("disabled|true")) {
                    rcSignedIn(RCPage.SHIPPING_AND_PAYMENT, opts, bops);
                }
                Wait.forPageReady();
                Wait.secondsUntilElementPresent(RCPage.SHIPPING_AND_PAYMENT + ".add_credit_card_button", 5);
                if (Elements.elementPresent(RCPage.SHIPPING_AND_PAYMENT + ".add_credit_card_button")) {
                    fillPaymentData(true, false, opts);
                }
                if (Elements.elementPresent(RCPage.SHIPPING_AND_PAYMENT + ".security_code")) {
                    TextBoxes.typeTextbox(RCPage.SHIPPING_AND_PAYMENT + ".security_code", "204");
                }
                if (Wait.untilElementPresent(RCPage.SHIPPING_AND_PAYMENT + ".phone_number")) {
                    fillContactDetails(true, RCPage.SHIPPING_AND_PAYMENT.toString(), TestUsers.getRandomValidAddress(opts));
                    Clicks.click(RCPage.SHIPPING_AND_PAYMENT + ".save_contact_button");
                    Wait.untilElementNotPresent(RCPage.SHIPPING_AND_PAYMENT + ".save_contact_button");
                }
                if (Elements.elementPresent(RCPage.SHIPPING_AND_PAYMENT + ".security_code")) {
                    TextBoxes.typeTextbox(RCPage.SHIPPING_AND_PAYMENT + ".security_code", "204");
                }
                break;
            case CONFIRMATION:
                Wait.untilElementPresent(RCPage.REVIEW + ".place_order");
                if (!Elements.elementPresent(RCPage.REVIEW + ".place_order") ||
                        Elements.getElementAttribute(RCPage.REVIEW + ".place_order", "disabled").matches("disabled|true")) {
                    rcSignedIn(RCPage.REVIEW, opts, bops);
                }
                if (!prodEnv()) {
                    Clicks.click(RCPage.REVIEW + ".place_order");
                } else {
                    System.err.println("Cannot place orders on prod!!!");
                }
                break;
        }
        resumePageHangWatchDog();
    }

    public void rcGuest(RCPage page, HashMap<String, String> opts, boolean bops) throws Throwable {
        if (page == null) {
            Assert.fail("page cannot be null");
        }
        Wait.forPageReady();
        switch (page) {
            case SHIPPING:
                if (!onPage(page.page)) {
                    checkoutUtils.navigateToCheckout(false, false);
                }
                break;
            case PAYMENT:
                if (Wait.untilElementPresent(RCPage.SHIPPING + ".card_number")) {
                    break;
                }
                boolean registry_address = Elements.elementPresent(By.className("rc-registry-address-note"));
                boolean isAddressAdded = Elements.elementPresent(RCPage.SHIPPING + (bops ? ".pickup_first_name" : ".shipping_first_name"));
                if (!isAddressAdded && !registry_address) {
                    rcGuest(RCPage.SHIPPING, opts, bops);
                }

                RCPage shipping = RCPage.SHIPPING;
                if (macys() && !registry_address) {
                    Wait.secondsUntilElementPresent(shipping + (bops ? ".pickup" : ".shipping") + "_first_name", 10);
                    if (Elements.getElementAttribute(shipping + (bops ? ".pickup" : ".shipping") + "_first_name", "value").equals("")) {
                        fillShippingData(true, bops, opts);

                        // enter shipping details when bag has mixed items (Bops + shipped)
                        if (Elements.elementPresent(shipping + ".shipping_first_name")
                                && Elements.getElementAttribute(shipping + ".shipping_first_name", "value").isEmpty()) {
                            fillShippingData(true, false, opts);
                        }
                    }
                }
                if (bloomingdales() && !registry_address) {
                    Wait.untilElementPresent(shipping + (bops ? ".pickup" : ".shipping") + "_first_name");
                    if (Elements.getElementAttribute(shipping + (bops ? ".pickup" : ".shipping") + "_first_name", "value").equals("")) {
                        new CheckoutPageBcom().fillGuestShippingData(opts, bops);
                    }
                    // enter shipping details when bag has mixed items (Bops + shipped)
                    if (Elements.elementPresent(shipping + ".shipping_first_name") && Elements.getElementAttribute(shipping + ".shipping_first_name", "value").equals("")) {
                        new CheckoutPageBcom().fillGuestShippingData(opts, false);
                    }
                }
                Wait.forPageReady();
                Clicks.click(shipping + ".continue_shipping_checkout_button");
                Wait.secondsUntilElementPresent(RCPage.SHIPPING + ".card_number", 15);
                break;
            case REVIEW:
                RCPage payment = RCPage.PAYMENT;
                if (!Elements.elementPresent(payment + ".card_number")) {
                    rcGuest(payment, opts, bops);
                }

                Wait.untilElementPresent(payment + ".card_number");
                if (Elements.getElementAttribute(payment + ".card_number", "value").equals("")) {
                    new Checkout().fillPaymentData(true, false, opts);
                }
                Clicks.click(payment + ".continue_payment_checkout_button");
                //if (safari())
                //    try {
                //        untilElementPresent(page + ".edit_payment_info");
                //    } catch (Error | Exception e) {
                //    }
                //else
                Wait.untilElementPresent(payment + ".edit_payment_info");
                break;
            case CONFIRMATION:
                if (!Elements.elementPresent(RCPage.REVIEW + ".edit_shipping_info")
                        && !Elements.elementPresent(RCPage.REVIEW + ".edit_payment_info")) {
                    rcGuest(RCPage.REVIEW, opts, bops);
                }
                if (!prodEnv()) {
                    Clicks.click(RCPage.REVIEW + ".place_order");
                } else {
                    System.err.println("Cannot place orders on prod!!!");
                }
                break;
        }
    }

    public void signInCheckout(String pageName, HashMap<String, String> opts, boolean bops) throws Exception {
        // check if we ended up in rc
        if (onPage("responsive_checkout_signed_in")) {
            rcSignedIn(RCPage.fromString(pageName), opts, bops);
            return;
        }

        String page = "shipping_payment_signed_in";
        switch (pageName.toLowerCase()) {
            case "shipping & payment":
                //Cookies.forceNonRc();
                if (!onPage(page)) {
                    checkoutUtils.navigateToCheckout(true, false);
                }
                break;
            case "order review":
                if (!Elements.elementPresent(page + (bops ? ".bops_section_header" : ".shipping_address_section"))) {
                    signInCheckout("shipping & payment", opts, bops);
                }

                if (!bops && !Elements.elementPresent(page + ".shipping_address_tiles") &&
                        !Elements.elementPresent(page + ".pickup_first_name")) {
                    By asab = Elements.element(page + ".add_shipping_address_button");
                    Wait.secondsUntilElementPresent(asab, 10);
                    Clicks.click(asab);
                    fillShippingData(false, false, opts);
                    Clicks.click(page + ".save_address");
                }

                if ((Elements.elementPresent(page + ".pickup_first_name") &&
                        Elements.getElementAttribute(page + ".pickup_first_name", "value").equals(""))) {
                    fillShippingData(false, bops, opts);
                }

                if (!Elements.elementPresent(page + ".credit_card_tiles")) {
                    By accb = Elements.element(page + ".add_credit_card_button");
                    Wait.secondsUntilElementPresent(accb, 10);
                    Clicks.click(accb);
                    new Checkout().fillPaymentData(false, false, opts);
                }

                if (Elements.elementPresent(page + ".security_code")) {
                    if (creditCard == null) {
                        creditCard = TestUsers.getValidVisaCreditCard();
                    }
                    TextBoxes.typeTextbox(page + ".security_code", creditCard.getSecurityCode());
                }
                Clicks.click(page + ".continue_checkout_button");
                break;
            case "order confirmation":
                if (!Elements.elementPresent("order_review.shipping_address_section")) {
                    signInCheckout("order review", opts, bops);
                }
                if (!prodEnv()) {
                    if (safari()) {
                        Wait.secondsUntilElementPresent("order_review.place_order_button", 10);
                    }
                    Clicks.click("order_review.place_order_button");
                } else {
                    System.err.println("Cannot place orders on prod!!!");
                }
                break;
        }
    }

    public void fillBillingAddress(HashMap<String, String> opts) throws Throwable {

        boolean responsive = onPage("responsive_checkout, responsive_checkout_signed_in".split(", "));
        boolean iship = onPage("iship_checkout");
        String page = (iship ? "iship_checkout" : (signedIn() ? (responsive ? "responsive_checkout_signed_in" : "shipping_payment_signed_in") : "responsive_checkout"));
        ProfileAddress address = TestUsers.getRandomValidAddress(opts);
        if (!iship && responsive) {
            Clicks.unSelectCheckbox(page + ".use_shipping_address");
            Wait.untilElementPresent(page + ".first_name");
//            if (!signedIn() && Elements.elementPresent(page + ".use_shipping_address") && Elements.getElementAttribute(page + ".use_shipping_address", (signedIn() ? "aria-checked" : "value")).equals("false")) {
//                Clicks.click(page + ".use_shipping_address");
//            }
            if(Elements.elementPresent(page + ".first_name") || Elements.getElementAttribute(page + ".use_shipping_address", (signedIn() ? "aria-checked" : "value")).equals("false")) {
                TextBoxes.typeTextbox(page + ".first_name", TestUsers.generateRandomFirstName());
                TextBoxes.typeTextbox(page + ".last_name", TestUsers.generateRandomLastName());
                TextBoxes.typeTextbox(page + ".address_line_1", address.getAddressLine1());
                TextBoxes.typeTextbox(page + ".address_line_2", address.getAddressLine2());
                TextBoxes.typeTextbox(page + ".address_city", address.getCity());
                String state = StatesUtils.getAbbreviation(address.getState());
                DropDowns.selectByText(page + ".address_state", state);
                TextBoxes.typeTextbox(page + ".address_zip_code", address.getZipCode().toString());
            }
        } else {
            TextBoxes.typeTextbox(page + ".billing" + "_first_name", TestUsers.generateRandomFirstName());
            TextBoxes.typeTextbox(page + ".billing" + "_last_name", TestUsers.generateRandomLastName());
            TextBoxes.typeTextbox(page + ".billing" + "_address_line_1", address.getAddressLine1());
            TextBoxes.typeTextbox(page + ".billing" + "_address_city", address.getCity());
            String state = address.getState();
            DropDowns.selectByText(page + ".billing" + "_address_state", state);
            TextBoxes.typeTextbox(page + ".billing" + "_address_zip_code", address.getZipCode().toString());
            TextBoxes.typeTextbox(page + ".billing" + "_phone_area_code", address.getPhoneAreaCode());
            TextBoxes.typeTextbox(page + ".billing" + "_phone_exchange", address.getPhoneExchange());
            TextBoxes.typeTextbox(page + ".billing" + "_phone_subscriber", address.getPhoneSubscriber());
            TextBoxes.typeTextbox(page + ".billing" + "_email", address.getEmail());
        }
    }

    public void add3DSecureCard(String card_type, boolean responsive, boolean iship) throws Throwable {
        String page = responsive ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout") : (!signedIn() ? "payment_guest" : "shipping_payment_signed_in");
        creditCard = TestUsers.getValid3DSecureCard(card_type);
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        Wait.forPageReady();
        if (signedIn()) {
            Clicks.clickIfPresent(page + ".change_credit_card_button");
            if (safari()) {
                Wait.secondsUntilElementPresent(page + ".add_credit_card_button", 10);
            }
            if (isCreditCardAlreadyAdded(creditCard, page)) {
                Clicks.click(page + ".edit_card");
            } else {
                Clicks.click(page + ".add_credit_card_button");
            }
            if (bloomingdales() && page.equals("responsive_checkout_signed_in")) {
                MyWallet.addCard(creditCard);
                Navigate.visit(page);
            } else {
                fillPaymentData(responsive, iship, opts);
                if (signedIn() && responsive) {
                    Clicks.clickIfPresent(page + ".save_card");
                    Wait.untilElementNotPresent(page + ".save_card");
                }
            }
        } else {
            Clicks.click(page + ".credit_card_radio_button");
            TextBoxes.typeTextbox(page + ".card_number", creditCard.getCardNumber());
            if (!responsive || macys()) {
                DropDowns.selectByText(page + ".expiry_month", creditCard.getExpiryMonthIndex() + (responsive || iship ? "" : " - " + creditCard.getExpiryMonth()));
                DropDowns.selectByText(page + ".expiry_year", creditCard.getExpiryYear());
                DropDowns.selectByText(page + ".card_type", card_type);
            } else {
                DropDowns.selectCustomText(page + ".expiry_month_list", page + ".expiry_month_options", creditCard.getExpiryMonthIndex() + (responsive || iship ? "" : " - " + creditCard.getExpiryMonth()));
                DropDowns.selectCustomText(page + ".expiry_year_list", page + ".expiry_year_options", creditCard.getExpiryYear());
                DropDowns.selectCustomText(page + ".card_type_list", page + ".card_type_options", card_type);
            }
        }
        /*if (signedIn()) {
            typeTextbox(page + ".first_name", TestUsers.generateRandomFirstName());
            typeTextbox(page + ".last_name", TestUsers.generateRandomLastName());
            typeTextbox(page + ".address_line_1", address.getString("address_line_1"));
            typeTextbox(page + ".address_city", address.getString("address_city"));
            String state = address.getString("address_state");
            state = StatesUtils.translateAbbreviation(state);
            selectByText(page + ".address_state", state);
            typeTextbox(page + ".address_zip_code", address.getString("address_zip_code"));
            String number = TestUsers.generateRandomPhoneNumber();
            typeTextbox(page + ".card_phone_area_code", number.substring(0, 3));
            typeTextbox(page + ".card_phone_exchange", number.substring(3, 6));
            typeTextbox(page + ".card_phone_subscriber", number.substring(6));
            typeTextbox(page + ".payment_email", TestUsers.generateRandomEmail(5));
            click(page + ".save_card");
            secondsUntilElementNotPresent(page + ".card_phone_area_code", 5);
        }*/
        // adding wait time, as it is consistently failing in jenkins
        Wait.secondsUntilElementPresent(page + ".security_code", 5);
        if (Elements.elementPresent(page + ".security_code") && Elements.getText(page + ".security_code").isEmpty()) {
            TextBoxes.typeTextbox(page + ".security_code", creditCard.getSecurityCode());
        }
        if (page.equals("shipping_payment_signed_in")) {
            Clicks.click(page + ".continue_checkout_button");
            Wait.forPageReady();
        }
    }

    public boolean isCreditCardAlreadyAdded(CreditCard creditCard, String page) throws Throwable {
        boolean isCardAdded = false;
        if (Elements.elementPresent(page + ".payment_info") && (Elements.findElement(Elements.element(page + ".payment_info")).findElements(By.tagName("p")).size() > 0) && (Elements.findElement(Elements.element(page + ".payment_info")).findElement(By.tagName("p")).getText().toLowerCase().contains(creditCard.getCardType().name.toLowerCase()))) {
            isCardAdded = true;
        }
        return isCardAdded;
    }

    public void selectPlaceOrderButton() {
        if (prodEnv()) {
            System.err.println("Cannot place orders on prod!!!");
            return;
        }
        if (signedIn() && onPage("responsive_checkout_signed_in")) {
            Clicks.click("responsive_checkout_signed_in.place_order");
        } else if (onPage("responsive_checkout")) {
            Clicks.click("responsive_checkout.place_order");
        } else if (onPage("order_review")) {
            Clicks.click("order_review.verify_page");
        } else {
            Assert.fail("Unable to place order");
        }
    }

    public String getPageName(String pageName) {
        String page;
        switch (pageName) {
            case "shipping":
                page = onPage("responsive_checkout") ? "responsive_checkout" : "shipping_guest";
                break;
            case "order review":
                page = onPage("order_review") ? "order_review" : "responsive_order_review_section";
                break;
            case "shipping & payment":
            case "shipping and payment":
                page = onPage("responsive_checkout_signed_in") ? "responsive_checkout_signed_in" : "shipping_payment_signed_in";
                break;
            default:
                page = onPage("order_confirmation") ? "order_confirmation" : "responsive_order_confirmation";
        }
        return page;
    }

    public void fillResponsiveBCAddressInfo(HashMap<String, String> opts, String section, RCPage page, boolean responsive) {
        ProfileAddress address = TestUsers.getRandomValidAddress(opts);
        Wait.untilElementPresent(page + ".use_shipping_address");
        Clicks.unSelectCheckbox(page + ".use_shipping_address");
        Wait.untilElementPresent(page + ".first_name");
//            if (!signedIn() && Elements.elementPresent(page + ".use_shipping_address")) {
//                Clicks.click(page + ".use_shipping_address");
//            }
        TextBoxes.typeTextbox(page + ".first_name", TestUsers.generateRandomFirstName());
        TextBoxes.typeTextbox(page + ".last_name", TestUsers.generateRandomLastName());
        TextBoxes.typeTextbox(page + ".address_line_1", address.getAddressLine1());
        TextBoxes.typeTextbox(page + ".address_line_2", address.getAddressLine2());
        TextBoxes.typeTextbox(page + ".address_city", address.getCity());
        String state = address.getState();
        if (responsive) {
            state = macys() ? StatesUtils.getAbbreviation(state) : state;
        }
        if (!responsive || macys() || Elements.elementPresent(page + ".address_state")) {
            DropDowns.selectByText(page + ".address_state", state);
        } else {
            DropDowns.selectCustomText(page + ".address_state_list", page + ".address_state_options", state);
        }
        TextBoxes.typeTextbox(page + ".address_zip_code", address.getZipCode().toString());
        if (responsive) {
            if (section.equals("shipping")) {
                TextBoxes.typeTextbox(page + ".phone_number", address.getBestPhone());
            } else {
                fillContactDetails(true, page.toString(), address);
            }
        }
    }

    /**
     * Method to retrun all the shipping method details displayed on checkout page
     * @return shippingMethods
     **/

    public HashMap<String, HashMap> getShippingMethods() {
        String pageName = signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout";
        HashMap<String, HashMap> shippinigMethods = new HashMap();
        List<WebElement> shippingMethodElements = Elements.findElements(pageName + "." + (signedIn() ? "signin_" : "") + "shipping_methods");
        for(WebElement element : shippingMethodElements) {
            if (element.isDisplayed()){
                String[] details = element.findElement(By.tagName("label")).getText().trim().split("\n");
                WebElement radioButtonElement = element.findElement(By.tagName("input"));
                ArrayList<String> shippingMethodDetails = new ArrayList<String>();
                for (String shippingInfo : details) { shippingMethodDetails.add(shippingInfo); }
                shippingMethodDetails.removeIf(item -> item == null || "".equals(item));
                HashMap shippingAttributes = new HashMap();
                shippingAttributes.put("isSelected", radioButtonElement.isSelected());
                shippingAttributes.put("isEnabled", element.isEnabled());

                HashMap shippingDetails = new HashMap();
                shippingDetails.put("shippingMethodAttributes", shippingAttributes);
                shippingDetails.put("shippingMethodDetails", shippingMethodDetails);
                shippinigMethods.put(shippingMethodDetails.get(0).toString().toLowerCase().replaceAll("\\s",""), shippingDetails);
            }
        }
        return shippinigMethods;
    }

}

