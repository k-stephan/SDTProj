package com.macys.sdt.framework.utils.analytics;

import com.macys.sdt.framework.runner.MainRunner;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CoremetricsUtils {

    private static final Logger logger = LoggerFactory.getLogger(CoremetricsUtils.class);


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
    public static String getCoremetricsValue(HarEntry harEntry, String key) {
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

}
