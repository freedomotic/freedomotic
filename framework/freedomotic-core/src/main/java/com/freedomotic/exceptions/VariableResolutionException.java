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

    public VariableResolutionException() {
    }

    public VariableResolutionException(String msg) {
        super(msg);
    }

    public VariableResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public VariableResolutionException(Throwable cause) {
        super(cause);
    }
}
