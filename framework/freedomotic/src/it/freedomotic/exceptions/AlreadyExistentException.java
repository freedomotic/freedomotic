
package it.freedomotic.exceptions;

/**
 *
 * @author enrico
 */


public class AlreadyExistentException extends Exception {

    /**
     * Creates a new instance of <code>AlreadyExistentException</code> without detail message.
     */
    public AlreadyExistentException() {
    }

    /**
     * Constructs an instance of <code>AlreadyExistentException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AlreadyExistentException(String msg) {
        super(msg);
    }
}
