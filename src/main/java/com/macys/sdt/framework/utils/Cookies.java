package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import static com.macys.sdt.framework.utils.StepUtils.*;

/**
 * This class provides an easy way to safely manage cookies either through the web driver or javascript.
 */
public class Cookies {

    private static final Logger logger = LoggerFactory.getLogger(Cookies.class);

    private static String domain = "." +
            Utils.removeFromString(RunConfig.url, "www1.", "www.", "http://", "https://", "m.", "m2qa1.");

    /**
     * Changes the domain used to create and update cookies
     *
     * @param domainName domain name to be used in future cookies
     */
    public static void changeDomain(String domainName) {
        domain = domainName;
    }

    /**
     * Gets a list of all cookies on the current page
     * <p>Will have more details on browsers which support web driver cookie management</p>
     *
     * @return list of cookies.
     */
    public static Set<Cookie> getCookies() {
        if (ie() || edge()) {
            Set<Cookie> cookies = new HashSet<>();
            for (String cookie : ((String) Navigate.execJavascript("return document.cookie")).split("; ")) {
                String[] cookieValue = cookie.split("=");
                if (cookieValue.length > 1) {
                    cookies.add(new Cookie(cookieValue[0], cookieValue[1]));
                } else {
                    cookies.add(new Cookie(cookieValue[0], ""));
                }
            }
            return cookies;
        } else {
            try {
                return WebDriverManager.getWebDriver().manage().getCookies();
            } catch (DriverNotInitializedException e) {
                Assert.fail("Driver not initialized");
            }
            return null;
        }
    }

    /**
     * Resets the domain back to the default domain based on the "website" environment variable
     */
    public static void resetDomain() {
        domain = RunConfig.url.replace("www1", "").replace("www", "").replace("http://", "").replaceFirst("m\\.", "");
    }

    /**
     * Adds a cookie to the browser
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     * @return true if cookie is added
     */
    public static boolean addCookie(String name, String value) {
        return !RunConfig.useAppium && (ie() || edge()) ? addCookieJavascript(name, value) : addCookie(name, value, "/", getExpiry());
    }

