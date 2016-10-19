package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class pulls and manages data from page and panel JSON files
 */
public class PageUtils {
    protected static HashMap<String, JSONObject> cachePagesProject = new HashMap<>();
    protected static HashMap<String, JSONObject> cachePagesShared = new HashMap<>();

    /**
     * Prints out the values of all saved pages/panels
     */
    public static void displayPageJSONHash() {
        for (Map.Entry mapEntry : cachePagesProject.entrySet()) {
            System.out.println("project page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
        for (Map.Entry mapEntry : cachePagesProject.entrySet()) {
            System.out.println("shared page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
    }

    /**
     * Loads a JSON object from file into memory
     * <p>
     * This method first looks in your project's directory (passed as environment variable "project"), then in the
     * shared directories. If on BCOM, it will default to MCOM if no page is found.
     * </p>
     *
     * @param pagePath name of page to load
     */
    public static void loadPageJSON(String pagePath) {
        if (cachePagesProject.get(pagePath) != null || cachePagesShared.get(pagePath) != null) {
            return;
        }

        String path = pagePath.replace(".page.", ".pages.").replace(".panel.", ".panels.").replace(".", "/");
        String resPath = "/resources/elements/" + path + ".json";

        // project elements first
        if (MainRunner.project != null) {
            path = MainRunner.projectDir + resPath;
            loadOnePageJSONFile(pagePath, path, "project");

            // also load panel elements
            if (pagePath.contains(".page.")) {
                path = path.replace("/pages/", "/panels/");
                loadOnePageJSONFile(pagePath, path, "project");
            }
        }

        // shared elements next
        path = "shared" + resPath;
        loadOnePageJSONFile(pagePath, path, "shared");

        // also load panel elements
        if (pagePath.contains(".page.")) {
            path = path.replace("/pages/", "/panels/");
            loadOnePageJSONFile(pagePath, path, "shared");
        }

        // on MCOM env, it is done
        if (!pagePath.contains(".bcom.")) {
            return;
        }

        // on BCOM env, fallback onto mcom elements
        resPath = resPath.replace("/bcom/", "/mcom/");
        pagePath = pagePath.replace(".bcom.", ".mcom.");

        // project elements first
        if (MainRunner.project != null) {
            path = MainRunner.projectDir + resPath;
            loadOnePageJSONFile(pagePath, path, "project");

            // also load panel elements
            if (pagePath.contains(".page.")) {
                path = path.replace("/pages/", "/panels/");
                loadOnePageJSONFile(pagePath, path, "project");
            }
        }

        // shared elements next
        path = "src/com/macys/sdt/shared" + resPath;
        loadOnePageJSONFile(pagePath, path, "shared");

        // also load panel elements
        if (pagePath.contains(".page.")) {
            path = path.replace("/pages/", "/panels/");
            loadOnePageJSONFile(pagePath, path, "shared");
        }
    }

    private static boolean loadOnePageJSONFile(String pagePath, String filePath, String cache) {
        File f = new File(filePath);
        if (f.exists() && !f.isDirectory()) {
            loadPageJsonFiles(pagePath, f, cache);
            return true;
        }

        // find file recursively under the directory
        String fName = f.getName();
        File dir = f.getParentFile();
        f = findPage(dir, fName);

        if (f != null && f.exists() && !f.isDirectory()) {
            int fileCount = countFoundPage(dir, fName);
            if (fileCount < 1) {
                return false;
            }
            if (fileCount == 1) {
                loadPageJsonFiles(pagePath, f, cache);
                return true;
            } else {
                Assert.fail("Resource Error: Multiple '" + fName + "'(total: " + fileCount + ") " +
                        " files found under '" + dir.getAbsolutePath() + "'");
            }
        }
        return false;
    }

    // recursively checks all subdirectories for a file matching pageName
    private static File findPage(File dir, String pageName) {
        File[] subDirs = dir.listFiles(File::isDirectory);
        File[] resources = dir.listFiles(File::isFile);
        if (resources != null) {
            for (File resource : resources) {
                if (resource.getName().equals(pageName)) {
                    return resource;
                }
            }
        }
        if (subDirs == null) {
            return null;
        }
        File resource = null;
        for (File subDir : subDirs) {
            resource = findPage(subDir, pageName);
            if (resource != null && resource.getName().equals(pageName)) {
                break;
            }
        }
        return resource;
    }

    // recursively checks all subdirectories for a file matching pageName
    private static int countFoundPage(File dir, String pageName) {
        int count = 0;
        File[] subDirs = dir.listFiles(File::isDirectory);
        File[] resources = dir.listFiles(File::isFile);
        if (resources != null) {
            for (File resource : resources) {
                if (resource.getName().equals(pageName)) {
                    count++;
                }
            }
        }
        if (subDirs == null) {
            return count;
        }

        for (File subDir : subDirs) {
            int subCount = countFoundPage(subDir, pageName);
            count += subCount;
        }
        return count;
    }

    private static void loadPageJsonFiles(String pagePath, File file, String cache) {
        if (cache.equals("project")) {
            if (cachePagesProject.get(pagePath) != null) {
                return;
            }
        } else {
            if (cachePagesShared.get(pagePath) != null) {
                return;
            }
        }

        JSONObject pageJson;
        try {
            pageJson = new JSONObject(Utils.readTextFile(file));
        } catch (IOException | JSONException e) {
            System.err.println("-->Error parsing json at PageUtils.loadPageJSON() for page: " + file.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        // put new DataFile entry
        if (cache.equals("project")) {
            cachePagesProject.put(pagePath, pageJson);
        } else {
            cachePagesShared.put(pagePath, pageJson);
        }

        // process included Panel files
        JSONArray includedDataFiles = null;
        try {
            includedDataFiles = pageJson.getJSONArray("include");
        } catch (JSONException e) {
            // no 'include'
        }

        if (includedDataFiles == null) {
            return;
        }

        for (int i = 0; i < includedDataFiles.length(); i++) {
            try {
                String panelName = includedDataFiles.getString(i);
                if (panelName.contains("panel.")) {
                    panelName = panelName.replace("panel.", "");
                    String[] parts = pagePath.split(Pattern.quote("."));
                    String panelPath = parts[0] + "." + parts[1] + ".panel." + panelName;
                    loadPageJSON(panelPath);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds JSON entry value from page JSON object
     *
     * @param element PageElement containing data to find
     * @return Value of the element
     */
    //
    public static String getElementJSONValue(PageElement element) {
        if (element.pageName == null || element.elementName == null) {
            return null;
        }

        // load page JSON into cache if not already loaded
        loadPageJSON(element.pagePath);

        // search for element value in page JSON file
        return findPageJSONValue(element.pagePath, element.elementName);
    }

    // get element value from JSON object in memory
    private static String findPageJSONValue(String pagePath, String elementName) {
        String result;
        result = findCachePageJSONValue(pagePath, elementName);

        // try panel
        if (result == null && pagePath.contains(".page.")) {
            result = findCachePageJSONValue(pagePath.replace(".page.", ".panel."), elementName);
        }

        // if bcom, try mcom
        if (result == null && pagePath.contains(".bcom.")) {
            String mcomPagePath = pagePath.replace(".bcom.", ".mcom.");
            result = findCachePageJSONValue(mcomPagePath, elementName);
            // try panel
            if (result == null && mcomPagePath.contains(".page.")) {
                result = findCachePageJSONValue(mcomPagePath.replace(".page.", ".panel."), elementName);
            }
        }
        return result;
    }

    private static String findCachePageJSONValue(String pagePath, String elementName) {
        String result = findPageJSONValueInternal(pagePath, elementName, "project");
        return result != null ? result : findPageJSONValueInternal(pagePath, elementName, "shared");
    }

    private static String findPageJSONValueInternal(String pagePath, String elementName, String cache) {
        String result = null;
        JSONObject pageData;
        if (cache.equals("project")) {
            pageData = cachePagesProject.get(pagePath);
        } else {
            pageData = cachePagesShared.get(pagePath);
        }

        try {
            result = (String) pageData.get(elementName);
        } catch (Exception e) {
            // skip any error
        }

        if (result != null) {
            return result;
        }

        // search in panels
        JSONArray includedDataFiles = null;
        try {
            includedDataFiles = pageData.getJSONArray("include");
        } catch (Exception e) {
            // no 'include'
        }

        if (includedDataFiles == null) {
            // System.out.println("No value found for " + pageName + "." + elementName);
            return result;
        }

        int count = includedDataFiles.length();
        for (int i = 0; i < count; i++) {
            try {
                String panelName = includedDataFiles.getString(i);
                if (panelName.contains("panel.")) {
                    panelName = panelName.replace("panel.", "");
                    String[] parts = pagePath.split(Pattern.quote("."));
                    String panelPath = parts[0] + "." + parts[1] + ".panel." + panelName;
                    result = findPageJSONValueInternal(panelPath, elementName, cache);
                    if (result != null) {
                        return result;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.out.println("No value found for " + pagePath + "." + elementName);
        return result;
    }
}
