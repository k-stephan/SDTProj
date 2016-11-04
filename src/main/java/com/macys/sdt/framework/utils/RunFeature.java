package com.macys.sdt.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.analytics.Analytics;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xerces.impl.dv.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class RunFeature {
    private static final String m_version = "1a.0001";
    private static int m_pid;
    private File m_repo_jar;
    private String m_workspace;
    private String m_eeURL;

    public RunFeature() throws Throwable {
        int remoteDebugDelay = Utils.parseInt(System.getenv("REMOTE_DEBUG_DELAY"), 0);
        if (remoteDebugDelay > 0) {
            Utils.threadSleep(remoteDebugDelay * 1000, "Remote debug delay:" + remoteDebugDelay);
        }

        System.out.println("RunFeature version: " + m_version);
        m_pid = Utils.getProcessId();
        this.m_eeURL = "http://" + System.getenv("EE") + "/json";
        this.m_workspace = System.getenv("WORKSPACE");
        this.m_repo_jar = new File(this.m_workspace + "/" + System.getenv("repo_jar"));
        this.cleanWorkSpace();
        this.dumpEnvironmentVariables();

        System.out.println("\n\nPreparing workspace...");
        Utils.extractResources(this.m_repo_jar, this.m_workspace, System.getenv("sdt_project").trim().replace(".", "/"));

        if (MainRunner.scenarios != null) {
            MainRunner.scenarios = MainRunner.scenarios.replaceAll("features/", System.getenv("sdt_project").trim().replace(".", "/") + "/features/");
        }
        System.out.println("\n\n.getAnalyticsGolds");
        getAnalyticsGolds();

        try {
            System.out.println("\n\nInitializing MainRunner()...");
            MainRunner.main(null);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        this.archive();
        System.exit(MainRunner.runStatus);
    }

    public static void main(String[] args) {
        try {
            //			Utils.get_tar_file_list(new File("C:\\Users\\m526092\\eclipse_workspace\\JenkinsSlave\\builds\\processed\\11.120.180.247.DSV_test_Windows_7@2.69.1441286019238.tar"), "testreport/");
            //			Utils.outputJarFile(new File("C:\\Users\\m526092\\eclipse_workspace\\JenkinsSlave\\repo\\SDT\\master.sdt.jar"), "sdt/features/", "features");

            if (args.length == 0) {
                new RunFeature();
            } else if (args[0].equals("-self_clean")) {
                new ProcessWatchDog();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void cleanWorkSpace() {
        System.err.println("-->cleanWorkSpace()...");
        try {
            File[] files = new File(m_workspace).listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.getName().equals(this.m_repo_jar.getName())) {
                    continue;
                }
                System.out.println("--> removing " + f.getCanonicalPath());
                if (f.isDirectory()) {
                    FileUtils.cleanDirectory(f);
                }
                if (!f.delete()) {
                    System.err.println("Failed to delete file: " + f.getCanonicalPath());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getBuildConsole(String jobBuildLink) {
        System.out.println("-->Archiver.getBuildConsole():" + jobBuildLink);
        String console = "console is not available...";
        try {
            console = "<pre>" + Utils.httpGet(jobBuildLink + "/logText/progressiveHtml", null) + "</pre>";
            //			System.out.println(Jsoup.parse(console).text());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return console;
    }

    public static void downloadGold(String url, File fgolds, String goldName) {
        try {
            System.out.println("->dowloading from:" + url);
            String data = Utils.httpGet(url, null);
            System.out.println("-->received :" + data.length() + " bytes");
            Utils.writeBinaryFile(data.getBytes(), new File(fgolds.getCanonicalPath() + "/" + goldName), false);
        } catch (Exception ex) {
            System.out.println("-->RunFeature.downloadGolds():Cannot download gold:" + url);
        }
    }

    public void getAnalyticsGolds() {
        String analytics = MainRunner.getEnvOrExParam("analytics");
        if (analytics == null) {
            System.out.println("->non analytics run: skip analytics gold download.");
            return;
        }
        System.out.println("->dowloading golds...");
        File fgoldDir = Utils.createDirectory(this.m_workspace + "/golds");
        String url = "http://" + System.getenv("EE") + "/getAnalyticsGold/" + analytics + "/";
        String global = MainRunner.getEnvVar("site_type").toLowerCase() + "_global.json";
        downloadGold(url + global, fgoldDir, global);

        MainRunner.getFeatureScenarios();
        for (String feature : MainRunner.features.keySet()) {
            Map featureMap = MainRunner.features.get(feature);
            if (featureMap != null) {
                try {
                    String goldName = Analytics.getGoldName(featureMap);
                    System.out.println("->dowloading golds:" + goldName);
                    downloadGold(url + goldName, fgoldDir, goldName);
                } catch (Exception ex) {
                    System.out.println("-->Cannot download gold:" + feature + ":" + ex.getMessage());
                    System.out.println(featureMap);
                    ex.printStackTrace();
                }
            } else {
                System.out.println("-->Cannot download gold:" + feature);
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
        System.out.println("\n\nDumping Environment variables...:" + logsPath + "/env_variables.json");
        Hashtable<String, String> h = new Hashtable<>();
        h.putAll(System.getenv());
        h.put("pid", Utils.getProcessId() + "");
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(h);
        System.out.println(json);
        System.out.println();
        System.out.println();
        Utils.writeSmallBinaryFile(json.getBytes(), f);
    }

    public void archive() throws Exception {
        System.out.println("Archiving logs directory...");
        File ftempfiles = Utils.createDirectory(new File("tempfiles"), true);

        File ws = new File(this.m_workspace);
        File flog = new File(ws.getCanonicalPath() + File.separator + "logs");
        if (!flog.exists() || (flog.isDirectory() && flog.listFiles().length == 0)) {
            System.out.println("Logs dir is empty:" + flog.getCanonicalPath());
            return;
        }
        
        File fenv = new File(flog.getCanonicalPath() + File.separator + "env_variables.json");
        if (!fenv.exists()){
        	this.dumpEnvironmentVariables();
        }
        if (!new File(flog.getCanonicalPath() + File.separator + "cucumber.json").exists()) {
            try {
                if (fenv.exists()) {
                    Map env = new Gson().fromJson(Utils.readTextFile(fenv), Map.class);
                    String jenkinsURL = env.get("JENKINS_URL").toString();
                    String jobName = env.get("JOB_NAME").toString();
                    String build = env.get("BUILD_NUMBER").toString();
                    String link = jenkinsURL + "job/" + jobName + "/" + build;
                    String console = getBuildConsole(link);
                    System.out.println("Notifying admins: " + this.m_eeURL);
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
                        System.out.println("==> " + res);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return;
        }


        File fpushed = new File(ws.getCanonicalPath() + File.separator + "jenkins_admin.pushed");
        if (fpushed.exists()) {
            return;
        }

        System.out.println(Utils.executeCMD("cd \"" + flog.getCanonicalPath() + "\" && tar -cvf log.tar *"));
        File flogtar = new File(flog.getCanonicalPath() + File.separator + "log.tar");
        System.out.println("Locating log.tar:" + flogtar.exists() + ":" + flogtar.getCanonicalPath() + ":" + flogtar.length());
        File ftempPushtar = new File(ftempfiles.getCanonicalFile() + File.separator +
                ws.getName().replaceAll(" ", "_") + "." +
                System.getenv("BUILD_NUMBER") + "." +
                System.currentTimeMillis() + ".tar");
        if (!flogtar.renameTo(ftempPushtar)) {
            System.err.println("Failed to rename lot tar to temp push tar");
        }
        sendToServer(ftempPushtar, fpushed);
    }

    public void sendToServer(File ftempPushtar, File fpushed) throws Exception {
        System.out.println("pushing log.tar:" + ftempPushtar.getCanonicalPath());
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
                ex.printStackTrace();
                Utils.threadSleep(5000, "Failed post log to EE.  Try again in 5 seconds...:" + i);
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
                    System.out.println(Utils.executeCMD("taskkill /f /t /PID " + m_pid));
                    break;
                }
                Utils.threadSleep(60 * 1000, "RunFeature.ProcessWatchDog.run()");
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
                        System.err.println("Unable to delete fPushed file");
                }
                ex.printStackTrace();
            }
        }

        public void pushLogObj(File fpushObj) throws Exception {
            FileInputStream fis = null;
            try {
                Map hpushObj = new Gson().fromJson(Utils.readTextFile(fpushObj), Map.class);
                File flogTar = new File(hpushObj.get("flogtar").toString());
                System.out.println("-->Pushing " + flogTar.getCanonicalPath());
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
                    //					System.out.println("-->result: " + result);
                    if (result.toString().toLowerCase().contains("error")) {
                        throw new Exception(result.toString());
                    }
                    total += read;
                    System.out.print(".");
                    Utils.threadSleep(200, null);
                }

                hparams.put("append", "close");
                hparams.remove("file_data");
                this.m_runFeature.postToServer(hparams, cookies, result);
                System.out.println("File uploaded:" + flogTar.getCanonicalPath() + ":" + total + ":" + hparams.get("file_name") + "\n" + hparams);
                if (!flogTar.delete()) {
                    System.err.println("Unable to delete log tar file");
                }
                if (!fpushObj.delete()) {
                    System.err.println("Unable to delete push object file");
                }
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
