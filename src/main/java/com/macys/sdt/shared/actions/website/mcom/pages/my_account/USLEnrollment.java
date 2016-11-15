package com.macys.sdt.shared.actions.website.mcom.pages.my_account;


import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Random;

import static com.macys.sdt.framework.utils.StatesUtils.translateAbbreviation;

public class USLEnrollment extends StepUtils {

    /**
     * Method to enroll into USL as a guest or signed  in user
     */
    public static void enroll(UserProfile customer) {
        User user = customer.getUser();
        ProfileAddress profileAddress = user.getProfileAddress();

        // Plenti does not allow PO BOX address
        while (profileAddress.getAddressLine1().toUpperCase().contains("PO BOX")) {
            TestUsers.clearCustomer();
            user = TestUsers.getuslCustomer(null, "Profile_Creation").getUser();
            profileAddress = user.getProfileAddress();
        }

        Wait.forPageReady();

        if (!signedIn()) {
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.first_name"), profileAddress.getFirstName());
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.last_name"), profileAddress.getLastName());
            // Plenti address line 1 does not allow special characters such as ., #, etc
            String addr1 = profileAddress.getAddressLine1();
            addr1 = addr1.replace(".", "");
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.address_line_1"), addr1);
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.address_line_2"), "");
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.address_city"), profileAddress.getCity());
            String state = profileAddress.getState();
            state = state.length() == 2 ? translateAbbreviation(state) : state;
            DropDowns.selectByText(Elements.element("usl_enrollment.address_state"), state);
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.address_zip_code"), String.valueOf(profileAddress.getZipCode()));
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.email"), profileAddress.getEmail());
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.verify_email"), profileAddress.getEmail());
            DropDowns.selectByText(Elements.element("usl_enrollment.dob_month"), WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()));
            DropDowns.selectByIndex(Elements.element("usl_enrollment.dob_day"), user.getDateOfBirth(user.getDateOfBirth()).getDayOfMonth());
            DropDowns.selectByText(Elements.element("usl_enrollment.dob_year"), String.valueOf(user.getDateOfBirth(user.getDateOfBirth()).getYear()));
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.password"), user.getLoginCredentials().getPassword());
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.verify_password"), user.getLoginCredentials().getPassword());
            DropDowns.selectByText(Elements.element("usl_enrollment.security_question"), user.getUserPasswordHint().getQuestion());
            TextBoxes.typeTextbox(Elements.element("usl_enrollment.security_answer"), user.getUserPasswordHint().getAnswer());
        }

