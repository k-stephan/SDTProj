package com.macys.sdt.framework.utils.rest;

import com.macys.sdt.framework.model.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.TestUtils;
import com.macys.sdt.framework.utils.rest.services.UserProfileService;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class UserProfileServiceTest {

/*    @Test
    public void defaultProfileTest() {
        if (MainRunner.getEnvOrExParam("website") == null) {
            HashMap<String, String> env = new HashMap<>();
            env.put("website", "http://qa17codemacys.fds.com");
            TestUtils.setEnv(env);
        }

        UserProfile profile = UserProfileService.createRandomUserProfile();
        Assert.assertNotNull(profile);
        Assert.assertNotNull(profile.getUser());
        Assert.assertNotNull(profile.getRegistry());
    }*/
}
