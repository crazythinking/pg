/**
 * 
 */
package net.engining.pg.param.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * 扩展{@link RedisCache}可根据通配符Key批量删除缓存
 * @author luxue
 *
 */
public class ExtRedisCache extends RedisCache {
	private static final Logger logger = LoggerFactory.getLogger(ExtRedisCache.class);
	
	private Vector<Object> mKeyElements = new Vector<>();
	
	@Autowired
	CacheManager cacheManager;

	/**
	 * @param name
	 * @param cacheWriter
	 * @param cacheConfig
	 */
	protected ExtRedisCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
		super(name, cacheWriter, cacheConfig);
	}

	@Override
	public void clear() {
		super.clear();
		mKeyElements.clear();
	}

	@Override
	public void put(final Object key, final Object value) {
		super.put(key, value);
		mKeyElements.add(key);
	}
	

	/* (non-Javadoc)
	 * @see org.springframework.data.redis.cache.RedisCache#get(java.lang.Object, java.util.concurrent.Callable)
	 */
	@Override
	public synchronized <T> T get(Object key, Callable<T> valueLoader) {
		T value = super.get(key, valueLoader); 
		if(!mKeyElements.contains(key)) {
			mKeyElements.add(key);
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
			for (Object item : mKeyElements) {
				if (lvPattern.matcher((String) item).matches()) {
					lvTmp.add(item);
					evictx(item);
				}
			}
			mKeyElements.removeAll(lvTmp);
		
		}
		else {
			super.evict(key);
			mKeyElements.remove(key);
		}
	}

	private void evictx(Object key) {
		super.evict(key);
	}

}
