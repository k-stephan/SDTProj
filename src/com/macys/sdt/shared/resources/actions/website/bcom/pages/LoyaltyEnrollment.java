package com.macys.sdt.shared.resources.actions.website.bcom.pages;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.resources.model.ProfileAddress;
import com.macys.sdt.framework.resources.model.User;
import com.macys.sdt.framework.resources.model.UserProfile;
import com.macys.sdt.framework.utils.StepUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Assert;

public class LoyaltyEnrollment extends StepUtils {

    public void guestUserLoyaltyEnrollment(UserProfile customer) {
        User user = customer.getUser();
        ProfileAddress profileAddress = user.getProfileAddress();
        
        Wait.forPageReady();
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.first_name"), profileAddress.getFirstName());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.last_name"), profileAddress.getLastName());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.address_line_1"), profileAddress.getAddressLine1());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.address_line_2"), profileAddress.getAddressLine2());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.address_city"), profileAddress.getCity());
      //  DropDowns.selectByText(Elements.element("loyalty_enrollment.address_state"), profileAddress.getState());
        DropDowns.selectCustomText("loyalty_enrollment.address_state", "loyalty_enrollment.state_options",  profileAddress.getState());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.address_zip_code"), String.valueOf(profileAddress.getZipCode()));
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.email"), profileAddress.getEmail());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.verify_email"), profileAddress.getEmail());
       // DropDowns.selectByText(Elements.element("loyalty_enrollment.dob_month"), WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
        DropDowns.selectCustomText("loyalty_enrollment.dob_month", "loyalty_enrollment.dob_month_option",  WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
     //   DropDowns.selectByIndex(Elements.element("loyalty_enrollment.dob_day"), user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
        DropDowns.selectCustomValue("loyalty_enrollment.dob_day", "loyalty_enrollment.dob_day_option", user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
      //  DropDowns.selectByIndex(Elements.element("loyalty_enrollment.dob_year"),user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
        DropDowns.selectCustomValue("loyalty_enrollment.dob_year", "loyalty_enrollment.dob_year_option", user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.phone_area_code"), profileAddress.getPhoneAreaCode());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.phone_exchange"), profileAddress.getPhoneExchange());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.phone_subscriber"), profileAddress.getPhoneSubscriber());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.password"), user.getLoginCredentials().getPassword());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.verify_password"), user.getLoginCredentials().getPassword());
       // DropDowns.selectByText(Elements.element("loyalty_enrollment.security_question"), user.getUserPasswordHint().getQuestion());
        DropDowns.selectCustomText("loyalty_enrollment.security_question", "loyalty_enrollment.security_question_option",  user.getUserPasswordHint().getQuestion());
        TextBoxes.typeTextbox(Elements.element("loyalty_enrollment.security_answer"), user.getUserPasswordHint().getAnswer());
        Clicks.selectCheckbox(Elements.element("loyalty_enrollment.agree_to_terms_and_conditions"));
        try {
            Clicks.click("loyalty_enrollment.enroll_btn");
            Wait.untilElementPresent(Elements.element("loyalty_enrollment_confirmation.loyalty_number"));
        } catch (Exception e) {
            Assert.fail("Failed to validate loyalty data in the page: " + e);
        }
    }

    public void signedInUserLoyaltyEnrollment(UserProfile customer) {
        ProfileAddress profileAddress = customer.getUser().getProfileAddress();
        TextBoxes.typeTextbox("loyalty_enrollment.address_line_1", profileAddress.getAddressLine1());
        TextBoxes.typeTextbox("loyalty_enrollment.address_line_2", profileAddress.getAddressLine2());
        TextBoxes.typeTextbox("loyalty_enrollment.address_city", profileAddress.getCity());
        DropDowns.selectCustomText("loyalty_enrollment.address_state_list", "loyalty_enrollment.state_options", profileAddress.getState());
        TextBoxes.typeTextbox("loyalty_enrollment.address_zip_code", profileAddress.getZipCode().toString());
        TextBoxes.typeTextbox("loyalty_enrollment.phone_area_code", profileAddress.getPhoneAreaCode());
        TextBoxes.typeTextbox("loyalty_enrollment.phone_exchange", profileAddress.getPhoneExchange());
        TextBoxes.typeTextbox("loyalty_enrollment.phone_subscriber", profileAddress.getPhoneSubscriber());
        Clicks.selectCheckbox("loyalty_enrollment.agree_to_terms_and_conditions");
        try {
            Clicks.click("loyalty_enrollment.enroll_now_signed_in");
            Wait.untilElementPresent("loyalty_enrollment_confirmation.loyalty_number");
        } catch (Exception e) {
            Assert.fail("Failed to validate loyalty data in the page: " + e);
        }
    }

}