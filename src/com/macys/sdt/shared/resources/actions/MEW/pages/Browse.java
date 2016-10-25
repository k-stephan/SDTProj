package com.macys.sdt.shared.resources.actions.MEW.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;

public class Browse extends StepUtils {

    public void sortBy(String value) {
        if (Elements.elementPresent("category_browse.sort_by")) {
            Clicks.click("category_browse.sort_by");
            DropDowns.selectByText("category_browse.sort_by_select", value);
            Clicks.click("category_browse.apply");
            Wait.forLoading(By.id("loading_mask"));
        } else {
            Assert.fail("Unable to find required brand");
        }

    }
}
