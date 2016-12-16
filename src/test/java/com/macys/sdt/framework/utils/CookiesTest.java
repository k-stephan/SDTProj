package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.interactions.InteractionsSuiteTest;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.runner.MainRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Need to decide which domain to use for testing Cookies")
public class CookiesTest {

    @BeforeClass
    public static void setUp() throws Exception {
        InteractionsSuiteTest.setUp();
        Assume.assumeTrue(MainRunner.driverInitialized());
        Navigate.visit("http://www.google.com");
        Cookies.changeDomain("google.com");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Cookies.resetDomain();
        InteractionsSuiteTest.tearDown();
    }

    @Test
    public void testAddCookie() throws Exception {
        String name = "testCookie1";
        String value = "testCookieValue1";
        Cookies.addCookie(name, value);
        Assert.assertEquals(value, Cookies.getCookieValue(name));
    }

    @Test
    public void testDeleteCookie() throws Exception {
        String name = "testCookie2";
        String value = "testCookieValue2";
        Cookies.addCookie(name, value);
        Cookies.deleteCookie(name);
        Assert.assertEquals("", Cookies.getCookieValue(name));
    }

    @Test
    public void testDeleteAllCookies() throws Exception {
        String name = "testCookie3";
        String value = "testCookieValue3";
        Cookies.addCookie(name, value);
        Cookies.deleteAllCookies();
        Assert.assertEquals("", Cookies.getCookieValue(name));
    }

    @Test
    public void testEditCookie() throws Exception {
        String name = "testCookie4";
        String value = "testCookieValue4";
        String newValue = "newValue";
        Cookies.addCookie(name, value);
        Cookies.editCookie(name, value, newValue);
        Assert.assertEquals(newValue, Cookies.getCookieValue(name));
    }

    @Test
    public void testAddCookieJavascript() throws Exception {
        String name = "testCookieJ";
        String value = "testCookieValueJ";
        Cookies.addCookieJavascript(name, value);
        Assert.assertEquals(value, Cookies.getCookieValue(name));
    }

    @Test
    public void testDeleteCookieJavascript() throws Exception {
        String name = "testCookieJ2";
        String value = "testCookieValueJ2";
        Cookies.addCookie(name, value);
        try {
            Cookies.deleteCookieJavascript(name);
        } catch (Exception e) {
            Assert.fail("Failed testDeleteCookieJavascript : " + e.getMessage());
        }
    }

    @Test
    public void testDeleteAllCookiesJavascript() throws Exception {
        String name = "testCookieJ3";
        String value = "testCookieValueJ3";
        Cookies.addCookie(name, value);
        try {
            Cookies.deleteAllCookiesJavascript();
        } catch (Exception e) {
            Assert.fail("Failed testDeleteAllCookiesJavascript : " + e.getMessage());
        }
    }

    @Test
    public void testPrintCookie() throws Exception {
        String name = "testCookieP";
        String value = "testCookieValueP";
        Cookies.addCookie(name, value);
        try {
            Cookies.printCookie(name);
        } catch(Exception e) {
            Assert.fail("Failed testPrintCookie : " + e.getMessage());
        }
    }

    @Test
    public void testResetIshipCookie() throws Exception {
        Cookies.resetIshipCookie();
        Assert.assertEquals("US", Cookies.getCookieValue("shippingCountry"));
    }
}