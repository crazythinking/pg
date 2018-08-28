package net.engining.pg.batch.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"WAIT|\u5F85\u5904\u7406"
,	"SUCCESS|\u5904\u7406\u6210\u529F"
,	"FAILED|\u5904\u7406\u5931\u8D25"
})
public enum CheckStatusDef {
    /** 待处理 */	WAIT,
    /** 处理成功 */	SUCCESS,
    /** 处理失败 */	FAILED;
}