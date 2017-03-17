package com.macys.sdt.framework.Exceptions;

/**
 * Exception for data errors
 */
public class DataException extends RuntimeException {
    private static final long serialVersionUID = 3719597044976490618L;

    public DataException(String msg) {
        super("ERROR - DATA: " + msg);
    }
}
