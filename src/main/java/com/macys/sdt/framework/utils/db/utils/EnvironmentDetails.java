package com.macys.sdt.framework.utils.db.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class EnvironmentDetails {

    static final String ENV_URL = MainRunner.url;

    public String envName, ipAddress, hostName;
    static boolean stage5 = MainRunner.url.matches(
            ".*?(http://)?(www\\.)?(m\\.)?qa[0-9][0-9]?code(macys|mcom|bcom|bloomingdales)\\.fds\\.com.*?");

    public EnvironmentDetails(String envName, String ipAddress, String hostName) {
        this.envName = envName;
        this.ipAddress = ipAddress;
        this.hostName = hostName;
    }

    /**
     * Returns my services application details.
     *
     * @param appName name of the app, ex: navapp, legacy, etc.
     * @return my services application details
     */
    public static EnvironmentDetails myServicesApp(String appName) {

        EnvironmentDetails appDetails = new EnvironmentDetails(null, null, null);

        try {
            String json = EnvironmentDetails.getJSONString();
            JSONObject jsonObject = new JSONObject(json);

            String eName = getEnv(ENV_URL);

            JSONArray myServicesInfo;
            if (stage5) {
                JSONArray environmentDetails = new JSONArray(jsonObject.get("envDetails").toString());
                myServicesInfo = (JSONArray)environmentDetails.getJSONObject(0).get("myServicesIpBoList");
            } else {
                myServicesInfo = (JSONArray)jsonObject.get("myServicesIpBoList");
            }

            for (int i = 0; i < myServicesInfo.length(); i++) {
                JSONObject appInfo = myServicesInfo.getJSONObject(i);
                if (appInfo.get("appName").toString().equalsIgnoreCase(appName)) {
                    appDetails = new EnvironmentDetails(
                            eName, appInfo.get("appIp").toString(), stage5 ? appInfo.get("hostName").toString() : "");
                    break;
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return appDetails;

    }

    /**
     * Returns application details.
     *
     * @param appName name of the app, ex: mspcustomer, msporder, etc.
     * @return application details
     */
    public static EnvironmentDetails otherApp(String appName) {

        EnvironmentDetails appDetails = new EnvironmentDetails(null, null, null);

        try {
            String json = EnvironmentDetails.getJSONString();
            JSONObject jsonObject = new JSONObject(json);

            String eName = getEnv(ENV_URL);

            JSONArray applicationInfo;
            if (stage5) {
                JSONArray environmentDetails = new JSONArray(jsonObject.get("envDetails").toString());
                applicationInfo = (JSONArray) environmentDetails.getJSONObject(0).get("applicationBolist");
            } else {
                applicationInfo = (JSONArray)jsonObject.get("applicationBolist");
            }

            for (int i = 0; i < applicationInfo.length(); i++) {
                JSONObject appInfo = applicationInfo.getJSONObject(i);
                if (appInfo.getString("appName").equalsIgnoreCase(appName)) {
                    String hName = null;
                    if (!appName.equalsIgnoreCase("f5_vip")) {
                        hName = appInfo.get("envName").toString();
                    }
                    appDetails = new EnvironmentDetails(eName, appInfo.get("ipAddress").toString(), hName);
                    break;
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return appDetails;

    }

    /**
     * Returns environment details.
     *
     * @return environment details
     * @throws IOException if response is unreadable
     */
    public static String getJSONString() throws IOException {

        String serviceUrl = getServiceURL(ENV_URL);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serviceUrl);

        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();
        httpClient.close();

        return response.toString();

    }

    /**
     * Returns the service URL.
     *
     * @param envUrl environment URL
     * @return service URL
     */
    static String getServiceURL(String envUrl) {
        final String GET_URL =  stage5 ?
                "http://mdc2vr6133:8088/EnvironmentDetailsRestApi/environmentService/getNewEnvDetails/" :
                "http://c4d.devops.fds.com/reinfo/";

        return GET_URL + getEnv(envUrl);
    }

    /**
     * Returns the environment name for a given URL.
     *
     * @param envUrl environment URL
     * @return environment name
     */
    private static String getEnv(String envUrl) {
        try {
            URL url = new URL(envUrl);
            String[] split = url.getHost().split("\\.");
            return split[0].matches("www|m") ? split[1] : split[0];
        } catch (MalformedURLException e) {
            System.err.println("Unable to get environment details");
            return null;
        }
    }
}