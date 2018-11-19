package net.engining.pg.support.db.id.generator;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import net.engining.pg.support.core.exception.ErrorCode;

public abstract class AbstractDataSourceBlockIdGenService extends AbstractDataSourceIdGenService
		implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDataSourceBlockIdGenService.class);

	private BigDecimal mFirstBigDecimal;

	private long mFirstLong;

	private int mBlockSize = 10;

	private int mAllocated;

	protected abstract BigDecimal allocateBigDecimalIdBlock(String tableName, int blockSize);

	protected abstract long allocateLongIdBlock(String tableName, int blockSize);

	protected BigDecimal getNextIdInnerBigDecimal(String tableName) {
		if (mAllocated >= mBlockSize) {
			// Need to allocate a new batch of ids
			try {
				mFirstBigDecimal = allocateBigDecimalIdBlock(tableName, mBlockSize);

				// Reset the allocated count
				mAllocated = 0;
			} catch (IdGenException ex) {
				// Set the allocated count to signal that there are not any ids
				// available.
				mAllocated = Integer.MAX_VALUE;
				throw ex;
			}
		}

		// We know that at least one id is available.
		// Get an id out of the currently allocated block.
		BigDecimal id = mFirstBigDecimal.add(new BigDecimal(new Integer(mAllocated).doubleValue()));
		mAllocated++;
		return id;
	}

	protected BigDecimal getNextIdInnerBigDecimal() {
		return getNextIdInnerBigDecimal("");
	}

	protected long getNextIdInnerLong() {
		return getNextIdInnerLong("");
	}

	protected long getNextIdInnerLong(String tableName) {
		if (mAllocated >= mBlockSize) {
			// Need to allocate a new batch of ids
			try {
				mFirstLong = allocateLongIdBlock(tableName, mBlockSize);

				// Reset the allocated count
				mAllocated = 0;
			} catch (IdGenException ex) {
				// Set the allocated count to signal that there are not any ids
				// available.
				mAllocated = Integer.MAX_VALUE;
				throw ex;
			}
		}

		// We know that at least one id is available.
		// Get an id out of the currently allocated block.
		long id = mFirstLong + mAllocated;
		if (id < 0) {
			// The value wrapped
			logger.error(
					" Unable to provide an id.   No more Ids are available, the maximum Long value has been reached.");
			throw new IdGenException(ErrorCode.CheckError,
					"Unable to provide an id.   No more Ids are available, the maximum Long value has been reached.");
		}
		mAllocated++;

		return id;
	}

	public void setBlockSize(int blockSize) {
		this.mBlockSize = blockSize;
	}

	public void afterPropertiesSet() {
		mAllocated = Integer.MAX_VALUE;
	}
}
