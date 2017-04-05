package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.RunConfig;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PageElement {

    /**
     * example : home.logo = home.logo or panel.home.logo
     */
    public String elementKey = null;

    /**
     * example : home.logo = home
     */
    public String pageName = null;

    /**
     * example : home.logo = website.mcom.page.home
     */
    public String pagePath = null;

    /**
     * example : home.logo = logo
     */
    public String elementName = null;

    /**
     * example : home.logo = [id, cssSelector]
     */
    public ArrayList<String> elementLocators = new ArrayList<>();

    /**
     * example : home.logo = [logoImage, div#newLogoImage]
     */
    public ArrayList<String> elementValues = new ArrayList<>();

    /**
     * setup and read page element data
     *
     * @param stringName in format home.logo = home.logo or panel.home.logo
     */
    public PageElement(String stringName) {
        elementKey = stringName;

        parseKey();
        parseValue(PageUtils.getElementJSONValue(this));
    }

    //

    /**
     * make 'home' or 'panel.home' to 'website.mcom.page.home' or 'website.mcom.panel.home' based on current execution status & setup
     *
     * @param pageName name of page or panel
     * @return full path of page or panel
     */
    private static String getPageFullPath(String pageName) {
        String pagePath;
        if (RunConfig.appTest) {
            pagePath = StepUtils.iOS() ? "iOS." : "android.";
        } else {
            pagePath = StepUtils.MEW() ? "MEW." : "website.";
        }

        pagePath += (StepUtils.macys() ? "mcom." : (StepUtils.bloomingdales() ? "bcom." : "other."));

        if (pageName.contains("panel.")) {
            pagePath = pagePath + pageName;
        } else {
            pagePath = pagePath + "page." + pageName;
        }
        return pagePath;
    }

    /**
     * make 'MEW.mcom.page.responsive_page' to 'responsive.mcom.page.responsive_page'
     * or 'website.mcom.page.responsive_page' to 'responsive.mcom.page.responsive_page'
     *
     * @param pagePath path of page or panel
     * @return responsive path of the page or panel
     */
    public static String getResponsivePath(String pagePath) {
        if (!(pagePath.contains("website") || pagePath.contains("MEW"))) {
            return pagePath;
        }
        pagePath = pagePath.replaceFirst("MEW\\.", "responsive.");
        pagePath = pagePath.replaceFirst("website\\.", "responsive.");
        pagePath = pagePath.replaceFirst("MEW/", "responsive/");
        pagePath = pagePath.replaceFirst("website/", "responsive/");
        return pagePath;
    }

    /**
     * This return the JSON page name
     *
     * @return page name home.logo = home
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * This return element name in the JSON page
     *
     * @return element name home.logo = logo
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * This return path of the page
     *
     * @return page path home.logo = website.mcom.page.home
     */
    public String getPagePath() {
        return pagePath;
    }

    /**
     * parse the locators and values for a page element
     *
     * @param values values in format 'id, b_id || class,  b_class'
     * @return list of values (b_id, b_class)
     */
    public ArrayList<String> parseValue(String values) {
        if (values == null) {
            return elementValues;
        }

        // parse element String
        for (String value : values.split("\\|\\|")) {
            if (value.contains(",")) {
                String[] parts = value.split(Pattern.quote(","));

                String locator = parts[0].trim();

                // add only valid locator and corresponding value
                if (isValidLocatorStrategy(locator)) {
                    value = value.replace(locator + ",", "").trim();
                    // Link text has some extra space in edge browser. So changing linkText locator to partialLinkText
                    if (locator.contains("linkText") && RunConfig.browser.contains("edge"))
                        locator = "partialLinkText";
                    elementLocators.add(locator);
                    elementValues.add(value);
                } else {
                    System.err.println("wrong locator : " + locator);
                }
            } else {    // else use case example : url where no locator present
                elementValues.add(value);
            }
        }
        return elementValues;
    }

    /**
     * check for valid locator strategy
     *
     * @param strategy locator strategy eg. id, name
     * @return true on valid locator strategy
     */
    private boolean isValidLocatorStrategy(String strategy) {
        switch (strategy) {
            case "id":
            case "linkText":
            case "link text":
            case "name":
            case "partialLinkText":
            case "partial link text":
            case "tagName":
            case "tag name":
            case "xpath":
            case "className":
            case "class":
            case "class name":
            case "cssSelector":
            case "css selector":
            case "css":
            case "UIAutomator":
                return true;
            default:
                return false;
        }
    }

    /**
     *  separate page name and element name from the element key
     */
    private void parseKey() {
        // page process
        if (!elementKey.matches("(.*?).(.*?)")) {
            System.err.println("-->Error - UI: element name format is not correct:" + elementKey);
            Assert.fail();
        }

        String[] parts = elementKey.split(Pattern.quote("."));
        if (parts.length >= 2) {
            pageName = parts[parts.length - 2];
            elementName = parts[parts.length - 1];
            pagePath = getPageFullPath(pageName);
        } else {
            System.err.println("-->Error - UI: element name format is not correct:" + elementKey);
            Assert.fail();
        }
    }
}
