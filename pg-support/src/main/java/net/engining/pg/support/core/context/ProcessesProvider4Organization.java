package net.engining.pg.support.core.context;

import org.apache.commons.lang3.StringUtils;

/**
 * 为进程提供获取机构信息的能力
 * @author luxue
 *
 */
public class ProcessesProvider4Organization implements Provider4Organization{
	
	private static final String GLOBLE_ORGID = "*";
	private String defaultOrgId = GLOBLE_ORGID;

	/* (non-Javadoc)
	 * @see net.engining.pg.support.core.context.Provider4Organization#getCurrentOrganizationId()
	 */
	@Override
	public String getCurrentOrganizationId() {
		return this.defaultOrgId;
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.core.context.Provider4Organization#setCurrentOrganizationId(java.lang.String)
	 */
	@Override
	public void setCurrentOrganizationId(String orgId) {
		if(StringUtils.isBlank(orgId)){
			this.defaultOrgId = GLOBLE_ORGID;
		}
		else {
			this.defaultOrgId = orgId;
		}
		
	}
	
	
}
