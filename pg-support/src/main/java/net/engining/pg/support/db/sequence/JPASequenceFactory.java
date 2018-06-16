package net.engining.pg.support.db.sequence;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import net.engining.pg.support.core.context.ApplicationContextHolder;
import net.engining.pg.support.db.DbType;

//@Component
public class JPASequenceFactory {
	
	private static Map<DbType, SequenceOperator> sequenceOperatorMap;
	
	public JPASequenceFactory(){
		Map<String, SequenceOperator> map = ApplicationContextHolder.getBeansOfType(SequenceOperator.class);
		sequenceOperatorMap = Maps.newHashMap();
		map.forEach((key, value) -> sequenceOperatorMap.put(value.getCode(), value));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends SequenceOperator> T getSequenceOperator(DbType code){
		return (T)sequenceOperatorMap.get(code);
	}

	public static BigDecimal getNextValue(EntityManager em, DbType dbType, String sequenceName) {
			
		return JPASequenceFactory.getSequenceOperator(dbType).getNextValue(sequenceName);

	}
}
