package net.engining.pg.support.test;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import net.engining.pg.support.core.context.ApplicationContextHolder;
import net.engining.pg.support.test.config.JPA4H2ContextConfig;

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
		"net.engining.pg.support.test.db"
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
	
}
