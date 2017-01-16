package com.macys.sdt.framework.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for Registry Model
 */
public class RegistryTest {

    private Registry registry;

    public RegistryTest() {
        registry = new Registry();
    }

    @Test
    public void testGetEventType() throws Exception {
        registry.setEventType("WEDDING");
        String eventType = registry.getEventType();
        Assert.assertEquals("WEDDING", eventType);
    }

    @Test
    public void testGetEventMonth() throws Exception {
        registry.setEventMonth("December");
        String eventMonth = registry.getEventMonth();
        Assert.assertEquals("December", eventMonth);
    }

    @Test
    public void testGetEventDay() throws Exception {
        registry.setEventDay("17");
        String eventDay = registry.getEventDay();
        Assert.assertEquals("17", eventDay);
    }

    @Test
    public void testGetEventYear() throws Exception {
        registry.setEventYear("2016");
        String eventYear = registry.getEventYear();
        Assert.assertEquals("2016", eventYear);
    }

    @Test
    public void testGetEventLocation() throws Exception {
        registry.setEventLocation("Alaska");
        String eventLocation = registry.getEventLocation();
        Assert.assertEquals("Alaska", eventLocation);
    }

    @Test
    public void testGetNumberOfGuest() throws Exception {
        registry.setNumberOfGuest("110");
        String numberOfGuest = registry.getNumberOfGuest();
        Assert.assertEquals("110", numberOfGuest);
    }

    @Test
    public void testGetPreferredStoreState() throws Exception {
        registry.setPreferredStoreState("New York");
        String preferredStoreState = registry.getPreferredStoreState();
        Assert.assertEquals("New York", preferredStoreState);
    }

    @Test
    public void testGetPreferredStore() throws Exception {
        registry.setPreferredStore("New York - Herald Square");
        String preferredStore = registry.getPreferredStore();
        Assert.assertEquals("New York - Herald Square", preferredStore);
    }

    @Test
    public void testGetCoRegistrantFirstName() throws Exception {
        registry.setCoRegistrantFirstName("JAMES");
        String coRegistrantFirstName = registry.getCoRegistrantFirstName();
        Assert.assertEquals("JAMES", coRegistrantFirstName);
    }

    @Test
    public void testGetCoRegistrantLastName() throws Exception {
        registry.setCoRegistrantLastName("SMITH");
        String coRegistrantLastName = registry.getCoRegistrantLastName();
        Assert.assertEquals("SMITH", coRegistrantLastName);
    }

    @Test
    public void testGetId() throws Exception {
        registry.setId("1234567");
        String id = registry.getId();
        Assert.assertEquals("1234567", id);
    }
}