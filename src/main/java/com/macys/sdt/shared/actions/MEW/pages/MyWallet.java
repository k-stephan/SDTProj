package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;

public class MyWallet extends StepUtils {

    /**
     * Delete all credit cards from OC Wallet page
     */
    public static void deleteCreditCard() {
        if (Wait.untilElementPresent("oc_my_wallet.cc_container")) {
            int cc_size = Elements.findElements("oc_my_wallet.credit_cards").size();
            for (int i = 0; i < cc_size; i++) {
                if (Elements.findElements("oc_my_wallet.credit_cards").size() == 1) {
                    Clicks.click("oc_my_wallet.credit_cards");
                } else {
                    Clicks.click(Elements.findElements("oc_my_wallet.credit_cards").get(i));
                }
                Clicks.clickWhenPresent("oc_my_wallet.delete_card");
                Clicks.clickWhenPresent("oc_my_wallet.confirmation_yes_button");
            }
        } else {
            System.out.print("No Credit Cards founds");
        }
    }
}
