package net.engining.pg.batch.sdk;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.ScrollableResults;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.hibernate.HibernateQuery;

/**
 * 
 * @author luxue
 *
 * @param <T>
 */
public abstract class AbstractPgCursorQueryReader<T> implements ItemStreamReader<T> {
	@PersistenceContext
	protected EntityManager em;

	private AbstractPgCursorHelper<T> cursorHelper;

	private T lastItem;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void open(final ExecutionContext executionContext) throws ItemStreamException {
		cursorHelper = new AbstractPgCursorHelper<T>() {
			@Override
			protected ScrollableResults doOpenCursor(HibernateQuery<T> query) {
				return AbstractPgCursorQueryReader.this.doOpenCursor(executionContext, query);
			}
		};
		cursorHelper.openCursor(em);
	}

	@Override
	public void close() throws ItemStreamException {
		if (cursorHelper != null) {
			cursorHelper.closeCursor();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		ScrollableResults cursor = cursorHelper.getCursor();

		// 这段复制自
		// org.springframework.batch.item.database.HibernateCursorItemReader<T>
		if (cursor.next()) {
			T item = null;
			Object[] data = cursor.get();

			if (data.length > 1) {
				// If there are multiple items this must be a projection
				// and T is an array type.
				item = (T) data;
			}
			else {
				// Assume if there is only one item that it is the data the user
				// wants.
				// If there is only one item this is going to be a nasty shock
				// if T is an array type but there's not much else we can do...
				item = (T) data[0];
			}
			lastItem = item;
			return item;
		}
		return null;
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		// 因为刚进去时就会调update，所以在最初lastItem为null
		if (lastItem != null) {
			doUpdate(executionContext, lastItem);
		}
	}

	/**
	 * 由子类负责打开游标
	 * 
	 * @param executionContext
	 * @param query
	 * @return
	 */
	protected abstract ScrollableResults doOpenCursor(ExecutionContext executionContext, HibernateQuery<T> query);

	/**
	 * 每次提交时，供子类把最后处理的一个对象的断点信息写入上下文
	 * 
	 * @param executionContext
	 * @param lastItem
	 *            最后一次处理的对象
	 */
	protected abstract void doUpdate(ExecutionContext executionContext, T lastItem);
}
