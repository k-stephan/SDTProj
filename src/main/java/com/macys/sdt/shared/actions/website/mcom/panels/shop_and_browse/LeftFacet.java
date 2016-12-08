package com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import org.openqa.selenium.By;

public class LeftFacet extends StepUtils {

    public static int filterCount;
    public static String selectedFilter;

    public static void selectItemFromFacet(String toSelect, String facet) {
        expandFacet(facet);
        Clicks.clickElementByText(getFacetItems(facet), toSelect);
        if (bloomingdales()) {
            Clicks.javascriptClick(getFacetApply(facet));
            Wait.forLoading("left_facet.loading");
            Wait.forPageReady();
        }
        if (Elements.elementPresent("left_facet.loading"))
            Navigate.browserRefresh();

        // close technical popup error if it exists
        Clicks.clickIfPresent("category_browse.technical_error");

    }

    public static boolean isExpanded(String facet) {
        if (macys())
            return Elements.findElement(getFacetDiv(facet)).getAttribute("aria-expanded").equals("true");
        else
            return Elements.elementPresent(getFacetItems(facet));
    }

    public static void expandFacet(String facet) {
        if (!isExpanded(facet))
            Clicks.javascriptClick(getHeader(facet));
        if (facet.equalsIgnoreCase("size") && !Elements.elementPresent(getFacetItems(facet)))
            Elements.findElements("left_facet.expand_size_categories").forEach(Clicks::click);
    }

    public static void collapseFacet(String facet) {
        if (isExpanded(facet))
            Wait.untilElementPresent(getHeader(facet));
            Clicks.click(getHeader(facet));
    }

    public static By getHeader(String facet) {
        // sometimes the attribute we're checking is the exact header name
        // sometimes it's the header name in all caps with _ instead of space. Need to check.
        if (Elements.elementPresent(Elements.paramElement("left_facet.facet_header", facet)))
            return Elements.paramElement("left_facet.facet_header", facet);
        else
            return Elements.paramElement("left_facet.facet_header", fixIdentifier(facet));
    }

    public static By getFacetItems(String facet) {
        if (Elements.elementPresent(Elements.paramElement("left_facet.facet_items", facet)))
            return Elements.paramElement("left_facet.facet_items", facet);
        else
            return Elements.paramElement("left_facet.facet_items", fixIdentifier(facet));
    }

    public static By getFacetDiv(String facet) {
        if (Elements.elementPresent(Elements.paramElement("left_facet.facet_div", facet)))
            return Elements.paramElement("left_facet.facet_div", facet);
        else
            return Elements.paramElement("left_facet.facet_div", fixIdentifier(facet));
    }

    public static boolean facetPresent(String facet) {
        try {
            getFacetDiv(facet);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static By getFacetApply(String facet) {
        if (Elements.elementPresent(Elements.paramElement("left_facet.facet_apply", facet)))
            return Elements.paramElement("left_facet.facet_apply", facet);
        else
            return Elements.paramElement("left_facet.facet_apply", fixIdentifier(facet));
    }

    private static String fixIdentifier(String facet) {
        switch (facet.toLowerCase()) {
            case "gender":
                return "GENDER_AGE";
            case "more ways to shop":
                return "SHOWONLY";
            case "in-store pickup":
            case "pick up in-store":
                return "UPC_BOPS_PURCHASABLE";
            case "apparel type":
                return "MENS_PRODUCT_TYPE";
            case "size":
                return "SIZE_TYPE";
            case "color":
                return "COLOR_NORMAL";
            case "customers' top rated":
                return "CUSTRATINGS";
            case "designer":
                return "BRAND";
            case "sales & offers":
                return "SPECIAL_OFFERS";
            default:
                return facet.toUpperCase().replace(" ", "_");
        }
    }
}
