package net.engining.pg.web.handler;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.google.common.base.Optional;

import net.engining.pg.support.core.exception.ErrorMessageException;
import net.engining.pg.web.WebCommonResponse;
import net.engining.pg.web.WebCommonResponseBuilder;

/**
 * FIXME 不够通用，需重构
 * Controller全局异常处理
 * @author luxue
 *
 */
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@SuppressWarnings("rawtypes")
	@ExceptionHandler(value = { ConstraintViolationException.class })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public <T> WebCommonResponse<T> constraintViolationException(ConstraintViolationException ex) {
		WebCommonResponse<T> rsp = setupReturn(HttpStatus.BAD_REQUEST.toString(), "请求参数不符合规范！");
		for(ConstraintViolation constraintViolation : ex.getConstraintViolations()){
			String propName = constraintViolation.getPropertyPath().toString();
			rsp.putAdditionalRepMap(propName, constraintViolation.getMessage());
		}
		return rsp;
	}
	
	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public <T> WebCommonResponse<T> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
		WebCommonResponse<T> rsp = setupReturn(HttpStatus.BAD_REQUEST.toString(), "请求参数不符合规范！");
		BindingResult bindingResult = ex.getBindingResult();
		for(FieldError fieldError : bindingResult.getFieldErrors()){
			rsp.putAdditionalRepMap(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return rsp;
	}

	@ExceptionHandler(value = { ErrorMessageException.class })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public <T> WebCommonResponse<T> IllegalArgumentException(ErrorMessageException ex) {
		return setupReturn(ex.getErrorCode().toString(), ex.getMessage());
	}

	@ExceptionHandler(value = { NoHandlerFoundException.class })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public <T> WebCommonResponse<T> noHandlerFoundException(NoHandlerFoundException ex) {
		return setupReturn(HttpStatus.NOT_FOUND.toString(), ex.getMessage());
	}

	@ExceptionHandler(value = { Exception.class })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public <T> WebCommonResponse<T> unknownException(Exception ex) {
		//不可预料的异常，需要打印错误堆栈
		dump(ex.getCause());
		return setupReturn(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage());
	}

	private <T> WebCommonResponse<T> setupReturn(String errorCode, String msg) {
		if(StringUtils.isBlank(msg)){
			msg = WebCommonResponse.DESC_UNKNOW_FAIL;
		}
		return new WebCommonResponseBuilder<T>().build()
				.setStatusCode(errorCode)
				.setStatusDesc(msg);
	}
	
	private void dump(Throwable t){
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