package net.engining.pg.web.bean;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotBlank;

import net.engining.pg.web.AsynInd;

/**
 * 默认Request Header
 * @author luxue
 *
 */

public class DefaultRequestHeader implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 服务提供系统标识
	 */
	@NotBlank
	private String svPrId;

	/**
	 * 渠道Id（请求系统标识）
	 */
	@NotBlank
	private String channelId;

	/**
	 * 渠道签名
	 */
	private String channelSign;

	/**
	 * 清算日期（业务日期）
	 */
	private Date clearingDate;

	/**
	 * 请求交易流水号
	 */
	@NotBlank
	private String txnSerialNo;

	/**
	 * 交易请求时间
	 */
	private Date timestamp;

	/**
	 * 异步接口标识
	 */
	@NotBlank
	private AsynInd asynInd;

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
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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

}
