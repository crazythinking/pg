package net.engining.pg.support.testcase;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

/**
 * 基于Spring架构的测试案例抽象父类；各项目包创建自己的测试案例抽象子类；<br>
 * 注：通常一个Test类即启动一个Spirng Context；
 * @author luxue
 *
 */
@RunWith(SpringRunner.class)
@TestExecutionListeners({ 
	DirtiesContextBeforeModesTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class, 
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class, 
	SqlScriptsTestExecutionListener.class
	})
public abstract class AbstractJUnit4SpringContextTestsWithoutServlet implements TestCase{
	
	private static final Logger log = LoggerFactory.getLogger(AbstractJUnit4SpringContextTestsWithoutServlet.class);
	
	/**
	 * 测试案例输入数据上下文
	 */
	public Map<String, Object> testIncomeDataContext;
	
	/**
	 * 用于对测试案例断言的数据上下文
	 */
	public Map<String, Object> testAssertDataContext;
	
	@Before
	@Transactional
	public void beginTest() throws Exception {
		log.info("开始实例化该测试案例上下文........");
		// init test case context
		this.testIncomeDataContext = Maps.newHashMap();
		this.testAssertDataContext = Maps.newHashMap();

		log.info("开始为该测试案例准备数据........");
		// 执行测试案例的准备数据
		initTestData();

	}

	@Test
	public void runTestCase() throws Exception{
		log.info("开始执行该测试案例........");
		//执行测试案例
		testProcess();
		
		log.info("断言该测试案例的结果........");
		//执行测试案例完成后结案结果
		assertResult();
	}
	
	@After
	public void endTestCase() throws Exception{
		log.info("该测试案例进行结束处理........");
		end();
	}
	
}