        TextBoxes.typeTextbox(Elements.element("usl_enrollment.phone_number"), profileAddress.getBestPhone());
        Clicks.click(Elements.element("usl_enrollment.continue_button"));
    }

    /**
     * Method to enroll into USL from Plenti Site - Step 1
     */
    public static void enrollStep1(UserProfile customer) {
//        Wait.secondsUntilElementNotPresent(Elements.element("usl_enrollment.site_leaving_popup"), 30);
        acceptPlentiAuthenticationPopup();
        Wait.forPageReady();
        Wait.secondsUntilElementPresent(Elements.element("usl_enroll_step1.first_name"), 30);

        selectRandomSalutation();
        String password = "Password123";
        TextBoxes.typeTextbox(Elements.element("usl_enroll_step1.password"), password);
        TextBoxes.typeTextbox(Elements.element("usl_enroll_step1.verify_password"), password);
        String pin = "1234";
        TextBoxes.typeTextbox(Elements.element("usl_enroll_step1.pin"), pin);
        TextBoxes.typeTextbox(Elements.element("usl_enroll_step1.verify_pin"), pin);

        if (firefox())
            Clicks.javascriptClick(Elements.element("usl_enroll_step1.next_button"));
        else
            Clicks.click(Elements.element("usl_enroll_step1.next_button"));
    }

    /**
     * Method to enroll into USL from Plenti Site - Step 2
     */
    public static void completeEnrollment() {
        Wait.forPageReady();
        Wait.secondsUntilElementPresent(Elements.element("usl_enroll_step2.marketing_checkbox"), 2);
        Elements.findElements("usl_enroll_step2.marketing_checkbox").stream().limit(2).forEach(Clicks::click);

        if (firefox())
            Clicks.javascriptClick("usl_enroll_step2.next_button");
        else
            Clicks.click("usl_enroll_step2.next_button");
    }

    /**
     * Method to link credit card and set preferences for USL after completing the USL enrollment
     */
    public static void linkCreditCardAndSetPreferences() {
        Wait.secondsUntilElementPresent(Elements.element("usl_enroll_confirmation.enrollment_success_container"), 3);
        Wait.secondsUntilElementNotPresent(Elements.element("usl_enroll_confirmation.redirect_final_countdown"), 30);
        Wait.forPageReady();
        Wait.secondsUntilElementPresent(Elements.element("usl_set_preferences.continue_button"), 50);

        Clicks.javascriptClick(Elements.element("usl_set_preferences.maybe_later"));
        Clicks.click(Elements.element("usl_set_preferences.continue_button"));
    }

    /**
     * Private method for selecting random salutation
     */
    private static void selectRandomSalutation() {
        List<WebElement> options = Elements.findElement(Elements.element("usl_enroll_step1.salutation_list")).findElements(By.tagName("option"));
        int index = new Random().nextInt(options.size());
        if (safari())
            Elements.findElement("usl_enroll_step1.salutation").click();
        else
            Clicks.click(Elements.element("usl_enroll_step1.salutation"));
        Clicks.click(By.xpath("//li[@data-option-array-index='" + index + "']"));
    }

    /**
     * Private method for entering credentials in USL authentication popup
     */
    private static void acceptPlentiAuthenticationPopup() {
        try {
            Robot robot = new Robot();
            // Wait for authentication popup to display
            Thread.sleep(10000);
            robot.keyPress(KeyEvent.VK_O);
            robot.keyRelease(KeyEvent.VK_O);
            robot.keyPress(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_N);
            robot.keyPress(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_L);
            robot.keyPress(KeyEvent.VK_I);
            robot.keyRelease(KeyEvent.VK_I);
            robot.keyPress(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_N);
            robot.keyPress(KeyEvent.VK_E);
            robot.keyRelease(KeyEvent.VK_E);
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_MINUS);
            robot.keyRelease(KeyEvent.VK_MINUS);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_P);
            robot.keyRelease(KeyEvent.VK_P);
            robot.keyPress(KeyEvent.VK_P);
            robot.keyRelease(KeyEvent.VK_P);
            robot.keyPress(KeyEvent.VK_U);
            robot.keyRelease(KeyEvent.VK_U);

            robot.keyPress(KeyEvent.VK_TAB);
            Thread.sleep(100);
            robot.keyRelease(KeyEvent.VK_TAB);
            // Enter password
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_O);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyRelease(KeyEvent.VK_O);
            robot.keyPress(KeyEvent.VK_N);
            robot.keyRelease(KeyEvent.VK_N);
            robot.keyPress(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_L);
            robot.keyPress(KeyEvent.VK_2);
            robot.keyRelease(KeyEvent.VK_2);
            robot.keyPress(KeyEvent.VK_0);
            robot.keyRelease(KeyEvent.VK_0);
            robot.keyPress(KeyEvent.VK_1);
            robot.keyRelease(KeyEvent.VK_1);
            robot.keyPress(KeyEvent.VK_4);
            robot.keyRelease(KeyEvent.VK_4);
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_SLASH);
            robot.keyRelease(KeyEvent.VK_SLASH);
            robot.keyRelease(KeyEvent.VK_SLASH);
            robot.keyPress(KeyEvent.VK_P);
            robot.keyRelease(KeyEvent.VK_P);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            // Wait for authentication popup to close and redirect to plenti site
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}