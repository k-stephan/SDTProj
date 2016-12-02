package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class PageElementTest {

    @After
    public void restData() throws Exception {
        MainRunner.url = null;
        MainRunner.currentURL = "";
        MainRunner.brand = null;
        MainRunner.device = null;
        MainRunner.appTest = false;
    }

    @Test
    public void testGetPageName() throws Exception {
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("testPage", testElement.getPageName());
    }

    @Test
    public void testGetElementName() throws Exception {
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("testElement", testElement.getElementName());
    }

    @Test
    public void testMcomGetPagePath() throws Exception {
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("website.mcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testBcomGetPagePath() throws Exception {
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("website.bcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testMewMcomGetPagePath() throws Exception {
        MainRunner.url = "http://m.qa10codemacys.fds.com";
        MainRunner.currentURL = MainRunner.url;
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("MEW.mcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testMewBcomGetPagePath() throws Exception {
        MainRunner.url = "http://m.qa10codebloomingdales.fds.com";
        MainRunner.currentURL = MainRunner.url;
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("MEW.bcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testiOSMcomGetPagePath() throws Exception {
        MainRunner.brand = "mcom";
        MainRunner.appTest = true;
        MainRunner.device = "iPhone 6";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("iOS.mcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testiOSBcomGetPagePath() throws Exception {
        MainRunner.brand = "bcom";
        MainRunner.appTest = true;
        MainRunner.device = "iPad 2";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("iOS.bcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testAndroidMcomGetPagePath() throws Exception {
        MainRunner.brand = "mcom";
        MainRunner.appTest = true;
        MainRunner.device = "Samsung Galaxy s4";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("android.mcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testAndroidBcomGetPagePath() throws Exception {
        MainRunner.brand = "bcom";
        MainRunner.appTest = true;
        MainRunner.device = "Google Nexus 10";
        PageElement testElement = new PageElement("testPage.testElement");
        Assert.assertEquals("android.bcom.page.testPage", testElement.getPagePath());
    }

    @Test
    public void testGetResponsivePath() throws Exception {
        Assert.assertEquals(PageElement.getResponsivePath("website.mcom.page.testPage"), "responsive.mcom.page.testPage");
        Assert.assertEquals(PageElement.getResponsivePath("responsive.bcom.page.testPage"), "responsive.bcom.page.testPage");
    }

    @Test
    public void testParseValue() throws Exception {
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        PageElement testElement = new PageElement("testPage.testElement");
        testElement.parseValue("id, idForTest||className, classForTest");
        ArrayList<String> elementLocators = new ArrayList<>(Arrays.asList("id", "className"));
        ArrayList<String> elementValues = new ArrayList<>(Arrays.asList("idForTest", "classForTest"));
        Assert.assertEquals(elementLocators, testElement.elementLocators);
        Assert.assertEquals(elementValues, testElement.elementValues);
    }
}