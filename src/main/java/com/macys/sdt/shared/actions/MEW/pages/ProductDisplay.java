package com.macys.sdt.shared.actions.MEW.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;


public class ProductDisplay extends StepUtils {

    /**
     * Close Technical error popup if present
     */
    public static void closeTechnicalErrorDialog() {
        // close technical popup error IF exists
        Clicks.clickIfPresent("product_display.technical_error");
    }

    /**
     * Select random color on PDP page, if color options are present
     */
    public static void selectRandomColor() {
        By colorEl = Elements.element("product_display.select_default_color");
        if (Wait.untilElementPresent(colorEl)) {
            Clicks.clickRandomElement(colorEl);
            closeTechnicalErrorDialog();
        } else {
            System.out.println("No coloring options found");
        }
    }

    /**
     * Select random size on PDP page, if size options are present
     */
    public static void selectRandomSize() {
        By sizeEl = Elements.element("product_display.select_default_size");
        if (Wait.untilElementPresent(sizeEl)) {
            Clicks.clickRandomElement(sizeEl);
            closeTechnicalErrorDialog();
            for (int i = 0; i < 10; i++) {
                if (!Elements.elementPresent("product_display.validation_error") && !Elements.elementPresent("product_display.out_of_stock") && Elements.elementPresent("product_display.size_selected"))
                    return;
                Clicks.clickRandomElement(sizeEl);
            }
            Assert.fail("Could not select valid size");
        } else if (Elements.elementPresent("product_display.select_size_dropdown")) {
            DropDowns.selectRandomValue("product_display.select_size_dropdown");
        } else {
            System.out.println("No sizing options found");
        }
    }

    /**
     * Checks if on master PDP page
     *
     * @return true if member products are present on PDP page, else false
     */
    public static boolean isMasterMemberPage() {
        return Elements.elementPresent("product_display_master.choose_member_items");
    }

    /**
     * Checks if product is successfully added to bag
     *
     * @return true if on add to bag page or dialog and, no selection and validation error are present, else false
     */
    public static boolean addedToBag() {
        Wait.forPageReady();
        return (onPage("add_to_bag")
                || Elements.elementPresent("add_to_bag.checkout")
                || Elements.elementPresent("add_to_bag.continue"))
                && !Elements.elementPresent("product_display.validation_error")
                && !Elements.elementPresent("product_display.selection_error");
    }

    /**
     * Add random member product to bag from master PDP
     */
    public void addRandomMemberProductOnMasterPDP() {
        try {
            if (bloomingdales()) {
                Clicks.randomJavascriptClick(Elements.element("product_display_master.choose_member_items"));
                Clicks.click("product_display_master.add_member_to_bag");
            } else {
                Clicks.clickRandomElement("product_display_master.add_member_to_bag");
            }
            Wait.secondsUntilElementNotPresent("product_display_master.add_member_to_bag", MainRunner.timeout);
        } catch (NoSuchElementException e) {
            Assert.fail("addRandomMemberProductOnMasterPDP(): Unable to select product");
        }
    }

    /**
     * Select given quantity of product on PDP
     *
     * @param quantity to select
     */
    public static void selectQuantity(String quantity) {
        if (Elements.elementPresent("product_display.select_size_dropdown")) {
            if (macys()) {
                //implement code
            } else {
                DropDowns.selectByText("product_display.quantity", quantity);
            }
        } else {
            System.out.println("Unable to find size option");
        }
    }
}
