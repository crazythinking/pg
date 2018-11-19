package net.engining.pg.support.db.id.generator;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import net.engining.pg.support.core.exception.ErrorCode;

/**
 * TODO 改为使用jdbcTemplate基于表生成.
 */
public class TableIdGenService extends AbstractDataSourceBlockIdGenService {

	private static final Logger logger = LoggerFactory.getLogger(TableIdGenService.class);

	private String mTable = "ids";

	private String mTableName = "id";

	private String keyColumn = "table_name";

	private String nextValueColumn = "next_id";

	public TableIdGenService() {
	}

	/**
	 * @param tableName
	 *            表名
	 * @param blockSize
	 *            单次block大小
	 * @param useBigDecimals
	 * @return
	 */
	private Object allocateIdBlock(String tableName, int blockSize, boolean useBigDecimals) {
		tableName = (("".equals(tableName)) ? mTableName : tableName);
		logger.debug("Allocating a new block of {} ids for {}.", new Object[] { new Integer(blockSize), tableName });
		try {
			Connection conn = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rs = null;
				try {
					// Try to get a block without using transactions. This makes
					// this code portable, but works on the
					// assumption that requesting blocks of ids is a fairly rare
					// thing.
					int tries = 0;
					while (tries < 50) {
						Object oldNextId = null;
						// Find out what the next available id is.
						String query = "SELECT " + nextValueColumn + " FROM " + mTable + " WHERE " + keyColumn + " = '"
								+ tableName + "'";
						rs = stmt.executeQuery(query);

						if (!rs.next()) {
							try {
								query = "INSERT INTO " + mTable + "(" + keyColumn + ", " + nextValueColumn
										+ ") VALUES('" + tableName + "', '1')";
								int inserted = stmt.executeUpdate(query);

								if (inserted < 1) {
									logger.debug("no rows in '{}' being inserted.", mTable);
									tries++;
									continue;
								}
							} catch (SQLException e) {
								logger.warn(
										"Encountered an exception attempting to insert the '{}'.  May be a transaction conflict. Try again.",
										mTable, e);

								tries++;
								continue;
							}

							oldNextId = (useBigDecimals) ? new BigDecimal(1) : new Long(1);
						} else {
							oldNextId = (useBigDecimals) ? rs.getBigDecimal(1) : rs.getLong(1);
						}

						Object nextId;
						Object newNextId;
						// Get the next_id using the appropriate data type.
						if (useBigDecimals) {
							newNextId = ((BigDecimal) oldNextId)
									.add(new BigDecimal(new Integer(blockSize).doubleValue()));
							nextId = oldNextId;
						} else {
							newNextId = new Long((Long) oldNextId + blockSize);
							nextId = (Long) oldNextId;
						}

						// Update the value of next_id in the database so it
						// reflects the full block
						// being allocated. If another process has done the same
						// thing, then this
						// will either throw an exception due to transaction
						// isolation or return
						// an update count of 0. In either case, we will need to
						// try again.
						try {
							// Need to quote next_id values so that MySQL
							// handles large BigDecimals correctly.
							query = "UPDATE " + mTable + " SET " + nextValueColumn + " = " + newNextId + " WHERE "
									+ keyColumn + " = '" + tableName + "' " + "AND " + nextValueColumn + " = " + nextId
									+ "";
							int updated = stmt.executeUpdate(query);
							if (updated >= 1) {
								// Update was successful.
								// 2009.10.08 - without handling connection
								// directly
								// if (!autoCommit) { conn.commit(); }
								// Return the next id
								// obtained above.
								return nextId;
							} else {
								// 可能事务冲突，重试一次
								logger.debug("Update resulted in no rows being changed.");
							}
						} catch (SQLException e) {
							// Assume that this was
							// caused by a transaction
							// conflict. Try again.
							// Just show the exception
							// message to keep the
							// output small.

							logger.warn(
									"Encountered an exception attempting to update the '{}'.  May be a transaction conflict. Try again. ",
									mTable, e);

						}

						// If we got here, then we
						// failed, roll back the
						// connection so we can
						// try again.
						// 2009.10.08 - without handling connection directly
						// if (!autoCommit) { conn.rollback(); }
						tries++;
					}

					// If we got here then we ran out
					// of tries.
					logger.error("Although too many retries, unable to allocate a block of Ids.");
					return null;
				} finally {
					if (rs != null) {
						JdbcUtils.closeResultSet(rs);
					}
					if (stmt != null) {
						JdbcUtils.closeStatement(stmt);
					}
				}
			} finally {
				if (conn != null) {
					DataSourceUtils.releaseConnection(conn, getDataSource());
				}
			}
		} catch (Exception e) {
			logger.error("Although too many retries, unable to allocate a block of Ids.", e);
			throw new IdGenException(ErrorCode.SystemError, "Although too many retries, unable to allocate a block of Ids.", e);
		}
	}

	protected BigDecimal allocateBigDecimalIdBlock(String tableName, int blockSize) {
		return (BigDecimal) allocateIdBlock(tableName, blockSize, true);
	}

	protected long allocateLongIdBlock(String tableName, int blockSize) {
		Long id = (Long) allocateIdBlock(tableName, blockSize, false);

		return id.longValue();
	}

	public void setTable(String table) {
		this.mTable = table;
	}

	public void setKey(String key) {
		this.mTableName = key;
	}

	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}

	public void setNextValueColumn(String nextValueColumn) {
		this.nextValueColumn = nextValueColumn;
	}
}
