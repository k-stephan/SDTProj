package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.MyOffers;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.MyWallet;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

import java.text.SimpleDateFormat;
import java.util.*;

public class MyOffersSteps extends StepUtils {

    boolean walletEligibleOffers = Elements.elementPresent("my_offers.add_to_wallet");
    List<Map<String, Object>> offers = new ArrayList<>();

    /**
     * Adds all available offers to wallet from promotions page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I add all wallet eligible offers from deals and promotions page$")
    public void i_add_all_wallet_eligible_offers_from_deals_and_promotions_page() throws Throwable {
        if (walletEligibleOffers) {
            List<Map<String, Object>> offersList = MyOffers.offerList();

            for (Map<String, Object> ol : offersList) {
                Map<String, Object> offer = new HashMap<>();

                if (ol.get("isOfferAvailableToAddToWallet").equals(true)) {
                    offer.putAll(ol);
                    offers.add(offer);
                    break;
                }
            }
            MyOffers.addOfferToWallet(offers.get(0));
        } else {
            Assert.fail("ERROR - DATA : Eligible Wallet Offers not present in Deals and Promotion Page!!");
        }
    }

    /**
     * Verifies that the offers that were added show up in my wallet
     * <p>
     * Uses step "I add all wallet eligible offers from deals and promotions page" as basis for verification.
     * Requires that either this step be run, or another step updates the "offers" variable with
     * correct information
     * </p>
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see the added offers in my wallet page$")
    public void i_should_see_the_added_offers_in_my_wallet_page() throws Throwable {
        Clicks.click("my_offers.goto_my_wallet");
        // shouldBeOnPage("oc_my_wallet") ;
        Wait.secondsUntilElementPresent("oc_my_wallet.used_expired_offers_message", 30);

        if (walletEligibleOffers) {
            List<Map<String, Object>> walletOffers = MyWallet.offersList();

            walletOffers.sort(Comparator.comparing((map) -> map.get("specialRedemtionCode").toString()));
            offers.sort(Comparator.comparing((map) -> map.get("offerPromoCode").toString()));
            for (Map<String, Object> w : offers) {
                boolean found = false;
                for (Map<String, Object> o : walletOffers) {
                    if (o.get("offerName").toString().contains(w.get("offerTitle").toString()) || o.get("offerName").toString().contains(w.get("offerSubHeaderPromo").toString())) {
                        if (!o.get("specialRedemptionCode").toString().contains(w.get("offerPromoCode").toString())) {
                            Assert.fail("Offer code does not match");
                        }
                        if ((w.get("offerEndDate") != null)) {
                            if (!w.get("offerEndDate").toString().isEmpty()) {
                                String walletOfferEndDate = o.get("offerDate").toString().split("- ")[1];
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                                Calendar c = Calendar.getInstance();
                                c.setTime(new Date());
                                c.add(Calendar.DATE, 730);
                                String dateMoreThanTwoYears = sdf.format(c.getTime());
                                String addedOfferEndDate = sdf.format(sdf.parse(w.get("offerEndDate").toString()));
                                if ((sdf.parse(walletOfferEndDate).compareTo(sdf.parse(dateMoreThanTwoYears)) > 0) && !(walletOfferEndDate.equals(addedOfferEndDate))) {
                                    Assert.assertFalse("End dates does not match!!", ((sdf.parse(walletOfferEndDate).compareTo(sdf.parse(dateMoreThanTwoYears)) > 0) && !(walletOfferEndDate.equals(addedOfferEndDate))));
                                }
                            }
                        }
                        found = true;
                        break;
                    }

                }
                if (!found) {
                    Assert.fail("Offer not found in Wallet");
                }
            }
            System.out.println("Added offers from Deals & Promotions page are displaying on Wallet page");
        } else {
            String expectedMessage = "Add savings passes, deals, promos & more to save online & in-store!";
            if (!Elements.getText("oc_my_wallet.no_offer_message").equals(expectedMessage)) {
                Assert.fail(expectedMessage + "is not displayed on My Wallet page");
            }
            System.out.println("There are no wallet eligible offers on deals and promotions page to add to wallet");
        }
    }

}