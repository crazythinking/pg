package net.engining.pg.batch.sdk.test.kreader;

/**
 * 
 * @author luxue
 *
 */
public class BreakDownException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BreakDownException() {
		super("主动断点");
	}
}
