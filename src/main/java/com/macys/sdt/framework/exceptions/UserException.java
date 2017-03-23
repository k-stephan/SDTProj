package com.macys.sdt.framework.exceptions;

/**
 * Exception for errors when handling users
 */
public class UserException extends Exception {
    private static final long serialVersionUID = 3719597044976490616L;

    public UserException(String msg) {
        super("Error: User State: " + msg);
    }
}
