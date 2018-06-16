package net.engining.pg.support.test.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pg.support.test.support.AbstractTestCaseTemplate;

/**
 * @author luxue
 *
 */
public class SnowflakeIdGeneratorTest extends AbstractTestCaseTemplate{

	@PersistenceContext
	private EntityManager em;
	
	/* (non-Javadoc)
	 * @see net.engining.pg.support.test.TestCase#initTestData()
	 */
	@Override
	public void initTestData() {
		testIncomeDataContext.put("branch", "0000001");
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.test.TestCase#assertResult()
	 */
	@Override
	public void assertResult() {
		QPgIdTestEnt qPgIdTestEnt = QPgIdTestEnt.pgIdTest;
		String id = new JPAQueryFactory(em).select(qPgIdTestEnt.snowFlakeId).from(qPgIdTestEnt).where(qPgIdTestEnt.batchNumber.eq("0000001")).fetchOne();
		assertThat("查询的主键与暂存的主键不相等，表明主键未成功产生", id, equalTo(testAssertDataContext.get("id").toString()));
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.test.TestCase#testProcess()
	 */
	@Override
	@Transactional
	public void testProcess() {
		PgIdTestEnt pgIdTestEnt = new PgIdTestEnt();
		pgIdTestEnt.setBatchNumber("0000001");
		em.persist(pgIdTestEnt);
		
		testAssertDataContext.put("id", pgIdTestEnt.getSnowFlakeId());
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.test.TestCase#end()
	 */
	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}


}
