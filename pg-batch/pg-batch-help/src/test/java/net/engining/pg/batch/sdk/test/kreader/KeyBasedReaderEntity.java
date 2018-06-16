package net.engining.pg.batch.sdk.test.kreader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * 
 * @author luxue
 *
 */
@Entity
@IdClass(KeyBasedReaderEntityKey.class)
public class KeyBasedReaderEntity {

	@Id
	@Column
	private String data1;
	
	@Id
	@Column
	private String data2;
	
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
