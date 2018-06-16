package net.engining.pg.batch.sdk;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * 
 * @author Heyu.wang
 */
public class CompositeItemStreamReader implements ItemStreamReader<LineItem<?>> {
	private List<ItemStreamReader<LineItem<?>>> readers;

	private static final String EXECUTION_KEY = "compositeItemStreamReader.readerIndex";

	private int readerIndex;

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		readerIndex = executionContext.getInt(EXECUTION_KEY, 0);

		for (ItemStreamReader<LineItem<?>> reader : readers) {
			reader.open(executionContext);
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.putInt(EXECUTION_KEY, readerIndex);

		for (ItemStreamReader<LineItem<?>> reader : readers) {
			reader.update(executionContext);
		}
	}

	@Override
	public void close() throws ItemStreamException {
		for (ItemStreamReader<LineItem<?>> reader : readers) {
			reader.close();
		}
	}

	@Override
	public LineItem<?> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		while (readerIndex < readers.size()) {
			LineItem<?> item = readers.get(readerIndex).read();
			if (item != null) {
				return item;
			}
			readerIndex++;
		}
		return null;
	}

	@Required
	public void setReaders(List<ItemStreamReader<LineItem<?>>> readers) {
		this.readers = readers;
	}
}
