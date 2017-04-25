package com.macys.sdt.framework.utils.analytics;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to profile the load times of various pages
 */
public class PageLoadProfiler {

    private static long startTime;
    private static long lastLoadTime;
    private static String lastUrl;

    private static final Logger logger = LoggerFactory.getLogger(PageLoadProfiler.class);

    private PageLoadProfiler(){}

    public static void startTimer() {
        startTime = System.currentTimeMillis();
        lastUrl = MainRunner.currentURL;
    }

    public static void stopAndReport() {
        if (MainRunner.currentURL.equals(lastUrl)) {
            lastLoadTime = 0;
        } else {
            lastLoadTime = System.currentTimeMillis() - startTime;
            printLoadTime();
        }
    }

    public static void printLoadTime() {
        logger.info("Page load time: " + Utils.toDuration(lastLoadTime));
        lastLoadTime = 0;
    }

    public static long getLastLoadTime() {
        return lastLoadTime;
    }

}
