package net.engining.pg.batch.sdk.infrastructure;

import net.engining.pg.batch.sdk.file.FlatFileHeader;

/**
 * 针对文件头解析处理的逻辑接口
 * @author luxue
 *
 */
public interface FileHeaderLineParser {

	public FlatFileHeader parser(FlatFileHeader fileHeader);
}
