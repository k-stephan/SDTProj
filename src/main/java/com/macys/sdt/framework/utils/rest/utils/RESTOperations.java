package com.macys.sdt.framework.utils.rest.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Map;

public class RESTOperations {


    /**
     * POST operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media type (eg: application/json)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    @Deprecated
    public static Response doPOST(String resource, String mediaType, String requestPayload) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            System.out.println("requestpayload : " + requestPayload);
            response = webTarget.request(mediaType).post(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println("requestpayload : " + requestPayload);
            Invocation.Builder requestBuilder = webTarget.request(mediaType);
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.post(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            System.out.println("requestpayload : " + requestPayload);
            response = webTarget.request(mediaType).put(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * PUT operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media type (eg: application/json)
     * @param headers        : headers (put null if no data)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    public static Response doPUT(String resource, String mediaType, String requestPayload, Map<String, String> headers) {
        Response response = null;
        Client client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client, resource);
            System.out.println("requestpayload : " + requestPayload);
            Invocation.Builder requestBuilder = webTarget.request(mediaType);
            if (headers != null && !headers.isEmpty()) {
                for (String headerKey : headers.keySet()) {
                    requestBuilder.header(headerKey, headers.get(headerKey));
                }
            }
            response = requestBuilder.put(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
