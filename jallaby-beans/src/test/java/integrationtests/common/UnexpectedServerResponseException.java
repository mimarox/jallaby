package integrationtests.common;

public class UnexpectedServerResponseException extends RuntimeException {
	private static final long serialVersionUID = -5230841427363698079L;

	public UnexpectedServerResponseException(final String body) {
		super(body);
	}
}
