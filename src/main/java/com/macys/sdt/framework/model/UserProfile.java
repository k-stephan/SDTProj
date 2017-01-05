package com.macys.sdt.framework.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This class represents a UserProfile and contains all the information about that UserProfile
 */
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
     * Sets the Registry of UserProfile
     *
     * @param registry UserProfile
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
