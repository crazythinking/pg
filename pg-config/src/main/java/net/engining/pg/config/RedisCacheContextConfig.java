package net.engining.pg.config;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.engining.pg.param.cache.ExtParameterRedisCacheManager;
import net.engining.pg.param.cache.RedisPrefix;
import net.engining.pg.param.props.PgParamAndCacheProperties;
import net.engining.pg.props.CommonProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author luxue
 *
 */
@Configuration
@EnableCaching
public class RedisCacheContextConfig extends CachingConfigurerSupport {

	@Autowired
	PgParamAndCacheProperties pgParamAndCacheProperties;

	@Autowired
	CommonProperties commonProperties;
	
	@Bean("redisCacheTemplate")
	public RedisTemplate<String, Serializable> redisCacheTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Serializable> rt = new RedisTemplate<String, Serializable>();
		//支持事务, 考虑性能，缓存不开启事务
		//rt.setEnableTransactionSupport(true);
		rt.setConnectionFactory(factory);
		Jackson2JsonRedisSerializer<Serializable> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Serializable>(Serializable.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		
		rt.setKeySerializer(new StringRedisSerializer());
		rt.setValueSerializer(jackson2JsonRedisSerializer);
		rt.setHashKeySerializer(new StringRedisSerializer());
		rt.setHashValueSerializer(jackson2JsonRedisSerializer);
		rt.afterPropertiesSet();
		return rt;
	}
	
	/**
	 * 根据类名，方法名，参数组合生成缓存Key
	 * @return
	 */
	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};
	}
	
	/**
	 * 主缓存Manager；专门针对各微服务的缓存，便于区分
	 * @param redisCacheTemplate
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Bean
	@Primary
    public CacheManager cacheManager(@Qualifier("redisCacheTemplate") RedisTemplate redisCacheTemplate) {
        RedisCacheManager manager = new RedisCacheManager(redisCacheTemplate);
        manager.setUsePrefix(true);
        RedisCachePrefix cachePrefix = new RedisPrefix(commonProperties.getAppname());
        manager.setCachePrefix(cachePrefix);
        //设置超时
        defaultExpiration(manager);
        
        return manager;
    }
	
	/**
	 * FIXME 迁移到pg-parameter；统一的共享参数缓存Manager；各微服务间共享的参数缓存
	 * @param redisCacheTemplate
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Bean("cacheParameterManager")
    public CacheManager cacheParameterManager(@Qualifier("redisCacheTemplate") RedisTemplate redisCacheTemplate) {
		
        ExtParameterRedisCacheManager manager = new ExtParameterRedisCacheManager(redisCacheTemplate);
        //设置超时
        defaultExpiration(manager);
        //设置开启事务, 考虑性能，缓存不开启事务
        //manager.setTransactionAware(true);
		
        return manager;
    }
	
	private void defaultExpiration(RedisCacheManager manager){
		// 整体缓存过期时间, 默认过期时间5分钟
		long expriation = pgParamAndCacheProperties.getExpireDuration();
		TimeUnit expireTimeUnit = pgParamAndCacheProperties.getExpireTimeUnit();
		if (pgParamAndCacheProperties.getExpireDuration() > 0) {
			if (pgParamAndCacheProperties.getExpireTimeUnit() != null) {
				expireTimeUnit = pgParamAndCacheProperties.getExpireTimeUnit();
				switch (expireTimeUnit) {
					case DAYS:
						expriation = pgParamAndCacheProperties.getExpireDuration() * 24 * 60 * 60;
						break;
					case HOURS:
						expriation = pgParamAndCacheProperties.getExpireDuration() * 60 * 60;
						break;
					case MINUTES:
						expriation = pgParamAndCacheProperties.getExpireDuration() * 60;
						break;
					case SECONDS:
						expriation = pgParamAndCacheProperties.getExpireDuration();
						break;
					default:
						// 默认过期时间5分钟
						expriation = pgParamAndCacheProperties.getExpireDuration() * 60;
						break;
				}
			}

		}
		manager.setDefaultExpiration(expriation);
	}
	
}
