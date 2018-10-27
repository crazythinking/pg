package net.engining.pg.support.db.sequence;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;

import net.engining.pg.support.db.DbType;

/**
 * Oracle 的Sequence Operator
 * @author luxue
 *
 */
//@Component// 基础包里的类不应该直接被扫描，交由需要的项目@Bean的方式创建
public class OracleSequenceOperator implements SequenceOperator {

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
		Object obj = entityManager.unwrap(Session.class)
				.createNativeQuery(MessageFormat.format("select {0}.nextval for dual", sequenceName))
				.addScalar("seq", new BigDecimalType()).uniqueResult();
		assert obj instanceof BigDecimal;
		return (BigDecimal) obj;
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.db.sequence.SequenceOperator#getCode()
	 */
	@Override
	public DbType getCode() {
		return DbType.Oracle;
	}

}
