package net.engining.pg.config;

import java.io.Serializable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author luxue
 *
 */
@Configuration
public class RedisContextConfig {

	@Bean("redisTemplate")
	public RedisTemplate<String, Serializable> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Serializable> rt = new RedisTemplate<String, Serializable>();
		rt.setConnectionFactory(factory);
		Jackson2JsonRedisSerializer<Serializable> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Serializable>(
				Serializable.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);

		rt.setKeySerializer(new StringRedisSerializer());
		rt.setValueSerializer(jackson2JsonRedisSerializer);
		rt.setHashKeySerializer(new StringRedisSerializer());
		rt.setHashValueSerializer(jackson2JsonRedisSerializer);
		//support transaction 第二步：强制支持事物
		rt.setEnableTransactionSupport(true);
		rt.afterPropertiesSet();
		return rt;
	}
}
