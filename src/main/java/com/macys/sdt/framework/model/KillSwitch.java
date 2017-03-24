package com.macys.sdt.framework.model;

import com.google.gson.Gson;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.Utils;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * Gets kill switch data from EE API
 */
public class KillSwitch {
    private static Map<String, Map<String, String>> data;

    /**
     * Gets KS data in JSON format and returns it as a string.
     * <p>
     * Also fills the "data" variable with kill switch data in map format
     * </p>
     *
     * @return String representation of kill switch JSON data
     */
    public static String dump() {
        if (data != null && !data.isEmpty()) {
            return new Gson().toJson(data, Map.class);
        }

        // This is for KillSwitch Unit Test
        if (RunConfig.booleanParam("killSwitchUnitTest")) {
            File ksDataFile = new File("src/test/java/com/macys/sdt/framework/resources/sample_ks_data.json");
            if (ksDataFile.exists()) {
                try {
                    String sampleData = Utils.readTextFile(ksDataFile);
                    KillSwitch.data = new Gson().fromJson(sampleData, Map.class);
                    return sampleData;
                } catch (Exception ignored) {
                }
            }
        }

        try {
            String env = new URL(System.getenv("website")).getHost().replaceAll("www1.", "").replaceAll("www.", "").replaceAll(".fds.com", "").replaceAll(".com", "");
            String ksurl = Utils.getEEUrl() + "/api/ee/getKillSwitch/" + env;
            System.out.println("--> Dumping KillSwitch data for:" + ksurl);
            String ks = Utils.httpGet(ksurl, null);
            try {
                KillSwitch.data = new Gson().fromJson(ks, Map.class);
                return ks;
            } catch (Exception ex) {
                System.out.println("--> Killswitch data not available:" + ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    /**
     * Gets kill switch data as a Map
     *
     * @return Kill switch data from EE API
     */
    private static Map<String, Map<String, String>> getData() {
        if (data == null) {
            dump();
        }
        return data;
    }

    /**
     * Checks whether a kill switch with name "switchName" is enabled
     *
     * @param switchName name of kill switch to check
     * @return true if kill switch is enabled
     */
    public static boolean getEnabled(String switchName) {
        Map<String, String> data = getData().get(switchName);
        return data != null && data.get(KSData.EXPECTED_VALUE).equals("true");
    }

    /**
     * Gets a KSData object with the data for a kill switch named "switchName"
     *
     * @param switchName name of kill switch to get data for
     * @return KSData object with kill switch data
     */
    public static KSData getData(String switchName) {
        return new KSData(getData().get(switchName));
    }

    /**
     * Represents an individual kill switch
     */
    public static class KSData {
        private static final String FEATURE_NAME = "Feature Name";
        private static final String KEY_DISPLAY_NAME = "Key Display Name";
        private static final String EXPECTED_VALUE = "Expected Value";
        private Map<String, String> data;

        /**
         * Create a KSData object using a map with kill switch data from EE API
         *
         * @param data Map with kill switch data from EE API
         */
        public KSData(Map<String, String> data) {
            this.data = data;
        }

        /**
         * Create a KSData object by switch name from EE API
         *
         * @param switchName Name of switch to get data for
         */
        public KSData(String switchName) {
            this.data = KillSwitch.getData().get(switchName);
        }

        /**
         * Gets the feature name of the kill switch
         *
         * @return Kill switch name
         */
        public String getFeatureName() {
            return this.data.get(FEATURE_NAME);
        }

        /**
         * Gets the display name of a kill switch
         *
         * @return Kill switch display name
         */
        public String getKeyDisplayName() {
            return this.data.get(KEY_DISPLAY_NAME);
        }

        /**
         * Checks if this kill switch is enabled
         *
         * @return True if kill switch is enabled
         */
        public boolean getEnabled() {
            return this.data.get(EXPECTED_VALUE).equals("true");
        }
    }
}
