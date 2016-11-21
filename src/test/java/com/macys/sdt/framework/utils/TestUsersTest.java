package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.model.User;
import com.macys.sdt.framework.model.UserProfile;
import org.junit.Assert;
import org.junit.Test;

public class TestUsersTest {

    @Test
    public void defaultProfileTest() {
        UserProfile profile = UserProfile.getDefaultProfile();
        User user = profile.getUser();

        Assert.assertNotNull(profile);
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getProfileAddress());
        Assert.assertNotNull(user.getLoginCredentials());
        Assert.assertNotNull(user.getUserPasswordHint());
        Assert.assertNull(profile.getRegistry());
    }
}
