package net.engining.pg.batch.sdk.test.kreader;

import java.io.Serializable;

/**
 * 
 * @author luxue
 *
 */
public class KeyBasedReaderEntityKey implements Serializable {
	private static final long serialVersionUID = 1L;

	private String data1;
	
	private String data2;
	
	public KeyBasedReaderEntityKey()
	{
		
	}
	
	public KeyBasedReaderEntityKey(String data1, String data2)
	{
		this.data1 = data1;
		this.data2 = data2;
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
