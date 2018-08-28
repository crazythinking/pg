package net.engining.pg.batch.sdk.inter;

/**
 * 批量检查项处理接口，通常在批量执行前对检查项列表进行检查，决定是否可以执行批量
 * @author luxue
 *
 */
public interface CheckListCriterion {
	
	public boolean checkOk();
}
