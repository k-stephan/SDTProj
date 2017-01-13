package com.macys.sdt.framework.utils.rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import com.thoughtworks.xstream.mapper.XmlFriendlyMapper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UserProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    /**
     * This method will create user profile
     *
     * @param userProfileDetailInJson : user profile details in JSON
     * @return UserProfile that was created
     */
    public static UserProfile createUserProfile(String userProfileDetailInJson) {
        try {
            Response response = RESTOperations.doPOST(RESTEndPoints.CREATE_USER_PROFILE, MediaType.APPLICATION_JSON, userProfileDetailInJson, null);
            System.out.println("response : " + response);
            Assert.assertEquals(response.getStatus(), 201);
            LOGGER.info("User profile created successfully");
            UserProfile profile = new ObjectMapper().readValue(userProfileDetailInJson, UserProfile.class);
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
     */
    public static UserProfile createRandomUserProfile() {
        UserProfile userProfile = TestUsers.getCustomer(null);

        ObjectMapper mapper = new ObjectMapper();
        String createUserProfileDetail = null;
        try {
            createUserProfileDetail = mapper.writeValueAsString(userProfile);
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
     */
    public static boolean createUserProfile(UserProfile profile) {
        try {
            String profileJSON = new ObjectMapper().writeValueAsString(profile);
            UserProfile createdProfile = createUserProfile(profileJSON);
            if (createdProfile == null) {
                System.err.println("Error creating profile.");
                return false;
            }
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

}
