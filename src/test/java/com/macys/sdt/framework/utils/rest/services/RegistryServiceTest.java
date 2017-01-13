package com.macys.sdt.framework.utils.rest.services;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.macys.sdt.framework.model.registry.Registry;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class RegistryServiceTest {

    @Test
    public void testCreateRegistry() {
        try {
            Registry registry = new XmlMapper().readValue(new File("test.xml"), Registry.class);
            System.out.println(registry.userId);
        } catch (IOException e) {
            System.out.println("fuck");
            //fuckall
        }
    }
}
