package com.macys.sdt.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.macys.sdt.framework.model.KillSwitch;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.analytics.Analytics;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xerces.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class is the entry point for EE
 */
public class RunFeature {
    private static final String m_version = "1a.0001";
    private static int m_pid;
    private File m_repo_jar;
    private String m_workspace;
    private String m_eeURL;
    private static final Logger logger = LoggerFactory.getLogger(MainRunner.class);

    public RunFeature() throws Throwable {

        int remoteDebugDelay = Utils.parseInt(System.getenv("REMOTE_DEBUG_DELAY"), 0);
        if (remoteDebugDelay > 0) {
            Utils.threadSleep(remoteDebugDelay * 1000, "Remote debug delay:" + remoteDebugDelay);
        }

        logger.debug("RunFeature version: " + m_version);
        m_pid = Utils.getProcessId();
        this.m_eeURL = "http://" + System.getenv("EE") + "/json";
        this.m_workspace = System.getenv("WORKSPACE");
        this.m_repo_jar = new File(this.m_workspace + "/" + System.getenv("repo_jar"));
        this.cleanWorkSpace();
        this.dumpEnvironmentVariables();

        logger.info("Preparing workspace...");
        Utils.extractResources(this.m_repo_jar, this.m_workspace, System.getenv("sdt_project").trim().replace(".", "/"));

        if (RunConfig.scenarios != null) {
            RunConfig.scenarios = RunConfig.scenarios.replaceAll("features/", System.getenv("sdt_project").trim().replace(".", "/") + "/features/");
        }
        logger.info("get Analytics Golds...");
        getAnalyticsGolds();

        try {
            logger.info("\n\nInitializing MainRunner()...");
            MainRunner.main(null);
        } catch (Throwable th) {
            logger.error("issue in executing MainRunner main() due to : " + th.getMessage());
            logger.debug("issue in running MainRunner main() : " + th);
        }
        this.archive();
        System.exit(MainRunner.runStatus);
    }

    public static void main(String[] args) {
        try {
            //			Utils.get_tar_file_list(new File("C:\\Users\\m526092\\eclipse_workspace\\JenkinsSlave\\builds\\processed\\11.120.180.247.DSV_test_Windows_7@2.69.1441286019238.tar"), "testreport/");
            //			Utils.extractJarFile(new File("C:\\Users\\m526092\\eclipse_workspace\\JenkinsSlave\\repo\\SDT\\master.sdt.jar"), "sdt/features/", "features");

            if (args.length == 0) {
                new RunFeature();
            } else if (args[0].equals("-self_clean")) {
                new ProcessWatchDog();
            }
        } catch (Throwable e) {
            logger.error("issue in executing RunFeature main() due to : " + e.getMessage());
            logger.debug("issue in running MainRunner main() : " + e);
        }
    }

    public static boolean checkAborted() {
        if (System.getenv("BUILD_URL") == null) {
            return false;
        }
        try {
            String bstatus = Utils.httpGet(System.getenv("BUILD_URL") + "api/json", null);
            String result = (String) new Gson().fromJson(bstatus, Map.class).get("result");
            if (result == null) {
                result = "";
            }
            if (result.equals("ABORTED")) {
                return true;
            }
        } catch (Exception e) {
            logger.error("issue in checkAborted due to : " + e.getMessage());
            logger.debug("issue in running checkAborted : " + e);
        }
        return false;
    }

    public static String getBuildConsole(String jobBuildLink) {
        logger.info("get Build Console : " + jobBuildLink);
        String console = "console is not available...";
        try {
            console = "<pre>" + Utils.httpGet(jobBuildLink + "/logText/progressiveHtml", null) + "</pre>";
            //			logger.info(Jsoup.parse(console).text());
        } catch (Exception e) {
            logger.error("issue to get build console due to : " + e.getMessage());
            logger.debug("issue to get build console : " + e);
        }
        return console;
    }

    /**
     * download gold file from given url and set in the specified file path
     *
     * @param url          url to download gold file from
     * @param goldFilepath file path to save the retrieved gold file
     * @param goldFileName gold file name to store the gold file
     */
    public static void downloadGold(String url, File goldFilepath, String goldFileName) {
        try {
            logger.info("downloading gold file from: " + url);
            String data = Utils.httpGet(url, null);
            logger.info("received : " + data.length() + " bytes");
            Utils.writeBinaryFile(data.getBytes(), new File(goldFilepath.getCanonicalPath() + "/" + goldFileName), false);
        } catch (Exception e) {
            logger.warn("Cannot download gold from url: " + url + "due to : " + e.getMessage());
        }
    }

