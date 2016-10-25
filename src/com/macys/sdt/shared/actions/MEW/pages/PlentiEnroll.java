package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Assert;

/**
 * For enrolling Plenti in mew
 */
public abstract class PlentiEnroll extends StepUtils {

    /**
     * This method is to enroll customer to plenti
     *
     * @param customer customer details
     */
    public static void enroll(final UserProfile customer) {
        final User user = customer.getUser();
        final ProfileAddress profileAddress = user.getProfileAddress();

        if (Elements.getText("plenti_enroll.first_name").equals("null"))  {
            TextBoxes.typeTextbox("plenti_enroll.first_name", profileAddress.getFirstName());
            TextBoxes.typeTextbox("plenti_enroll.last_name", profileAddress.getLastName());
            DropDowns.selectByText("plenti_enroll.dob_month", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            DropDowns.selectByIndex("plenti_enroll.dob_day", user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
            DropDowns.selectByIndex("plenti_enroll.dob_year", user.getDateOfBirth(user.getDateOfBirth()).getYear());
            TextBoxes.typeTextbox("plenti_enroll.email", profileAddress.getEmail());
        }

        TextBoxes.typeTextbox("plenti_enroll.address_line_1", profileAddress.getAddressLine1());
        TextBoxes.typeTextbox("plenti_enroll.address_line_2", profileAddress.getAddressLine2());
        TextBoxes.typeTextbox("plenti_enroll.address_city", profileAddress.getCity());
        DropDowns.selectByText("plenti_enroll.address_state", StatesUtils.translateAbbreviation(profileAddress.getState()));
        TextBoxes.typeTextbox("plenti_enroll.address_zipcode", String.valueOf(profileAddress.getZipCode()));
        TextBoxes.typeTextbox("plenti_enroll.phone_number", profileAddress.getBestPhone());
        Clicks.clickWhenPresent("plenti_enroll.btn_goto_plenti");
        Assert.assertTrue("ERROR-DATA: Unable to enroll in to plenty due to inputted data issue", !Elements.elementPresent("plenti_enroll.page_level_error"));
    }


}

