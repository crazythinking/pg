package net.engining.pg.batch.sdk.test.kreader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;

import net.engining.pg.batch.sdk.test.suport.AbstractTestCase;


/**
 * 针对KeyBasedReader的单元测试;<br>
 * 这个类目前不支持方法间并发
 * @author licj
 *
 */
//在TestCase类之后，关联的ApplicationContext将被标记为脏；在每一个TestCase类上标记该策略；
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class KeyBasedReaderTest extends AbstractTestCase{
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private BreakDownProcessor processor;
	
	@Test
	public void statelessReader() throws Exception
	{
		prepareEntities(1243);
		jobLauncherTestUtils.launchStep("statelessReaderStep");
		validate();
	}
	
	@Test
	public void normalReader() throws Exception
	{
		//生成一组随机数据
		prepareEntities(1243);
		//先测试不并发的情况，Writer里会把读到的记录删了
		jobLauncherTestUtils.launchStep("normalReader");
		//验证比较结果，只要验证数据库表是空的就行，下同
		validate();
		
	}
	
	/**
	 * 分别测试1个并发和7个并发的情况
	 * @throws Exception
	 */
	@Test
	public void partitionReader1() throws Exception
	{
		//1个
		prepareEntities(1243);
		jobLauncherTestUtils.launchStep("partitionReader1");
		//验证比较结果
		validate();
	}
	
	@Test
	public void partitionReader7() throws Exception
	{
		//7个
		prepareEntities(1243);
		jobLauncherTestUtils.launchStep("partitionReader7");
		//验证比较结果
		validate();
	}
	
	@Test
	public void partitionReader100() throws Exception
	{
		//记录数比网格数还少
		prepareEntities(50);
		//运行100个的网格
		jobLauncherTestUtils.launchStep("partitionReader100");
		//验证比较结果
		validate();
	}
	
	/**
	 * 第一个chunk就断点，不并发
	 */
	@Test
	public void breakPointAtFirstChunk()
	{
		prepareEntities(1243);
		//把断点设上
		processor.setBreakDownCounter(5);
		
		JobParametersBuilder jpb = new JobParametersBuilder().addString("name", "breakPointAtFirstChunk");
		jobLauncherTestUtils.launchStep("normalReader", jpb.toJobParameters());
		//应该会断批，但不会抛异常，然后把断点拿走
		processor.setBreakDownCounter(-1);
		jobLauncherTestUtils.launchStep("normalReader", jpb.toJobParameters());
		
		validate();
	}

	/**
	 * 中间chunk断点，不并发
	 */
	@Test
	public void breakPointAtMiddleChunk()
	{
		prepareEntities(1243);
		//把断点设上
		processor.setBreakDownCounter(500);
		
		JobParametersBuilder jpb = new JobParametersBuilder().addString("name", "breakPointAtMiddleChunk");
		jobLauncherTestUtils.launchStep("normalReader", jpb.toJobParameters());
		//应该会断批，但不会抛异常，然后把断点拿走
		processor.setBreakDownCounter(-1);
		jobLauncherTestUtils.launchStep("normalReader", jpb.toJobParameters());
		
		validate();
	}

	/**
	 * 中间chunk断点，并发
	 */
	@Test
	public void breakPointAtMiddleChunkPartition()
	{
		prepareEntities(1243);
		//把断点设上
		processor.setBreakDownCounter(200);
		
		JobParametersBuilder jpb = new JobParametersBuilder().addString("name", "breakPointAtMiddleChunkPartition");
		jobLauncherTestUtils.launchStep("partitionReader7", jpb.toJobParameters());
		//应该会断批，但不会抛异常，然后把断点拿走
		processor.setBreakDownCounter(-1);
		jobLauncherTestUtils.launchStep("partitionReader7", jpb.toJobParameters());
		
		validate();
	}

	private void validate() {
		assertThat("数据库表多记录，表明不是所有记录都被处理", (Long)em.createQuery("select count(*) from KeyBasedReaderEntity").getSingleResult(), equalTo(0L));
	}

	@Transactional(rollbackFor = Exception.class)
	private void prepareEntities(int n) {
		for (int i = 0; i<n; i++)
		{
			KeyBasedReaderEntity entity = new KeyBasedReaderEntity();
			entity.setData1(UUID.randomUUID().toString());
			entity.setData2(UUID.randomUUID().toString());
			em.persist(entity);
		}
	}
	
}
