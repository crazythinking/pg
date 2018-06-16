package net.engining.pg.batch.sdk.test.kreader;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;

/**
 * 
 * @author luxue
 *
 */
public class StatelessProcessor implements ItemProcessor<KeyBasedReaderEntity, KeyBasedReaderEntity> {
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public KeyBasedReaderEntity process(KeyBasedReaderEntity item) throws Exception {
		em.remove(item);
		return item;
	}

}
