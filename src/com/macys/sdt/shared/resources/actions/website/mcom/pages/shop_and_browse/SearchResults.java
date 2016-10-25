package com.macys.sdt.shared.resources.actions.website.mcom.pages.shop_and_browse;

import org.openqa.selenium.By;

public class SearchResults {

    public static By showItemsPerPage(String items) {
        return By.id(items + "_items");
    }

    public static By selectGridColumns(String cols) {
        return cols.equalsIgnoreCase("3") ? By.className("gridCol3") : By.className("gridCol4");
    }
}
