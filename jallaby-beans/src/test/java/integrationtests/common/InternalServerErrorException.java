package integrationtests.common;

public class InternalServerErrorException extends Exception {
	private static final long serialVersionUID = -6540894553468393531L;

	public InternalServerErrorException(String body) {
		super(body);
	}
}
