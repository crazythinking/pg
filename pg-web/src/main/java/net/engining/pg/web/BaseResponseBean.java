/**
 * 
 */
package net.engining.pg.web;

import java.io.Serializable;

/**
 * 具体业务交易的Response Bean的基类
 * @author luxue
 *
 */
public class BaseResponseBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 返回结果码
	 */
	private String returnCode;
	
	/**
	 * 返回结果描述
	 */
	private String returnDesc;

	/**
	 * @return the returnCode
	 */
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode the returnCode to set
	 */
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return the returnDesc
	 */
	public String getReturnDesc() {
		return returnDesc;
	}

	/**
	 * @param returnDesc the returnDesc to set
	 */
	public void setReturnDesc(String returnDesc) {
		this.returnDesc = returnDesc;
	}
	

}
