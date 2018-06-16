
/* Drop Tables */

DROP TABLE PG_FILE_ITEM;
DROP TABLE PG_KEY_CONTEXT;




/* Create Tables */

-- 批量处理文件数据行信息表
CREATE TABLE PG_FILE_ITEM
(
	-- ITEM_ID
	ITEM_ID bigint NOT NULL,
	-- 批执行序号
	BATCH_NUMBER varchar(50),
	-- 文件名
	FILENAME varchar(200) NOT NULL,
	-- 行数据
	LINE clob NOT NULL,
	PRIMARY KEY (ITEM_ID)
);


-- 批量处理数据主键信息表
CREATE TABLE PG_KEY_CONTEXT
(
	-- CONTEXT_ID
	CONTEXT_ID bigint NOT NULL,
	-- 主键列表 : !!!java.util.ArrayList!!!
	KEY_LIST blob NOT NULL,
	-- 数据产生时间
	SETUP_DATE timestamp,
	PRIMARY KEY (CONTEXT_ID)
);



