/**
 * 
 */
package net.engining.pg.batch.sdk.infrastructure;

/**
 * 批量任务执行行为类型
 * @author luxue
 *
 */
public enum BatchJobType {

	/**
	 * 单个业务周期内不可重复执行的 Batch Job
	 */
	UNREPEATABLE_4_ONE_TERM,
	
	/**
	 * 单个业务周期内可重复执行的 Batch Job
	 */
	REPEATABLE_4_ONE_TERM
}
