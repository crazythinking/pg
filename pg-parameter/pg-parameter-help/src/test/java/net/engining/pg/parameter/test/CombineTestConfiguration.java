package net.engining.pg.parameter.test;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import net.engining.pg.parameter.JsonLocalCachedParameterFacility;
import net.engining.pg.parameter.LocalCachedParameterFacility;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.ProcessesProvider4Organization;
import net.engining.pg.parameter.Provider4Organization;
import net.engining.pg.parameter.test.cache.ControlledTicker;
import net.engining.pg.parameter.test.config.JPA4H2ContextConfig;
import net.engining.pg.support.core.context.ApplicationContextHolder;

/**
 * 
 * @author Eric Lu
 *
 */
@Configuration
@Import(value={
		JPA4H2ContextConfig.class
})
@EntityScan(basePackages = {
		"net.engining.pg.parameter.entity"
	})
public class CombineTestConfiguration {
	
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
	public ParameterFacility parameterFacility(ControlledTicker controlledTicker){
		LocalCachedParameterFacility localCachedParameterFacility = new LocalCachedParameterFacility();
		localCachedParameterFacility.setTicker(controlledTicker);
		return localCachedParameterFacility;
	}
	
	/**
	 * 参数体系辅助Bean，建议项目必须注入
	 * @return
	 */
	@Bean("jsonparameterFacility")
	public ParameterFacility jsonparameterFacility(ControlledTicker controlledTicker){
		JsonLocalCachedParameterFacility localCachedParameterFacility = new JsonLocalCachedParameterFacility();
		localCachedParameterFacility.setTicker(controlledTicker);
		return localCachedParameterFacility;
	}
	
	@Bean
	public Provider4Organization provider4Organization(){
		ProcessesProvider4Organization processesProvider4Organization = new ProcessesProvider4Organization();
		//从配置文件获取默认机构ID
		processesProvider4Organization.setCurrentOrganizationId("0000001");
		return processesProvider4Organization;
	}
	
}
