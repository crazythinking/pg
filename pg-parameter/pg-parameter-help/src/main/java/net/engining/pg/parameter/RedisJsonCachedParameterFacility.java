package net.engining.pg.parameter;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * 当使用Redis作为统一的参数缓存时，需要区分其他微服务的cacheManager，所以这里固定cacheManager="cacheParameterManager"；
 * 
 * 注意：<br>
 * 1.由于使用@Cacheable是使用AOP实现的，那么如果存在类内部的方法调用类内部的加缓存注解的方法不会走代理，不会走代理，就不能正常创建缓存，所以每次都需要去调用数据库。<br>
 * 2.因为@Cacheable由AOP实现，所以如果该方法被其它注解切入，当缓存命中的时候，则其它注解不能正常切入并执行，@Before 也不行，当缓存没有命中的时候，其它注解可以正常工作。<br>
 * <br>
 * 因此原则上可能被外部直接调用到的参数操作方法需要相应的缓存操作，但是出于性能考虑对add方法不做缓存操作，由get方法缓存。
 * 
 * @author luxue
 *
 */
@CacheConfig(cacheManager="cacheParameterManager")
public class RedisJsonCachedParameterFacility extends JsonLocalCachedParameterFacility {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisJsonCachedParameterFacility.class);
	
	/* 
	 * 用于缓存默认获取当前生效的参数，因此不需要effective date
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#getParameter(java.lang.Class, java.lang.String)
	 */
	@Override
	@Cacheable(cacheNames="parameter", key="#paramClass.getName() + \"_\" + #key")
	public <T> T getParameter(Class<T> paramClass, String key) {
		
		logger.trace("本操作将从数据库加载该参数，如果不是内部方法调用将触发向Redis服务器添加参数缓存； param class:{}, key:{}", paramClass.getName(), key);
		return super.getParameter(paramClass, key);
	}
	
	/* 
	 * 用于缓存指定effective date的参数
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.LocalCachedParameterFacility#getParameter(java.lang.Class, java.lang.String, java.util.Date)
	 */
	@Override
	@Cacheable(cacheNames="parameter", key="#paramClass.getName() + \"_\" + #key + \"_\" + #effectiveDate.getTime()")
	public <T> T getParameter(Class<T> paramClass, String key, Date effectiveDate) {
		logger.trace("本操作将从数据库加载该参数，如果不是内部方法调用将触发向Redis服务器添加参数缓存；param class:{}, key:{}, effective date:{}", paramClass.getName(), key, effectiveDate);
		return super.getParameter(paramClass, key, effectiveDate);
	}
	
	/* 
	 * 用于缓存默认获取当前生效的全局唯一参数，不允许为空
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#loadUniqueParameter(java.lang.Class)
	 */
	@Override
	@Cacheable(cacheNames="parameter", key="#paramClass.getName() + \"_*\"")
	public <T> T loadUniqueParameter(Class<T> paramClass) {
		logger.trace("本操作将从数据库加载该参数，如果不是内部方法调用将触发向Redis服务器添加参数缓存；param class:{}", paramClass.getName());
		return super.loadUniqueParameter(paramClass);
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#updateParameter(net.engining.pg.parameter.HasKey)
	 */
	@Override
	@CacheEvict(cacheNames="parameter", key="#parameter.getClass().getName() + \"_\" + #parameter.getKey()")
	public void updateParameter(HasKey parameter) {
		logger.trace("本操作将先到数据库更新该参数，如果不是内部方法调用将触发向Redis服务器删除参数缓存; param key:{}, param class:{}", parameter.getKey(), parameter.getClass().getName());
		super.updateParameter(parameter);
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.JsonLocalCachedParameterFacility#updateParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	@CacheEvict(cacheNames="parameter", key="#parameter.getClass().getName() + \"_\" + #key")
	public <T> void updateParameter(String key, T parameter) {
		logger.trace("本操作将先到数据库更新该参数，如果不是内部方法调用将触发向Redis服务器删除参数缓存; param key:{}, param class:{}", key, parameter.getClass().getName());
		super.updateParameter(key, parameter);
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#updateUniqueParameter(java.lang.Object)
	 */
	@Override
	@CacheEvict(cacheNames="parameter", key="#parameter.getClass().getName() + \"_*\"")
	public <T> void updateUniqueParameter(T parameter) {
		logger.trace("本操作将先到数据库更新该参数，如果不是内部方法调用将触发向Redis服务器删除参数缓存; param class:{}", parameter.getClass().getName());
		super.updateUniqueParameter(parameter);
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.LocalCachedParameterFacility#updateParameter(java.lang.String, java.lang.Object, java.util.Date)
	 */
	@Override
	@CacheEvict(cacheNames="parameter", key="#parameter.getClass().getName() + \"_\" + #key + \"_\" + #effectiveDate.getTime()")
	public <T> void updateParameter(String key, T parameter, Date effectiveDate) {
		logger.trace("本操作将先到数据库更新该参数，如果不是内部方法调用将触发向Redis服务器删除参数缓存; param key:{}, param class:{}, effective date:{}", key, parameter.getClass().getName(), effectiveDate.getTime());
		super.updateParameter(key, parameter, effectiveDate);
	}
	
	/* 
	 * TODO 需要支持按匹配符批量删除
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#removeParameter(java.lang.Class, java.lang.String)
	 */
	@Override
	@CacheEvict(cacheNames="parameter", key="\"regex:\" + #paramClass.getName() + \"_\" + #key + \":*\"")
	public <T> boolean removeParameter(Class<T> paramClass, String key) {
		logger.trace("本操作将先到数据库删除该参数，如果不是内部方法调用将触发向Redis服务器删除参数缓存; param key:{}, param class:{}", key, paramClass.getName());
		return super.removeParameter(paramClass, key);
	}
	
	/* 
	 * 由于原接口的removeParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.JsonLocalCachedParameterFacility#removeParameter(java.lang.Class, java.lang.String, java.util.Date)
	 */
	@Override
	@CacheEvict(cacheNames="parameter", key="#paramClass.getName() + \"_\" + #key + \"_\" + #effectiveDate.getTime()")
	public <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate) {
		logger.trace("本操作将先到数据库删除该参数，如果不是内部方法调用将触发向Redis服务器删除参数缓存; param key:{}, param class:{}, effective date:{}", key, paramClass.getName());
		return super.removeParameter(paramClass, key, effectiveDate);
	}
}
