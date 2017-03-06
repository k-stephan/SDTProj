package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Retrieves and stores information about the environment under test
 */
public class EnvironmentDetails {

    private static String site = null;
    private static String type = null;
    private static String appServer = null;
    private static String server = null;
    private static String timestamp = null;
    private static String release = null;
    private static String releaseDate = null;
    private static String version = null;

    private EnvironmentDetails() {
    }

    public static String getSite() {
        if (site == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return site;
    }

    public static String getType() {
        if (type == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return type;
    }

    public static String getAppServer() {
        if (appServer == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return appServer;
    }

    public static String getServer() {
        if (server == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return server;
    }

    public static String getTimestamp() {
        if (timestamp == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return timestamp;
    }

    public static String getRelease() {
        if (release == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return release;
    }

    public static String getReleaseDate() {
        if (releaseDate == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return releaseDate;
    }

    public static String getVersion() {
        if (version == null && !waitForReady()) {
            loadEnvironmentDetails(MainRunner.url);
        }
        return version;
    }

    private static boolean ready = false;
    private static Thread t = null;

    public static void loadEnvironmentDetails(String environment) {
        loadEnvironmentDetails(environment, true);
    }

    public static void loadEnvironmentDetails(String environment, boolean waitForFinish) {
        String env = environment.replace("http://", "https://");
        t = new Thread(() -> {
            try {
                String html = Utils.httpGet(env, null);
                Document doc = Jsoup.parse(html);
                site = doc.select("site").html();
                type = doc.select("type").html();
                appServer = doc.select("appserver").html();
                server = doc.select("server").html();
                timestamp = doc.select("timestamp").html();
                release = doc.select("release").html();
                releaseDate = doc.select("releasedate").html();
                version = doc.select("version").html();
                ready = true;
            } catch (Exception e) {
                System.err.println("Unable to get environment details from " + environment);
            }
        });
        t.start();
        if (waitForFinish) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for env details request to return");
            }
        }
    }

    public static String getDetails() {
        return "\n======> Environment Details <======\n\n" + site + "\n" + type + "\n" + appServer
                + "\n" + server + "\n" + timestamp + "\n" + release + "\n" + releaseDate + "\n" + version
                + "\n\n" + "===================================\n";
    }

    public static boolean waitForReady() {
        if (t != null) {
            if (t.isAlive()) {
                try {
                    t.join();
                    return true;
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting for env details request to return");
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static boolean ready() {
        return ready;
    }
}
