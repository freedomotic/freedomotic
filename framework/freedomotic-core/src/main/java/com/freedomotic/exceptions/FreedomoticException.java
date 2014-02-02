/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.exceptions;

import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class FreedomoticException
        extends Exception {

    /**
     * Creates a new instance of
     * <code>FreedomoticException</code> without detail message.
     */
    public FreedomoticException() {
    }

    /**
     * Constructs an instance of
     * <code>FreedomoticException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public FreedomoticException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public FreedomoticException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public FreedomoticException(Throwable cause) {
        super(cause);
    }
    private static final Logger LOG = Logger.getLogger(FreedomoticException.class.getName());
}
