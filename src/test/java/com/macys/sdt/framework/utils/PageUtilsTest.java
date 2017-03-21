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
    public static void setUp() {
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";
        MainRunner.workspace = System.getProperty("user.dir") + "/";
    }

    @After
    public void reset() throws Exception {
        PageUtils.projectPages.clear();
        PageUtils.sharedPages.clear();
    }

    @Test
    public void testDisplayPageJSONHash() {
        PageUtils.loadPageJSON("website.mcom.page.test_page");
        PageUtils.loadPageJSON("website.bcom.page.test_page");
        try {
            PageUtils.displayPageJSONHash();
        } catch (Exception e) {
            Assert.fail("Failed to display PageJSONHash - " + e.getMessage());
        }
    }

    @Test
    public void testLoadPageJSON() {
        PageUtils.loadPageJSON("website.mcom.page.test_page");
        Assert.assertNotNull(PageUtils.projectPages.get("website.mcom.page.test_page"));
        Assert.assertNotNull(PageUtils.projectPages.get("website.mcom.panel.test_panel"));

        // fallback
        PageUtils.loadPageJSON("website.bcom.page.mcom_another_page");
        Assert.assertNotNull(PageUtils.projectPages.get("website.mcom.page.mcom_another_page"));

        // responsive
        PageUtils.loadPageJSON("MEW.mcom.page.responsive_page");
        Assert.assertNotNull(PageUtils.projectPages.get("responsive.mcom.page.responsive_page"));

        // no file
        PageUtils.loadPageJSON("website.mcom.page.page_dos_not_exists");
        Assert.assertNull(PageUtils.projectPages.get("website.mcom.page.page_dos_not_exists"));
    }

    @Test
    public void testGetElementJSONValue() {
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

    @Test
    public void testDuplicatePagePanelName() {
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        PageElement pageElement = new PageElement("test.page_element");
        PageElement panelElement = new PageElement("test.panel_element");

        Assert.assertEquals(pageElement.elementValues.get(0), "page_element");
        Assert.assertEquals(panelElement.elementValues.get(0), "panel_element");
    }
}