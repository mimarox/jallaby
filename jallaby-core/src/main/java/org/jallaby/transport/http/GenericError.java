package org.jallaby.transport.http;

public class GenericError {
	private final String exceptionName;
	private final String exceptionMessage;
	
	public GenericError(final String exceptionName, final String exceptionMessage) {
		this.exceptionName = exceptionName;
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * @return the exceptionName
	 */
	public String getExceptionName() {
		return exceptionName;
	}

	/**
	 * @return the exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}	
}
