package net.engining.pg.web.bean;

import java.io.Serializable;

/**
 * 用于接受/login 的登录用户对象；结合Spring security，从request中获取
 * @author luxue
 *
 */
public class WebLoginUser  implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * 登录Id；
	 */
	private String loginId;
	
	/**
	 * 登录密码
	 */
	private String password;

	/**
	 * @return the loginId
	 */
	public String getLoginId() {
		return loginId;
	}

	/**
	 * @param loginId the loginId to set
	 */
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	

}
