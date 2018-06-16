package net.engining.pg.batch.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * 简单把 {@link LineItem}中的Object扒出来的处理器，并且校验行是否有效。
 * 如果无效，视 {@link #throwInvalidException}设置，如果为true就简单抛异常，否则返回null，中止当前记录处理。
 * {@link #throwInvalidException}默认为true。
 * @author licj
 *
 */
public class SimpleExtractorProcessor<T> implements ItemProcessor<LineItem<T>, T> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean throwInvalidException = true;

	@Override
	public T process(LineItem<T> item) throws Exception {
		if (item.isValid())
		{
			return item.getLineObject();
		}
		else
		{
			if (throwInvalidException)
			{
				throw new IllegalArgumentException("无效记录，行号：" + item.getLineNumber());
			}
			else
			{
				logger.warn("无效记录，行号：" + item.getLineNumber());
				return null;
			}
		}
	}

	protected boolean isThrowInvalidException() {
		return throwInvalidException;
	}

	protected void setThrowInvalidException(boolean throwInvalidException) {
		this.throwInvalidException = throwInvalidException;
	}
}
