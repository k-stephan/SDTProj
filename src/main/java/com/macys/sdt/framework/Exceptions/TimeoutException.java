package com.macys.sdt.framework.Exceptions;

/**
 * Exception for timeout errors
 */
public class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = 3719597044976490619L;

    public TimeoutException(String msg) {
        super("ERROR - TIMEOUT: " + msg);
    }
}
