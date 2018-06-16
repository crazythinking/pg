package net.engining.pg.batch.sdk;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

/**
 * 
 * @author luxue
 *
 * @param <T>
 */
public class NullItemWriter<T> implements ItemWriter<T> {

	@Override
	public void write(List<? extends T> items) throws Exception {
	}

}
