package com.macys.sdt.framework.utils.db.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentTest {

    @Test
    public void testSetAndGetConfigs() throws Exception {
        List<DBConfig> configs = new ArrayList<>();
        configs.add(new DBConfig("qa0codemacys", "11.168.114.97", "BLX01", "db2Schema"));
        configs.add(new DBConfig("qa0codebloomingdales", "11.168.114.101", "BLX04", "db2Schema"));
        Environment env = new Environment();
        env.setConfigs(configs);
        Assert.assertTrue(env.getConfigs().size() == 2);
    }
}