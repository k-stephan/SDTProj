package com.macys.sdt.shared.utils;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.Product;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.shared.actions.MEW.pages.CreateProfileMEW;
import com.macys.sdt.shared.actions.MEW.panels.GlobalNav;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.MyAccount;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.macys.sdt.framework.utils.TestUsers.getRandomProduct;
import static com.macys.sdt.framework.utils.TestUsers.getValidVisaCreditCard;
import static com.macys.sdt.framework.utils.Utils.getResourceFile;

public abstract class CommonUtils extends StepUtils {

    public static boolean isNewProfileCreated = false;

    public static void navigateDirectlyToProduct(String productID) {
        String product_url = MainRunner.url + "/shop/product/?ID=" + productID;
        Navigate.visit(product_url);
    }

    public static void navigateDirectlyToProduct(int id) {
        navigateDirectlyToProduct("" + id);
    }

    public static void selectRandomProduct(boolean hasRating, boolean master) {
        boolean done = false;
        int count = 0;
        String error = null;
        while (!done && ++count < 3) {
            try {
                if (onPage("category_browse") || onPage("search_result")) {
                    if (hasRating) {
                        selectMasterMemberProduct("search_result.product_thumbnail_with_review", master);
                    } else {
                        selectMasterMemberProduct("search_result.product_thumbnail_link", master);
                    }
                } else if (onPage("product_display")) {
                    if (!master) {
                        Clicks.clickRandomElement("product_display.member_product_link");
                    }
                } else {
                    Assert.fail("not on a page with products to select");
                }
                done = true;
            } catch (Exception e) {
                //try again!
                System.out.println("ShopAndBrowse.selectRandomProduct(): retrying:" + count);
                Utils.threadSleep(1000, null);
                error = e.toString();
            }
        }
        if (!done) {
            Assert.fail("Could not select product - " + error);
        }
    }

    public static void quickViewRandomProduct(boolean hasRating, boolean master) {
        boolean clickable = false;
        int count = 0;
        String error = null;
        while (!clickable && ++count < 3) {
            try {
                if (onPage("category_browse") || onPage("search_result")) {
                    if (hasRating) {
                        quickViewMasterMemberProduct("search_result.product_thumbnail_with_review", master);
                    } else {
                        quickViewMasterMemberProduct("search_result.product_thumbnail_link", master);
                    }
                } else if (onPage("product_display")) {
                    quickViewMasterMemberProduct("recommendations.recommended_products", master);
                } else {
                    Assert.fail("not on a page with products to select");
                }
                clickable = true;
            } catch (Exception e) {
                //try again!
                Navigate.browserRefresh();
                System.out.println("ShopAndBrowse.selectRandomProduct(): retrying:" + count);
                Utils.threadSleep(1000, null);
                error = e.toString();
            }
        }
        if (!clickable) {
            Assert.fail("Could not find clickable product - " + error);
        }
    }

    public static void quickViewMasterMemberProduct(String el, boolean master) {
        boolean found = false;
        List<WebElement> elements = Elements.findElements(el, element -> {
            WebElement price = element.findElement(By.xpath("../..")).findElement(By.cssSelector("div.prices"));
            return master && price.getText().contains(" - ");
        });
        WebElement selected = elements.get(new Random().nextInt(elements.size()));
        Clicks.hoverForSelection(selected);
        if (macys()) {
            if (Elements.elementPresent("search_result.product_thumbnail_quickview_tablet")) {
                Clicks.clickRandomElement("search_result.product_thumbnail_quickview_tablet");
            } else {
                WebElement thumbnail = Elements.getRandomElement("search_result.product_thumbnail");
                Clicks.hoverForSelection(thumbnail);
                if (!Wait.untilElementPresent("search_result.product_thumbnail_quickview")) {
                    Clicks.click(thumbnail);
                }
                Clicks.click("search_result.product_thumbnail_quickview");
                Clicks.clickIfPresent("quick_view.survey_close_button");
                Wait.untilElementPresent("quick_view.quick_view_product_dialog");
            }
            if (Elements.elementPresent("quick_view.product_error")) {
                Clicks.click("quick_view.quick_view_close_dialog");
            } else {
                found = true;
            }
        } else {
            Clicks.click("search_result.product_thumbnail_quickview");
            if (Elements.elementPresent("quick_view.product_error")) {
                Clicks.click("quick_view.quick_view_close_dialog");
            } else {
                found = true;
            }
        }
        if (!found) {
            Assert.fail("Unable to find " + (master ? "master" : "member") + " product on current page");
        }
    }

