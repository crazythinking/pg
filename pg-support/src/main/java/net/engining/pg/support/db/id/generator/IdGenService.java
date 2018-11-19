package net.engining.pg.support.db.id.generator;

import java.math.BigDecimal;

/**
 * ID号生成服务接口.
 */
public interface IdGenService {

    BigDecimal getNextIdBigDecimal() throws IdGenException;

    long getNextIdLong() throws IdGenException;

    int getNextIdInteger() throws IdGenException;

    short getNextIdShort() throws IdGenException;

    byte getNextIdByte() throws IdGenException;

    String getNextIdString() throws IdGenException;

    String getNextIdString(String tableName) throws IdGenException;

    String getNextIdString(Class<?> clazz) throws IdGenException;

    String getNextIdString(String tableName, Class<?> clazz) throws IdGenException;
}
