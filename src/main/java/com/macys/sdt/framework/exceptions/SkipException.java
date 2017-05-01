package com.macys.sdt.framework.exceptions;

/**
 * Exception to throw to skip a step
 */
public class SkipException extends RuntimeException {
    private static final long serialVersionUID = 3719597044976490615L;

    public SkipException(String msg) {
        super("Error: Skipped step: " + msg);
    }
}
