package net.engining.pg.batch.sdk;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * 基于Key的无状态简单Reader，适合于带处理状态位的记录或是处理完后删除的记录场景,不支持断点续批，不支持并发；
 * 
 * @author licj
 *
 * @param <KEY>
 * @param <INFO>
 */
public abstract class AbstractKeyBasedReader<KEY, INFO> implements ItemReader<INFO> {

	/**
	 * 加载要处理记录的Keys
	 * @return
	 */
	protected abstract List<KEY> loadKeys();

	/**
	 * 根据记录的Key，加载记录数据
	 * @param key
	 * @return
	 */
	protected abstract INFO loadItemByKey(KEY key);

	private Iterator<KEY> keyIterator;

	@Override
	public INFO read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (keyIterator == null) {
			keyIterator = loadKeys().iterator();
		}

		if (!keyIterator.hasNext()) {
			// 清除引用
			keyIterator = null;
			return null;
		}
		return loadItemByKey(keyIterator.next());
	}

}
