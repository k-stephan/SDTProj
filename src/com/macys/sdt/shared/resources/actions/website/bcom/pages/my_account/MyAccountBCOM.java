package com.macys.sdt.shared.resources.actions.website.bcom.pages.my_account;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;

public class MyAccountBCOM extends StepUtils {

    public void navigateToLeftNavigationPage(String pageName) {
        String left_navigation_link = "navigation";
        switch (pageName.toLowerCase()) {
            case "my account":
                left_navigation_link += ".goto_my_account";
                break;
            case "my profile":
                left_navigation_link += ".goto_my_profile";
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
            case "member benefits":
                left_navigation_link += ".goto_member_benefits";
                break;
        }
        Wait.untilElementPresent(left_navigation_link);
        Clicks.click(left_navigation_link);
    }

}