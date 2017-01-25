package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;

public class Deals extends StepUtils {

    /**
     * Clicks on random offer on deals and promotions page
     */
    public static void selectRandomOffer() {
        By offerEl = Elements.element("deals_and_promotions.offers_list_container");
        if (Elements.elementPresent(offerEl)) {
            Clicks.clickRandomElement(offerEl);
        } else {
            Assert.fail("ERROR-DATA: Offers are not displaying in the Deals & Promotions page");
        }
    }
}
