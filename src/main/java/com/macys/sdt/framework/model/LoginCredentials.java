package com.macys.sdt.framework.model;

/**
 * This class represents a LoginCredentials and contains all the information about that LoginCredentials
 */
public class LoginCredentials {
    private String password;

    public LoginCredentials() {
    }

    public LoginCredentials(String password) {
        this.password = password;
    }

    /**
     * Gets the DefaultLoginCredentials of LoginCredentials
     *
     * @return LoginCredentials DefaultLoginCredentials
     */
    public static LoginCredentials getDefaultLoginCredentials() {
        return new LoginCredentials("Macys12345");
    }

    /**
     * Gets the Password of LoginCredentials
     *
     * @return LoginCredentials Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the Password of LoginCredentials
     *
     * @param password LoginCredentials
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
