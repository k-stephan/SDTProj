package com.macys.sdt.framework.Exceptions;

/**
 * Exception to throw to skip a step
 */
public class SkipException extends Exception {
    private static final long serialVersionUID = 3719597044976490615L;

    public SkipException(String msg) {
        super(msg);
    }
}
