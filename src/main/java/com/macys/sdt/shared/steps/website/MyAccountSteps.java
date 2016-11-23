package com.macys.sdt.shared.steps.website;


import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.*;
import com.macys.sdt.shared.actions.website.bcom.pages.LoyallistAssociation;
import com.macys.sdt.shared.actions.website.bcom.pages.LoyaltyEnrollment;
import com.macys.sdt.shared.actions.website.bcom.pages.my_account.MyAccountBCOM;
import com.macys.sdt.shared.actions.website.mcom.pages.my_account.*;
import com.macys.sdt.shared.utils.CommonUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class MyAccountSteps extends StepUtils {

    public boolean userProfileHasCheckoutEligibleAddress = false;
    private String simplePwd = "1234567";

    @When("^I navigate to My Wallet page from My Account page$")
    public void iNavigateToMyWalletPageFromMyAccountPage() throws Throwable {
        if (!onPage("my_account")) {
            iNavigateToMyAccountPage();
        }
        if (Elements.elementPresent("my_account.one_time_add_card_overlay")) {
            CreateProfile.closeSecurityAlertPopUp();
            Clicks.click("my_account.add_card_overlay_no_thanks_button");
        }
        Clicks.click("navigation.goto_my_wallet_link");

        if (bloomingdales() && Elements.elementPresent("my_account.new_offer_popup"))
            Clicks.click("my_account.close_new_offer_popup");
    }

    @And("^I remove an offer from my wallet$")
    public void iRemoveAnOfferFromMyWallet() throws Throwable {
        if (Elements.elementPresent("oc_my_wallet.deleteOffers")) {
            Clicks.clickRandomElement("oc_my_wallet.deleteOffers");
            Clicks.click("oc_my_wallet.yes_delete_offer");
        } else {
            System.out.println("No offers to delete");
        }
    }

    @And("^I click on add offer on wallet page$")
    public void iClickOnAddOfferOnWalletPage() throws Throwable {
        Clicks.click((macys() ? "oc_my_wallet" : "my_bwallet")+".add_offer_pass");
    }

    @When("^I provide an offer to my wallet$")
    public void iProvideAnOfferToMyWallet() throws Throwable {
        Clicks.click("my_account.goto_deals_promotions");
        if (Elements.elementPresent("my_offers.add_to_wallet")) {
            Clicks.click("my_offers.add_to_wallet");
        } else {
            System.out.println("No offers available to add to wallet");
        }
    }

    @When("^I add credit card to my wallet$")
    public void i_add_credit_card_to_my_wallet() throws Throwable {
        Clicks.click("oc_my_wallet.add_credit_card");
        Wait.untilElementPresent("oc_my_wallet.credit_card_overlay");
        CreditCard visa_card = TestUsers.getValidVisaCreditCard();
        String phoneNum = TestUsers.generateRandomPhoneNumber();
        DropDowns.selectByText("oc_my_wallet.credit_card_type", "Visa");
        TextBoxes.typeTextbox("oc_my_wallet.card_number", visa_card.getCardNumber());
        DropDowns.selectByText("oc_my_wallet.exp_month", visa_card.getExpiryMonthIndex() + " - " + visa_card.getExpiryMonth());
        DropDowns.selectByText("oc_my_wallet.exp_year", visa_card.getExpiryYear());
        Clicks.clickIfPresent("oc_my_wallet.use_my_shipping_address");
        TextBoxes.typeTextbox("oc_my_wallet.phone_area_code", phoneNum.substring(0, 3));
        TextBoxes.typeTextbox("oc_my_wallet.phone_ex_code", phoneNum.substring(3, 6));
        TextBoxes.typeTextbox("oc_my_wallet.phone_sub_code", phoneNum.substring(6));
        TextBoxes.typeTextbox("oc_my_wallet.credit_card_email", "test@macys.com");
        Clicks.click("oc_my_wallet.save_card");
        Wait.forPageReady();
    }

    @When("^I navigate to my account page$")
    public void iNavigateToMyAccountPage() throws Throwable {
        Wait.secondsUntilElementPresent("home.goto_my_account_link", 10);
        if (!onPage("my_account")) {
            if (ie()) {
                Clicks.click(Elements.element("home.goto_my_account_link"),
                        () -> Wait.untilElementPresentWithRefreshAndClick(
                                Elements.element("my_account.csr_add_card_to_my_account_button"),
                                Elements.element("home.goto_my_account_link")));

            } else {
                if (StepUtils.safari()) {
                    Utils.threadSleep(3000, null);
                }
                Clicks.click("home.goto_my_account_link");
            }
            if (Elements.elementPresent("my_account.one_time_add_card_overlay")) {
                CreateProfile.closeSecurityAlertPopUp();
                Clicks.click("my_account.add_card_overlay_no_thanks_button");
                Wait.untilElementNotPresent("my_account.one_time_add_card_overlay");
            }
            if (StepUtils.safari()) {
                Utils.threadSleep(3000, null);
            }
            shouldBeOnPage(signedIn() ? "my_account" : "sign_in");
        }
    }

    @And("^I navigate to my profile page$")
    public void iNavigateToMyProfilePage() throws Throwable {
        Clicks.click("my_account.goto_my_profile");
        if (safari())
            Wait.secondsUntilElementPresent("my_profile.verify_page",20);
        if (!onPage("my_profile")) {
            Assert.fail("Not navigated to the my profile page");
        }
    }

    @When("^I sign in to my existing profile$")
    public void iSignInToMyExistingProfile() throws Throwable {
        CommonUtils.signInOrCreateAccount();
        iNavigateToMyAccountPage();
    }

    @And("^I navigate to my profile$")
    public void iNavigateToMyProfile() throws Throwable {
        Clicks.click("my_account.my_profile");
    }

    @And("^I create a new profile$")
    public void iCreateANewProfile() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot create profiles in production");
        }

        TestUsers.clearCustomer();
        CreateProfile.createProfile(TestUsers.getCustomer(null));
        if (!prodEnv() && !onPage("my_account")) {
            Assert.fail("New Profile is not created");
        }
        Wait.forPageReady();
        CreateProfile.closeSecurityAlertPopUp();
        TestUsers.currentEmail = TestUsers.getCustomerInformation().getUser().getProfileAddress().getEmail();
        TestUsers.currentPassword = TestUsers.getCustomerInformation().getUser().getLoginCredentials().getPassword();
        if (Elements.elementPresent("my_account.one_time_add_card_overlay")) {
            CreateProfile.closeSecurityAlertPopUp();
            Clicks.click("my_account.add_card_overlay_no_thanks_button");
        }
        CreateProfile.closeSecurityAlertPopUp();
        Utils.threadSleep(9000, null);
    }
    @And("^I create a new profile with simple password$")
    public void createNewProfileWithSimplePwd()  throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot create profiles in production");
        }
        TestUsers.clearCustomer();

        UserProfile customer = TestUsers.getCustomer(null);
        User user = customer.getUser();
        user.getLoginCredentials().setPassword(simplePwd);
        CreateProfile.createProfile(customer);
        Wait.forPageReady();
    }

    @When("^I create a new profile and my age is less that 13 years$")
    public void createNewProfileWithAgeLessThanThirteen()  throws Throwable {
        String targetDOB = "-12-31";//signify Dec 31st.
        int yearsToDeduct = 13;
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot create profiles in production");
        }
        TestUsers.clearCustomer();

        UserProfile customer = TestUsers.getCustomer(null);
        User user = customer.getUser();

        //From current year, if we subtract yearsToDeduct, we will get target year.
        String targetYear = String.valueOf((Calendar.getInstance().get(Calendar.YEAR))-yearsToDeduct);
        targetDOB = targetYear + targetDOB;
        user.setDateOfBirth(targetDOB);
        CreateProfile.createProfile(customer);
        Wait.forPageReady();
    }

    @When("^I create a new profile with missing first_name, last_name, email and primary_phone_number$")
    public void createNewProfileWithMissingData()  throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot create profiles in production");
        }
        TestUsers.clearCustomer();

        UserProfile customer = TestUsers.getCustomer(null);
        User user = customer.getUser();
        ProfileAddress profileAddress = user.getProfileAddress();
        profileAddress.setFirstName("");
        profileAddress.setLastName("");
        profileAddress.setEmail("");
        /*
        Parameters:
        1. false - suggests to use valid dob.
        2. true - suggests to input EMPTY phone field so as to validate phone inline error msg.
         */
        CreateProfile.createProfile(customer, false, true);
        Wait.forPageReady();
    }

    @When("^I try to create a new account with invalid data$")
    public void createNewProfileWithInvalidData()  throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot create profiles in production");
        }
        TestUsers.clearCustomer();

        UserProfile customer = TestUsers.getCustomer(null);
        User user = customer.getUser();
        ProfileAddress profileAddress = user.getProfileAddress();
        profileAddress.setFirstName("10");
        profileAddress.setLastName("20");
        profileAddress.setEmail("davsin@gmailcom");
        /*
        Parameters:
        1. true  - suggests to use invalid dob.
        2. false - suggests not to input EMPTY phone field.
        3. true  - suggests not to input invalid/incomplete phone field.
         */
        //true parameter in below method suggests entering invalid date while creating new profile.
        CreateProfile.createProfile(customer, true, false, true);
        Wait.forPageReady();
    }

    @When("^I create a new account with all same digits for phone$")
    public void createNewProfileWithAllSameDigitsForPhone()  throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot create profiles in production");
        }
        TestUsers.clearCustomer();

        UserProfile customer = TestUsers.getCustomer(null);
        /*
        Parameters:
        1. true  - suggests to use invalid dob.
        2. false - suggests not to input EMPTY phone field.
        3. false - suggests not to input invalid/incomplete phone field.
        4. true  - suggests to input all same digits for phone.
         */
        CreateProfile.createProfile(customer, false, false, false, true);
        Wait.forPageReady();
    }

    @When("^I navigate to my plenti page$")
    public void iNavigateToMyPlentiPage() throws Throwable {
        Clicks.hoverForSelection("my_account.goto_my_account");
        Wait.untilElementPresent("my_account.goto_my_plenti");
        Clicks.click("my_account.goto_my_plenti");
        CreateProfile.closeSecurityAlertPopUp();
    }

    @When("^I click on the 'Join For Free' button$")
    public void iClickOnTheJoinForFreeButton() throws Throwable {
        Wait.untilElementPresent("usl_home.plenti_index_content");
        Clicks.click("usl_home.enroll_today_button");
    }

    @When("^I click again on the 'Join For Free' button$")
    public void iClickAgainOnTheJoinForFreeButton() throws Throwable {
        Assert.assertTrue("Unable to click on \"JOIN FOR FREE\" button", Clicks.clickWhenPresent("usl_home.join_free"));
    }

    @And("^I click on the 'Join Now' button$")
    public void iClickOnTheJoinNowButton() throws Throwable {
        Wait.untilElementPresent("usl_join_for_free.join_now");
        if (ie()) {
            Clicks.click(Elements.element("usl_join_for_free.join_now"),
                    () -> Wait.untilElementPresentWithRefreshAndClick(
                            Elements.element("usl_enrollment.continue_button"),
                            Elements.element("usl_join_for_free.join_now")));
        } else {
            if (firefox())
                Clicks.javascriptClick("usl_join_for_free.join_now");
            else
                Clicks.click("usl_join_for_free.join_now");
        }
    }

    @And("^I goto plenty enroll page directly")
    public void iGotoPlentyEnrollPageDirectly() throws Throwable  {
        Navigate.visit("usl_enrollment");
    }


    @When("^I click on the 'Cancel' button$")
    public void iClickOnTheCancelButton() throws Throwable {
        Wait.untilElementPresent("usl_enrollment.continue_button");
        Clicks.click("usl_enrollment.cancel_button");
    }

    @When("^I click on the 'YES, CANCEL' button$")
    public void iClickOnTheYESCANCELButton() throws Throwable {
        Wait.untilElementPresent("usl_cancel_dialog.cancel_dialog");
        Clicks.click("usl_cancel_dialog.yes_cancel_button");
    }

    @And("^I enter the 'Phone number'$")
    public void iEnterThePhoneNumber() throws Throwable {
        Wait.untilElementPresent("usl_enrollment.continue_button");
        String phone_no = TestUsers.generateRandomPhoneNumber();
        try {
            TextBoxes.typeTextbox("usl_enrollment.phone_number", phone_no);
            Clicks.click("usl_enrollment.continue_button");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            Assert.fail("Cannot continue:" + e);
        }
    }

    @When("^I opt for guest user enrollment from USL sign in page$")
    public void iOptForGuestUserEnrollmentFromUSLSignInPage() throws Throwable {
        try {
            Clicks.click(Elements.findElement(By.className("fsrCloseBtn")));
        } catch (NoSuchElementException e) {
            //ignore exception
        }
        USLEnrollment.enroll(TestUsers.getuslCustomer(null, "Profile_Creation"));
    }

    @When("^I sign out from my current profile$")
    public void iSignOutFromMyCurrentProfile() throws Throwable {
        Clicks.clickIfPresent(By.className("container-close"));
        Clicks.hoverForSelection("home.goto_my_account_link");
        Wait.secondsUntilElementPresent("header.goto_sign_out_link", 10);
        Clicks.click("header.goto_sign_out_link");
        if(safari())
            Wait.secondsUntilElementNotPresent("header.goto_sign_out_link", 10);

        closeBcomPopup();
    }

    @And("^I navigate to the loyalty enrollment page$")
    public void iNavigateToTheLoyaltyEnrollmentPage() throws Throwable {
        Clicks.click("loyalty_home.create_profile_enroll_button");
        shouldBeOnPage("loyalty_enrollment");
    }

    @When("^I navigate to the loyalty landing page as a \"([^\"]*)\" user$")
    public void iNavigateToTheLoyaltyLandingPageAsAUser(String user_type) throws Throwable {
        // Before landing to the Loyalty enrollment page check whether the loyalty account already associated to the signed in account
        if (signedIn() && Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            System.out.println("--> User is already enrolled in Loyalty!!");
            // No need to remove and create Loyalty, instead we can use the same.
//            click("my_account.view_my_loyalllist_account");
//            forPageReady();
//            shouldBeOnPage("loyallist_account_summary");
//            click("loyallist_account_summary.remove_button");
//            untilElementPresent("loyallist_account_summary.lty_account_panel");
//            click("loyallist_account_summary.remove_confirmation_btn");
//            shouldBeOnPage("loyalty_association");
        } else {
            Clicks.click("home.goto_loyallist");
            switch (user_type.toLowerCase()) {
                case "guest":
                    shouldBeOnPage("loyalty_home");
                    break;
                case "signed_in":
                    shouldBeOnPage("loyalty_enrollment");
                    break;
            }
        }
    }

    @When("^I navigate to the loyallist account association page$")
    public void iNavigateToTheLoyallistAccountAssociationPage() throws Throwable {
        // Before landing to the Loyalty association page check whether the loyalty account already associated to the signed in account
        if (Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            Clicks.click("my_account.view_my_loyalllist_account");
            shouldBeOnPage("loyallist_account_summary");
            Clicks.click("loyallist_account_summary.remove_button");
            Wait.untilElementPresent("loyallist_account_summary.lty_account_panel");
            Clicks.click("loyallist_account_summary.remove_confirmation_btn");
        } else {
            Clicks.clickWhenPresent("my_account.goto_my_loyallist");
        }
        Wait.untilElementPresent("loyalty_association.verify_page");
        shouldBeOnPage("loyalty_association");
    }

    @And("^I add fully_enrolled_usl id on my account page$")
    public void iAddFullyEnrolledUslIdOnMyAccountPage() throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("iAddFullyEnrolledUslIdOnMyAccountPage()");

        String plenti_id = TestUsers.getEnrolledUslId().getPlentiId();
        TextBoxes.typeTextbox("my_account.usl_id", plenti_id);
        Clicks.click("my_account.apply_usl_id_button");
        if (Elements.elementPresent("my_account.error_message")) {
            Assert.fail("ERROR - ENV: Unable to look up Plenti ID");
        }
        Wait.untilElementPresent("my_account.go_to_my_XXXXXX");
    }

    @And("^I remove USL ID from shipping and payment page$")
    public void iRemoveUSLIDFromShippingAndPaymentPage() throws Throwable {
        Wait.untilElementPresent("shipping_payment_signed_in.remove_usl_button");
        if (ie()) {
            Clicks.click(Elements.element("shipping_payment_signed_in.remove_usl_button"),
                    () -> Wait.untilElementPresentWithRefreshAndClick(
                            Elements.element("shipping_payment_signed_in.apply_usl_id_button"),
                            Elements.element("shipping_payment_signed_in.remove_usl_button")));
        } else {
            Clicks.click("shipping_payment_signed_in.remove_usl_button");
        }
        Wait.untilElementPresent("shipping_payment_signed_in.apply_usl_id_button");
    }

    @And("^I should be able to associate my account by loyallist number using \"([^\"]*)\" details$")
    public void iShouldBeAbleToAssociateMyAccountByLoyallistNumberUsingDetails(String loyallist_type) throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("iShouldBeAbleToAssociateMyAccountByLoyallistNumberUsingDetails()");

        LoyallistAssociation.loyaltyAssociation(TestUsers.getLoyallistInformation(loyallist_type));
        Wait.untilElementPresent("loyallist_account_summary.verify_page");
        shouldBeOnPage("loyallist_account_summary");
    }

    @Then("^I should be able to enroll in to the loyalty program as a \"([^\"]*)\" user$")
    public void iShouldBeAbleToEnrollInToTheLoyaltyProgramAsAUser(String user_type) throws Throwable {
        if (prodEnv())
            throw new Exceptions.ProductionException("iShouldBeAbleToEnrollInToTheLoyaltyProgramAsAUser()");
        String pageName = null;
        if (signedIn() && Elements.elementPresent("my_account.view_my_loyalllist_account")) {
            Clicks.click("my_account.view_my_loyalllist_account");
            Wait.forPageReady();
            System.out.println("--> User is already enrolled in Loyalty, navigating to loyalty_enrollment_confirmation!!");
            pageName = "loyallist_account_summary";
        } else {
            LoyaltyEnrollment enrollmentPage = new LoyaltyEnrollment();
            switch (user_type.toLowerCase()) {
                case "guest":
                    enrollmentPage.guestUserLoyaltyEnrollment(TestUsers.getCustomer(null));
                    break;
                case "signed_in":
                    enrollmentPage.signedInUserLoyaltyEnrollment(TestUsers.getCustomer(null));
                    break;
            }
            pageName = "loyalty_enrollment_confirmation";
        }
        if (!Elements.elementPresent(pageName + ".loyalty_number")) {
            Assert.fail("Loyalty Enrollment Confirmation Page Not Loaded Properly");
        } else {
            System.out.println("Loyalty Enrollment Confirmation Page Loaded Successfully!!!");
        }
    }

    @Then("^I verify the My Account Pages are rendered properly$")
    public void iVerifyTheMyAccountPagesAreRenderedProperly(List<String> pageNames) throws Throwable {
        MyAccount wmp = new MyAccount();
        for (String pageName : pageNames) {
            wmp.navigateToLeftNavigationPage(pageName);
            if (!wmp.navigatedToExpectedPage(pageName)) {
                Assert.fail("Not navigated to the " + pageName);
            }
        }
    }

    @Then("^I should see user logged in to account successfully$")
    public void iShouldSeeUserLoggedInToAccountSuccessfully() throws Throwable {
        if (!signedIn()) {
            Assert.fail("User is not logged into to the profile!!");
        }
    }

    @And("^I update profile details on my profile$")
    public void iUpdateAndOnMyProfile() throws Throwable {
        UserProfile customer = TestUsers.getCustomer(null);
        TextBoxes.typeTextbox("my_profile.verify_page", customer.getUser().getProfileAddress().getFirstName());
        TextBoxes.typeTextbox("my_profile.last_name", customer.getUser().getProfileAddress().getLastName());
        if(Elements.getText("my_profile.address_line_1").isEmpty()){
            User user = customer.getUser();
            ProfileAddress profileAddress = user.getProfileAddress();
            TextBoxes.typeTextbox("my_profile.address_line_1", profileAddress.getAddressLine1());
            TextBoxes.typeTextbox("my_profile.address_city", profileAddress.getCity());
            if(macys() || MEW())
                DropDowns.selectByText("my_profile.address_state", ((profileAddress.getState().length() == 2) ? StatesUtils.translateAbbreviation(profileAddress.getState()) : profileAddress.getState()));
            else{
                DropDowns.selectCustomText("create_profile.address_state_list", "create_profile.state_options", ((profileAddress.getState().length() == 2) ? StatesUtils.translateAbbreviation(profileAddress.getState()) : profileAddress.getState()));
                Clicks.clickIfPresent("my_profile.gender_female");
            }
            TextBoxes.typeTextbox("my_profile.address_zip_code", String.valueOf(profileAddress.getZipCode()));
            if(macys()){
                DropDowns.selectByText("my_profile.gender", user.getGender());
                DropDowns.selectByText("create_profile.security_question", user.getUserPasswordHint().getQuestion());
                TextBoxes.typeTextbox("create_profile.security_answer", user.getUserPasswordHint().getAnswer());
            }
        }
        Clicks.click("my_profile.update_profile_button");
        if (macys()) {
            if (!Elements.elementPresent("my_profile.update_message")) {
                Assert.fail("Profile not updated");
            }
        } else {
            if (safari()) {
                Wait.untilElementPresent("my_account.verify_page");
            }
            if (!onPage("my_account")) {
                Assert.fail("Could not update profile");
            }
        }
    }

    @Then("^I verify my profile is updated$")
    public void iVerifyMyProfileIsUpdated() throws Throwable {
        if (bloomingdales()) {
            Clicks.click("my_account.goto_my_profile");
        }
        UserProfile customer = TestUsers.getCustomer(null);
        String capturedFirstName = customer.getUser().getProfileAddress().getFirstName();
        String capturedLastName = customer.getUser().getProfileAddress().getLastName();
        try {
            String updatedFirstName = Elements.getElementAttribute("my_profile.verify_page", "value");
            String updatedLastName = Elements.getElementAttribute("my_profile.last_name", "value");
            if (!(capturedFirstName.equals(updatedFirstName) && capturedLastName.equals(updatedLastName))) {
                Assert.fail("Profile is not Updated");
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            Assert.fail("Cannot continue:" + e);
        }
    }

    @When("^I enroll into the USL program from loyalty home page$")
    public void iEnrollIntoTheUSLProgramFromLoyaltyHomePage() throws Throwable {
        // Now we have new USL home page in qa environment which is pointing to production, So we are directly visit USL sign in page instead of USL home.
//        iClickOnTheJoinForFreeButton();
//        iClickOnTheJoinNowButton();
        if (!signedIn()) {
            Wait.forPageReady();
            Wait.secondsUntilElementPresent("usl_sign_in.goto_create_profile", 30);
            Clicks.click("usl_sign_in.goto_create_profile");
        }
        TestUsers.clearCustomer();
        pausePageHangWatchDog();
        USLEnrollment.enroll(TestUsers.getuslCustomer(null, "Profile_Creation"));
        USLEnrollment.enrollStep1(TestUsers.getuslCustomer(null, "Profile_Creation"));
        USLEnrollment.completeEnrollment();
        USLEnrollment.linkCreditCardAndSetPreferences();
        Wait.forPageReady();
        resumePageHangWatchDog();
        shouldBeOnPage("usl_confirmation");
    }

    @Then("^I should see USL enrollment confirmation page$")
    public void iShouldSeeUSLEnrollmentConfirmationPage() throws Throwable {
        if (!onPage("usl_confirmation")) {
            Assert.fail("Not navigated to USL enrollment confirmation page");
        }
    }

    @Then("^I should see attributes on USL enrollment confirmation page:$")
    public void iShouldSeeAttributesOnUSLEnrollmentConfirmationPage(List<String> elements) throws Throwable {
        for (String el : elements) {
            el = "usl_confirmation." + el;
            if (!Elements.elementPresent(el)) {
                Assert.fail(el + "attribute is not displayed on USL enrollment confirmation page");
            }
        }
        int lengthOfUslId = Elements.getText("usl_confirmation.usl_id").length();
        if (lengthOfUslId != 16) {
            Assert.fail("Length of USL ID on USL enrollment confirmation page is not equal to 16");
        }
    }

    @When("^I navigate to USL account summary page$")
    public void iNavigateToUSLAccountSummaryPage() throws Throwable {
        Clicks.click("my_account.goto_my_plenti");
        if (safari())
            Wait.secondsUntilElementPresent("usl_account_summary.verify_page", 20);
        if (!onPage("usl_account_summary")) {
            Assert.fail("Not navigated to the USL account summary page");
        }
    }

    @Then("^I should see USL basic attributes on USL account summary page$")
    public void iShouldSeeUSLBasicAttributesOnUSLAccountSummaryPage() throws Throwable {
        USLAccountSummary.verifyUSLAccountSummaryPageInformation("fully enrolled", null);
    }

    @Then("^I should not see credit card section on create profile page$")
    public void iShouldNotSeeCreditCardSectionOnCreateProfilePage() throws Throwable {
        if (Elements.elementPresent("create_profile.add_card_number") && Elements.elementPresent("create_profile.ssn_number")) {
            Assert.fail("Credit card section is displayed on create profile page!!");
        }
    }

    @And("^I create a new profile without closing the add card overlay$")
    public void iCreateANewProfileWithoutClosingTheAddCardOverlay() throws Throwable {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("iCreateANewProfileWithoutClosingTheAddCardOverlay()");
        }
        TestUsers.clearCustomer();
        Clicks.click("home.goto_my_account_link");
        if(safari())
            Wait.secondsUntilElementPresent("sign_in.create_profile", 10);
        Clicks.click("sign_in.create_profile");
        CreateProfile.createProfile(TestUsers.getCustomer(null));
        if(safari())
            Wait.secondsUntilElementPresent("my_account.verify_page", 10);
        if (!prodEnv() && !onPage("my_account")) {
            Assert.fail("New Profile is not created");
        }
        Wait.forPageReady();
        CreateProfile.closeSecurityAlertPopUp();
        TestUsers.currentEmail = TestUsers.getCustomerInformation().getUser().getProfileAddress().getEmail();
        TestUsers.currentPassword = TestUsers.getCustomerInformation().getUser().getLoginCredentials().getPassword();
        if (Elements.elementPresent("my_account.one_time_add_card_overlay")) {
            CreateProfile.closeSecurityAlertPopUp();
        }
        CreateProfile.closeSecurityAlertPopUp();
        Utils.threadSleep(9000, null);
    }

    @Then("^I (should|should not) see one time add card overlay and its components$")
    public void iShouldSeeOneTimeAddCardOverlayAndItsComponents(String condition) throws Throwable {
        String add_card_elements[] = {"one_time_add_card_overlay", "add_card_overlay_add_card_button", "add_card_overlay_close_button", "add_card_overlay_apply_today_link"};
        pausePageHangWatchDog();
        Wait.secondsUntilElementPresent("my_account.one_time_add_card_overlay", 5);
        if (condition.equals("should"))
            for (String element : add_card_elements)
                Assert.assertTrue(element + " element is not displayed on add_card_overlay!!", Elements.elementPresent("my_account." + element));
        else
            Assert.assertFalse("Add credit card overlay is displayed on my account page!!", Elements.elementPresent("my_account.one_time_add_card_overlay"));

        resumePageHangWatchDog();
    }

    @When("^I select \"([^\"]*)\" on add credit card overlay$")
    public void iSelectFieldOnAddCreditCardOverlay(String element_name) throws Throwable {
        pausePageHangWatchDog();
        Wait.untilElementPresent("my_account." + element_name);
        Clicks.click("my_account." + element_name);
        resumePageHangWatchDog();
    }

    @Then("^I should be redirected to \"([^\"]*)\" page$")
    public void iShouldBeRedirectedToExpectedPage(String expected_page) throws Throwable {
        if (!onPage(expected_page)) {
            Assert.fail("User is not redirected to " + expected_page + " page");
        }
    }

    @Then("^I should see below fields on credit service gateway page:$")
    public void iShouldSeeBelowFieldsOnCreditServiceGatewayPage(List<String> elements) throws Throwable {
        String page = "credit_service_gateway_" + (signedIn() ? "signedin" : "guest");
        Clicks.click(page + ".other_ways_to_pay_link");
        elements.forEach(element ->
                Assert.assertTrue(element + " is not displayed on credit service gateway guest page",
                        Elements.elementPresent(page + "." + element)));
    }

    @Then("^I should see below footer credit links:$")
    public void iShouldSeeBelowFooterCreditLinks(List<HashMap<String, String>> credit_footer_links) throws Throwable {
        ArrayList<String> failed_elements = new ArrayList<>();
        for (Map set : credit_footer_links) {
            try {
                if (!Elements.elementPresent("home." + set.get("credit_link"))) {
                    failed_elements.add("home." + set.get("credit_link"));
                }
            } catch (NoSuchElementException e) {
                System.out.println(set.get("credit_link").toString() + " element not present!!");
            }
        }
        if (failed_elements.size() > 0) {
            Assert.fail("Following Elements are not displayed in footer section:" + failed_elements.toString() + "!!");
        }
    }

    @Then("^I should be navigated to below respective credit services pages:$")
    public void iShouldBeNavigatedToBelowRespectiveCreditServicesPages(List<HashMap<String, String>> credit_footer_links) throws Throwable {
        ArrayList<String> failed_elements = new ArrayList<>();
        Assert.assertFalse("ERROR - ENV : CITI Credit services are down!!", Wait.untilElementPresent("credit_service_gateway_signedin.credit_services_down_message"));
        for (Map set : credit_footer_links) {
            Clicks.click("home." + set.get("credit_link"));
            if (safari()) {
                if (Elements.getValues(set.get("landing_page").toString() + ".verify_page").isEmpty())
                    Wait.secondsUntilElementPresent(set.get("landing_page").toString() + ".verify_page", 20);
                else
                    Utils.threadSleep(5000, "Waiting for page (without verify_page element) to load..");
            }
            try {
                if (!onPage(set.get("landing_page").toString())) {
                    failed_elements.add(set.get("landing_page").toString());
                }
            } catch (NoSuchElementException e) {
                System.out.println(set.get("landing_page") + " page not displayed!!");
            } finally {
                Navigate.browserBack();
            }
        }
        if (failed_elements.size() > 0) {
            Assert.fail("Following pages are not displayed:" + failed_elements.toString() + "!!");
        }
    }

    @Then("^I should be navigated to below citi pages from citi gateway page:$")
    public void iShouldBeNavigatedToBelowCitiPagesFromCitiGatewayPage(List<String> citiPages) throws Throwable {
        ArrayList<String> failedPagesList = new ArrayList<>();
        Assert.assertFalse("ERROR - ENV : CITI Credit services are down!!", Wait.untilElementPresent("credit_service_gateway_signedin.credit_services_down_message"));
        if(safari())
            Wait.secondsUntilElementPresent("credit_service_gateway_signedin.verify_page", 20);
        Assert.assertTrue("credit services page is not displayed!!", onPage("credit_service_gateway_signedin"));
        for (String pageName : citiPages) {
            String elementName = (pageName.equals("apply_credit_card") ? "apply_now_button" : (pageName.equals("fusion_activate_card") ? "activate_card" : "add_card_button"));
            if (Elements.elementPresent("credit_service_gateway_signedin." + elementName))
                Clicks.click("credit_service_gateway_signedin." + elementName);
            else
                Assert.fail(elementName + " is not found in the credit services page");
            Wait.secondsUntilElementNotPresent(By.className("loading"), 20);
            Wait.forPageReady();
            Wait.secondsUntilElementPresent("credit_service_gateway_signedin.speed_bump_continue_button", (safari() ? 20 : 2));
            if(Elements.elementPresent("credit_service_gateway_signedin.speed_bump_continue_button"))
                Clicks.click("credit_service_gateway_signedin.speed_bump_continue_button");
            try {
                Wait.secondsUntilElementPresent((pageName + ".verify_page"), (safari() ? 20 : 15));
                // Added run after navigation as we are redirecting to citi site which takes more time to redirect.
                Navigate.runAfterNavigation();
                if (!onPage(pageName))
                    failedPagesList.add(pageName);
            } catch (NoSuchElementException e) {
                System.out.println(pageName + " page not displayed!!");
            }
            Navigate.browserBack();
            if (safari() || ie())
                new PageNavigation().I_navigate_to_the_page_from_footer("credit services");
        }
        if (failedPagesList.size() > 0)
            Assert.fail("Following pages are not displayed:" + failedPagesList.toString() + "!!");
    }

    @And("^I clear all the cookies$")
    public static void iClearAllTheCookies() throws Throwable {
        Cookies.deleteAllCookiesJavascript();
        Navigate.browserRefresh();
    }

    @Then("^I verify the ability to cancel the order in order details page$")
    public void iVerifyTheAbilityToCancelTheOrderInOrderDetailsPage() throws Throwable {
        Wait.secondsUntilElementPresent("order_status.cancel_order_button", 5);
        Clicks.click("order_status.cancel_order_button");
        if(safari())
            Wait.secondsUntilElementPresent("order_status.order_cancel_yes_button", 10);
        Clicks.click("order_status.order_cancel_yes_button");
        Wait.secondsUntilElementNotPresent("order_status.order_cancel_yes_button", 10);
        String cancelText = macys() ? "canceled" : "CANCELLED";
        if (!Elements.getText("order_status.order_status_text").contains(cancelText) &&
                !Elements.getText("order_status.order_total_amount").replace("$", "").equals("0.00")) {
            Assert.fail("Order not cancelled successfully");
        }
    }

    @And("^I provide (valid|invalid) offer \"([^\"]*)\" to my wallet$")
    public void I_provide_valid_offer_to_my_wallet(String validity, String promo_code) throws Throwable {
        try {
            if (prodEnv()) {
                Wait.untilElementPresent("add_offer_dialog.promo_code");
                TextBoxes.typeTextbox("add_offer_dialog.promo_code", promo_code);
                Clicks.click("add_offer_dialog.save_offer");
            } else {
                new MyWalletSteps().I_saved_omnichannel_offer_having_more_than_one_promo_code_in_wallet();
            }
        } catch (NoSuchElementException e) {
            Assert.fail("Element is not visible on page: " + e);
        }
        if (validity.equals("valid"))
            Assert.assertTrue("ERROR-DATA: Not a valid promo code", Elements.elementPresent("oc_my_wallet.delete_offers"));
        else
            Assert.assertTrue("ERROR-DATA: Not an invalid promo code", Elements.elementPresent("add_offer_dialog.offer_error"));
    }

    @And("^I add checkout eligible address on my address book page$")
    public void iAddCheckoutEligibleAddressOnMyAddressBookPage() throws Throwable{
        iNavigateToMyAccountPage();
        // Add CC overlay will display to user only when user visit my account for first time.
        Clicks.clickIfPresent("my_account.add_card_overlay_no_thanks_button");
        Wait.forPageReady();
        new MyAccount().navigateToLeftNavigationPage("my address book");
        Wait.forPageReady();
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        if(MyAddressBook.isAddressAdded())
            new MyAddressBook().updateAddress(0, opts);
        else
            new MyAddressBook().addAddress(opts);
        System.out.println("-> Added Checkout eligible address in address book page!!");
    }

    @Given("^I visit the web site as a registered user with checkout eligible address$")
    public void iVisitTheWebSiteAsARegisteredUserWithCheckoutEligibleAddress() throws Throwable{
        new PageNavigation().I_visit_the_web_site_as_a_registered_user();
        if(!userProfileHasCheckoutEligibleAddress) {
            iAddCheckoutEligibleAddressOnMyAddressBookPage();
            userProfileHasCheckoutEligibleAddress = true;
        }
        iNavigateToMyAccountPage();
    }

    @And("^I click on \"([^\"]*)\" link in my account page$")
    public void I_click_on_link_in_my_account_page(String link) throws Throwable {
        new MyAccountBCOM().navigateToLeftNavigationPage(link);
        Wait.forPageReady();
    }

    @Then("^I navigate to top of the list page$")
    public void I_navigate_to_top_of_the_list_page() throws Throwable {
        By menuEl = Elements.paramElement("navigation.goto_top_of_the_list", "Top of the List");
        if (Elements.elementPresent(menuEl)) {
            Clicks.click(menuEl);
        } else {
            System.out.println("Unable to find top of the list page");
        }
        shouldBeOnPage("loyalty_benefits");
    }

    @And("^I select view all lists on my account page using mobile website$")
    public void iSelectViewAllListsOnMyAccountPage() throws Throwable {
        if (!onPage("my_account"))
            Navigate.visit("my_account");
        Clicks.click("my_account.my_lists");
        if (Elements.elementPresent("my_account.view_all_link"))
            Clicks.click("my_account.view_all_link");
    }

    @Then("^I should see date of birth auto-populated$")
    public void validateDateOfBirth(){
        Assert.assertTrue("Error - DOB entered while creating new profile do not match with DOB on My Profile page.",
                          new MyProfile().getDobFromMyProfilePage());
    }
}
