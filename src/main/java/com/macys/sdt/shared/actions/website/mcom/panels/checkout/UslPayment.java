package com.macys.sdt.shared.actions.website.mcom.panels.checkout;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.junit.Assert;

public class UslPayment extends StepUtils {

    /**
     * Method to apply usl points as payment type in checkout page
     */
    public void addUslAsPayment() {
        String payment = "apply_my_plenti";
        Wait.secondsUntilElementPresent(payment + ".use_usl", (safari() ? 20 : 5));
        if (!Elements.elementPresent(payment + ".usl_rc_redeem_amount"))
            Clicks.click(payment + ".use_usl");
        Wait.secondsUntilElementPresent(payment + ".usl_rc_redeem_amount", 5);
        if (Elements.elementPresent(payment + ".usl_rc_redeem_amount")) {
            TextBoxes.typeTextbox(payment + ".usl_rc_redeem_pin", TestUsers.getUSLInformation().getUslPin());
            Clicks.click(payment + ".usl_rc_apply_points");
        }
        Wait.untilElementPresent("responsive_order_summary.gift_plenti_reward_values");
        Assert.assertTrue("ERROR - ENV: Unable to apply USL points in checkout pages!!", Elements.elementPresent("responsive_order_summary.gift_plenti_reward_values"));
    }
}
