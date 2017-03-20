package com.macys.sdt.framework.model.user;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.macys.sdt.framework.model.addresses.ProfileAddress;
import com.macys.sdt.framework.model.registry.Registry;
import com.macys.sdt.framework.utils.TestUsers;

/**
 * This class represents a UserProfile and contains all the information about that UserProfile
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {

    private User user;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Registry registry;

    public UserProfile() {
    }

    public UserProfile(User user, Registry registry) {
        this.user = user;
        this.registry = registry;
    }

    /**
     * Gets the DefaultProfile of UserProfile
     *
     * @return UserProfile DefaultProfile
     */
    public static UserProfile getDefaultProfile() {
        return new UserProfile(User.getDefaultUser(), null);
    }

    public static UserProfile v2Profile() {
        UserProfile profile = new UserProfile();
        profile.user = new User();
        ProfileAddress address = new ProfileAddress();
        address.setFirstName("first");
        address.setLastName("last");
        address.setEmail(TestUsers.generateRandomEmail(7));
        profile.user.setProfileAddress(address);
        LoginCredentials credentials = new LoginCredentials();
        credentials.setPassword("Macys12345");
        profile.user.setLoginCredentials(credentials);
        return profile;
    }

    /**
     * Gets the User of UserProfile
     *
     * @return UserProfile User
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the User of UserProfile
     *
     * @param user UserProfile
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the Registry of UserProfile
     *
     * @return UserProfile Registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * A custom getter to prevent the writing of incorrect data when attempting to create a user using the user service.
     * The user service uses a different representation than the registry service, so we need the correct data
     *
     * @return registry with only data for user service filled in
     */
    @JsonGetter("registry")
    public Registry serializeRegistry() {
        if (this.registry == null) {
            return null;
        }
        return Registry.getUserServiceRegistry(this.registry);
    }

    /**
     * Sets the Registry of UserProfile
     *
     * @param registry UserProfile
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
