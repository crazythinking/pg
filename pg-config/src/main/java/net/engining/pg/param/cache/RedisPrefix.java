/**
 * 
 */
package net.engining.pg.param.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author luxue
 *
 */
public class RedisPrefix implements RedisCachePrefix {
	
	@SuppressWarnings("rawtypes")
	private final RedisSerializer serializer;
    private final String delimiter;

    public RedisPrefix() {
        this(null);
    }

    public RedisPrefix(String delimiter) {
        this.serializer = new StringRedisSerializer();
        this.delimiter = delimiter;
    }

    @SuppressWarnings("unchecked")
	@Override
    public byte[] prefix(String cacheName) {
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
        return this.serializer.serialize(prefix);
    }
}
