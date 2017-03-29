package com.macys.sdt.framework.utils.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.macys.sdt.framework.exceptions.DriverNotInitializedException;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.runner.WebDriverManager;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.Utils.ProcessWatchDog;
import cucumber.api.Scenario;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.macys.sdt.framework.runner.RunConfig.sharedResourceDir;
import static java.lang.Runtime.getRuntime;

/**
 * Created by M694182 on 10/22/2015.
 * <p>
 * File Location: [WORKSPACE]/tag_collection/[SHA1(feature + scenario)]-[yyyy-MM-dd-HH_mm_ss].json
 * File Format:
 * {  "feature": "feature file name",
 * "scenario": "scenario name",
 * "steps": [
 * { "step": "step name",
 * "tags": [
 * { "tag_name": "tag description",
 * "tag_url": "tag_url",
 * "params": [
 * { "param": "parameter name",
 * "description": "parameter description",
 * "value": "parameter value"
 * },
 * { "param": "parameter name",
 * "description": "parameter description",
 * "value": "parameter value"
 * },
 * :
 * :
 * ]
 * },
 * { "tag_name": "tag description",
 * "tag_url": "tag_url",
 * "params": [
 * { "param": "parameter name",
 * "description": "parameter description",
 * "value": "parameter value"
 * },
 * { "param": "parameter name",
 * "description": "parameter description",
 * "value": "parameter value"
 * },
 * :
 * :
 * ]
 * },
 * },
 * :
 * :
 * ]
 * }
 */

public class DATagCollector {

    private static final Logger logger = LoggerFactory.getLogger(DATagCollector.class);

    private static String capture_file = null;
    private static String output_file = null;
    private static JSONObject json_top = null;
    private static JSONArray json_steps = null;
    private static Boolean tag_collection_started = false;

