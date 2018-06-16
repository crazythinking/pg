package net.engining.pg.support.db.querydsl;

import java.io.Serializable;
import java.util.List;

public class FetchResponse<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private long rowCount;

	private List<T> data;

	private long start;

	private boolean exact = true;

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public boolean isExact() {
		return exact;
	}

	public void setExact(boolean exact) {
		this.exact = exact;
	}
}
