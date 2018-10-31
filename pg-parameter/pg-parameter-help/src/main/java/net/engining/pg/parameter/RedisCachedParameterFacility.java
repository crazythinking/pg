package net.engining.pg.parameter;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.google.common.base.Optional;

/**
 * @author luxue
 *
 */
@CacheConfig(cacheManager="cacheParameterManager", cacheNames="parameter")
public class RedisCachedParameterFacility extends LocalCachedParameterFacility{
	
	private static final Logger logger = LoggerFactory.getLogger(RedisCachedParameterFacility.class);
	
	@Override
	@Cacheable(key="#paramClass.getName() + \"_\" + #key")
	public <T> T getParameter(Class<T> paramClass, String key) {
		
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.getParameter(paramClass, key);
	}
	
	@Override
	@Cacheable(key="#paramClass.getName() + \"_\" + #key + \"_\" + #effectiveDate.toString()")
	public <T> T getParameter(Class<T> paramClass, String key, Date effectiveDate) {
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.getParameter(paramClass, key, effectiveDate);
	}

//	@Override
//	@Cacheable
//	public <T> Map<String, T> getParameterMap(Class<T> paramClass) {
//		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
//		return super.getParameterMap(paramClass);
//	}
//	
//	
//	@Override
//	@Cacheable
//	public <T> Map<String, T> getParameterMap(Class<T> paramClass, Date effectiveDate) {
//		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
//		return super.getParameterMap(paramClass, effectiveDate);
//	}
	
//	@Override
//	@Cacheable
//	public <T> TreeBasedTable<String, Date, T> getParameterTable(Class<T> paramClass) {
//		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
//		return super.getParameterTable(paramClass);
//	}
	
	@Override
	@Cacheable(key="#paramClass.getName() + \"_*\"")
	public <T> Optional<T> getUniqueParameter(Class<T> paramClass) {
		logger.debug("本操作将先重数据库加载该参数，然后触发向Redis服务器添加参数缓存");
		return super.getUniqueParameter(paramClass);
	}
	
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
	
	@Override
	@CacheEvict(key="#paramClass.getName() + \"_\" + #key")
	public <T> boolean removeParameter(Class<T> paramClass, String key) {
		logger.debug("本操作将先到数据库删除该参数，然后触发向Redis服务器删除参数缓存");
		return super.removeParameter(paramClass, key);
	}
	
	@Override
	@CacheEvict(key="#paramClass.getName() + \"_\" + #key + \"_\" + #effectiveDate.toString()")
	public <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate) {
		logger.debug("本操作将先到数据库删除该参数，然后触发向Redis服务器删除参数缓存");
		return super.removeParameter(paramClass, key, effectiveDate);
	}
}
