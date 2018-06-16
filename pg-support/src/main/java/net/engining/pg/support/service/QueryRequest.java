package net.engining.pg.support.service;

import java.io.Serializable;

/**
 * 分页查询请求对象
 * @author Ronny
 *
 */
@Deprecated
public class QueryRequest implements Serializable{

	private static final long serialVersionUID = -7181259448184722753L;

	/**
	 * 请求结果集的首行行号
	 */
	private int firstRow = 0;
	
	/**
	 * 请求结果集的尾行行号
	 */
	private int lastRow = -1;
	
	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getLastRow() {
		return lastRow;
	}

	public void setLastRow(int lastRow) {
		this.lastRow = lastRow;
	}

}
