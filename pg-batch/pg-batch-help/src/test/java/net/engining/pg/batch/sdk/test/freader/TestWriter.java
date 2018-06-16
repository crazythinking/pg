package net.engining.pg.batch.sdk.test.freader;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;

import net.engining.pg.batch.sdk.LineItem;

/**
 * @author licj
 *
 */
public class TestWriter implements ItemWriter<LineItem<InputItem>> {
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public void write(List<? extends LineItem<InputItem>> items)
			throws Exception {
		for (LineItem<InputItem> item : items)
		{
			FileReaderEntity entity = new FileReaderEntity();
			entity.setData1(item.getLineObject().data1);
			em.persist(entity);
		}
	}


}
