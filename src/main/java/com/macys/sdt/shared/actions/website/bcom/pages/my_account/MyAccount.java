package com.macys.sdt.shared.actions.website.bcom.pages.my_account;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;

public class MyAccount extends StepUtils {

    /**
     * Navigates to given page from my account pages using left navigation
     * <p>Page Options: <br>
     * "my account", "my profile", "my address book", "my wallet", "wish list", "order status" or "member benefits"
     * </p>
     *
     * @param pageName target page name
     */
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


   /* public boolean navigatedToExpectedPage(String pageName) {
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
            String verifyElement = getElementValue(verifyElementKey);
            if (verifyElement != null)
                secondsUntilElementPresent(navigated_page_name + ".verify_page", 20);
            else
                Utils.threadSleep(5000, "Navigated page is not loaded properly!!");
        }
        return onPage(navigated_page_name.toLowerCase());
    }

    public void setSecurityQuestion(){
        forPageReady();
        untilElementPresent("sign_in.security_question");
        if (elementPresent("sign_in.security_question")) {
            UserProfile customer = TestUsers.getCustomer(null);
            selectByText("sign_in.security_question", customer.getUser().getUserPasswordHint().getQuestion());
            typeTextbox("sign_in.security_answer", customer.getUser().getUserPasswordHint().getAnswer());
            click("sign_in.save_and_continue");
            forPageReady();
            System.out.println("Set security question to user!!");
        }
    }*/
}