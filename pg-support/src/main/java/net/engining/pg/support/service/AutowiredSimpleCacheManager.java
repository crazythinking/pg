package net.engining.pg.support.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;

/**
 * 用于改造 {@link SimpleCacheManager}，以便缓存不用集中配置，而可以由各处配置需要的缓存
 * @author licj
 *
 */
public class AutowiredSimpleCacheManager extends SimpleCacheManager
{
	@Autowired(required=false)
	private List<Cache> autowiredCaches;
	
	@Override
	public void afterPropertiesSet() {
		
		//先设置缓存，再调父类
		if (autowiredCaches != null)
			setCaches(autowiredCaches);
		
		super.afterPropertiesSet();
	}
}
