package net.engining.pg.web;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 具体业务交易的Nested Response Bean的基类
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
	 * 其他附加信息
	 */
	private Map<String, Serializable> additionalRepMap;
	
	public BaseResponseBean() {
		this.additionalRepMap = Maps.newHashMap();
	}

	/**
	 * @return the additionalRepMap
	 */
	public Map<String, Serializable> getAdditionalRepMap() {
		return additionalRepMap;
	}

	/**
	 * @param additionalRepMap
	 *            the additionalRepMap to set
	 */
	public void putAdditionalRepMap(String repKey, Serializable repBean) {
		add(repKey, repBean);
	}

	private void add(String repKey, Serializable repBean) {
		this.additionalRepMap.put(repKey, repBean);
	}
	
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
