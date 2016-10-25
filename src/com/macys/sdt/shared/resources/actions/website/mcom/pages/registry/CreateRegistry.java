package com.macys.sdt.shared.resources.actions.website.mcom.pages.registry;


import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.resources.model.ProfileAddress;
import com.macys.sdt.framework.resources.model.Registry;
import com.macys.sdt.framework.resources.model.User;
import com.macys.sdt.framework.resources.model.UserProfile;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.db.models.RegistryService;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class CreateRegistry extends StepUtils {

    public static void fillRegistryUserDetails(UserProfile customer) {
        if (onPage("new_create_registry")) {
            createRegistryUser(customer);

        } else {
            closeFSRDialogIfAppeared();
            int date = TestUsers.generateRandomDateIndex();
            int year = TestUsers.generateRandomYearIndex();

            Wait.forPageReady();
            Registry registry = customer.getRegistry();
            User user = customer.getUser();
            ProfileAddress profileAddress = user.getProfileAddress();
            DropDowns.selectByText("create_registry.event_type", registry.getEventType());
            DropDowns.selectByText("create_registry.event_month", registry.getEventMonth());
            DropDowns.selectByText("create_registry.event_day", registry.getEventDay());
            DropDowns.selectByText("create_registry.event_year", registry.getEventYear());

            if (macys()) {
                DropDowns.selectByText("create_registry.event_location", registry.getEventLocation());
                DropDowns.selectByText("create_registry.preferred_store_state", registry.getPreferredStoreState());
            }
            TextBoxes.typeTextbox("create_registry.number_of_guests", registry.getNumberOfGuest());
            TextBoxes.typeTextbox("create_registry.first_name", profileAddress.getFirstName());
            TextBoxes.typeTextbox("create_registry.last_name", profileAddress.getLastName());

            TextBoxes.typeTextbox("create_registry.address_line_1", profileAddress.getAddressLine1());
            TextBoxes.typeTextbox("create_registry.address_line_2", "");
            TextBoxes.typeTextbox("create_registry.address_city", profileAddress.getCity());
            DropDowns.selectByText("create_registry.address_state", profileAddress.getState());
            TextBoxes.typeTextbox("create_registry.address_zip_code", String.valueOf(profileAddress.getZipCode()));

            DropDowns.selectByText("create_registry.security_question", user.getUserPasswordHint().getQuestion());
            TextBoxes.typeTextbox("create_registry.security_answer", user.getUserPasswordHint().getAnswer());
            DropDowns.selectByText("create_registry.dob_month", TestUsers.generateRandomMonth());
            DropDowns.selectByIndex("create_registry.dob_day", date);
            DropDowns.selectByValue("create_registry.dob_year", Integer.toString(year));

            TextBoxes.typeTextbox("create_registry.phone_area_code", profileAddress.getPhoneAreaCode());
            TextBoxes.typeTextbox("create_registry.phone_exchange", profileAddress.getPhoneExchange());
            TextBoxes.typeTextbox("create_registry.phone_subscriber", profileAddress.getPhoneSubscriber());

            TextBoxes.typeTextbox("create_registry.co_registrant_first_name", registry.getCoRegistrantFirstName());
            TextBoxes.typeTextbox("create_registry.co_registrant_last_name", registry.getCoRegistrantLastName());
            DropDowns.selectByIndex("create_registry.preferred_store", 1);

            Clicks.javascriptClick("create_registry.create_registry_button");
            try {
                Wait.secondsUntilElementPresent("create_registry.close_overlay_chat", 30);
                Clicks.click("create_registry.close_overlay_chat");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void fillRegistryUserDetailsForExistingUser(UserProfile customer) {
        closeFSRDialogIfAppeared();

        Wait.forPageReady();
        Registry registry = customer.getRegistry();
        ProfileAddress profileAddress = customer.getUser().getProfileAddress();
        DropDowns.selectByText("create_registry.event_type", registry.getEventType());
        DropDowns.selectByText("create_registry.event_month", registry.getEventMonth());
        DropDowns.selectByText("create_registry.event_day", registry.getEventDay());
        DropDowns.selectByText("create_registry.event_year", registry.getEventYear());

        if (macys()) {
            DropDowns.selectByText(Elements.element("create_registry.event_location"), registry.getEventLocation());
            DropDowns.selectByText(Elements.element("create_registry.preferred_store_state"), registry.getPreferredStoreState());
        }
        TextBoxes.typeTextbox("create_registry.number_of_guests", registry.getNumberOfGuest());
        TextBoxes.typeTextbox("create_registry.co_registrant_first_name", registry.getCoRegistrantFirstName());
        TextBoxes.typeTextbox("create_registry.co_registrant_last_name", registry.getCoRegistrantLastName());
        TextBoxes.typeTextbox("create_registry.phone_area_code", profileAddress.getPhoneAreaCode());
        TextBoxes.typeTextbox("create_registry.phone_exchange", profileAddress.getPhoneExchange());
        TextBoxes.typeTextbox("create_registry.phone_subscriber", profileAddress.getPhoneSubscriber());

        DropDowns.selectByIndex("create_registry.preferred_store", 1);

        Clicks.click("create_registry.create_registry_button");
        try {
            Wait.secondsUntilElementPresent("create_registry.close_overlay_chat", 5);
            Clicks.click(Elements.element("create_registry.close_overlay_chat"));
        } catch (Exception e) {
            //error just means there was no chat popup
        }
    }

    public static void closeFSRDialogIfAppeared() {
        try {
            Clicks.click(Elements.findElement(By.className("fsrCloseBtn")));
        } catch (Exception e) {
            //ignore errors
        }
    }

    public static void verifyRegistryIsCreatedInDB(String registryNo) {
        if (!RegistryService.registryExists(registryNo))
            Assert.fail("Could not find registry in DB");
    }

    // Below method is used to fill registry user details on new create registry page: wgl/registry/create-registry

    public static void createRegistryUser(UserProfile customer) {
        closeFSRDialogIfAppeared();
        fillProfileInfo(customer);
        fillRegistryPersonalInfo(customer, true);
        fillRegistryContactInfo(customer, true);
        fillRegistryShippingInfo();
        fillRegistryStoreInfo(customer);
        Clicks.click("new_create_registry.create_registry_button");
        Wait.forPageReady();
        if(Elements.elementPresent("new_create_registry.error_message") && Elements.getText("new_create_registry.error_message").contains("technical error"))
            Assert.fail("ERROR - ENV: Registry Services are down in environment!! --"+ Elements.getText("new_create_registry.error_message"));
    }

    public static void createRegistryUserForExistingUser(UserProfile customer) {
        closeFSRDialogIfAppeared();
        fillRegistryAccountInfo(customer, false);
        fillRegistryPersonalInfo(customer, false);
        fillRegistryContactInfo(customer, false);
        fillRegistryShippingInfo();
        fillRegistryStoreInfo(customer);
        Clicks.click(Elements.element("new_create_registry.create_registry_button"));
    }

    private static void fillRegistryAccountInfo(UserProfile customer, boolean isNewUser) {
        TextBoxes.typeTextbox("new_create_registry.security_answer", customer.getUser().getUserPasswordHint().getAnswer());
        Clicks.javascriptClick("new_create_registry.continue_button");
    }

    private static void fillProfileInfo(UserProfile customer) {
        TextBoxes.typeTextbox("new_create_registry.new_user_email", customer.getUser().getProfileAddress().getEmail());
        TextBoxes.typeTextbox("new_create_registry.new_user_password", customer.getUser().getLoginCredentials().getPassword());
        TextBoxes.typeTextbox("new_create_registry.new_user_password_verify", customer.getUser().getLoginCredentials().getPassword());
        DropDowns.selectByText("new_create_registry.security_question", customer.getUser().getUserPasswordHint().getQuestion());
        TextBoxes.typeTextbox("new_create_registry.security_answer", customer.getUser().getUserPasswordHint().getAnswer());
        Clicks.click("new_create_registry.continue_button");
    }

    private static void fillRegistryPersonalInfo(UserProfile customer, boolean isNewUser) {
        try {
            User user = customer.getUser();
            ProfileAddress profileAddress = user.getProfileAddress();
            int dobDate = TestUsers.generateRandomDateIndex();
            int dobYear = new Random().nextInt(52) + 1950;
            Calendar eventDateInstance = Calendar.getInstance();
            Calendar dobInstance = Calendar.getInstance();
            DecimalFormat decimalFormat = new DecimalFormat("00");
            Registry registry = customer.getRegistry();
            eventDateInstance.setTime(new SimpleDateFormat("dd.MMMMM.yyyy").parse(registry.getEventDay() + "." + registry.getEventMonth() + "." + registry.getEventYear()));
            dobInstance.setTime(new SimpleDateFormat("dd.MMMMM.yyyy").parse(String.valueOf(dobDate) + "." + WordUtils.capitalize(user.getDateOfBirth(user.getDateOfBirth()).getMonth().name().toLowerCase()) + "." + String.valueOf(dobYear)));
            String fullDob = decimalFormat.format(dobInstance.get(Calendar.MONTH) + 1) + decimalFormat.format(dobInstance.get(Calendar.DAY_OF_MONTH)) + dobInstance.get(Calendar.YEAR);
            String fullEventDate = decimalFormat.format(eventDateInstance.get(Calendar.MONTH) + 1) + decimalFormat.format(eventDateInstance.get(Calendar.DAY_OF_MONTH)) + eventDateInstance.get(Calendar.YEAR);
            if (isNewUser) {
                TextBoxes.typeTextbox("new_create_registry.first_name", profileAddress.getFirstName());
                TextBoxes.typeTextbox("new_create_registry.last_name", profileAddress.getLastName());
                TextBoxes.typeTextbox("new_create_registry.date_of_birth", fullDob);
            }
            TextBoxes.typeTextbox("new_create_registry.co_registrant_first_name", registry.getCoRegistrantFirstName());
            TextBoxes.typeTextbox("new_create_registry.co_registrant_last_name", registry.getCoRegistrantLastName());
            TextBoxes.typeTextbox("new_create_registry.event_date", fullEventDate);
            TextBoxes.typeTextbox("new_create_registry.number_of_guests", registry.getNumberOfGuest());
            Clicks.selectCheckbox("new_create_registry.public_profile");
            Clicks.click("new_create_registry.personal_info_continue_button");
        } catch (ParseException e) {
            Assert.fail("Unable to fill registry personal info: " + e);
        }
    }

    private static void fillRegistryContactInfo(UserProfile customer, boolean isNewUser) {
        ProfileAddress profileAddress = customer.getUser().getProfileAddress();
        TextBoxes.typeTextbox("new_create_registry.address_line_1", profileAddress.getAddressLine1());
        TextBoxes.typeTextbox("new_create_registry.address_line_2", profileAddress.getAddressLine2());
        TextBoxes.typeTextbox("new_create_registry.address_city", profileAddress.getCity());
        DropDowns.selectByValue("new_create_registry.address_state", (profileAddress.getState().length() == 2 ? profileAddress.getState() : StatesUtils.getAbbreviation(profileAddress.getState())));
        TextBoxes.typeTextbox("new_create_registry.address_zip_code", String.valueOf(profileAddress.getZipCode()));
        TextBoxes.typeTextbox("new_create_registry.phone", profileAddress.getBestPhone());
        Clicks.click("new_create_registry.contact_info_continue_button");
    }

    private static void fillRegistryShippingInfo() {
        Clicks.selectCheckbox("new_create_registry.go_green_checkbox");
        Clicks.click("new_create_registry.shipping_info_continue_button");
    }

    private static void fillRegistryStoreInfo(UserProfile customer) {
        DropDowns.selectByText("new_create_registry.preferred_store_state", customer.getRegistry().getPreferredStoreState());
        DropDowns.selectByIndex("new_create_registry.preferred_store", 1);
    }
}
