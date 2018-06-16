package net.engining.pg.batch.sdk.test.kreader;

import org.springframework.batch.item.ItemProcessor;

/**
 * 
 * @author luxue
 *
 */
public class BreakDownProcessor implements ItemProcessor<KeyBasedReaderEntity, KeyBasedReaderEntity> {

	private int breakDownCounter = -1;

	@Override
	public synchronized KeyBasedReaderEntity process(KeyBasedReaderEntity item) throws Exception {
		// 用于提供断点测试
		if (breakDownCounter < 0) {
			return item;
		}
		if (breakDownCounter == 0) {
			throw new BreakDownException();
		}
		breakDownCounter--;
		return item;
	}

	public int getBreakDownCounter() {
		return breakDownCounter;
	}

	public void setBreakDownCounter(int breanDownCounter) {
		this.breakDownCounter = breanDownCounter;
	}

}
