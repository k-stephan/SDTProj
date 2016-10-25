package com.macys.sdt.shared.actions.website.bcom.pages;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.website.mcom.pages.checkout.Checkout;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.MyAddressBook;
import com.macys.sdt.shared.utils.CheckoutUtils;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;

public class CheckoutPageBcom extends StepUtils {

    private CheckoutUtils m_checkout = new CheckoutUtils();

    public void fillGuestShippingData(HashMap<String, String> opts, boolean bops) throws NoSuchElementException {
        boolean iship = opts != null && opts.get("country") != null && !opts.get("country").equalsIgnoreCase("United States");
        String page = onPage("responsive_checkout, responsive_checkout_signed_in".split(", ")) ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout") : "shipping_guest";
        String type = bops ? ".pickup" : ".shipping";
        if (!Wait.untilElementPresent(page + type + "_first_name"))
            throw new NoSuchElementException("Element not present:" + Elements.element(page + type + "_first_name"));

        ProfileAddress address = TestUsers.getRandomValidAddress(opts);

        Wait.forPageReady();
        TextBoxes.typeTextbox(page + type + "_first_name", TestUsers.generateRandomFirstName());
        TextBoxes.typeTextbox(page + type + "_last_name", TestUsers.generateRandomFirstName());
        if (bops) {
            TextBoxes.typeTextbox(page + ".pickup_email_address", TestUsers.generateRandomEmail(5));
        } else {
            try {
                TextBoxes.typeTextbox(page + ".shipping_address_line_1", address.getAddressLine1());
                TextBoxes.typeTextbox(page + ".shipping_address_line_2", address.getAddressLine2());
                TextBoxes.typeTextbox(page + ".shipping_address_city", address.getCity());
                if (iship) {
                    if (address.getProvince() != null) {
                        DropDowns.selectByText(page + ".shipping_address_province", address.getProvince());
                    }
                    TextBoxes.typeTextbox(page + ".shipping_email_address", TestUsers.generateRandomEmail(5));
                } else
                    DropDowns.selectCustomText(page + ".shipping_address_state_list", page + ".shipping_address_state_options", (address.getState().length() == 2) ? StatesUtils.translateAbbreviation(address.getState()) : address.getState());
                TextBoxes.typeTextbox(page + ".shipping_address_" + (iship ? "postal_code" : "zip_code"), address.getZipCode().toString());
            } catch (Exception e) {
                Assert.fail("Failed to get new address: " + e);
            }
        }
        String phoneNum = TestUsers.generateRandomPhoneNumber();

        if (onPage("responsive_checkout, responsive_checkout_signed_in".split(", ")) || iship) {
            TextBoxes.typeTextbox(page + type + "_phone_number", phoneNum);
        } else {
            TextBoxes.typeTextbox(page + type + "_phone_area_code", phoneNum.substring(0, 3));
            TextBoxes.typeTextbox(page + type + "_phone_exchange", phoneNum.substring(3, 6));
            TextBoxes.typeTextbox(page + type + "_phone_subscriber", phoneNum.substring(6));
        }
    }

    public void fillGuestCardDetails(boolean responsive, boolean iship) {
        String page = iship ? "iship_checkout" : (responsive ? (signedIn() ? "responsive_checkout_signed_in" : "responsive_checkout") : (signedIn() ? "shipping_payment_signed_in" : "payment_guest"));
        CreditCard visaCard = TestUsers.getValidVisaCreditCard();

        Wait.forPageReady();
        Clicks.click(page + ".credit_card_radio_button");
        DropDowns.selectCustomText(page + ".card_type_list", page + ".card_type_options", visaCard.getCardType().toString());
        TextBoxes.typeTextbox(page + ".card_number", visaCard.getCardNumber());
        if (responsive)
            DropDowns.selectCustomText(page + ".expiry_month_list", page + ".expiry_month_options", visaCard.getExpiryMonthIndex());
        else
            DropDowns.selectByText(page + ".expiry_month", visaCard.getExpiryMonthIndex() + (iship ? " - " : "") + visaCard.getExpiryMonth());
        DropDowns.selectCustomText(page + ".expiry_year_list", page + ".expiry_year_options", visaCard.getExpiryYear());
        TextBoxes.typeTextbox(page + ".security_code", visaCard.getSecurityCode());

    }

    public void fillGuestContactDetails(boolean iship) {
        String page = iship ? "iship_checkout" : "payment_guest";
        if (!iship) {
            TextBoxes.typeTextbox(page + ".payment_email", TestUsers.generateRandomEmail(5));
            String number = TestUsers.generateRandomPhoneNumber();
            TextBoxes.typeTextbox(page + ".card_phone_area_code", number.substring(0, 3));
            TextBoxes.typeTextbox(page + ".card_phone_exchange", number.substring(3, 6));
            TextBoxes.typeTextbox(page + ".card_phone_subscriber", number.substring(6));
        }
    }

