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
public class RepositoryException
        extends FreedomoticException {

    /**
     * Creates a new instance of
     * <code>DaoLayerException</code> without detail message.
     */
    public RepositoryException() {
    }

    /**
     * Constructs an instance of
     * <code>DaoLayerException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RepositoryException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public RepositoryException(Throwable cause) {
        super(cause);
    }
    private static final Logger LOG = Logger.getLogger(RepositoryException.class.getName());
}
