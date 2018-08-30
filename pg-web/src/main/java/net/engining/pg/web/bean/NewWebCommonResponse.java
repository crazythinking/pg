package net.engining.pg.web.bean;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

import net.engining.pg.support.core.exception.ErrorCode;

/**
 * 重构，支持自定义ResponseHead
 * 通用的Web Response
 * 
 * @author luxue
 *
 */
public class NewWebCommonResponse<H,T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 返回结果码
	 */
	private String statusCode = ErrorCode.Success.getValue();

	/**
	 * 返回结果描述
	 */
	private String statusDesc = ErrorCode.Success.getLabel();
	
	/**
	 * 返回头
	 */
	private H responseHead;

	/**
	 * 返回业务数据
	 */
	private T responseData;
	
	/**
	 * 其他附加信息
	 */
	private Map<String, Serializable> additionalRepMap;
	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public NewWebCommonResponse<H,T> setStatusCode(String statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	/**
	 * @return the statusDesc
	 */
	public String getStatusDesc() {
		return statusDesc;
	}

	/**
	 * @param statusDesc the statusDesc to set
	 */
	public NewWebCommonResponse<H,T> setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
		return this;
	}
	
	/**
	 * @return the responseHead
	 */
	public H getResponseHead() {
		return responseHead;
	}

	/**
	 * @param responseHead the responseHead to set
	 */
	public NewWebCommonResponse<H,T> setResponseHead(H responseHead) {
		this.responseHead = responseHead;
		return this;
	}

	public NewWebCommonResponse() {
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
	public NewWebCommonResponse<H,T> putAdditionalRepMap(String repKey, Serializable repBean) {
		add(repKey, repBean);
		return this;
	}

	private void add(String repKey, Serializable repBean) {
		this.additionalRepMap.put(repKey, repBean);
	}

	/**
	 * @return the responseData
	 */
	public T getResponseData() {
		return responseData;
	}

	/**
	 * @param responseData
	 *            the responseData to set
	 */
	public NewWebCommonResponse<H,T> setResponseData(T responseData) {
		this.responseData = responseData;
		return this;
	}

}
