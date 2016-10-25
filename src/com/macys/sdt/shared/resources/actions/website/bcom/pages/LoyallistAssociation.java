package com.macys.sdt.shared.resources.actions.website.bcom.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.resources.model.LoyalistDetails;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;

public class LoyallistAssociation extends StepUtils {

    public static void loyaltyAssociation(LoyalistDetails loyalty_customer) {
        Wait.forPageReady();
        TextBoxes.typeTextbox("loyalty_association.loyallist_number", loyalty_customer.getLoyaltyId());
        TextBoxes.typeTextbox("loyalty_association.loyallist_last_name", loyalty_customer.getLastName());
        TextBoxes.typeTextbox("loyalty_association.loyallist_zip_code", loyalty_customer.getZipCode());
        try {
            Clicks.click("loyalty_association.submit_id");
            Assert.assertFalse("ERROR-DATA: Sorry, we were unable to locate your Loyallist account. Please add valid data", Elements.elementPresent("loyalty_association.lty_error"));
        } catch (Exception e) {
            Assert.fail("Failed to validate loyallist data in the page: " + e);
        }
    }
}
