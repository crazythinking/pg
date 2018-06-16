package net.engining.pg.support.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 方便写键值对的工具类。可以用 new MapBuilder<K,V>().add(xx, xx).add(xx,xx).build()来生成map
 * @author licj
 *
 */
public class MapBuilder<K, V> {
	private Map<K, V> map = new HashMap<K, V>();
	
	public MapBuilder<K, V> add(K key, V value)
	{
		map.put(key, value);
		return this;
	}
	
	public Map<K, V> build()
	{
		return map;
	}
}
