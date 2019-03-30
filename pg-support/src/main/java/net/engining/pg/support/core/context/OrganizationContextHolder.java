package net.engining.pg.support.core.context;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import javax.persistence.EntityManager;

/**
 * 通过{@link ThreadContextHolder}提供PCX需要的线程上下文工具类，提供oranizationId和username的便捷方法。
 * 
 * @author zhangkun
 *
 */
public abstract class OrganizationContextHolder {

	private static final String ORGANIZATION_NAME = "net.engining.organization.id";
	private static final String GLOBLE_ORGID = "*";

	/**
	 * 取得当前线程的organizationId
	 * 
	 * @return 当前线程的机构号
	 */
	public static String getCurrentOrganizationId() {
		String orgId = (String) ThreadContextHolder.getObject(ORGANIZATION_NAME);
		if(StringUtils.isBlank(orgId)){
			orgId = GLOBLE_ORGID;
		}
		return orgId;
	}
	
	/**
	 * 设置当前线程的organizationId
	 * 
	 * @param orgId 机构号
	 */
	public static void setCurrentOrganizationId(String orgId) {
		if(StringUtils.isBlank(orgId)){
			ThreadContextHolder.setObject(ORGANIZATION_NAME, GLOBLE_ORGID);
		}
		else {
			ThreadContextHolder.setObject(ORGANIZATION_NAME, orgId);
		}
		
	}
	
	public static void enableOrgFilter(EntityManager em)
	{
		em.unwrap(Session.class).enableFilter("org").setParameter("org", getCurrentOrganizationId());
	}
	
	public static void disableOrgFilter(EntityManager em)
	{
		em.unwrap(Session.class).disableFilter("org");
	}
}
