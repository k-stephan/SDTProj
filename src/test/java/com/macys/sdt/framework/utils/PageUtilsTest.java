package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for PageUtils
 */
public class PageUtilsTest {

    @BeforeClass
    public static void setUp() throws Exception {
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";
    }

    @After
    public void reset() throws Exception {
        PageUtils.cachePagesProject.clear();
        PageUtils.cachePagesShared.clear();
    }

    @Test
    public void testDisplayPageJSONHash() throws Exception {
        PageUtils.loadPageJSON("website.mcom.page.test_page");
        PageUtils.loadPageJSON("website.bcom.page.test_page");
        try {
            PageUtils.displayPageJSONHash();
        } catch (Exception e) {
            Assert.fail("Failed to display PageJSONHash - " + e.getMessage());
        }
    }

    @Test
    public void testLoadPageJSON() throws Exception {
        PageUtils.loadPageJSON("website.mcom.page.test_page");
        Assert.assertNotNull(PageUtils.cachePagesProject.get("website.mcom.page.test_page"));
        Assert.assertNotNull(PageUtils.cachePagesProject.get("website.mcom.panel.test_panel"));

        // fallback
        PageUtils.loadPageJSON("website.bcom.page.mcom_another_page");
        Assert.assertNotNull(PageUtils.cachePagesProject.get("website.mcom.page.mcom_another_page"));

        // responsive
        PageUtils.loadPageJSON("MEW.mcom.page.responsive_page");
        Assert.assertNotNull(PageUtils.cachePagesProject.get("responsive.mcom.page.responsive_page"));

        // no file
        PageUtils.loadPageJSON("website.mcom.page.page_dos_not_exists");
        Assert.assertNull(PageUtils.cachePagesProject.get("website.mcom.page.page_dos_not_exists"));
    }

    @Test
    public void testGetElementJSONValue() throws Exception {
        MainRunner.url = "http://www.qa0codebloomingdales.fds.com";
        String elementJSONValue = PageUtils.getElementJSONValue(new PageElement("test_page.test_element"));
        Assert.assertNotNull(elementJSONValue);
        Assert.assertEquals("id, b_id || class,  b_class", elementJSONValue);

        // fallback
        Assert.assertNotNull(PageUtils.getElementJSONValue(new PageElement("mcom_another_page.another_element")));

        //no element
        Assert.assertNull(PageUtils.getElementJSONValue(new PageElement("test_page.element_dos_not_exists")));

        // no file
        Assert.assertNull(PageUtils.getElementJSONValue(new PageElement("page_dos_not_exists.test_element")));
    }
}