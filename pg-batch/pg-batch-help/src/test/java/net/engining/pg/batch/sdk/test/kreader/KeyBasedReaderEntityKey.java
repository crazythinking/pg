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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data1 == null) ? 0 : data1.hashCode());
		result = prime * result + ((data2 == null) ? 0 : data2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyBasedReaderEntityKey other = (KeyBasedReaderEntityKey) obj;
		if (data1 == null) {
			if (other.data1 != null)
				return false;
		} else if (!data1.equals(other.data1))
			return false;
		if (data2 == null) {
			if (other.data2 != null)
				return false;
		} else if (!data2.equals(other.data2))
			return false;
		return true;
	}

}
