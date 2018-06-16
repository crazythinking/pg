package net.engining.pg.batch.sdk.test.freader;

import java.io.Serializable;

/**
 * 
 * @author luxue
 *
 */
public class FlatInputItem implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public String org;

	public String data1;

	public String data2;

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getData1() {
		return data1;
	}

	public void setData1(String data1) {
		this.data1 = data1;
	}

	public String getData2() {
		return data2;
	}

	public void setData2(String data2) {
		this.data2 = data2;
	}

}
