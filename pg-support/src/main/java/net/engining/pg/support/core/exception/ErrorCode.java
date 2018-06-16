/**
 * 
 */
package net.engining.pg.support.core.exception;

/**
 * @author luxue
 *
 */
public enum ErrorCode
{
	/**
	 * 请求不合法
	 */
	BadRequest,
	
	/**
	 * 系统内部错误
	 */
	SystemError,
	
	/**
	 * 账户限制
	 */
	Restricted,
	
	/**
	 * 其它错误
	 */
	Other,
	/**
	 * 数据为空
	 */
	Null,
	
	/**
	 * 检查错误
	 */
	CheckError
}
