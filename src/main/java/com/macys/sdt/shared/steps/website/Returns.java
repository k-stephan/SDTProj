package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.models.ReturnService;
import com.macys.sdt.shared.actions.MEW.panels.GlobalNav;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.ReturnsPage;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.json.JSONObject;
import org.junit.Assert;

import java.util.*;


public class Returns extends StepUtils {

    public int maxTries = 10;
    public JSONObject returnOrderDetails;
    String orderNum = null;

    @When("^I navigate to confirmation page using \"([^\"]*)\" order as a \"([^\"]*)\" user$")
    public void I_navigate_to_confirmation_page_using_order_as_a_user(String orderType, String userType) throws Throwable {
        ReturnsPage returnsPage = new ReturnsPage();
        HashMap order = new HashMap();
        order.put("return_order", orderType);
        returnOrderDetails = Utils.getVirtualReturns(order);
        pausePageHangWatchDog();
        returnsPage.navigateToSelectionPage(orderType, userType);
        returnsPage.selectItemsAndContinueToSubmitPage();
        Clicks.click("return_submit.submit_return");
        if(safari())
            Wait.secondsUntilElementPresent("return_confirmation.crl_number", 10);
        Wait.forPageReady();
        if (!onPage("return_confirmation"))
            Assert.fail("User is not navigated to return confirmation page!!");
        resumePageHangWatchDog();
    }

    @Then("^I should see barcode and crl number$")
    public void I_should_see_barcode_and_crl_number() throws Throwable {
        if (!Elements.elementPresent("return_confirmation.crl_number"))
            Assert.fail("crl number not displayed on return confirmation page!!");
        if (!Elements.elementPresent("return_confirmation.barcode"))
            Assert.fail("barcode not displayed on return confirmation page!!");
    }

    @Given("^I visit order history page as a guest user$")
    public void I_visit_order_history_page_as_a_guest_user() throws Throwable {
        (new PageNavigation()).I_visit_the_web_site_as_a_guest_user();
        Clicks.click("home.goto_order_status");
        Wait.forPageReady();
        if (!onPage("order_status"))
            Assert.fail("User is not redirected to order status page!!");
    }

    @When("^I select \"([^\"]*)\" order as a \"([^\"]*)\" user$")
    public void I_select_order_as_as_user(String orderType, String userType) throws Throwable {
        HashMap order = new HashMap();
        order.put("return_order", orderType);
        returnOrderDetails = Utils.getVirtualReturns(order);
        String orderNumber = returnOrderDetails.getString("order_number");
        ReturnService returns = new ReturnService();
        returns.deleteReturnRecord(orderNumber);
        if (userType.equals("signed")) {
            if (returns.orderExistsByOrderNumber(orderNumber)) {
                Map userData = returns.getUserDetails(orderNumber);
                String password = Utils.decryptPassword(userData.get("password").toString());
                UserProfile customer = TestUsers.getCustomer(null);
                customer.getUser().getLoginCredentials().setPassword(password);
                CommonUtils.signInOrCreateAccount();
            } else {
                CommonUtils.signInOrCreateAccount();
                returns.insertOrderByOrderNumber(orderNumber, TestUsers.currentEmail);
            }
        }
    }

    @When("^I associate \"([^\"]*)\" order in db$")
    public void I_associate_order_in_db(String orderType) throws Throwable {
        orderNum = Utils.getOrderNumber(orderType);
        ReturnService returnObj = new ReturnService();
        if (returnObj.orderExistsByOrderNumber(orderNum)) {
            Map userData = returnObj.getUserDetails(orderNum);
            String passWord = Utils.decryptPassword(userData.get("password").toString());
            if(safari())
                Wait.secondsUntilElementPresent("home.goto_my_account_link", 15);
            if (MEW()) {
                GlobalNav.openGlobalNav();
                GlobalNav.navigateOnGnByName("My Account");
                GlobalNav.closeGlobalNav();
            } else {
                Clicks.click("home.goto_my_account_link");
            }
            if(safari())
                Wait.secondsUntilElementPresent("sign_in.email", 15);
            TextBoxes.typeTextbox("sign_in.email", userData.get("email").toString());
            TextBoxes.typeTextbox("sign_in.password", passWord);
            Clicks.click("sign_in.verify_page");
            Wait.secondsUntilElementNotPresent("sign_in.verify_page", 5);
        } else {
            CommonUtils.signInOrCreateAccount();
            UserProfile customer = TestUsers.getCustomer(null);
            String email = customer.getUser().getProfileAddress().getEmail();
            returnObj.insertOrderByOrderNumber(orderNum, email);
        }
    }

