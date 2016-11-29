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

import static com.macys.sdt.framework.utils.PageElement.getResponsivePath;
import static com.macys.sdt.framework.utils.Utils.log;
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
        String responsivePath = getResponsivePath(pagePath);
        if (responsivePath.startsWith("responsive")) {
            if (cachePagesProject.get(responsivePath) != null || cachePagesShared.get(responsivePath) != null) {
                return;
            }
        }
        if (cachePagesProject.get(pagePath) != null || cachePagesShared.get(pagePath) != null) {
            return;
        }

        String path = pagePath.replace(".page.", ".pages.").replace(".panel.", ".panels.").replace(".", "/");
        String resPath = "/resources/elements/" + path + ".json";

        // find & load files
        loadJSONFiles(resPath, pagePath);

        // on MCOM env, we're done done
        if (!pagePath.contains(".bcom.")) {
            return;
        }

        // on BCOM env, fallback onto mcom elements
        resPath = resPath.replace("/bcom/", "/mcom/");
        pagePath = pagePath.replace(".bcom.", ".mcom.");
        loadJSONFiles(resPath, pagePath);

    }

    private static void loadJSONFiles(String resPath, String page) {
        String path;
        String responsivePage = getResponsivePath(page);
        String responsivePath;
        // project elements first
        if (MainRunner.project != null) {
            path = MainRunner.projectDir + resPath;
            responsivePath = getResponsivePath(path);
            loadPageAndPanels(responsivePage, responsivePath, "project");
            loadPageAndPanels(page, path, "project");

            // also load panel elements
            if (page.contains(".page.")) {
                path = path.replace("/pages/", "/panels/");
                responsivePath = responsivePath.replace("/pages/", "/panels/");
                loadPageAndPanels(responsivePage, responsivePath, "project");
                loadPageAndPanels(page, path, "project");
            }
        }

        // shared elements next
        path = "shared" + resPath;
        responsivePath = getResponsivePath(path);
        loadPageAndPanels(responsivePage, responsivePath, "shared");
        loadPageAndPanels(page, path, "shared");

        // also load panel elements
        if (page.contains(".page.")) {
            path = path.replace("/pages/", "/panels/");
            responsivePath = responsivePath.replace("/pages/", "/panels/");
            loadPageAndPanels(responsivePage, responsivePath, "shared");
            loadPageAndPanels(page, path, "shared");
        }
    }

    private static boolean loadPageAndPanels(String pagePath, String filePath, String cache) {
        File f = new File(filePath);
        if (f.exists() && !f.isDirectory()) {
            loadPageJsonFiles(pagePath, f, cache);
            return true;
        }

        // find file recursively under the directory
        String fName = f.getName();
        File dir = f.getParentFile();
        f = findFile(dir, fName);
        if (filePath.startsWith("shared/") && f == null){
        	dir = new File("com/macys/sdt/" + dir.getPath());
        	f = findFile(dir, fName);
        }

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
    private static File findFile(File dir, String pageName) {
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
            resource = findFile(subDir, pageName);
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
        String responsivePath = getResponsivePath(pagePath);
        if (cache.equals("project")) {
            if (cachePagesProject.get(pagePath) != null || cachePagesProject.get(responsivePath) != null) {
                return;
            }
        } else {
            if (cachePagesShared.get(pagePath) != null || cachePagesShared.get(responsivePath) != null) {
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
        String responsivePath = getResponsivePath(pagePath);
        JSONObject pageData = null;
        if (cache.equals("project")) {
            if (responsivePath.startsWith("responsive")) {
                pageData = cachePagesProject.get(responsivePath);
            }
            if (pageData == null) {
                pageData = cachePagesProject.get(pagePath);
            }
        } else {
            if (responsivePath.startsWith("responsive")) {
                pageData = cachePagesShared.get(responsivePath);
            }
            if (pageData == null) {
                pageData = cachePagesShared.get(pagePath);
            }
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
            log.debug("No value found for " + pagePath + "." + elementName);
            return null;
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
        return null;
    }
}
