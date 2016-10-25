package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.db.models.PromotionService;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.junit.Assert;

import java.util.NoSuchElementException;

public class MyWalletSteps extends StepUtils {

    String promoCode = null;

    @Then("^I saved omnichannel offer having more than one promo code in wallet$")
    public void I_saved_omnichannel_offer_having_more_than_one_promo_code_in_wallet() throws Throwable {
        PromotionService promotionService = new PromotionService();
        promoCode = promotionService.getWalletEligiblePromoCode();
        if (promoCode == null)
            Assert.fail("ERROR - DATA: Wallet eligible promo code is not available in database!!");
        Clicks.click((macys() ? "oc_my_wallet" : "my_bwallet")+".add_offer_btn");
        TextBoxes.typeTextbox("add_offer_dialog.promo_code", promoCode);
        Clicks.clickIfPresent("add_offer_dialog.save_offer");
    }

    @And("^I verify the promo code added to my wallet page$")
    public void I_verify_the_promo_code_added_to_my_wallet_page() throws Throwable {
        try {
            if (macys()) {
                Elements.elementPresent("oc_my_wallet.offers_container");
            } else {
                Elements.elementPresent("my_bwallet.offers_container");
            }
        } catch (NoSuchElementException e) {
            Assert.fail("Promo code is not added to wallet page");
        }
    }
}
