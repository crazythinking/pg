package net.engining.pg.parameter;

import org.springframework.context.ApplicationEvent;

/**
 * 参数修改事件，对参数的增、改、删都会触发此事件。<br/>事件中包含参数的机构号、参数类型、参数主键，可唯一标识到一个参数。
 * 
 * @author zhangkun
 *
 */
public class ParameterChangedEvent extends ApplicationEvent{
	
	private static final long serialVersionUID = 1L;


	public ParameterChangedEvent(Object source, String orgId, String paramClass, String key) {
		super(source);
		this.orgId = orgId;
		this.paramClass = paramClass;
		this.key = key;
	}

	private String orgId;
	private String paramClass;
	private String key;

	
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

}
