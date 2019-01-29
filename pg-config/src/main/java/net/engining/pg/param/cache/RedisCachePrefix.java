/**
 * 
 */
package net.engining.pg.param.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.cache.CacheKeyPrefix;

/**
 * @author luxue
 *
 */
public class RedisCachePrefix implements CacheKeyPrefix {
	
	private final String delimiter;

	public RedisCachePrefix() {
        this(null);
    }

    public RedisCachePrefix(String delimiter) {
        this.delimiter = delimiter;
    }

	@Override
	public String compute(String cacheName) {
    	String prefix = "";
    	if(this.delimiter != null) {
    		if(StringUtils.isNoneBlank(cacheName)) {
    			prefix = this.delimiter.concat(":").concat(cacheName).concat(":");
    		}
    		else {
    			prefix = this.delimiter.concat(":");
    		}
    	}
    	else {
    		if(StringUtils.isNoneBlank(cacheName)) {
    			prefix = cacheName.concat(":");
    		}
    	}
        return prefix;
    }
}