    public void fillGuestPaymentData(HashMap<String, String> opts, boolean iship) {
        String page = iship ? "iship_checkout" : "payment_guest";
        if (iship) {
            Wait.untilElementPresent(page + ".card_number");
        }

        iship = opts != null && opts.get("country") != null && !opts.get("country").equalsIgnoreCase("United States");

        ProfileAddress address = TestUsers.getRandomValidAddress(opts);

        fillGuestCardDetails(true, iship);

        TextBoxes.typeTextbox(page + ".first_name", TestUsers.generateRandomFirstName());
        TextBoxes.typeTextbox(page + ".last_name", TestUsers.generateRandomLastName());

        try {
            TextBoxes.typeTextbox(page + ".address_line_1", address.getAddressLine1());
            TextBoxes.typeTextbox(page + ".address_city", address.getCity());
            if (!iship) {
                String state = address.getState();
                DropDowns.selectByText(page + ".address_state", state);
            }
            TextBoxes.typeTextbox(page + ".address_zip_code", address.getZipCode().toString());
        } catch (Exception e) {
            Assert.fail("Failed to get new address: " + e);
        }
        fillGuestContactDetails(iship);
    }

    public void guestCheckout(String pageName, HashMap<String, String> opts, boolean bops) throws Exceptions.EnvException {
        String page = "shipping_guest";
        switch (pageName.toLowerCase()) {
            case "shipping":
                if (!onPage(page))
                    m_checkout.navigateToCheckout(false, false);
                break;
            case "payment":
                if (!onPage(page)) {
                    guestCheckout("shipping", opts, bops);
                    shouldBeOnPage(page);
                }

                if (Elements.getElementAttribute(page + (bops ? ".pickup" : ".shipping") + "_first_name", "value").equals("")) {
                    fillGuestShippingData(opts, bops);
                }

                // below condition used to enter shipping details when bag has mixed items "Bops + normal"

                if (Elements.elementPresent(page + ".shipping_first_name") && Elements.getElementAttribute(page + ".shipping_first_name", "value").equals("")) {
                    fillGuestShippingData(opts, false);
                }
                Clicks.click(page + ".continue_checkout");
                try {
                    WebElement prod_unavailable = Elements.findElement(page + ".special_order_message");
                    if (prod_unavailable.isDisplayed() && prod_unavailable.getText().contains("unavailable")) {
                        Assert.fail("DATA ERROR : Product Not Available.");
                    }
                } catch (Exception e) {
                    //ignore exception
                }

                break;
            case "order review":
                if (!Elements.elementPresent("payment_guest.credit_card_radio_button")) {
                    guestCheckout("payment", opts, bops);
                    shouldBeOnPage("payment_guest");
                }
                if (onPage("payment_guest")) {
                    Clicks.click("payment_guest.credit_card_radio_button");
                    if (Elements.getElementAttribute("payment_guest.card_number", "value").equals("")) {
                        fillGuestPaymentData(opts, false);
                        Clicks.click("payment_guest.continue_checkout_button");
                    }
                }
                break;
            case "order confirmation":
                if (!onPage("order_review")) {
                    guestCheckout("order review", opts, bops);
                    shouldBeOnPage("order_review");
                }
                if (onPage("order_review")) {
                    if (!prodEnv()) {
                        Clicks.click("page.order_review.place_order_button");
                    } else {
                        System.err.println("Cannot Place Orders in Production!!!");
                    }
                }
                break;
        }
    }

    public void signInCheckout(String pageName, HashMap<String, String> opts) throws Exception {
        String page = "shipping_payment_signed_in";
        switch (pageName.toLowerCase()) {
            case "shipping & payment":
                if (!onPage(page))
                    m_checkout.navigateToCheckout(true, false);
                break;
            case "order review":
                if (!onPage(page)) {
                    signInCheckout("shipping & payment", opts);
                }
                if (!Elements.elementPresent(page + ".shipping_address_tiles")) {
                    Clicks.click(page + ".add_shipping_address_button");
                    new Checkout().fillShippingData(false, false, opts);
                    Clicks.click(page + ".save_address");
                }
                if (!Elements.elementPresent(page + ".credit_card_tiles")) {
                    Clicks.click(page + ".add_credit_card_button");
                    new Checkout().fillPaymentData(false, false, opts);
                }

                if (Elements.elementPresent(page + ".security_code")) {
                    CreditCard visa_card = TestUsers.getValidVisaCreditCard();
                    TextBoxes.typeTextbox(page + ".security_code", visa_card.getSecurityCode());
                }
                Clicks.click(page + ".continue_checkout_button");
                break;
            case "order confirmation":
                if (!Elements.elementPresent("order_review.shipping_address_section")) {
                    signInCheckout("order review", opts);
                }
                if (!prodEnv()) {
                    Wait.untilElementPresent("order_review.place_order_button");
                    if (onPage("order_review")) {
                        Clicks.click("order_review.place_order_button");
                        Wait.untilElementNotPresent("order_review.place_order_button");
                    }
                    Wait.forPageReady("order number");
                    Wait.secondsUntilElementPresent("order_confirmation.order_number", 5);
                    if (!((onPage("responsive_order_confirmation", "order_confirmation"))
                            && (Wait.untilElementPresent("responsive_order_confirmation.order_confirmation_message")
                            || Wait.untilElementPresent("order_confirmation.order_number")))) {
                        Assert.fail("Order not placed successfully!!");
                    }
                } else {
                    System.err.println("Cannot Place Orders in Production!!!");
                }
                break;
        }
    }

