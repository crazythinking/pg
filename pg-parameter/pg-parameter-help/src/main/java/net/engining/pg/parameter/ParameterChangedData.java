package net.engining.pg.parameter;

import java.io.Serializable;
import java.util.UUID;

/**
 * 参数刷新相关数据，可用于amqp传递 
 * @author binarier
 *
 */
public class ParameterChangedData implements Serializable{

	private static final long serialVersionUID = 3504925084744780510L;
	private String orgId;
	private String paramClass;
	private String key;
	
	/**
	 * 用于在广播时标识自身
	 */
	private UUID source;

	/**
	 * 
	 * @return 机构号，全局参数用*代替
	 */
	public String getOrgId() {
		return orgId;
	}

	/**
	 * 
	 * @param orgId 机构号，全局参数用*代替
	 */
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	/**
	 * 
	 * @return 参数对象类全名（包含类路径）
	 */
	public String getParamClass() {
		return paramClass;
	}

	/**
	 * 
	 * @param paramClass 参数对象类全名（包含类路径）
	 */
	public void setParamClass(String paramClass) {
		this.paramClass = paramClass;
	}

	/**
	 * 
	 * @return 参数主键
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 
	 * @param key 参数主键
	 */
	public void setKey(String key) {
		this.key = key;
	}

	public UUID getSource() {
		return source;
	}

	public void setSource(UUID source) {
		this.source = source;
	}

}
