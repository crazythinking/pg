SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS BT_IMPORT_EXCEPTION;
DROP TABLE IF EXISTS BT_SYS_CHECKLIST;
DROP TABLE IF EXISTS PG_FILE_ITEM;
DROP TABLE IF EXISTS PG_KEY_CONTEXT;




/* Create Tables */

-- 批量数据导入异常记录表
CREATE TABLE BT_IMPORT_EXCEPTION
(
	ID int NOT NULL AUTO_INCREMENT COMMENT '序号',
	BATCH_SEQ varchar(20) COMMENT '批次号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INSPECTION_CD varchar(20) COMMENT '检查项代码',
	FILE_PATH varchar(200) COMMENT '文件路径',
	EXCEPTION_MSG mediumtext COMMENT '异常信息',
	LINE_CONTENT mediumtext COMMENT '行内容',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ID),
	UNIQUE (ID)
) COMMENT = '批量数据导入异常记录表';


-- 批量系统检查项记录表
CREATE TABLE BT_SYS_CHECKLIST
(
	SEQ int NOT NULL AUTO_INCREMENT COMMENT '序号',
	BATCH_SEQ varchar(20) COMMENT '批次号',
	ORG varchar(12) COMMENT '机构号',
	BRANCH_NO varchar(9) COMMENT '分行号',
	INSPECTION_CD varchar(20) NOT NULL COMMENT '检查项代码',
	CHECK_LIST_DESC varchar(40) COMMENT '检查项描述',
	CHECK_LIST_TYPE varchar(20) COMMENT '检查项类型',
	CHECK_TIMES int COMMENT '待检查次数',
	-- ///
	-- @net.engining.pg.batch.enums.CheckStatusDef
	CHECK_STATUS varchar(16) COMMENT '状态 : ///
@net.engining.pg.batch.enums.CheckStatusDef',
	SKIPABLE boolean COMMENT '是否可跳过',
	-- ///
	-- @net.engining.pg.batch.enums.SkipConditionTypeDef
	SKIP_CONDITION_TYPE varchar(6) COMMENT '跳过条件类型 : ///
@net.engining.pg.batch.enums.SkipConditionTypeDef',
	SKIP_CON_MAX_COUNT int COMMENT '跳过检查最大次数',
	SKIP_CON_DEADLINE timestamp DEFAULT NOW() COMMENT '跳过检查终结时间',
	CHECK_BIZ_DATA mediumtext COMMENT '检查项业务数据',
	-- $$$@CreatedDate$$$
	SETUP_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '创建日期 : $$$@CreatedDate$$$',
	-- $$$@LastModifiedDate$$$
	LAST_UPDATE_DATE timestamp DEFAULT NOW() NOT NULL COMMENT '最后更新日期 : $$$@LastModifiedDate$$$',
	BIZ_DATE date NOT NULL COMMENT '系统业务日期',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (SEQ),
	UNIQUE (SEQ)
) COMMENT = '批量系统检查项记录表';


-- 批量处理文件数据行信息表
CREATE TABLE PG_FILE_ITEM
(
	ITEM_ID bigint NOT NULL AUTO_INCREMENT COMMENT 'ITEM_ID',
	BATCH_NUMBER varchar(50) COMMENT '批执行序号',
	FILENAME varchar(200) NOT NULL COMMENT '文件名',
	LINE text NOT NULL COMMENT '行数据',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (ITEM_ID)
) COMMENT = '批量处理文件数据行信息表';


-- 批量处理数据主键信息表
CREATE TABLE PG_KEY_CONTEXT
(
	CONTEXT_ID bigint NOT NULL AUTO_INCREMENT COMMENT 'CONTEXT_ID',
	-- !!!java.util.ArrayList!!!
	KEY_LIST blob NOT NULL COMMENT '主键列表 : !!!java.util.ArrayList!!!',
	SETUP_DATE timestamp COMMENT '数据产生时间',
	JPA_VERSION int NOT NULL COMMENT '乐观锁版本号',
	PRIMARY KEY (CONTEXT_ID)
) COMMENT = '批量处理数据主键信息表';



