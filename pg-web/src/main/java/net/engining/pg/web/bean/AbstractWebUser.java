package net.engining.pg.web.bean;

import java.io.Serializable;

/**
 * WebUser抽象类，可用于结合Spring Security等的user对象；各web项目可继承扩展
 * @author luxue
 *
 */
public abstract class AbstractWebUser  implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * 用户序号，系统用户唯一标识；
	 */
	private String userNo;
	
	/**
	 * 客户ID，用于对接客户系统的唯一键
	 */
	private String custId;
	
	/**
	 * Profile ID，用于对接权限系统的唯一键
	 */
	private String puId;
	
	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getPuId() {
		return puId;
	}

	public void setPuId(String puId) {
		this.puId = puId;
	}

}
