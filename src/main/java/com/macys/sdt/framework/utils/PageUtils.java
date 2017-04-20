package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.RunConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.macys.sdt.framework.utils.PageElement.getResponsivePath;

/**
 * This class pulls and manages data from page and panel JSON files
 */
public class PageUtils {

    private static final Logger logger = LoggerFactory.getLogger(PageUtils.class);

    /**
     * This store paths and JSON values of the JSON page and panels present in the project
     */
    protected static HashMap<String, JSONObject> projectPages = new HashMap<>();

    /**
     * This store paths and JSON values of the JSON page and panels present in the shared space
     */
    protected static HashMap<String, JSONObject> sharedPages = new HashMap<>();

    /**
     * Finds JSON entry value from page JSON object
     *
     * @param element PageElement containing data to find
     * @return Value of the element
     */
    public static String getElementJSONValue(PageElement element) {
        if (element.pageName == null || element.elementName == null) {
            return null;
        }

        // load page JSON into cache if not already loaded
        loadPageJSON(element.pagePath);

        // search for element value in page JSON file
        return findPageJSONValue(element.pagePath, element.elementName);
    }

    /**
     * Prints out the values of all saved pages/panels
     */
    public static void displayPageJSONHash() {
        for (Map.Entry mapEntry : projectPages.entrySet()) {
            logger.info("project page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
        for (Map.Entry mapEntry : projectPages.entrySet()) {
            logger.info("shared page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
    }

    /**
     * Loads a JSON object from file into memory
     * <p>
     * This method first looks in your project's directory (passed as environment variable "project"), then in the
     * shared directories. If on BCOM, it will default to MCOM if no page is found.
     * </p>
     *
     * @param pagePath name of page to load (page path  home.logo = website.mcom.page.home)
     */
    public static void loadPageJSON(String pagePath) {
        String responsivePath = getResponsivePath(pagePath);
        if (responsivePath.startsWith("responsive")) {
            if (projectPages.get(responsivePath) != null || sharedPages.get(responsivePath) != null) {
                return;
            }
        }
        if (projectPages.get(pagePath) != null || sharedPages.get(pagePath) != null) {
            return;
        }

        String path = pagePath.replace(".page.", ".pages.").replace(".panel.", ".panels.").replace(".", "/");
        String resPath = "/elements/" + path + ".json";

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
        String responsivePage = getResponsivePath(page);
        ArrayList<String> projectPaths = new ArrayList<>();
        ArrayList<String> responsiveProjectPaths = new ArrayList<>();
        String sharedPath = RunConfig.workspace + RunConfig.sharedResourceDir + resPath;
        for (int i = 0; i < RunConfig.projectResourceDirs.size(); i++) {
            projectPaths.add(i, RunConfig.workspace + RunConfig.projectResourceDirs.get(i) + resPath);
            responsiveProjectPaths.add(getResponsivePath(projectPaths.get(i)));

            // project page first
            loadPageAndPanels(responsivePage, responsiveProjectPaths.get(i));
            loadPageAndPanels(page, projectPaths.get(i));
        }
        String sharedResponsivePath = getResponsivePath(sharedPath);


        // shared page
        loadPageAndPanels(responsivePage, sharedResponsivePath);
        loadPageAndPanels(page, sharedPath);

        // panels
        if (page.contains(".page.")) {
            page = page.replace(".page.", ".panel.");
            responsivePage = responsivePage.replace(".page.", ".panel.");
            for (int i = 0; i < projectPaths.size(); i++) {
                projectPaths.set(i, projectPaths.get(i).replace("/pages/", "/panels/"));
                responsiveProjectPaths.set(i, responsiveProjectPaths.get(i).replace("/pages/", "/panels/"));

                // project panel
                loadPageAndPanels(responsivePage, responsiveProjectPaths.get(i));
                loadPageAndPanels(page, projectPaths.get(i));
            }
            // shared panel
            sharedPath = sharedPath.replace("/pages/", "/panels/");
            sharedResponsivePath = sharedResponsivePath.replace("/pages/", "/panels/");
            loadPageAndPanels(responsivePage, sharedResponsivePath);
            loadPageAndPanels(page, sharedPath);
        }
    }

    private static void loadPageAndPanels(String pagePath, String filePath) {
        File f = new File(filePath);
        if (f.exists() && !f.isDirectory()) {
            loadPageJsonFiles(pagePath, f);
            return;
        }

        // find matching files recursively under the directory
        String fName = f.getName();
        File dir = f.getParentFile();
        List<File> files = findFiles(dir, fName);

        if (!files.isEmpty()) {
            if (files.size() == 1) {
                loadPageJsonFiles(pagePath, files.get(0));
            } else {
                Assert.fail("Resource Error: Multiple '" + fName + "'(total: " + files.size() + ") " +
                        " files found under '" + dir.getAbsolutePath() + "'");
            }
        }
    }

    /**
     * Recursively checks given directory and all subdirectories for a file matching given page name
     *
     * @param dir      directory
     * @param fileName file name
     * @return File matching given page name
     */
    private static List<File> findFiles(File dir, String fileName) {
        ArrayList<File> list = new ArrayList<>();
        File[] subDirs = dir.listFiles(File::isDirectory);
        File[] resources = dir.listFiles(File::isFile);
        if (resources != null) {
            for (File resource : resources) {
                if (resource.getName().equals(fileName)) {
                    list.add(resource);
                    // can't have files in one dir w/ same name, safe to break
                    break;
                }
            }
        }
        if (subDirs == null) {
            return list;
        }
        for (File subDir : subDirs) {
            list.addAll(findFiles(subDir, fileName));
        }
        return list;
    }

    /**
     * Loads the given page into the page cache
     *
     * @param pagePath full path to page
     * @param file     File with page JSON
     */
    private static void loadPageJsonFiles(String pagePath, File file) {
        JSONObject pageJson;
        try {
            pageJson = new JSONObject(Utils.readTextFile(file));
        } catch (IOException | JSONException e) {
           logger.error("-->Error parsing json at PageUtils.loadPageJSON() for page: " + file.getAbsolutePath());
           logger.debug("error parsing json: " + e);
           return;
        }

        // put new DataFile entry
        try {
            if (file.getCanonicalPath().replace(File.separator, "/").contains("shared/resources")) {
                sharedPages.put(pagePath, pageJson);
            } else {
                // if multiple projects have files w/ the same name we may hit a collision
                if (projectPages.get(pagePath) != null) {
                    logger.warn("Multiple definitions of page \"" + pagePath + "\" found. Elements may not load correctly.");
                    pageJson = mergeJsonObjects(projectPages.get(pagePath), pageJson);
                }
                projectPages.put(pagePath, pageJson);
            }
        } catch (IOException e) {
            sharedPages.put(pagePath, pageJson);
        }

        // process included Panel files
        JSONArray includedDataFiles;
        try {
            includedDataFiles = pageJson.getJSONArray("include");
        } catch (JSONException e) {
            // no 'include'
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
                logger.warn("issue in loading json : " + e.getMessage());
            }
        }
    }

    /**
     * Attempts to merge two JSONObjects, warning user about conflicts.
     * <p>
     * If conflict occurs, element from "first" JSONObject is kept
     * </p>
     *
     * @param first  base JSONObject
     * @param second other JSONObject
     * @return merged JSONObject
     */
    private static JSONObject mergeJsonObjects(JSONObject first, JSONObject second) {
        for (String key : second.keySet()) {
            if (first.has(key)) {
                logger.warn("Found duplicate element definition for \"" + key + "\": \""
                        + first.get(key) + "\" and \"" + second.get(key) + "\". Keeping \"" + first.get(key) + "\"");
            } else {
                first.put(key, second.get(key));
            }
        }
        return first;
    }

    /**
     * Gets element value from JSON object in memory - checks page and panel for given path
     *
     * @param pagePath    full path to the page or panel in question
     * @param elementName name of element to find
     * @return String value of given element
     */
    private static String findPageJSONValue(String pagePath, String elementName) {
        String value;
        value = getCachedElement(pagePath, elementName);

        // try panel
        if (value == null && pagePath.contains(".page.")) {
            value = getCachedElement(pagePath.replace(".page.", ".panel."), elementName);
        }

        // if bcom, try mcom
        if (value == null && pagePath.contains(".bcom.")) {
            String mcomPagePath = pagePath.replace(".bcom.", ".mcom.");
            value = getCachedElement(mcomPagePath, elementName);
            // try panel
            if (value == null && mcomPagePath.contains(".page.")) {
                value = getCachedElement(mcomPagePath.replace(".page.", ".panel."), elementName);
            }
        }
        return value;
    }

    /**
     * Gets the value of the element from the given page or included panels
     *
     * @param pagePath    full path to the page in question
     * @param elementName name of element to find
     * @return String value of given element
     */
    private static String getCachedElement(String pagePath, String elementName) {
        String value = null;
        String responsivePath = getResponsivePath(pagePath);
        JSONObject projectData = null;
        JSONObject sharedData = null;
        if (responsivePath.startsWith("responsive")) {
            projectData = projectPages.get(responsivePath);
            sharedData = sharedPages.get(responsivePath);
        }
        projectData = projectData == null ? projectPages.get(pagePath) : projectData;
        sharedData = sharedData == null ? sharedPages.get(pagePath) : sharedData;

        // check project if it has the page
        if (projectData != null) {
            value = getCachedElement(pagePath, elementName, projectData);
        }
        // if no good value from project, check shared if it has the page
        if (value == null && sharedData != null) {
            value = getCachedElement(pagePath, elementName, sharedData);
        }
        // warning for when no value found
        if (value == null && !elementName.matches("verify_page|url")) {
            // don't need to print verify_page and url elements as they come up empty a lot
            logger.debug("No value found for " + pagePath + "." + elementName);
        }
        return value;
    }

    /**
     * Gets the value of the element from the given page or included panels
     *
     * @param pagePath    full path to the page in question
     * @param elementName name of element to find
     * @param pageData    page JSONObject
     * @return String value of given element
     */
    private static String getCachedElement(String pagePath, String elementName, JSONObject pageData) {
        String value;
        try {
            value = pageData.getString(elementName);
            return value;
        } catch (JSONException ex) {
            // Element wasn't in the page, check included panels
            value = checkIncludedPanels(pagePath, elementName, pageData);
        }
        return value;
    }

    /**
     * Checks the page's included panels for the given element
     *
     * @param pagePath    full path to the page in question
     * @param elementName name of element to find
     * @param pageData    page JSONObject
     * @return String value of given element
     */
    private static String checkIncludedPanels(String pagePath, String elementName, JSONObject pageData) {
        String value;
        JSONArray includedDataFiles;
        try {
            includedDataFiles = pageData.getJSONArray("include");
        } catch (Exception e) {
            // no 'include'
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
                    value = getCachedElement(panelPath, elementName);
                    if (value != null) {
                        return value;
                    }
                }
            } catch (Exception e) {
                logger.warn("issue in check page's included panels for give element : " + e.getMessage());
            }
        }
        return null;
    }
}
