package com.macys.sdt.framework.Exceptions;

/**
 * Exception thrown when driver is requested but not initialized
 */
public class DriverNotInitializedException extends Exception {

    private static final long serialVersionUID = 3719597044976490620L;

    public DriverNotInitializedException(String msg) {
        super("ERROR - APP: " + msg);
    }
}
