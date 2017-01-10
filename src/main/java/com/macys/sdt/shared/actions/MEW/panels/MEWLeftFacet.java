package com.macys.sdt.shared.actions.MEW.panels;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.Utils;
import org.openqa.selenium.By;

public class MEWLeftFacet extends StepUtils {

    public static void selectFacetOnLeftNav(String facet) {
        String facetFixed = fixFacetName(facet);
        Clicks.clickWhenPresent(Elements.paramElement("left_facet.select_facet", facetFixed));
        Wait.untilElementPresent("left_facet.sub_facet_container");
    }

    public static void selectSubFacetOnLeftNav(String subFacet) {
        if (subFacet.matches("^\\$[0-9][0-9][0-9]? - \\$[0-9][0-9][0-9]?")) {
            if(bloomingdales())
            {
                Clicks.clickElementByText("left_facet.select_price_sub_facet", subFacet);
            }
            else {
                String rangeStart = subFacet.substring(1, 3);
                Clicks.click(Elements.paramElement("left_facet.select_price_sub_facet", rangeStart));
            }
        } else {
            closePopup();
            Utils.threadSleep(1000, null);
            Wait.untilElementPresent(Elements.paramElement("left_facet.select_sub_facet", subFacet));
            Clicks.clickWhenPresent(Elements.paramElement("left_facet.select_sub_facet", subFacet));
        }
    }

    public static void selectSubFacetsFromLeftNav(String subFacet) {
        Clicks.clickElementByText("left_facet.select_sub_facets", subFacet);
    }

    public static void confirmFacets() {
        if(macys()) {
            Clicks.clickIfPresent("left_facet.done");
            Clicks.clickIfPresent("left_facet.apply");
        }
        else {
            Clicks.javascriptClick(Elements.findElement(By.cssSelector("button.btn.apply-facets-btn")));
        }
        closePopup();
        Wait.secondsUntilElementNotPresent("left_facet.apply", 50);
        //bcom mew facet panel collapse again after refresh
        //Navigate.browserRefresh();
    }

    private static String fixFacetName(String facet) {
        final String alt;
        switch (facet.toLowerCase()) {
            case "pick up in-store":
                alt = "UPC_BOPS_PURCHASABLE";
                break;
            case "size":
                alt = "SIZE_TYPE";
                break;
            case "color":
                alt = "COLOR_NORMAL";
                break;
            case "customers' top rated":
                alt = "CUSTRATINGS";
                break;
            case "performance features":
                alt = "FABRIC_PROPERTY";
                break;
            case "pattern":
                alt = "FABRIC_PATTERN";
                break;
            case "department":
                alt = "FOB";
                break;
            case "designer":
                alt = "BRAND";
                break;
            case "sales & offers":
                alt = "SPECIAL_OFFERS";
                break;
            default:
                alt = facet.toUpperCase().replaceAll(" ", "_").replaceAll("-", "_");
        }
        return alt;
    }

    public static void selectSizeFacetOnLeftNav(String sizeFacet) {
        //Temporary solution for size facet selection bcom_mew
        Elements.findElement(By.cssSelector(".facet-selection-items>li[data-facet-value$="+"\""+ sizeFacet +"\"")).click();
    }
}
