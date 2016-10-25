package com.macys.sdt.framework.model;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
