package net.engining.pg.batch.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.util.ExecutionContextUserSupport;

/**
 * 用于将Item中属性作为items传递到代理的writers里的writer. 这样便于复用现有writer，如果是List属性，则加入所有的对象
 * 
 * @author licj
 *
 * @param <T>
 */
public class PropertyDelegateWriter<T> implements ItemStreamWriter<T> {

	private Map<String, ItemWriter<?>> writers;

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		for (ItemWriter<?> writer : writers.values()) {
			if (writer instanceof ItemStream) {
				((ItemStream) writer).open(executionContext);
			}
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		for (ItemWriter<?> writer : writers.values()) {
			if (writer instanceof ItemStream) {
				((ItemStream) writer).update(executionContext);
			}
		}
	}

	@Override
	public void close() throws ItemStreamException {
		for (ItemWriter<?> writer : writers.values()) {
			if (writer instanceof ItemStream) {
				((ItemStream) writer).close();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void write(List<? extends T> items) throws Exception {
		for (Entry<String, ItemWriter<?>> entry : writers.entrySet()) {
			// 按writer的维度来，把所有item的对应属性组成一个List传过去，更符合writer的语义
			List delegateItems = new ArrayList();
			for (T item : items) {
				Object prop = PropertyUtils.getSimpleProperty(item, entry.getKey());
				if (prop != null) {
					if (prop instanceof Collection) {
						delegateItems.addAll((Collection) prop);
					}
					else {
						delegateItems.add(prop);
					}
				}
			}
			entry.getValue().write(delegateItems);
		}
	}

	@PostConstruct
	public void init() {
		for (Entry<String, ItemWriter<?>> entry : writers.entrySet()) {
			ItemWriter<?> writer = entry.getValue();

			if (writer instanceof ExecutionContextUserSupport) {
				// 统一用key来改写writer的名字，以免出现未命名的bean id作name，并且随时间变化的情况
				((ExecutionContextUserSupport) writer).setName(entry.getKey());
			}
		}
	}

	public void setWriters(Map<String, ItemWriter<?>> writers) {
		this.writers = writers;
	}

}
