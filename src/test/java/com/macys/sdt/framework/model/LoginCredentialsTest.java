package com.macys.sdt.framework.model;

import com.macys.sdt.framework.model.user.LoginCredentials;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for LoginCredentials Model
 */
public class LoginCredentialsTest {

    @Test
    public void testGetDefaultLoginCredentials() throws Exception {
        LoginCredentials logincredentials = LoginCredentials.getDefaultLoginCredentials();
        Assert.assertNotNull(logincredentials);
        Assert.assertNotNull(logincredentials.getPassword());
    }

    @Test
    public void testGetPassword() throws Exception {
        LoginCredentials loginCredentials = new LoginCredentials();
        String password = "password";
        loginCredentials.setPassword(password);
        Assert.assertEquals(password, loginCredentials.getPassword());
    }
}