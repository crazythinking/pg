package net.engining.pg.web;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 通用的Web Response
 * @author luxue
 *
 */
public class WebCommonResquest<T> implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 服务提供系统标识
	 */
	private String svPrId;
	
	/**
	 * 渠道Id（请求系统标识）
	 */
	private String channelId;
	
	/**
	 * 渠道签名
	 */
	private String channelSign;
	
	/**
	 * 清算日期
	 */
	private Date clearingDate;
	
	/**
	 * 请求交易流水号
	 */
	private String txnSerialNo;
	
	/**
	 * 交易日期
	 */
	private Date txnDate;
	
	/**
	 * 交易时间
	 */
	private Date txnTime;
	
	/**
	 * 异步接口标识
	 */
	private AsynInd asynInd;
	
	/**
	 * 请求业务数据
	 */
	private T requestData;
	
	/**
	 * 其他附加信息
	 */
	private Map<String, Serializable> additionalReqMap;

	/**
	 * @return the svPrId
	 */
	public String getSvPrId() {
		return svPrId;
	}

	/**
	 * @param svPrId the svPrId to set
	 */
	public void setSvPrId(String svPrId) {
		this.svPrId = svPrId;
	}

	/**
	 * @return the channelId
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * @param channelId the channelId to set
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/**
	 * @return the channelSign
	 */
	public String getChannelSign() {
		return channelSign;
	}

	/**
	 * @param channelSign the channelSign to set
	 */
	public void setChannelSign(String channelSign) {
		this.channelSign = channelSign;
	}

	/**
	 * @return the clearingDate
	 */
	public Date getClearingDate() {
		return clearingDate;
	}

	/**
	 * @param clearingDate the clearingDate to set
	 */
	public void setClearingDate(Date clearingDate) {
		this.clearingDate = clearingDate;
	}

	/**
	 * @return the txnSerialNo
	 */
	public String getTxnSerialNo() {
		return txnSerialNo;
	}

	/**
	 * @param txnSerialNo the txnSerialNo to set
	 */
	public void setTxnSerialNo(String txnSerialNo) {
		this.txnSerialNo = txnSerialNo;
	}

	/**
	 * @return the txnDate
	 */
	public Date getTxnDate() {
		return txnDate;
	}

	/**
	 * @param txnDate the txnDate to set
	 */
	public void setTxnDate(Date txnDate) {
		this.txnDate = txnDate;
	}

	/**
	 * @return the txnTime
	 */
	public Date getTxnTime() {
		return txnTime;
	}

	/**
	 * @param txnTime the txnTime to set
	 */
	public void setTxnTime(Date txnTime) {
		this.txnTime = txnTime;
	}

	/**
	 * @return the asynInd
	 */
	public AsynInd getAsynInd() {
		return asynInd;
	}

	/**
	 * @param asynInd the asynInd to set
	 */
	public void setAsynInd(AsynInd asynInd) {
		this.asynInd = asynInd;
	}

	/**
	 * @return the requestData
	 */
	public T getRequestData() {
		return requestData;
	}

	/**
	 * @param requestData the requestData to set
	 */
	public void setRequestData(T requestData) {
		this.requestData = requestData;
	}

	/**
	 * @return the additionalReqMap
	 */
	public Map<String, Serializable> getAdditionalReqMap() {
		return additionalReqMap;
	}

	/**
	 * @param additionalReqMap the additionalReqMap to set
	 */
	public void setAdditionalReqMap(Map<String, Serializable> additionalReqMap) {
		this.additionalReqMap = additionalReqMap;
	}
	
}
