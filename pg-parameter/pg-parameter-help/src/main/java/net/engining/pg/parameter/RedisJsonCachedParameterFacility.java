package net.engining.pg.parameter;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.google.common.base.Optional;

/**
 * 当使用Redis作为统一的参数缓存时，需要区分其他微服务的cacheManager，所以这里固定cacheManager="cacheParameterManager"；
 * FIXME 暂时在gm-config里统一进行配置，考虑是否有更好的解耦的方式；
 * @author luxue
 *
 */
@CacheConfig(cacheManager="cacheParameterManager", cacheNames="parameter")
public class RedisJsonCachedParameterFacility extends JsonLocalCachedParameterFacility{
	
	private static final Logger logger = LoggerFactory.getLogger(RedisJsonCachedParameterFacility.class);
	
	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#getParameter(java.lang.Class, java.lang.String)
	 */
	@Override
	@Cacheable(key="#paramClass.getName() + \"_\" + #key")
	public <T> T getParameter(Class<T> paramClass, String key) {
		
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.getParameter(paramClass, key);
	}
	
	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.LocalCachedParameterFacility#getParameter(java.lang.Class, java.lang.String, java.util.Date)
	 */
	@Override
	@Cacheable(key="#paramClass.getName() + \"_\" + #key + \"_\" + #effectiveDate.toString()")
	public <T> T getParameter(Class<T> paramClass, String key, Date effectiveDate) {
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.getParameter(paramClass, key, effectiveDate);
	}

//	/* (non-Javadoc)
//	 * @see net.engining.pg.parameter.ParameterFacility#getParameterMap(java.lang.Class)
//	 */
//	@Override
//	@Cacheable
//	public <T> Map<String, T> getParameterMap(Class<T> paramClass) {
//		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
//		return super.getParameterMap(paramClass);
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see net.engining.pg.parameter.ParameterFacility#getParameterMap(java.lang.Class, java.util.Date)
//	 */
//	@Override
//	@Cacheable
//	public <T> Map<String, T> getParameterMap(Class<T> paramClass, Date effectiveDate) {
//		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
//		return super.getParameterMap(paramClass, effectiveDate);
//	}
	
//	/* (non-Javadoc)
//	 * @see net.engining.pg.parameter.LocalCachedParameterFacility#getParameterTable(java.lang.Class)
//	 */
//	@Override
//	@Cacheable
//	public <T> TreeBasedTable<String, Date, T> getParameterTable(Class<T> paramClass) {
//		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
//		return super.getParameterTable(paramClass);
//	}
	
	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#getUniqueParameter(java.lang.Class)
	 */
	@Override
	@Cacheable(key="#paramClass.getName() + \"_*\"")
	public <T> Optional<T> getUniqueParameter(Class<T> paramClass) {
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.getUniqueParameter(paramClass);
	}
	
	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#loadUniqueParameter(java.lang.Class)
	 */
	@Override
	@Cacheable(key="#paramClass.getName() + \"_*\"")
	public <T> T loadUniqueParameter(Class<T> paramClass) {
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.loadUniqueParameter(paramClass);
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；
	 * 然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#updateParameter(net.engining.pg.parameter.HasKey)
	 */
	@Override
	@CacheEvict
	public void updateParameter(HasKey parameter) {
		logger.debug("本操作将先更新该参数到数据库，然后触发向Redis服务器更新参数缓存");
		super.updateParameter(parameter);
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；
	 * 然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.LocalCachedParameterFacility#updateParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	@CacheEvict(key="#parameter.getClass().getName() + \"_\" + #key")
	public <T> void updateParameter(String key, T parameter) {
		
		logger.debug("本操作将先更新该参数到数据库，然后触发向Redis服务器更新参数缓存");
		super.updateParameter(key, parameter);
		
	}
	
	/* 
	 * 由于原接口的updateParameter没有返回对象，因此无法直接更新到Redis，只能在这里删除；
	 * 然后再次get的时候缓存到Redis
	 * (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#updateUniqueParameter(java.lang.Object)
	 */
	@Override
	@CacheEvict(key="#parameter.getClass().getName() + \"_*\"")
	public <T> void updateUniqueParameter(T parameter) {
		logger.debug("本操作将先更新该参数到数据库，然后触发向Redis服务器更新参数缓存");
		super.updateUniqueParameter(parameter);
	}
	
	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.ParameterFacility#removeParameter(java.lang.Class, java.lang.String)
	 */
	@Override
	@CacheEvict(key="#paramClass.getName() + \"_\" + #key")
	public <T> boolean removeParameter(Class<T> paramClass, String key) {
		logger.debug("本操作将先到数据库删除该参数，然后触发向Redis服务器删除参数缓存");
		return super.removeParameter(paramClass, key);
	}
	
	/* (non-Javadoc)
	 * @see net.engining.pg.parameter.LocalCachedParameterFacility#removeParameter(java.lang.Class, java.lang.String, java.util.Date)
	 */
	@Override
	@CacheEvict(key="#paramClass.getName() + \"_\" + #key + \"_\" + #effectiveDate.toString()")
	public <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate) {
		logger.debug("本操作将先到数据库删除该参数，然后触发向Redis服务器删除参数缓存");
		return super.removeParameter(paramClass, key, effectiveDate);
	}
}
