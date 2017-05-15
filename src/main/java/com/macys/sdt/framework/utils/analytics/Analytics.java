package com.macys.sdt.framework.utils.analytics;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.utils.Utils;
import gherkin.formatter.model.Result;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Analytics {

    protected Logger logger = LoggerFactory.getLogger(Analytics.class);

    protected ArrayList<LinkedTreeMap> entries;
    protected LinkedTreeMap gold;
    protected LinkedTreeMap scenario_info;
    protected int step;
    protected Result step_result;
    protected ArrayList<String> step_click_elements = new ArrayList<>();
    protected HashMap<String, String> step_page_sources = new HashMap<>();
    protected HashMap<String, Object> results = new HashMap<>();
    protected List<String> global_ignores = new ArrayList<>();
    protected List<String> global_has_values = new ArrayList<>();
    protected Map<String, String> global_values = new HashMap<>();
    protected HashMap<String, Integer> tag_histogram = new HashMap<>();

    public Analytics() {
        this.loadGlobals();
    }

    public static String getGoldName(Map sInfo) {
        String feature = sInfo.get("uri").toString().replace('\\', '/').split("features/")[1];
        return Utils.getScenarioShaKey(feature, sInfo.get("name").toString());
    }

    public abstract Map analyze(LinkedTreeMap scenarioInfo, int step, ArrayList entries, Result result) throws Exception;

    protected abstract Map getAnalyticsDataDiff() throws Exception;

    public void recordPageSource(String link, String pageSource) {
        step_page_sources.put(link, pageSource);
    }

    /**
     * read analytics global data file (xxx_global.json)
     * and set global variables ("global_ignores", "global_has_values", "global_values") to corresponding values present in the file
     */
    protected void loadGlobals() {
        try {
            String site_type = RunConfig.getEnvVar("site_type");
            if (site_type == null)  {
                logger.warn("site_type absent");
                return;
            }
            File fglobal = new File(getGoldPath() + site_type.toLowerCase() + "_global.json");
            if (fglobal.exists()) {
                // read analytics global data file and convert to Map
                Map globals = new Gson().fromJson(Utils.readTextFile(fglobal), Map.class);

                // assign values "ignore", "has_value" and "update" to global variables
                if (globals.get("ignore") != null) {
                    this.global_ignores = (List) globals.get("ignore");
                }
                if (globals.get("has_value") != null) {
                    this.global_has_values = (List) globals.get("has_value");
                }
                if (globals.get("update") != null) {
                    this.global_values = (Map) globals.get("update");
                }
            } else {
                logger.info("Global analytics data file does not exist.");
            }
        }  catch (Exception e) {
             logger.warn("Cannot load analytics global data file : " + e.getMessage());
        }
    }

    public void recordClickElement(String elHtml) {
        step_click_elements.add(elHtml);
    }

    /**
     * This method records analytics data fired
     *
     * @return Map of analytics data
     */
    protected Map record() {
        if (gold == null) {
            gold = new LinkedTreeMap();
            gold.put("0", this.scenario_info);
        }
        logger.info("recording analytics for step " + (this.step - 1));
        HashMap<String, Object> hrecord = new HashMap<>();
        hrecord.put("har_entries", this.entries);
        hrecord.put("tag_check", new HashMap());
        gold.put(this.step + "", hrecord);
        logger.debug("recorded analytics data : \n" + hrecord);
        return hrecord;
    }

    /**
     * Create a gold directory and return the directory path. If directory already exists return path of the gold directory
     *
     * @return path of gold directory
     * @throws IOException io related exception
     */
    public String getGoldPath() throws IOException {
        return Utils.createDirectory(RunConfig.workspace + "/golds").getCanonicalPath() + "/";
    }

    /**
     * Flush the analytics data in a file.
     * If gold file does not exist, it will create gold file with generated analytics data.
     * If gold file exist, it will create analytics result file of comparison between gold and current run data for coremetrics report.
     *
     * @param isScenarioPassed whether the current scenario run is passed or failed
     * @throws IOException when issue with flushing data
     */
    public void flush(boolean isScenarioPassed) throws IOException {
        File fgold = new File(getGoldPath() + getGoldName(this.scenario_info));
        if (!fgold.exists() || fgold.length() == 0) { // gold file does not exist
            if (isScenarioPassed) {
                File flogGold = Utils.createDirectory(RunConfig.logs + "/golds");
                fgold = new File(flogGold.getCanonicalPath() + "/" + fgold.getName());

                logger.info("writing gold file...");
                // write gold file
                Utils.writeSmallBinaryFile(new Gson().toJson(this.gold).getBytes(), fgold);
                logger.info("Flushing recorded analytics as gold: " + fgold.getCanonicalPath());

                logger.info("writing tag histogram file...");
                // write tag histogram in json file
                Utils.writeSmallBinaryFile(new Gson().toJson(this.tag_histogram).getBytes(), new File(RunConfig.logs + fgold.getName() + ".tag_histogram.json"));
            } else {
                logger.info("Scenario did not pass. Skip gold recording.");
            }
            this.gold = null;
        } else { // gold file exist hence comparision data between gold and current execution
            File fresult = new File(RunConfig.logs + fgold.getName() + ".analytics.result.json");
            Utils.writeSmallBinaryFile(new Gson().toJson(this.results).getBytes(), fresult);
            logger.info("Flushing analytics comparision results: " + fresult.getCanonicalPath());
            this.results = new HashMap<>();

            logger.info("writing tag histogram file...");
            // write tag histogram in json file
            Utils.writeSmallBinaryFile(new Gson().toJson(this.tag_histogram).getBytes(), new File(RunConfig.logs + fgold.getName() + ".tag_histogram.json"));
        }

        this.gold = null;
        this.tag_histogram.clear();
    }

    /**
     * convert harentry present in arraylist to json format
     *
     * @param harEntries arraylist containg har entries
     *
     * @return har entries
     */
    protected ArrayList convertHarEntries(ArrayList harEntries) {
        return this.entries = new Gson().fromJson(new Gson().toJson(harEntries), ArrayList.class);
    }

    /**
     * compare analytics data of gold and current execution for a tag
     *
     * @param tagid coremetrics tag id
     * @param gmap gold analytics value
     * @param cmap current analytics value
     * @return comparison data between gold and current analytics data for a tag
     */
    protected Map compareEntries(String tagid, Map gmap, Map cmap) {
        Set gset = gmap.keySet();
        Set cset = cmap.keySet();

        HashMap hdiff = new HashMap();
        hdiff.putAll(saveDiff(Sets.intersection(gset, cset).iterator(), gmap, cmap, tagid));
        hdiff.putAll(saveDiff(Sets.difference(gset, cset).iterator(), gmap, cmap, tagid));
        hdiff.putAll(saveDiff(Sets.difference(cset, gset).iterator(), gmap, cmap, tagid));
        return hdiff;
    }

    /**
     * get "har_entries" data stored in scenario gold file
     * gold file value stored in global variable "gold"
     * step is the step index stored in global variable "step"
     *
     * @return "har_entries" data stored in scenario gold file
     * @throws Exception throw unexpected exceptions
     */
    protected ArrayList getGoldStepHarEntries() throws Exception {
        Map record = (Map) this.gold.get(this.step + "");
        if (record == null) {
            throw new Exception("ERROR : cannot find gold step: " + (this.step - 1));
        }
        return (ArrayList) record.get("har_entries");
    }

    /**
     * strip the input string of any whitespace character
     *
     * @param s input string
     * @return input string stripped of any whitespace character
     */
    private String stripNonChars(String s) {
        return s.trim().toLowerCase().replaceAll("\\s", "");
    }

    /**
     * compare the gold and current coremetrics value by checking if current value contains gold value
     *
     * @param cval current run coremetrics value
     * @param gval gold coremetrics value
     *
     * @return true if value is equal
     */
    private boolean compareEqual(String cval, String gval) {
        String gcomp = stripNonChars(gval);
        String ccomp = stripNonChars(cval);
        return ccomp.contains(gcomp);
    }

    private Object compareElementClicks(String cval) {
        Object res;
        if (cval == null || cval.isEmpty()) {
            return null;
        }

        res = findInClickElements(cval, this.step_click_elements);
        if (res != null) {
            return res;
        }

        Map hsteps = (Map) this.results.get("steps");
        if (hsteps == null) {
            return null;
        }

        for (int step = this.step - 1; step > 0; step--) {
            Map hstep = (Map) hsteps.get(step);
            List<String> clickEls = (List) hstep.get("click_elements");
            res = findInClickElements(cval, clickEls);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private String findInClickElements(String cval, List<String> clickEls) {
        String ccomp = stripNonChars(cval);
        for (String eclick : clickEls) {
            if (eclick.contains(cval)) {
                return eclick;
            }
            if (stripNonChars(eclick).contains(ccomp)) {
                return eclick;
            }
            Document doc = Jsoup.parse(eclick);
            if (doc.text().contains(cval)) {
                return eclick;
            }
            if (stripNonChars(doc.text()).contains(ccomp)) {
                return eclick;
            }
        }
        return null;
    }

    private Object comparePageSrc(String cval) {
        Object res = findInPageSrc(cval, this.step_page_sources);
        if (res != null) {
            return res;
        }

        Map hsteps = (Map) this.results.get("steps");
        if (hsteps == null) {
            return null;
        }

        for (int step = this.step - 1; step > 0; step--) {
            Map hstep = (Map) hsteps.get(step);
            Map pageSrcs = (Map) hstep.get("page_sources");
            res = findInPageSrc(cval, pageSrcs);
            if (res != null) {
                return res;
            }
        }

        return null;
    }

    private Object findInPageSrc(String cval, Map pageSrcs) {
        String ccomp = stripNonChars(cval);
        for (Object o : pageSrcs.keySet()) {
            String key = o.toString();
            String html = this.step_page_sources.get(key);
            if (html == null) {
                continue;
            }
            if (html.contains(cval)) {
                return key;
            }
            if (stripNonChars(html).contains(ccomp)) {
                return key;
            }
            Document doc = Jsoup.parse(html);
            html = doc.text();
            if (stripNonChars(html).contains(ccomp)) {
                return key;
            }
        }
        return null;
    }

    private String getCurrentURL() {
        return WebDriverManager.getCurrentUrl();
    }

    /**
     * This method compare coremetrics gold value with current execution value
     *
     * @param hdiff hashmap having value comparison status with other information
     * @param tagAttr tag attribute
     * @param gval coremetrics gold value
     * @param cval coremetrics current execution value
     * @param tagid coremetrics tag id
     */
    private void compareFilter(HashMap hdiff, String tagAttr, String gval, String cval, String tagid) {
        HashMap compare = new HashMap();
        compare.put("gold", gval);
        compare.put("run", cval);
        HashMap hresult = new HashMap();
        hresult.put("compare", compare);

        try {
            if (tagAttr.equals("ul")) {
                String page_url = getCurrentURL();
                if (cval.equals(page_url)) {
                    hresult.put("action", "page_url: " + page_url);
                    hresult.put("status", "pass");
                } else {
                    hresult.put("status", "fail");
                }
            } else {

                // when tag attribute is updated globally
                String globalValue = this.global_values.get(tagAttr);
                if (globalValue != null) {
                    compare.put("gold", globalValue);
                    hresult.put("action", "update_global");
                    hresult.put("status", globalValue.equals(cval) ? "pass" : "fail");
                    return;
                }

                // when tag attribute is updated globally for a specific tag
                globalValue = this.global_values.get(tagid + "." + tagAttr);
                if (globalValue != null) {
                    compare.put("gold", globalValue);
                    hresult.put("action", "update_global_tag");
                    hresult.put("status", globalValue.equals(cval) ? "pass" : "fail");
                    return;
                }

                Object res;

                if (gval.startsWith("_ignore_") ||
                        this.global_ignores.contains("all." + tagAttr) ||
                        this.global_ignores.contains(tagid + "." + tagAttr)) {
                    // when tag attribute is to be ignored
                    // default : when specific tag attribute is ignored
                    String action = "ignore";
                    if (this.global_ignores.contains("all." + tagAttr)) {
                        // when tag attribute is ignored globally
                        action = "ignore_global";
                    } else if (this.global_ignores.contains(tagid + "." + tagAttr)) {
                        // when tag attribute is ignored globally for a specific tag
                        action = "ignore_global_tag";
                    }
                    hresult.put("action", action);
                    hresult.put("status", "pass");
                } else if (gval.startsWith("_has_value_") ||
                        this.global_has_values.contains("all." + tagAttr) ||
                        this.global_has_values.contains(tagid + "." + tagAttr)) {

                    // has value scenario where tag attribute needs to have value irrespective to actual value
                    // here for all has_value scenario action is set to "has_value"
                    if (cval != null && !cval.isEmpty()) {
                        hresult.put("status", "pass");
                    } else {
                        hresult.put("status", "fail");
                    }
                    hresult.put("action", "has_value");
                } else if (cval.isEmpty() || gval.isEmpty())   {
                    if (cval.isEmpty() && gval.isEmpty())   {
                        // when both current and gold value are empty
                        hresult.put("action", "current and gold value are empty");
                        hresult.put("status", "pass");
                    } else if (cval.isEmpty() && !gval.isEmpty()) {
                        // when current value is empty
                        hresult.put("action", "current value is empty");
                        hresult.put("status", "fail");
                    } else {
                        // when gold value is empty
                        hresult.put("action", "gold value is empty");
                        hresult.put("status", "fail");
                    }
                } else if (compareEqual(cval, gval)) {
                    // when current value contains gold value, value stripped of any whitespace character
                    hresult.put("status", "pass");
                } else if (cval.contains(gval)) {
                    // when current value contains gold value
                    hresult.put("action", "currentValue_contains_goldValue");
                    hresult.put("status", "pass");
                } else if (gval.contains(cval)) {
                    // when gold value contains current value
                    hresult.put("status", "pass");
                    hresult.put("action", "goldValue_contains_currentValue");
                } else if ((res = compareElementClicks(cval)) != null) {
                    hresult.put("status", "pass");
                    hresult.put("action", "element_click:" + res);
                } else if ((res = comparePageSrc(cval)) != null) {
                    hresult.put("status", "pass");
                    hresult.put("action", "page_src:" + res);
                } else {
                    hresult.put("status", "fail");
                }
            }
        } finally {
            // add tag attribute with result information
            hdiff.put(tagAttr, hresult);
        }
    }

    /**
     * it compares attributes present in gold and current tag value for a specific tagid
     *
     * @param it iterator on which basis elements will be checked in gold and current
     * @param gmap gold values for a tag
     * @param cmap current values for a tag
     * @param tagid tag id
     *
     * @return diff data of gold and current attribute value of a tag value
     */
    private HashMap saveDiff(Iterator it, Map gmap, Map cmap, String tagid) {
        HashMap hdiff = new HashMap();
        while (it.hasNext()) {
            String tagAttr = it.next().toString();
            String gval = (String) gmap.get(tagAttr);
            if (gval == null) {
                gval = "";
            }
            String cval = (String) cmap.get(tagAttr);
            if (cval == null) {
                cval = "";
            }
            compareFilter(hdiff, tagAttr, gval, cval, tagid);
        }
        return hdiff;
    }

    /**
     * check if a string is email or not
     *
     * @param email email string to check for
     * @return true if the value input is email string
     */
    public boolean emailValidator(String email) {
        if (email == null) {
            return false;
        }
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
