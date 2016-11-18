package com.macys.sdt.shared.steps.MEW;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.shared.actions.MEW.pages.PlentiEnroll;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

public class Plenti extends StepUtils {
    @And("^I navigate to my plenti page using mobile website$")
    public void I_navigate_to_my_plenti_page_using_mobile_website() throws Throwable {
        Wait.untilElementPresent("my_account.container");
        Wait.untilElementPresent("my_account.my_plenti");
        Assert.assertTrue("ERROR-ENV: Unable to locate my plenty option in my account page", Elements.elementPresent("my_account.my_plenti"));
        Clicks.click("my_account.my_plenti");
    }

    @And("^I click \"([^\"]*)\" button using mobile website$")
    public void I_click_button_using_mobile_website(String button) throws Throwable {
        if (onPage("plenti_summary")) {
            I_remove_the_plenti_points_from_profile();
            I_navigate_to_my_plenti_page_using_mobile_website();
        }

        switch (button.toLowerCase()) {
            case "join for free":
                Clicks.clickWhenPresent("plenti_home.learn_more");
                Wait.untilElementPresent("plenti_home.btn_join_free");
                Assert.assertTrue("ERROR-ENV: Unable to locate join for free element", !Elements.findElements("plenti_home.btn_join_free").isEmpty());
             //   Clicks.click(Elements.findElements("plenti_home.btn_join_free").get(0));
                break;
            case "join now":
                Navigate.visit("plenti_enroll"); //goto plenty enroll page directly
             //   Wait.untilElementPresent("plenti_join.btn_join_now");
             //   Assert.assertTrue("ERROR-ENV: Unable to locate join now element", Elements.elementPresent("plenti_join.btn_join_now"));
             //   Clicks.click("plenti_join.btn_join_now");
                break;
            case "enroll cancel":
                Wait.untilElementPresent("plenti_enroll.btn_cancel");
                Assert.assertTrue("ERROR-ENV: Unable to locate enroll cancel element", Elements.elementPresent("plenti_enroll.btn_cancel"));
                Clicks.click("plenti_enroll.btn_cancel");
                Assert.assertTrue("ERROR-ENV: Current environment is deviated in to a " + url() + " environment", MEW());
                break;
            case "yes, cancel":
                Wait.forPageReady();
                Wait.untilElementPresent("plenti_enrollment_cancel_confirm.cancel_overlay");
                Assert.assertTrue("ERROR-ENV: Unable to locate yes, cancel element", Elements.elementPresent("plenti_enrollment_cancel_confirm.cancel_overlay"));
                Clicks.click("plenti_enrollment_cancel_confirm.btn_yes_cancel");
                // Assert.assertTrue("ERROR-ENV: Current environment is deviated in to a " + url() + " environment", MEW());
                break;
            default:
                System.out.println("Invalid button: " + button);
        }
    }

    @And("^I opt for enrolment from plenti sign in page using mobile website$")
    public void I_opt_for_enrolment_from_plenti_sign_in_page_using_mobile_website() throws Throwable {
        PlentiEnroll.enroll(TestUsers.getuslCustomer(null, "Profile_Creation"));
    }

    @And("^I enter the plenti phone number using mobile website$")
    public void I_enter_the_plenti_phone_number_using_mobile_website() throws Throwable {
        TextBoxes.typeTextbox(Elements.element("plenti_enroll.phone_number"), TestUsers.getuslCustomer(null, "Profile_Creation").getUser().getProfileAddress().getBestPhone());
    }

    @And("^I remove the plenti points from profile$")
    public void I_remove_the_plenti_points_from_profile() throws Throwable  {
        Wait.untilElementPresent("plenti_summary.remove_usl_id");
        Clicks.click("plenti_summary.remove_usl_id");
        Clicks.clickWhenPresent("plenti_summary.confirm_delete_button");
        Wait.forPageReady();
        shouldBeOnPage("my_account");
    }

    @Then("^I should see USL basic attributes on plenti summary page$")
    public void I_should_see_USL_basic_attributes_on_plenti_summary_page() throws Throwable {
        String actualUslId = Elements.getText(Elements.element("plenti_summary.added_usl_id"));
        String addedUslId = TestUsers.getEnrolledUslId().getPlentiId();
        String expectedUslId = StringUtils.overlay(addedUslId, StringUtils.repeat("*", addedUslId.length() - 4), 0, addedUslId.length() - 4);
        Assert.assertTrue("USL ID is not displayed correctly on USL account summary page", actualUslId.equals(expectedUslId));

        Elements.elementShouldBePresent("plenti_summary.remove_usl_id");
        Elements.elementShouldBePresent("plenti_summary.goto_learn_more");
        Elements.elementShouldBePresent("plenti_summary.goto_faq");

        String actualRedeemMessage = Elements.getText(Elements.element("plenti_summary.redeem_message"));
        String expectedRedeemMessage = TestUsers.getCustomerInformation().getUser().getProfileAddress().getFirstName() + " 's Plenti points balance:";
        Assert.assertTrue("Redeem message is not displayed correctly on USL account summary page", actualRedeemMessage.equals(expectedRedeemMessage));
    }
}
