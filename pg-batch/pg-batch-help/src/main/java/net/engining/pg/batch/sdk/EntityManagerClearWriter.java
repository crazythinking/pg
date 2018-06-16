package net.engining.pg.batch.sdk;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;

/**
 * 用于清理 {@link EntityManager}缓存.
 * 因为 {@link PgBatchTransactionManager}不再使用，所以这个Writer也没有意义了。
 * @author licj
 *
 */
@Deprecated
public class EntityManagerClearWriter<T> implements ItemWriter<T>
{
	@PersistenceContext
	private EntityManager em;

	@Override
	public void write(List<? extends T> items) throws Exception {
		em.flush();
		em.clear();
	}

}
