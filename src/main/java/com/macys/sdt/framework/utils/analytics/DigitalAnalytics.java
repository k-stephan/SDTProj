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
                return getAnalyticsDataDiff();
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
     * get all coremetrics call for the step from browser's coremetrics call
     *
     * @param entries all browser's coremetrics call for a step (har entries)
     * @param shouldAddCountInTagHistogram put tag count in tag_histogram if true else skip adding
     * @return all coremetrics call for the step (in form of HashMap tagId and value) stored in argument "entries"
     */
    private HashMap<String, List> getTagIds(ArrayList<LinkedTreeMap> entries, boolean shouldAddCountInTagHistogram) {
        HashMap<String, List> hlastIds = new HashMap<>();

        logger.info("getTagIds() : entries: " + entries.size());

        for (LinkedTreeMap entry : entries) {

            // extract analytics data from "queryString" in entry
            Map<String, String> e = this.getAnalyticsData(entry);

            if (e.get("tid") == null) {
                continue;
            }

            String tid = e.get("tid");
            List<Map> tentries = hlastIds.get(tid);
            if (tentries == null) {
                tentries = new ArrayList<>();
            }
            tentries.add(e);
            hlastIds.put(tid, tentries);

            // put "tid" specific count in tag_histogram
            if (shouldAddCountInTagHistogram) {
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

    /**
     * This method extract and compare overall analytics data of gold and current execution for a step execution
     *
     * @return overall diff data of gold and current execution for a step
     * @throws Exception in case of error
     */
    protected Map getAnalyticsDataDiff() throws Exception {
        HashMap hdiff = new HashMap();
        try {
            HashMap<String, List> hlgoldTagIds = getTagIds(getGoldStepHarEntries(), false);
            HashMap<String, List> hlcurrentTagIds = getTagIds(this.entries, true);

            // record tagid present in gold but not in current execution
            Set<String> tagIdsDiffGold = Sets.difference(hlgoldTagIds.keySet(), hlcurrentTagIds.keySet());
            for (String tagid : tagIdsDiffGold) {
                recordTagDiff(hdiff, tagid, hlgoldTagIds.get(tagid), new ArrayList());
            }

            // record tagid present in current execution but not in gold
            Set<String> tagIDsDiffCurrent = Sets.difference(hlcurrentTagIds.keySet(), hlgoldTagIds.keySet());
            for (String tagid : tagIDsDiffCurrent) {
                recordTagDiff(hdiff, tagid, new ArrayList(), hlcurrentTagIds.get(tagid));
            }

            // record tagid present in both gold and current execution
            Set<String> tagIdsIntersect = Sets.intersection(hlgoldTagIds.keySet(), hlcurrentTagIds.keySet());
            for (String tagid : tagIdsIntersect) {
                recordTagDiff(hdiff, tagid, hlgoldTagIds.get(tagid), hlcurrentTagIds.get(tagid));
            }

            if (!hdiff.isEmpty()) {
                logger.info("Analytics Compare Result: \n" + Utils.jsonPretty(hdiff) + "\n");
            } else {
                logger.info("gold and current analytics data are same. No Diff!!");
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
     * extract analytics data as Map (from "queryString" from request) from browser's call to coremetrics
     *
     * @param entryQuery browser's coremetrics call
     *
     * @return analytics data (extracted from query string) as Map
     */
    protected Map<String, String> getAnalyticsData(Map entryQuery) {
        HashMap<String, String> data = new HashMap<>();
        Map request = (Map) entryQuery.get("request");
        ArrayList<Map> list = (ArrayList) request.get("queryString");
        for (Map vpair : list) {
            String val = vpair.get("value").toString();
            if (vpair.get("action") != null) {
                val = "_" + vpair.get("action") + "_" + val;
            }
            data.put((String) vpair.get("name"), val);
        }
        return data;
    }


}
