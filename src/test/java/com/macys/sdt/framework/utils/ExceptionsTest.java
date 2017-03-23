package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.exceptions.*;
import org.junit.Test;

/**
 * Tests for exceptions
 */
public class ExceptionsTest {

    @Test(expected = OopsException.class)
    public void testOopsException() throws Exception {
        throw new OopsException("OopsException");
    }

    @Test(expected = ProductionException.class)
    public void testProductionException() throws Exception {
        throw new ProductionException("ProductionException");
    }

    @Test(expected = SkipException.class)
    public void testSkipException() throws Exception {
        throw new SkipException("SkipException");
    }

    @Test(expected = UserException.class)
    public void testUserException() throws Exception {
        throw new UserException("UserException");
    }

    @Test(expected = EnvException.class)
    public void testEnvException() throws Exception {
        throw new EnvException("EnvException");
    }

    @Test(expected = DataException.class)
    public void testDataException() throws Exception {
        throw new DataException("DataException");
    }

    @Test(expected = TimeoutException.class)
    public void testTimeoutException() throws Exception {
        throw new TimeoutException("TimeoutException");
    }
}