    public static void selectRandomProductMEW(boolean hasRating, boolean master) {
        boolean clickable = false;
        int count = 0;
        String error = null;
        boolean searchResults = Elements.elementPresent("search_result.search_results_thumbnail_wrapper");
        while (!clickable && count++ < 5) {
            try {
                selectMasterMemberProduct((searchResults ? "search_result" : "category_browse") // page name
                        + "." + "product_thumbnail_" + (hasRating ? "with_review" : "link"), master); // element name
                clickable = true;
                Navigate.browserRefresh();
                Wait.forPageReady();
            } catch (Exception e) {
                // check if it worked
                if (onPage("product_display")) {
                    return;
                }

                // try again!
                Navigate.browserRefresh();
                System.out.println("ShopAndBrowse.selectRandomProductMEW(): retrying:" + count);
                if (searchResults) {
                    Wait.untilElementPresent("search_result.product_thumbnail_link");
                } else {
                    Wait.untilElementPresent("category_browse.product_thumbnail_link");
                }
                error = e.toString();
            }
        }
        if (!clickable) {
            Assert.fail("Could not find clickable product - " + error);
        }
    }

    public static void selectMasterMemberProduct(String el, boolean master) {
        if (master) {
            Clicks.clickRandomElement(el, element -> {
                WebElement parent;
                WebElement price = null;
                if (MEW()) {
                    parent = element.findElement(By.xpath(".."));
                    try {
                        if (Elements.elementPresent("category_browse.browse_thumbnail_wrapper")) {
                            price = parent.findElement(By.cssSelector(".m-browse-price-regular.m-product-grid-price-regular"));
                        } else if (Elements.elementPresent("search_result.search_results_thumbnail_wrapper")) {
                            price = parent.findElement(By.cssSelector(".m-search-price-regular.m-product-grid-price-regular"));
                        }
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        return true;
                    }
                } else {
                    parent = element.findElement(By.xpath("../.."));
                    price = parent.findElement(By.cssSelector("div.prices"));
                }
                return price != null && price.getText().contains(" - ");
            });
        } else {
            try {
                Clicks.click(Elements.getRandomElement(el));
            } catch (org.openqa.selenium.interactions.MoveTargetOutOfBoundsException e) {
                // Sometimes thumbnail images are not clickable. Click product description to navigate to PDP.
                Clicks.clickRandomElement("product_thumbnails.product_short_description");
            }
            /*if (MEW()) {
                browserRefresh();
            }*/
        }
        // If we were looking for member but found master, navigate to one of the member products it contains.
        if (!master && isMasterProduct()) {
            Clicks.clickRandomElement("product_display.member_product_link");
            Clicks.click("quick_view.quick_view_close_dialog");
        }
    }

    public static boolean isMasterProduct() {
        return onPage("product_display") && Elements.elementPresent("product_display.member_product_list");
    }

