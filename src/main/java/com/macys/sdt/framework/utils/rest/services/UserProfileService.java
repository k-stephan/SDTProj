package com.macys.sdt.framework.utils.rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.macys.sdt.framework.Exceptions.EnvException;
import com.macys.sdt.framework.Exceptions.ProductionException;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    /**
     * This method will create user profile
     *
     * @param userProfileDetailInXml : user profile details in JSON
     * @return UserProfile that was created
     * @throws ProductionException if called while executing against production
     */
    public static UserProfile createUserProfile(String userProfileDetailInXml) throws ProductionException, EnvException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        } else if (StepUtils.bloomingdales()) {
            throw new EnvException("BCOM Environments do not support the user service");
        }
        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-macys-webservice-client-id", macys() ? RESTEndPoints.MCOM_API_KEY : RESTEndPoints.BCOM_API_KEY);
            Response response = RESTOperations.doPOST(getServiceURL(), MediaType.APPLICATION_XML,
                    userProfileDetailInXml, headers);
            System.out.println("response : " + response);
            String entity = response.readEntity(String.class);
            Assert.assertEquals(response.getStatus(), 200);
            LOGGER.info("User profile created successfully");
            User user = new XmlMapper().readValue(entity, User.class);
            UserProfile profile = new UserProfile(user, null);
            TestUsers.setCurrentCustomer(profile);
            return profile;
        } catch (Exception e) {
            LOGGER.error("error creating user profile", e.getCause());
            Assert.fail(e.getMessage());
        }
        return null;
    }

    /**
     * This method will create random user profile
     *
     * @return UserProfile that was created
     * @throws ProductionException if called while executing against production
     */
    public static UserProfile createRandomUserProfile() throws ProductionException, EnvException {
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        } else if (StepUtils.bloomingdales()) {
            throw new EnvException("BCOM Environments do not support the user service");
        }
        UserProfile userProfile = TestUsers.getCustomer(null);
        XmlMapper mapper = new XmlMapper();
        String createUserProfileDetail = null;
        try {
            createUserProfileDetail = mapper.writeValueAsString(userProfile.getUser())
                    .replace("<User>", "<user>").replace("</User>", "</user>");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("user detail : " + createUserProfileDetail);
        createUserProfile(createUserProfileDetail);
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
        if (StepUtils.prodEnv()) {
            throw new ProductionException("Cannot use services on prod!");
        } else if (StepUtils.bloomingdales()) {
            throw new EnvException("BCOM Environments do not support the user service");
        }
        try {
            String createUserProfileDetail = new XmlMapper().writeValueAsString(profile.getUser())
                    .replace("<User>", "<user>").replace("</User>", "</user>");
            UserProfile createdProfile = createUserProfile(createUserProfileDetail);
            if (createdProfile == null) {
                System.err.println("Error creating profile.");
                return false;
            }
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private static String getServiceURL() {
        try {
            URL url = new URL(MainRunner.url);
            return "http://api." + url.getHost().replace("www.", "").replace("www1.", "").replace("m.", "")
                    + RESTEndPoints.CREATE_USER_PROFILE;
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
