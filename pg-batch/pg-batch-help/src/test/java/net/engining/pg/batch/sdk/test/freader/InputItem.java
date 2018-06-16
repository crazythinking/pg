package net.engining.pg.batch.sdk.test.freader;

import net.engining.pg.support.cstruct.CChar;

/**
 * 
 * @author luxue
 *
 */
public class InputItem {
	@CChar( value = 12, order = 100 )
	public String org;

	@CChar( value = 80, order = 200)
	public String data1;

	@CChar( value = 30, order = 300)
	public String data2;

}
