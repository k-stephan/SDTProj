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

    /**
     * Prints out the values of all saved pages/panels
     */
    public static void displayPageJSONHash() {
        for (Map.Entry mapEntry : projectPages.entrySet()) {
            System.out.println("project page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
        for (Map.Entry mapEntry : projectPages.entrySet()) {
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
        String responsivePage = getResponsivePath(page);
        String projectPath = MainRunner.workspace + MainRunner.projectDir + resPath;
        String sharedPath = MainRunner.workspace + "shared" + resPath;
        String responsivePath = getResponsivePath(projectPath);
        String sharedResponsivePath = getResponsivePath(sharedPath);
        // project elements first
        loadPageAndPanels(responsivePage, responsivePath);
        loadPageAndPanels(page, projectPath);

        // shared elements
        loadPageAndPanels(responsivePage, sharedResponsivePath);
        loadPageAndPanels(page, sharedPath);

        // also load panel elements
        if (page.contains(".page.")) {
            projectPath = projectPath.replace("/pages/", "/panels/");
            responsivePath = responsivePath.replace("/pages/", "/panels/");
            loadPageAndPanels(responsivePage, responsivePath);
            loadPageAndPanels(page, projectPath);

            sharedPath = sharedPath.replace("/pages/", "/panels/");
            responsivePath = sharedResponsivePath.replace("/pages/", "/panels/");
            loadPageAndPanels(sharedResponsivePath, responsivePath);
            loadPageAndPanels(page, sharedPath);
        }
    }

    private static boolean loadPageAndPanels(String pagePath, String filePath) {
        File f = new File(filePath);
        if (f.exists() && !f.isDirectory()) {
            loadPageJsonFiles(pagePath, f);
            return true;
        }

        // find file recursively under the directory
        String fName = f.getName();
        File dir = f.getParentFile();
        f = findFile(dir, fName);
        if (filePath.contains("shared/") && f == null) {
            dir = new File("com/macys/sdt/" + dir.getPath());
            f = findFile(dir, fName);
        }

        if (f != null && f.exists() && !f.isDirectory()) {
            int fileCount = countFoundPages(dir, fName);
            if (fileCount < 1) {
                return false;
            }
            if (fileCount == 1) {
                loadPageJsonFiles(pagePath, f);
                return true;
            } else {
                Assert.fail("Resource Error: Multiple '" + fName + "'(total: " + fileCount + ") " +
                        " files found under '" + dir.getAbsolutePath() + "'");
            }
        }
        return false;
    }

    /**
     * Recursively checks all subdirectories for a file matching given page name
     *
     * @param dir directory
     * @param pageName file name or page name
     * @return File matching given page name
     */
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

    /**
     * Recursively checks all subdirectories to count files matching given page name
     *
     * @param dir directory
     * @param pageName file name or page name
     * @return number of same file name found
     */
    private static int countFoundPages(File dir, String pageName) {
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
            int subCount = countFoundPages(subDir, pageName);
            count += subCount;
        }
        return count;
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
            System.err.println("-->Error parsing json at PageUtils.loadPageJSON() for page: " + file.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        // put new DataFile entry
        try {
            if (file.getCanonicalPath().replace("/", ".").replace("\\", ".").contains(MainRunner.project)) {
                projectPages.put(pagePath, pageJson);
            } else {
                sharedPages.put(pagePath, pageJson);
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
                e.printStackTrace();
            }
        }
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
        if (value == null) {
            log.debug("No value found for " + pagePath + "." + elementName);
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
                e.printStackTrace();
            }
        }
        return null;
    }
}
