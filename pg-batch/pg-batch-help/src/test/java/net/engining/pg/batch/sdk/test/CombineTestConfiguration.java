package net.engining.pg.batch.sdk.test;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;

import net.engining.pg.batch.sdk.test.config.JPA4H2ContextConfig;
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
@ImportResource(value = {
		"reader-test-job.xml"
})
@EntityScan(basePackages = {
		"net.engining.pg.batch.entity",
		"net.engining.pg.batch.sdk.test"
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
	public JobLauncherTestUtils jobLauncherTestUtils(){
		JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
		return jobLauncherTestUtils;
		
	}
}
