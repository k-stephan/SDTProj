package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.AbbreviationHelper;
import com.macys.sdt.framework.utils.StepUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;


public class CreateProfile extends StepUtils {

    /**
     * Creates a new profile
     * <p> Edge Cases: <br>
     *  1. to use invalid dob. <br>
     *  2. to input EMPTY phone field. <br>
     *  3. to input invalid/incomplete phone field. <br>
     *  4. to input all same digits for phone. <br>
     *  <br> For Ex: to input all same digits for phone <br>
     *  createProfile(customer, false, false, false, true)
     * </p>
     *
     * @param customer instance of UserProfile model
     * @param edgeCase optional boolean values for edge Cases
     */
    public static void createProfile(UserProfile customer, boolean... edgeCase) {
        if (!onPage("create_profile")) {
            Navigate.visit("create_profile");
        }
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
            selectDropDownIfPresent("create_profile.address_state", ((profileAddress.getState().length() == 2) ? AbbreviationHelper.translateStateAbbreviation(profileAddress.getState()) : profileAddress.getState()));
            if(edgeCase.length > 0 && edgeCase[0]) {
                selectDropDownIfPresent("create_profile.dob_month", "February");
                selectDropDownIfPresent("create_profile.dob_day", "31");//February doesn't have 31 days thereby making this date as invalid date.
                selectDropDownIfPresent("create_profile.dob_year", "1960");//just any random year
            }
            else {
                selectDropDownIfPresent("create_profile.dob_month", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
                selectDropDownIfPresent("create_profile.dob_day", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth()));
                selectDropDownIfPresent("create_profile.dob_year", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getYear()));
            }
            selectDropDownIfPresent("create_profile.security_question", user.getUserPasswordHint().getQuestion());
        } else {
            //Bloomingdales
            selectCustomDropDownIfPresent("create_profile.address_state_list", "create_profile.state_options", ((profileAddress.getState().length() == 2) ? AbbreviationHelper.translateStateAbbreviation(profileAddress.getState()) : profileAddress.getState()));
            if(Elements.elementPresent("create_profile.dob_month")) {
                if (edgeCase.length > 0 && edgeCase[0])
                    selectDropDownIfPresent("create_profile.dob_month", "February");
                else
                    DropDowns.selectByText("create_profile.dob_month", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            }
            else {
                if (edgeCase.length > 0 && edgeCase[0])
                    selectCustomDropDownIfPresent("create_profile.dob_month_list", "create_profile.dob_month_options", "February");
                else
                    selectCustomDropDownIfPresent("create_profile.dob_month_list", "create_profile.dob_month_options", WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            }
            if(Elements.elementPresent("create_profile.dob_day")){
                if (edgeCase.length > 0 && edgeCase[0])
                    DropDowns.selectByText("create_profile.dob_day", "31");//February doesn't have 31 days thereby making this date as invalid date.
                else
                    DropDowns.selectByText("create_profile.dob_day", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth()));
            }
            else {
                if (edgeCase.length > 0 && edgeCase[0])
                    selectCustomDropDownIfPresent("create_profile.dob_day_list", "create_profile.dob_day_options", "31");//February doesn't have 31 days thereby making this date as invalid date.
                else
                    selectCustomDropDownIfPresent("create_profile.dob_day_list", "create_profile.dob_day_options", String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth()));
            }
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

        Clicks.selectCheckbox(Elements.element("create_profile.textme_yes"));
        if(edgeCase.length > 0 && edgeCase[1]) {
            typeTextBoxIfPresent("create_profile.phone_number", "");//missing phone
        }
        else if(edgeCase.length > 0 && edgeCase[2]) {
            typeTextBoxIfPresent("create_profile.phone_number", "500");//invalid/incomplete phone
        }
        else if(edgeCase.length > 0 && edgeCase[3]) {
            typeTextBoxIfPresent("create_profile.phone_number", "5555555555");//all same digits for phone
        }
        else {
            typeTextBoxIfPresent("create_profile.phone_number", profileAddress.getBestPhone());//valid phone
        }

        if (chrome())
            Clicks.click("create_profile.create_profile_button");
        else
            Clicks.javascriptClick("create_profile.create_profile_button");

        Wait.secondsUntilElementNotPresent("create_profile.create_profile_button", MainRunner.timeout);
        resumePageHangWatchDog();
    }

    /**
     * Fills the textbox with the specified value if it is present
     *
     * @param elementName page.element is the field name which has to fill
     * @param value is to be filled in the text box
     */
    public static void typeTextBoxIfPresent(String elementName, String value) {
        if (Elements.elementPresent(elementName))
            TextBoxes.typeTextbox(elementName, value);
    }

    /**
     * Selects the specified text from the dropdown if dropdown is present
     *
     * @param elementName page.element dropdown element
     * @param value to select in dropdown
     */
    public static void selectDropDownIfPresent(String elementName, String value) {
        if (Elements.elementPresent(elementName))
            DropDowns.selectByText(elementName, value);
    }

    /**
     * Select text from a drop down list if drop down is present
     * Note: Using this for BCOM as in Account pages the select elements are hidden in UI
     *
     * @param elementName drop down list element
     * @param elementOptions list of elements
     * @param value to select
     */
    public static void selectCustomDropDownIfPresent(String elementName, String elementOptions, String value) {
        if (Elements.elementPresent(elementName))
            DropDowns.selectCustomText(elementName, elementOptions, value);
    }

    /**
     * Closes the security alert popup for IE browser if appears
     */
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
