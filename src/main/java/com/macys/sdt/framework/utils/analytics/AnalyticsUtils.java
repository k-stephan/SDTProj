package com.macys.sdt.framework.utils.analytics;

import com.google.gson.Gson;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.ScenarioHelper;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarRequest;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.macys.sdt.framework.utils.StepUtils.harBuffer;

public class AnalyticsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsUtils.class);


    /**
     * This method will return coremetrics call converted to HAR format
     *
     * @return List of coremetrics call converted to HarEntry
     */
    public static List<HarEntry> getCoremetricsData() {
        // get current captured har values
        Har har = MainRunner.browsermobServer.getHar();

        List<HarEntry> entries = new ArrayList<>();
        try {
            for (HarEntry harEntry : har.getLog().getEntries()) {
                HarRequest request = harEntry.getRequest();
                if (request.getUrl().contains("cm?")) {
                    entries.add(harEntry);
                }
            }
            return entries;
        } catch (Exception e) {
            logger.error("Issue in retrieving coremetrics data due to : " + e);
            return null;
        }
    }

    /**
     * This method will search for a key in a coremetrics call converted to HAR
     *
     * @param harEntry the coremetrics call converted to HAR
     * @param key search key
     * @return value (if exist) otherwise empty string
     */
    public static String getSpecificAnalyticsValue(HarEntry harEntry, String key) {
        logger.info("search key : " + key);
        HarRequest request = harEntry.getRequest();

        if (request == null) {
            logger.trace("har entry request is null");
            return "";
        }

        List<HarNameValuePair> queryString = request.getQueryString();
        for (HarNameValuePair harNameValuePair : queryString) {
            if (harNameValuePair.getName().equals(key)) {
                return harNameValuePair.getValue();
            }
        }
        logger.info("search key is absent");
        return "";
    }

    /**
     * If analytics is enabled, collect http archive data from this step and analyze it
     */
    public static void collectAnalyticsData() {
        Har har = MainRunner.browsermobServer.newHar();
        if (RunConfig.analytics == null) {
            return;
        }
        int step = ScenarioHelper.getScenarioIndex();
        if (step == 0) {
            return;
        }

        ArrayList<HarEntry> entries = new ArrayList<>();
        try {
            for (HarEntry harEntry : har.getLog().getEntries()) {
                HarRequest request = harEntry.getRequest();
                if (request.getUrl().contains("cm?")) {
                    entries.add(harEntry);
                }
            }
            harBuffer = entries;
            RunConfig.analytics.analyze(ScenarioHelper.getScenarioInfo(), step, entries, ScenarioHelper.getLastStepResult());
        } catch (Throwable ex) {
            logger.trace("collect analytics data issue : " + ex);
            Assert.fail(RunConfig.analytics.getClass().getSimpleName() + " test failed: " + ex.getMessage());
        }
    }

    /**
     * If analytics is enabled, collect http archive data for the last step and get it
     * @return list of har entries
     */
    public static ArrayList getHarBuffer() {
        if (RunConfig.analytics == null) {
            return null;
        }

        return new Gson().fromJson(new Gson().toJson(harBuffer), ArrayList.class);
    }

    /**
     * Flushes all analytics data
     */
    public static void flushAnalytics() {
        if (RunConfig.analytics == null) {
            return;
        }
        try {
            RunConfig.analytics.flush(ScenarioHelper.isScenarioPassed());
        } catch (IOException e) {
            logger.debug("issue in flushing analytics data due to : " + e.getMessage());
        }
    }

    /**
     * Gets the browsermob HTTP Archive
     *
     * @return browsermob har
     */
    public static Har getHar() {
        try {
            Har har = MainRunner.browsermobServer.getHar();
            har.writeTo(new File(ScenarioHelper.scenario.getName() + "." + ScenarioHelper.getScenarioIndex() + ".har"));
            return har;
        } catch (Throwable ex) {
            logger.warn("get HAR issue : " + ex.getMessage());
            return null;
        }
    }

}
