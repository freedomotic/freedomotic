package com.freedomotic.exceptions;

/**
 * It is an unchecked exception type for Freedomotic
 * @author P3trur0 https://flatmap.it
 *
 */
public class FreedomoticRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public FreedomoticRuntimeException(String message) {
		super("Freedomotic Runtime error >> "+message);
	}

}
