package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyOffersSteps extends StepUtils {

    List<String> promoCodes = new ArrayList<>();
    List<String> walletPromoCodes = new ArrayList<>();

    /**
     * Adds all available offers to wallet from deals and promotions page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I add all wallet eligible offers from deals and promotions page using mobile website$")
    public void i_add_all_wallet_eligible_offers_from_deals_and_promotions_page_using_mobile_website() throws Throwable {
        shouldBeOnPage("deals_and_promotions");
        pausePageHangWatchDog();
        Wait.untilElementPresent("deals_and_promotions.offers_container");
        int count = Elements.findElements("deals_and_promotions.offers_container").size();
        for (int i = 0; i < count; i++) {
            Clicks.click(Elements.findElements("deals_and_promotions.offers_container").get(i));
            Wait.untilElementPresent("offer_details.offer_promocode");
            if (Elements.elementPresent("offer_details.add_to_wallet")) {
                promoCodes.add(Elements.findElement("offer_details.offer_promocode").getText().replace("Promo code: ", ""));
                Clicks.click("offer_details.add_to_wallet");
                Wait.untilElementPresent("offer_details.toast_message");
            }
            Clicks.clickWhenPresent("offer_details.back");
            Wait.untilElementNotPresent("offer_details.back");
            Wait.untilElementPresent("deals_and_promotions.offers_container");
        }
    }

    /**
     * Verifies that the offers that were added show up in my wallet
     * <p>
     * Uses step "I add all wallet eligible offers from deals and promotions page using mobile website" as basis for verification.
     * Requires that either this step be run, or another step updates the "promoCodes" variable with correct information
     * </p>
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see the added offers in my wallet page using mobile website$")
    public void i_should_see_the_added_offers_in_my_wallet_page() throws Throwable {
        shouldBeOnPage("oc_my_wallet");
        Clicks.clickIfPresent("oc_my_wallet.view_all_saved_offers");
        List<WebElement> offers = Elements.findElements("oc_my_wallet.offers_list");
        walletPromoCodes.addAll(offers.stream().map(offer -> offer.getAttribute("data-id")).collect(Collectors.toList()));
        for (String promoCode : promoCodes) {
            Assert.assertTrue(promoCode + "is not added to wallet", walletPromoCodes.contains(promoCode));
        }
    }
}