    //sign in to a non-registry account
    public static void signInOrCreateAccount() {
        if (MEW()) {
            GlobalNav.openGlobalNav();
            GlobalNav.navigateOnGnByName("My Account");
            GlobalNav.closeGlobalNav();

        } else {
            String elementName = "home." + (macys() ? "goto_sign_in_link" : "goto_my_account_link");
            Wait.untilElementPresent(elementName);
            Clicks.click(elementName);
        }
        if (macys()) {
            closeIECertError();
        }
        if (prodEnv()) {
            try {
                UserProfile customer = TestUsers.getProdCustomer();
                TextBoxes.typeTextbox("sign_in.email", customer.getUser().getProfileAddress().getEmail());
                TextBoxes.typeTextbox("sign_in.password", customer.getUser().getLoginCredentials().getPassword());
                Clicks.click("sign_in.verify_page");
            } catch (Exceptions.UserException e) {
                Assert.fail(e.getMessage());
            }
            Assert.assertFalse("Prod account not recognized", Elements.elementPresent("sign_in.error_message"));
        } else {
            if (!onPage("my_account") && !signedIn()) {
                UserProfile customer = TestUsers.getCustomer(null);
                pausePageHangWatchDog();
                TextBoxes.typeTextbox("sign_in.email", customer.getUser().getProfileAddress().getEmail());
                TextBoxes.typeTextbox("sign_in.password", customer.getUser().getLoginCredentials().getPassword());
                Clicks.click("sign_in.verify_page");
                resumePageHangWatchDog();
                // if this account works, we're good to go
                if (!Wait.secondsUntilElementPresent("sign_in.error_message", safari() || ie() ? 40 : 10)) {
                    isNewProfileCreated = false;
                    return;
                }
                new MyAccount().setSecurityQuestion();
                if (MEW()) {
                    if (Elements.elementPresent("sign_in.verify_page") || Elements.elementPresent("sign_in.error_message")) {
                        Clicks.javascriptClick("sign_in.create_account");
                        CreateProfileMEW.createProfile(customer);
                    }
                } else {
                    if (Elements.elementPresent("sign_in.error_message") && MainRunner.getWebDriver().getTitle().contains("Sign In")) {
                        Clicks.click("sign_in.create_profile");
                        CreateProfile.createProfile(customer);
                    }
                    Wait.forPageReady();
                    Assert.assertTrue("New Profile could not be created", onPage("my_account") || onPage("my_profile"));
                    Clicks.clickIfPresent("my_account.add_card_overlay_no_thanks_button");
                    isNewProfileCreated = true;
                }
            }
        }
    }

    public static void navigateToRandomProduct() throws Exception {
        navigateToRandomSubCategory();
        selectRandomProduct(false, false);
    }

    public static void navigateToRandomCategory() throws Exception {
        Clicks.clickRandomElement("header.category", (el) -> el.isDisplayed() && !el.getText().matches("BRANDS|DESIGNERS|THE REGISTRY|GETTING STARTED"));
    }

    public static void navigateToRandomSubCategory() throws Exception {
        navigateToRandomCategory();
        while (!onPage("category_browse")) {
            Clicks.clickRandomElement("category_splash.subcategory", el -> el.isDisplayed() && !el.getText().matches("(.*?)(Brands|Impulse|DESIGNERS)(.*?)"));

            // Safari is not waiting for page load after clicking on subcategory
            if (safari()) {
                Utils.threadSleep(1000, null);
                Wait.forPageReady();
            }

            if (!onPage("category_browse") && !onPage("category_splash")) {
                Navigate.browserBack();
            }
        }
    }

    public static void scrollDownPageWhenSidebarPresent() {
        if (Elements.elementPresent("home.sidebar_iframe")) {
            Navigate.execJavascript("window.scrollTo(0, document.body.scrollHeight)");
        }
    }

