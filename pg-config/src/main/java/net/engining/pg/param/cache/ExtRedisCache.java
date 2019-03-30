/**
 * 
 */
package net.engining.pg.param.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * 扩展{@link RedisCache}可根据通配符Key批量删除缓存
 * 
 * @author luxue
 *
 */
public class ExtRedisCache extends RedisCache {

	private static final Logger logger = LoggerFactory.getLogger(ExtRedisCache.class);
	
	private Vector<String> paramKeyElements = new Vector<>();
	
	private final RedisOperations<? extends Object, ? extends Object> redisOperations;
	private final RedisCacheMetadata cacheMetadata;
	private final CacheValueAccessor cacheValueAccessor;
	/**
	 * @param name
	 * @param prefix
	 * @param redisOperations
	 * @param expiration
	 */
	public ExtRedisCache(String name, byte[] prefix, RedisOperations<? extends Object, ? extends Object> redisOperations, long expiration) {
		super(name, prefix, redisOperations, expiration);
		RedisSerializer<?> serializer = redisOperations.getValueSerializer() != null ? redisOperations.getValueSerializer()
				: (RedisSerializer<?>) new JdkSerializationRedisSerializer();

		this.cacheMetadata = new RedisCacheMetadata(name, prefix);
		this.cacheMetadata.setDefaultExpiration(expiration);
		this.redisOperations = redisOperations;
		this.cacheValueAccessor = new CacheValueAccessor(serializer);
		new String(prefix);
	}

	@Override
	public void clear() {
		super.clear();
		paramKeyElements.clear();
	}

	@Override
	public void put(final Object key, final Object value) {
		super.put(key, value);
		paramKeyElements.add((String)key);
	}

	@Override
	public synchronized <T> T get(Object key, Callable<T> valueLoader) {
		T value = super.get(key, valueLoader);
		if (!paramKeyElements.contains(key)) {
			paramKeyElements.add((String)key);
		}
		return value;

	}

	/**
	 * 扩展：支持按通配符批量删除缓存；在@CacheEvict的key定义前加“regex:”前缀标识将使用正则表达式，“:*”标识通配多个字符
	 */
	@Override
	public void evict(Object key) {
		if (key != null && key instanceof String && ((String) key).startsWith("regex:")) {

			String lvsPattern = ((String) key).split("regex:")[1];

			lvsPattern = lvsPattern.replace(":*", ".*");
			Pattern lvPattern = Pattern.compile(lvsPattern);
			List<Object> lvTmp = new ArrayList<Object>();
			for (String item : paramKeyElements) {
				if (lvPattern.matcher((String) item).matches()) {
					lvTmp.add(item);
					logger.trace("remove caches by key:{}",(String) item);
					super.evict(item);
				}
			}
			paramKeyElements.removeAll(lvTmp);

		}
		else {
			super.evict(key);
			paramKeyElements.remove(key);
		}
	}

	/**
	 * @return the paramKeyElements
	 */
	public Vector<String> getParamKeyElements() {
		return paramKeyElements;
	}

	/**
	 * @param paramKeyElements
	 *                             the paramKeyElements to set
	 */
	public void setParamKeyElements(Vector<String> paramKeyElements) {
		this.paramKeyElements = paramKeyElements;
	}
	
	
	@Override
	public RedisCacheElement get(final RedisCacheKey cacheKey) {

		Assert.notNull(cacheKey, "CacheKey must not be null!");

		Boolean exists = (Boolean) redisOperations.execute(new RedisCallback<Boolean>() {

			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.exists(cacheKey.getKeyBytes());
			}
		});

		if (null == exists) {
			return null;
		}
		if (!exists) {
			return null;
		}

		byte[] bytes = doLookup(cacheKey);

		// safeguard if key gets deleted between EXISTS and GET calls.
		if (bytes == null) {
			return null;
		}