    public void fillBillingAddress(HashMap<String, String> opts) {

        boolean responsive = onPage("responsive_checkout, responsive_checkout_signed_in".split(", "));
        boolean iship = false;
        CheckoutUtils.RCPage page = signedIn() ? CheckoutUtils.RCPage.SHIPPING_AND_PAYMENT : CheckoutUtils.RCPage.PAYMENT;
        ProfileAddress address = TestUsers.getRandomValidAddress(opts);
        if ((!iship && responsive) || (!responsive && !signedIn())) {
            TextBoxes.typeTextbox(page + ".first_name", address.getFirstName());
            TextBoxes.typeTextbox(page + ".last_name", address.getLastName());
            TextBoxes.typeTextbox(page + ".address_line_1", address.getAddressLine1());
            TextBoxes.typeTextbox(page + ".address_line_2", address.getAddressLine2());
            TextBoxes.typeTextbox(page + ".address_city", address.getCity());
            if(!responsive || macys())
                DropDowns.selectByText(page + ".address_state", (address.getState().length() == 2) ? StatesUtils.translateAbbreviation(address.getState()) : address.getState());
            else
                DropDowns.selectCustomText(page + ".address_state_list", page + ".address_state_options", (address.getState().length() == 2) ? StatesUtils.translateAbbreviation(address.getState()) : address.getState());
            TextBoxes.typeTextbox(page + ".address_zip_code", address.getZipCode().toString());
        } else {
            TextBoxes.typeTextbox(page + ".billing" + "_first_name", address.getFirstName());
            TextBoxes.typeTextbox(page + ".billing" + "_last_name", address.getLastName());
            TextBoxes.typeTextbox(page + ".billing" + "_address_line_1", address.getAddressLine1());
            TextBoxes.typeTextbox(page + ".billing" + "_address_city", address.getCity());
            String state = address.getState();
            DropDowns.selectByText(page + ".billing" + "_address_state", state);
            TextBoxes.typeTextbox(page + ".billing" + "_address_zip_code", address.getZipCode().toString());
            TextBoxes.typeTextbox(page + ".billing" + "_phone_area_code", address.getPhoneAreaCode());
            TextBoxes.typeTextbox(page + ".billing" + "_phone_exchange", address.getPhoneExchange());
            TextBoxes.typeTextbox(page + ".billing" + "_phone_subscriber", address.getPhoneSubscriber());
            TextBoxes.typeTextbox(page + ".billing" + "_email", TestUsers.generateRandomEmail(5));
        }
    }

    public void editShippingAddress(HashMap<String, String> opts) throws Throwable{
        String page = (signedIn() ? (onPage("shipping_payment_signed_in") ? "shipping_payment_signed_in" : "responsive_checkout_signed_in") : "responsive_checkout");
        switch (page){
            case "responsive_checkout":
                Clicks.click(page + ".edit_shipping_section");
                Wait.untilElementPresent(page + ".shipping_first_name");
                new Checkout().fillShippingData(true, false, opts);
                Clicks.click(page + ".continue_shipping_checkout_button");
                Wait.untilElementPresent(page + ".edit_shipping_section");
                break;
            case "responsive_checkout_signed_in":
                Clicks.clickIfPresent(page + ".change_shipping_address");
                Wait.secondsUntilElementPresentAndClick(page + ".edit_shipping_address", 5);
                Wait.forPageReady();
                if(macys()){
                    new Checkout().fillShippingData(true, false, opts);
                    Clicks.click(page + ".save_shipping_address");
                } else {
                    shouldBeOnPage("my_address_book");
                    new MyAddressBook().updateAddress(0, opts);
                    Navigate.visit(page);
                }
                Wait.forPageReady();
                Wait.untilElementPresent(page + ".change_shipping_address");
                if(bloomingdales()) {
                    Clicks.click(page + ".change_shipping_address");
                    Wait.untilElementPresent(page + ".save_shipping_address");
                    Clicks.click(page + ".save_shipping_address");
                }
                break;
            case "shipping_payment_signed_in":
                Clicks.click(page + ".edit_shipping_addresses");
                new Checkout().fillShippingData(false, false, opts);
                Clicks.click(page + ".save_address");
                Wait.untilElementNotPresent(page + ".save_address");
                Wait.forPageReady();
                break;
            default:
                Assert.fail("In correct page name found : " + page);
        }
    }
}
