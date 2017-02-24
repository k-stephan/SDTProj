package com.macys.sdt.framework.utils.rest.utils;


import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Optional;
import java.util.regex.Pattern;

public class RESTUtils {

    private static final Logger logger = LoggerFactory.getLogger(RESTUtils.class);

    /**
     * convert relative REST uri to absolute URI
     *
     * @param relativePath : relative REST uri
     * @return absolute REST uri
     */
    public static String fullURI(String relativePath) {
        return getBaseAddress().toString().replace("m.", "") + relativePath;
    }

    /**
     * Create REST client
     *
     * @return REST client
     */
    public static Client createClient() {
        return ClientBuilder.newClient();
    }

    /**
     * create web target with base address and resource path
     *
     * @param client   : REST client
     * @param resource : resource path to specific API
     * @return WebTarget
     */
    public static WebTarget createTarget(Client client, String resource) {
        String baseAddress = getBaseAddress();
        WebTarget webTarget;
        String pattern = "^https?://.+";        //works for both http and https

        //return webTarget when full uri is passed
        if (resource != null && Pattern.matches(pattern, resource)) {
            webTarget = client.target(resource);
            return webTarget;
        }

        //return webTarget when relative uri is passed
        webTarget = client.target(baseAddress);

        if (resource != null && !resource.isEmpty()) {
            webTarget = webTarget.path(resource);
        }
        return webTarget;
    }

    /**
     * get the base address of the web site
     *
     * @return base address
     */
    public static String getBaseAddress() {
        try {
            String baseAddress = Optional.ofNullable(MainRunner.url).orElseThrow(Exception::new);
            baseAddress = baseAddress.replace("m.", "");
            return baseAddress;
        } catch (Exception e) {
            Assert.fail("Website detail not present");
        }
        return null;
    }
}
