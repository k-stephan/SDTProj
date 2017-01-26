package com.macys.sdt.framework.runner;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of Default wait Time in Seconds
 */
public class Timeouts {

    private Timeouts() {}

    private static final int DEFAULT_GENERAL_TIMEOUT = 5;
    private static final int DEFAULT_UNTIL_ELEMENT_PRESENT_TIMEOUT = 5;
    private static final String GENERAL_TIMEOUT_KEY = "general_timeout";
    private static final String UNTIL_ELEMENT_PRESENT_TIMEOUT_KEY = "until_element_present_timeout";

    private static Map<String, Integer> timeouts = new HashMap<>();
    private static Timeouts instance;

    /**
     * Gets Default wait Time for element
     *
     * @return Default wait Time for element
     */
    public int untilElementPresent() {
        return getTimeout(UNTIL_ELEMENT_PRESENT_TIMEOUT_KEY, DEFAULT_UNTIL_ELEMENT_PRESENT_TIMEOUT);
    }

    /**
     * Gets Default general wait Time
     *
     * @return Default general wait Time
     */
    public int general() {
        return getTimeout(GENERAL_TIMEOUT_KEY, DEFAULT_GENERAL_TIMEOUT);
    }

    public static Timeouts instance() {
        if (instance == null) {
            instance = new Timeouts();
        }
        return instance;
    }

    private int getTimeout(String key, int defaultSeconds) {
        if (!timeouts.containsKey(key)) {
            String customValue = MainRunner.getEnvOrExParam(key);
            int timeout;
            if (customValue != null && customValue.matches("^\\d+$")) {
                timeout = Integer.parseInt(customValue);
            } else {
                timeout = defaultSeconds;
            }
            timeouts.put(key, timeout);
        }
        return timeouts.get(key);
    }

}
