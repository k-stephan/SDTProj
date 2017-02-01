package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.utils.db.models.OrderServices;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.junit.Assert;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderConfirmation extends StepUtils {

    public static String orderNumber = null;

    /**
     * Verifies the display of USL data on order confirmation page
     */
    @Then("^I should see USL information on order confirmation page$")
    public void I_should_see_usl_information_on_order_confirmation_page() {

        boolean responsive = onPage("responsive_order_confirmation");
        String type = responsive ? "responsive_" : "";
        String plenti_points_redeemed;
        if (Elements.elementPresent((type + "order_confirmation.plenti_section").trim())) {
            Wait.secondsUntilElementPresent((type + "order_confirmation.plenti_points_redeemed").trim(), 20);
            plenti_points_redeemed = Elements.getText((type + "order_confirmation.plenti_points_redeemed".trim())).replaceAll("\\D+", "");
            Assert.assertFalse("ERROR - ENV : Plenti services are down in environment!!", Elements.getText(type + "order_confirmation.error_message").contains("Your Plenti account is not available"));
            if (!plenti_points_redeemed.equals(TestUsers.getUSLInformation().getRedeemedPlentiPoints())) {
                Assert.fail("Plenti Redeem points mismatch");
            }
        } else {
            Assert.assertFalse("ERROR - ENV : Plenti services are down in environment!!", Elements.getText(type + "order_confirmation.error_message").contains("Your Plenti account is not available"));
            Assert.fail("Plenti section is not visible");
        }
    }

    /**
     * Verifies paypal elements on order confirmation page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see paypal icon and email on order confirmation page$")
    public void I_should_see_paypal_icon_and_email_on_order_confirmation_page() throws Throwable {
        pausePageHangWatchDog();
        boolean responsive = onPage("responsive_order_confirmation");
        String type = responsive ? "responsive_" : "";
        if (Wait.untilElementPresent((type + "order_confirmation.paypal_section").trim())) {
            String paypalUserEmail = Elements.getText(((type + "order_confirmation.paypal_email_text")).trim());
            if (!paypalUserEmail.equals(TestUsers.getPayPalInformation().get("email"))) {
                Assert.fail("Paypal Email is not correct on order confirmation page");
            }
        } else {
            Assert.fail("Paypal section is not visible on order confirmation page");
        }
        resumePageHangWatchDog();
    }

    /**
     * Saves order number from order confirmation page to variable
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I capture order number from order_confirmation page$")
    public void I_capture_order_number_from_order_confirmation_page() throws Throwable {
        Wait.secondsUntilElementNotPresent("responsive_order_summary.place_order", 15);
        Wait.secondsUntilElementNotPresent("responsive_order_summary.mask", 15);
        Wait.forPageReady();
        Wait.secondsUntilElementPresent("responsive_order_confirmation.verify_page", 20);
        boolean responsive = onPage("responsive_order_confirmation");
        String page = responsive ? "responsive_order_confirmation" : "order_confirmation";
        Assert.assertTrue("Order number not displayed.", Wait.untilElementPresent(page + ".order_number"));
        orderNumber = Elements.getText(page + ".order_number");
    }

    /**
     * Verifies the display of the order confirmation page
     *
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see order confirmation section displayed with order details$")
    public void I_should_see_order_confirmation_section_displayed_with_order_details() throws Throwable {
        String page = "responsive_order_confirmation";
        Assert.assertTrue("Order number is not displayed,", Elements.getText(page + ".order_number").length() > 0);
        Assert.assertTrue("Order total is not displayed,", Elements.getText(page + ".order_total").length() > 0);
        Assert.assertFalse("Order confirm email is not displayed,", Elements.getText(page + ".order_confirmation_email_address").isEmpty());
    }

    /**
     * Verifies that the correct shipping method shows on the order confirmation page
     *
     * @param expectedText shipping type
     * @throws Throwable if any exception occurs
     */
    @Then("^I verify (No Hurry|standard) shipping method details on confirmation page$")
    public void I_Verify_NoHurry_In_Shipping_Method_Section(String expectedText) throws Throwable {
        String actualText;
        expectedText = expectedText.toLowerCase();
        actualText = Elements.getText(Elements.element("responsive_order_confirmation.shipping_methods_set")).split("\n")[1].toLowerCase();
        Assert.assertTrue("Expected shipping method is not displayed!!", (actualText.contains(expectedText)));
    }

    /**
     * Verifies the no-hurry order is present in the database
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I verify the order details in DB for nohurry$")
    public void I_verify_the_order_details_in_DB_for_nohurry() throws Throwable {
        orderNumber = Elements.getText("responsive_order_confirmation.order_number").split(":\n")[1];
        OrderServices order = new OrderServices();
        String inventoryDownMsg = Elements.getText("responsive_order_confirmation.rc_inventory_down_message");

        //Getting Order details from site DB and verifying the order status
        HashMap orderDetails = order.getOrderDetails(orderNumber);
        Assert.assertTrue("Order placed in batch mode", (orderDetails.get("ORDER_STATUS").toString()).equals((inventoryDownMsg.contains("inventory system is temporarily offline") ? "BPEND" : "RCMPL")));

        //Getting shipment details from site DB and verifying the shipment code for nohurry
        List<String> shipMethodCode = order.getShipMethodCode(orderNumber);
        Assert.assertTrue("Shipping method code for nohurry is not correct", shipMethodCode.contains("W"));

        //Getting prepare order request in XML Element format from site DB and verifying the shipType for noHurry shipping method
        List<Element> prepareOrderReq = order.getPrepareOrderRequest(orderNumber);
        List<Object> shipTypes = new ArrayList<>();
        for (int index = 0; index < shipMethodCode.size(); index++)
            shipTypes.add(prepareOrderReq.get(prepareOrderReq.size() - 1).getElementsByTagName("shipType").item(index).getTextContent());
        Assert.assertTrue("NoHurry shipping method code is not displayed in prepare order request", shipTypes.contains("W"));
    }


}
