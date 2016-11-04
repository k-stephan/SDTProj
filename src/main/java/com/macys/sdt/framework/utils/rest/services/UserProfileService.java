package com.macys.sdt.framework.utils.rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.utils.TestUsers;
import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.rest.utils.RESTEndPoints;
import com.macys.sdt.framework.utils.rest.utils.RESTOperations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class UserProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    private static String filepath = "src/com/macys/sdt/shared/resources/data/website/mcom";

    /**
     * This method will create user profile
     *
     * @param userProfileDetailInJson : user profile details in JSON
     * @return UserProfile that was created
     */
    public static UserProfile createUserProfile(String userProfileDetailInJson) {
        try {
            Response response = RESTOperations.doPOST(RESTEndPoints.CREATE_USER_PROFILE, MediaType.APPLICATION_JSON, userProfileDetailInJson);
            System.out.println("response : " + response);
            Assert.assertEquals(response.getStatus(), 201);
            LOGGER.info("User profile created successfully");
            return new ObjectMapper().readValue(userProfileDetailInJson, UserProfile.class);
        } catch (Exception e) {
            LOGGER.error("error creating user profile", e.getCause());
            Assert.fail(e.getMessage());
        }
        return null;
    }

    /**
     * This method will create default user profile
     *
     * @return UserProfile that was created
     */
    public static UserProfile createUserProfileFromFile() {
        File userProfileFile = Utils.getResourceFile(filepath, "user_profile.json");
        JSONObject createUserProfileDetail = Utils.getFileDataInJson(userProfileFile);
        return createUserProfile(createUserProfileDetail.toString());
    }

    /**
     * This method will create random user profile
     *
     * @return UserProfile that was created
     */
    public static UserProfile createRandomUserProfile() {
        String projectLocation = System.getProperty("user.dir");
        String absolutePath = projectLocation + "/" + filepath;
        File userProfileFile = Utils.getResourceFile(absolutePath, "user_profile.json");
        ObjectMapper mapper = new ObjectMapper();
        UserProfile userProfile = null;
        User user = null;
        try {
            userProfile = mapper.readValue(userProfileFile, UserProfile.class);
            user = userProfile.getUser();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (user != null) {
            ProfileAddress profileAddress = user.getProfileAddress();
            if (profileAddress == null) {
                profileAddress = new ProfileAddress();
            }
            profileAddress.setEmail(TestUsers.generateRandomEmail(16));
            profileAddress.setFirstName(TestUsers.generateRandomFirstName());
            profileAddress.setLastName(TestUsers.generateRandomLastName());
            profileAddress.setBestPhone(TestUsers.generateRandomPhoneNumber());
            user.setProfileAddress(profileAddress);
            user.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").format(TestUsers.generateRandomDate()));
            userProfile.setUser(user);
        }
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
