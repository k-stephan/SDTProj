package com.macys.sdt.framework.utils.analytics;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.macys.sdt.framework.utils.Utils;
import gherkin.formatter.model.Result;

import java.io.File;
import java.util.*;

public class DigitalAnalytics extends Analytics {

    public Map analyze(LinkedTreeMap scenarioInfo, int step, ArrayList entries, Result result) throws Exception {
        this.step_result = result;
        this.entries = this.convertHarEntries(entries);
        this.scenario_info = scenarioInfo;
        this.step = step;
        File fgold = new File(this.getGoldPath() + getGoldName(this.scenario_info));
        if (fgold.exists() && fgold.length() > 0) {
            try {
                if (this.gold == null) {
                    this.gold = new Gson().fromJson(Utils.readTextFile(fgold), LinkedTreeMap.class);
                }
                return test();
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("Error loading gold: " + fgold.getAbsolutePath());
                return record();
            }
        } else {
            return record();
        }
    }

    /**
     *  get all coremetrics call for the step from browser's coremetrics call
     *
     * @param entries all browser's coremetrics call for a step
     * @param bcount put tag count in tag_histogram if true else skip adding
     * @return all coremetrics call for the step from browser's coremetrics call
     */
    private HashMap getLastTagIds(ArrayList<LinkedTreeMap> entries, boolean bcount) {
        HashMap<String, ArrayList> hlastIds = new HashMap<>();
        if (bcount) {
            logger.info(this.getClass().getSimpleName() + ".getLastTagIds() : entries: " + entries.size());
        }
        for (LinkedTreeMap entry : entries) {
            // extract analytics data from query string in entry
            Map e = this.getAnalyticsData(entry);
            if (e.get("tid") == null) {
                continue;
            }

            String tid = e.get("tid").toString();
            ArrayList tentries = hlastIds.get(tid);
            if (tentries == null) {
                tentries = new ArrayList();
            }
            tentries.add(e);
            hlastIds.put(tid, tentries);
            if (bcount) {
                Integer count = this.tag_histogram.get(tid);
                if (count == null) {
                    count = 0;
                }
                this.tag_histogram.put(tid, ++count);
            }
        }
        return hlastIds;
    }

    /**
     * save diff data between gold and current coremetrics value in HashMap(hdiff) passed as argument
     *
     * @param hdiff HashMap having diff data between gold and current coremetrics value
     * @param tagid coremetrics tag id
     * @param golds gold coremetrics value
     * @param curs current coremetrics value
     */
    private void recordTagDiff(HashMap hdiff, String tagid, List<Map> golds, List<Map> curs) {
        for (int i = 0; i < curs.size(); i++) {
            Map gold = new HashMap();
            try {
                gold = golds.get(i);
                if (gold == null) {
                    gold = new HashMap();
                }
            } catch (Exception ex) {
                // nothing to do here
            }
            hdiff.put(i > 0 ? tagid + ":" + i : tagid, compareEntries(tagid, gold, curs.get(i)));
        }
    }

    protected Map test() throws Exception {
        HashMap hdiff = new HashMap();
        try {
            HashMap<String, List> hlgoldIds = getLastTagIds(getGoldStepHarEntries(), false);
            HashMap<String, List> hlcurIds = getLastTagIds(this.entries, true);

            // TODO : if a value is present in current but not in gold will be skipped from reporting =: to fix
            Set<String> tagIdsDiff = Sets.difference(hlgoldIds.keySet(), hlcurIds.keySet());
            for (String tagid : tagIdsDiff) {
                if (hlgoldIds.get(tagid) == null) {
                    recordTagDiff(hdiff, tagid, new ArrayList(), hlcurIds.get(tagid));
                } else {
                    recordTagDiff(hdiff, tagid, hlgoldIds.get(tagid), new ArrayList());
                }
            }

            Set<String> tagIdsIntersect = Sets.intersection(hlgoldIds.keySet(), hlcurIds.keySet());
            for (String tagid : tagIdsIntersect) {
                recordTagDiff(hdiff, tagid, hlgoldIds.get(tagid), hlcurIds.get(tagid));
            }

            if (!hdiff.isEmpty()) {
                logger.info(this.getClass().getSimpleName() + " Results: \n" + Utils.jsonPretty(hdiff) + "\n");
            }
            return hdiff;
        } finally {
            HashMap hstepResult = new HashMap();
            if (this.entries.size() > 0) {
                Map lastEntry = this.entries.get(this.entries.size() - 1);
                hstepResult.put("pageref", lastEntry.get("pageref"));
                hstepResult.put("cookies", ((Map) lastEntry.get("request")).get("cookies"));
            } else {
                hstepResult.put("pageref", "");
                hstepResult.put("cookies", "");
            }
            hstepResult.put("tids", hdiff);
            hstepResult.put("page_sources", this.step_page_sources);
            hstepResult.put("click_elements", this.step_click_elements);
            HashMap<Integer, HashMap> hsteps = (HashMap) this.results.get("steps");
            if (hsteps == null) {
                hsteps = new HashMap();
                this.results.put("steps", hsteps);
            }
            hstepResult.put("result", this.step_result);
            hsteps.put(this.step, hstepResult);

            this.results.put("scenario", this.scenario_info);


            step_click_elements = new ArrayList();
            step_page_sources = new HashMap();
        }
    }

    /**
     * extract analytics data as Map (from query string from request) from browser's call to coremetrics
     *
     * @param entryQuery browser's coremetrics call
     *
     * @return analytics data (extracted from query string) as Map
     */
    protected Map getAnalyticsData(Map entryQuery) {
        HashMap data = new HashMap();
        Map request = (Map) entryQuery.get("request");
        ArrayList<Map> list = (ArrayList) request.get("queryString");
        for (Map vpair : list) {
            String val = vpair.get("value").toString();
            if (vpair.get("action") != null) {
                val = "_" + vpair.get("action") + "_" + val;
            }
            data.put(vpair.get("name"), val);
        }
        return data;
    }


}