    public static void addCreditCardFromBWallet(CreditCard creditCard, UserProfile customer) {
         if (creditCard == null) {
            creditCard = getValidVisaCreditCard();
        }
        if (customer == null) {
            customer = TestUsers.getCustomer(null);
        }

        int year = 5;
        int month = 4;

        if (MEW()) {
            Wait.untilElementPresent("oc_my_wallet.add_credit_card");
            try {
                Clicks.click("oc_my_wallet.add_credit_card");
            } catch (Exception | Error e) {
                Navigate.browserRefresh();
                Utils.threadSleep(2000, null);
                Wait.untilElementPresent("oc_my_wallet.add_credit_card");
                Clicks.click("oc_my_wallet.add_credit_card");
            }
            Wait.untilElementPresent("credit_card.card_type");
            DropDowns.selectByText("credit_card.card_type", creditCard.getCardType().name);
            TextBoxes.typeTextbox("credit_card.card_number", creditCard.getCardNumber());

            DropDowns.selectByIndex("credit_card.expiry_month", month);
            DropDowns.selectByIndex("credit_card.expiry_year", year);
            TextBoxes.typeTextbox("credit_card.first_name", customer.getUser().getProfileAddress().getFirstName());
            TextBoxes.typeTextbox("credit_card.last_name", customer.getUser().getProfileAddress().getLastName());
            TextBoxes.typeTextbox("credit_card.address_line_1", customer.getUser().getProfileAddress().getAddressLine1());
            TextBoxes.typeTextbox("credit_card.address_city", customer.getUser().getProfileAddress().getCity());
            if (bloomingdales()) {
                DropDowns.selectByText("credit_card.address_state", customer.getUser().getProfileAddress().getState());
            } else {
                DropDowns.selectByValue("credit_card.address_state", customer.getUser().getProfileAddress().getState());
            }
            TextBoxes.typeTextbox("credit_card.address_zip_code", String.valueOf(customer.getUser().getProfileAddress().getZipCode()));
            if (bloomingdales() || onPage("shipping_payment_signed_in")) {
                TextBoxes.typeTextbox("credit_card.card_phone_area_code", customer.getUser().getProfileAddress().getPhoneAreaCode());
                TextBoxes.typeTextbox("credit_card.card_phone_exchange", customer.getUser().getProfileAddress().getPhoneExchange());
                TextBoxes.typeTextbox("credit_card.card_phone_subscriber", customer.getUser().getProfileAddress().getPhoneSubscriber());
            } else {
                TextBoxes.typeTextbox("credit_card.phone_number", customer.getUser().getProfileAddress().getBestPhone());
            }
            TextBoxes.typeTextbox("credit_card.payment_email", customer.getUser().getProfileAddress().getEmail());
            Clicks.click("credit_card.save_card");
            Assert.assertTrue("ERROR-ENV: Unable to add a credit card successfully", Wait.untilElementPresent("credit_card.credit_card_section"));
        } else {
            Clicks.click("my_bwallet.add_credit_card_btn");
            if (!Elements.elementPresent("credit_card_dialog.card_number")) {
                Assert.fail("Add credit card dialog did not appear");
            }

            if (macys())  {
                DropDowns.selectByText("credit_card_dialog.card_type", creditCard.getCardType().name);
                DropDowns.selectByIndex("credit_card_dialog.expiry_month", month);
                DropDowns.selectByIndex("credit_card_dialog.expiry_year", year);
                DropDowns.selectByText("credit_card_dialog.address_state", customer.getUser().getProfileAddress().getState());
            } else {
                DropDowns.selectCustomText("credit_card_dialog.card_type", "credit_card_dialog.card_type_options",  creditCard.getCardType().name);
                DropDowns.selectCustomText("credit_card_dialog.address_state", "credit_card_dialog.address_state_options", customer.getUser().getProfileAddress().getState());
                DropDowns.selectCustomValue("credit_card_dialog.expiry_month", "credit_card_dialog.expiry_month_options", month);
                DropDowns.selectCustomText("credit_card_dialog.expiry_year", "credit_card_dialog.expiry_year_options", creditCard.getExpiryYear());
            }
            TextBoxes.typeTextbox("credit_card_dialog.card_number", creditCard.getCardNumber());
            TextBoxes.typeTextbox("credit_card_dialog.first_name", customer.getUser().getProfileAddress().getFirstName());
            TextBoxes.typeTextbox("credit_card_dialog.last_name", customer.getUser().getProfileAddress().getLastName());
            TextBoxes.typeTextbox("credit_card_dialog.address_line_1", customer.getUser().getProfileAddress().getAddressLine1());
            TextBoxes.typeTextbox("credit_card_dialog.address_city", customer.getUser().getProfileAddress().getCity());
            TextBoxes.typeTextbox("credit_card_dialog.address_zip_code", String.valueOf(customer.getUser().getProfileAddress().getZipCode()));
            TextBoxes.typeTextbox("credit_card_dialog.payment_email", customer.getUser().getProfileAddress().getEmail());
            TextBoxes.typeTextbox("credit_card_dialog.card_phone_area_code", customer.getUser().getProfileAddress().getPhoneAreaCode());
            TextBoxes.typeTextbox("credit_card_dialog.card_phone_exchange", customer.getUser().getProfileAddress().getPhoneExchange());
            TextBoxes.typeTextbox("credit_card_dialog.card_phone_subscriber", customer.getUser().getProfileAddress().getPhoneSubscriber());
            // click fails if it's not visible beforehand
            if (!Elements.elementPresent("credit_card_dialog.save_card")) {
                Clicks.hoverForSelection("credit_card_dialog.save_card");
            }
            Clicks.click("credit_card_dialog.save_card");
        }
    }

