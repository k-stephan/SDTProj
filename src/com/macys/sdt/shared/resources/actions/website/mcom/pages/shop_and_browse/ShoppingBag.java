package com.macys.sdt.shared.resources.actions.website.mcom.pages.shop_and_browse;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.resources.model.Product;
import com.macys.sdt.framework.utils.Cookies;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.db.models.CampaignService;
import com.macys.sdt.framework.utils.db.models.ProductService;
import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;

public class ShoppingBag extends StepUtils {

    public static void emptyCurrentShoppingBag() {
        if (!onPage("shopping_bag"))
            Navigate.visit("shopping_bag");
        Wait.forPageReady();
        try {
            String elementName = macys() ? ".remove_current_bag_items" : ".remove_item";
            while (Wait.untilElementPresent("shopping_bag" + elementName)){
               Clicks.click("shopping_bag" + elementName);
            }
        } catch (StaleElementReferenceException | org.openqa.selenium.NoSuchElementException e) {
            // just means there are no more items left to remove
        }
    }

    public static void emptySavedShoppingBag() {
        if (!onPage("shopping_bag"))
            Navigate.visit("shopping_bag");
        Wait.forPageReady();
        try {
            String elementName = macys() ? ".remove_saved_bag_items" : ".remove_item";
            while (Wait.untilElementPresent("shopping_bag" + elementName))
                Clicks.click("shopping_bag" + elementName);
        } catch (StaleElementReferenceException | org.openqa.selenium.NoSuchElementException e) {
            // just means there are no more items left to remove
        }
    }

    public static void mergeSavedBag() {
        try {
            Clicks.click("shopping_bag.add_to_current_bag");
        } catch (Exception e) {
            Assert.fail("Could not click on add to current bag button");
            e.printStackTrace();
        }
    }

    public static void removeBonusItemsFromBag() {
        if (Elements.elementPresent("shopping_bag.line_items")) {
            Elements.findElements("shopping_bag.line_items").forEach(item -> {
                try {
                    WebElement el = item.findElement(By.className(macys() ? "colFreeTotal" : "gwpItemText"));
                    if (el.isDisplayed()) {
                        WebElement removeLink = item.findElement(Elements.element("shopping_bag.remove_item"));
                        Clicks.click(removeLink);
                        Wait.untilElementNotPresent(removeLink);
                    }
                } catch (Exception e) {
                    // exception will be thrown on BCOM non-gift items. No need to take action.
                }
            });
            System.out.println("Removed all bonus items from bag");
        }
    }

    public static void removeItem(int itemIndex) {
        try {
            Clicks.click(Elements.findElements("shopping_bag.line_items").get(itemIndex).findElement(Elements.element("shopping_bag.remove_item")));
            Navigate.browserRefresh();
            System.out.println("Removed given item from bag!!");
        } catch (Exception e) {
            Assert.fail("Error while removing given item from bag!!");
            e.printStackTrace();
        }
    }

    public static int findItemIndexByType(List<Product> bagItems, String itemType) {
        int finalIndex = -1;
        Predicate<Product> func;
        switch (itemType) {
            case "registry":
                func = (p) -> p.registryItem;
                break;
            case "normal":
                func = (p) -> p.normalItem;
                break;
            default:
                func = (p) -> p.bopsEligible;
        }
        for (int index = 0; index < bagItems.size(); index++) {
            Product p = bagItems.get(index);
            if (func.test(p)) {
                finalIndex = index;
                break;
            }
        }
        return finalIndex;
    }

