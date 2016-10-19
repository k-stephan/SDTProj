package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PageElement {
    public String elementKey = null;                                  // home.logo => home.logo or panel.home.logo
    public String pageName = null;                                    // home.logo => home
    public String pagePath = null;                                    // home.logo => website.mcom.page.home
    public String elementName = null;                                 // home.logo => logo
    public ArrayList<String> elementLocators = new ArrayList<>();     // home.logo => [id, cssSelector]
    public ArrayList<String> elementValues = new ArrayList<>();       // home.logo => [logoImage, div#newLogoImage]

    public PageElement(String stringName) {
        elementKey = stringName;

        parseKey();
        parseValue(PageUtils.getElementJSONValue(this));
    }

    public String getPageName() {
        return pageName;
    }

    public String getElementName() {
        return elementName;
    }

    public String getPagePath() {
        return pagePath;
    }

    public ArrayList<String> parseValue(String values) {
        if (values == null) {
            return elementValues;
        }

        // parse element String
        for (String value : values.split("\\|\\|")) {
            String[] parts = value.split(Pattern.quote(","));
            int count = parts.length;

            String locator = parts[0].trim();
            if (isValidLocatorStrategy(locator)) {
                elementLocators.add(locator);
                value = value.replace(locator + ",", "").trim();
            }
            // System.out.println("elementLocators = " + elementLocators);

            elementValues.add(value);
            // System.out.println("elementValues = " + elementValues);
        }
        return elementValues;
    }

    private boolean isValidLocatorStrategy(String strategy) {
        switch (strategy) {
            case "id":
            case "linkText":
            case "name":
            case "partialLinkText":
            case "tagName":
            case "xpath":
            case "className":
            case "class":
            case "cssSelector":
            case "UIAutomator":
                return true;
            default:
                return false;
        }
    }

    // separate page name and element name from the element key
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

    // make 'home' or 'panel.home' to 'website.mcom.page.home' or 'website.mcom.panel.home'
    public static String getPageFullPath(String pageName) {
        String pagePath;
        if (MainRunner.appTest) {
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
}
