package net.engining.pg.parameter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.engining.pg.support.db.querydsl.FetchResponse;
import net.engining.pg.support.db.querydsl.Range;

public class ParameterFetchResponseBuilder<T> {
	
	private ParameterFacility parameterFacility;
	
	private Range range;
	
	public ParameterFetchResponseBuilder(ParameterFacility parameterFacility)
	{
		this.parameterFacility = parameterFacility;
	}
	
	public ParameterFetchResponseBuilder<T> range(Range range)
	{
		this.range = range;
		return this;
	}
	
	public FetchResponse<T> build(Class<T> parameterClass)
	{
		Map<String, T> map = parameterFacility.getParameterMap(parameterClass);
		
		FetchResponse<T> fr = new FetchResponse<T>();
		fr.setRowCount(map.size());

		int i = 0;
		List<T> data = Lists.newArrayList();
		for (Entry<String, T> entry : map.entrySet())
		{
			if (range == null || 
				(i >= range.getStart() && i < range.getStart() + range.getLength()))
			{
				data.add(entry.getValue());
			}
			i++;
		}
		
		fr.setData(data);
		
		return fr;
	}
	
	public FetchResponse<T> build(Class<T> parameterClass, ParameterFilterCallback<T> filterCallback){
		
		Map<String, T> map = parameterFacility.getParameterMap(parameterClass);
		map = filterCallback.filter(map);
		
		FetchResponse<T> fr = new FetchResponse<T>();
		fr.setRowCount(map.size());

		int i = 0;
		List<T> data = Lists.newArrayList();
		for (Entry<String, T> entry : map.entrySet())
		{
			if (range == null || 
				(i >= range.getStart() && i < range.getStart() + range.getLength()))
			{
				data.add(entry.getValue());
			}
			i++;
		}
		
		fr.setData(data);
		
		return fr;
	}

}
