package com.macys.sdt.shared.resources.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.resources.model.CreditCard;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.resources.actions.website.mcom.panels.my_account.CreditCardDialog;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWallet extends StepUtils {
    public static void addCard() {
        CreditCard visaCreditCard = TestUsers.getValidVisaCreditCard();
        addCard(visaCreditCard);
    }

    public static void  addCard(CreditCard creditCard) {
        String pageName = macys() ? "oc_my_wallet" : "my_bwallet";
        try {
            Wait.untilElementPresent(pageName + (macys() ? ".add_credit_card" : ".add_credit_card_btn"));
            if (!Elements.elementPresent(pageName + ".credit_card_type")) {
                Clicks.click(pageName + (macys() ? ".add_credit_card" : ".add_credit_card_btn"));
            }
            Wait.untilElementPresent("oc_my_wallet.credit_card_overlay");
            CreditCardDialog.addCreditCard(creditCard);
        } catch (Exception e) {
            Assert.fail("Unable to add the card successfully to the wallet " + e);
        }
    }

    /**
     * Method to return list of offers
     *
     * @return offers list
     */
    public static List<Map<String, Object>> offersList() {
        List<Map<String, Object>> offersList = new ArrayList<>();
        if (macys()) {
            for (int i = 0; i < offerElements().size(); i++) {
                Map<String, Object> offer = new HashMap<>();
                offer.put("offerName", Elements.findElements(Elements.element("oc_my_wallet.offer_names")).get(i).getText());
                offer.put("specialRedemptionCode", Elements.findElements(Elements.element("oc_my_wallet.special_redemption_codes")).get(i).getText());
                offer.put("offerDate", Elements.findElements(Elements.element("oc_my_wallet.offer_dates")).get(i).getText());
                offer.put("isDetailsExclusionsAvailable", Elements.findElements(Elements.element("oc_my_wallet.view_details_and_exclusions")).get(i).isDisplayed());
                offer.put("isRemoveOfferAvailable", Elements.findElements(Elements.element("oc_my_wallet.delete_offers")).get(i).isDisplayed());
                offersList.add(offer);
            }
        } else {
            for (int i = 0; i < offerElements().size(); i++) {
                Map<String, Object> offer = new HashMap<>();
                if (offerElements().size() >= 1 && !offerElements().get(0).getText().contains("There are no valid offers available in your bWallet")) {
                    if (!offerElements().get(i).findElement(By.cssSelector(".wlt_offerOnlineCode")).isDisplayed())
                        Elements.findElements(Elements.element("my_bwallet.view_details_and_exclusions")).get(i).click();
                    offer.put("offerName", Elements.findElements(Elements.element("my_bwallet.offer_names")).get(i).getText());
                    offer.put("offerType", Elements.findElements(Elements.element("my_bwallet.offer_types")).get(i).getText());
                    offer.put("isDetailsExclusionsAvailable", Elements.findElements(Elements.element("my_bwallet.view_details_and_exclusions")).get(i).isDisplayed());
                    offer.put("onlineCode", offerElements().get(i).findElement(By.cssSelector(".wlt_offerOnlineCode")).getText());

                    String instoreCode = null;
                    if (offerElements().get(i).findElements(By.cssSelector(".wlt_offerInStoreCode")).size() == 1)
                        instoreCode = offerElements().get(i).findElement(By.cssSelector(".wlt_offerInStoreCode")).getText();
                    offer.put("instoreCode", instoreCode);

                    String legalDisclaimer = null;
                    if (offerElements().get(i).findElements(By.cssSelector(".wlt_offerLegalDisclaimer")).size() == 1)
                        legalDisclaimer = offerElements().get(i).findElement(By.cssSelector(".wlt_offerLegalDisclaimer")).getText();
                    offer.put("legalDisclaimer", legalDisclaimer);

                    offersList.add(offer);
                }
            }
        }
        return offersList;
    }

    /**
     * Private method to return the list of offer container elements
     *
     * @return offer container elements
     */
    private static List<WebElement> offerElements() {
        List<WebElement> offerElementsList;
        if (macys())
            offerElementsList = Elements.findElement(Elements.element("oc_my_wallet.offers_container")).findElements(By.cssSelector(".public-offer"));
        else
            offerElementsList = Elements.findElement(Elements.element("my_bwallet.offers_container")).findElements(By.cssSelector(".wlt_panelRow"));
        return offerElementsList;
    }

}