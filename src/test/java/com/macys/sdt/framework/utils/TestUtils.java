package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.db.models.RegistryService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class TestUtils {

    /**
     * This modifies the copy of env variables held by the JVM. For test purposes ONLY
     *
     * @param newEnv map of environment variables you want to set
     */
    public static void setEnv(Map<String, String> newEnv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newEnv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> CIEnv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            CIEnv.putAll(newEnv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newEnv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Test
    public void asdf() {
        MainRunner.url = "http://www.qa15codemacys.fds.com";
        RegistryService.registryExists("12345");
    }
}
