package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.utils.AbbreviationHelper;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

public class MyAddressBook extends StepUtils {

    public void updateAddress(int index, HashMap<String, String> opts) throws Throwable {
        ProfileAddress addressObject = new ProfileAddress();
        TestUsers.getRandomValidAddress(opts, addressObject);
        String pageName = "my_address_book";
        List<WebElement> allAddress = Elements.findElements(pageName + ".addresses");
        Clicks.click(allAddress.get(index).findElement((macys() ? (Elements.elementPresent(By.linkText("edit")) ? By.linkText("edit") : By.linkText("Edit")) : By.className("account_addressLink"))));
        addressBookCommonFields(pageName, addressObject);
        Wait.forPageReady();
        System.out.println("->editAddress(): Address updated!!");
    }

    public void addAddress(HashMap<String, String> opts) throws Throwable {
        ProfileAddress addressObject = new ProfileAddress();
        TestUsers.getRandomValidAddress(opts, addressObject);
        String pageName = "my_address_book";
        Wait.forPageReady();
        TextBoxes.typeTextbox(pageName + ".first_name", addressObject.getFirstName());
        TextBoxes.typeTextbox(pageName + ".last_name", addressObject.getLastName());
        addressBookCommonFields(pageName, addressObject);
        Wait.forPageReady();
        System.out.println("->addAddress(): Address Added!!");
    }

    public void addressBookCommonFields(String pageName, ProfileAddress addressObject) throws Throwable {
        TextBoxes.typeTextbox(pageName + ".address_line_1", addressObject.getAddressLine1());
        TextBoxes.typeTextbox(pageName + ".address_line_2", addressObject.getAddressLine2());
        TextBoxes.typeTextbox(pageName + ".address_city", addressObject.getCity());
        String addressState = (addressObject.getState().length() == 2 ? AbbreviationHelper.translateStateAbbreviation(addressObject.getState()) : addressObject.getState());
        if (macys()) {
            DropDowns.selectByText(pageName + ".address_state", addressState);
        } else {
            DropDowns.selectCustomText(pageName + ".address_state_list", pageName + ".state_options", addressState);
        }
        TextBoxes.typeTextbox(pageName + ".address_zip_code", addressObject.getZipCode().toString());
        TextBoxes.typeTextbox(pageName + ".phone_area_code", "123");
        TextBoxes.typeTextbox(pageName + ".phone_exchange", "456");
        TextBoxes.typeTextbox(pageName + ".phone_subscriber", "4565");
        Clicks.click(pageName + ".add_address_button");
    }

    public static boolean isAddressAdded() {
        return Elements.findElements("my_address_book.addresses").size() > 0;
    }
}
