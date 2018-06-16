package net.engining.pg.batch.sdk;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;

/**
 * 
 * @author luxue
 *
 * @param <T>
 */
public class JpaItemPersistWriter<T> implements ItemWriter<T> {

	@PersistenceContext
	private EntityManager em;

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T item : items) {
			if (item instanceof List<?>) {
				for (Object sub : (List<?>) item) {
					em.persist(sub);
				}
			}
			else {
				em.persist(item);
			}
		}
	}

}
