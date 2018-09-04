package net.engining.pg.batch.sdk.inter;

import java.util.Date;

import net.engining.pg.batch.enums.CheckStatusDef;

/**
 * 批量系统检查项服务接口，由具体的业务端根据实际实现
 * @author luxue
 *
 */
public interface BtSysCheckListInterface {

	/**
	 * 用于每个批量周期根据检查项参数遍历生成相应检查类型的检查项记录；
	 * 
	 * @param bizDate
	 * @param batchSeq
	 * @param checkListType	通常由下游业务项目制定枚举类
	 */
	public void initCheckList(Date bizDate, String batchSeq, String checkListType);
	
	/**
	 * 根据主键定位到某个检查项，并根据该数据决定最终检查项状态，然后更新该数据
	 * @param seq
	 */
	public void checkBtSysCheckListItem(Integer seq, CheckStatusDef checkStatusDef);
}
