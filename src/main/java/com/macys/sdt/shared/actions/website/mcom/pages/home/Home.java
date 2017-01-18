package com.macys.sdt.shared.actions.website.mcom.pages.home;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class Home extends StepUtils {

    public void selectMainCategory(String category) {
        if (safari() || ie())
            Elements.findElements(Elements.element("home.category")).stream().filter(el -> el.isDisplayed() && el.getText().equalsIgnoreCase(category)).findFirst().get().findElement(By.tagName("a")).click();
        else
            Clicks.clickElementByText(Elements.element("home.category"), category);
        try {
            Wait.untilElementPresent(Elements.element("category_splash.left_navigation_container"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectSubCategory(String subCategory) {
        if (safari())
            Elements.findElements(Elements.element("category_splash.subcategory")).stream().filter(el -> el.isDisplayed() && el.getText().equalsIgnoreCase(subCategory)).findFirst().get().click();
        else {
            if (macys())
                Clicks.clickElementByText("category_splash.subcategory", subCategory);
            else
                Clicks.clickElementByText("category_splash.subcategory", subCategory);
        }
        try {
            Wait.untilElementPresent(Elements.element("category_browse.facets_panel"));
        } catch (Exception e) {
            Assert.fail("ERROR - TIMEOUT: Could not browse to " + subCategory + ": " + e);
        }
    }

    public void selectRandomCategory() {
        List<String> categoryNames = getAllMainCategoryNames();
        selectMainCategory(categoryNames.get(new Random().nextInt(categoryNames.size())));
    }

    public void selectRandomSubCategory() {
        List<String> subCategoryNames = getAllSubCategoryNames();
        selectSubCategory(subCategoryNames.get(new Random().nextInt(subCategoryNames.size())));
    }

    public List<String> getAllMainCategoryNames() {
        return Elements.findElements(Elements.element("home.category")).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<String> getExpectedMainCategories(String mode) {
        if (macys()) {
            if (mode.equalsIgnoreCase("registry"))
                return Arrays.asList("WEDDING CREATE_REGISTRY", "DINING & ENTERTAINING", "KITCHEN", "BED & BATH", "HOME DECOR", "LUGGAGE", "CLEANING & ORGANIZING");
            else
                return Arrays.asList("HOME", "BED & BATH", "WOMEN", "MEN", "JUNIORS", "KIDS", "ACTIVE", "BEAUTY", "SHOES", "HANDBAGS", "JEWELRY", "WATCHES", "BRANDS");
        } else {
            if (mode.equalsIgnoreCase("registry"))
                return Arrays.asList("GETTING STARTED", "BRANDS", "DINING & ENTERTAINING", "KITCHEN", "BED & BATH", "HOME DECOR", "LUGGAGE", "CLEANING & ORGANIZING", "SALE");
            else
                return Arrays.asList("DESIGNERS", "WHAT'S NEW", "WOMEN", "SHOES", "HANDBAGS", "JEWELRY & ACCESSORIES", "BEAUTY", "MEN", "KIDS", "HOME", "GIFTS", "THE CREATE_REGISTRY", "SALE", "OUTLET");
        }
    }

    public List<String> getAllSubCategoryNames() {
        List<WebElement> linkCategories = macys() ? Elements.findElement(Elements.element("category_splash.left_navigation_container")).findElements(By.xpath("//*[@id=\"firstNavSubCat\"]/li//a"))
                : Elements.findElements(Elements.element("category_splash.subcategories"));
        return linkCategories.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
}
