package net.engining.pg.support.db.id.generator;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class SnowflakeSequenceIdGenerator implements IdentifierGenerator, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(SnowflakeSequenceIdGenerator.class);

	public long workerId = 0L;
	public long datacenterId = 0L;

	private SnowflakeSequenceID snowflakeSequenceID;
	
	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		
		if(Optional.fromNullable(snowflakeSequenceID).isPresent()){
			logger.info("已经存在snowflakeSequenceID实例，无需再次实例化");
		}
		else {
			// 是否通过MAC产生
			boolean macflg = false;
			
			logger.info("尝试从JVM Property 中加载配置");
			// 加载JVM配置
			Properties jvmProperties = System.getProperties();
			if (Optional.fromNullable(jvmProperties).isPresent()) {
				try {
					workerId = Long.parseLong(jvmProperties.getProperty("pg.snowflake.workerId"));
					datacenterId = Long.parseLong(jvmProperties.getProperty("pg.snowflake.dataCenterId"));
				} catch (Exception e) {
					logger.warn("snowflake.properties配置异常：" + e.getMessage());
					logger.warn("仍然未能正确找到配置属性，将使用MAC产生snowflake Id");
					macflg = true;
				}
			} else {
				logger.warn("仍然未能正确找到配置属性，将使用MAC产生snowflake Id");
				macflg = true;
			}

			if(macflg){
				snowflakeSequenceID = new SnowflakeSequenceID();
			}
			else {
				snowflakeSequenceID = new SnowflakeSequenceID(workerId, datacenterId);
			}
		}

	}

	@Override
	public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
		return snowflakeSequenceID.nextIdString();
	}

}
