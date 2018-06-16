package net.engining.pg.batch.sdk.file;

import org.springframework.batch.item.file.LineCallbackHandler;

import com.alibaba.fastjson.JSON;

/**
 * 针对Json交互文件的LineCallbackHandler，用来处理文件的Header Line
 * @author luxue
 *
 */
public class JsonFileHeaderLineCallbackHandler<H extends FlatFileHeader> implements LineCallbackHandler{
	
	private Class<H> fileHeaderClass;
	
	private H header;

	@Override
	public void handleLine(String line) {
		header = JSON.parseObject(line, fileHeaderClass);
		
	}

	public H getHeader() {
		return header;
	}

}