    /**
     * Method to get all product details on shopping bag page
     *
     * @return product details
     */
    public static List<Product> getAllProductDetails() {
        List<Product> productDetails = new ArrayList<>();
        int noOfProducts = Elements.findElements(Elements.element("shopping_bag.item_names")).size();
        for (int i = 0; i < noOfProducts; i++) {
            Product p = new Product(Integer.parseInt(Elements.findElements(Elements.element("shopping_bag.product_ids")).get(i).getText()));
            p.name = Elements.findElements(Elements.element("shopping_bag.item_names")).get(i).getText();

            if (macys() && !Elements.findElements(Elements.element("shopping_bag.quantities")).get(i).getSize().toString().isEmpty()) {
                p.quantity = Integer.parseInt(new Select(Elements.findElements("shopping_bag.quantities").get(i)).getFirstSelectedOption().getText());
                p.giftCard = false;
            } else if (bloomingdales() && Elements.findElements((ie() ? By.className("itemQtyDropdown") : By.cssSelector(".itemQtyDropdown>.current"))).get(i).isDisplayed()) {
                p.quantity = Integer.parseInt((ie() ? DropDowns.getSelectedValue(By.className("itemQtyDropdown")) : Elements.findElements(By.cssSelector(".itemQtyDropdown>.current")).get(i).getText()));
                p.giftCard = false;
            } else {
                p.quantities = Elements.findElements(Elements.element("shopping_bag.gift_card_quantities")).get(i).getText().split("\n");
                p.giftCard = true;
            }

            WebElement lineItem = Elements.findElements(Elements.element("shopping_bag.line_items")).get(i);
            p.registryItem = !lineItem.findElements(Elements.element("shopping_bag.registrant_name_details")).isEmpty();
            p.bopsEligible = !lineItem.findElements(Elements.element("shopping_bag.bops_shipping")).isEmpty();
            p.normalItem = (lineItem.findElements(Elements.element("shopping_bag.bops_shipping")).isEmpty() && lineItem.findElements(Elements.element("shopping_bag.registrant_name_details")).isEmpty());

            String price = Elements.findElements(Elements.element("shopping_bag.individual_prices")).get(i).getText();
            String individualPrice, individualCurrency, salePrice = null, saleCurrency = null;

            if (macys()) {
                if (price.contains("\n")) {
                    individualPrice = price.split("\n")[1].split(" ")[1].replaceAll("[^0-9.]", "");
                    individualCurrency = price.split("\n")[1].split(" ")[1].replaceAll("[0-9.,]", "");

                    salePrice = price.split("\n")[0].split(" ")[1].replaceAll("[^0-9.]", "");
                } else {
                    individualPrice = price.replaceAll("[^0-9.]", "");
                    individualCurrency = price.replaceAll("[0-9.,]", "");
                }
            } else {
                individualPrice = price.replaceAll("[^0-9.]", "");
                individualCurrency = price.replaceAll("[0-9.,]", "");
                if (lineItem.findElements(By.className("priceSale")).size() == 1) {
                    String priceSale = lineItem.findElement(By.className("priceSale")).getText();
                    salePrice = priceSale.split(" ")[1].replaceAll("[^0-9.]", "");
                }
            }

            p.individualPrice = Double.parseDouble(individualPrice);
            p.currency = individualCurrency;
            if (salePrice != null)
                p.salePrice = Double.parseDouble(salePrice);

            String tPrice = Elements.findElements(Elements.element("shopping_bag.item_totals")).get(i).getText();
            String totalPrice = tPrice.replaceAll("[^0-9.]", "");

            p.totalPrice = Double.parseDouble(totalPrice);

            String colorName = null, sizeName = null;

            if (lineItem.findElements(By.cssSelector(macys() ? ".valColor" : ".itemColor")).size() == 1)
                colorName = lineItem.findElement(By.cssSelector(macys() ? ".valColor" : ".itemColor")).getText();
            p.colorName = colorName;

            if (lineItem.findElements(By.cssSelector(macys() ? ".valSize" : ".itemSize")).size() == 1)
                sizeName = lineItem.findElement(By.cssSelector(macys() ? ".valSize" : ".itemSize")).getText();
            p.sizeName = sizeName;

            int promoDescSize = lineItem.findElements(By.cssSelector(macys() ? ".promoDesc" : ".itemPromoPricingKey")).size();
            String[] promoDescriptions = new String[promoDescSize];

            if (promoDescSize > 0) {
                for (int j = 0; j < promoDescSize; j++) {
                    promoDescriptions[j] = lineItem.findElements(By.cssSelector(macys() ? ".promoDesc" : ".itemPromoPricingKey")).get(j).getText();
                }
            } else
                promoDescriptions = null;

            p.promoDescriptions = promoDescriptions;

            int promoPriceSize = lineItem.findElements(By.cssSelector(macys() ? ".promotionDescPad>.promoPrice" : ".itemPromoPricingValue")).size();
            double[] promoPrices = new double[promoPriceSize];
            String[] promoCurrencies = new String[promoPriceSize];

            if (promoPriceSize > 0) {
                for (int k = 0; k < promoPriceSize; k++) {
                    promoPrices[k] = Double.parseDouble(lineItem.findElements(By.cssSelector(macys() ?
                            ".promotionDescPad>.promoPrice" :
                            ".itemPromoPricingValue")).get(k).getText().replaceAll("[^0-9.]", ""));
                    promoCurrencies[k] = lineItem.findElements(By.cssSelector(macys() ? ".promotionDescPad>.promoPrice" : ".itemPromoPricingValue")).get(k).getText().replaceAll("[0-9.,-]", "");
                }
            } else {
                promoPrices = null;
                promoCurrencies = null;
            }

            p.promoPrices = promoPrices;

            productDetails.add(p);
        }
        return productDetails;
    }