    /**
     * Checks if the test failed due to product unavailability or other checkout related environment issues
     */
    public static void checkProductUnavailability() {
        Utils.redirectSErr();
        if (onPage("responsive_checkout")) {
            if ((Elements.elementPresent("responsive_checkout.error_container")) && (Elements.elementPresent("responsive_checkout.item_level_error"))) {
                Utils.resetSErr();
                Assert.fail("ERROR - DATA: Unable to checkout your added product due to Product Unavailability Issue");
            }
            if (Elements.elementPresent("responsive_checkout.error_container")) {
                Utils.resetSErr();
                Assert.fail("ERROR - ENV: Unable to process your checkout. This may be due to an environment issue");
            }
        } else if (onPage("responsive_checkout_signed_in")) {
            if (Elements.elementPresent("responsive_checkout_signed_in.error_container") && Elements.elementPresent("responsive_checkout_signed_in.item_level_error")) {
                Utils.resetSErr();
                Assert.fail("ERROR - DATA: Unable to checkout your added product due to Product Unavailability Issue");
            }
            if (Elements.elementPresent("responsive_checkout_signed_in.error_container")) {
                Utils.resetSErr();
                Assert.fail("ERROR - ENV: Unable to process your checkout. This may be due to an environment issue");
            }
        } else if (onPage("shipping_payment_signed_in")) {
            if (Elements.elementPresent("shipping_payment_signed_in.error_container") && Elements.elementPresent("shipping_payment_signed_in.item_level_error")) {
                Utils.resetSErr();
                Assert.fail("ERROR - DATA: Unable to checkout your added product due to Product Unavailability Issue");
            }
            if (Elements.elementPresent("shipping_payment_signed_in.error_container")) {
                Utils.resetSErr();
                Assert.fail("ERROR - ENV: Unable to process your checkout. This may be due to an environment issue");
            }
        }
        Utils.resetSErr();
    }

    public static void closeUpdateBrowserPopup() {
        Clicks.clickIfPresent("home.updatebrowser_popup_close");
    }

    public static void closeStylistPopup() {
        if (Elements.elementPresent("home.sidebar_iframe")) {
            switchToFrame("home.sidebar_iframe");
            Clicks.clickIfPresent("home.minimize_connect_popup");
            Clicks.clickIfPresent("home.close_connect_popup");
            switchToFrame("default");
        }
    }

