package net.engining.pg.support.utils;

import java.io.Serializable;
import java.util.Map;

/**
 * 指定model可以与Map互转
 * @author licj
 *
 */
public interface HasMapping {
	Map<String, Serializable> convertToMap();
	
	void updateFromMap(Map<String, Serializable> map);
}
