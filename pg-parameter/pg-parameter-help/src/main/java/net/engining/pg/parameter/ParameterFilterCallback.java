package net.engining.pg.parameter;

import java.util.Map;

/**
 * 针对ParameterFetchResponseBuilder查询过滤的回调接口
 * @author luxue
 *
 */
public interface ParameterFilterCallback<T> {

	public Map<String, T> filter(Map<String, T> map);
}