    /**
     * Method to update product quantity
     *
     * @param product  to update quantity
     * @param quantity to update for a product
     */
    public static void updateQuantity(Product product, String quantity) {
        int index = productIndex(product);
        if (macys() || Elements.findElements("shopping_bag.quantities").get(index).isDisplayed()) {
            DropDowns.selectByText(Elements.findElements("shopping_bag.quantities").get(index), quantity);
            Wait.secondsUntilElementNotPresent("shopping_bag.loading_mask", 10);
        } else {
            Clicks.click(Elements.findElements("shopping_bag.quantities_list").get(index));
            List<WebElement> quantityList = Elements.findElements("shopping_bag.quantities_options").get(index).findElements(By.cssSelector("li"));
            for (WebElement el : quantityList) {
                if (el.getText().equalsIgnoreCase(quantity)) {
                    Clicks.click(el);
                    break;
                }
            }
        }

        if (product.totalPrice == getAllProductDetails().get(index).totalPrice) {
            Assert.fail("Product quantity is not updated.");
        }
    }

    /**
     * Method to get quantity options
     *
     * @param product to get quantity options
     * @return quantity options
     */
    public static List<String> quantityOptions(Product product) {
        List<String> quantities = new ArrayList<>();
        List<WebElement> options;

        int index = productIndex(product);
        if (macys()) {
            Select select = new Select(Elements.findElements(Elements.element("shopping_bag.quantities")).get(index));
            options = select.getOptions();
        } else {
            Clicks.click(Elements.findElements(Elements.element("shopping_bag.quantities_list")).get(index));
            options = Elements.findElements(Elements.element("shopping_bag.quantities_options")).get(index).findElements(By.cssSelector("li"));
        }

        for (WebElement o : options) {
            quantities.add(o.getText());
        }

        if (bloomingdales() && Elements.findElements(Elements.element("shopping_bag.quantities_list")).get(index).isDisplayed())
            Clicks.click(Elements.findElements(Elements.element("shopping_bag.quantities_list")).get(index));

        Collections.sort(quantities);
        return quantities;
    }

    /**
     * Method to return subtotal amount on shopping bag  page
     *
     * @return bag subtotal
     */
    public static double subtotal() {
        double subtotal = -1;

        if (macys()) {
            String subtotalAmount = Elements.getText(Elements.element("shopping_bag.subtotal_price"));
            subtotal = Double.valueOf(subtotalAmount.replaceAll("[^0-9.]", ""));
        } else {
            try {
                JSONObject jsonObject = new JSONObject(getBagDetailsFromRest());
                subtotal = Double.valueOf(((JSONObject) ((JSONObject) jsonObject.get("bag")).get("bagSummary")).get("merchandiseTotal").toString());
            } catch (JSONException e) {
                Assert.fail("subtotal(): " + e);
            }
        }
        return subtotal;
    }

