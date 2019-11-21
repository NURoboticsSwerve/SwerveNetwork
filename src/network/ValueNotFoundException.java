package network;

/**
 * Thrown when a value from a {@link NetworkClient} is requested, but no value exists
 * for the name given.
 * 
 * @see NetworkClient
 * 
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class ValueNotFoundException extends RuntimeException {
	ValueNotFoundException(String msg) {
		super(msg);
	}
}
