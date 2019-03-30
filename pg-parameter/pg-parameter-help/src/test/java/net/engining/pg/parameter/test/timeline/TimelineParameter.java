package net.engining.pg.parameter.test.timeline;

import net.engining.pg.parameter.HasEffectiveDate;
import net.engining.pg.parameter.test.cache.InnerParameter;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public class TimelineParameter implements HasEffectiveDate {

	public DateTime effectiveDate;
	
	public String data;
	
	public List<InnerParameter> paramList;
	
	public TimelineParameter(String data, DateTime effectiveDate)
	{
		this.effectiveDate = effectiveDate;
		this.data = data;
	}
	
	@Override
	public Date getEffectiveDate()
	{
		return effectiveDate.toDate();
	}

	@Override
	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = new DateTime(effectiveDate);
	}


}