    /**
     * Method to get mb money earn information based on bag total
     *
     * @param productDetails list of products
     * @param bagSubtotal    for getting earn information
     * @return mb money earn information
     */
    public static Map<String, Object> getMbmoneyEarnInformationBasedOnBagTotal(List<Product> productDetails, Double bagSubtotal) {
        Map<String, Object> mbmoneyEarnInformation = new HashMap<>();

        int mbMoneyEstimatedEarnAmount = 0;
        int nextGetAmount = 0;
        double nextSpentAmount = 0.00;

        for (Product p : productDetails) {
            if (p.promoDescriptions != null) {
                double[] promoPrices =  p.promoPrices;
                double promoTotal = DoubleStream.of(promoPrices).sum();
                if (promoTotal == p.totalPrice) {
                    productDetails.remove(p);
                }
            }
        }

        String[] productIds = new String[productDetails.size()];
        for (int i = 0; i < productDetails.size(); i++) {
            productIds[i] = "" + productDetails.get(i).id;
        }

        List<Map<String, String>> departmentIdForProduct = ProductService.getDepartmentIdForProduct(productIds);

        for (Product product : productDetails)
            for (Map<String, String> productDept : departmentIdForProduct)
                if (product.id == Integer.parseInt(productDept.get("productId")))
                    product.department = productDept.get("departmentId");

        List<Map<String, String>> mbmoneyCampaignExclusionDetails = CampaignService.getMbmoneyCampaignExclusionDetails(macys() ? "MEarn" : "BEarn");

        List<String> excludedDepartmentList = new ArrayList<>();

        for (Map<String, String> exclusion : mbmoneyCampaignExclusionDetails) {
            if (exclusion.get("exclusionName").equals("DEPARTMENT"))
                excludedDepartmentList.add(exclusion.get("exclusionValue"));
        }

        List<Product> mbmoneyEligibleItems = new ArrayList<>();
        double excludedOrderTotal = 0.00;

        for (Product product : productDetails) {
            if (!excludedDepartmentList.contains(product.department))
                mbmoneyEligibleItems.add(product);
            else
                excludedOrderTotal += product.totalPrice;
        }

        double eligibleOrderTotal = excludedOrderTotal == 0.00 ? bagSubtotal : bagSubtotal - excludedOrderTotal;

        List<Map<String, String>> mbmoneyCampaignAttributeDetails = CampaignService.getMbmoneyCampaignAttributeDetails(macys() ? "MEarn" : "BEarn");

        if (macys()) {
            List<String> thresholdLimitData = new ArrayList<>();
            List<Double> thresholdLimits = new ArrayList<>();
            List<Integer> estEarnAmounts = new ArrayList<>();
            for (Map<String, String> attribute : mbmoneyCampaignAttributeDetails) {
                if (attribute.get("campaignAttrType").equals("THRESHOLD")) {
                    thresholdLimitData.add(attribute.get("campaignAttrName"));
                    estEarnAmounts.add(Integer.valueOf(attribute.get("campaignAttrValue")));
                }
            }

            Collections.sort(estEarnAmounts);

            for (String data : thresholdLimitData) {
                if (data.contains("-")) {
                    String[] t = data.split("-");
                    thresholdLimits.add(Double.valueOf(t[0]));
                    thresholdLimits.add(Double.valueOf(t[1]));
                } else
                    thresholdLimits.add(Double.valueOf(data));
            }

            Collections.sort(thresholdLimits);

            if (thresholdLimits.get(0) > eligibleOrderTotal)
                mbMoneyEstimatedEarnAmount = 0;
            else if (thresholdLimits.get(thresholdLimits.size() - 1) <= eligibleOrderTotal)
                mbMoneyEstimatedEarnAmount = estEarnAmounts.get(estEarnAmounts.size() - 1);
            else {
                for (Map<String, String> attribute : mbmoneyCampaignAttributeDetails) {
                    if (attribute.get("campaignAttrType").equals("THRESHOLD") && (attribute.get("campaignAttrName").contains("-")))
                        if (Double.valueOf(attribute.get("campaignAttrName").split("-")[0]) <= eligibleOrderTotal && Double.valueOf(attribute.get("campaignAttrName").split("-")[1]) >= eligibleOrderTotal)
                            mbMoneyEstimatedEarnAmount = Integer.valueOf(attribute.get("campaignAttrValue"));
                }
            }

            if (estEarnAmounts.indexOf(mbMoneyEstimatedEarnAmount) + 1 < estEarnAmounts.size())
                nextGetAmount = estEarnAmounts.get(estEarnAmounts.indexOf(mbMoneyEstimatedEarnAmount) + 1);

            if (nextGetAmount != 0) {
                for (Map<String, String> attribute : mbmoneyCampaignAttributeDetails) {
                    if (attribute.get("campaignAttrType").equals("THRESHOLD") && Integer.parseInt(attribute.get("campaignAttrValue")) == nextGetAmount) {
                        nextSpentAmount = Double.valueOf(attribute.get("campaignAttrName").split("-")[0]) - eligibleOrderTotal;
                        nextSpentAmount = Double.parseDouble(new DecimalFormat("##.##").format(nextSpentAmount));
                    }
                }
            }
        } else {
            List<Integer> earnRules = new ArrayList<>();
            List<Integer> earnAmounts = new ArrayList<>();
            int earnMax = 0;
            for (Map<String, String> attribute : mbmoneyCampaignAttributeDetails) {
                if (attribute.get("campaignAttrType").equals("EARNRULE")) {
                    earnRules.add(Integer.valueOf(attribute.get("campaignAttrName")));
                    earnAmounts.add(Integer.valueOf(attribute.get("campaignAttrValue")));
                } else if (attribute.get("campaignAttrType").equals("GENERAL") && attribute.get("campaignAttrName").equals("EARNMAX")) {
                    earnMax = Integer.parseInt(attribute.get("campaignAttrValue"));
                }
            }

            Collections.sort(earnRules);
            Collections.sort(earnAmounts);

            if (earnRules.size() == 1 && earnAmounts.size() == 1) {
                int earnRule = earnRules.get(0);
                int earnAmount = earnAmounts.get(0);
                mbMoneyEstimatedEarnAmount = Math.min(((int) (Math.floor((eligibleOrderTotal / earnRule))) * earnAmount), earnMax);
            } else {
                int index = -1;
                for (Integer i : earnRules) {
                    if (eligibleOrderTotal < i)
                        break;
                    index++;
                }
                mbMoneyEstimatedEarnAmount = Math.min(((index == -1) ? 0 : earnAmounts.get(index)), earnMax);
            }
        }

        mbmoneyEarnInformation.put("estimatedEarnAmount", mbMoneyEstimatedEarnAmount);
        mbmoneyEarnInformation.put("nextSpentAmount", nextSpentAmount);
        mbmoneyEarnInformation.put("nextGetAmount", nextGetAmount);
        mbmoneyEarnInformation.put("eligibleOrderTotal", eligibleOrderTotal);
        mbmoneyEarnInformation.put("mbmoneyEligibleItems", mbmoneyEligibleItems);

        return mbmoneyEarnInformation;
    }

