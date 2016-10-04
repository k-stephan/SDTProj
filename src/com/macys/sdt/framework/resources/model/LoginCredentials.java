package com.macys.sdt.framework.resources.model;

public class LoginCredentials {
    private String password;

    public LoginCredentials() {
    }

    public LoginCredentials(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