    /**
     * Method to return all contextual media information
     *
     * @return Contextual Media information
     */
    public static JSONObject getContextualizeMedia() {

        File queries = getResourceFile("contextualize_media.json");
        JSONObject jsonObject = null;

        try {
            String jsonTxt = Utils.readTextFile(queries);
            jsonObject = new JSONObject(jsonTxt);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    /**
     * Pulls attributes from an "and" separated list into a HashMap
     *
     * @param productTrue  List of attributes separated by "and"
     * @param productFalse List of attributes separated by "and"
     * @return HashMap containing values from true/false strings
     */
    public static HashMap<String, Boolean> extractOptions(String productTrue, String productFalse) {
        HashMap<String, Boolean> optionMap = new HashMap<>();
        if (productTrue != null) {
            for (String s : productTrue.split(" ")) {
                if (!s.equalsIgnoreCase("and")) {
                    optionMap.put(s, true);
                }
            }
        }
        if (productFalse != null) {
            for (String s : productFalse.split(" ")) {
                if (!s.equalsIgnoreCase("and")) {
                    optionMap.put(s, false);
                }
            }
        }
        return optionMap;
    }

    /**
     * Navigates to a random product having attirbutes in productTrue but not productFalse
     *
     * @param productTrue  List of attributes separated by "and"
     * @param productFalse List of attributes separated by "and"
     * @return The Product that was selected
     * @throws Exceptions.EnvException if not navigated to product display page
     */
    public static Product navigateToRandomProduct(String productTrue, String productFalse) throws Exceptions.EnvException {
        HashMap<String, Boolean> opts = CommonUtils.extractOptions(productTrue, productFalse);
        Product p = getRandomProduct(opts);
        Assert.assertNotNull("ERROR - DATA:  No " + productTrue + " product" + (productFalse != null ? " that is not " + productFalse : "") + " was found in products list.", p);
        CommonUtils.navigateDirectlyToProduct(p.id);
        Assert.assertFalse("ERROR - DATA: This product is no longer available!!", (macys() && Elements.elementPresent(By.className("prodNotavailableMsg"))));
        shouldBeOnPage("product_display");
        return p;
    }

    public static void closeIECertError() {
        if (ie() && MainRunner.getWebDriver().getTitle().contains("Certificate Error:")) {
            if (MainRunner.browserVersion.matches("11.0|11|10.0|10")) {
                MainRunner.getWebDriver().get("javascript:document.getElementById('overridelink').click();");
            }
        }
    }

    /**
     * Runs an action until either the action is successful or maxTries is reached.
     *
     * @param action   action to be performed (lambda with no args and boolean return value)
     * @param maxTries maximum number of tries - 0 for default (5)
     * @param message  error message to print on failure
     */
    public static void retryAction(BooleanSupplier action, int maxTries, String message) {
        maxTries = maxTries <= 0 ? 5 : maxTries;
        int i;
        for (i = 0; i < maxTries; i++) {
            if (action.getAsBoolean()) {
                break;
            }
        }
        if (i == maxTries) {
            Assert.fail(message != null ? message : "");
        }
    }

    /**
     * Checks for sort by options similar to the option you're looking for
     * <p>
     * Example1: Feature file has "Price: Low to High", where as in Website, it is listed as "Price: (low to high)"
     * Example2: Feature file has "Price: Customer Top Rated", where as in Website, it is listed as "Customers' Top Rated"
     * This method is to find the match with matching regex and returns boolean value based on the match found
     * </p>
     *
     * @param sortByOptions List of all available options
     * @param listOption Option you're looking for
     * @return true if a similar option exists in the list
     */
    public static boolean matchSimilarSortBy(List<String> sortByOptions, String listOption) {
        for (String sortOption : sortByOptions) {
            // So far seen four different combinations for price and customer options
            if (listOption.startsWith("Price")) {
                Pattern pricePattern = Pattern.compile("(?i)(.*Low(.*?)High*.)|(.*High(.*?)Low*.)");
                Matcher priceMatcher = pricePattern.matcher(sortOption);
                if (priceMatcher.find()) {
                    return true;
                }
            } else if (listOption.startsWith("Customer")) {
                Pattern customerPattern = Pattern.compile("(?i)(.*Customer(.*?)Rated*.)");
                Matcher customerMatcher = customerPattern.matcher(sortOption);
                if (customerMatcher.find()) {
                    return true;
                }
            } else {
                Pattern optionPattern = Pattern.compile(listOption, Pattern.CASE_INSENSITIVE);
                Matcher optionMatcher = optionPattern.matcher(sortOption);
                if (optionMatcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }
}