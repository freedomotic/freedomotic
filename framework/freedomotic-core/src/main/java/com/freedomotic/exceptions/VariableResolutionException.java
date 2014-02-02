/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.exceptions;

/**
 *
 * @author nicoletti
 */
public class VariableResolutionException extends FreedomoticException {

    /**
     *
     */
    public VariableResolutionException() {
    }

    /**
     *
     * @param msg
     */
    public VariableResolutionException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public VariableResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause
     */
    public VariableResolutionException(Throwable cause) {
        super(cause);
    }
}
