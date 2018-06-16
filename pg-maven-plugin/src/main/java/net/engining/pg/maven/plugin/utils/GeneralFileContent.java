package net.engining.pg.maven.plugin.utils;

/**
 * 通用文件描述，包含文件名、编码和内容
 * @author chenjun.li
 *
 */
public class GeneralFileContent {
	
	public static final String DEFAULT_ENCODING = "UTF-8";
	
	private String filename;
	
	private String content;
	
	private String encoding;
	
	public GeneralFileContent()
	{
	}
	
	public GeneralFileContent(String filename, String content) {
		this.filename = filename;
		this.content = content;
		this.encoding = DEFAULT_ENCODING;
	}
	
	public GeneralFileContent(String filename, String content, String encoding) {
		this.filename = filename;
		this.content = content;
		this.encoding = encoding;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
