package net.engining.pg.batch.sdk;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.transaction.annotation.Transactional;

/**
 * 这个类在处理断点时有问题，不应再使用
 * 
 * @author licj
 *
 * @param <T>
 */
@Deprecated
public class PgHibernateCursorReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements ItemStream {
	@PersistenceContext
	protected EntityManager em;

	private String queryString;

	private ScrollableResults cursor;

	private boolean clearOnUpdate = true;

	public PgHibernateCursorReader() {
		setName(getClass().getSimpleName());
	}

	/**
	 * 打开游标，默认实现使用 {@link #queryString}直接建游标
	 */
	@Transactional(rollbackFor = Exception.class)
	protected ScrollableResults openCursor() {
		if (StringUtils.isBlank(queryString)) {
			throw new IllegalArgumentException("使用默认实现时必须指定HQL");
		}
		return em.unwrap(Session.class).createQuery(queryString).scroll(ScrollMode.FORWARD_ONLY);
	}

	@Override
	protected void jumpToItem(int itemIndex) throws Exception {
		for (int i = 0; i < itemIndex; i++) {
			cursor.next();
			// 暂定1000，每1000条清一下
			if (i % 1000 == 0) {
				em.clear(); // Clears in-memory cache
			}
		}
	}

	@Override
	protected T doRead() throws Exception {
		// 这段复制自
		// org.springframework.batch.item.database.HibernateCursorItemReader<T>
		if (cursor.next()) {
			Object[] data = cursor.get();

			if (data.length > 1) {
				// If there are multiple items this must be a projection
				// and T is an array type.
				@SuppressWarnings("unchecked")
				T item = (T) data;
				return item;
			}
			else {
				// Assume if there is only one item that it is the data the user
				// wants.
				// If there is only one item this is going to be a nasty shock
				// if T is an array type but there's not much else we can do...
				@SuppressWarnings("unchecked")
				T item = (T) data[0];
				return item;
			}

		}

		return null;
	}

	@Override
	protected void doOpen() throws Exception {
		cursor = openCursor();
	}

	@Override
	protected void doClose() throws Exception {
		if (cursor != null) {
			cursor.close();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);
		// 调用基类后进行清理工作，以便在每次断点时清空一级缓存
		if (clearOnUpdate) {
			em.flush(); // TODO
						// 确定是否需要。org.springframework.batch.item.database.HibernateCursorItemReader<T>里是没有的
			em.clear();
		}
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
}
