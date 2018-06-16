package net.engining.pg.parameter;

import java.text.MessageFormat;
import java.util.Date;

public class ParameterNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String orgId;
	private String paramClass;
	private String paramKey;
	private Date effectiveDate;

	public ParameterNotFoundException(String orgId, String paramClass, String paramKey, Date effectiveDate) {
		super(MessageFormat.format("类型为[{0}]，key为[{1}], 生效日期为[{2}]，机构为[{3}]的参数不存在", paramClass, paramKey, effectiveDate,
				orgId));
		this.orgId = orgId;
		this.paramClass = paramClass;
		this.paramKey = paramKey;
		this.effectiveDate = effectiveDate;
	}

	public ParameterNotFoundException(String paramClass, String paramKey) {
		super(MessageFormat.format("类型为[{0}]，key为[{1}]，任意生效日期的参数不存在", paramClass, paramKey));
		this.paramClass = paramClass;
		this.paramKey = paramKey;
	}

	/**
	 * @return the orgId
	 */
	public String getOrgId() {
		return orgId;
	}

	/**
	 * @param orgId
	 *            the orgId to set
	 */
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getParamClass() {
		return paramClass;
	}

	public void setParamClass(String paramClass) {
		this.paramClass = paramClass;
	}

	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
}
