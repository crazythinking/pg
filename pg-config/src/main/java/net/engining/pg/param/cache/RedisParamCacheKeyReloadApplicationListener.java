/**
 * 
 */
package net.engining.pg.param.cache;

import java.io.Serializable;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * TODO 需要在服务启动时去redis加载所用缓存的参数key
 * @author luxue
 *
 */
public class RedisParamCacheKeyReloadApplicationListener implements ApplicationListener<ApplicationReadyEvent>{
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		
		ExtParameterRedisCacheManager cacheManager = (ExtParameterRedisCacheManager) event.getApplicationContext().getBean("cacheParameterManager");
		RedisTemplate<String, Serializable> rt = (RedisTemplate<String, Serializable>) event.getApplicationContext().getBean("redisTemplate");
		Cache cache = cacheManager.getCache("parameter");
	}

}
