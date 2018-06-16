package net.engining.pg.batch.sdk.test.freader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 
 * @author luxue
 *
 */
@Entity
public class FileReaderEntity {

	@Id
	@Column
	private String data1;
	
	public String getData1() {
		return data1;
	}

	public void setData1(String data1) {
		this.data1 = data1;
	}
}
