/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.exceptions;

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

    public DaoLayerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoLayerException(Throwable cause) {
        super(cause);
    }
}