		return new RedisCacheElement(cacheKey, fromStoreValue(deserialize(bytes)));
	}

	private byte[] doLookup(Object key) {
		
		RedisCacheKey cacheKey = key instanceof RedisCacheKey ? (RedisCacheKey) key : getRedisCacheKey(key);

		return (byte[]) redisOperations.execute(new AbstractRedisCacheCallback<byte[]>(
				new BinaryRedisCacheElement(new RedisCacheElement(cacheKey, null), cacheValueAccessor), cacheMetadata) {

			@Override
			public byte[] doInRedis(BinaryRedisCacheElement element, RedisConnection connection) throws DataAccessException {
				return connection.get(element.getKeyBytes());
			}
		});
	}
	
	private RedisCacheKey getRedisCacheKey(Object key) {
		return new RedisCacheKey(key).usePrefix(this.cacheMetadata.getKeyPrefix())
				.withKeySerializer(redisOperations.getKeySerializer());
	}
	
	private Object deserialize(byte[] bytes) {
		return bytes == null ? null : cacheValueAccessor.deserializeIfNecessary(bytes);
	}
	
	
	static abstract class AbstractRedisCacheCallback<T> implements RedisCallback<T> {

		private long WAIT_FOR_LOCK_TIMEOUT = 300;
		private final BinaryRedisCacheElement element;
		private final RedisCacheMetadata cacheMetadata;

		public AbstractRedisCacheCallback(BinaryRedisCacheElement element, RedisCacheMetadata metadata) {
			this.element = element;
			this.cacheMetadata = metadata;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.redis.core.RedisCallback#doInRedis(org.springframework.data.redis.connection.RedisConnection)
		 */
		@Override
		public T doInRedis(RedisConnection connection) throws DataAccessException {
			waitForLock(connection);
			return doInRedis(element, connection);
		}

		public abstract T doInRedis(BinaryRedisCacheElement element, RedisConnection connection) throws DataAccessException;

		protected void processKeyExpiration(RedisCacheElement element, RedisConnection connection) {
			if (!element.isEternal()) {
				connection.expire(element.getKeyBytes(), element.getTimeToLive());
			}
		}

		protected void maintainKnownKeys(RedisCacheElement element, RedisConnection connection) {

			if (!element.hasKeyPrefix()) {

				connection.zAdd(cacheMetadata.getSetOfKnownKeysKey(), 0, element.getKeyBytes());

				if (!element.isEternal()) {
					connection.expire(cacheMetadata.getSetOfKnownKeysKey(), element.getTimeToLive());
				}
			}
		}

		protected void cleanKnownKeys(RedisCacheElement element, RedisConnection connection) {

			if (!element.hasKeyPrefix()) {
				connection.zRem(cacheMetadata.getSetOfKnownKeysKey(), element.getKeyBytes());
			}
		}

		protected boolean waitForLock(RedisConnection connection) {

			boolean retry;
			boolean foundLock = false;
			do {
				retry = false;
				if (connection.exists(cacheMetadata.getCacheLockKey())) {
					foundLock = true;
					try {
						Thread.sleep(WAIT_FOR_LOCK_TIMEOUT);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					retry = true;
				}
			} while (retry);

			return foundLock;
		}

		protected void lock(RedisConnection connection) {
			waitForLock(connection);
			connection.set(cacheMetadata.getCacheLockKey(), "locked".getBytes());
		}

		protected void unlock(RedisConnection connection) {
			connection.del(cacheMetadata.getCacheLockKey());
		}
	}

	
	
	
	static class BinaryRedisCacheElement extends RedisCacheElement {

		private byte[] keyBytes;
		private byte[] valueBytes;
		private RedisCacheElement element;
		private boolean lazyLoad;
		private CacheValueAccessor accessor;

		public BinaryRedisCacheElement(RedisCacheElement element, CacheValueAccessor accessor) {

			super(element.getKey(), element.get());
			this.element = element;
			this.keyBytes = element.getKeyBytes();
			this.accessor = accessor;

			lazyLoad = element.get() instanceof Callable;
			this.valueBytes = lazyLoad ? null : accessor.convertToBytesIfNecessary(element.get());
		}

		@Override
		public byte[] getKeyBytes() {
			return keyBytes;
		}

		public long getTimeToLive() {
			return element.getTimeToLive();
		}

		public boolean hasKeyPrefix() {
			return element.hasKeyPrefix();
		}

		public boolean isEternal() {
			return element.isEternal();
		}

		public RedisCacheElement expireAfter(long seconds) {
			return element.expireAfter(seconds);
		}

		@Override
		public byte[] get() {

			if (lazyLoad && valueBytes == null) {
				try {
					valueBytes = accessor.convertToBytesIfNecessary(((Callable<?>) element.get()).call());
				} catch (Exception e) {
					throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e.getMessage(), e);
				}
			}
			return valueBytes;
		}
	}

	
	
	
	static class RedisCacheMetadata {

		private final String cacheName;
		private final byte[] keyPrefix;
		private final byte[] setOfKnownKeys;
		private final byte[] cacheLockName;
		private long defaultExpiration = 0;

		/**
		 * @param cacheName must not be {@literal null} or empty.
		 * @param keyPrefix can be {@literal null}.
		 */
		public RedisCacheMetadata(String cacheName, byte[] keyPrefix) {

			Assert.hasText(cacheName, "CacheName must not be null or empty!");
			this.cacheName = cacheName;
			this.keyPrefix = keyPrefix;

			StringRedisSerializer stringSerializer = new StringRedisSerializer();

			// name of the set holding the keys
			this.setOfKnownKeys = usesKeyPrefix() ? new byte[] {} : stringSerializer.serialize(cacheName + "~keys");
			this.cacheLockName = stringSerializer.serialize(cacheName + "~lock");
		}

		/**
		 * @return true if the {@link RedisCache} uses a prefix for key ranges.
		 */
		public boolean usesKeyPrefix() {
			return (keyPrefix != null && keyPrefix.length > 0);
		}

		/**
		 * Get the binary representation of the key prefix.
		 *
		 * @return never {@literal null}.
		 */
		public byte[] getKeyPrefix() {
			return this.keyPrefix;
		}

		/**
		 * Get the binary representation of the key identifying the data structure used to maintain known keys.
		 *
		 * @return never {@literal null}.
		 */
		public byte[] getSetOfKnownKeysKey() {
			return setOfKnownKeys;
		}

		/**
		 * Get the binary representation of the key identifying the data structure used to lock the cache.
		 *
		 * @return never {@literal null}.
		 */
		public byte[] getCacheLockKey() {
			return cacheLockName;
		}

		/**
		 * Get the name of the cache.
		 *
		 * @return
		 */
		public String getCacheName() {
			return cacheName;
		}

		/**
		 * Set the default expiration time in seconds
		 * 
		 * @param seconds
		 */
		public void setDefaultExpiration(long seconds) {
			this.defaultExpiration = seconds;
		}

		/**
		 * Get the default expiration time in seconds.
		 *
		 * @return
		 */
		public long getDefaultExpiration() {
			return defaultExpiration;
		}

	}

	/**
	 * @author Christoph Strobl
	 * @since 1.5
	 */
	static class CacheValueAccessor {

		@SuppressWarnings("rawtypes") //
		private final RedisSerializer valueSerializer;

		@SuppressWarnings("rawtypes")
		CacheValueAccessor(RedisSerializer valueRedisSerializer) {
			valueSerializer = valueRedisSerializer;
		}

		@SuppressWarnings("unchecked")
		byte[] convertToBytesIfNecessary(Object value) {

			if (value == null) {
				return new byte[0];
			}

			if (valueSerializer == null && value instanceof byte[]) {
				return (byte[]) value;
			}

			return valueSerializer.serialize(value);
		}

		Object deserializeIfNecessary(byte[] value) {

			if (valueSerializer != null) {
				return valueSerializer.deserialize(value);
			}

			return value;
		}
	}
}
