package com.macys.sdt.framework.exceptions;

/**
 * Exception for environment errors
 */
public class EnvException extends RuntimeException {
    private static final long serialVersionUID = 3719597044976490617L;

    public EnvException(String msg) {
        super("Error - ENV: " + msg);
    }
}
