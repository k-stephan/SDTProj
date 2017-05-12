package com.macys.sdt.framework.utils.rest.utils;

import com.macys.sdt.framework.runner.RunConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD;

public class RESTOperations {

    private static Logger logger = LoggerFactory.getLogger(RESTOperations.class);
    /**
     * POST operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media eventType (eg: application/json)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    @Deprecated
    public static Response doPOST(String resource, String mediaType, String requestPayload) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            logger.info("request payload : " + requestPayload);
            response = webTarget.request(mediaType).post(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            logger.error("error in REST POST call : " + e.getMessage());
            logger.trace("REST POST call error : " + e);
        }
        return response;
    }

    /**
     * POST operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media type (eg: application/json)
     * @param headers        : headers (put null if no data)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    public static Response doPOST(String resource, String mediaType, String requestPayload, Map<String, String> headers) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            logger.info("request payload : " + requestPayload);
            Invocation.Builder requestBuilder = webTarget.request(mediaType);
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.post(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            logger.error("error in REST POST call : " + e.getMessage());
            logger.trace("REST POST call error : " + e);
        }
        return response;
    }

    /**
     * GET operation
     *
     * @param resource : REST uri
     * @return REST response
     */
    @Deprecated
    public static Response doGET(String resource) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            response = webTarget.request().get();
        } catch (Exception e) {
            logger.error("error in REST GET call : " + e.getMessage());
            logger.trace("REST GET call error : " + e);
        }
        return response;
    }

    /**
     * GET operation
     *
     * @param resource : REST uri
     * @param headers  : headers (put null if no data)
     * @return REST response
     */
    public static Response doGET(String resource, Map<String, String> headers) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            Invocation.Builder requestBuilder = webTarget.request();
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.get();
        } catch (Exception e) {
            logger.error("error in REST GET call : " + e.getMessage());
            logger.trace("REST GET call error : " + e);
        }
        return response;
    }

    /**
     * GET operation with Basic authentication detail
     *
     * @param resource  : REST uri
     * @param headers   : headers (put null if no data)
     * @param user      : user name
     * @param password  : password
     * @return REST response
     */
    public static Response doGETWithBasicAuth(String resource, Map<String, String> headers, String user, String password) {
        Response response = null;
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().build();
        Client client = RESTUtils.createClient();
        client.register(feature);
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            Invocation.Builder requestBuilder = webTarget.request()
                    .property(HTTP_AUTHENTICATION_BASIC_USERNAME, user)
                    .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.get();
        } catch (Exception e) {
            logger.error("error in REST GET call : " + e.getMessage());
            logger.trace("REST GET call error : " + e);
        }
        return response;
    }

    /**
     * DELETE operation
     *
     * @param resource : REST uri
     * @return REST response
     */
    @Deprecated
    public static Response doDELETE(String resource) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            response = webTarget.request().delete();
        } catch (Exception e) {
            logger.error("error in REST DELETE call : " + e.getMessage());
            logger.trace("REST DELETE call error : " + e);
        }
        return response;
    }

    /**
     * DELETE operation
     *
     * @param resource : REST uri
     * @param headers  : headers (put null if no data)
     * @return REST response
     */
    public static Response doDELETE(String resource, Map<String, String> headers) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            Invocation.Builder requestBuilder = webTarget.request();
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.delete();
        } catch (Exception e) {
            logger.error("error in REST DELETE call : " + e.getMessage());
            logger.trace("REST DELETE call error : " + e);
        }
        return response;
    }

    /**
     * PUT operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media type (eg: application/json)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    @Deprecated
    public static Response doPUT(String resource, String mediaType, String requestPayload) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            logger.info("request payload : " + requestPayload);
            response = webTarget.request(mediaType).put(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            logger.error("error in REST PUT call : " + e.getMessage());
            logger.trace("REST PUT call error : " + e);
        }
        return response;
    }

    /**
     * PUT operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media eventType (eg: application/json)
     * @param headers        : headers (put null if no data)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    public static Response doPUT(String resource, String mediaType, String requestPayload, Map<String, String> headers) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            logger.info("request payload : " + requestPayload);
            Invocation.Builder requestBuilder = webTarget.request(mediaType);
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.put(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            logger.error("error in REST PUT call : " + e.getMessage());
            logger.trace("REST PUT call error : " + e);
        }
        return response;
    }
}
