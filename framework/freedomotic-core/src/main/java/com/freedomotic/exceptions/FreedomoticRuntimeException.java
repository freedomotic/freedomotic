package com.freedomotic.exceptions;

/**
 * It is an unchecked exception type for Freedomotic.
 * @author P3trur0 https://flatmap.it
 *
 */
public class FreedomoticRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a Freedomotic specific runtime exception.
	 * @param message details of the exception 
	 */
	public FreedomoticRuntimeException(String message) {
		super("Freedomotic Runtime error >> "+message);
	}
	
    /**
     * Builds a Freedomotic specific runtime exception.
     * @param message details of the exception
     * @param cause root cause of the exception
     */
    public FreedomoticRuntimeException(String message, Throwable cause) {
        super("Freedomotic Runtime error >> " + message, cause);
    }

}
