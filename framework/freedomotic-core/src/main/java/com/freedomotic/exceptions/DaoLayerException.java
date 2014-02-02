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
public class DaoLayerException
        extends FreedomoticException {

    /**
     * Creates a new instance of
     * <code>DaoLayerException</code> without detail message.
     */
    public DaoLayerException() {
    }

    /**
     * Constructs an instance of
     * <code>DaoLayerException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DaoLayerException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public DaoLayerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public DaoLayerException(Throwable cause) {
        super(cause);
    }
    private static final Logger LOG = Logger.getLogger(DaoLayerException.class.getName());
}
