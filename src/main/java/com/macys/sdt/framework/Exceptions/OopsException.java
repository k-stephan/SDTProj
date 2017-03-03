package com.macys.sdt.framework.Exceptions;

/**
 * Exception to throw when an "oops" error shows on the page
 */
public class OopsException extends Exception {
    private static final long serialVersionUID = 3719597044976490613L;

    public OopsException(String msg) {
        super(msg);
    }
}
