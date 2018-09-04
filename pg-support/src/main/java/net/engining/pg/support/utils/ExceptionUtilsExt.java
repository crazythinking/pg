/**
 * 
 */
package net.engining.pg.support.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

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
			t.printStackTrace(printWriter);
			log.error(StringUtils.CR+StringUtils.LF+stringWriter.toString()+StringUtils.CR+StringUtils.LF);
		}
		
	}
	
	public static void main(String[] args) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Exception ex = new NullPointerException();
		ExceptionUtils.printRootCauseStackTrace(ex.getCause(), printWriter);
		System.out.println(StringUtils.CR+StringUtils.LF+stringWriter.toString()+StringUtils.CR+StringUtils.LF);
	}
}
