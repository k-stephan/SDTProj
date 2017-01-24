package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.models.ReturnService;
import com.macys.sdt.shared.steps.website.MyAccountSteps;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

public class ReturnsPage extends StepUtils {

    public JSONObject returnOrderDetails;
    public Map selectedLineItem, orderItems;
    public List itemsSelected;

    /*
        Navigate to order selection page with given order type and user type
        @param[String, String] orderType, userType
    */
    public void navigateToSelectionPage(String orderType, String userType) {
        HashMap order = new HashMap();
        order.put("return_order", orderType);
        returnOrderDetails = Utils.getVirtualReturns(order);
        try {
            String orderNumber = returnOrderDetails.getString("order_number");
            ReturnService returns = new ReturnService();
            returns.deleteReturnRecord(orderNumber);
            if (userType.equals("guest")) {
                System.out.println("Selected order as guest user");
            } else {
                if (returns.orderExistsByOrderNumber(orderNumber))
                    returns.deleteOrderRecord(orderNumber);
                if (!signedIn())
                    new MyAccountSteps().iSignInToMyExistingProfile();
                returns.insertOrderByOrderNumber(orderNumber, TestUsers.currentEmail);
            }
            Clicks.click(Elements.element("home.goto_order_status"));
            if(safari())
                Wait.secondsUntilElementPresent("order_status.verify_page", 10);
            Wait.forPageReady();
            if (userType.equals("guest")) {
                lookupOrderByEmail(returnOrderDetails);
                Assert.assertFalse("ERROR - DATA: Given order number is not present in environment!!", Elements.elementPresent("order_status.error_message"));
                Clicks.click("order_details.return_items");
            } else {
                Wait.forPageReady();
                findOrderInDateRange(orderNumber);
                Wait.forPageReady();
                returnOrder(orderNumber);
            }
        } catch (Throwable e) {
            Assert.fail("navigateToSelectionPage(): " + e);
        }
    }

    /*
        Select Items in order return selection page and navigate to return submit page
    */
    public void selectItemsAndContinueToSubmitPage() {
        orderItems = orderItemDetails();
        selectedLineItem = (Map) ((List) orderItems.get("orderLineItemList")).get(new Random().nextInt(((List) orderItems.get("orderLineItemList")).size()));
        if (selectedLineItem.get("reasonForReturnDescription").getClass() == ArrayList.class)
            ((List) selectedLineItem.get("reasonForReturnDescription")).remove(0);
        Map item = new HashMap<>();
        item.put("upcId", selectedLineItem.get("upcId"));
        item.put("quantity", "1");
        if (selectedLineItem.get("reasonForReturnDescription").getClass() == ArrayList.class)
            item.put("reasonForReturn", ((List) selectedLineItem.get("reasonForReturnDescription")).get(new Random().nextInt(((List) selectedLineItem.get("reasonForReturnDescription")).size())));
        else
            item.put("reasonForReturn", selectedLineItem.get("reasonForReturnDescription"));
        itemsSelected = new ArrayList<>();
        itemsSelected.add(item);
        selectItemsAndContinue(itemsSelected, returnOrderDetails);
        if(safari())
            Wait.secondsUntilElementPresent("return_submit.submit_return", 15);
        Wait.forPageReady();
        if (!onPage("return_submit"))
            Assert.fail("User is not navigated to return submission page!!");
    }

    /*
        Select items in return selection page and continue
        @param[List<Map>, JSONObject] items to be selected and return order details (JSON data)
     */
    public void selectItemsAndContinue(List<Map> itemsSelected, JSONObject returnOrderDetails) {
        for (Map item : itemsSelected)
            selectItem(item.get("upcId").toString(), item.get("quantity").toString(), item.get("reasonForReturn").toString());
        try {
            TextBoxes.typeTextbox(Elements.element("return_selection.email"), returnOrderDetails.get("email").toString());
            TextBoxes.typeTextbox(Elements.element("return_selection.confirm_email"), returnOrderDetails.get("email").toString());
        } catch (JSONException e) {
            Assert.fail("selectItemsAndContinue(): " + e);
        }
        if (Elements.elementPresent(Elements.element("return_selection.refund_method_container")))
            selectRefundMethod("default");
        Clicks.click(Elements.element("return_selection.submit_return"));
        Wait.forPageReady();
    }

    /*
        Select refund option in return selection page
        @param[String] refund option
     */
    public void selectRefundMethod(String refundType) {
        switch (refundType) {
            case "orignal_payment":
                Clicks.click(Elements.element("return_selection.credit_refund"));
                break;
            case "giftcard_payment":
                Clicks.click(Elements.element("return_selection.gift_card_refund"));
                break;
            default:
                List<WebElement> refunds = Elements.findElements(Elements.element("return_selection.refund_method"));
                refunds.get(new Random().nextInt(refunds.size())).click();
                break;
        }
    }

