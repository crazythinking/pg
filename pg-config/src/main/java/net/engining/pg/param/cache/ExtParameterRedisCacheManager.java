package net.engining.pg.param.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 只用于Parameter的Redis Cache Manager
 * 
 * @author luxue
 *
 */
public class ExtParameterRedisCacheManager extends RedisCacheManager {

	private static final Logger log = LoggerFactory.getLogger(ExtParameterRedisCacheManager.class);
	
	public static final String PARAM_CACHE_NAME = "parameter-CC";

	private ExtRedisCache redisCache;

	@SuppressWarnings("rawtypes")
	private RedisOperations redisOperations;

	public ExtParameterRedisCacheManager(@SuppressWarnings("rawtypes") RedisOperations redisOperations) {
		super(redisOperations);
		super.setUsePrefix(true);
		RedisCachePrefix cachePrefix = new RedisPrefix();
		super.setCachePrefix(cachePrefix);
		// 设置服务启动时从redis获取缓存的参数key
		super.setLoadRemoteCachesOnStartup(true);
		this.redisOperations = redisOperations;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() {
		long expiration = computeExpiration(PARAM_CACHE_NAME);
		this.redisCache = new ExtRedisCache(PARAM_CACHE_NAME, (super.isUsePrefix() ? super.getCachePrefix().prefix(PARAM_CACHE_NAME) : null), super.getRedisOperations(), expiration);
		initializeCaches();
	}
	
	@Override
	protected ExtRedisCache createCache(String cacheName) {
		return this.redisCache;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Set<String> loadRemoteCacheKeys() {
		return (Set<String>) this.redisOperations.execute(new RedisCallback<Set<String>>() {

			@Override
			public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {

				Set<String> redisKeys = null;

				log.trace("only getting cacheKeys form {}:* ", PARAM_CACHE_NAME);
				//FIXME 该处性能可能有问题
				redisKeys = redisOperations.keys(PARAM_CACHE_NAME+":*");

				Set<String> cacheKeys = new LinkedHashSet<String>();

				if (!CollectionUtils.isEmpty(redisKeys)) {
					for (String key : redisKeys) {
						key = key.replace(PARAM_CACHE_NAME+":", "");
						redisCache.getParamKeyElements().add(key);
					}
				}

				return cacheKeys;
			}
		});
	}

}
