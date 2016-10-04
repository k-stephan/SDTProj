package com.macys.sdt.framework.utils;

/**
 * A Class to contain all our exception types
 */
public class Exceptions {

    /**
     * Exception to throw when an "oops" error shows on the page
     */
    public static class OopsException extends Exception {
        private static final long serialVersionUID = 3719597044976490613L;

        public OopsException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception to throw when a step or operation should not run on production (Ex.: placing an order)
     */
    public static class ProductionException extends Exception {
        private static final long serialVersionUID = 3719597044976490614L;

        public ProductionException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception to throw to skip a step
     */
    public static class SkipException extends Exception {
        private static final long serialVersionUID = 3719597044976490615L;

        public SkipException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception for errors when handling users
     */
    public static class UserException extends Exception {
        private static final long serialVersionUID = 3719597044976490616L;

        public UserException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception for environment errors
     */
    public static class EnvException extends Exception {
        private static final long serialVersionUID = 3719597044976490617L;

        public EnvException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception for data errors
     */
    public static class DataException extends Exception {
        private static final long serialVersionUID = 3719597044976490618L;

        public DataException(String msg) {
            super(msg);
        }
    }

    /**
     * Exception for timeout errors
     */
    public static class TimeoutException extends Exception {
        private static final long serialVersionUID = 3719597044976490619L;

        public TimeoutException(String msg) {
            super(msg);
        }
    }
}
