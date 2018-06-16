SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS PG_FILE_ITEM;
DROP TABLE IF EXISTS PG_KEY_CONTEXT;




/* Create Tables */

-- 批量处理文件数据行信息表
CREATE TABLE PG_FILE_ITEM
(
	ITEM_ID bigint NOT NULL AUTO_INCREMENT COMMENT 'ITEM_ID',
	BATCH_NUMBER varchar(50) COMMENT '批执行序号',
	FILENAME varchar(200) NOT NULL COMMENT '文件名',
	LINE text NOT NULL COMMENT '行数据',
	PRIMARY KEY (ITEM_ID)
) COMMENT = '批量处理文件数据行信息表';


-- 批量处理数据主键信息表
CREATE TABLE PG_KEY_CONTEXT
(
	CONTEXT_ID bigint NOT NULL AUTO_INCREMENT COMMENT 'CONTEXT_ID',
	-- !!!java.util.ArrayList!!!
	KEY_LIST blob NOT NULL COMMENT '主键列表 : !!!java.util.ArrayList!!!',
	SETUP_DATE timestamp COMMENT '数据产生时间',
	PRIMARY KEY (CONTEXT_ID)
) COMMENT = '批量处理数据主键信息表';



