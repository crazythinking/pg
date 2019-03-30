package net.engining.pg.parameter.test;

import net.engining.pg.config.RedisCacheContextConfig;
import net.engining.pg.param.props.PgParamAndCacheProperties;
import net.engining.pg.parameter.*;
import net.engining.pg.parameter.test.cache.ControlledTicker;
import net.engining.pg.parameter.test.config.JPA4H2ContextConfig;
import net.engining.pg.props.CommonProperties;
import net.engining.pg.support.core.context.ApplicationContextHolder;
import net.engining.pg.support.core.context.ProcessesProvider4Organization;
import net.engining.pg.support.core.context.Provider4Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

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
		RedisCacheContextConfig.class
})
@EntityScan(basePackages = {
		"net.engining.pg.parameter.entity"
	})
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
	public ParameterInter parameterFacility(ControlledTicker controlledTicker){
		if (pgParamAndCacheProperties.isEnableRedisCache()) {
			if(pgParamAndCacheProperties.isJsonParameterFacility()){
				JsonRedisCachedParameterFacility jsonRedisCachedParameterFacility = new JsonRedisCachedParameterFacility();
				jsonRedisCachedParameterFacility.setTicker(controlledTicker);
				jsonRedisCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
				jsonRedisCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
				return jsonRedisCachedParameterFacility;
			}
			XmlRedisCachedParameterFacility xmlRedisCachedParameterFacility = new XmlRedisCachedParameterFacility();
			xmlRedisCachedParameterFacility.setTicker(controlledTicker);
			xmlRedisCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
			xmlRedisCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
			return xmlRedisCachedParameterFacility;
		}
		
		if(pgParamAndCacheProperties.isJsonParameterFacility()){
			JsonGuavaCachedParameterFacility jsonGuavaCachedParameterFacility = new JsonGuavaCachedParameterFacility();
			jsonGuavaCachedParameterFacility.setTicker(controlledTicker);
			jsonGuavaCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
			jsonGuavaCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
			return jsonGuavaCachedParameterFacility;
		}
		XmlGuavaCachedParameterFacility xmlGuavaCachedParameterFacility = new XmlGuavaCachedParameterFacility();
		xmlGuavaCachedParameterFacility.setTicker(controlledTicker);
		xmlGuavaCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
		xmlGuavaCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
		return xmlGuavaCachedParameterFacility;
	}
	
	@Bean
	public Provider4Organization provider4Organization(){
		ProcessesProvider4Organization processesProvider4Organization = new ProcessesProvider4Organization();
		//从配置文件获取默认机构ID
		processesProvider4Organization.setCurrentOrganizationId("0000001");
		return processesProvider4Organization;
	}
	
}
