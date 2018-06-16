package net.engining.pg.batch.sdk.test.freader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;

import net.engining.pg.batch.sdk.FileHeader;
import net.engining.pg.batch.sdk.PgFileResourceMock;
import net.engining.pg.batch.sdk.test.suport.AbstractTestCase;

/**
 * 针对PgFileReader的单元测试;<br>
 * @author luxue
 *
 */
//在TestCase类之后，关联的ApplicationContext将被标记为脏；在每一个TestCase类上标记该策略；
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class PgFileReaderTest extends AbstractTestCase{
	
	@Resource
	private PgFileResourceMock<FileHeader, InputItem> input;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Test
	public void fileReader() throws Exception
	{
		//生成一组随机数据
		InputItem[] items = prepareItems(1243);
		input.prepare(items);
		
		//先测试不并发的情况
		jobLauncherTestUtils.launchStep("fnormalReader");
		//验证比较结果
		validate(items);
		
		
		//分别测试1个并发和7个并发的情况
		//1个
		jobLauncherTestUtils.launchStep("fpartitionReader1");
		//验证比较结果
		validate(items);
		
		//7个
		jobLauncherTestUtils.launchStep("fpartitionReader7");
		//验证比较结果
		validate(items);
		
		//重新准备文件，目的是记录数比网格数还少
		items = prepareItems(50);
		input.prepare(items);
		//运行100个的网格
		jobLauncherTestUtils.launchStep("fpartitionReader100");
		//验证比较结果
		validate(items);
	}
	
	private InputItem[] prepareItems(int n) {
		InputItem[] items = new InputItem[n]; 
		for (int i = 0; i<n; i++)
		{
			items[i] = new InputItem();
			items[i].org = "00000";
			items[i].data1 = UUID.randomUUID().toString();
		}
		return items;
	}
	
	@Transactional(rollbackFor = Exception.class)
	private void validate(InputItem items[])
	{
		Long count = (Long)em.createQuery("select count(*) from FileReaderEntity").getSingleResult();
		
		assertThat("数据库结果数目不相等", count.intValue(), equalTo(items.length));
		for (int i = 0; i<items.length; i++)
		{
			FileReaderEntity entity = em.find(FileReaderEntity.class, items[i].data1);
			assertThat("文件第" + i + "项没有找到", entity, notNullValue());
			em.remove(entity);
		}
		assertTrue("最后数据库多数据", em.createQuery("from FileReaderEntity").getResultList().isEmpty());
	}
}