    @And("^I lookup with order number and emailaddress in (OH|EasyReturns) page$")
    public void I_lookup_with_order_number_and_emailaddress_in_page(String pageName) throws Throwable {
        if (pageName.equals("OH")) {
            if (!onPage("order_status"))
                Assert.fail("User not redirected to order status page!!");
        } else {
            if (!onPage("easy_returns"))
                Assert.fail("User not redirected to easy_returns page!!");
        }
        ReturnsPage returnsPage = new ReturnsPage();
        for (int index = 0; index < maxTries; index++) {
            returnsPage.lookupOrderByEmail(returnOrderDetails);
            if (title().contains("Order Details") || title().contains("Order Status"))
                break;
        }
    }

    @And("^I select return items button in \"(OH|OD)\" page$")
    public void I_select_return_items_button_in_page(String pageName) throws Throwable {
        ReturnsPage returnsPage = new ReturnsPage();
        switch (pageName) {
            case "OH":
                Clicks.click("home.goto_order_status");
                returnsPage.findOrderInDateRange(returnOrderDetails.getString("order_number"));
                returnsPage.returnOrder(returnOrderDetails.getString("order_number"));
                break;
            case "OD":
                Assert.assertFalse("ERROR - DATA : Order test data is not present in environment!!", Elements.elementPresent("order_status.error_message"));
                Clicks.click("order_details.return_items");
                break;
            default:
                Assert.fail("Invalid page name: " + pageName);
        }
        Wait.untilElementPresent("return_selection.order_number");
        if (!onPage("return_selection"))
            Assert.fail("User not redirected to return selection page!!");
    }

    @And("^I select items and continue to submit page$")
    public void I_select_items_and_continue_to_submit_page() throws Throwable {
        ReturnsPage returnsPage = new ReturnsPage();
        returnsPage.returnOrderDetails = returnOrderDetails;
        returnsPage.selectItemsAndContinueToSubmitPage();
    }

    @Then("^I should be navigated to OH page$")
    public void I_should_be_navigated_to_OH_page() throws Throwable {
        if (!onPage("order_status"))
            Assert.fail("User not redirected to order status page!!");
    }

    @And("^I set the return \"([^\"]*)\" status for \"([^\"]*)\" order in db$")
    public void I_set_the_return_status_for_order_in_db(String returnStatus, String orderType) throws Throwable {
        HashMap<String, String> order = new HashMap<>();
        order.put("return_order", orderType);
        returnOrderDetails = Utils.getVirtualReturns(order);
        ReturnService returnService = new ReturnService();
        returnService.updateReturnStatus(returnStatus, returnOrderDetails);
    }

    @Then("^I should see line item qty as \"([^\"]*)\" for \"([^\"]*)\" shipment$")
    public void I_should_see_line_item_qty_as_for_shipment(String quantityType, String shipmentStatus) throws Throwable {
        if (!onPage("order_details"))
            Assert.fail("User is not in order details page!!");
        String statusHeader = (macys() ? (shipmentStatus.split("\\|")[0]) : (shipmentStatus.split("\\|")[shipmentStatus.split("\\|").length - 1]));
        ReturnsPage returnsPage = new ReturnsPage();
        Map shipment = new HashMap<>();
        for (Map ship : returnsPage.getOrderDetails())
            if (ship.get("headerStatus").toString().contains(statusHeader))
                shipment = ship;
        ReturnService returnService = new ReturnService();
        Map returnDetails = returnService.getReturnInitiatedOrder(returnOrderDetails.getString("order_number"));
        String expectedQuantity = null;
        switch (quantityType) {
            case "return_credited_qty of return_initiated_qty":
                expectedQuantity = quantityType.replace("return_initiated_qty", returnDetails.get("quantity").toString()).replace("return_credited_qty", returnOrderDetails.getString("credited_qty").toString());
                break;
            case "0 of return_initiated_qty":
                expectedQuantity = quantityType.replace("return_initiated_qty", returnDetails.get("quantity").toString());
                break;
            case "return_credited_qty":
                expectedQuantity = returnOrderDetails.getString("credited_qty");
                break;
            case "return_qty":
                expectedQuantity = returnDetails.get("quantity").toString();
        }
        String actualQuantity;
        if (((List) shipment.get("lineItems")).size() > 1) {
            Map lineItemDetails = new HashMap<>();
            for (Map lineItem : ((List<Map>) shipment.get("lineItems")))
                lineItemDetails = lineItem;
            if (lineItemDetails == null)
                Assert.fail("line item not found");
            actualQuantity = lineItemDetails.get("itemQty").toString();
        } else
            actualQuantity = ((List<Map>) shipment.get("lineItems")).get(0).get("itemQty").toString();
        if (!actualQuantity.equals(expectedQuantity))
            Assert.fail(actualQuantity + " is not equal to " + expectedQuantity);
    }

