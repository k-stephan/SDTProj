package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;

public class LoyaltyEnrollment extends StepUtils {

    public void signedInUserLoyaltyEnrollment(UserProfile customer) {
        ProfileAddress profileAddress = customer.getUser().getProfileAddress();
        TextBoxes.typeTextbox("loyalty_enrollment.address_line_1", profileAddress.getAddressLine1());
        TextBoxes.typeTextbox("loyalty_enrollment.address_line_2", profileAddress.getAddressLine2());
        TextBoxes.typeTextbox("loyalty_enrollment.address_city", profileAddress.getCity());
        DropDowns.selectByValue("loyalty_enrollment.address_state", StatesUtils.getAbbreviation(profileAddress.getState()));
        TextBoxes.typeTextbox("loyalty_enrollment.address_zip_code", profileAddress.getZipCode().toString());
        TextBoxes.typeTextbox("loyalty_enrollment.phone_number", profileAddress.getBestPhone());
        Clicks.click("loyalty_enrollment.goto_terms_and_conditions");
        Wait.untilElementPresent("loyalty_enrollment.accept_terms_and_conditions");
        Clicks.click("loyalty_enrollment.accept_terms_and_conditions");
        Wait.until(() -> Elements.getText("loyalty_enrollment.terms_and_conditions_status").equals("Accepted"));
        Clicks.javascriptClick("loyalty_enrollment.submit_btn");
        Wait.untilElementPresent("loyalty_enrollment_confirmation.loyalty_number");
    }
}