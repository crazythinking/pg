package net.engining.pg.batch.sdk.test.kreader;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.engining.pg.batch.sdk.KeyBasedStreamReader;

/**
 * 
 * @author luxue
 *
 */
public class TestReader extends KeyBasedStreamReader<KeyBasedReaderEntityKey, KeyBasedReaderEntity> {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	protected List<KeyBasedReaderEntityKey> loadKeys() {

		return em.createQuery("select new net.engining.pg.batch.sdk.test.kreader.KeyBasedReaderEntityKey(t.data1, t.data2) from KeyBasedReaderEntity t").getResultList();
	}

	@Override
	protected KeyBasedReaderEntity loadItemByKey(KeyBasedReaderEntityKey key) {
		
		return em.find(KeyBasedReaderEntity.class, key);
	}
	
}
