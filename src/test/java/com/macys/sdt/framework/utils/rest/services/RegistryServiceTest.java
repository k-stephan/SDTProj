package com.macys.sdt.framework.utils.rest.services;


import com.macys.sdt.framework.model.registry.Registry;
import com.macys.sdt.framework.model.user.UserProfile;
import com.macys.sdt.framework.runner.MainRunner;
import org.junit.Test;

public class RegistryServiceTest {

    @Test
    public void testCreateRegistry() {
        MainRunner.url = "http://www.qa15codemacys.fds.com";
        UserProfile profile = UserProfileService.createRandomUserProfile();

        Registry registry = new Registry();
        registry.addRandomData();
        registry.setUserId("2158517601");
        registry = RegistryService.createRegistry(registry);
        System.out.println(registry);

    }
}
