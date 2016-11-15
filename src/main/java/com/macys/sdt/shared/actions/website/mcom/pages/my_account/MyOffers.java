package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOffers extends StepUtils {

    /**
     * Method to return list of offers
     *
     * @return offers list
     */
    public static List<Map<String, Object>> offerList() {

        List<Map<String, Object>> offersList = new ArrayList<>();
        pausePageHangWatchDog();
        if (macys()) {
            for (WebElement el : offerElements()) {
                Map<String, Object> offer = new HashMap<>();

                String offerTitle;
                if (el.findElements(By.cssSelector(".title_2")).size() == 1)
                    offerTitle = el.findElement(By.cssSelector(".title_2")).getText();
                else
                    offerTitle = el.findElement(By.cssSelector(".offerHeaderPromo")).getText();
                String offerSubHeaderPromo = null;
                if(el.findElements(By.className("offerSubHeaderPromo")).size() > 0)
                    offerSubHeaderPromo = el.findElement(By.className("offerSubHeaderPromo")).getText();
                String offerPromoCode = null;
                if (el.findElements(By.cssSelector(".ftr_txt2>b")).size() == 1)
                    offerPromoCode = el.findElement(By.cssSelector(".ftr_txt2>b")).getText();

                String offerEndDate = null;
                if (el.findElements(By.cssSelector(".ftr_txt1>b")).size() == 1)
                    offerEndDate = el.findElement(By.cssSelector(".ftr_txt1>b")).getText();

                boolean isDetailsExclusionsAvailable = el.findElement(By.cssSelector(".ftr_txt3.detailsButton")).isDisplayed();

                boolean isOfferAvailableToAddToWallet = false;
                if (el.findElements(By.cssSelector(".add-to-wallet")).size() == 1)
                    isOfferAvailableToAddToWallet = true;

                boolean addToWalletDisabled = false;
                if (isOfferAvailableToAddToWallet && !el.findElement(By.cssSelector(".add-to-wallet")).isEnabled())
                    addToWalletDisabled = true;

                WebElement offerShareButtonEle = el.findElement(By.cssSelector(".share"));
                boolean isOfferPromotion = Boolean.parseBoolean(offerShareButtonEle.getAttribute("data-has-promotion"));
                boolean isOfferCoupon = Boolean.parseBoolean(offerShareButtonEle.getAttribute("data-has-coupon"));

                String offerCouponURL = null;
                if (isOfferCoupon)
                    offerCouponURL = offerShareButtonEle.getAttribute("data-coupon-url");

                String offerPromoURL = null;
                if (isOfferPromotion)
                    offerPromoURL = offerShareButtonEle.getAttribute("data-shop-url");

                boolean isShopNowAvailable = false;
                if (el.findElements(By.cssSelector(".shop-now")).size() == 1)
                    isShopNowAvailable = true;

                offer.put("offerTitle", offerTitle);
                offer.put("offerSubHeaderPromo", offerSubHeaderPromo);
                offer.put("offerPromoCode", offerPromoCode);
                offer.put("offerEndDate", offerEndDate);
                offer.put("isDetailsExclusionsAvailable", isDetailsExclusionsAvailable);
                offer.put("isOfferAvailableToAddToWallet", isOfferAvailableToAddToWallet);
                offer.put("isOfferPromotion", isOfferPromotion);
                offer.put("isOfferCoupon", isOfferCoupon);
                offer.put("offerCouponURL", offerCouponURL);
                offer.put("offerPromoURL", offerPromoURL);
                offer.put("isAddToWalletButtonDisabled", addToWalletDisabled);
                offer.put("isShopNowAvailable", isShopNowAvailable);
                offersList.add(offer);
            }
        } else {
            for (WebElement el : offerElements()) {
                Map<String, Object> offer = new HashMap<>();

                String offerTitle = el.findElement(By.cssSelector(".ofr_ttl")).getText();

                String offerEndDate = el.findElement(By.cssSelector(".ofr_expDate")).getText();
                if (offerEndDate.equals(""))
                    offerEndDate = null;
                else
                    offerEndDate = offerEndDate.split(" ")[2];

                String offerDescription = el.findElement(By.cssSelector(".ofr_description")).getText();

                boolean isOfferPromotion = false;
                if (el.findElements(By.cssSelector(".ofr_shopLink")).size() == 1)
                    isOfferPromotion = true;

                boolean isOfferCoupon = false;
                if (el.findElements(By.cssSelector(".ofr_couponLink")).size() == 1)
                    isOfferCoupon = true;

                String offerCouponURL = null;
                if (isOfferCoupon)
                    offerCouponURL = el.findElement(By.cssSelector(".ofr_couponLink")).getAttribute("href");

                String offerPromoURL = null;
                if (isOfferPromotion)
                    offerPromoURL = el.findElement(By.cssSelector(".ofr_shopLink")).getAttribute("href");

                boolean isInfoExclusions = false;
                if (el.findElements(By.cssSelector(".ofr_dtlLink")).size() == 1)
                    isInfoExclusions = true;

                offer.put("offerTitle", offerTitle);
                offer.put("offerEndDate", offerEndDate);
                offer.put("offerDescription", offerDescription);
                offer.put("isOfferPromotion", isOfferPromotion);
                offer.put("isOfferCoupon", isOfferCoupon);
                offer.put("offerCouponURL", offerCouponURL);
                offer.put("offerPromoURL", offerPromoURL);
                offer.put("isInfoExclusionsAvailable", isInfoExclusions);
                offersList.add(offer);
            }
        }
        resumePageHangWatchDog();
        return offersList;
    }

    /**
     * Method to add the given offer to wallet from deals and promotions page
     * or the first available product if null is passed
     *
     * @param offer offer to add
     */
    public static void addOfferToWallet(Map<String, Object> offer) {
        int index = 0;
        pausePageHangWatchDog();
        if (offer == null) {
            List<Map<String, Object>> offersList = MyOffers.offerList();
            for (Map<String, Object> ol : offersList) {
                if (ol.get("isOfferAvailableToAddToWallet").equals(true)) {
                    break;
                }
                index++;
            }
        } else {
            if (offer.get("isOfferAvailableToAddToWallet").equals(true))
                index = offerList().indexOf(offer);
            else
                System.out.println("Cannot add offer to wallet.");
        }

        List<WebElement> elements = offerElements();
        elements.get(index).findElement(By.cssSelector(".add-to-wallet")).click();
        Wait.attributeChanged(elements.get(index).findElement(By.cssSelector(".add-to-wallet")), "class", ".*disabled.*");
        if (signedIn() && !(elements.get(index).findElement(By.cssSelector(".add-to-wallet")).getAttribute("class").contains("disabled")))
            Assert.fail("The add offer to bag element did not become disabled after use");
        resumePageHangWatchDog();
    }

    /**
     * Private method to return the list of offer container elements
     *
     * @return offer container elements
     */
    private static List<WebElement> offerElements() {
        List<WebElement> offerElementsList;
        if (macys())
            offerElementsList = Elements.findElement(Elements.element("my_offers.offers_container")).findElements(By.cssSelector("li.offer"));
        else
            offerElementsList = Elements.findElement(Elements.element("my_offers.offers_container")).findElements(By.cssSelector(".ofr_tbl"));
        return offerElementsList;
    }

}