package net.engining.pg.batch.sdk.inter;

import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import net.engining.pg.batch.entity.model.BtSysChecklist;
import net.engining.pg.batch.enums.CheckStatusDef;
import net.engining.pg.batch.enums.SkipConditionTypeDef;
import net.engining.pg.batch.param.BatchSysCheck;
import net.engining.pg.parameter.ParameterFacility;

public class BtSysCheckListService implements BtSysCheckListInterface {
	
	@Autowired
	private ParameterFacility parameterCacheFacility;

	@PersistenceContext
	private EntityManager em;
	
	private String[] inspectionCds;

	/**
	 * @return the inspectionCds
	 */
	public String[] getInspectionCds() {
		return inspectionCds;
	}

	/**
	 * @param inspectionCds the inspectionCds to set
	 */
	public void setInspectionCds(String[] inspectionCds) {
		this.inspectionCds = inspectionCds;
	}

	@Override
	@Transactional
	public void initCheckList(Date bizDate, String batchSeq, String checkListType) {
		Map<String, BatchSysCheck> map = parameterCacheFacility.getParameterMap(BatchSysCheck.class);

		// 循环检查项枚举，找到枚举对应的初始化参数，添加数据
		for (String cd : inspectionCds) {
			BatchSysCheck cact = map.get(cd + "|" + checkListType);
			if (cact != null) {
				BtSysChecklist check = new BtSysChecklist();
				check.setInspectionCd(cact.inspectionCd);
				check.setCheckListDesc(cact.checkListDesc);
				check.setCheckListType(cact.checkListType);
				check.setCheckTimes(0);
				check.setCheckStatus(CheckStatusDef.WAIT);
				check.setSkipable(cact.skipable);
				check.setSkipConditionType(cact.skipConditionType);
				check.setSkipConMaxCount(cact.skipConMaxCount);
				check.setSkipConDeadline(cact.skipConDeadline);
				check.setBizDate(bizDate);
				check.setBatchSeq(batchSeq);
				check.fillDefaultValues();

				em.persist(check);
			}
		}

	}

	@Override
	@Transactional
	public void checkBtSysCheckListItem(Integer seq, CheckStatusDef checkStatusDef) {
		BtSysChecklist cactSysChecklist = em.find(BtSysChecklist.class, seq);
		if (checkStatusDef.equals(CheckStatusDef.SUCCESS)) {
			cactSysChecklist.setCheckTimes(cactSysChecklist.getCheckTimes() + 1);
			cactSysChecklist.setCheckStatus(CheckStatusDef.SUCCESS);
		}
		else {
			if (cactSysChecklist.getSkipable()) {
				if (SkipConditionTypeDef.COUNT.equals(cactSysChecklist.getSkipConditionType())) {
					if (cactSysChecklist.getCheckTimes() < cactSysChecklist.getSkipConMaxCount()) {
						cactSysChecklist.setCheckTimes(cactSysChecklist.getCheckTimes() + 1);
						 cactSysChecklist.setCheckStatus(CheckStatusDef.WAIT);
					}
					else {
						cactSysChecklist.setCheckStatus(CheckStatusDef.FAILED);
					}
				}
				Date time = new Date();
				if (SkipConditionTypeDef.TIME.equals(cactSysChecklist.getSkipConditionType())) {
					if (time.after(cactSysChecklist.getSkipConDeadline())) {
						cactSysChecklist.setCheckStatus(CheckStatusDef.FAILED);
					}
					else {
						cactSysChecklist.setCheckStatus(CheckStatusDef.WAIT);
					}
				}
			}
			else {
				cactSysChecklist.setCheckStatus(CheckStatusDef.WAIT);
			}
		}
	}
}
