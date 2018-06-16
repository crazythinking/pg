package net.engining.pg.support.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储消息用的对象
 * 
 * @author zhangkun
 * 
 */
@Deprecated
@SuppressWarnings("serial")
public class YakMessage implements Serializable {
	
	/**
	 * 在customAttributes中保存MAK，密钥重置用
	 */
	public static final String MAC_KEY_NAME = "com.allinfinance.hsp.mac.key";
	
	/**
	 * 在customAttributes中保存关键域信息，验证响应消息位图，TPS规则引擎用
	 */
	public static final String ROUTE_KEY_NAME = "com.allinfinance.tps.route.key";

	/**
	 * 在customAttributes中保存响应消息位图，验证响应消息位图，TPS规则引擎用
	 */
	public static final String RSP_FIELDS_NAME = "com.allinfinance.tps.response.fields";

	private byte[] rawMessage;
	
	private Boolean isHeartBeat = false;
	
	private String srcChannelId;
	
	private Boolean isRequest = false;
	
	private Boolean isInComing = false;

	private Map<Integer, String> headAttributes = new HashMap<Integer, String>();

	private Map<Integer, String> bodyAttributes = new HashMap<Integer, String>();

	private Map<String, Serializable> customAttributes = new HashMap<String, Serializable>();

	/**
	 * 
	 * @return 消息原文
	 */
	public byte[] getRawMessage() {
		return rawMessage;
	}

	/**
	 * 
	 * @param rawMessage
	 *            消息原文
	 */
	public void setRawMessage(byte[] rawMessage) {
		this.rawMessage = rawMessage;
	}

	/**
	 * 
	 * @return 是否心跳报文
	 */
	public Boolean getIsHeartBeat() {
		return isHeartBeat;
	}

	/**
	 * 
	 * @param isHeartBeat 是否心跳报文
	 */
	public void setIsHeartBeat(Boolean isHeartBeat) {
		this.isHeartBeat = isHeartBeat;
	}

	/**
	 * 
	 * @return 来源渠道号
	 */
	public String getSrcChannelId() {
		return srcChannelId;
	}

	/**
	 * 
	 * @param srcChannelId 来源渠道号
	 */
	public void setSrcChannelId(String srcChannelId) {
		this.srcChannelId = srcChannelId;
	}

	/**
	 * 
	 * @return 请求/响应标识
	 */
	public Boolean getIsRequest() {
		return isRequest;
	}

	/**
	 * 
	 * @param isRequest 请求/响应标识
	 */
	public void setIsRequest(Boolean isRequest) {
		this.isRequest = isRequest;
	}

	/**
	 * 
	 * @return 接入/发出标识
	 */
	public Boolean getIsInComing() {
		return isInComing;
	}

	/**
	 * 
	 * @param isInComing 接入/发出标识
	 */
	public void setIsInComing(Boolean isInComing) {
		this.isInComing = isInComing;
	}
	
	/**
	 * 
	 * @return 消息头map
	 */
	public Map<Integer, String> getHeadAttributes() {
		return headAttributes;
	}

	/**
	 * 
	 * @param headAttributes
	 *            消息头map
	 */
	public void setHeadAttributes(Map<Integer, String> headAttributes) {
		this.headAttributes = headAttributes;
	}

	/**
	 * 
	 * @return 消息内容map
	 */
	public Map<Integer, String> getBodyAttributes() {
		return bodyAttributes;
	}

	/**
	 * 
	 * @param bodyAttributes
	 *            消息内容map
	 */
	public void setBodyAttributes(Map<Integer, String> bodyAttributes) {
		this.bodyAttributes = bodyAttributes;
	}

	/**
	 * @return 自定义内容map
	 */
	public Map<String, Serializable> getCustomAttributes() {
		return customAttributes;
	}

	/**
	 * 
	 * @param customAttributes
	 *            自定义内容map
	 */
	public void setCustomAttributes(Map<String, Serializable> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public String getBody(int field)
	{
		return  bodyAttributes.get(field);
	}
}
