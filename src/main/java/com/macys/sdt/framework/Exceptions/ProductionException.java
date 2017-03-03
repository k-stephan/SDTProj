package com.macys.sdt.framework.Exceptions;

/**
 * Exception to throw when a step or operation should not run on production (Ex.: placing an order)
 */
public class ProductionException extends Exception {
    private static final long serialVersionUID = 3719597044976490614L;

    public ProductionException(String msg) {
        super(msg);
    }
}