    public void cleanWorkSpace() {
        logger.info("cleanWorkSpace()...");
        try {
            File[] files = new File(m_workspace).listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.getName().equals(this.m_repo_jar.getName())) {
                    continue;
                }
                logger.info("removing " + f.getPath());
                if (f.isDirectory()) {
                    try {
                        FileUtils.cleanDirectory(f);
                    } catch (IOException iex) {
                        logger.error("Cannot clean " + f.getPath() + " : " + iex.getMessage());
                        continue;
                    }
                }
                if (!f.delete()) {
                    logger.error("Failed to delete file: " + f.getPath());
                }
            }
        } catch (Exception e) {
            logger.error("issue in cleaning workspace due to : " + e.getMessage());
            logger.debug("issue in cleaning workspace : " + e);
        }
    }

    /**
     * get analytics gold file
     */
    public void getAnalyticsGolds() {
        String analytics = RunConfig.getEnvOrExParam("analytics");
        if (analytics == null) {
            logger.info("non analytics run: skip analytics gold download.");
            return;
        } else {
            logger.info("analytics : " + analytics);
        }
        File goldDir = Utils.createDirectory(this.m_workspace + "/golds");
        String url = "http://" + System.getenv("EE") + "/getAnalyticsGold/" + analytics + "/";
        String globalFileName = RunConfig.getEnvVar("site_type").toLowerCase() + "_global.json";

        logger.info("downloading global gold file: " + globalFileName);

        // download global gold file
        downloadGold(url + globalFileName, goldDir, globalFileName);

        RunConfig.getFeatureScenarios();
        for (String feature : RunConfig.features.keySet()) {
            Map featureMap = RunConfig.features.get(feature);
            if (featureMap != null) {
                try {
                    String goldName = Analytics.getGoldName(featureMap);
                    logger.info("downloading scenario gold file: " + goldName);

                    // download scenario gold file
                    downloadGold(url + goldName, goldDir, goldName);
                } catch (Exception e) {
                    logger.info("Cannot download gold: " + feature + " : " + e.getMessage());
                    logger.info("features : " + featureMap.toString());
                    logger.debug("issue in download gold file : " + e);
                }
            } else {
                logger.info("Cannot download gold : " + feature);
            }
        }
    }

    public void getSdtRepo() throws IOException {
        File fEEUrl = new File(this.getClass().getResource("/sdt/resources/EE.server.loc").getFile());
        String adminServerURL = Utils.readTextFile(fEEUrl).trim() + "download/getSDTV1Repo";
        FileUtils.copyURLToFile(new URL(adminServerURL), new File(this.m_workspace + "/" + this.m_repo_jar));
    }

    public void dumpEnvironmentVariables() {
        String logsPath = this.m_workspace + "/logs";
        //File f = Utils.createDirectory(new File(logsPath), true);
        File f = new File(logsPath + "/env_variables.json");
        logger.info("\n\nDumping Environment variables...:" + logsPath + "/env_variables.json");
        Hashtable<String, String> h = new Hashtable<>();
        h.putAll(System.getenv());
        h.put("pid", Utils.getProcessId() + "");
        h.put("kill_switch", KillSwitch.dump());
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(h);
        logger.info(json + "\n\n");
        Utils.writeSmallBinaryFile(json.getBytes(), f);
    }

    public void archive() throws Exception {
        logger.info("Archiving logs directory...");
        File ftempfiles = Utils.createDirectory(new File("tempfiles"), true);

        File ws = new File(this.m_workspace);
        File logFile = new File(ws.getCanonicalPath() + File.separator + "logs");
        if (!logFile.exists() || (logFile.isDirectory() && logFile.listFiles().length == 0)) {
            logger.info("Logs dir is empty:" + logFile.getCanonicalPath());
            return;
        }

        File fEnv = new File(logFile.getCanonicalPath() + File.separator + "env_variables.json");
        if (!fEnv.exists()) {
            this.dumpEnvironmentVariables();
        }
        if (!new File(logFile.getCanonicalPath() + File.separator + "cucumber.json").exists()) {
            try {
                if (fEnv.exists()) {
                    Map env = new Gson().fromJson(Utils.readTextFile(fEnv), Map.class);
                    String jenkinsURL = env.get("JENKINS_URL").toString();
                    String jobName = env.get("JOB_NAME").toString();
                    String build = env.get("BUILD_NUMBER").toString();
                    String link = jenkinsURL + "job/" + jobName + "/" + build;
                    String console = getBuildConsole(link);
                    logger.info("Notifying admins: " + this.m_eeURL);
                    StringBuilder res = new StringBuilder();
                    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                        Hashtable<String, String> hparams = new Hashtable<>();
                        hparams.put("action", "sendMail");
                        hparams.put("_to", "_admin");
                        hparams.put("_msg", "Reported by Archiver : job " + jobName + ":" + build + "\n\n" + link +
                                "\n\n===== Log =====\n\n" + console);
                        hparams.put("_subject", "Abornormal Completion:" + jobName + ": " + env.get("NODE_NAME") + " " + new Date());
                        Utils.post(client, this.m_eeURL, hparams, new StringBuilder(), res);
                    } finally {
                        logger.info("==> " + res);
                    }
                }
            } catch (Exception e) {
                logger.error("issue in archiving logs due to : " + e.getMessage());
                logger.debug("issue in archiving logs : " + e);
            }

            return;
        }


        File fpushed = new File(ws.getCanonicalPath() + File.separator + "jenkins_admin.pushed");
        if (fpushed.exists()) {
            return;
        }

        logger.info(Utils.executeCMD("cd \"" + logFile.getCanonicalPath() + "\" && tar -cvf log.tar *"));
        File flogtar = new File(logFile.getCanonicalPath() + File.separator + "log.tar");
        logger.info("Locating log.tar:" + flogtar.exists() + ":" + flogtar.getCanonicalPath() + ":" + flogtar.length());
        File ftempPushtar = new File(ftempfiles.getCanonicalFile() + File.separator +
                ws.getName().replaceAll(" ", "_") + "." +
                System.getenv("BUILD_NUMBER") + "." +
                System.currentTimeMillis() + ".tar");
        if (!flogtar.renameTo(ftempPushtar)) {
            logger.error("Failed to rename lot tar to temp push tar");
        }
        sendToServer(ftempPushtar, fpushed);
    }

    public void sendToServer(File ftempPushtar, File fpushed) throws Exception {
        logger.info("pushing log.tar: " + ftempPushtar.getCanonicalPath());
        HashMap<String, String> hparams = new HashMap<>();
        hparams.put("file_name", "builds/" + InetAddress.getLocalHost().getHostAddress() + "." + ftempPushtar.getName());
        hparams.put("last_modified", ftempPushtar.lastModified() + "");
        Thread th = new PushLog(this, ftempPushtar, hparams, fpushed);
        th.run();
        th.join();
    }

    public void postToServer(Map hparams, StringBuilder cookies, StringBuilder result) throws Exception {
        for (int i = 0; i < 5; i++) {
            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                Utils.post(client, this.m_eeURL, hparams, cookies, result);
                return;
            } catch (Exception ex) {
                logger.error("issue in post to server : " + ex.getMessage());
                Utils.threadSleep(5000, "Failed post log to EE.  Try again in 5 seconds...: " + i);
            }
        }
    }

    public static class ProcessWatchDog extends Thread {
        public ProcessWatchDog() {
            this.start();
        }

        public void run() {
            long ts = System.currentTimeMillis();
            long dur = 3 * 60 * 60 * 1000;
            while (System.currentTimeMillis() - ts < dur) {
                if (checkAborted()) {
                    logger.info(Utils.executeCMD("taskkill /f /t /PID " + m_pid));
                    break;
                }
                Utils.threadSleep(60 * 1000, "ProcessWatchDog.run()");
            }
        }
    }

    public static class PushLog extends Thread {
        private File m_fpushed, m_pushObj;
        private RunFeature m_runFeature;

        public PushLog(RunFeature runFeature, File flogtar, HashMap hparams, File fpushed) throws Exception {
            this.m_runFeature = runFeature;
            this.m_fpushed = fpushed;
            Hashtable<String, Object> hobj = new Hashtable<>();
            hobj.put("flogtar", flogtar.getCanonicalPath());
            hobj.put("hparam", hparams);
            String parent = flogtar.getAbsoluteFile().getParent();
            this.m_pushObj = new File(parent + File.separator + "push.obj." + System.currentTimeMillis());
            Utils.writeBinaryFile(new Gson().toJson(hobj).getBytes(), this.m_pushObj, false);
        }

        public void run() {
            try {
                pushLogObj(this.m_pushObj);
            } catch (Exception ex) {
                if (m_fpushed != null && !m_fpushed.delete()) {
                    logger.error("Unable to delete fPushed file");
                }
                logger.error("issue due to " + ex.getMessage());
            }
        }

        public void pushLogObj(File fpushObj) throws Exception {
            FileInputStream fis = null;
            try {
                Map hpushObj = new Gson().fromJson(Utils.readTextFile(fpushObj), Map.class);
                File flogTar = new File(hpushObj.get("flogtar").toString());
                logger.info("Pushing " + flogTar.getCanonicalPath());
                fis = new FileInputStream(flogTar);
                StringBuilder cookies = new StringBuilder();
                StringBuilder result = new StringBuilder();
                Map<String, String> hparams = (Map) hpushObj.get("hparam");
                hparams.put("action", "uploadFile1");
                hparams.put("append", "false");
                byte buffer[] = new byte[500 * 1024];
                int read, total = 0;
                while ((read = fis.read(buffer)) != -1) {
                    byte[] wbuffer = new byte[read];
                    System.arraycopy(buffer, 0, wbuffer, 0, read);
                    hparams.put("file_data", Base64.encode(wbuffer));
                    this.m_runFeature.postToServer(hparams, cookies, result);
                    hparams.put("append", "true");
                    //					logger.info("result: " + result);
                    if (result.toString().toLowerCase().contains("error")) {
                        throw new Exception(result.toString());
                    }
                    total += read;
                    logger.info(".");
                    Utils.threadSleep(200, null);
                }

                hparams.put("append", "close");
                hparams.remove("file_data");
                this.m_runFeature.postToServer(hparams, cookies, result);
                logger.info("File uploaded: " + flogTar.getCanonicalPath() + ":" + total + ":" + hparams.get("file_name") + "\n" + hparams);
                if (!flogTar.delete()) {
                    logger.error("Unable to delete log tar file");
                }
                if (!fpushObj.delete()) {
                    logger.error("Unable to delete push object file");
                }
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    logger.error("issue in closing file due to : " + ex.getMessage());
                }
            }
        }
    }
}
