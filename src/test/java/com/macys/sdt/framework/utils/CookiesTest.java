package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.runner.MainRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Ignore("Need to decide which domain to use for testing Cookies")
public class CookiesTest {

    @BeforeClass
    public static void setUp() throws Exception {
        MainRunner.workspace = "";
        MainRunner.browser = "firefox";
        MainRunner.remoteOS = "Windows 7";
        MainRunner.timeout = 90;
        MainRunner.url = "http://www.macys.com";
        boolean preCondition = false;
        try {
            MainRunner.getWebDriver();
            MainRunner.PageHangWatchDog.init();
            MainRunner.PageHangWatchDog.resetWatchDog();
            Navigate.visit(MainRunner.url);
            MainRunner.debugMode = true;
            preCondition = true;
        } catch (Exception e) {
            System.err.println("-->Error - Test setUp:" + e.getMessage());
        }
        Assume.assumeTrue("Precondition not met.", preCondition);
        Cookies.changeDomain("macys.com");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            if (MainRunner.driverInitialized()) {
                MainRunner.resetDriver(true);
            }
        } catch (Exception e) {
            System.err.println("-->Error - Test tearDown:" + e.getMessage());
        }
        Cookies.resetDomain();
        MainRunner.debugMode = false;
        MainRunner.browser = null;
        MainRunner.remoteOS = null;
        MainRunner.url = null;
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

    @Test
    public void testAddSegment() throws Exception {
        int segment = 1234;
        Cookies.addSegment(segment);
        Assert.assertTrue(Cookies.getCookieValue("SEGMENT").contains(Integer.toString(segment)));
    }

    @Test
    public void testRemoveSegment() throws Exception {
        int segment = 9876;
        Cookies.addSegment(segment);
        Cookies.removeSegment(segment);
        Assert.assertFalse(Cookies.getCookieValue("SEGMENT").contains(Integer.toString(segment)));
    }

    @Test
    public void testEditSegments() throws Exception {
        List<String> toAdd = new ArrayList<>();
        toAdd.add("1122");
        toAdd.add("2244");
        List<String> toRemove = new ArrayList<>();
        toRemove.add("2467");
        toRemove.add("1457");
        toRemove.forEach(Cookies::addSegment);
        Cookies.editSegments(toAdd, toRemove);
        String segments = Cookies.getCookieValue("SEGMENT");
        for(String seg : toAdd) {
            Assert.assertTrue(segments.contains(seg));
        }
        for(String seg : toRemove) {
            Assert.assertFalse(segments.contains(seg));
        }
    }

    @Test
    public void testSetSingleSegment() throws Exception {
        String segment = "9999";
        Cookies.setSingleSegment(segment);
        Assert.assertEquals("{\"EXPERIMENT\":[" + segment + "]}", Cookies.getCookieValue("SEGMENT"));
    }

    @Test
    public void testForceRc() throws Exception {
        MainRunner.brand = "mcom";
        Cookies.forceRc();
        String segments = Cookies.getCookieValue("SEGMENT");
        Assert.assertTrue(segments.contains("1067"));
        Assert.assertFalse(segments.contains("1066"));
        MainRunner.brand = null;
    }

    @Test
    public void testForceNonRc() throws Exception {
        MainRunner.brand = "bcom";
        Cookies.forceNonRc();
        String segments = Cookies.getCookieValue("SEGMENT");
        Assert.assertTrue(segments.contains("1097"));
        Assert.assertFalse(segments.contains("1098"));
        MainRunner.brand = null;
    }

    @Test
    public void testDisableForeseeSurvey() throws Exception {
        Cookies.disableForeseeSurvey();
        Assert.assertEquals("365", Cookies.getCookieValue("fsr.o"));
    }

    @Test
    public void testSetDefaultSegments() throws Exception {
        Assert.assertTrue(Cookies.setDefaultSegments());
    }

    @Test
    public void testDisableExperimentation() throws Exception {
        Cookies.disableExperimentation();
        Assert.assertEquals("false", Cookies.getCookieValue("mercury"));
        Assert.assertEquals("{\"EXPERIMENT\":[]}", Cookies.getCookieValue("SEGMENT"));
    }
}