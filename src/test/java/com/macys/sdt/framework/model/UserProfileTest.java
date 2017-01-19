package com.macys.sdt.framework.model;

import com.macys.sdt.framework.model.registry.Registry;
import com.macys.sdt.framework.model.user.User;
import com.macys.sdt.framework.model.user.UserProfile;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for UserProfile Model
 */
public class UserProfileTest {

    private UserProfile userProfile;

    public UserProfileTest() {
        userProfile = new UserProfile();
    }

    @Test
    public void testGetDefaultProfile() throws Exception {
        UserProfile profile = UserProfile.getDefaultProfile();
        Assert.assertNotNull(profile);
        Assert.assertNotNull(profile.getUser());
        Assert.assertNull(profile.getRegistry());
    }

    @Test
    public void testGetUser() throws Exception {
        UserProfile profile = UserProfile.getDefaultProfile();
        userProfile.setUser(profile.getUser());
        User user = userProfile.getUser();
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getDateOfBirth());
        Assert.assertNotNull(user.getGender());
        Assert.assertNotNull(user.getSubscribedToNewsLetter());
        Assert.assertNotNull(user.getUserPasswordHint());
        Assert.assertNotNull(user.getProfileAddress());
        Assert.assertNotNull(user.getLoginCredentials());
    }

    @Test
    public void testGetRegistry() throws Exception {
        Registry registry = new Registry();
        registry.setEventType("WEDDING");
        registry.setEventMonth("December");
        registry.setEventDay("17");
        registry.setEventYear("2016");
        registry.setEventLocation("Alaska");
        registry.setNumberOfGuest("110");
        registry.setPreferredStoreState("New York");
        registry.setPreferredStore("New York - Herald Square");
        registry.setCoRegistrantFirstName("JAMES");
        registry.setCoRegistrantLastName("SMITH");
        registry.setId("1234567");
        userProfile.setRegistry(registry);
        Registry registryInfo = userProfile.getRegistry();
        Assert.assertNotNull(registryInfo);
        Assert.assertEquals(registryInfo.getEventType(), "WEDDING");
        Assert.assertEquals(registryInfo.getEventMonth(), "December");
        Assert.assertEquals(registryInfo.getEventDay(), "17");
        Assert.assertEquals(registryInfo.getEventYear(), "2016");
        Assert.assertEquals(registryInfo.getEventLocation(), "Alaska");
        Assert.assertEquals(registryInfo.getNumberOfGuest(), "110");
        Assert.assertEquals(registryInfo.getPreferredStoreState(), "New York");
        Assert.assertEquals(registryInfo.getPreferredStore(), "New York - Herald Square");
        Assert.assertEquals(registryInfo.getCoRegistrantFirstName(), "JAMES");
        Assert.assertEquals(registryInfo.getCoRegistrantLastName(), "SMITH");
        Assert.assertEquals(registryInfo.getId(), "1234567");
    }
}