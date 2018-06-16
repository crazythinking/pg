package net.engining.pg.support.service;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ronny
 *
 */
@Deprecated
public class QueryResult<T> implements Serializable {

	private static final long serialVersionUID = 2598447086724769620L;

	/**
     * 返回列表在结果集中的首行位置
     */
    private int firstRow;
    
    /**
     * 返回列表在结果集中的尾行位置
     */
    private int lastRow;
    
    
    /**
     * 查询结果集记录数
     */
    private int totalRows;
    
    
    /**
     * 响应码
     */
    private String resultcode;
    
    /**
     * 响应备注说明
     */
    private String memo;
    
    
    /**
     * 查询结果
     */
    private List<T> result;


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


	public int getTotalRows() {
		return totalRows;
	}


	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}


	public String getResultcode() {
		return resultcode;
	}


	public void setResultcode(String resultcode) {
		this.resultcode = resultcode;
	}


	public String getMemo() {
		return memo;
	}


	public void setMemo(String memo) {
		this.memo = memo;
	}


	public List<T> getResult() {
		return result;
	}


	public void setResult(List<T> result) {
		this.result = result;
	}
}
