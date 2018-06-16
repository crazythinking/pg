/**
 * 
 */
package net.engining.pg.parameter;

/**
 * 为应用提供获取机构信息的能力
 * @author luxue
 *
 */
public interface Provider4Organization {
	
	/**
	 * 获取当前机构号
	 * @return
	 */
	public String getCurrentOrganizationId();
	
	/**
	 * 设置当前机构号
	 * @return
	 */
	public void setCurrentOrganizationId(String orgId);
	
}
