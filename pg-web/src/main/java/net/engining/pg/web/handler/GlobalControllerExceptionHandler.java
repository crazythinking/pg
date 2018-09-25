package net.engining.pg.web.handler;

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

import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;
import net.engining.pg.support.utils.ExceptionUtilsExt;
import net.engining.pg.web.WebCommonResponse;
import net.engining.pg.web.WebCommonResponseBuilder;

/**
 * 
 * Controller全局异常处理
 * @author luxue
 *
 */
@Deprecated
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
		log.error("ERROR_CODE:{}, ERROR_DESC:{}, have exceptions as flowing: ",ex.getErrorCode().getValue(), ex.getErrorCode().getLabel());
		ExceptionUtilsExt.dump(ex);
		return setupReturn(ex.getErrorCode().getValue(), ex.getErrorCode().getLabel()+" : "+ex.getMessage());
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
		log.error("ERROR_CODE:{}, ERROR_DESC:{}, have exceptions as flowing: ",ErrorCode.SystemError.getValue(), ErrorCode.SystemError.getLabel());
		ExceptionUtilsExt.dump(ex);
		return setupReturn(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage());
	}

	private <T> WebCommonResponse<T> setupReturn(String errorCode, String msg) {
		if(StringUtils.isBlank(msg)){
			msg = ErrorCode.UnknowFail.getLabel();
		}
		return new WebCommonResponseBuilder<T>().build()
				.setStatusCode(errorCode)
				.setStatusDesc(msg);
	}
	
}