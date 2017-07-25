package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retrieves and stores information about the environment under test
 */
public class EnvironmentDetails {


    private static final Logger logger = LoggerFactory.getLogger(EnvironmentDetails.class);
    private static String envUrl = RunConfig.url;
    private static boolean stage5 = RunConfig.url.matches(
            ".*?(http://)?(www\\.)?(m\\.)?((mcom|bcom)?external[0-9][0-9]?[0-9]?|qa[0-9][0-9]?code(macys|mcom|bcom|bloomingdales))\\.fds\\.com.*?");
    private static boolean zeus = RunConfig.url.matches(".*?\\.tbe\\.zeus\\.fds\\.com.*?");
    private static boolean detailsCollected = false;
    private static String site = null;
    private static String type = null;
    private static String appServer = null;
    private static String server = null;
    private static String timestamp = null;
    private static String release = null;
    private static String releaseDate = null;
    private static String version = null;
    private static JSONObject servicesJson;
    private static boolean ready = false;
    private static Thread t = null;
    private EnvironmentDetails() {}

    public static boolean isStage5() {
        return stage5;
    }

    public static boolean isZeus() {
        return zeus;
    }

    public static String getEnvUrl() {
        return envUrl;
    }

    public static void setEnvUrl(String url) {
        envUrl = url;
    }

    public static JSONObject getServicesJson() {
        // make sure thread getting env details is done
        waitForReady();
        return servicesJson;
    }

