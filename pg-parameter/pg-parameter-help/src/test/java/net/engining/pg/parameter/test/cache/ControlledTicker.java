package net.engining.pg.parameter.test.cache;

import com.google.common.base.Ticker;

/**
 * 测试Ticker
 * @author binarier
 *
 */
public class ControlledTicker extends Ticker
{
	private long value = 0;
	
	@Override
	public long read() {

		return value;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
}
