package com.macys.sdt.shared.actions.website.mcom.pages.my_account;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.utils.Exceptions;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

public class USLAccountSummary extends StepUtils {

    /**
     * Method to verify USL information displayed on USL account summary page
     *
     * @param status Expected USL status
     * @param enrollmentMessage Expected message after enrollment
     * @throws Exceptions.EnvException If expected elements are not present
     */
    public static void verifyUSLAccountSummaryPageInformation(String status, String enrollmentMessage) throws Exceptions.EnvException {
        try {
            String actualUslId = Elements.getText(Elements.element("usl_account_summary.added_usl_id"));
            String addedUslId = TestUsers.getEnrolledUslId().getPlentiId();
            String expectedUslId = StringUtils.overlay(addedUslId, StringUtils.repeat("*", addedUslId.length() - 4), 0, addedUslId.length() - 4);
            if (!actualUslId.equals(expectedUslId))
                Assert.fail("USL ID is not displayed correctly on USL account summary page");
        } catch (Exception e) {
            Assert.fail("verifyUSLAccountSummaryPageInformation(): " + e);
        }

        Elements.elementShouldBePresent("usl_account_summary.remove_usl_id");
        Elements.elementShouldBePresent("usl_account_summary.goto_faq");
        Elements.elementShouldBePresent("usl_account_summary.goto_learn_more");

        if (status.contains("cancelled")) {
            Elements.elementShouldBePresent("usl_account_summary.redeem_message");
            Elements.elementShouldBePresent("usl_account_summary.pending_points");
            Elements.elementShouldBePresent("usl_account_summary.available_points");

            String cancelMessage = Elements.getText(Elements.element("usl_account_summary.cancel_message")).replaceAll("\"", "");

            if (!cancelMessage.equals(enrollmentMessage))
                Assert.fail("Cancel message is not displayed correctly on USL account summary page");

            if (!Elements.elementPresent(Elements.element("usl_account_summary.disable_linking_section")))
                Assert.fail("Disable linking section is not displayed on USL enrollment confirmation page");

        } else {
            String actualRedeemMessage = Elements.getText(Elements.element("usl_account_summary.redeem_message"));
            String expectedRedeemMessage = TestUsers.getCustomerInformation().getUser().getProfileAddress().getFirstName() + " 's Plenti points balance:";

            if (!actualRedeemMessage.contains(expectedRedeemMessage))
                Assert.fail("Redeem message is not displayed correctly on USL account summary page");

            if (status.equals("fully enrolled")) {
                if(Elements.elementPresent("usl_account_summary.pre_enrolled_message"))
                    Assert.fail("pre_enrolled_message displayed on USL summary page");
                if(Elements.elementPresent("usl_account_summary.cancel_message"))
                    Assert.fail("cancel_message displayed on USL summary page");
                if(Elements.elementPresent("usl_account_summary.anonymous_message"))
                    Assert.fail("anonymous_message displayed on USL summary page");
            } else {
                String anonymousMessage = Elements.getText(Elements.element("usl_account_summary.cancel_message")).replaceAll("\"", "");
                if (!anonymousMessage.equals(enrollmentMessage))
                    Assert.fail("Anonymous message is not displayed correctly on USL account summary page");

                if (status.equals("anonymous")) {
                    if (!Elements.elementPresent(Elements.element("usl_account_summary.goto_anonymous_enrollment")))
                        Assert.fail("Anonymous enrollment link is not displayed on USL enrollment confirmation page");

                }
            }
        }
    }
}
