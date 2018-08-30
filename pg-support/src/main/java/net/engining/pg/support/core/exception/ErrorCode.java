package net.engining.pg.support.core.exception;

import net.engining.pg.support.enums.BaseEnum;

/**
 * @author luxue
 *
 */
public enum ErrorCode implements BaseEnum<ErrorCode, String>
{
	/**
	 * 请求成功
	 */
	Success("0000","Success"),
	
	/**
	 * 未知错误
	 */
	UnknowFail("9999","Failed, Unknown Reason"),
	
	/**
	 * 系统内部错误
	 */
	SystemError("9900","系统内部错误"),
	
	/**
	 * 账户限制
	 */
	Restricted("9800","账户限制"),
	
	/**
	 * 检查错误
	 */
	CheckError("9700","检查错误"),
	
	/**
	 * 请求不合法
	 */
	BadRequest("9600","请求不合法"),
	
	/**
	 * 相关数据为空
	 */
	Null("9100","相关数据为空"),
	
	/**
	 * 其它异常非系统错误
	 */
	Other("9000","其它异常非系统错误"),
	
	;
	
	private final String value;

    private final String label;

    ErrorCode(String value, String label) {
        this.value = value;
        this.label = label;
    }

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getLabel() {
		return label;
	}
}
