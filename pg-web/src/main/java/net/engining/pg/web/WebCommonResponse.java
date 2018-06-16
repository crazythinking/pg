package net.engining.pg.web;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;

import com.google.common.collect.Maps;

/**
 * FIXME 重构，定义header
 * 通用的Web Response
 * 
 * @author luxue
 *
 */
public class WebCommonResponse<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	// TODO 需要定义一套统一的返回码枚举类
	public final static String CODE_OK = "0000";
	public final static String DESC_SUCCESS = "Success";
	public final static String CODE_UNKNOW_FAIL = "9999";
	public final static String DESC_UNKNOW_FAIL = "Failed, Unknown Reason";

	/**
	 * 返回结果码
	 */
	private String statusCode;

	/**
	 * 返回结果描述
	 */
	private String statusDesc;

	/**
	 * 请求交易流水号
	 */
	private String txnSerialNo;

	/**
	 * 服务提供方返回流水号
	 */
	private String svPrSerialNo;
	
	/**
	 * 交易返回时间
	 */
	private Date timestamp;

	/**
	 * 返回业务数据
	 */
	private T responseData;

	/**
	 * 其他附加信息
	 */
	private Map<String, Serializable> additionalRepMap;

	public WebCommonResponse() {
		this.additionalRepMap = Maps.newHashMap();
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public WebCommonResponse<T> setTimestamp() {
		this.timestamp = LocalDate.now().toDate();
		return this;
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
	public WebCommonResponse<T> putAdditionalRepMap(String repKey, Serializable repBean) {
		add(repKey, repBean);
		return this;
	}

	private void add(String repKey, Serializable repBean) {

		this.additionalRepMap.put(repKey, repBean);

	}

	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public WebCommonResponse<T> setStatusCode(String statusCode) {
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
	 * @param statusDesc
	 *            the statusDesc to set
	 */
	public WebCommonResponse<T> setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
		return this;
	}

	/**
	 * @return the txnSerialNo
	 */
	public String getTxnSerialNo() {
		return txnSerialNo;
	}

	/**
	 * @param txnSerialNo
	 *            the txnSerialNo to set
	 */
	public WebCommonResponse<T> setTxnSerialNo(String txnSerialNo) {
		this.txnSerialNo = txnSerialNo;
		return this;
	}

	/**
	 * @return the svPrSerialNo
	 */
	public String getSvPrSerialNo() {
		return svPrSerialNo;
	}

	/**
	 * @param svPrSerialNo
	 *            the svPrSerialNo to set
	 */
	public WebCommonResponse<T> setSvPrSerialNo(String svPrSerialNo) {
		this.svPrSerialNo = svPrSerialNo;
		return this;
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
	public WebCommonResponse<T> setResponseData(T responseData) {
		this.responseData = responseData;
		return this;
	}

}
