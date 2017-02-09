package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.runner.MainRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import static com.macys.sdt.framework.utils.StepUtils.*;

/**
 * This class provides an easy way to safely manage cookies either through the web driver or javascript.
 */
public class Cookies {

    private static String domain = "." + MainRunner.url.replaceFirst("www1.", "").
            replaceFirst("www.", "").replaceFirst("http://", "").replaceFirst("m\\.", "");

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
                cookies.add(new Cookie(cookieValue[0], cookieValue[1]));
            }
            return cookies;
        } else {
            return MainRunner.getWebDriver().manage().getCookies();
        }
    }

    /**
     * Resets the domain back to the default domain based on the "website" environment variable
     */
    public static void resetDomain() {
        domain = MainRunner.url.replace("www1", "").replace("www", "").replace("http://", "").replaceFirst("m\\.", "");
    }

    /**
     * Adds a cookie to the browser
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     * @return true if cookie is added
     */
    public static boolean addCookie(String name, String value) {
        return !MainRunner.useAppium && (ie() || edge()) ? addCookieJavascript(name, value) : addCookie(name, value, "/", getExpiry());
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
        if (MainRunner.useAppium) {
            return false;
        }

        if (expiry == null) {
            expiry = getExpiry();
        }
        if (ie() || edge()) {
            addCookieJavascript(name, value, path, expiry);
        } else {
            try {
                String encodedValue = encodeURL(value);
                WebDriver.Options options = MainRunner.getWebDriver().manage();
                options.deleteCookieNamed(name);
                options.addCookie(new Cookie(name, encodedValue, domain, path, expiry));
            } catch (Exception e) {
                System.out.println("Unable to set " + name + " cookie value: " + e);
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
        if (MainRunner.useAppium) {
            return false;
        }

        try {
            WebDriver.Options options = MainRunner.getWebDriver().manage();
            options.deleteCookieNamed(name);
            return true;
        } catch (Exception e) {
            System.out.println("Unable to delete " + name + " cookie");
            return false;
        }
    }

    /**
     * Deletes all cookies. Uses JS on browsers that aren't supported by Selenium cookies
     */
    public static void deleteAllCookies() {
        if (MainRunner.useAppium) {
            return;
        }

        if (ie() || safari() || edge()) {
            deleteAllCookiesJavascript();
        } else {
            MainRunner.getWebDriver().manage().deleteAllCookies();
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
        if (MainRunner.useAppium) {
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
        if (MainRunner.useAppium) {
            return false;
        }

        String encodedValue = encodeURL(value);
        try {
            Navigate.execJavascript("document.cookie = '" + name + "=" + encodedValue + "'");
            return true;
        } catch (Exception e) {
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
        if (MainRunner.useAppium) {
            return false;
        }
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
            return false;
        }
    }

    /**
     * Deletes a cookie using javascript
     *
     * @param name name of cookie to delete
     */
    public static void deleteCookieJavascript(String name) {
        if (MainRunner.useAppium) {
            return;
        }

        Navigate.execJavascript("document.cookie = '" + name + "=; expires=Thu, 18 Dec 2013 12:00:00 UTC'");
    }

    /**
     * Delete all cookies using javascript
     */
    public static void deleteAllCookiesJavascript() {
        if (MainRunner.useAppium) {
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
        if (MainRunner.useAppium) {
            return "";
        }

        try {
            if (ie() || safari() || edge()) {
                String cookieValue = "";
                for (String cookie : ((String) Navigate.execJavascript("return document.cookie")).split("; ")) {
                    if (cookie.split("=")[0].equals(name)) {
                        cookieValue = cookie.split("=")[1];
                    }
                }
                return cookieValue;
            }

            return URLDecoder.decode(MainRunner.getWebDriver().manage().getCookieNamed(name).getValue(), "UTF-8");
        } catch (Exception e) {
            System.err.println("Unable to get " + name + " cookie value: " + e);
            return "";
        }
    }

    /**
     * Prints out the value of a cookie (for debugging purposes)
     *
     * @param name name of cookie to print out
     */
    public static void printCookie(String name) {
        try {
            System.out.println(getCookieValue(name));
        } catch (Exception e) {
            System.out.println("Unable to print cookie \"" + name + "\"");
        }
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
                    "\"Shipping_Country\":\"false\"},\"to\":3.1,\"c\":\"" + MainRunner.url + "\"," +
                    "\"pv\":10,\"lc\":{\"d0\":{\"v\":10,\"s\":true}}," +
                    "\"cd\":0,\"sd\":0,\"l\":\"en\",\"i\":-1,\"f\":1406073395349}";
        } else {
            fsr_r = "{\"d\":365,\"i\":\"d036702-53369766-67bf-6dea-4b996\",\"e\":1408990569653, \"s\":1}";
            fsr_s = "{\"v2\":-2,\"v1\":1,\"rid\":\"d036702-53369766-67bf-6dea-4b996\"," +
                    "\"cp\":{isAuthenticated:\"none\"}," +
                    "\"to\":3,\"c\":" + MainRunner.url +
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
            System.err.println("Unable to get segment json: " + e);
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
            Assert.fail("Unable to parse JSON: " + e);
        }
        return false;
    }

    /**
     * Disables all experimentation through the segment and mercury cookies
     *
     * @return true if experiments are disabled successfully
     */
    public static boolean disableExperimentation() {
        if (MainRunner.debugMode) {
            System.out.println("Disabling experimentation");
        }
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


    private static String encodeURL(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Could not encode string!");
            return value;
        }
    }

    private static String decodeURL(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            System.err.println("Unable to get " + value + " cookie value: " + e);
            return "";
        }
    }
}
