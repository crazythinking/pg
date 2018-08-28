package net.engining.pg.batch.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"TIME|\u65F6\u95F4"
,	"COUNT|\u6B21\u6570"
})
public enum SkipConditionTypeDef {
    /** 时间 */	TIME,
    /** 次数 */	COUNT;
}