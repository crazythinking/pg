/**
 * 
 */
package net.engining.pg.support.db.sequence;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.springframework.stereotype.Component;

import net.engining.pg.support.db.DbType;

/**
 * DB2的Sequence Operator
 * @author luxue
 *
 */
//@Component// 基础包里的类不应该直接被扫描，交由需要的项目@Bean的方式创建
public class DB2SequenceOperator implements SequenceOperator {

	@PersistenceContext
	private EntityManager entityManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.engining.pg.support.db.sequence.SequenceOperator#getNextValue(java.
	 * lang.String)
	 */
	@Override
	public BigDecimal getNextValue(String sequenceName) {
		// 临时DB2解决方案
		Object obj = entityManager.unwrap(Session.class)
				.createSQLQuery(
						MessageFormat.format("select nextval for {0} as seq from SYSIBM.SYSDUMMY1", sequenceName))
				.addScalar("seq", new BigDecimalType()).uniqueResult();
		assert obj instanceof BigDecimal;

		return (BigDecimal) obj;
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.db.sequence.SequenceOperator#getCode()
	 */
	@Override
	public DbType getCode() {
		return DbType.DB2;
	}

}
