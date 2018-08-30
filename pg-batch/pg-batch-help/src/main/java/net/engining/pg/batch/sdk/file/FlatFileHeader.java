package net.engining.pg.batch.sdk.file;

import java.io.Serializable;

/**
 * 交互文件头定义
 * @author luxue
 *
 */
public class FlatFileHeader implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum Type {
		/**
		 * 该类型表示只一个数字标识Header
		 */
		SimpleInteger,
		/**
		 * 该类型表示通过一行或多行String标识Header
		 */
		SimpleString,
		/**
		 * 该类型表示通过一个或多个Json Object标识Header
		 */
		JsonString,
	}
	
	/**
	 * 明细记录总行数
	 */
	private int totalLines;
	
	private String headContent;

	public int getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}

	/**
	 * @return the headContent
	 */
	public String getHeadContent() {
		return headContent;
	}

	/**
	 * @param headContent the headContent to set
	 */
	public void setHeadContent(String headContent) {
		this.headContent = headContent;
	}

}
