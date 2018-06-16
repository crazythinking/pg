package net.engining.pg.support.core.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;

import com.google.common.base.Optional;


public class ErrorMessageException extends NestedRuntimeException{
	
	private static final Logger log = LoggerFactory.getLogger(ErrorMessageException.class);

	private static final long serialVersionUID = 1L;

	private ErrorCode errorCode;
	
	public ErrorMessageException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public ErrorMessageException(ErrorCode errorCode, String message, Throwable cause){
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	
	public void dump(Throwable t){
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		if(Optional.fromNullable(t).isPresent()){
			t.printStackTrace(printWriter);
			if(log.isDebugEnabled()){
				log.debug(StringUtils.CR+StringUtils.LF+stringWriter.toString()+StringUtils.CR+StringUtils.LF);
			}
		}
		
	}
}
