package net.engining.pg.support.db.id.generator;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engining.pg.support.core.exception.ErrorCode;

public abstract class AbstractIdGenService implements IdGenService {


	private static final Logger logger = LoggerFactory.getLogger(AbstractIdGenService.class);

    private static final BigDecimal BIG_DECIMAL_MAX_LONG = new BigDecimal(new Long(Long.MAX_VALUE).doubleValue());

    /**
     * 同步信号
     */
    private final Object mSemaphore = new Object();

    // ID生成策略
    private IdGenStrategy strategy = new IdGenStrategy() {
        public String makeId(String originalId, Class<?> clazz) {
            return originalId;
        }
    };

    private boolean mUseBigDecimals = false;

    public AbstractIdGenService() {
    }

    protected abstract BigDecimal getNextIdInnerBigDecimal();

    protected abstract BigDecimal getNextIdInnerBigDecimal(String tableName);

    protected abstract long getNextIdInnerLong();

    protected abstract long getNextIdInnerLong(String tableName);

    public void setUseBigDecimals(boolean useBigDecimals) {
        mUseBigDecimals = useBigDecimals;
    }

    protected final boolean isUsingBigDecimals() {
        return mUseBigDecimals;
    }

    protected final long getNextLongIdChecked(long maxId) {
        long nextId;
        if (mUseBigDecimals) {
            BigDecimal bd;
            synchronized (mSemaphore) {
                bd = getNextIdInnerBigDecimal();
            }

            if (bd.compareTo(BIG_DECIMAL_MAX_LONG) > 0) {
                logger.error("Unable to provide an id.   No more Ids are available, the maximum Long value has been reached.");
                throw new IdGenException(ErrorCode.CheckError, "Unable to provide an id.   No more Ids are available, the maximum Long value has been reached.");
            }
            nextId = bd.longValue();
        } else {
            synchronized (mSemaphore) {
                nextId = getNextIdInnerLong();
            }
        }

        if (nextId > maxId) {
            logger.error("Unable to provide an id.   No more Ids are available, the maximum Long value has been reached.");
            throw new IdGenException(ErrorCode.CheckError, "Unable to provide an id.   No more Ids are available, the maximum Long value has been reached.");
        }

        return nextId;
    }

    protected final BigDecimal getNextBigDecimalId(String tableName) {
        BigDecimal bd;
        if (mUseBigDecimals) {
            synchronized (mSemaphore) {
                bd = getNextIdInnerBigDecimal(tableName);
            }
        } else {
            synchronized (mSemaphore) {
                bd = new BigDecimal(new Long(getNextIdInnerLong(tableName)).doubleValue());
            }
        }

        return bd;
    }

    public final BigDecimal getNextIdBigDecimal() {
        return getNextBigDecimalId("");
    }

    public final long getNextIdLong() {
        return getNextLongIdChecked(Long.MAX_VALUE);
    }

    public final int getNextIdInteger() {
        return (int) getNextLongIdChecked(Integer.MAX_VALUE);
    }

    public final short getNextIdShort() {
        return (short) getNextLongIdChecked(Short.MAX_VALUE);
    }

    public final byte getNextIdByte() {
        return (byte) getNextLongIdChecked(Byte.MAX_VALUE);
    }

    public final String getNextIdString() {
        return getNextIdString("", null);
    }

    public String getNextIdString(String tableName) {
        return getNextIdString(tableName, null);
    }

    public String getNextIdString(Class<?> clazz) {
        return getNextIdString("", clazz);
    }

    public String getNextIdString(String tableName, Class<?> clazz) {
        return strategy.makeId(getNextBigDecimalId(tableName).toString(), clazz);
    }

    public IdGenStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(IdGenStrategy strategy) {
        this.strategy = strategy;
    }
}
