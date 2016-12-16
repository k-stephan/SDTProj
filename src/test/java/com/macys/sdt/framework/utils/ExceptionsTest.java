package com.macys.sdt.framework.utils;

import org.junit.Test;

public class ExceptionsTest {

    @Test(expected = Exceptions.OopsException.class)
    public void testOopsException() throws Exception {
        throw new Exceptions.OopsException("OopsException");
    }

    @Test(expected = Exceptions.ProductionException.class)
    public void testProductionException() throws Exception {
        throw new Exceptions.ProductionException("ProductionException");
    }

    @Test(expected = Exceptions.SkipException.class)
    public void testSkipException() throws Exception {
        throw new Exceptions.SkipException("SkipException");
    }

    @Test(expected = Exceptions.UserException.class)
    public void testUserException() throws Exception {
        throw new Exceptions.UserException("UserException");
    }

    @Test(expected = Exceptions.EnvException.class)
    public void testEnvException() throws Exception {
        throw new Exceptions.EnvException("EnvException");
    }

    @Test(expected = Exceptions.DataException.class)
    public void testDataException() throws Exception {
        throw new Exceptions.DataException("DataException");
    }

    @Test(expected = Exceptions.TimeoutException.class)
    public void testTimeoutException() throws Exception {
        throw new Exceptions.TimeoutException("TimeoutException");
    }
}