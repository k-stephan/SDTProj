package com.macys.sdt.framework.model;

import com.macys.sdt.framework.utils.TestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class KillSwitchTest {

    @BeforeClass
    public static void setUp() throws Exception {
        HashMap<String, String> env = new HashMap<>();
        env.put("killSwitchUnitTest", "true");
        TestUtils.setEnv(env);
    }

    @Test
    public void testGetEnabled() throws Exception {
        Assert.assertTrue(KillSwitch.getEnabled("simplifiedRegistryCheckoutEnabled"));
        Assert.assertFalse(KillSwitch.getEnabled("newSignUpEnabled"));
    }

    @Test
    public void testGetData() throws Exception {
        KillSwitch.KSData data = KillSwitch.getData("uslEnabled");
        Assert.assertEquals("USLFeatureEnabled", data.getFeatureName());
        Assert.assertEquals("Loyalty Feature", data.getKeyDisplayName());
        Assert.assertTrue(data.getEnabled());

        KillSwitch.KSData ksData = new KillSwitch.KSData("threeDSecurityEnabled");
        Assert.assertEquals("ThreeDSecurityEnabled", ksData.getFeatureName());
        Assert.assertEquals("3D Security Feature", ksData.getKeyDisplayName());
        Assert.assertTrue(ksData.getEnabled());
    }
}