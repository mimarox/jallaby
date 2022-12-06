package org.jallaby.event;

public class InvalidEventException extends RuntimeException {
	private static final long serialVersionUID = -2420344981171211360L;
	
	public InvalidEventException(final String message) {
		super(message);
	}
}
