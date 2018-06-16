package net.engining.pg.batch.sdk;

import java.text.MessageFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.orm.jpa.vendor.Database;

import com.google.common.base.Optional;

/**
 * 通用表清空步骤
 * 
 * @author licj
 * 
 */
public class TableTruncateTasklet implements Tasklet {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<Class<?>> entities;

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private JpaProperties jpaProperties;
	
	@Value("${spring.jpa.database}")
	private Database database;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception 
	{
		for (Class<?> entity : entities)
		{
			Table table = entity.getAnnotation(Table.class);
			if (table == null || StringUtils.isBlank(table.name())){
				logger.warn("{} 不是JPA实体或实体没有用@Table注释指定数据库表名。", entity.getCanonicalName());}

			logger.info("清空表：" + table.name());
			
			if(Optional.fromNullable(jpaProperties).isPresent()){
				database = jpaProperties.getDatabase();
			}
			
			switch (database)
			{
			case ORACLE:
			case MYSQL:
			case HSQL:
			case H2:
				truncateTable(table);
				break;
			case DB2:
				truncateDB2(table);
				break;
			default:
				logger.info("没有指定有效的jpaDatabaseType属性，默认执行truncate table命令");
				truncateTable(table);
				break;
			}

		}

		return RepeatStatus.FINISHED;
	}
	
	private void truncateTable(Table table)
	{
		em.createNativeQuery("truncate table " + table.name())
			.executeUpdate();
	}

	/**
	 * DB2专用的不记日志清空表的步聚。 这里不使用v9.7以后才用的truncate命令，而使用ALTER TABLE X ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE
	 */
	private void truncateDB2(Table table)
	{
		// 因为在做alter期间不能rollback，所以最好不要抛异常。rollback后会使表不可用。
		try 
		{
			em.createNativeQuery(MessageFormat.format("ALTER TABLE {0} ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE", table.name()))
				.executeUpdate();
		}
		catch (Exception e)
		{
			logger.error("清表过程出错", e);
		}
	}

	public List<Class<?>> getEntities() {
		return entities;
	}

	@Required
	public void setEntities(List<Class<?>> entities) {
		this.entities = entities;
	}

	/**
	 * @return the database
	 */
	public Database getDatabase() {
		return database;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
}
