package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.ChangePreferredStore;

public class MyAccount extends StepUtils {

    public void navigateToLeftNavigationPage(String pageName) {
        String left_navigation_link = "navigation";
        switch (pageName.toLowerCase()) {
            case "my account":
                left_navigation_link += ".goto_my_account";
                break;
            case "my profile":
                left_navigation_link += ".goto_my_profile";
                break;
            case "my preferences":
                left_navigation_link += ".goto_my_preferences_link";
                break;
            case "my address book":
                left_navigation_link += ".goto_my_address_book_link";
                break;
            case "my wallet":
                left_navigation_link += ".goto_my_wallet_link";
                break;
            case "wish list":
                left_navigation_link += ".goto_my_wish_list";
                break;
            case "order status":
                left_navigation_link += ".goto_order_status";
                break;
            case "furniture & mattress status":
                left_navigation_link += ".goto_furniture_mattress_status";
                break;
            case "gift card balance":
                left_navigation_link += ".goto_gift_card_balance";
                break;
        }
        Wait.untilElementPresent(left_navigation_link);
        Clicks.click(left_navigation_link);
    }


    public boolean navigatedToExpectedPage(String pageName) {
        String navigated_page_name;
        switch (pageName.toLowerCase()) {
            case "my wallet":
                navigated_page_name = macys() ? "oc_my_wallet" : "my_bwallet";
                break;
            case "furniture & mattress status":
                navigated_page_name = "furniture_mattress_status_page";
                break;
            default:
                navigated_page_name = pageName.replaceAll(" ", "_");
        }
        if (safari()) {
            String verifyElementKey = navigated_page_name + ".verify_page";
            if (!Elements.getValues(verifyElementKey).isEmpty())
                Wait.secondsUntilElementPresent(navigated_page_name + ".verify_page", 20);
            else
                Utils.threadSleep(5000, "Navigated page is not loaded properly!!");
        }
        return onPage(navigated_page_name.toLowerCase());
    }

    public void setSecurityQuestion(){
        Wait.forPageReady();
        Wait.untilElementPresent("sign_in.security_question");
        if (Elements.elementPresent("sign_in.security_question")) {
            UserProfile customer = TestUsers.getCustomer(null);
            DropDowns.selectByText("sign_in.security_question", customer.getUser().getUserPasswordHint().getQuestion());
            TextBoxes.typeTextbox("sign_in.security_answer", customer.getUser().getUserPasswordHint().getAnswer());
            Clicks.click("sign_in.save_and_continue");
            Wait.forPageReady();
            System.out.println("Set security question to user!!");
        }
    }

    public static void setPreferredStore(String zipcode) {
        Wait.untilElementPresent("my_account.change_locations");
        Clicks.click(Elements.element("my_account.change_locations"));
        ChangePreferredStore.setPreferredStore(zipcode);
    }
}