package net.engining.pg.batch.sdk.test.suport;

import org.h2.tools.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import net.engining.pg.batch.sdk.test.BatchTestApplication;
import net.engining.pg.support.core.context.ApplicationContextHolder;

/**
 * 用于每个TestCase类独享Spring容器
 * 
 * @author luxue
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BatchTestApplication.class)
// 用于指定在测试类执行之前可以做的一些动作，如这里的DependencyInjectionTestExecutionListener.class，就可以对一测试类中的依赖进行注入，TransactionalTestExecutionListener.class用于对事务进行管理；
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public abstract class AbstractTestCase {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractTestCase.class);

	/**
	 * 初始化整个单元测试周期内必须初始化的数据，通常包括参数和数据;<br>
	 * 这里可以考虑与init项目同步，确保测试的初始化与上线的初始化一致;
	 * 
	 * @throws Exception
	 */
	@BeforeClass // 所有测试案例之前，只执行一次，且必须为static void
	@Transactional
	public static void beforeTest() throws Exception {
		//TODO
	}
	
	@After
	public void afterTestCase(){
		
	}

	/**
	 * 
	 * @throws Exception
	 */
	@AfterClass // 所有测试案例之后，只执行一次，且必须为static void
	public static void afterTest() throws Exception {
		
		Server h2tcp = ApplicationContextHolder.getBean("h2tcp");
		h2tcp.stop();
		log.warn("H2 TCP server is closed");
		// System.setProperty("debug.break.before", "");// 断点清理
	}

}
