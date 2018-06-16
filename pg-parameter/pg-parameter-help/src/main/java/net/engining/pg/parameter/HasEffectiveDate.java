package net.engining.pg.parameter;

import java.util.Date;

/**
 * 参数支持生效日期字段
 * @author binarier
 *
 */
public interface HasEffectiveDate {

	Date getEffectiveDate();
	
	void setEffectiveDate(Date effectiveDate);
}
