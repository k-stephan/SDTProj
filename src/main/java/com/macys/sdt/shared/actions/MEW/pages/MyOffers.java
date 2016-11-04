package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.db.models.PromotionService;
import org.json.JSONException;
import org.junit.Assert;
import org.openqa.selenium.By;


public class MyOffers extends StepUtils {

    public static String promoCode = null;

    public static void deleteOffers() {
        Navigate.browserRefresh();
        if (Wait.untilElementPresent("oc_my_wallet.available_offers")) {
            int offer_size = Elements.findElements("oc_my_wallet.available_offers").size();
            for (int i = 0; i < offer_size; i++) {
                if (Elements.findElements("oc_my_wallet.available_offers").size() == 1) {
                    Clicks.click("oc_my_wallet.available_offers");
                } else {
                    Clicks.click(Elements.findElements("oc_my_wallet.available_offers").get(i));
                }
                Wait.untilElementPresent("oc_my_wallet.container");
                Assert.assertTrue("Unable to click remove offer code button", Clicks.clickWhenPresent("oc_my_wallet.remove_offer_code_button"));
                Clicks.click("oc_my_wallet.remove_offer_confirmation_ok");
                Clicks.click("oc_my_wallet.back_btn");
            }
        } else {
            System.out.print("No Offers found");
        }
    }

    public static void addOffers() {
        try {
            if (prodEnv()) {
                promoCode = TestUsers.getValidPromotion().getString("promo_code");
            } else {
                PromotionService promotionService = new PromotionService();
                promoCode = promotionService.getWalletEligiblePromoCode();
                System.out.println("promocode : " + promoCode);
                if (promoCode == null)
                    Assert.fail("ERROR:: Test Data: Wallet eligible promo code is not available in database!!");
            }
            TextBoxes.typeTextbox("oc_my_wallet.input_offer_code", promoCode);
            Clicks.javascriptClick("oc_my_wallet.add_offer_code_button");
            By el = Elements.element("oc_my_wallet.add_offer_error_msg");
            if (Elements.elementPresent(el)) {
                if (Elements.findElement(el).getText().contains("This offer is already in your wallet")) {
                    System.out.print("Same offer is already added to you wallet. Please use it for checkout");
                    Navigate.browserBack();
                    Navigate.browserRefresh();
                    if (!Wait.untilElementPresent("oc_my_wallet.available_offers")) {
                        Assert.fail("Add offers are not displaying in the My Wallet Page");
                    }
                } else if (Elements.findElement(el).getText().contains("Sorry, but we don't recognize the promo code you entered")) {
                    Assert.fail("ERROR-DATA: Added offer is not valid. Please use a valid offer");
                }
            } else {
                Wait.untilElementPresent("oc_my_wallet.add_offer_confirmation");
                Clicks.javascriptClick("oc_my_wallet.add_offer_confirmation_ok");
                Wait.untilElementPresent("oc_my_wallet.available_offers");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail("Not returned any valid promo-codes");
        }
    }
}