    @Then("^I verify order details for \"([^\"]*)\" items in OH page$")
    public void I_verify_order_details_for_items_in_OH_page(String status) throws Throwable {
        ReturnsPage returnObj = new ReturnsPage();
        returnObj.findOrderInDateRange(orderNum);
        Map orderHash = returnObj.getOrder(orderNum);
        if (orderHash.get("orderNumber").toString().isEmpty())
            Assert.fail("Order number is empty");
        if (orderHash.get("orderDate").toString().isEmpty())
            Assert.fail("Order date is empty");
        if (orderHash.get("orderTotal").toString().isEmpty())
            Assert.fail("Order total is empty");
        String statusHeader = macys() ? status.split("\\|")[0] : status.split("\\|")[1];
        String shipmentStatus = null;
        if (status.equals("backordered:|BACKORDERED")) {
            shipmentStatus = Elements.getText("order_details.order_status_text");
        } else {
            for (int index = 0; index < ((List<Map>) orderHash.get("orderDetails")).size(); index++) {
                if (((Set) (((List<Map>) orderHash.get("orderDetails")).get(index).keySet())).iterator().next().toString().replace("\n", " ").contains(statusHeader))
                    shipmentStatus = ((Set) (((List<Map>) orderHash.get("orderDetails")).get(index).keySet())).iterator().next().toString().replace("\n", " ");
            }
        }
        if(shipmentStatus == null)
            Assert.fail("ERROR - DATA: No order found in page with order status: "+statusHeader);
        if (!shipmentStatus.contains(statusHeader))
            Assert.fail("ERROR - DATA: MST related Data Issue");
        if (!status.equals("backordered:|BACKORDERED")) {
            for (Map orderDetails : ((List<Map>) orderHash.get("orderDetails"))) {
                orderDetails.keySet().forEach(orderDetailsKey -> {
                    if (orderDetailsKey.equals(statusHeader)) {
                        for (Map item : (List<Map>) orderDetails.get(statusHeader)) {
                            if (item.get("itemDescription").toString().isEmpty())
                                Assert.fail("Item description is empty");
                            if (item.get("itemAction").toString().isEmpty())
                                Assert.fail("Item action is empty");
                            if (item.get("itemPrice").toString().isEmpty())
                                Assert.fail("item price is empty");
                            if (item.get("itemQty").toString().isEmpty())
                                Assert.fail("item quantity is empty");
                        }
                    }
                });
            }
        } else {
            if (bloomingdales()) {
                if (!Elements.getText("order_details.backordered_text").replace("/n", " ").trim().contains("The item(s) below is on backorder and delivery will be delayed. To continue to wait, you must call 1-800-777-0000. Otherwise, the item(s) will be removed from your order."))
                    Assert.fail("backordered_text messaage is not correct!!");
                if (!Elements.getText("order_details.backordered_subtitle").replace("/n", " ").trim().contains("Please note, you do not have to call if:"))
                    Assert.fail("backordered_subtitle messaage is not correct!!");
                if (!Elements.getText("order_details.backordered_submessage").replace("/n", " ").trim().contains("You already have called. The item(s) was already on backorder when you placed the order. The delivery delay is less than 30 days."))
                    Assert.fail("backordered_submessage messaage is not correct!!");
            }
            Elements.getText("order_details.backordered_text").trim().contains("This item is on backorder and the delivery date is delayed. Please contact Customer Service at 1-800-BUY-MACY(289-6229) ???forAssistance???");
        }
    }

