package com.macys.sdt.framework.utils.analytics;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.runner.MainRunner;
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

    private Logger logger = LoggerFactory.getLogger(Analytics.class);

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

    protected abstract Map test() throws Exception;

    public void recordPageSource(String link, String pageSource) {
        step_page_sources.put(link, pageSource);
    }

    protected void loadGlobals() {
        try {
            File fglobal = new File(getGoldPath() + MainRunner.getEnvVar("site_type").toLowerCase() + "_global.json");
            if (fglobal.exists()) {
                Map globals = new Gson().fromJson(Utils.readTextFile(fglobal), Map.class);
                if (globals.get("ignore") != null) {
                    this.global_ignores = (List) globals.get("ignore");
                }
                if (globals.get("has_value") != null) {
                    this.global_has_values = (List) globals.get("has_value");
                }
                if (globals.get("update") != null) {
                    this.global_values = (Map) globals.get("update");
                }
            }
        } catch (Exception ex) {
            System.out.println("Cannot load global data: " + ex.getMessage());
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
        System.out.println("INFO : recording analytics for step " + (this.step - 1));
        HashMap<String, Object> hrecord = new HashMap<>();
        hrecord.put("har_entries", this.entries);
        hrecord.put("tag_check", new HashMap());
        gold.put(this.step + "", hrecord);
        logger.debug("recorded analytics data : \n" + hrecord);
        return hrecord;
    }

    public String getGoldPath() throws IOException {
        return Utils.createDirectory(MainRunner.workspace + "/golds").getCanonicalPath() + "/";
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
        if (!fgold.exists() || fgold.length() == 0) {
            if (isScenarioPassed) {
                File flogGold = Utils.createDirectory(MainRunner.logs + "/golds");
                fgold = new File(flogGold.getCanonicalPath() + "/" + fgold.getName());
                Utils.writeSmallBinaryFile(new Gson().toJson(this.gold).getBytes(), fgold);
                System.out.println("INFO : Flushing recorded analytics as gold: " + fgold.getCanonicalPath());
            } else {
                System.out.println("INFO : Scenario did not pass.  Skip gold recording.");
            }
            this.gold = null;
        } else {
            File fresult = new File(MainRunner.logs + fgold.getName() + ".analytics.result.json");
            Utils.writeSmallBinaryFile(new Gson().toJson(this.results).getBytes(), fresult);
            System.out.println("INFO : Flushing analytics results: " + fresult.getCanonicalPath());
            this.results = new HashMap();
        }
        Utils.writeSmallBinaryFile(new Gson().toJson(this.tag_histogram).getBytes(), new File(MainRunner.logs + fgold.getName() + ".tag_histogram.json"));
        this.gold = null;
        this.tag_histogram.clear();
    }

    protected ArrayList convertHarEntries(ArrayList harEntries) {
        return this.entries = new Gson().fromJson(new Gson().toJson(harEntries), ArrayList.class);
    }

    protected Map compareEntries(String tagid, Map gmap, Map cmap) {
        Set gset = gmap.keySet();
        Set cset = cmap.keySet();

        HashMap hdiff = new HashMap();
        hdiff.putAll(saveDiff(Sets.intersection(gset, cset).iterator(), gmap, cmap, tagid));
        hdiff.putAll(saveDiff(Sets.difference(gset, cset).iterator(), gmap, cmap, tagid));
        return hdiff;
    }

    protected ArrayList getGoldStepHarEntries() throws Exception {
        Map record = (Map) this.gold.get(this.step + "");
        if (record == null) {
            throw new Exception("ERROR : cannot find gold step: " + (this.step - 1));
        }
        return (ArrayList) record.get("har_entries");
    }

    private String stripNonChars(String s) {
        return s.trim().toLowerCase().replaceAll("\\s", "");
    }

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
                    hresult.put("action", "page_url:" + page_url);
                    hresult.put("status", "pass");
                }
            } else {
                String globalValue = this.global_values.get(tagAttr);
                if (globalValue != null) {
                    compare.put("gold", globalValue);
                    hresult.put("action", "update_global");
                    hresult.put("status", globalValue.equals(cval) ? "pass" : "fail");
                    return;
                }

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
                    String action = "ignore";
                    if (this.global_ignores.contains("all." + tagAttr)) {
                        action = "ignore_global";
                    } else if (this.global_ignores.contains(tagid + "." + tagAttr)) {
                        action = "ignore_global_tag";
                    }
                    hresult.put("action", action);
                    hresult.put("status", "pass");
                } else if (gval.startsWith("_has_value_") ||
                        this.global_has_values.contains("all." + tagAttr) ||
                        this.global_has_values.contains(tagid + "." + tagAttr)) {
                    if (cval != null && !cval.isEmpty()) {
                        hresult.put("status", "pass");
                    } else {
                        hresult.put("status", "fail");
                    }
                    hresult.put("action", "has_value");
                } else if (compareEqual(cval, gval)) {
                    hresult.put("status", "pass");
                } else if (cval.contains(gval)) {
                    hresult.put("action", "val_contains_gold");
                    hresult.put("status", "pass");
                } else if (gval.contains(cval)) {
                    hresult.put("status", "pass");
                    hresult.put("action", "gold_contains_val");
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
            hdiff.put(tagAttr, hresult);
        }
    }

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

    public boolean attributeFormatValidation_emailValidator(String email) {
        if (email == null) {
            return false;
        }
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static class AnalyticsException extends Exception {
        private static final long serialVersionUID = -5394782789087798477L;

        public AnalyticsException(String msg) {
            super(msg);
        }
    }
}
