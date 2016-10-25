package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;


public class CreateProfile extends StepUtils {

    public static void createProfile(UserProfile customer) {
        pausePageHangWatchDog();
        User user = customer.getUser();
        ProfileAddress profileAddress = user.getProfileAddress();
        if(!safari())
            stopPageLoad();
        TextBoxes.typeTextbox("create_profile.verify_page", profileAddress.getFirstName());
        TextBoxes.typeTextbox("create_profile.last_name", profileAddress.getLastName());
        typeTextBoxIfPresent("create_profile.address_line_1", profileAddress.getAddressLine1());
        typeTextBoxIfPresent("create_profile.address_city", profileAddress.getCity());

        if (macys() || Elements.elementPresent("create_profile.address_state")) {
            selectDropDownIfPresent("create_profile.address_state", ((profileAddress.getState().length() == 2) ? StatesUtils.translateAbbreviation(profileAddress.getState()) : profileAddress.getState()));
            selectDropDownIfPresent("create_profile.dob_month", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            selectDropDownIfPresent("create_profile.dob_day", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth()));
            selectDropDownIfPresent("create_profile.dob_year", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getYear()));
            selectDropDownIfPresent("create_profile.security_question", user.getUserPasswordHint().getQuestion());
        } else {
            selectCustomDropDownIfPresent("create_profile.address_state_list", "create_profile.state_options", ((profileAddress.getState().length() == 2) ? StatesUtils.translateAbbreviation(profileAddress.getState()) : profileAddress.getState()));
            if(Elements.elementPresent("create_profile.dob_month"))
                DropDowns.selectByText("create_profile.dob_month", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            else
                selectCustomDropDownIfPresent("create_profile.dob_month_list", "create_profile.dob_month_options", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            if(Elements.elementPresent("create_profile.dob_day"))
                DropDowns.selectByText("create_profile.dob_day", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth()));
            else
                selectCustomDropDownIfPresent("create_profile.dob_day_list", "create_profile.dob_day_options", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth()));
            if(Elements.elementPresent("create_profile.dob_year"))
                DropDowns.selectByText("create_profile.dob_year", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getYear()));
            else
                selectCustomDropDownIfPresent("create_profile.dob_year_list", "create_profile.dob_year_options", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getYear()));
            if(Elements.elementPresent("create_profile.security_question"))
                DropDowns.selectByText("create_profile.security_question", user.getUserPasswordHint().getQuestion());
            else
                selectCustomDropDownIfPresent("create_profile.security_question_list", "create_profile.security_question_options", user.getUserPasswordHint().getQuestion());
        }

        typeTextBoxIfPresent("create_profile.address_zip_code", String.valueOf(profileAddress.getZipCode()));
        if (macys()) {
            selectDropDownIfPresent("create_profile.gender", user.getGender());
        } else {
            Clicks.clickIfPresent("create_profile.gender_female");
        }

        TextBoxes.typeTextbox("create_profile.email", profileAddress.getEmail());
        typeTextBoxIfPresent("create_profile.email_verify", profileAddress.getEmail());
        TextBoxes.typeTextbox("create_profile.password", user.getLoginCredentials().getPassword());
        if (Elements.elementPresent("create_profile.password_verify"))
            TextBoxes.typeTextbox("create_profile.password_verify", user.getLoginCredentials().getPassword());

        typeTextBoxIfPresent("create_profile.security_answer", user.getUserPasswordHint().getAnswer());

        if (chrome())
            Clicks.click("create_profile.create_profile_button");
        else
            Clicks.javascriptClick("create_profile.create_profile_button");

        Wait.secondsUntilElementNotPresent("create_profile.create_profile_button", MainRunner.timeout);
        resumePageHangWatchDog();
    }

    public static void typeTextBoxIfPresent(String elementName, String value) {
        if (Elements.elementPresent(elementName))
            TextBoxes.typeTextbox(elementName, value);
    }

    public static void selectDropDownIfPresent(String elementName, String value) {
        if (Elements.elementPresent(elementName))
            DropDowns.selectByText(elementName, value);
    }

    public static void selectCustomDropDownIfPresent(String elementName, String elementOptions, String value) {
        if (Elements.elementPresent(elementName))
            DropDowns.selectCustomText(elementName, elementOptions, value);
    }

    public static void closeSecurityAlertPopUp() {
        if (ie()) {
            try {
                Alert alert = MainRunner.getWebDriver().switchTo().alert();
                alert.accept();
            } catch (NoAlertPresentException e) {
                e.printStackTrace();
            }
        }
    }
}