    /**
     * starts the tag collection
     * @param scenario current scenario
     */
    public static void start(Scenario scenario) {
        // init outfile per scenario
        json_top = new JSONObject();
        json_steps = new JSONArray();
        String tag_collection_dir = RunConfig.workspace + "/logs/";
        Utils.createDirectory(tag_collection_dir);
        output_file = tag_collection_dir + getShaKey(scenario) + "-" + getTimestamp() + ".json";

        // if monitor already started, skip login
        if (tag_collection_started) {
            return;
        }

        // load any page to let the win auth module to work
        try {
            WebDriverManager.getWebDriver().navigate().to("http://www.harvard.edu");
            Wait.forPageReady();
        } catch (DriverNotInitializedException e) {
            Assert.fail("Driver not initialized");
        }

        // Window authentication dialog could happen when the first login after while
        Process p;

        boolean success = false;
        for (int i = 0; i < 5; i++) {
            try {
                String file_path = sharedResourceDir + "/framework/digital_analytics/plugin_utilities/da_login.exe";
                p = getRuntime().exec(file_path + " 90067660 MCOMREG macy$123");
                ProcessWatchDog pw = new ProcessWatchDog(p, 30 * 1000, "DATagCollector.start()");
                p.waitFor();  // wait for process to complete
                int exitStatus = p.exitValue();
                if (exitStatus == 0) {
                    logger.info("da_login.exe launch success: exit_status: " + exitStatus);
                    success = true;
                    pw.interrupt();
                    break;
                } else {
                    logger.error("ERROR - ENV: IBM Digital Analytics Plugin cannot be enabled: Retrying...: " + i);
                }
            } catch (Exception e) {
                Assert.fail("Cannot execute plugin utility: da_login.exe: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (!success) {
            Assert.fail("ERROR - ENV: IBM Digital Analytics Plugin cannot be enabled. Retry has exhausted");
            return;
        }
        logger.info("IBM Digital Analytics Plugin successfully enabled.");
        tag_collection_started = true;
    }

    /**
     * capture the digital analytics data
     *
     * @param step_name : current step name
     */
    public static void capture(String step_name) {
        // if monitor have not started, skip
        if (!tag_collection_started) {
            return;
        }

        if (capture_file == null) {
            capture_file = RunConfig.workspace + "/temp/digital_analytics_capture";
        }
        Process p = null;
        try {
            String file_path = sharedResourceDir + "/framework/digital_analytics/plugin_utilities/da_capture.exe";
            p = getRuntime().exec(file_path + " " + capture_file);
            p.waitFor();  // wait for process to complete
        } catch (Exception e) {
            Assert.fail("Cannot execute plugin utility: da_capture.exe");
        }
        if (p.exitValue() != 0) {
            Assert.fail("ERROR - ENV: Could not capture IBM Digital Analytics Plugin Tag Monitor Data");
        }

        JSONObject step_item = new JSONObject();
        JSONArray step_tags = new JSONArray();
        try {
            step_item.put("step", step_name);

            // get capture file
            String content = new Scanner(new File(capture_file)).useDelimiter("\\Z").next();

            if (!parseBuffer(step_tags, content)) {
                Assert.fail("ERROR - ENV: Could not capture IBM Digital Analytics Plugin Tag Monitor Data");
            }
            step_item.put("tags", step_tags);
            // logger.info("****** Chang: " + content);
        } catch (Exception e) {
            // logger.info("****** Chang: Parsing Error happened");
            e.printStackTrace();
        }

        try {
            json_steps.put(step_item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  flush tag collection file per each scenario
     */
    public static void flush() {
        // no monitor run, skip
        if (!tag_collection_started) {
            return;
        }

        try {
            json_top.put("steps", json_steps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // write json object to file
        String jsonString = json_top.toString();
        // convert to pretty Json format
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(jsonString);
        String prettyJsonString = gson.toJson(je);

        try (PrintStream out = new PrintStream(new FileOutputStream(output_file))) {
            out.print(prettyJsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Cannot write file: " + output_file);
        }
    }

    public static void close() {
        // no monitor run, skip
        if (!tag_collection_started) {
            return;
        }

        Process p;
        try {
            String file_path = sharedResourceDir + "/framework/digital_analytics/plugin_utilities/da_close.exe";

            p = getRuntime().exec(file_path);
            p.waitFor();  // wait for process to complete
        } catch (Exception e) {
            // ignore all errors
            logger.warn("ignored error : " + e.getMessage());
        }

        tag_collection_started = false;
    }

    public static Boolean monitor_run() {
        return tag_collection_started;
    }

    private static String getShaKey(Scenario scenario) {
        Iterator it = RunConfig.features.values().iterator();
        Map feature = (Map) it.next();
        String featurepath = feature.get("uri").toString();

        try {
            json_top.put("feature", featurepath);
            json_top.put("scenario", scenario.getName());
        } catch (Exception e) {
            // ignore all errors
            logger.warn("ignored error : " + e.getMessage());
        }

        return Utils.getScenarioShaKey(featurepath, scenario.getName());
    }

    private static String getTimestamp() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss");
        return sdf.format(date);
    }

    private static String regexReplace(String input, String pattern, String replace) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.replaceAll(replace);
    }

    private static Boolean parseBuffer(JSONArray json_tags, String buffer) {
        String lines[] = buffer.split("\\r?\\n");

        // parse info
        Boolean flag_findCorrectHeader = false;
        // tag info
        String tag_name;
        String tag_url;

        // param info

        String param_name = null;
        String param_description = null;
        String param_value;

        JSONObject new_tag = null;
        JSONArray param_list = null;
        JSONObject param = null;

        for (String s : lines) {
            s = s.trim();
            if (s.matches("Tag Monitor")) {
                flag_findCorrectHeader = true;
            } else if (s.matches("^Clear.*$")) {
                // ignore
                logger.info("ignore");
            } else if (s.matches(".* tag \\(.*$")) {
                // new tag
                new_tag = new JSONObject();

                // find tag name
                tag_name = regexReplace(s, " \\(.*$", "");

                // find tag url
                tag_url = regexReplace(s, "^.* \\(", "");
                tag_url = regexReplace(tag_url, "\\)$", "");

                try {
                    new_tag.put("tag_name", tag_name);
                    new_tag.put("tag_url", tag_url);
                    param_list = new JSONArray();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (s.matches(".*:$")) {
                param_name = regexReplace(s, "^.* \\(", "");
                param_name = regexReplace(param_name, "\\):$", "");

                param_description = regexReplace(s, "\\([\\w_]+\\):", "");
                param_description = param_description.trim();

                // new param
                param = new JSONObject();
            } else if (s.isEmpty()) {
                // flush previous tag
                if (param_name != null) {
                    // end of tag
                    try {
                        new_tag.put("params", param_list);
                        json_tags.put(new_tag);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    param_name = null;
                }
            } else {
                // param value
                if (param_name != null) {
                    param_value = regexReplace(s, "\\\"\\s\\(.*\\)$", "");
                    param_value = regexReplace(param_value, "\\\"", "");

                    try {
                        param.put("param", param_name);
                        param.put("description", param_description);
                        param.put("value", param_value);
                        param_list.put(param);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // last param item without blank lines
        if (param_name != null) {
            // end of tag
            try {
                new_tag.put("params", param_list);
                json_tags.put(new_tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return flag_findCorrectHeader;
    }
}
