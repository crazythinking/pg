package net.engining.pg.batch.param;

import java.io.Serializable;
import java.util.Date;

import net.engining.pg.batch.enums.CheckStatusDef;
import net.engining.pg.batch.enums.SkipConditionTypeDef;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 该参数用于配置批量任务执行前提条件的检查项
 * @author luxue
 *
 */
public class BatchSysCheck implements Serializable{

	private static final long serialVersionUID = 1L;
	
    /**
     * 检查项代码，参数编码，用于标识检查项
     */
    @PropertyInfo(name="检查项代码", length=20)
    public String inspectionCd;
	
	/**
     * 检查项描述
     */
    @PropertyInfo(name="检查项描述", length=40)
    public String checkListDesc;
    
    /**
     * 检查项类型，用于标识该检查项针对具体某个批量
     */
	@PropertyInfo(name="检查项类型", length=30)
    public String checkListType;
    
    
    /**
     * 是否可跳过
     */
    @PropertyInfo(name="是否可跳过", length=1)
    public Boolean skipable;
    
    /**
     * 跳过条件类型(时间TIME，次数COUNT)
     */
    @PropertyInfo(name="跳过条件类型", length=10)
    public SkipConditionTypeDef skipConditionType;

    /**
     * 跳过检查最大次数
     */
    @PropertyInfo(name="跳过检查最大次数", length=3)
    public int skipConMaxCount;

    /**
     * 跳过检查终结时间
     */
    @PropertyInfo(name="跳过检查终结时间", length=20)
    public Date skipConDeadline;
    
    public String getKey() {
    	return key(inspectionCd, checkListType);
    }
    
    public static String key(String inspectionCd, String checkListType) {
    	return inspectionCd + "|" + checkListType;
    }

}
