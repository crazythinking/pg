package net.engining.pg.parameter.test.cache;

import com.google.common.collect.Lists;
import net.engining.pg.parameter.JsonRedisCachedParameterFacility;
import net.engining.pg.parameter.ParameterInter;
import net.engining.pg.parameter.test.suport.AbstractTestCaseTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

/**
 * @author luxue
 *
 */
@ActiveProfiles(profiles={
		"redisjsonp"
})
@DirtiesContext(classMode= ClassMode.AFTER_CLASS)
public class RedisCachedParameterTest extends AbstractTestCaseTemplate {
	
	private static final Logger log = LoggerFactory.getLogger(RedisCachedParameterTest.class);
	
	@Autowired
	private ParameterInter redisJsonCachedParameterFacility;

	/* (non-Javadoc)
	 * @see net.engining.pg.support.testcase.TestCase#initTestData()
	 */
	@Override
	public void initTestData() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.testcase.TestCase#assertResult()
	 */
	@Override
	public void assertResult() throws Exception {
		SampleParameter sp = redisJsonCachedParameterFacility.getParameter(SampleParameter.class, "0001");
		UnqueSimpleParameter usp = redisJsonCachedParameterFacility.loadUniqueParameter(UnqueSimpleParameter.class);
		log.debug(sp.toString());
		log.debug(usp.toString());
		
		sp.param1 = "22222";
		redisJsonCachedParameterFacility.updateParameter("0001", sp);
		redisJsonCachedParameterFacility.removeParameter(UnqueSimpleParameter.class, "*");
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.testcase.TestCase#testProcess()
	 */
	@Override
	public void testProcess() throws Exception {
		log.info("开始测试Redis Cached Parameter Normal案例");

		SampleParameter sp = new SampleParameter();
		sp.param1 = "param1";
		sp.param2 = "param2";
		sp.paramList = Lists.newArrayList();
		InnerParameter usp1 = new InnerParameter();
		usp1.innerParameter = "up1";
		sp.paramList.add(usp1);
		usp1 = new InnerParameter();
		usp1.innerParameter = "up3";
		sp.paramList.add(usp1);
		redisJsonCachedParameterFacility.addParameter("0001", sp);
		redisJsonCachedParameterFacility.getParameter(sp.getClass(), "0001");
		redisJsonCachedParameterFacility.getParameter(sp.getClass(), "0001", new Date());
		
		UnqueSimpleParameter usp = new UnqueSimpleParameter();
		usp.param1 = "up1";
		usp.param2 = "up2";
		redisJsonCachedParameterFacility.addParameter(redisJsonCachedParameterFacility.UNIQUE_PARAM_KEY, usp);
		redisJsonCachedParameterFacility.loadUniqueParameter(usp.getClass());
		
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.testcase.TestCase#end()
	 */
	@Override
	public void end() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
