package com.macys.sdt.framework.utils.rest.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class RESTOperations {


    /**
     * POST operation
     *
     * @param resource       : REST uri
     * @param mediaType      : media type (eg: application/json)
     * @param requestPayload : request payload (compatible with mediaType)
     * @return REST response
     */
    public static Response doPOST(String resource, String mediaType, String requestPayload) {
        Response response = null;
        Optional<Client> client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client.get(), resource);
            System.out.println("requestpayload : " + requestPayload);
            response = webTarget.request(mediaType).post(Entity.entity(requestPayload, mediaType));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.get().close();
        }
        return response;
    }

    /**
     * GET operation
     *
     * @param resource : REST uri
     * @return REST response
     */
    public static Response doGET(String resource) {
        Response response = null;
        Optional<Client> client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client.get(), resource);
            response = webTarget.request().get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.get().close();
        }
        return response;
    }

    /**
     * DELETE operation
     *
     * @param resource : REST uri
     * @return REST response
     */
    public static Response doDELETE(String resource) {
        Response response = null;
        Optional<Client> client = RESTUtils.createClient();
        try {
            WebTarget webTarget = RESTUtils.createTarget(client.get(), resource);
            response = webTarget.request().delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.get().close();
        }
        return response;
    }
}
