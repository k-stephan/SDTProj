package com.macys.sdt.framework.model;

public class LoginCredentials {
    private String password;

    public LoginCredentials() {
    }

    public LoginCredentials(String password) {
        this.password = password;
    }

    public static LoginCredentials getDefaultLoginCredentials() {
        return new LoginCredentials("Macys12345");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
