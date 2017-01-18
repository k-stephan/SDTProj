package com.macys.sdt.framework.utils.rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.macys.sdt.framework.model.registry.Registry;
import com.macys.sdt.framework.utils.Cookies;
import com.macys.sdt.framework.utils.db.utils.EnvironmentDetails;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * This talks to the v3 registry SDP service
 */
public class RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    public static Registry createRegistry(Registry registry) {
        try {
            String registryXml = new XmlMapper().writeValueAsString(registry);
            registryXml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
                    + registryXml.replace("<Registry>", "<registry>").replace("</Registry>", "</registry>");
            String token = Cookies.getSecureUserToken();
            //String token = "13_MYW7loj5u3c2Zg7XSOhgBYLq5BEqSB0RYzz5kER0zDFyj/AxKZX202NWN7kP9RCa5olVC+503Ax6w/1JD8BSLw==";
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Macys-SecurityToken", token);
            Response response = RESTOperations.doPOST(getServiceURL(), MediaType.APPLICATION_XML, registryXml, headers);
            System.out.println("response : " + response);
            Assert.assertEquals(response.getStatus(), 201);
            LOGGER.info("User profile created successfully");
            return registry;
        } catch (JsonProcessingException e) {
            LOGGER.error("error creating registry", e.getCause());
            return null;
        }
    }

    public static Registry createRandomRegistry() {
        return null;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("mspcustomer").ipAddress + ":8080" + RESTEndPoints.CREATE_REGISTRY;
    }

}
