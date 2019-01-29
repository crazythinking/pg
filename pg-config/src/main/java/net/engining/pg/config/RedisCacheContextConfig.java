package net.engining.pg.config;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.engining.pg.param.cache.ExtDefaultRedisCacheWriter;
import net.engining.pg.param.cache.ExtParameterRedisCacheManager;
import net.engining.pg.param.cache.RedisCachePrefix;
import net.engining.pg.param.props.PgParamAndCacheProperties;
import net.engining.pg.props.CommonProperties;

/**
 * 
 * @author luxue
 *
 */
@Configuration
@EnableCaching
public class RedisCacheContextConfig extends CachingConfigurerSupport {
	
	@Autowired
	CommonProperties commonProperties;

	@Autowired
	PgParamAndCacheProperties pgParamAndCacheProperties;
	
	/**
	 * 根据类名，方法名，参数组合生成缓存Key
	 * 
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
	 * @param factory
	 * @return
	 */
	@Bean
	@Primary
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Serializable> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Serializable>(Serializable.class);

		// 解决查询缓存转换异常的问题
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 整体缓存过期时间, 默认过期时间5分钟
		config = defaultExpiration(config);
		
		// 配置序列化（解决乱码的问题）
		config = config
				.computePrefixWith(new RedisCachePrefix(commonProperties.getAppname()))
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
				.disableCachingNullValues();

		RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
				.cacheDefaults(config)
//				.transactionAware()
				.build();
		
		return cacheManager;
	}
	
	/**
	 * 统一的共享参数缓存Manager，固定缓存管理器Bean名称 {@link net.engining.pg.parameter.RedisCachedParameterFacility} {@link net.engining.pg.parameter.RedisCachedParameterFacility}；各微服务间共享的参数缓存
	 * @param factory
	 * @return
	 */
	@Bean("cacheParameterManager")
    public CacheManager cacheParameterManager(RedisConnectionFactory factory) {
		
		RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Serializable> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Serializable>(Serializable.class);

		// 解决查询缓存转换异常的问题
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 整体缓存过期时间, 默认过期时间5分钟
		defaultExpiration(config);
		
		// 配置序列化（解决乱码的问题）
		config = config
				.computePrefixWith(new RedisCachePrefix())
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
				.disableCachingNullValues();
		
		Assert.notNull(factory, "ConnectionFactory must not be null!");
		ExtParameterRedisCacheManager cacheManager = new ExtParameterRedisCacheManager(new ExtDefaultRedisCacheWriter(factory), config);
//		cacheManager.setTransactionAware(true);
		
		return cacheManager;
    }
	
	private RedisCacheConfiguration defaultExpiration(RedisCacheConfiguration config){
		// 整体缓存过期时间, 默认过期时间5分钟
		long expriation = pgParamAndCacheProperties.getExpireDuration();
		TimeUnit expireTimeUnit = pgParamAndCacheProperties.getExpireTimeUnit();
		if (pgParamAndCacheProperties.getExpireDuration() > 0) {
			if (pgParamAndCacheProperties.getExpireTimeUnit() != null) {
				expireTimeUnit = pgParamAndCacheProperties.getExpireTimeUnit();
				switch (expireTimeUnit) {
				case DAYS:
					expriation = pgParamAndCacheProperties.getExpireDuration() * 24 * 60 * 60;
					config.entryTtl(Duration.ofDays(expriation));
					break;
				case HOURS:
					expriation = pgParamAndCacheProperties.getExpireDuration() * 60 * 60;
					config.entryTtl(Duration.ofHours(expriation));
					break;
				case MINUTES:
					expriation = pgParamAndCacheProperties.getExpireDuration() * 60;
					config.entryTtl(Duration.ofMinutes(expriation));
					break;
				case SECONDS:
					expriation = pgParamAndCacheProperties.getExpireDuration();
					config.entryTtl(Duration.ofSeconds(expriation));
					break;
				default:
					//默认过期时间5分钟
					config.entryTtl(Duration.ofMinutes(expriation));
					break;
				}
			}
		}
		return config;
	}

}
