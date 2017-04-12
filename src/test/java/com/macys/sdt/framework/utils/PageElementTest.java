package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.runner.RunConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for PageElement
 */
public class PageElementTest {

    @BeforeClass
    public static void setUp() throws Exception {
        RunConfig.project = "framework";
        RunConfig.projectResourceDirs = new ArrayList<>();
        RunConfig.projectResourceDirs.add("src/test/java/com/macys/sdt/framework/resources");
        RunConfig.url = null;
        MainRunner.currentURL = "";
        RunConfig.brand = null;
        RunConfig.device = null;
        RunConfig.appTest = false;
        RunConfig.workspace = System.getProperty("user.dir") + "/";
    }

    @After
    public void reset() throws Exception {
        RunConfig.url = null;
        MainRunner.currentURL = "";
        RunConfig.brand = null;
        RunConfig.device = null;
        RunConfig.appTest = false;
    }

    @Test
    public void testMcomPageElement() throws Exception {
        RunConfig.url = "http://www.qa0codemacys.fds.com";
        RunConfig.browser = "";
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("test_page", testElement.getPageName());
        Assert.assertEquals("test_element", testElement.getElementName());
        Assert.assertEquals("website.mcom.page.test_page", testElement.getPagePath());
        Assert.assertEquals(new ArrayList<>(Arrays.asList("name", "linkText")), testElement.elementLocators);
        Assert.assertEquals(new ArrayList<>(Arrays.asList("m_name", "m text")), testElement.elementValues);
        PageElement testElementV = new PageElement("test_page.verify_page");
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("id")), testElementV.elementLocators);
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("verifyPageElementM")), testElementV.elementValues);
        PageElement testElementP = new PageElement("test_page.another_element");
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("cssSelector")), testElementP.elementLocators);
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("ul#rc-mcom-list > li")), testElementP.elementValues);
    }

    @Test
    public void testBcomPageElement() throws Exception {
        RunConfig.url = "http://www.qa0codebloomingdales.fds.com";
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("test_page", testElement.getPageName());
        Assert.assertEquals("test_element", testElement.getElementName());
        Assert.assertEquals("website.bcom.page.test_page", testElement.getPagePath());
        Assert.assertEquals(new ArrayList<>(Arrays.asList("id", "class")), testElement.elementLocators);
        Assert.assertEquals(new ArrayList<>(Arrays.asList("b_id", "b_class")), testElement.elementValues);
        PageElement testElementV = new PageElement("test_page.verify_page");
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("id")), testElementV.elementLocators);
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("verifyPageElementB")), testElementV.elementValues);
        PageElement testElementP = new PageElement("test_page.another_element");
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("xpath")), testElementP.elementLocators);
        Assert.assertEquals(new ArrayList<>(Collections.singletonList("//link[@rel='bcomRel']")), testElementP.elementValues);
    }

    @Test
    public void testMewMcomGetPagePath() throws Exception {
        RunConfig.url = "http://m.qa10codemacys.fds.com";
        MainRunner.currentURL = RunConfig.url;
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("MEW.mcom.page.test_page", testElement.getPagePath());
    }

    @Test
    public void testMewBcomGetPagePath() throws Exception {
        RunConfig.url = "http://m.qa10codebloomingdales.fds.com";
        MainRunner.currentURL = RunConfig.url;
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("MEW.bcom.page.test_page", testElement.getPagePath());
    }

    @Test
    public void testiOSMcomGetPagePath() throws Exception {
        RunConfig.brand = "mcom";
        RunConfig.appTest = true;
        RunConfig.device = "iPhone 6";
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("iOS.mcom.page.test_page", testElement.getPagePath());
    }

    @Test
    public void testiOSBcomGetPagePath() throws Exception {
        RunConfig.brand = "bcom";
        RunConfig.appTest = true;
        RunConfig.device = "iPad 2";
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("iOS.bcom.page.test_page", testElement.getPagePath());
    }

    @Test
    public void testAndroidMcomGetPagePath() throws Exception {
        RunConfig.brand = "mcom";
        RunConfig.appTest = true;
        RunConfig.device = "Samsung Galaxy s4";
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("android.mcom.page.test_page", testElement.getPagePath());
    }

    @Test
    public void testAndroidBcomGetPagePath() throws Exception {
        RunConfig.brand = "bcom";
        RunConfig.appTest = true;
        RunConfig.device = "Google Nexus 10";
        PageElement testElement = new PageElement("test_page.test_element");
        Assert.assertEquals("android.bcom.page.test_page", testElement.getPagePath());
    }

    @Test
    public void testGetResponsivePath() throws Exception {
        Assert.assertEquals(PageElement.getResponsivePath("website.mcom.page.test_page"), "responsive.mcom.page.test_page");
        Assert.assertEquals(PageElement.getResponsivePath("responsive.bcom.page.test_page"), "responsive.bcom.page.test_page");
    }
}