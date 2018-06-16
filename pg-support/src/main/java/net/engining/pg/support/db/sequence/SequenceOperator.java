package net.engining.pg.support.db.sequence;

import java.math.BigDecimal;

import net.engining.pg.support.db.DbType;

public interface SequenceOperator {
	
	DbType getCode();
	
	BigDecimal getNextValue(String sequenceName);
	
}
