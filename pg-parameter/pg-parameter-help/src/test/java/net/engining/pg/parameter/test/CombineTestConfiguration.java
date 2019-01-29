package net.engining.pg.parameter.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import net.engining.pg.config.RedisCacheContextConfig;
import net.engining.pg.config.RedisContextConfig;
import net.engining.pg.param.cache.RedisParamCacheKeyReloadApplicationListener;
import net.engining.pg.param.props.PgParamAndCacheProperties;
import net.engining.pg.parameter.JsonLocalCachedParameterFacility;
import net.engining.pg.parameter.LocalCachedParameterFacility;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.ProcessesProvider4Organization;
import net.engining.pg.parameter.Provider4Organization;
import net.engining.pg.parameter.RedisCachedParameterFacility;
import net.engining.pg.parameter.RedisJsonCachedParameterFacility;
import net.engining.pg.parameter.test.cache.ControlledTicker;
import net.engining.pg.parameter.test.config.JPA4H2ContextConfig;
import net.engining.pg.props.CommonProperties;
import net.engining.pg.support.core.context.ApplicationContextHolder;

/**
 * 
 * @author Eric Lu
 *
 */
@Configuration
@EnableConfigurationProperties(value = { 
		CommonProperties.class,
		PgParamAndCacheProperties.class,
		})
@Import(value={
		JPA4H2ContextConfig.class,
		RedisContextConfig.class,
		RedisCacheContextConfig.class
})
@EntityScan(basePackages = {
		"net.engining.pg.parameter.entity"
	})
@EnableCaching
public class CombineTestConfiguration {
	
	
	@Autowired
	PgParamAndCacheProperties pgParamAndCacheProperties;
	
	/**
	 * ApplicationContext的静态辅助Bean，建议项目必须注入
	 * @return
	 */
	@Bean
	@Lazy(value=false)
	public ApplicationContextHolder applicationContextHolder(){
		return new ApplicationContextHolder();
	}
	
	@Bean
	public ControlledTicker controlledTicker(){
		return new ControlledTicker();
	}
	
	/**
	 * 参数体系辅助Bean，建议项目必须注入
	 * @return
	 */
	@Bean
	public ParameterFacility parameterFacility(){
		if (pgParamAndCacheProperties.isEnableRedisCache()) {
			if(pgParamAndCacheProperties.isJsonParameterFacility()){
				RedisJsonCachedParameterFacility redisCachedParameterFacility = new RedisJsonCachedParameterFacility();
				redisCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
				redisCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
				return redisCachedParameterFacility;
			}
			RedisCachedParameterFacility redisCachedParameterFacility = new RedisCachedParameterFacility();
			redisCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
			redisCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
			return redisCachedParameterFacility;
		}
		
		if(pgParamAndCacheProperties.isJsonParameterFacility()){
			JsonLocalCachedParameterFacility localCachedParameterFacility = new JsonLocalCachedParameterFacility();
			localCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
			localCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
			return localCachedParameterFacility;
		}
		LocalCachedParameterFacility localCachedParameterFacility = new LocalCachedParameterFacility();
		localCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
		localCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
		return localCachedParameterFacility;
	}
	
	@Bean
	public Provider4Organization provider4Organization(){
		ProcessesProvider4Organization processesProvider4Organization = new ProcessesProvider4Organization();
		//从配置文件获取默认机构ID
		processesProvider4Organization.setCurrentOrganizationId("0000001");
		return processesProvider4Organization;
	}
	
	@Bean
	public RedisParamCacheKeyReloadApplicationListener redisParamCacheKeyReloadApplicationListener() {
		return new RedisParamCacheKeyReloadApplicationListener();
	}
	
}
