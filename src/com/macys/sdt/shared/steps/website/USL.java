package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.model.UslInfo;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.website.mcom.pages.checkout.Checkout;
import cucumber.api.java.en.And;

public class USL extends StepUtils {

    @And("^I lookup plenti id using valid usl phone number on (payment|shopping bag|my account) page$")
    public void I_lookup_plenti_id_using_valid_usl_phone_number(String page) throws Throwable {
        String pageName = null;
        UslInfo usl_info;
        switch (page) {
            case "payment":
                pageName = "shipping_payment_signed_in";
                break;
            case "shopping bag":
                pageName = "shopping_bag";
                break;
        }
        // String phone_no = CommonUtils.getEnrolledUslId().getString("usl_phone");
        usl_info = TestUsers.getUSLInformation();
        String phone_no = usl_info.getUslPhone();
        Wait.untilElementPresent(pageName + ".lookup_link");
        Clicks.click(pageName + ".lookup_link");
        TextBoxes.typeTextbox(pageName + ".usl_phone_number", phone_no);
        Wait.forPageReady();
        Clicks.click(pageName + ".usl_search_phone");
        Wait.untilElementPresent(pageName + ".usl_added_result");
        Wait.secondsUntilElementPresent(pageName+".added_usl_id", 5);
    }

    @And("^I add usl as payment on (payment|shopping bag|my account) page$")
    public void iAddUslAsPayment(String page) throws Throwable {
        String pageName = null;
        switch (page) {
            case "payment":
                pageName = "shipping_payment_signed_in";
                break;
            case "shopping bag":
                pageName = "shopping_bag";
                break;
        }
        Checkout.addUslAsPayment();
        Wait.secondsUntilElementPresent(page + ".plenti_points_text", 5);
    }


}
