package net.engining.pg.batch.sdk;

/**
 * 
 * @author luxue
 *
 * @param <T>
 */
public class LineItem<T>
{
	private T lineObject;
	
	private boolean valid = false;
	
	private int lineNumber;
	
	public T getLineObject() {
		return lineObject;
	}

	public void setLineObject(T lineObject) {
		this.lineObject = lineObject;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}