    public static String getSite() {
        if (site == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return site;
    }

    public static String getType() {
        if (type == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return type;
    }

    public static String getAppServer() {
        if (appServer == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return appServer;
    }

    public static String getServer() {
        if (server == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return server;
    }

    public static String getTimestamp() {
        if (timestamp == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return timestamp;
    }

    public static String getRelease() {
        if (release == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return release;
    }

    public static String getReleaseDate() {
        if (releaseDate == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return releaseDate;
    }

    public static String getVersion() {
        if (version == null && !waitForReady()) {
            loadEnvironmentDetails(RunConfig.url);
        }
        return version;
    }

    public static void loadEnvironmentDetails() {
        loadEnvironmentDetails(null, true);
    }

    public static void loadEnvironmentDetails(String environment) {
        loadEnvironmentDetails(environment, true);
    }

    public static void loadEnvironmentDetails(String environment, final boolean printOnFinish) {
        Set<Cookie> cookies = Cookies.getCookies();
        String cookieStr = cookies == null ? null :
                Utils.listToString(cookies.stream()
                                .map(c -> c.getName() + "=" + c.getValue())
                                .collect(Collectors.toList()),
                        ";", null);

        environment = environment == null ? envUrl : environment;
        String env = environment.replace("http://", "https://");
        t = new Thread(() -> {
            try {
                // basic site details
                HashMap<String, String> headers = new HashMap<>();
                if (cookieStr != null) {
                    headers.put("cookie", cookieStr);
                }
                String html = RESTOperations.doGET(env, headers).readEntity(String.class);
                Document doc = Jsoup.parse(html);
                Element siteInfo = doc.select("div#soasta_pageinfo").last();
                site = siteInfo.select("site").html();
                type = siteInfo.select("type").html();
                appServer = siteInfo.select("appserver").html();
                server = siteInfo.select("server").html();
                timestamp = siteInfo.select("timestamp").html();
                release = siteInfo.select("release").html();
                releaseDate = siteInfo.select("releasedate").html();
                version = siteInfo.select("version").html();
            } catch (Exception e) {
                logger.error("Unable to get environment details from " + env);
            }
            try {
                // services data
                String serviceUrl = getServiceURL(envUrl);
                HashMap<String, String> headers = new HashMap<>();
                if (zeus) {
                    headers.put("Authorization", "Token c8b9cd48daa6942784d6dd23f6cde39b3d10b941");
                }
                servicesJson = new JSONObject(RESTOperations.doGET(serviceUrl, headers).readEntity(String.class));
                ready = true;
                detailsCollected = true;
                if (printOnFinish) {
                    logger.info(getDetails());
                }
            } catch (Exception e) {
                logger.error("Unable to get server details for " + env);
            }
        });
        t.setName("EnvironmentDetails");
        t.start();
    }

    public static void waitForThread() {
        if (t == null) {
            return;
        }
        try {
            t.join(20000);
        } catch (InterruptedException e) {
            logger.error("Unable to wait for env details thread");
        }
    }

    /**
     * get environment details
     *
     * @return environment details
     */
    public static String getDetails() {
        if (!ready) {
            return "Environment Details are not ready yet\n";
        }
        if (site == null) {
            return "\n======> Environment Details <======\n\nUnable to get environment details\n\n"
                    + "===================================\n";
        }
        return String.format("\n======> Environment Details <======\n\nSite: %s\nType: %s\nApp Server: %s\nServer: %s\n" +
                        "Timestamp: %s\nRelease: %s\nRelease Date: %s\nVersion: %s\n\n===================================\n",
                site, type, appServer, server, timestamp, release, releaseDate, version);
    }

    public static boolean waitForReady() {
        if (detailsCollected) {
            return true;
        }

        if (t == null || !t.isAlive()) {
            loadEnvironmentDetails();
        }

        try {
            t.join();
            return true;
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for env details request to return");
            return false;
        }
    }

    public static void updateStage5() {
        stage5 = envUrl.matches(
                ".*?(http://)?(www\\.)?(m\\.)?qa[0-9][0-9]?code(macys|mcom|bcom|bloomingdales)\\.fds\\.com.*?");
    }

    /**
     * Returns my services application details.
     *
     * @param appName name of the app, ex: navapp, legacy, etc.
     * @return my services application details
     */
    public static AppDetails myServicesApp(String appName) {

        AppDetails appDetails = new AppDetails(null, null, null);

        try {
            String eName = getEnv(envUrl);
            JSONArray myServicesInfo;
            // make sure thread getting env details is done
            if (WebDriverManager.driverInitialized())
                waitForReady();
            if (stage5) {
                JSONArray environmentDetails = new JSONArray(servicesJson.get("envDetails").toString());
                myServicesInfo = (JSONArray) environmentDetails.getJSONObject(0).get("myServicesIpBoList");
            } else if (zeus) {
                return getZeusApp(appName);
            } else {
                myServicesInfo = (JSONArray) servicesJson.get("myServicesIpBoList");
            }

            for (int i = 0; i < myServicesInfo.length(); i++) {
                JSONObject appInfo = myServicesInfo.getJSONObject(i);
                if (appInfo.get("appName").toString().equalsIgnoreCase(appName)) {
                    appDetails = new AppDetails(
                            eName, appInfo.get("appIp").toString(), stage5 ? appInfo.get("hostName").toString() : "");
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn("issue in returning my services application details due to : " + e.getMessage());
        }

        return appDetails;
    }

    /**
     * Returns application details.
     *
     * @param appName name of the app, ex: mspcustomer, msporder, etc.
     * @return application details
     */
    public static AppDetails otherApp(String appName) {

        AppDetails appDetails = null;

        try {
            String eName = getEnv(envUrl);

            JSONArray applicationInfo;
            // make sure thread getting env details is done
            if (WebDriverManager.driverInitialized())
                waitForReady();
            if (zeus) {
                return getZeusApp(appName);
            } else if (stage5) {
                JSONArray environmentDetails = new JSONArray(servicesJson.get("envDetails").toString());
                applicationInfo = (JSONArray) environmentDetails.getJSONObject(0).get("applicationBolist");
            } else {
                applicationInfo = (JSONArray) servicesJson.get("applicationBolist");
            }

            for (int i = 0; i < applicationInfo.length(); i++) {
                JSONObject appInfo = applicationInfo.getJSONObject(i);
                if (appInfo.getString("appName").equalsIgnoreCase(appName)) {
                    String hName = null;
                    if (!appName.equalsIgnoreCase("f5_vip")) {
                        hName = appInfo.get("envName").toString();
                    }
                    appDetails = new AppDetails(eName, appInfo.get("ipAddress").toString(), hName);
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn("issue in returning application details due to : " + e.getMessage());
        }

        return appDetails;
    }

    private static AppDetails getZeusApp(String appName) {
        JSONArray componentList = servicesJson.getJSONObject("component").getJSONArray("components");
        JSONObject obj = Utils.findObjectWithProperty(componentList, "name", appName);
        JSONArray component = obj == null ? null : obj.getJSONObject("io").getJSONArray("offerings");
        if (component == null) {
            return null;
        }
        String ip = null;
        String url = null;
        String port = null;
        for (int i = 0; i < component.length(); i++) {
            obj = component.getJSONObject(i);
            String name = obj.getString("name");
            if (name.endsWith("_ip")) {
                ip = obj.getString("value");
            } else if (name.endsWith("_url")) {
                url = obj.getString("value");
            } else if (name.endsWith("_port")) {
                port = obj.getString("value");
            }
        }

        return new AppDetails(getEnv(envUrl), ip, url, port);
    }

    /**
     * Fills services details with test data from file
     *
     * @throws Exception if response is unreadable
     */
    static void getTestServiceData() throws Exception {
        // This is for AppDetails Unit Test
        File envDetailsFile = new File("src/test/java/com/macys/sdt/framework/resources/sample_env_details.json");
        if (envDetailsFile.exists()) {
            servicesJson = new JSONObject(Utils.readTextFile(envDetailsFile));
        }
    }

    /**
     * Returns the service URL.
     *
     * @param envUrl environment URL
     * @return service URL
     */
    public static String getServiceURL(String envUrl) {
        final String GET_URL = stage5 ?
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/" :
                zeus ? "https://stable.zeus.fds.com/zeus_core/apis/v1/environments/" :
                        "http://c4d.devops.fds.com/reinfo/";

        return GET_URL + getEnv(envUrl) + (zeus ? "/?format=json" : "");
    }

    /**
     * Returns the environment name for a given URL.
     *
     * @param envUrl environment URL
     * @return environment name
     */
    public static String getEnv(String envUrl) {
        try {
            URL url = new URL(envUrl);
            String[] split = url.getHost().split("\\.");
            return split[0].matches("www|m|m2qa1") ? split[1] : split[0];
        } catch (MalformedURLException e) {
            logger.error("Unable to get environment details");
            return null;
        }
    }

    public static class AppDetails {

        public String envName, ipAddress, hostName, port;

        public AppDetails(String envName, String ipAddress, String hostName) {
            this.envName = envName;
            this.ipAddress = ipAddress;
            this.hostName = hostName;
        }

        public AppDetails(String envName, String ipAddress, String hostName, String port) {
            this.envName = envName;
            this.ipAddress = ipAddress;
            this.hostName = hostName;
            this.port = port;
        }

    }
}
