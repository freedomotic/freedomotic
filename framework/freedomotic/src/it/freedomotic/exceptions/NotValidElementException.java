
package it.freedomotic.exceptions;

/**
 *
 * @author enrico
 */


public class NotValidElementException extends Exception {

    /**
     * Creates a new instance of <code>NotAValidElementException</code> without detail message.
     */
    public NotValidElementException() {
    }

    /**
     * Constructs an instance of <code>NotAValidElementException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NotValidElementException(String msg) {
        super(msg);
    }
}
