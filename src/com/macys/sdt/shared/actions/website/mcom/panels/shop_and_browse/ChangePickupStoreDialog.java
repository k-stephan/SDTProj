package com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;

/**
 * Created by atepliashin on 9/26/16.
 */
public class ChangePickupStoreDialog extends StepUtils {

    public static void searchStores(String searchPhrase) {
        Wait.untilElementPresent("change_pickup_store_dialog.address_zip_code");
        TextBoxes.typeTextbox("change_pickup_store_dialog.address_zip_code", searchPhrase);
        Clicks.click("change_pickup_store_dialog.search_button");
        Wait.untilElementPresent("change_pickup_store_dialog.search_results_section");
    }

    public static void selectFirstStore() {
        Clicks.click("change_pickup_store_dialog.select_store_button");
    }

    public static String getSearchFieldText() {
        return Elements.getElementAttribute("change_pickup_store_dialog.address_zip_code", "value");
    }
}
