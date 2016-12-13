package com.macys.sdt.framework.runner;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Tests for MainRunner
 */
public class MainRunnerTest {
    @Test
    public void testScenarioRecognition() {
        MainRunner.scenarios = "src/test/java/com/macys/sdt/framework/Features/website/mcom/test.feature " +
                "src/test/java/com/macys/sdt/framework/Features/website/mcom/test2.feature";

        String website = "http://www.macys.com";
        String browser = "firefox";
        String project = "framework";
        MainRunner.url = website;
        MainRunner.browser = browser;
        MainRunner.project = project;

        ArrayList<String> scenarios = MainRunner.getFeatureScenarios();
        Assert.assertTrue(scenarios.size() == 2);
        Assert.assertTrue(scenarios.get(0).equals(MainRunner.scenarios.split(" ")[0]));
        Assert.assertTrue(scenarios.get(1).equals(MainRunner.scenarios.split(" ")[1]));
    }
}
