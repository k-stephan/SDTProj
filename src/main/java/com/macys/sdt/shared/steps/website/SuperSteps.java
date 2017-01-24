package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.MEW.pages.CreateProfileMEW;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.CreateProfile;
import com.macys.sdt.shared.actions.website.mcom.pages.registry.CreateRegistry;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

import static com.macys.sdt.framework.utils.StepUtils.*;

public class SuperSteps {

    /**
     * Creates a new user account.
     * <p>
     * Cannot be used on production
     * </p>
     *
     * @throws Throwable if on prod or any exception occurs
     */
    @Then("^I create an account$")
    public void createAnAccount() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot Create New Accounts in Production");
        }
        UserProfile customer = TestUsers.getCustomer(null);
        if (MEW()) {
            if (Elements.elementPresent("sign_in.verify_page") || Elements.elementPresent("sign_in.error_message")) {
                Clicks.javascriptClick("sign_in.create_account");
                CreateProfileMEW.createProfile(customer);
            }
        } else {
            CreateProfile.createProfile(customer);
            Wait.forPageReady();
            Assert.assertTrue("New Profile could not be created", onPage("my_account") || onPage("my_profile"));
            Clicks.clickIfPresent("my_account.add_card_overlay_no_thanks_button");
        }
    }

    /**
     * Creates a new registry
     * <p>
     * Cannot be used on production
     * </p>
     *
     * @throws Throwable if on prod or any exception occurs
     */
    @And("^I create a new registry$")
    public void I_create_a_new_registry() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot Create New Registries in Production");
        }

        if (!onPage("create_registry", "new_create_registry")) {
            Navigate.visit("new_create_registry");
            if (!onPage("create_registry", "new_create_registry")) {
                Navigate.visit("create_registry");
            }
        }
        CreateRegistry.fillRegistryUserDetails(TestUsers.getNewRegistryUser());
    }

    /**
     * Adds a product with the given attributes to bag and selects checkout if elected
     * <p>
     * <p>
     * example: I add an "orderable and available and bops_available" product to my bag that is not "click_to_call" and checkout<br>
     * this would add a bops product that does not have the "click_to_call" attribute (of course those two wouldn't
     * overlap anyway, but that's not the point)
     * </p>
     *
     * @param productTrue  Properties expected to be true separated by either a space or the word "and"
     * @param productFalse Properties expected to be false separated by either a space or the word "and"
     * @param checkout     present if you want to end up on shopping bag page
     * @throws Throwable if any exception occurs or product cannot be found
     */
    @When("^I add an? \"(.*?)\" product to my bag(?: that is not(?: an?)? \"(.*?)\")?(?: and \"?(.*?)\"? ?checkout)?$")
    public void I_add_a_product_to_my_bag(String productTrue, String productFalse, String checkout) throws Throwable {
        ShopAndBrowse shop = new ShopAndBrowse();
        shop.iNavigateToPdp(productTrue, productFalse);
        shop.I_add_product_to_my_bag_from_standard_PDP_Page();

        if (checkout != null) {
            if (onPage("add_to_bag")) {
                Clicks.click("add_to_bag.checkout");
            } else if (Elements.elementPresent("add_to_bag_dialog.add_to_bag_checkout")) {
                Clicks.click("add_to_bag_dialog.add_to_bag_checkout");
            } else {
                Clicks.click("product_display.member_atb_checkout");
            }
        }
    }

}
