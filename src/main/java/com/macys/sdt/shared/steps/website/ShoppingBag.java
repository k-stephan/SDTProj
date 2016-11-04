package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import org.junit.Assert;
import com.macys.sdt.framework.utils.StepUtils;
import cucumber.api.java.en.Then;


public class ShoppingBag extends StepUtils {

    @Then("^I should see payment badge$")
    public void I_should_see_payment_badge() throws Throwable {

        if (!onPage("shopping_bag"))
            Navigate.visit("shopping_bag");
    	
        if (!Elements.elementPresent("shopping_bag.payment_badge")) {
            Assert.fail("Payment badge not displayed");
        }
    } 
    
    @Then("^I should see promo inline error message$")
    public void I_should_see_promo_inline_error_message() throws Throwable {
        Wait.untilElementPresent(Elements.element("shopping_bag.promo_inline_error_message"));
    }

    @Then("^I should see promo error message on the top of bag page$")
    public void I_should_see_promo_error_message_on_the_top_of_bag_page() throws Throwable {
        Wait.untilElementPresent(Elements.element("shopping_bag.error_message"));
    }
    
}
