package com.macys.sdt.shared.actions.website.bcom.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.junit.Assert;

import java.util.HashMap;

public class PaypalLogin extends StepUtils {

    public void login() throws Throwable {
        try {
            Wait.forPageReady("PayPal");
            Thread.sleep(3000);
            boolean proceed = true;
            HashMap<String, String> paypal_customer = TestUsers.getPayPalInformation();
            String email = paypal_customer.get("email");
            String password = paypal_customer.get("password");
            int tries = 0;
            while (proceed) {
                tries++;
                if (tries > 3) {
                    throw new Exception("Unable to login to Paypal");
                }
                if (Elements.elementPresent("paypal_login.error_msg")) {
                    Assert.fail("Paypal Login incorrect");
                }
                if (Elements.elementPresent("paypal_login.paypal_login2")) {
                    TextBoxes.typeTextbox("paypal_login.paypal_email2", email);
                    TextBoxes.typeTextbox("paypal_login.paypal_password2", password);
                    Clicks.click("paypal_login.paypal_login2");
                    Thread.sleep(3000);
                    if (Wait.untilElementPresent("paypal_login.continue")) {
                        proceed = false;
                    }

                } else if (Wait.untilElementPresent("paypal_login.login_iframe")) {
                    Thread.sleep(3000);
                    Wait.untilElementPresent("paypal_login.login_iframe");
                    switchToFrame("paypal_login.login_iframe");
                    Clicks.click("paypal_login.paypal_email");
                    TextBoxes.typeTextbox("paypal_login.paypal_email", email);
                    Clicks.click("paypal_login.paypal_password");
                    TextBoxes.typeTextbox("paypal_login.paypal_password", password);
                    Clicks.click("paypal_login.paypal_login");
                    Thread.sleep(6000);
                    switchToFrame("default");
                    if (Wait.untilElementPresent("paypal_login.continue")) {
                        Thread.sleep(3000);
                        proceed = false;
                    }

                }
            }

        } catch (Exception e) {
            Assert.fail("Unable to login into paypal due to " + e);
        }
    }
}