    /**
     * Adds a cookie to the browser
     *
     * @param name   name of the cookie
     * @param value  value of the cookie
     * @param path   path value of the cookie (usually '/')
     * @param expiry expiration date of the cookie
     * @return true if cookie is added
     */
    public static boolean addCookie(String name, String value, String path, Date expiry) {
        if (RunConfig.appTest) {
            return false;
        }

        if (expiry == null) {
            expiry = getExpiry();
        }
        if (ie() || edge() || (iOS() && safari())) {
            addCookieJavascript(name, value, path, expiry);
        } else {
            try {
                String encodedValue = encodeURL(value);
                WebDriver.Options options = WebDriverManager.getWebDriver().manage();
                options.deleteCookieNamed(name);
                options.addCookie(new Cookie(name, encodedValue, domain, path, expiry));
            } catch (Exception e) {
                logger.warn("Unable to set \'" + name + "\' cookie value due to : " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Deletes a cookie through the web driver
     *
     * @param name name of the cookie to be deleted
     * @return true if cookie is deleted
     */
    public static boolean deleteCookie(String name) {
        if (RunConfig.appTest) {
            return false;
        }

        try {
            WebDriver.Options options = WebDriverManager.getWebDriver().manage();
            options.deleteCookieNamed(name);
            return true;
        } catch (Exception e) {
            logger.warn("Unable to delete \'" + name + "\' cookie");
            return false;
        }
    }

    /**
     * Deletes all cookies. Uses JS on browsers that aren't supported by Selenium cookies
     */
    public static void deleteAllCookies() {
        if (RunConfig.appTest) {
            return;
        }

        if (ie() || safari() || edge()) {
            deleteAllCookiesJavascript();
        } else {
            try {
                WebDriverManager.getWebDriver().manage().deleteAllCookies();
            } catch (DriverNotInitializedException e) {
                Assert.fail("Driver not initialized");
            }
        }
    }

    /**
     * Edits a cookie to replace part of its value with another
     *
     * @param name    name of the cookie to be edited
     * @param replace value to replace
     * @param with    value to insert
     * @return true if cookie is edited
     */
    public static boolean editCookie(String name, String replace, String with) {
        if (RunConfig.appTest) {
            return false;
        }

        String oldValue = getCookieValue(name);
        return oldValue.contains(replace) && addCookie(name, oldValue.replace(replace, with), "/", null);
    }

    /**
     * Adds or replaces a cookie using javascript
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     * @return true if cookie is added
     */
    public static boolean addCookieJavascript(String name, String value) {
        if (RunConfig.appTest) {
            return false;
        }
        removeDuplicateCookieJS(name);
        String encodedValue = encodeURL(value);
        try {
            Navigate.execJavascript("document.cookie = '" + name + "=" + encodedValue + "'");
            return true;
        } catch (Exception e) {
            logger.warn("issue in add or replace cookie using js : " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds or replaces a cookie using javascript
     *
     * @param name   name of the cookie
     * @param value  value of the cookie
     * @param path   path value of the cookie (usually '/')
     * @param expiry expiration date of the cookie
     * @return true if cookie is added
     */
    public static boolean addCookieJavascript(String name, String value, String path, Date expiry) {
        if (RunConfig.appTest) {
            return false;
        }
        removeDuplicateCookieJS(name);
        String encodedValue = encodeURL(value);
        if (expiry == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 5);
            expiry = calendar.getTime();
        }
        try {
            Navigate.execJavascript("document.cookie = '" + name + "=" + encodedValue + "; path=" + path + "; expires=" + expiry.toString() + "; domain=" + domain + "'");
            return true;
        } catch (Exception e) {
            logger.warn("issue in add or replace cookie using js : " + e.getMessage());
            return false;
        }
    }

    /**
     * delete cookie name passed as argument if present
     *
     * @param name name of the cookie
     */
    private static void removeDuplicateCookieJS(String name ) {
        String cookies = (String)Navigate.execJavascript("return document.cookie");
        if (cookies.contains(name)) {
            deleteCookieJavascript(name);
        }
    }

    /**
     * Deletes a cookie using javascript
     *
     * @param name name of cookie to delete
     */
    public static void deleteCookieJavascript(String name) {
        if (RunConfig.appTest) {
            return;
        }

        Navigate.execJavascript("document.cookie = '" + name + "=; expires=Thu, 18 Dec 2013 12:00:00 UTC'");
    }

    /**
     * Delete all cookies using javascript
     */
    public static void deleteAllCookiesJavascript() {
        if (RunConfig.appTest) {
            return;
        }

        resetIshipCookie();
        Navigate.execJavascript(
                "var cookies=document.cookie.split(';');" +
                        "for (var i=0; i<cookies.length; i++){" +
                        "var spcook=cookies[i].split('=');" +
                        "console.log(spcook[0]);" +
                        "document.cookie=spcook[0]+'='+spcook[1]+'; " +
                        "expires=Thu, 21 Sep 1979 00:00:01 UTC; " +
                        "domain=" + domain + "; path=/';" +
                        "}");
    }

    /**
     * Gets the value of a cookie
     *
     * @param name name of the cookie you're looking for
     * @return String representation of cookie value. Empty string if cookie not found.
     */
    public static String getCookieValue(String name) {
        if (RunConfig.appTest) {
            return "";
        }

        try {
            if (ie() || (iOS() && safari()) || edge()) {
                String cookieValue = "";
                for (String cookie : ((String) Navigate.execJavascript("return document.cookie")).split("; ")) {
                    if (cookie.split("=")[0].equals(name)) {
                        cookieValue = cookie.split("=")[1];
                    }
                }
                return cookieValue;
            }

            return URLDecoder.decode(WebDriverManager.getWebDriver().manage().getCookieNamed(name).getValue(), "UTF-8");
        } catch (Exception e) {
            logger.warn("Unable to get \'" + name + "\' cookie value: " + e.getMessage());
            return "";
        }
    }

    /**
     * Prints out the value of a cookie (for debugging purposes)
     *
     * @param name name of cookie to print out
     */
    public static void printCookie(String name) {
        logger.info(String.format("Cookie \'%s\' has value : %s", name, getCookieValue(name)));
    }

    /**
     * Sets the ISHIP cookie value to "US"
     */
    public static void resetIshipCookie() {
        addCookieJavascript("shippingCountry", "US", "/", null);
    }

    /**
     * Adds a segment value to the segment cookie
     *
     * @param segment value to be added (should contain an int)
     * @return true if segment is added successfully
     */
    public static boolean addSegment(String segment) {
        String current = getCookieValue("SEGMENT");

        // remove ending "]}"
        current = (current.length() > 2) ? current.substring(0, current.length() - 2) : current;
        // add new value
        return addCookie("SEGMENT", current + "," + segment + "]}");
    }

    /**
     * Removes a segment value from the segment cookie
     *
     * @param segment value to be removed (should contain an int)
     * @return true if segment is removed successfully
     */
    public static boolean removeSegment(String segment) {
        return editCookie("SEGMENT", "," + segment, "");
    }

    /**
     * Adds a segment value to the segment cookie
     *
     * @param segment value to be added
     * @return true if segment is added successfully
     */
    public static boolean addSegment(int segment) {
        return addSegment(Integer.toString(segment));
    }

    /**
     * Removes a segment value from the segment cookie
     *
     * @param segment value to be removed
     * @return true if segment is removed successfully
     */
    public static boolean removeSegment(int segment) {
        return removeSegment(Integer.toString(segment));
    }

    /**
     * Adds and removes the given values from the segment cookie
     *
     * @param toAdd    list of values to add
     * @param toRemove list of values to remove
     * @return true if edit is successful
     */
    public static boolean editSegments(List<String> toAdd, List<String> toRemove) {
        String segments = getCookieValue("SEGMENT");
        for (String remove : toRemove) {
            remove = "," + remove;
            if (segments.contains(remove + ",") || segments.contains(remove + "]")) {
                segments = segments.replace(remove, "");
            }
        }
        addCookie("SEGMENT", segments);
        ArrayList<String> duplicates = new ArrayList<>();
        for (String add : toAdd) {
            add = "," + add;
            if (segments.contains(add + ",") || segments.contains(add + "]")) {
                duplicates.add(add.substring(1, add.length()));
            }
        }
        toAdd.removeAll(duplicates);
        String allAdds = String.join(",", toAdd.toArray(new String[]{}));
        return addSegment(allAdds);
    }

    /**
     * Replaces the "SEGMENT" cookie with one with only this value
     *
     * @param value value to insert into the segment cookie
     * @return true if segment is set successfully
     */
    public static boolean setSingleSegment(String value) {
        return addCookie("SEGMENT", "{\"EXPERIMENT\":[" + value + "]}");
    }

    /**
     * Forces responsive checkout behavior when available
     */
    public static void forceRc() {
        removeSegment(macys() ? "1066" : "1097");
        addSegment(macys() ? "1067" : "1098");
    }

    /**
     * Forces legacy checkout behavior when available
     */
    public static void forceNonRc() {
        removeSegment(macys() ? "1067" : "1098");
        addSegment(macys() ? "1066" : "1097");
    }

    /**
     * Disables the foresee survey popup
     */
    public static void disableForeseeSurvey() {
        String fsr_r;
        String fsr_s;
        if (StepUtils.MEW()) {
            fsr_r = "{\"d\":90,\"i\":\"de25df2-105324912-a3ea-edc0-dcdd0\",\"e\":1406678138341}";
            fsr_s = "{\"v\":1,\"rid\":\"de25df2-105324912-a3ea-edc0-dcdd0\"," +
                    "\"cp\":{\"isAuthenticated\":\"none\",\"MEW_2_0\":\"2.0\",\"Currency\":\"false\"," +
                    "\"Shipping_Country\":\"false\"},\"to\":3.1,\"c\":\"" + RunConfig.url + "\"," +
                    "\"pv\":10,\"lc\":{\"d0\":{\"v\":10,\"s\":true}}," +
                    "\"cd\":0,\"sd\":0,\"l\":\"en\",\"i\":-1,\"f\":1406073395349}";
        } else {
            fsr_r = "{\"d\":365,\"i\":\"d036702-53369766-67bf-6dea-4b996\",\"e\":1408990569653, \"s\":1}";
            fsr_s = "{\"v2\":-2,\"v1\":1,\"rid\":\"d036702-53369766-67bf-6dea-4b996\"," +
                    "\"cp\":{isAuthenticated:\"none\"}," +
                    "\"to\":3,\"c\":" + RunConfig.url +
                    "\"pv\":1," +
                    "\"lc\":{\"d0\":{\"v\":1,\"s\":false}}," +
                    "\"cd\":0}";
        }
        addCookie("fsr.r", fsr_r, "/", null);
        addCookie("fsr.s", fsr_s, "/", null);
        addCookie("fsr.o", "365");
    }

    /**
     * Creates a dummy expiration 5 days in the future
     *
     * @return Date with valid future expiration date
     */
    private static Date getExpiry() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 5);
        return calendar.getTime();
    }

    /**
     * Retrieves information on all active segmentation from http://segments.macys.com/campaign/all
     *
     * @return JSONArray containing segmentation information
     */
    public static JSONArray getSegmentJSON() {
        try {
            String response = Utils.httpGet("http://segments.macys.com/campaign/all", null);
            return new JSONArray(response);
        } catch (Exception e) {
            logger.warn("Unable to get segment json due to : " + e.getMessage());
        }
        return null;
    }

    /**
     * Sets all experiments to their default/control behavior.
     *
     * @return true if operation is successful
     */
    public static boolean setDefaultSegments() {
        try {
            JSONArray experiments = Cookies.getSegmentJSON();
            if (experiments == null) {
                throw new JSONException("unable to get expected experiments");
            }

            ArrayList<String> toAdd = new ArrayList<>();
            ArrayList<String> toRemove = new ArrayList<>();
            for (int i = 0; i < experiments.length(); i++) {
                JSONObject experiment = experiments.getJSONObject(i);
                JSONArray recipes = experiment.getJSONArray("recipes");
                ArrayList<String> removeTemp = new ArrayList<>();
                Map<Integer, Integer> weights = new HashMap<>();
                for (int j = 0; j < recipes.length(); j++) {
                    JSONObject recipe = recipes.getJSONObject(j);
                    String name = recipe.getString("name");
                    weights.put(j, recipe.getInt("weight"));
                    if (name.matches("(.*?)(?i)control|ctrl(.*?)")) {
                        toAdd.add(recipe.getString("id"));
                    } else {
                        removeTemp.add(recipe.getString("id"));
                    }
                }
                // if we never found a "control" option, get the one with the highest weight
                if (removeTemp.size() == recipes.length()) {
                    int maxWeight = 0, index = 0;
                    for (int key : weights.keySet()) {
                        if (weights.get(key) > maxWeight) {
                            maxWeight = weights.get(key);
                            index = key;
                        }
                    }
                    toAdd.add(removeTemp.get(index));
                    removeTemp.remove(index);
                }
                toRemove.addAll(removeTemp);
            }
            return Cookies.editSegments(toAdd, toRemove);
        } catch (JSONException e) {
            Assert.fail("Unable to parse JSON: " + e.getMessage());
        }
        return false;
    }

    /**
     * Disables all experimentation through the segment and mercury cookies
     *
     * @return true if experiments are disabled successfully
     */
    public static boolean disableExperimentation() {
        logger.debug("Disabling experimentation");
        addCookie("mercury", "false");
        return setSingleSegment("");
    }

    /**
     * Retrieves the secure user token from the cookie to use for service calls
     *
     * @return value of secure user token cookie
     */
    public static String getSecureUserToken() {
        return getCookieValue("secure_user_token");
    }

    /**
     * Retrieves the user ID of the currently signed in user
     *
     * @return user ID of signed in user
     */
    public static String getCurrentUserId() {
        return getCookieValue((macys() ? "macys" : "bloomingdales") + "_online_uid");
    }

    private static boolean onDal = false;

    /**
     * Sets the cookie for dallas site
     */
    public static void setDalCookie() {
        if (!onDal) {
            addCookie("dca", "dal");
            onDal = true;
            Navigate.browserRefresh();
        }
    }

    private static String encodeURL(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Could not encode string : " + value);
            return value;
        }
    }

    private static String decodeURL(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            logger.warn("Unable to decode URL : " + value + " due to : " + e.getMessage());
            return "";
        }
    }
}