    /**
     * Method to get mmoney estimated earn information on bag page
     *
     * @return mmoney estimated earn information
     */
    public static Map<String, String> getMmoneyEstimateEarnInformation() {
        Map<String, String> mmoneyEstimatedEarnInfo = new HashMap<>();

        if (Elements.elementPresent(Elements.element("shopping_bag.mmoney_earn_section"))) {
            List<WebElement> mmoneyEarnInfo = Elements.findElements(By.cssSelector("#earnContainer>span"));

            String mmoneyEarnAmount = mmoneyEarnInfo.get(0).getText().replaceAll("[^0-9]", "");
            String mmoneyNextSpent = mmoneyEarnInfo.size() > 1 ? mmoneyEarnInfo.get(1).getText().split(" ")[1].replaceAll("$", "") : null;
            String mmoneyNextGet = mmoneyEarnInfo.size() > 1 ? mmoneyEarnInfo.get(1).getText().split(" ")[mmoneyEarnInfo.get(1).getText().split(" ").length - 1].replaceAll("$", "") : null;

            mmoneyEstimatedEarnInfo.put("mmoneyEarnAmountText", mmoneyEarnInfo.get(0).getText());
            mmoneyEstimatedEarnInfo.put("mmoneyTeaserLineText", mmoneyEarnInfo.size() > 1 ? mmoneyEarnInfo.get(1).getText() : null);
            mmoneyEstimatedEarnInfo.put("mmoneyEstimatedEarnAmount", mmoneyEarnAmount);
            mmoneyEstimatedEarnInfo.put("mmoneyNextSpentAmount", mmoneyNextSpent);
            mmoneyEstimatedEarnInfo.put("mmoneyNextGetAmount", mmoneyNextGet);
        } else {
            Assert.fail("mMoney Earn section is not displaying on shopping bag page!!!");
        }

        return mmoneyEstimatedEarnInfo;
    }

    /**
     * Method to get product index
     *
     * @param product for which we need to get index
     * @return product index
     */
    private static int productIndex(Product product) {
        System.out.println("Looking for item: " + product);
        int index = -1;
        boolean found = false;
        for (Product p : getAllProductDetails()) {
            index++;
            if (product.id == p.id) {
                found = true;
                break;
            }
        }
        if (!found)
            Assert.fail("Unable to find an item that matches: " + product);
        return index;
    }

    /**
     * Method to get bag details from rest
     *
     * @return bag details
     */
    private static String getBagDetailsFromRest() {
        String userId = Cookies.getCookieValue(macys() ? "macys_online_uid" : "bloomingdales_online_uid");

        String url = "http://" + EnvironmentDetails.otherApp("msporder").ipAddress + ":8080/api/order/v1/bags?userId=" + userId;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        if (signedIn())
            httpGet.addHeader("X-Macys-SecurityToken", Cookies.getCookieValue("secure_user_token"));
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }

            reader.close();
            httpClient.close();
            return response.toString();
        } catch (IOException e) {
            Assert.fail("getBagDetailsFromRest(): " + e);
        }
        return "";
    }
}