    @And("^I verify order details in OD page$")
    public void I_verify_order_details_in_OD_page() throws Throwable {
        List shippingMethod = new ArrayList<>();
        ReturnsPage returnObj = new ReturnsPage();
        returnObj.clickOrderDetailsButton(orderNum);
        List<Map> orderDetails = returnObj.getOrderDetails();
        for (Map itemDetails : orderDetails) {
            if (itemDetails.get("headerStatus").toString().isEmpty())
                Assert.fail("header is empty");
            if (itemDetails.get("address") == null)
                Assert.fail("address is empty");
            if (((Map) itemDetails.get("address")).size() > 0) {
                if (((Map) itemDetails.get("address")).containsKey("shippingMethod"))
                    shippingMethod.add(((Map) itemDetails.get("address")).get("shippingMethod").toString());
            }
            for (Map itemList : ((List<Map>) itemDetails.get("lineItems"))) {
                if (macys()) {
                    if (itemList.get("itemDescription").toString().isEmpty())
                        Assert.fail("Item description is empty");
                    if (itemList.get("giftBox").toString().isEmpty())
                        Assert.fail("gift box is not opted");
                    if (itemList.get("itemQty").toString().isEmpty())
                        Assert.fail("item quantity is empty");
                    if (itemList.get("itemPrice").toString().isEmpty())
                        Assert.fail("item price is empty");

                } else {
                    if (itemList.get("itemDescription").toString().isEmpty())
                        Assert.fail("Item description is empty");
                    if (itemList.get("giftBox").toString().isEmpty() && !itemList.get("status").toString().contains("Submitted"))
                        Assert.fail("gift box is not opted");
                    if (itemList.get("itemQty").toString().isEmpty())
                        Assert.fail("item quantity is empty");
                    if (itemList.get("itemPrice").toString().isEmpty())
                        Assert.fail("item price is empty");
                    if (itemList.get("status").toString().isEmpty())
                        Assert.fail("item status is empty");
                    if (itemList.get("total").toString().isEmpty())
                        Assert.fail("item total is empty");
                }
                if (!itemDetails.get("orderTotalDetails").toString().isEmpty()) {
                    if (((Map) itemDetails.get("orderTotalDetails")).keySet().size() < 0)
                        Assert.fail("Order total details is empty");
                }
            }
        }
    }

    @And("^I navigate to order details page as a \"([^\"]*)\" user$")
    public void I_navigate_to_order_details_page_as_user(String userType) throws Throwable {
        Clicks.click("home.goto_order_status");
        if(safari()){
            Wait.secondsUntilElementPresent("order_status.verify_page", 10);
            Wait.forPageReady();
        }
        ReturnsPage returnsPage = new ReturnsPage();
        if (userType.equals("signed")) {
            returnsPage.findOrderInDateRange(returnOrderDetails.getString("order_number"));
            returnsPage.selectOrder(returnOrderDetails.getString("order_number"));
        } else {
            if (returnOrderDetails.has("email"))
                returnsPage.lookupOrderByEmail(returnOrderDetails);
            else
                returnsPage.lookupOrderByPhone(returnOrderDetails);
        }
        if(safari()){
            Wait.secondsUntilElementPresent("order_details.verify_page", 10);
            Wait.forPageReady();
        }
        if (!onPage("order_details"))
            Assert.fail("User is not navigated to order details page!!");
    }

    @Then("^I should see return status as \"(submitted|in transit|delivered|received|incomplete|complete)\" with updated date$")
    public void I_should_see_return_status_as_with_updated_data(String returnStatus) throws Throwable {
        ReturnsPage returnsPage = new ReturnsPage();
        if (returnStatus.equals("delivered"))
            returnStatus = "received";
        String returnState = (macys() ? ("return : " + returnStatus) : ("return status " + returnStatus));
        List<Map> orderDetails = returnsPage.getOrderDetails();
        Map shipment = new HashMap<>();
        for (Map ship : orderDetails)
            if (ship.get("headerStatus").toString().toLowerCase().contains(returnState))
                shipment = ship;
        String statusCode = null;
        switch (returnStatus) {
            case "submitted":
                statusCode = "RETURN STATUS Submitted";
                break;
            case "in transit":
                statusCode = "RETURN STATUS Intransit";
                break;
            case "received":
                statusCode = "RETURN STATUS Received";
                break;
            case "incomplete":
                statusCode = "RETURN STATUS Incomplete";
        }
        ReturnService returnService = new ReturnService();
        String updatedDate = (returnStatus.equals("complete") ? returnOrderDetails.getString("item_credited_date") : (returnService.getStatusUpdatedDate(statusCode, returnOrderDetails.getString("order_number"))));
        if (macys()) {
            if (!shipment.get("headerStatus").toString().contains(updatedDate))
                Assert.fail("updated date is not matching in header status!!");
        } else {
            for (Map line : (List<Map>) shipment.get("lineItems"))
                if (!line.get("status").toString().contains(updatedDate))
                    Assert.fail("updated date is not matching in status!!");
        }
    }
}
