package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.LeftFacet;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.macys.sdt.shared.steps.website.ShopAndBrowse.brandSearch;

public class Facets extends StepUtils {


    @When("^I search for zipcode \"([^\"]*)\" in bops change store dialog")
    public static void I_search_for_zipcode_in_bops_facet(String zipCode) throws Throwable {
        Wait.forPageReady();
        String panel = "change_pickup_store_dialog";
        if(MEW()){
            Wait.untilElementPresent(panel + ".address_zip_code");
            TextBoxes.typeTextNEnter(panel + ".address_zip_code", zipCode);
        }else {
            try {
                Wait.untilElementPresent(panel + ".address_zip_code");
                if (bloomingdales())
                    Wait.secondsUntilElementNotPresent(By.className("loading"), 50);
            } catch (ElementNotVisibleException | NoSuchElementException e) {
                Clicks.click(Elements.paramElement("category_browse.facet", "Pick Up In Store"));
            }

            if (!onPage("shopping_bag") && !Elements.elementPresent(panel + ".bops_facet_store_overlay")) {
                LeftFacet.expandFacet("Pick Up In-Store");
                Clicks.click("category_browse.bops_location");
                TextBoxes.typeTextbox(panel + ".address_zip_code", zipCode);

            } else {
                TextBoxes.typeTextbox(panel + ".address_zip_code", zipCode);
            }
            Clicks.click(panel + ".search_button");
        }
        if (macys())
            Wait.forLoading(By.id("loading_mask"));
        else
            Wait.secondsUntilElementNotPresent(By.className("loading"), 50);
    }

    @Then("^I should see \"([^\"]*)\" facet listed on left nav$")
    public void I_should_see_facet_listed_on_left_nav(String facet) throws Throwable {
        if (!Elements.elementPresent("left_facet.facet_div"))
            Navigate.browserRefresh();

        if (!LeftFacet.facetPresent(facet))
            Assert.fail("Failed to find facet: " + facet);
    }

    @Then("^I should be on \"([^\"]*)\" subsplash page$")
    public void i_should_be_on_subsplash_page(String arg1) throws Throwable {
        if(safari())
            Wait.secondsUntilElementPresent("category_sub_splash.verify_page", 20);
        shouldBeOnPage("category_sub_splash");
    }

    @When("^I select \"([^\"]*)\" in the subsplash page$")
    public void i_select_in_the_subsplash_page(String arg1) throws Throwable {
        Clicks.click(Elements.element("category_sub_splash.shop_makeup"));
    }

    @When("^I select \"([^\"]*)\" facet listed on left nav$")
    public void i_select_facet_listed_on_left_nav(String facet) throws Throwable {
        if(safari())
            Wait.secondsUntilElementPresent("category_browse.chanel_facet_links", 10);
        List<WebElement> linkCategories = Elements.findElements(Elements.element("category_browse.chanel_facet_links"));
        for (WebElement linkCat : linkCategories) {
            if (linkCat.getText().equalsIgnoreCase(facet)) {
                Clicks.click(linkCat);
                break;
            }
        }
        Wait.forPageReady();
    }

    @Then("^I should see store values in bops change store dialog")
    public void I_should_see_store_values_under_bops_facet() throws Throwable {
        Elements.elementShouldBePresent("change_pickup_store_dialog.bops_stores");
    }

    @And("^I should see radius dropdown in bops change store dialog")
    public void I_should_see_radius_dropdown_under_bops_facet() throws Throwable {
        if (!Elements.elementPresent("left_facet.bops_store_search_radius"))
            Assert.fail("bops radius not visible");
    }

    @And("^I select any bops store$")
    public void I_select_any_bops_store() throws Throwable {
        if (!Elements.elementPresent("left_facet.facet_div"))   {
            if (macys())  {
                Clicks.clickRandomElement(LeftFacet.getFacetItems("Pick Up In-Store"));
            } else {
                //This element clickable action for bcom
                Clicks.clickRandomElement(LeftFacet.getFacetItems("Pick Up In Store"));
            }
        }
        if (bloomingdales()) {
            Clicks.javascriptClick(LeftFacet.getFacetApply("Pick Up In Store"));
            Wait.forLoading("left_facet.loading");
            Wait.forPageReady();
        }  else {
            Clicks.clickRandomElement(Elements.element("left_facet.bops_stores"));
        }
    }

    @When("^I select \"([^\"]*)\" item from \"([^\"]*)\" facet on left nav$")
    public void I_select_item_from_facet_on_left_nav(String selected_item, String facet) throws Throwable {
        pausePageHangWatchDog();
        if (Elements.elementPresent(Elements.paramElement("left_facet.facet_div", facet))) {
            LeftFacet.selectItemFromFacet(selected_item, facet);
        } else if (Elements.elementPresent("facet_chart.facet_div")) {
            LeftFacet.selectItemFromFacet(selected_item, facet);
        }
    }

    @When("^I type a character \"([^\"]*)\" in brand search box$")
    public void I_type_a_character_in_brand_search_box(String search_text) throws Throwable {
        LeftFacet.expandFacet("Brand");
        TextBoxes.typeTextbox("search_result.brand_search", search_text);
        brandSearch = search_text;
    }

    @Then("^subfacet header \"([^\"]*)\" should be expanded under Brand facet$")
    public void subfacet_header_should_be_expanded_under_Brand_facet(String header) throws Throwable {
        if (header.equalsIgnoreCase("All Brands")) {
            String attr = Elements.getElementAttribute(Elements.element("search_result.allbrand_header"), "class");
            if (attr.contains("collapsed"))
                Wait.attributeChanged(Elements.element("search_result.allbrand_header"), "class", attr);
        }
    }

    @Then("^the \"([^\"]*)\" facet should be \"(collapsed|expanded)\" on left nav$")
    public void the_facet_should_be_on_left_nav(String facet, String state) throws Throwable {
        switch (state) {
            case "collapsed":
                if (LeftFacet.isExpanded(facet)) {
                    Assert.fail("Facet '" + facet + "' is not collapsed");
                }
                break;
            case "expanded":
                if (!LeftFacet.isExpanded(facet)) {
                    Assert.fail("Facet '" + facet + "' is not expanded");
                }
        }
    }

    @And("^I select any bops facet value$")
    public void I_select_any_bops_facet_value() throws Throwable {
        if (macys()) {
            Clicks.clickRandomElement(LeftFacet.getFacetItems("Pick Up In Store"));
        } else {
            LeftFacet.expandFacet("Pick Up In Store");
            Clicks.clickRandomElement(LeftFacet.getFacetItems("Pick Up In Store"));
        }

    }

    @When("^I open bops change store dialog through \"([^\"]*)\"$")
    public void I_open_bops_change_store_dialog_through(String facet) throws Throwable {
        LeftFacet.expandFacet(facet);
        Assert.assertTrue("Unable to locate bops location link", Wait.untilElementPresent("category_browse.bops_location"));
        I_should_see_radius_dropdown_under_bops_facet();
        I_select_from_Pick_Up_In_Store_facet_section("100 miles");
        Clicks.clickWhenPresent("category_browse.bops_location");
        Wait.untilElementPresent("left_facet.change_pickup_store_overlay");
    }

    @And("^I select \"([^\"]*)\" from Pick Up In-Store facet section$")
    public void I_select_from_Pick_Up_In_Store_facet_section(String distance) throws Throwable {
        DropDowns.selectByText(Elements.element("left_facet.bops_store_search_radius"), distance);
    }
}
