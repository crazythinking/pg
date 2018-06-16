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
import org.springframework.beans.factory.annotation.Required;

/**
 * DB2专用的不记日志清空表的步聚。 这里不使用v9.7以后才用的truncate命令，而使用ALTER TABLE X ACTIVATE NOT
 * LOGGED INITIALLY WITH EMPTY TABLE
 * 
 * @author licj
 *
 */
public class DB2TruncateTasklet497 implements Tasklet {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<Class<?>> entities;

	@PersistenceContext
	private EntityManager em;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		// 因为在做alter期间不能rollback，所以最好不要抛异常。rollback后会使表不可用。

		try {
			for (Class<?> entity : entities) {
				Table table = entity.getAnnotation(Table.class);
				if (table == null || StringUtils.isBlank(table.name())) {
					logger.warn("{} 不是JPA实体或实体没有用@Table注释指定数据库表名。", entity.getCanonicalName());
				}

				em.createNativeQuery(MessageFormat.format("ALTER TABLE {0} ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE", table.name()))
						.executeUpdate();

				logger.info("清空表：" + table.name());
			}
		}
		catch (Exception e) {
			logger.error("清表过程出错", e);
		}

		return RepeatStatus.FINISHED;
	}

	public List<Class<?>> getEntities() {
		return entities;
	}

	@Required
	public void setEntities(List<Class<?>> entities) {
		this.entities = entities;
	}

}
