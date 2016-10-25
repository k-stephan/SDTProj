package com.macys.sdt.shared.steps.MEW;


import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Home extends StepUtils {
    public String searchTerm = null;

    @When("^I search using mobile website for \"([^\"]*)\"$")
    public void I_search_using_mobile_webitefor(String value) throws Throwable {
        Assert.assertTrue("ERROR-ENV: Search text field is not visible", Wait.untilElementPresent("home.search_field"));
        TextBoxes.typeTextNEnter("home.search_field", value);
        Wait.untilElementPresent("search_result.verify_page");
        shouldBeOnPage("search_result");
    }

    @When("^I type \"([^\"]*)\" in mew search box$")
    public void I_type_in_mew_search_box(String autokey) throws Throwable {
        searchTerm = autokey;
        Assert.assertTrue("ERROR-ENV: Search text field is not visible", Wait.untilElementPresent("home.search_field"));
        TextBoxes.typeTextbox("home.search_field", searchTerm);
    }

    @Then("^I select \"([^\"]*)\" from mew autocomplete suggestions$")
    public void I_select_from_mew_autocomplete_suggestions(String select_term) throws Throwable {
        Wait.untilElementPresent("header.search_suggestions");
        if (!Elements.elementPresent("header.search_suggestions_container"))
            I_type_in_mew_search_box(searchTerm);
        Clicks.clickElementByText("header.search_suggestions", select_term);
        Wait.untilElementPresent("search_result.verify_page");
        shouldBeOnPage("search_result");
    }

    @Then("^I should see \"([^\"]*)\" in mew autocomplete suggestions$")
    public void I_should_see_in_mew_autocomplete_suggestions(String text) throws Throwable {
        Wait.untilElementPresent("header.search_suggestions");
        List<WebElement> list = Elements.findElements("header.search_suggestions");
        if (list == null || list.size() == 0) {
            Assert.fail("Auto completion has no results");
        } else {
            list.forEach(el ->
                    Assert.assertTrue("Search word not found in auto complete",
                            el.getText().toLowerCase().contains(text.toLowerCase())));
        }
    }

    @Then("^I should not see mew autocomplete suggestions$")
    public void I_should_see_mew_autocomplete_suggestions() throws Throwable {
        if (Elements.elementPresent("header.search_suggestions")) {
            Assert.fail("Wedding registry should not have autocomplete results");
        }
    }
}

