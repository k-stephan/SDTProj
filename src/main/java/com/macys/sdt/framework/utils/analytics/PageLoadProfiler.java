package com.macys.sdt.framework.utils.analytics;

/**
 * Used to profile the load times of various pages
 */
public class PageLoadProfiler {

    private static long startTime;
    private static long lastLoadTime;

    public static void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public static void stopTimer() {
        lastLoadTime = System.currentTimeMillis() - startTime;
    }

    public static long getLoadTime() {
        return lastLoadTime;
    }

}
