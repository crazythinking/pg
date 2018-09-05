package net.engining.pg.support.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;

/**
 * @author luxue
 *
 */
public class ExceptionUtilsExt extends ExceptionUtils{
	
	private static final Logger log = LoggerFactory.getLogger(ExceptionUtilsExt.class);

	public static void dump(Throwable t){
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		if(Optional.fromNullable(t).isPresent()){
//			t.printStackTrace(printWriter);
			printRootCauseStackTrace(t, printWriter);
			log.error(StringUtils.CR+StringUtils.LF+stringWriter.toString()+StringUtils.CR+StringUtils.LF);
		}
		
	}
	
	public static void main(String[] args) {
		Exception ex = new ErrorMessageException(ErrorCode.BadRequest, "exception test####################");
//		ExceptionUtils.printRootCauseStackTrace(ex, printWriter);
//		System.out.println(StringUtils.CR+StringUtils.LF+stringWriter.toString()+StringUtils.CR+StringUtils.LF);
		ExceptionUtilsExt.dump(ex);
	}
}
