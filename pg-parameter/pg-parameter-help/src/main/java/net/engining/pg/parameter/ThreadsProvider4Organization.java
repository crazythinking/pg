/**
 * 
 */
package net.engining.pg.parameter;

/**
 * 为线程提供获取机构信息的能力
 * @author luxue
 *
 */
public class ThreadsProvider4Organization implements Provider4Organization{


	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.Provider4Organization#setCurrentOrganizationId(java.lang.String)
	 */
	@Override
	public void setCurrentOrganizationId(String orgId) {
		OrganizationContextHolder.setCurrentOrganizationId(orgId);
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.Provider4Organization#getCurrentOrganizationId()
	 */
	@Override
	public String getCurrentOrganizationId() {
		return OrganizationContextHolder.getCurrentOrganizationId();
	}
	
}
