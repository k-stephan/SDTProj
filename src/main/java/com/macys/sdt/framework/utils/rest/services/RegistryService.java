package com.macys.sdt.framework.utils.rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.macys.sdt.framework.Exceptions.ProductionException;
import com.macys.sdt.framework.Exceptions.UserException;
import com.macys.sdt.framework.model.registry.Registry;
import com.macys.sdt.framework.model.user.TokenCredentials;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.utils.Cookies;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.EnvironmentDetails;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);

    /**
     * Attempts to create a registry using xml registry service
     *
     * @param registry Registry to be created
     * @param token Secure user token to use
     * @return Registry that was created or null on failure
     * @throws ProductionException if called while executing against production
     */
    public static Registry createRegistry(Registry registry, String token) throws ProductionException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        }
        try {
            String registryXml = new XmlMapper().writeValueAsString(registry);
            registryXml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>"
                    + registryXml.replace("<Registry>", "<registry>").replace("</Registry>", "</registry>");

            Map<String, String> headers = new HashMap<>();
            headers.put("X-Macys-SecurityToken", token);

            Response response = RESTOperations.doPOST(getServiceURL(), MediaType.APPLICATION_XML, registryXml, headers);

            LOGGER.info("response : " + response);
            Assert.assertEquals(response.getStatus(), 200);
            LOGGER.info("User profile created successfully");
            return registry;
        } catch (JsonProcessingException e) {
            LOGGER.error("error creating registry", e.getCause());
            return null;
        }
    }

    /**
     * Creates a random registry optionally using given user
     *
     * @param user User to base off of. Pass null to use current signed in user
     * @return Registry that is created or null on failure
     * @throws ProductionException if called while executing against production
     */
    public static Registry createRandomRegistry(User user) throws ProductionException, UserException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        }

        if (user == null) {
            UserProfile customer = TestUsers.getCustomer(null);
            user = customer.getUser();
        }
        if (user.getTokenCredentials() == null) {
            user.setTokenCredentials(new TokenCredentials());
            user.getTokenCredentials().setToken(Cookies.getSecureUserToken());
        }
        if (user.getId() == null || user.getId().equals("1234")) {
            user.setId(Cookies.getCurrentUserId());
            if (user.getId().isEmpty()) {
                throw new UserException("Registry service requires user with valid user ID");
            }
        }
        Registry registry = new Registry();
        registry.addRandomData();
        registry.setUserId(user.getId());
        Registry serviceCopy = Registry.getRegistryServiceRegistry(registry);
        serviceCopy = RegistryService.createRegistry(serviceCopy, user.getTokenCredentials().getToken());
        Assert.assertNotNull(serviceCopy);
        return registry;
    }

    private static String getServiceURL() {
        return "http://" + EnvironmentDetails.otherApp("mspcustomer").ipAddress + ":8080" + RESTEndPoints.CREATE_REGISTRY;
    }

}