    /*
        select an item in return selection page
        @param[String, String, String] upc, qty and reason for return
     */
    public void selectItem(String itemUpc, String quantity, String reasonForReturn) {
        int index = lineItemIndex(itemUpc);
        WebElement lineItemElement = Elements.findElements(Elements.element("return_selection.line_item")).get(index);
        Clicks.javascriptClick(By.name("returnDetails.returnShipment.lineItemList[" + index + "].itemSelected"));
        if (reasonForReturn != null && Elements.elementPresent(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode")))
            DropDowns.selectByText(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode"), reasonForReturn);
        if (quantity != null && !quantity.equals("1"))
            DropDowns.selectByText(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode"), quantity);
    }

    /*
        To get line item from return selection page
        @param[String] upc
        @return[int] index of the line item
     */
    public int lineItemIndex(String itemUpc) {
        List<Map> orderItemList = getOrderLineItemList();
        for (int index = 0; index < orderItemList.size(); index++) {
            if (orderItemList.get(index).get("upcId").toString().equals(itemUpc))
                return index;
        }
        Assert.fail("Unable to find a item that matches " + itemUpc + "!!");
        return -1;
    }

    /*
        Get all order item details in return selection page
        @return[Map] orderItemDetails in return selection page
     */
    public Map orderItemDetails() {
        Map items = new HashMap<>();
        items.put("orderHeader", getOrderHeader());
        items.put("orderLineItemList", getOrderLineItemList());
        return items;
    }

    /*
        Get all order item list in OD page
        @return[List<Map>] all orders and items present in order details page
     */
    public List<Map> getOrderLineItemList() {
        List<Map> itemDetails = new ArrayList<>();
        List<WebElement> lineItems = Elements.findElements(Elements.element("return_selection.line_item"));
        for (int index = 0; index < lineItems.size(); index++) {
            WebElement line = lineItems.get(index);
            boolean isLineItemDisabled = false;
            boolean isLineItemVisible = (line.findElements(By.name("returnDetails.returnShipment.lineItemList[" + index + "].itemSelected")).size() > 0);
            String callToAction = ((line.findElements(By.className((macys() ? "selectionReminder" : "orReturns_itemCtaMessage"))).size() > 0) ? (line.findElement(By.className((macys() ? "selectionReminder" : "orReturns_itemCtaMessage"))).getText()) : "NA");
            if (isLineItemVisible)
                isLineItemDisabled = (line.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].itemSelected"))).getAttribute("type").equals("hidden");
            boolean vrFlag = (isLineItemDisabled || !(isLineItemVisible));
            List returnQuantity = new ArrayList<>();
            String quantitySelected;
            if (!line.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].selectedQuantity")).getAttribute("type").equals("hidden")) {
                if (!callToAction.equals("NA"))
                    returnQuantity = DropDowns.getAllValues(By.name("returnDetails.returnShipment.lineItemList[" + index + "].selectedQuantity"));
                quantitySelected = DropDowns.getSelectedValue(By.name("returnDetails.returnShipment.lineItemList[" + index + "].selectedQuantity"));
            } else {
                returnQuantity.add("1");
                quantitySelected = "1";
            }
            Map details = new HashMap<>();
            details.put("vrFlag", vrFlag);
            details.put("isLineItemCheckBoxHidden", !(isLineItemVisible));
            details.put("isItemSelected", isLineItemDisabled);
            details.put("vendorName", Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].vendorName")).getAttribute("value"));
            details.put("upcId", Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].upc")).getAttribute("value"));
            details.put("itemDescription", Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].itemDescription")).getAttribute("value"));
            details.put("sizeDescription", (Elements.elementPresent(By.name("returnDetails.returnShipment.lineItemList[" + index + "].sizeDescription")) ? Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].sizeDescription")).getAttribute("value") : ""));
            details.put("colorDescription", (Elements.elementPresent(By.name("returnDetails.returnShipment.lineItemList[" + index + "].colorDescription")) ? Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].colorDescription")).getAttribute("value") : ""));
            details.put("returnQuantity", returnQuantity);
            details.put("isReasonDisabled", (vrFlag ? "NA" : (Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode")).getAttribute((macys() ? "disabled" : "aria-disabled")))));
            details.put("quantitySelected", quantitySelected);
            details.put("reasonForReturnDescription", (vrFlag ? "NA" : (DropDowns.getAllValues(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode")))));
            details.put("isQuantityDisabled", (vrFlag ? "NA" : (Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].selectedQuantity"))).getAttribute((macys() ? "disabled" : "aria-disabled"))));
            details.put("reasonSelected", (vrFlag ? (Elements.findElement(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode")).findElement(By.xpath("..")).getText()) : (Elements.getText(By.name("returnDetails.returnShipment.lineItemList[" + index + "].reasonForReturnCode")))));
            details.put("productImgSrc", ((Elements.findElements(Elements.element("return_selection.line_item")).size() > index) ? (Elements.findElements(Elements.element("return_selection.line_item")).get(index).findElement(By.tagName("img")).getAttribute("src")) : null));
            details.put("callToAction", callToAction);
            itemDetails.add(details);
        }
        return itemDetails;
    }

    /*
        Get the order header details from order details page
        @return[Map] order header details
     */
    public Map getOrderHeader() {
        Map orderHeader = new HashMap<>();
        orderHeader.put("orderNumber", Elements.getText(Elements.element("return_selection.order_number")));
        String date = Elements.getText(Elements.element("return_selection.order_date"));
        orderHeader.put("orderDate", date.split(":")[date.split(":").length - 1].trim());
        String total = "";
        if (Elements.elementPresent(Elements.element("return_selection.order_total"))) {
            total = Elements.getText(Elements.element("return_selection.order_total"));
            if (bloomingdales())
                total = total.split("TOTAL")[total.split("TOTAL").length - 1].trim();
        }
        orderHeader.put("orderTotal", total);
        return orderHeader;
    }

    /*
        Return an order from order details page
         @param[String] order number
     */
    public void returnOrder(String orderNumber) throws Throwable {
        Wait.forPageReady();
        List<WebElement> orderContainers = Elements.findElements(Elements.element("order_status.order_history_container"));
        orderContainers.get(findOrderIndex(orderNumber)).findElement(By.className((macys() ? "returnItemsBtn" : "orHist_orderReturnsLinkItem"))).click();
        if(safari())
            Wait.secondsUntilElementPresent("return_selection.order_number",10);
        Wait.forPageReady();
        if (!onPage("return_selection")) {
            Assert.fail("User not redirected to return selection page!!");
        }
    }

    public Map getOrder(String orderNumber) throws Throwable {
        List<Map> orderList = getOrderList();
        Map orderDetails = new HashMap<>();
        for(int index = 0; index < orderList.size(); index++){
            if(orderList.get(index).get("orderNumber").equals(orderNumber))
                orderDetails = orderList.get(index);
        }
        return orderDetails;
    }

    /*
        Get the order index from order details page
        @param[String] order number
        @return[int] index of order present in order details page
     */
    public int findOrderIndex(String orderNumber) throws Throwable {
        List<WebElement> orderContainers = Elements.findElements(Elements.element("order_status.order_history_container"));
        for (int index = 0; index < orderContainers.size(); index++) {
            String pageOrderNumber = null;
            if (macys()) {
                pageOrderNumber = orderContainers.get(index).findElements(By.xpath("//span[@id='orderNumber_" + orderNumber + "']")).get(0).getText();
            } else {
                pageOrderNumber = Elements.findElements(Elements.element("order_status.order_history_order_text")).get(index).findElement(By.tagName("span")).getText().trim();
            }
            if (pageOrderNumber.contains(orderNumber))
                return index;
        }
        Assert.fail("ERROR - DATA: Did not find order number " + orderNumber + " in page");
        return -1;
    }

    /*
        To find order using date range in order details page
        @param[String] order number
     */
    public void findOrderInDateRange(String orderNumber) throws Throwable {
        Wait.forPageReady();
        List<Map> orderDetails = getOrderList();
        for (Map order : orderDetails) {
            if (order.get("orderNumber").toString().equals(orderNumber))
                return;
        }
        List<String> dropDownValues = (macys() ? DropDowns.getAllValues("order_status.order_date_range") : DropDowns.getAllCustomValues("order_status.order_date_range_list", "order_status.order_date_range_options"));
        for (int index = 1; index < dropDownValues.size(); index++) {
            if(macys())
                DropDowns.selectByText("order_status.order_date_range", dropDownValues.get(index));
            else
                DropDowns.selectCustomText("order_status.order_date_range_list", "order_status.order_date_range_options", dropDownValues.get(index));
            if(safari() || MEW())
                Utils.threadSleep(2000, null);
            Wait.forPageReady();
            for (Map order : getOrderList()) {
                if (order.get("orderNumber").toString().equals(orderNumber))
                    return;
            }
        }
        Assert.fail("ERROR - DATA: Did not find order number " + orderNumber + " in the available date ranges.");
    }

    /*
        Get all order details from order details page
        @return[List<Map>] order list from order details
     */
    public List<Map> getOrderList() throws Throwable {
        List<Map> orderDetails = new ArrayList<>();
        int pageCount = (macys() && Elements.elementPresent("order_status.page_navigation")) ? Elements.findElement("order_status.page_navigation").findElements(By.className("pgTopAlign")).size() : 1;
        for(int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            List<WebElement> orderContainers = Elements.findElements(Elements.element("order_status.order_history_container"));
            for (int index = 0; index < orderContainers.size(); index++) {
                Map order = new HashMap<>();
                if (macys()) {
                    String orderNo = orderContainers.get(index).findElement(By.cssSelector("label.devider span")).getText();
                    if (orderContainers.get(index).findElements(By.xpath("//span[@id='orderNumber_" + orderNo + "']")).size() == 0)
                        continue;
                    order.put("orderNumber", orderContainers.get(index).findElements(By.xpath("//span[@id='orderNumber_" + orderNo + "']")).get(0).getText());
                    order.put("orderDate", Elements.findElements(Elements.element("order_status.order_history_order_date")).get(index).getText());
                    order.put("orderTotal", orderContainers.get(index).findElements(By.xpath("//span[@id='orderTotalSpan_" + orderNo + "']")).get(0).getText());
                } else {
                    order.put("orderNumber", Elements.findElements(Elements.element("order_status.order_history_order_text")).get(index).findElement(By.tagName("span")).getText().trim());
                    order.put("orderDate", Elements.findElements(Elements.element("order_status.order_history_order_date")).get(index).findElement(By.className("orStatus_boldText")).getText());
                    order.put("orderTotal", Elements.findElements(Elements.element("order_status.order_history_order_total")).get(index).findElement(By.className("orStatus_boldText")).getText());
                }
                order.put("orderDetails", getShipmentsDetails(orderContainers.get(index)));
                orderDetails.add(order);
            }
            if(macys()){
                if(Elements.elementPresent("order_status.page_navigation") && Elements.findElement("order_status.page_navigation").findElement(By.className("pgNext")).isDisplayed())
                    Elements.findElement("order_status.page_navigation").findElement(By.className("pgNext")).click();
                Wait.forPageReady();
            }
        }
        return orderDetails;
    }

    /*
        Get the shipment details from order details page
        @param[WebElement] order container element
        @return[List<Map>] all shipment details
     */
    public List<Map> getShipmentsDetails(WebElement containerElement) throws Throwable {
        List<Map> shipmentDetails = new ArrayList<>();
        if (macys()) {

            List<WebElement> shipmentElements = containerElement.findElements(By.className("orderStatus"));
            for (WebElement shipment : shipmentElements) {
                HashMap details = new HashMap();
                String headerStatus = shipment.findElement(By.tagName("h2")).getText();
                if (shipment.findElements(By.cssSelector("h2 span")).size() > 0) {
                    String spanText = shipment.findElement(By.tagName("h2")).findElement(By.tagName("span")).getText();
                    if (spanText != null)
                        headerStatus = statusCleansing(headerStatus, spanText);
                }
                details.put(headerStatus, getLineItems(shipment));
                shipmentDetails.add((HashMap) details.clone());
            }

        } else {
            List<WebElement> shipmentsLevelElements = new ArrayList<>();
            String[] classNames = {"orStatus_trackingStatus", "orStatus_orderSectionHeader", "orHist_itemInfoContainer"};
            for (WebElement shipmentElement : containerElement.findElements(By.tagName("div")))
                if (Arrays.asList(classNames).contains(shipmentElement.getAttribute("class")))
                    shipmentsLevelElements.add(shipmentElement);
            int loopCounter = 0;
            while (loopCounter < shipmentsLevelElements.size()) {
                String headerStatus = shipmentsLevelElements.get(loopCounter).getText().trim();
                if (shipmentsLevelElements.get(loopCounter).findElements(By.tagName("span")).size() > 0) {
                    String spanText = shipmentsLevelElements.get(loopCounter).findElement(By.tagName("span")).getText();
                    if (spanText != null)
                        statusCleansing(headerStatus, spanText);
                }
                WebElement leElement = shipmentsLevelElements.get(loopCounter);
                List<Map> itemList = new ArrayList<>();
                while (((loopCounter + 1) < shipmentsLevelElements.size()) && shipmentsLevelElements.get(loopCounter + 1).getAttribute("class").equals("orHist_itemInfoContainer")) {
                    Map details = new HashMap<>();
                    details.put("itemDescription", leElement.findElements(By.tagName("div")).get(0).getText());
                    details.put("itemAction", leElement.findElements(By.tagName("div")).get(1).getText());
                    details.put("itemPrice", leElement.findElements(By.tagName("div")).get(2).getText());
                    details.put("itemQty", leElement.findElements(By.tagName("div")).get(3).getText());
                    itemList.add(details);
                    loopCounter += 1;
                }
                Map status = new HashMap<>();
                status.put(headerStatus, itemList);
                shipmentDetails.add(status);
                loopCounter += 1;
            }
        }
        return shipmentDetails;
    }

    /*
        get all line items present in order history page
        @param[WebElement] shipment container
        @return[List<Map>] all line item details
     */
    public List<HashMap> getLineItems(WebElement shipmentContainer) throws Throwable {
        List<HashMap> lineItems = new ArrayList<>();
        List<WebElement> lineElements = shipmentContainer.findElements(By.className("orderStatusDetails"));
        HashMap details = new HashMap();
        for (WebElement container : lineElements) {
            for(WebElement liElement : container.findElements(By.tagName("li"))){
                if(liElement.findElements(By.className("itemStatus")).size() > 0)
                    details.put("itemAction", liElement.findElement(By.className("itemStatus")).getText());
                if(liElement.findElements(By.className("price")).size() > 0)
                    details.put("itemPrice", liElement.findElement(By.className("price")).getText());
                if(liElement.findElements(By.tagName("span")).size() > 0)
                    details.put("itemQty", liElement.findElement(By.tagName("span")).getText());
                if(macys() && liElement.getAttribute("class").contains("description") && liElement.findElements(By.tagName("p")).size() > 0)
                    details.put("itemDescription", liElement.findElement(By.tagName("p")).getText());
                if(liElement.findElements(By.tagName("p")).size() > 0){
                    for(WebElement pElement : liElement.findElements(By.tagName("p"))){
                        if(pElement.getText().contains("color"))
                            details.put("itemColor", pElement.getText());
                        if(pElement.getText().contains("size"))
                            details.put("itemSize", pElement.getText());
                        if(bloomingdales() && pElement.findElements(By.tagName("a")).size() > 0)
                            details.put("itemDescription", pElement.getText());
                    }
                }
            }
            lineItems.add((HashMap) details.clone());
        }
        return lineItems;
    }

    public String statusCleansing(String headerStatus, String spanText) throws Throwable {
        String textToInclude = (macys() ? "for store pick-up" : "IN-STORE PICK UP");
        if (!headerStatus.contains(textToInclude)) {
            if (spanText != null) {
                headerStatus = headerStatus.replace(spanText, "");
            }
        }
        return headerStatus.replace("\n", " ").trim();
    }

    /*
        lookup order by using email and order number
     */
    public void lookupOrderByEmail(JSONObject returnOrderDetails) throws Throwable {
        Wait.untilElementPresent(Elements.element("order_status.view_order_details"));
        TextBoxes.typeTextbox(Elements.element("order_status.order_number"), returnOrderDetails.get("order_number").toString());
        TextBoxes.typeTextbox(Elements.element("order_status.email"), returnOrderDetails.get("email").toString());
        Clicks.click(Elements.element("order_status.view_order_details"));
    }

    /*
        lookup order by using phone and order number
     */
    public void lookupOrderByPhone(JSONObject returnOrderDetails) throws Throwable {
        Wait.untilElementPresent(Elements.element("order_status.view_order_details"));
        TextBoxes.typeTextbox(Elements.element("order_status.order_number"), returnOrderDetails.get("order_number").toString());
        TextBoxes.typeTextbox(Elements.element("order_status.phone_number"), returnOrderDetails.get("phone_number").toString());
        Clicks.click(Elements.element("order_status.view_order_details"));
    }

    public List<Map<String, Object>> getOrderDetails() throws Throwable {
        List<Map<String, Object>> orderDetails = new ArrayList<>();
        String headerStatus;
        for (WebElement container : Elements.findElements(Elements.element("order_details.order_details_container"))) {
            Map<String, Object> shipment = new HashMap<>();
            if (macys()) {
                headerStatus = container.findElements(By.tagName("div")).get(0).getText();
                boolean isShippingAddressExists = container.findElement(By.className("shippingAddress")).findElements(By.tagName("h3")).size() > 0;
                shipment.put("isShippingAddressAvailable", isShippingAddressExists);
                shipment.put("address", (isShippingAddressExists ? getShippingAddress(container.findElement(By.className("shippingAddress"))) : "Not available"));
                shipment.put("trackMyReturn", (container.findElements(By.className("trackReturnBtn")).size() > 0));
            } else {
                shipment.put("address", getShippingAddress(container.findElement(By.className("orDetails_shipContainer"))));
                shipment.put("trackMyReturn", (container.findElements(By.className("orStatus_trackingEnabled")).size() > 0));
                if (macys())
                    headerStatus = container.findElement(By.className("orStatus_trackingStatus")).getText();
                else
                    headerStatus = (container.findElements(By.className("orStatus_trackingStatus")).size() > 0) ? (container.findElement(By.className("orStatus_trackingStatus")).getText()) : (container.findElement(By.className("orStatus_subHeader")).getText());
            }
            shipment.put("headerStatus", headerStatus);
            shipment.put("lineItems", getLineItemsDetails(container));
            shipment.put("orderTotalDetails", ((container.findElements(By.className((macys() ? "grandTotal" : "orDetails_totalArrowContainer"))).size() > 0) ? getOrderSubTotalDetails(container) : ""));
            orderDetails.add(shipment);
        }
        return orderDetails;
    }

    public Map getOrderSubTotalDetails(WebElement orderTotalContainer) throws Throwable {
        Map orderTotalInfo = new HashMap<>();
        if (macys()) {
            orderTotalContainer.findElement(By.className("grandTotal")).click();
            WebElement totalElement = orderTotalContainer.findElement(By.className("total"));
            List<String> orderTotalInfoArr = new ArrayList<>();
            List<String> orderTotalInfoSpanArr = new ArrayList<>();
            for (WebElement element : totalElement.findElements(By.tagName("label"))) {
                orderTotalInfoArr.add(element.getText());
                orderTotalInfoSpanArr.add(element.findElement(By.tagName("span")).getText());
            }
            for (int index = 0; index < orderTotalInfoSpanArr.size(); index++)
                orderTotalInfo.put(orderTotalInfoArr.get(index).toString().replaceAll("\n", "").replaceAll((orderTotalInfoSpanArr.get(index).toString().contains("$") ? "\\" + orderTotalInfoSpanArr.get(index).toString() : orderTotalInfoSpanArr.get(index).toString()), ""), orderTotalInfoSpanArr.get(index).toString());
        } else {
            orderTotalContainer.findElement(By.className("orDetails_totalArrowContainer")).click();
            orderTotalContainer.findElement(By.className("orDetails_shipmentSubTotal")).isDisplayed();
            Wait.untilElementPresent(By.className("orDetails_shipmentSubTotal"));
            List<WebElement> totalElements = orderTotalContainer.findElement(By.className("orDetails_shipmentSubTotal")).findElements(By.tagName("div"));
            for (int index = 0; index < totalElements.size() - 2; index++)
                orderTotalInfo.put(totalElements.get(index).findElements(By.tagName("span")).get(0).getText(), totalElements.get(index).findElements(By.tagName("span")).get((totalElements.get(index).findElements(By.tagName("span")).size() - 1)).getText());
            if (Elements.elementPresent(Elements.element("order_details.additional_text")))
                orderTotalInfo.put(Elements.getText(Elements.element("order_details.additional_text")), Elements.getText(Elements.element("order_details.additional_amount_text")));
            orderTotalContainer.findElement(By.className("orDetails_totalArrowContainer")).click();
        }
        return orderTotalInfo;
    }

    public void selectOrder(String orderNumber) throws Throwable {
        Wait.forPageReady();
        List<WebElement> orderContainers = Elements.findElements(Elements.element("order_status.order_history_container"));
        for (int index = 0; index < orderContainers.size(); index++) {
            if (macys()) {
                orderContainers.get(index).findElement(By.id("orderDetailBtn_" + orderNumber)).click();
            } else {
                Elements.findElements(Elements.element("order_status.order_details_buttons")).get(findOrderIndex(orderNumber)).click();
            }
        }
        Wait.forPageReady();
    }

    public List<Map> getLineItemsDetails(WebElement shipmentContainer) throws Throwable {
        List<Map> itemList = new ArrayList<>();
        if (macys()) {
            WebElement shipmentDetailsInfoContainer = shipmentContainer.findElement(By.id("shippingDetailsInfoDiv"));
            List<WebElement> lineItemsElements = shipmentDetailsInfoContainer.findElements(By.xpath("./ul/li"));
            boolean isVrItem = shipmentDetailsInfoContainer.findElements(By.className("returnReminderInfo")).size() > 0;
            int index = 0;
            for (WebElement container : lineItemsElements) {
                String lineItemText = null;
                if (container.getAttribute("class").equals("returnReminderInfo"))
                    continue;
                if (shipmentDetailsInfoContainer.findElements(By.id("returnsItemReceived")).size() > 0)
                    lineItemText = shipmentDetailsInfoContainer.findElement(By.id("returnsItemReceived")).getText();
                else if (shipmentDetailsInfoContainer.findElements(By.id("returnsItemMissing")).size() > 0)
                    lineItemText = shipmentDetailsInfoContainer.findElement(By.id("returnsItemMissing")).getText();
                List<WebElement> details = container.findElements(By.tagName("li"));
                Map items = new HashMap<>();
                String name = null, gift = null, qty = null, price = null;
                if (details.size() >= 0)
                    name = details.get(0).getText();
                if (details.size() >= 1)
                    gift = details.get(1).getText();
                if (details.size() >= 2)
                    qty = details.get(2).getText();
                if (details.size() >= 3)
                    price = details.get(3).getText();
                items.put("itemDescription", name.split("\n")[0]);
                items.put("giftBox", gift.split("\n")[gift.split("\n").length - 1]);
                items.put("itemQty", qty.split("\n")[qty.split("\n").length - 1]);
                items.put("itemPrice", (price == null ? "" : price.split("\n")[price.split("\n").length - 1]));
                items.put("lineItemText", lineItemText);
                items.put("writeAReview", (name.contains("write a review") ? true : false));
                if (container.findElement(By.xpath("..")).findElements(By.className("returnReminderInfo")).size() > 0) {
                    String[] remainderText = container.findElement(By.xpath("..")).findElements(By.className("returnReminderInfo")).get(index).getText().split("\n");
                    items.put("callToActionLine1", (isVrItem ? remainderText[2] : "NA"));
                    items.put("callToActionLine2", (isVrItem ? remainderText[3] : "NA"));
                    items.put("reasonCode", (isVrItem ? remainderText[0] : "NA"));
                }
                index += 1;
                itemList.add(items);
            }
        } else {
            List<WebElement> lineItemElements = shipmentContainer.findElements(By.className("orDetails_itemContainer"));
            for (WebElement lineItem : lineItemElements) {
                int index = 0;
                boolean vrItem = lineItem.findElements(By.xpath(".//div[contains(@class,'orStatus_infoIcon')]")).size() > 0;
                List<WebElement> lines = lineItem.findElements(By.className("orStatus_cellContainer"));
                String name = null, status = null, gift = null, qty = null, price = null, total = null;
                if (lines.size() >= 0)
                    name = lines.get(0).getText();
                if (lines.size() >= 1)
                    status = lines.get(1).getText();
                if (lines.size() >= 2)
                    gift = lines.get(2).getText();
                if (lines.size() >= 3)
                    qty = lines.get(3).getText();
                if (lines.size() >= 4)
                    price = lines.get(4).getText();
                if (lines.size() >= 5)
                    total = lines.get(5).getText();
                Map data = new HashMap<>();
                data.put("itemDescription", name);
                data.put("status", status);
                data.put("giftBox", gift);
                data.put("itemQty", qty);
                data.put("itemPrice", (price != null ? price : ""));
                data.put("total", (total != null ? total : ""));
                data.put("writeAReview", (vrItem ? "NA" : (name.contains("WRITE REVIEW") ? true : false)));
                data.put("callToAction", (vrItem ? lineItem.findElement(By.xpath(".//div[contains(@class,'orStatus_infoIcon')]")).getText() : "NA"));
                index += 1;
                itemList.add(data);
            }
        }
        return itemList;
    }

    public Map getShippingAddress(WebElement container) throws Throwable {
        Map shippingAddress = new HashMap<>();
        if (macys()) {
            if (container.findElement(By.tagName("h3")).getText().equals("Shipping Address")) {
                if (container.findElements(By.tagName("h3")).size() >= 1) {
                    List<WebElement> address = container.findElement(By.tagName("ul")).findElements(By.tagName("li"));
                    String warehouseName = null, warehouseAddress = null, warehouseCity = null, phoneNumber = null, firstNameLastName = null,
                            city = null, phone = null, email = null;
                    if (address.size() >= 1)
                        warehouseName = firstNameLastName = address.get(0).getText();
                    if (address.size() >= 2)
                        warehouseAddress = address.get(1).getText();
                    if (address.size() >= 3)
                        warehouseCity = city = address.get(2).getText();
                    if (address.size() >= 4)
                        phone = phoneNumber = address.get(3).getText();
                    if (address.size() >= 5)
                        email = address.get(4).getText();
                    shippingAddress.put("addressType", "Return Address");
                    shippingAddress.put("warehouseName", warehouseName);
                    shippingAddress.put("warehouseAddress", warehouseAddress);
                    shippingAddress.put("warehouseCity", warehouseCity.split(",")[0].trim());
                    shippingAddress.put("warehouseState", warehouseCity.split(",")[warehouseCity.split(",").length - 1].trim().split(" ")[0].trim());
                    shippingAddress.put("warehouseZipCode", warehouseCity.split(",")[warehouseCity.split(",").length - 1].trim().split(" ")[warehouseCity.split(",")[warehouseCity.split(",").length - 1].trim().split(" ").length - 1].trim());
                    shippingAddress.put("phoneNumber", phoneNumber);
                    boolean isPrivacy = Elements.elementPresent(Elements.element("order_details.address_privacy_message"));
                    shippingAddress.put("addressType", "Shipping");
                    shippingAddress.put("firstNameLastName", firstNameLastName);
                    shippingAddress.put("shippingMethod", container.getText().split("\n")[container.getText().split("\n").length - 1].trim());
                    shippingAddress.put("addressLine1", (isPrivacy ? null : (address.get(1).getText())));
                    shippingAddress.put("city", (isPrivacy ? null : (city.split(",")[0])));
                    shippingAddress.put("state", (isPrivacy ? null : (city.split(",")[city.split(",").length - 1].trim().split(" ")[0].trim())));
                    shippingAddress.put("zipCode", (isPrivacy ? null : (city.split(" ")[city.split(" ").length - 1].trim().split(" ")[city.split(" ")[city.split(" ").length - 1].trim().split(" ").length - 1].trim())));
                    shippingAddress.put("phone", (isPrivacy ? null : phone));
                    shippingAddress.put("email", (isPrivacy ? null : email));
                    shippingAddress.put("addressPrivacyMessage", (isPrivacy ? (Elements.getText(Elements.element("order_details.address_privacy_message"))) : null));
                }
            } else {
                List<WebElement> address = container.findElements(By.tagName("li"));
                shippingAddress.put("addressType", "BOPS");
                shippingAddress.put("bopsStoreLocation", container.findElement(By.id("storeNumber")).getText());
                shippingAddress.put("bopsStoreName", address.get(0).getText());
                shippingAddress.put("bopsAddressLine1", address.get(1).getText());
                shippingAddress.put("bopsAddressLine2", address.get(2).getText());
                shippingAddress.put("bopsCity", (address.get(3).getText().split(",")[0]));
                shippingAddress.put("bopsState", (address.get(3).getText().split(",")[address.get(3).getText().split(",").length - 1].trim().split(" ")[0]));
                shippingAddress.put("bopsZipCode", (address.get(3).getText().split(",")[address.get(3).getText().split(",").length - 1].trim().split(" ")[address.get(3).getText().split(",")[address.get(3).getText().split(",").length - 1].trim().split(" ").length - 1]));
                shippingAddress.put("bopsStoreNumber", address.get(4).getText());
                shippingAddress.put("bopsFirstLastName", address.get(5).getText());
                shippingAddress.put("bopsEmail", address.get(6).getText());
                shippingAddress.put("bopsphone", address.get(7).getText());
            }
        } else {
            List<WebElement> divElements = container.findElements(By.tagName("div"));
            if (container.findElements(By.className("orDetails_shipMethod")).size() > 0) {
                if (container.findElement(By.className("orDetails_shipMethod")).getText().equals("SHIPPING METHOD")) {
                    String firstLastName = null, addressLine1 = null, cityState = null, zipCode = null, shippingType = null, shippingMethod = null;
                    boolean privacy = Elements.elementPresent(Elements.element("order_details.address_privacy_message"));
                    if (divElements.size() >= 0)
                        firstLastName = divElements.get(0).getText();
                    if (divElements.size() >= 1)
                        addressLine1 = divElements.get(1).getText();
                    if (divElements.size() >= 2)
                        cityState = divElements.get(2).getText();
                    if (divElements.size() >= 3)
                        zipCode = divElements.get(3).getText();
                    if (divElements.size() >= 4)
                        shippingType = divElements.get(4).getText();
                    if (divElements.size() >= 5)
                        shippingMethod = divElements.get(5).getText();
                    shippingAddress.put("addressType", "Shipping");
                    shippingAddress.put("firstLastName", firstLastName);
                    shippingAddress.put("addressLine1", (privacy ? null : addressLine1.trim()));
                    shippingAddress.put("city", (privacy ? null : cityState.replace(cityState.split(" ")[cityState.split(" ").length - 1], "")));
                    shippingAddress.put("state", (privacy ? null : cityState.split(" ")[cityState.split(" ").length - 1]));
                    shippingAddress.put("zipCode", (privacy ? null : zipCode));
                    shippingAddress.put("shippingType", (privacy ? null : shippingType));
                    shippingAddress.put("shippingMethod", (privacy ? null : shippingMethod));
                    shippingAddress.put("addressPrivacyMessage", (privacy ? Elements.getText(Elements.element("order_details.address_privacy_message")) : null));
                } else {
                    String store = null, storeName = null, addressLine1 = null, cityState = null, zipCode = null, storeNumber = null, shippingMethod = null,
                            firstLastName = null, email = null, phone = null;
                    if (divElements.size() >= 0)
                        store = divElements.get(0).getText();
                    if (divElements.size() >= 1)
                        storeName = divElements.get(1).getText();
                    if (divElements.size() >= 2)
                        addressLine1 = divElements.get(2).getText();
                    if (divElements.size() >= 3)
                        cityState = divElements.get(3).getText();
                    if (divElements.size() >= 4)
                        zipCode = divElements.get(4).getText();
                    if (divElements.size() >= 5)
                        storeNumber = divElements.get(5).getText();
                    if (divElements.size() >= 6)
                        shippingMethod = divElements.get(6).getText();
                    if (divElements.size() >= 7)
                        firstLastName = divElements.get(7).getText();
                    if (divElements.size() >= 8)
                        email = divElements.get(8).getText();
                    if (divElements.size() >= 9)
                        phone = divElements.get(9).getText();
                    shippingAddress.put("addressType", "BOPS");
                    shippingAddress.put("bopsStoreLocation", store);
                    shippingAddress.put("bopsStoreName", storeName);
                    shippingAddress.put("bopsAddressLine1", addressLine1.trim());
                    shippingAddress.put("bopsCity", cityState.split(",")[0].trim());
                    shippingAddress.put("bopsState", cityState.split(",")[cityState.split(",").length - 1].trim());
                    shippingAddress.put("bopsZipCode", zipCode);
                    shippingAddress.put("bopsStoreNumber", storeNumber);
                    shippingAddress.put("bopsShippingMethod", shippingMethod);
                    shippingAddress.put("bopsFirstLastName", firstLastName);
                    shippingAddress.put("bopsEmail", email);
                    shippingAddress.put("bopsPhone", phone);
                }
            } else {
                String warehouse = null, warehouseAddress = null, cityState = null, zipCode = null;
                if (divElements.size() >= 0)
                    warehouse = divElements.get(0).getText();
                if (divElements.size() >= 1)
                    warehouseAddress = divElements.get(1).getText();
                if (divElements.size() >= 2)
                    cityState = divElements.get(2).getText();
                if (divElements.size() >= 3)
                    zipCode = divElements.get(3).getText();
                shippingAddress.put("addressType", "Return Address");
                shippingAddress.put("warehouseName", warehouse.trim());
                shippingAddress.put("warehouseAddress", warehouseAddress.trim());
                shippingAddress.put("warehouseCity", cityState.split(",")[0].trim());
                shippingAddress.put("warehouseState", cityState.split(",")[cityState.split(",").length - 1].trim());
                shippingAddress.put("warehouseZipCode", zipCode);
            }
        }
        return shippingAddress;
    }

    public void clickOrderDetailsButton(String orderNum) {
        List<WebElement> orderNumberButtons = Elements.findElements(Elements.element("order_status.order_number_detail"));
        Boolean orderExists = false;
        if (orderNumberButtons.size() > 0) {
            for (WebElement orderDetails : orderNumberButtons) {
                if (macys()) {
                    if (orderDetails.getText().contains(orderNum)) {
                        Clicks.click(By.id("orderDetailBtn_" + orderNum));
                        orderExists = true;
                        break;
                    }
                } else {
                    if (orderDetails.getAttribute("href").contains(orderNum)) {
                        Clicks.click(orderDetails);
                        orderExists = true;
                        break;
                    }
                }
            }

        }
        if (orderExists == false)
            Assert.fail("Order number not found");
    }
}
