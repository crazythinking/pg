package net.engining.pg.batch.sdk.test.kreader;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;

/**
 * 
 * @author luxue
 *
 */
public class TestWriter implements ItemWriter<KeyBasedReaderEntity>
{
	@PersistenceContext
	private EntityManager em;

	@Override
	public void write(List<? extends KeyBasedReaderEntity> items) throws Exception {
		//这里删掉读到的记录
		for (KeyBasedReaderEntity entity : items)
		{
			em.remove(entity);
		}
	}
}
