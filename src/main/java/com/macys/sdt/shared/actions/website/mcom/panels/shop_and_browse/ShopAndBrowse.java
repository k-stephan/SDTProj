package com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse;

import com.macys.sdt.framework.interactions.*;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.shared.utils.CommonUtils;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.NoSuchElementException;


public class ShopAndBrowse extends StepUtils {

    public void searchForAnItem(String item_name) {
        try {
            TextBoxes.typeTextbox("home.search_field", item_name);
            Clicks.click("home.search_button");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    public void navigateToRandomCategoryWithPopularSearchLink(int max_attempts) throws Exception {
        CommonUtils.retryAction(() -> {
            try {
                CommonUtils.navigateToRandomSubCategory();
            } catch (Exception e) {
                return false;
            }
            if (popularSearchLinksAreAvailable()) {
                selectOnePopularSearchLink();

                // Safari is not waiting for page load after clicking on PopularSearchLink
                if(safari()) {
                    Utils.threadSleep(1000, null);
                    Wait.forPageReady();
                }

                if (sortByAvailable() && paginationAvailable()) {
                    return true;
                }
            }
            return false;
        }, max_attempts, "ERROR - DATA: Popular search links page is not available in " + max_attempts + " attempts");
    }

    public void navigateToRandomCategoryBrowsePage(int max_attempts) throws Exception {
        for (int i = 0; i < max_attempts; i++) {
            try {
                CommonUtils.navigateToRandomSubCategory();
            } catch (Exception e) {
                //retry on exception
                if (MainRunner.debugMode) {
                    System.out.println("Number of Attempts for Browse Page is" + i);
                }
            }
            if (onPage("category_browse") || paginationAvailable() || sortByAvailable()) {
                return;
            }
        }
        Assert.fail("ERROR - DATA : Popular search links page is not available in " + max_attempts + " attempts");
    }

    public int getProductCount() {
        Wait.untilElementPresent("category_browse.product_count_span");
        String productCountText = Elements.getText("category_browse.product_count_span");
        Assert.assertFalse("ERROR - APP : Product count is not displayed on browse page!!", productCountText.isEmpty());
        return Integer.parseInt((macys() ? productCountText.trim() : productCountText.split(" ")[0]));
    }

    public int getPageCount() {
        int pageCount = 0;
        if (paginationAvailable()) {
            if (macys()) {
                List<WebElement> pageLinks = Elements.findElements("category_browse.goto_each_number_link");
                for (WebElement link : pageLinks) {
                    if (link.getText().matches("[0-9]+") && Integer.parseInt(link.getText()) > pageCount) {
                        pageCount = Integer.parseInt(link.getText());
                    }
                }
            } else {
                String currentPage = (Elements.getText("category_browse.goto_current_page_number"));
                pageCount = Integer.parseInt(currentPage.split(" of ")[1]);
            }
        }
        return pageCount;
    }

    public void gotoPageNumber(int number) {
        if (getPageCount() < number) {
            Assert.fail("ERROR APP: User con't navigate to " + number);
        }
        if (paginationAvailable()) {
            if (getCurrentPageNumber() == number) {
                System.out.println("User is already on same page !!");
            } else {
                findPageNumber(number);
                System.out.println("Navigated to " + number + " requested page");
            }
        }
    }

    public void findPageNumber(int number) {
        while (number != getCurrentPageNumber()) {
            if (getCurrentPageNumber() < number) {
                Clicks.click("category_browse.goto_next_page_via_arrow");
            } else {
                Clicks.click("category_browse.goto_previous_page_via_arrow");
            }
            Wait.untilElementPresent("category_browse.loading_mask");
            Wait.untilElementNotPresent("category_browse.loading_mask");
        }
    }

    public void sortByValue(String value) {
        if (bloomingdales())
            DropDowns.selectCustomText("category_browse.sort_by", "category_browse.sort_by_options", value);
        else
            DropDowns.selectByText("category_browse.sort_by", value);
        Wait.untilElementPresent("category_browse.loading_mask");
        Wait.untilElementNotPresent("category_browse.loading_mask");
    }

    public int getCurrentPageNumber() {
        String currentPage = (Elements.getText("category_browse.goto_current_page_number"));
        return Integer.parseInt((macys() ? currentPage : currentPage.split(" of ")[0]));
    }

    public boolean sortByAvailable() {
        return Elements.elementPresent("category_browse.sort_by");
    }

    public boolean paginationAvailable() {
        return Elements.elementPresent("category_browse.goto_current_page_number");
    }

    public void selectOnePopularSearchLink() {
        List<WebElement> seo_links = Elements.findElements("category_browse.seo_tag_links");
        for (WebElement link : seo_links) {
            if (link != null && (link.getAttribute("href").contains((macys() ? "/featured/" : "/buy/")))) {
                Clicks.click(link);
                return;
            }
        }
    }

    public boolean popularSearchLinksAreAvailable() {
        List<WebElement> seo_links = Elements.findElements("category_browse.seo_tag_links");
        for (WebElement element : seo_links) {
            if (element.getAttribute("href").contains((macys() ? "/featured/" : "buy"))) {
                return true;
            }
        }
        return false;
    }
}
