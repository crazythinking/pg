package net.engining.pg.param.cache;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

/**
 * 只用于Parameter的Redis Cache Manager
 * @author luxue
 *
 */
public class ExtParameterRedisCacheManager extends RedisCacheManager {
	
	private RedisCacheWriter cacheWriter;
	private RedisCacheConfiguration defaultCacheConfig;

	/**
	 * @param cacheWriter
	 * @param defaultCacheConfiguration
	 */
	public ExtParameterRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
		super(cacheWriter, defaultCacheConfiguration);
		this.cacheWriter = cacheWriter;
		this.defaultCacheConfig = defaultCacheConfiguration;
	}

	@Override
	protected ExtRedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
		return new ExtRedisCache(name, this.cacheWriter, cacheConfig != null ? cacheConfig : this.defaultCacheConfig);
	}
	
}
