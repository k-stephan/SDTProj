package com.macys.sdt.shared.resources.actions.website.mcom.panels.shop_and_browse;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.interactions.Wait;

/**
 * Created by atepliashin on 9/30/16.
 */
public class ChangePreferredStore  {

    public static void setPreferredStore(String zipcode) {
        setPreferredStore(zipcode, 0);
    }

    public static void setPreferredStore(String zipcode, Integer number) {
        Wait.untilElementNotPresent("change_preferred_store.search_is_active");
        TextBoxes.typeTextbox("change_preferred_store.zipcode", zipcode);
        Clicks.click("change_preferred_store.search");
        Wait.untilElementPresent("change_preferred_store.make_this_my_store");
        Elements.findElements("change_preferred_store.make_this_my_store").get(number).click();
    }
}
