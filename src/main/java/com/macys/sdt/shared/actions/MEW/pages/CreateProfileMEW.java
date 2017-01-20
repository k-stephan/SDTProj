package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.Alert;

import java.util.HashMap;


public class CreateProfileMEW extends StepUtils {

    public static void createProfile(final UserProfile customer) {
        final User user = customer.getUser();
        final ProfileAddress profileAddress = user.getProfileAddress();
        if (!onPage("create_profile")) {
            Navigate.visit("create_profile");
        }

        Wait.forPageReady();
        TextBoxes.typeTextbox("create_profile.verify_page", profileAddress.getFirstName());
        TextBoxes.typeTextbox("create_profile.last_name", profileAddress.getLastName());
        ifPresentDo("create_profile.address_line_1", () -> TextBoxes.typeTextbox("create_profile.address_line_1", profileAddress.getAddressLine1()));
        ifPresentDo("create_profile.address_line_2", () -> TextBoxes.typeTextbox("create_profile.address_line_2", profileAddress.getAddressLine2()));
        ifPresentDo("create_profile.address_city", () -> TextBoxes.typeTextbox("create_profile.address_city", profileAddress.getCity()));
        ifPresentDo("create_profile.address_state", () -> DropDowns.selectByValue("create_profile.address_state", profileAddress.getState()));
        ifPresentDo("create_profile.address_zip_code", () -> TextBoxes.typeTextbox("create_profile.address_zip_code", String.valueOf(profileAddress.getZipCode())));
        DropDowns.selectByText("create_profile.dob_month", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
        DropDowns.selectByIndex("create_profile.dob_day", user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
        DropDowns.selectByValue("create_profile.dob_year", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getYear()));

        if (macys()) {
            ifPresentDo("create_profile.gender", () -> DropDowns.selectByText("create_profile.gender", user.getGender()));
        } else {
            Clicks.clickIfPresent("create_profile.gender_female");
        }
        TextBoxes.typeTextbox("create_profile.email", profileAddress.getEmail());
        ifPresentDo("create_profile.email_verify", () -> TextBoxes.typeTextbox("create_profile.email_verify", profileAddress.getEmail()));
        TextBoxes.typeTextbox("create_profile.password", user.getLoginCredentials().getPassword());
        ifPresentDo("create_profile.password_verify", () -> TextBoxes.typeTextbox("create_profile.password_verify", user.getLoginCredentials().getPassword()));
        ifPresentDo("create_profile.security_question", () -> DropDowns.selectByText("create_profile.security_question", user.getUserPasswordHint().getQuestion()));
        ifPresentDo("create_profile.security_answer", () -> TextBoxes.typeTextbox("create_profile.security_answer", user.getUserPasswordHint().getAnswer()));

        Clicks.click("create_profile.create_profile_button");
        Wait.secondsUntilElementNotPresent("create_profile.create_profile_button", MainRunner.timeout);
    }

    public static void removeAddress() {
        String page;
        if (onPage("responsive_checkout_signed_in") || onPage("my_address_book")) {
            page = "my_address_book";
            Clicks.javascriptClick(page + ".remove_link");
            Wait.untilElementPresent(page + ".message");
        } else if (onPage("shipping_payment_signed_in")) {
            page = "shipping_payment_signed_in";
            Clicks.javascriptClick(page + ".address_delete_button");
            Clicks.javascriptClick(page + ".confirm_delete");
            Wait.untilElementPresent(page + ".no_address");
        }
    }

    public static void addNewAddress() {
        String page = null;
        if (onPage("responsive_checkout_signed_in") || onPage("my_address_book")) {
            page = "my_address_book";
        }  else if (onPage("shipping_payment_signed_in")) {
            page = "shipping_payment_signed_in";
        }

        if (prodEnv()) {
            page = "shipping_payment_signed_in";
        }
        final HashMap<String, String> opts = new HashMap<>();
        opts.put("sdd_eligible", "true");

        if (macys() || MEW()) {
            opts.put("site", "mcom");
        } else {
            opts.put("site", "bcom");
        }
        final ProfileAddress address = new ProfileAddress();
        TestUsers.getRandomValidAddress(opts, address);

        Wait.forPageReady();
        TextBoxes.typeTextbox(page + ".first_name", TestUsers.generateRandomFirstName());
        TextBoxes.typeTextbox(page + ".last_name", TestUsers.generateRandomLastName());
        TextBoxes.typeTextbox(page + ".address_line_1", address.getAddressLine1());
        TextBoxes.typeTextbox(page + ".address_line_2", address.getAddressLine2());
        TextBoxes.typeTextbox(page + ".address_city", address.getCity());
        DropDowns.selectByText(page + ".address_state", address.getState());
        TextBoxes.typeTextbox(page + ".address_zip_code", address.getZipCode().toString());
        final String page1 = page;
        ifPresentDo(page + ".phone_area_code", () -> TextBoxes.typeTextbox(page1 + ".phone_area_code", address.getPhoneAreaCode()));
        ifPresentDo(page + ".phone_exchange", () -> TextBoxes.typeTextbox(page1 + ".phone_exchange", address.getPhoneExchange()));
        ifPresentDo(page + ".phone_subscriber", () -> TextBoxes.typeTextbox(page1 + ".phone_subscriber", address.getPhoneSubscriber()));
        ifPresentDo(page + ".phone_number", () -> TextBoxes.typeTextbox(page1 + ".phone_number", address.getBestPhone()));
        Clicks.click(page + ".add_shipping_address_button");
        Wait.untilElementPresent(page + ".message");
    }

    public static void closeSecurityAlertPopUp() {
        if (ie()) {
            try {
                final Alert alert = MainRunner.getWebDriver().switchTo().alert();
                alert.accept();
            } catch (org.openqa.selenium.NoAlertPresentException e) {
                e.printStackTrace();
            }
        }
    }

}
