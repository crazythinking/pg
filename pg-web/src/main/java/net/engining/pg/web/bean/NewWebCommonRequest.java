package net.engining.pg.web.bean;

import java.io.Serializable;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Maps;

/**
 * 重构，支持自定义RequestHead
 * 通用的Web Request
 * 
 * @author luxue
 *
 */
public class NewWebCommonRequest<H,T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 请求头
	 */
	@NotNull
	private H requestHead;
	
	/**
	 * 请求业务数据
	 */
	@NotNull
	private T requestData;

	/**
	 * 其他附加信息
	 */
	private Map<String, Serializable> additionalReqMap;

	public NewWebCommonRequest() {
		this.additionalReqMap = Maps.newHashMap();
	}

	/**
	 * @return the requestHead
	 */
	public H getRequestHead() {
		return requestHead;
	}

	/**
	 * @param requestHead the requestHead to set
	 */
	public NewWebCommonRequest<H,T> setRequestHead(H requestHead) {
		this.requestHead = requestHead;
		return this;
	}

	/**
	 * @return the requestData
	 */
	public T getRequestData() {
		return requestData;
	}

	/**
	 * @param requestData
	 *            the requestData to set
	 */
	public NewWebCommonRequest<H,T> setRequestData(T requestData) {
		this.requestData = requestData;
		return this;
	}

	/**
	 * @return the additionalReqMap
	 */
	public Map<String, Serializable> getAdditionalReqMap() {
		return additionalReqMap;
	}

	public NewWebCommonRequest<H,T> putAdditionalRepMap(String reqKey, Serializable reqBean) {
		add(reqKey, reqBean);
		return this;
	}

	private void add(String reqKey, Serializable reqBean) {
		this.additionalReqMap.put(reqKey, reqBean);
	}

}
