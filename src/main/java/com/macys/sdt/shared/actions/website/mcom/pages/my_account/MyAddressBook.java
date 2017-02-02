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

    /**
     * Updates the address which is at the given index
     *
     * @param index index of address
     * @param opts custom address options
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Adds address with the specified condition like "checkout_eligible is true"
     *
     * @param opts address options
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Fills the given ProfileAddress on the given page
     *
     * @param pageName page on which address has to be filled
     * @param addressObject Address to fill
     * @throws Throwable if any exception occurs
     */
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

    /**
     * Verifies whether address is present or not
     *
     * @return true if at least one address is present else false
     */
    public static boolean isAddressAdded() {
        return Elements.findElements("my_address_book.addresses").size() > 0;
    }
}
