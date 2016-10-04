package com.macys.sdt.framework.shared.steps.website;

import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.utils.StepUtils;
import cucumber.api.java.en.Given;

//TODO : including this class to push directory. will delete once proper class are pushed.
public class TestJar extends StepUtils{

    @Given("^test jar$")
    public void test_jar() throws Throwable {
        System.out.println("TestJar.test_jar");
        Navigate.visit("home");
    }


}
