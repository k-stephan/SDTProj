package com.macys.sdt.framework.utils.rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.macys.sdt.framework.exceptions.EnvException;
import com.macys.sdt.framework.exceptions.ProductionException;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.runner.RunConfig;
import com.macys.sdt.framework.utils.ObjectMapperProvider;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static com.macys.sdt.framework.utils.StepUtils.macys;

public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    /**
     * This method will create user profile
     *
     * @param userProfileXml user profile details in XML
     * @return UserProfile that was created
     * @throws ProductionException if called while executing against production
     */
    private static UserProfile createUserProfile(String userProfileXml, boolean v1) throws ProductionException, EnvException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        } else if (StepUtils.bloomingdales()) {
            throw new EnvException("BCOM Environments do not support the user service");
        }
        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-macys-webservice-client-id", macys() ? RESTEndPoints.MCOM_API_KEY : RESTEndPoints.BCOM_API_KEY);
            Response response = RESTOperations.doPOST(getServiceURL(v1), MediaType.APPLICATION_XML,
                    userProfileXml, headers);
            logger.info("create user profile response : " + response);
            String entity = response.readEntity(String.class);
            Assert.assertEquals(v1 ? 200 : 201, response.getStatus());
            logger.info("User profile created successfully");
            User user = ObjectMapperProvider.getXmlMapper().readValue(v1 ? entity : userProfileXml, User.class);
            UserProfile profile = new UserProfile(user, null);
            TestUsers.setCurrentCustomer(profile);
            return profile;
        } catch (Exception e) {
            logger.error("error creating user profile : ", e.getCause());
            Assert.fail(e.getMessage());
        }
        return null;
    }

    /**
     * This method will create the given user profile
     * <p>
     * Note: The v2 service requires significantly less data to create an account than v1.
     * If you have only a few fields filled in, use v2.
     * </p>
     *
     * @param profile profile to create
     * @param v1      true for service v1, false for service v2
     * @return true if profile was created successfully
     * @throws ProductionException if called while executing against production
     */
    public static boolean createUserProfile(UserProfile profile, boolean v1) throws ProductionException, EnvException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        } else if (StepUtils.bloomingdales()) {
            throw new EnvException("BCOM Environments do not support the user service");
        }
        try {
            String createUserProfileDetail = ObjectMapperProvider.getXmlMapper().writeValueAsString(profile.getUser());
            UserProfile createdProfile = createUserProfile(createUserProfileDetail, v1);
            if (createdProfile == null) {
                logger.error("Error creating profile.");
                return false;
            }
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * This method will create random user profile
     *
     * @return UserProfile that was created
     * @throws ProductionException if called while executing against production
     */
    public static UserProfile createRandomUserProfile() throws ProductionException, EnvException {
        UserProfile userProfile = TestUsers.getCustomer(null);
        createUserProfile(userProfile, true);
        return userProfile;
    }

    /**
     * This method will create a user profile from the given object
     *
     * @param profile UserProfile to create
     * @return true if profile was created successfully
     * @throws ProductionException if called while executing against production
     */
    public static boolean createUserProfile(UserProfile profile) throws ProductionException, EnvException {
        return createUserProfile(profile, true);
    }

    private static String getServiceURL(boolean v1) {
        try {
            URL url = new URL(RunConfig.url);
            return "http://api." + url.getHost().replace("www.", "").replace("www1.", "").replace("m.", "")
                    + (v1 ? RESTEndPoints.CREATE_USER_PROFILE : RESTEndPoints.CREATE_USER_PROFILE_V2);
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
