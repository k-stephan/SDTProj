package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.db.models.PromotionService;
import com.macys.sdt.shared.actions.MEW.pages.ProductDisplay;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductDisplayPage extends StepUtils {

    public static String mew_promotional_prod_id = null;
    public static String mew_promotion_code = null;

    /**
     * Adds a product with the given attributes to bag and selects checkout if elected
     *
     * <p>
     * <b>Example: </b> I add an "orderable and available and bops_available" product to my bag using mobile website that is not "click_to_call" and checkout<br>
     * this would add a bops product that does not have the "click_to_call" attribute (of course those two wouldn't overlap anyway, but that's not the point)
     * </p>
     *
     * @param productTrue  Properties expected to be true separated by either a space or the word "and"
     * @param productFalse Properties expected to be false separated by either a space or the word "and"
     * @param checkout     present if you want to end up on shopping bag page
     * @throws Throwable if any exception occurs or product cannot be found
     */
    @When("^I add an? \"(.*?)\" product to my bag using mobile website(?: that is not(?: an?)? \"(.*?)\")?(?: and \"?(.*?)\"? ?checkout)?$")
    public void I_add_a_product_to_my_bag_using_mobile_website(String productTrue, String productFalse, String checkout) throws Throwable {
        if (prodEnv()) {
            CommonUtils.navigateToRandomProduct(productTrue, productFalse);
        } else {
            //All the none production related promotional products need to be get from siteDB with their respective attributes
            switch (productTrue) {
                case "orderable and promo_code_eligible":
                    String[] productsWithPromoCode = new PromotionService().getPromoCodeForPromotionalProducts();
                    if (productsWithPromoCode == null)
                        Assert.fail("ERROR-DATA: Promo-Code eligible promotions are not available in database!!");
                    mew_promotional_prod_id = productsWithPromoCode[1];
                    mew_promotion_code = productsWithPromoCode[0];
                    CommonUtils.navigateDirectlyToProduct(mew_promotional_prod_id);
                    break;
                case "orderable and pwp":
                    //TODO
                    break;
                default:
                    CommonUtils.navigateToRandomProduct(productTrue, productFalse);
            }
        }
        Clicks.clickIfPresent("product_display.no_thanks");
        I_add_product_to_my_bag_from_standard_PDP_Page_using_mobile_site();
        if (checkout != null) {
            if (onPage("add_to_bag")) {
                Clicks.click("add_to_bag.checkout");
                shouldBeOnPage("shopping_bag");
            }
        }
    }

    /**
     * Adds "Lenox Vintage Jewel 5 Piece Place Setting" (ID 86800) product to bag
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I add a predefined orderable product to my bag using mobile website$")
    public void I_select_a_predefined_orderable_random_member_product_using_mobile_website() throws Throwable {
        /*
              86800: Lenox Vintage Jewel 5 Piece Place Setting
         */
        String[] predefined_product_ids = {"86800"};

        Random rn = new Random();
        CommonUtils.navigateDirectlyToProduct(predefined_product_ids[rn.nextInt(predefined_product_ids.length)]);

        Clicks.clickIfPresent("product_display.no_thanks");
        I_add_product_to_my_bag_from_standard_PDP_Page_using_mobile_site();
        if (onPage("add_to_bag")) {
            Clicks.clickWhenPresent("add_to_bag.checkout");
            shouldBeOnPage("shopping_bag");
        }
    }

    /**
     * Adds product to bag from current PDP
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add product to my bag from standard PDP Page using mobile site$")
    public void I_add_product_to_my_bag_from_standard_PDP_Page_using_mobile_site() throws Throwable {
        for (int count = 0; count < 5; count++) {
            try {
                if (bloomingdales() && ProductDisplay.isMasterMemberPage()) {
                    Clicks.clickRandomElement("product_display_master.choose_member_items", WebElement::isDisplayed);
                }
                scrollToLazyLoadElement("product_display.add_to_bag_button");
                ProductDisplay.selectRandomColor();
                ProductDisplay.selectRandomSize();
                scrollToLazyLoadElement("product_display.add_to_bag_button");
                if (ProductDisplay.isMasterMemberPage()) {
                    Clicks.clickRandomElement("product_display.add_to_bag_button", WebElement::isDisplayed);
                } else {
                    //There are two element with same id. One for tablet and another for mobile. so had to filter out the displayed element and click in below step
                    Clicks.clickRandomElement("product_display.add_to_bag_button", WebElement::isDisplayed);
                }
                Clicks.clickIfPresent("product_display.technical_error");

                if (isErrorPaneVisible()) {
                    Clicks.click("home.popup_close");
                }
                Wait.untilElementPresent("add_to_bag.checkout");
                if (ProductDisplay.addedToBag())
                    return;
            } catch (IllegalArgumentException | NoSuchElementException e) {
                if (MainRunner.debugMode)   {
                    e.printStackTrace();
                }
                System.out.println("Failed to add product to bag, trying again. " + (4 - count) + " tries remaining.");
            } catch (Exception e)   {
                if (MainRunner.debugMode)   {
                    e.printStackTrace();
                }
            }
            Navigate.browserRefresh();
            closePopup();
        }
        if (!ProductDisplay.addedToBag())
            Assert.fail("Unable to add product to bag");
    }

    /**
     * Clicks on share button on PDP
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select share button on pdp page using mobile website$")
    public void I_select_share_button_on_pdp_page_using_mobile_website() throws Throwable {
        Assert.assertTrue("ERROR-ENV: Share button is not visible", Wait.untilElementPresent("product_display.share_button"));
        Clicks.click("product_display.share_button");
    }

    /**
     * Selects the given social icon on PDP
     *
     * @param social_icon "facebook", "twitter", "pinterest", "email" or "google_plus"
     * @throws Throwable if any exception occurs
     */
    @And("^I select \"([^\"]*)\" social icon on PDP Page using mobile website$")
    public void I_select_social_icon_on_PDP_Page_using_mobile_website(String social_icon) throws Throwable {
        if (Elements.elementPresent("product_display.share_button"))
            Clicks.click("product_display.share_button");
        switch (social_icon) {
            case "facebook":
                if (!Clicks.clickIfPresent("product_display.social_icon_facebook"))
                    Assert.fail(social_icon + " is not Available");
                break;
            case "twitter":
                if (!Clicks.clickIfPresent("product_display.social_icon_twitter"))
                    Assert.fail(social_icon + " is not Available");
                break;
            case "pinterest":
                if (!Clicks.clickIfPresent("product_display.social_icon_pinterest"))
                    Assert.fail(social_icon + " is not Available");
                break;
            case "email":
                if (!Clicks.clickIfPresent("product_display.social_icon_email"))
                    Assert.fail(social_icon + " is not Available");
                break;
            case "google_plus":
                if (!Clicks.clickIfPresent("product_display.social_icon_google_plus"))
                    Assert.fail(social_icon + " is not Available");
        }

        if (ie() && MainRunner.getWebDriver().getTitle().contains("Certificate Error:")) {
            Set<String> windowSet = MainRunner.getWebDriver().getWindowHandles();
            Assert.assertTrue("The popup window has not opened.", windowSet.size() > 1);
            Navigate.switchWindow(1);
            Navigate.execJavascript("document.getElementById('overridelink').click();");
        }
        Clicks.clickIfPresent("product_display.social_close");
    }

    /**
     * Verifies the expected title of a popup window for social icons
     *
     * @param social_icon_pop_up "Facebook", "twitter", "pinterest", "emailemail" or "google_plus"
     * @throws Throwable if any exception occurs
     */
    @Then("^I should see \"([^\"]*)\" title in the popup window using mobile website$")
    public void I_should_see_title_in_the_popup_window_using_mobile_website(String social_icon_pop_up) throws Throwable {
        int window_index = 0;
        switch (social_icon_pop_up) {
            case "Facebook":
                window_index = Navigate.findIndexOfWindow(social_icon_pop_up);
                break;
            case "twitter":
                window_index = Navigate.findIndexOfWindow("Share a link on Twitter");
                break;
            case "pinterest":
                window_index = Navigate.findIndexOfWindow("Pinterest");
                break;
            case "email":
                window_index = Elements.elementPresent("product_display.email_panel") ? 1 : 0;
                break;
            case "google_plus":
                window_index = Navigate.findIndexOfWindow("Google+");
                break;
        }
        if (window_index <= 0) {
            // email may come up in desktop client, which we can't detect automatically.
            if (social_icon_pop_up.equals("email"))
                System.out.println("email is not displayed. Check manually for desktop client.");
            else
                Assert.fail(social_icon_pop_up + " is not displayed");
        }
    }

    /**
     * Searches nearby stores for stock of the current product
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I search for a product in a nearby store using mobile website$")
    public void I_search_for_a_product_in_a_nearby_store_using_mobile_website() throws Throwable {
        Assert.assertTrue("ERROR-ENV: Unable to locate product in store", Elements.elementPresent("product_display.find_store_link"));
        Clicks.click("product_display.find_store_link");
        Assert.assertTrue("ERROR-ENV: Find in store overlay is not visible ", Elements.elementPresent("product_display.find_in_store_overlay"));
        if (macys())
            DropDowns.selectByText("change_pickup_store.search_distance", "Show stores within 100 miles");

        TextBoxes.typeTextbox("change_pickup_store.address_zip_code", "22102");
        Clicks.click("change_pickup_store.search_button");
    }

    /**
     * Verifies the member products are displayed on master PDP
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I should see member products listed in mobile website$")
    public void I_should_see_member_products_listed_in_mobile_website() throws Throwable {
        Assert.assertTrue("Member items not visible", Elements.anyPresent("product_display_master.member_prod_list"));
    }

    /**
     * Adds random member product to bag from master PDP and selects the opted button
     *
     * @param option "checkout" or "continue shopping"
     * @throws Throwable if any exception occurs
     */
    @And("^I add member product from PDP and select \"([^\"]*)\" using mobile website$")
    public void I_add_member_product_from_PDP_and_select_using_mobile_website(String option) throws Throwable {
        new ProductDisplay().addRandomMemberProductOnMasterPDP();
        Wait.secondsUntilElementPresent("add_to_bag.checkout", 25);
        if (option.equalsIgnoreCase("checkout")) {
            Clicks.click("add_to_bag.checkout");
            shouldBeOnPage("shopping_bag");
        } else if (option.equalsIgnoreCase("continue shopping")) {
            Clicks.click("add_to_bag.continue");
            shouldBeOnPage("product_display_master");
        }
    }

    /**
     * Selects random alternative image on PDP
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I select a random alternative image using mobile website$")
    public void I_select_a_random_alternative_image_using_mobile_website() throws Throwable {
        Assert.assertTrue("ERROR-DATA: Alternative images are not visible under the selected product", Wait.untilElementPresent("product_display.alt_images"));
        Clicks.clickRandomElement("product_display.alt_images"); //Should checkout selected alternate product
    }

    /**
     * Clicks on "Add to Wish List button" on PDP and waits for wish list overlay
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I click Add to Wish List button on PDP using mobile website$")
    public void I_click_add_to_Wish_List_button_on_PDP_using_mobile_website() throws Throwable {
        Clicks.click("product_display.add_to_wishlist");
        Assert.assertTrue("ERROR-ENV: Unable to navigate wish list overlay", Wait.secondsUntilElementPresent("product_display.wish_list_overlay", 10));
    }

    /**
     * Clicks on "view list" on Add to Wish List overlay
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I click on view list in ATW overlay from PDP using mobile website$")
    public void I_click_on_view_list_in_ATW_overlay_from_PDP_using_mobile_website() throws Throwable {
        Clicks.click("product_display.view_list");
        Assert.assertTrue("ERROR-ENV: Unable to navigate wish list page", onPage("wish_list"));
    }

    /**
     * Selects size and color on current PDP page
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I select product related attributes from PDP using mobile website$")
    public void I_select_product_related_attributes_from_PDP_using_mobile_website() throws Throwable {
        ProductDisplay.selectRandomColor();
        ProductDisplay.selectRandomSize();
    }

    /**
     * Adds a product with the given attributes to bag from BVR page and selects checkout if elected
     *
     * @param productTrue  Properties expected to be true separated by either a space or the word "and"
     * @param productFalse Properties expected to be false separated by either a space or the word "and"
     * @param checkout     present if you want to end up on shopping bag page
     * @throws Throwable if any exception occurs
     */
    @When("^^I add \"([^\"]*)\" product to my bag(?: that is not(?: an?)? \"(.*?)\")? from BVR page using mobile website(?: and \"?(.*?)\"? ?checkout)?$")
    public void I_add_a_product_to_registry_using_mobile_website(String productTrue, String productFalse, String checkout) throws Throwable {
        if (prodEnv()) {
            CommonUtils.navigateToRandomProduct(productTrue, productFalse);
        } else {
            //All the none production related promotional products need to be get from siteDB with their respective attributes
            switch (productTrue) {
                case "orderable and promo_code_eligible":
                    String[] productsWithPromoCode = new PromotionService().getPromoCodeForPromotionalProducts();
                    if (productsWithPromoCode == null)
                        Assert.fail("ERROR-DATA: Promo-Code eligible promotions are not available in database!!");
                    mew_promotional_prod_id = productsWithPromoCode[1];
                    mew_promotion_code = productsWithPromoCode[0];
                    CommonUtils.navigateDirectlyToProduct(mew_promotional_prod_id);
                    break;
                case "orderable and pwp":
                    //TODO
                    break;
                default:
                    CommonUtils.navigateToRandomProduct(productTrue, productFalse);
            }
        }
        I_add_product_to_my_registry_from_standard_PDP_Page_using_mobile_site();
        if (Elements.elementPresent("add_to_registry_overlay.add_to_registry_overlay"))
            Clicks.click("add_to_registry_overlay.view_registry");
        if (bloomingdales()) {
            Clicks.click("registry_bvr.category_header");
            Wait.untilElementPresent("registry_bvr.quantity");
            DropDowns.selectByValue("registry_bvr.quantity", "1");
        }
        Wait.untilElementPresent("registry_bvr.add_to_bag_btn");
        Clicks.click("registry_bvr.add_to_bag_btn");
        if (checkout != null) {
            Wait.untilElementPresent("registry_add_to_bag.registry_chkout_button");
            Clicks.click("registry_add_to_bag.registry_chkout_button");
            shouldBeOnPage("shopping_bag");

        }
    }

    /**
     * Adds a product to registry from current PDP
     *
     * @throws Throwable if any exception occurs
     */
    @And("^I add product to my registry from standard PDP Page using mobile site$")
    public void I_add_product_to_my_registry_from_standard_PDP_Page_using_mobile_site() throws Throwable {
        try {
//            for (int count = 0; count < 5; count++) {
                ProductDisplay.selectRandomColor();
                ProductDisplay.selectRandomSize();
                if (ProductDisplay.isMasterMemberPage()) {
                    Clicks.clickRandomElement("product_display.add_to_registry");
                } else {
                    Clicks.click("product_display.add_to_registry");
                }
                Clicks.clickIfPresent("product_display.technical_error");
                if (isErrorPaneVisible()) {
                    Clicks.click("home.popup_close");
                }
//            }
            Wait.forPageReady();
        } catch (IllegalArgumentException | NoSuchElementException e) {
            Assert.fail("Unable to add product to registry.");
        }
    }

    /**
     * Clicks on "continue shopping" button on add to bag page
     *
     * @throws Throwable if any exception occurs
     */
    @When("^I click on the continue shopping button from ATB page using mobile website$")
    public void iClickOnTheContinueShoppingButtonFromATBPageUsingMobileWebsite() throws Throwable {
        shouldBeOnPage("add_to_bag");
        Wait.secondsUntilElementPresent("add_to_bag.continue_shopping", 25);
        Clicks.click("add_to_bag.continue_shopping");
    }

    /**
     * Navigates directly to given product PDP from current PDP
     *
     * @param productID target product ID
     * @throws Throwable if any exception occurs
     */
    @When("^I replace product ID with available \"([^\"]*)\" product ID using mobile website$")
    public void iReplaceProductIDWithAvailableProductID(int productID) throws Throwable {
        shouldBeOnPage("product_display");
        try {
            String url = MainRunner.getCurrentUrl();
            String pattern = "ID=(.*)&";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(url);
            m.find();
            String new_url = url.replace(m.group(), "ID=" + productID + "&");
            Navigate.visit(new_url);
        } catch (Exception e) {
            Assert.fail("ERROR - DATA: Unable to append predefined prodID to URL" + e);
        }
    }

}
