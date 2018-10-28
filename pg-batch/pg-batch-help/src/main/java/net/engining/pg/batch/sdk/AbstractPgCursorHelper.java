package net.engining.pg.batch.sdk;

import javax.persistence.EntityManager;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.querydsl.jpa.hibernate.HibernateQuery;;

/**
 * 用于处理游标的类
 * @author luxue
 *
 * @param <INFO>
 */
public abstract class AbstractPgCursorHelper<INFO> {
	protected ScrollableResults cursor;

	public void closeCursor() {
		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * 根据配置打开游标，按条件打开游标
	 */
	public ScrollableResults openCursor(EntityManager em) {
		HibernateQuery<INFO> hq = new HibernateQuery<INFO>(em.unwrap(Session.class));
		cursor = doOpenCursor(hq);
		return cursor;
	}

	/**
	 * 打开游标
	 * @param query
	 * @return
	 */
	protected abstract ScrollableResults doOpenCursor(HibernateQuery<INFO> query);

	public ScrollableResults getCursor() {
		return cursor;
	}
}