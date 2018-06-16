package net.engining.pg.support.test.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 针对H2数据库的context配置，通常用于Test
 * @author Administrator
 *
 */
@Configuration
@EnableTransactionManagement(mode=AdviceMode.ASPECTJ)//等同于<tx:annotation-driven mode="aspectj" transaction-manager="transactionManager" />
public class JPA4H2ContextConfig {
	
	@Autowired
	DataSource dataSource;

	@Bean
	public JdbcTemplate jdbcTemplate(){
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource);
		return jdbcTemplate;
		
	}
	
	/**
	 * h2 tcp server, 方便使用工具访问h2
	 * @return
	 * @throws SQLException
	 */
	@Bean(name="h2tcp", initMethod="start", destroyMethod="stop")
	public Server h2tcp() throws SQLException{
		
		return Server.createTcpServer("-tcp","-tcpAllowOthers","-tcpPort","49151");
		
	}
	
}
