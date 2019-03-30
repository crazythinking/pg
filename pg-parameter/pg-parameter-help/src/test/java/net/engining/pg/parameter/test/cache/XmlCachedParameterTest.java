package net.engining.pg.parameter.test.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeBasedTable;
import net.engining.pg.parameter.ParameterNotFoundException;
import net.engining.pg.parameter.XmlGuavaCachedParameterFacility;
import net.engining.pg.parameter.test.suport.AbstractTestCaseTemplate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles(profiles={
		"xmlp"
})
@DirtiesContext(classMode= ClassMode.AFTER_CLASS)
public class XmlCachedParameterTest extends AbstractTestCaseTemplate {
	
	private static final Logger log = LoggerFactory.getLogger(XmlCachedParameterTest.class);

	@Autowired
	private XmlGuavaCachedParameterFacility parameterFacility;
	
	@Autowired
	private ControlledTicker ticker;
	
	@Test
	public void normal()
	{
		log.info("开始测试Cached Parameter Normal案例");

		SampleParameter sp = new SampleParameter();
		sp.param1 = "param1";
		sp.param2 = "param2";
		sp.paramList = Lists.newArrayList();
		
		parameterFacility.addParameter("0001", sp);
		
		//Table取测试
		TreeBasedTable<String, Date, SampleParameter> table = parameterFacility.getParameterTable(SampleParameter.class);
		assertThat(table.cellSet().size(), equalTo(1));
		assertThat(table.rowKeySet(), hasSize(1));
		assertThat(table.rowKeySet(), contains("0001"));
		SampleParameter firstSample = table.row("0001").values().iterator().next();
		assertThat(firstSample.param1, equalTo("param1"));
		
		//单条取测试
		SampleParameter got = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat("经过串行化后取到的不该是原对象", got, not(equalTo(sp)));	//这里没有重写equals，所以是实例相等
		assertThat(got.param1, equalTo("param1"));
		assertThat(got.param2, equalTo("param2"));
		
		//确认缓存
		SampleParameter cached = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat("确认取到原对象", cached, equalTo(got));	//这里没有重写equals，所以是实例相等
		assertThat("确认取到原对象", cached, equalTo(firstSample));	//这里没有重写equals，所以是实例相等
		assertThat(cached.param1, equalTo("param1"));
		assertThat(cached.param2, equalTo("param2"));

		//确认缓存刷新
		sp.param1 = "new param";
		parameterFacility.updateParameter("0001", sp);
		SampleParameter updated = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat("确认经过刷新后取到的不该是原对象", updated, not(equalTo(got)));	//这里没有重写equals，所以是实例相等
		assertThat(updated.param1, equalTo("new param"));
		assertThat(updated.param2, equalTo("param2"));
		
		//删除
		assertThat(
				parameterFacility.removeParameter(SampleParameter.class, "0001"),
				equalTo(true)
		);
		SampleParameter deleted = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat(deleted, nullValue());
		
	}
	
	@Test
	@DirtiesContext
	public void expire()
	{
		log.info("开始测试Cached Parameter 过期案例");
		
		SampleParameter sp = new SampleParameter();
		sp.param1 = "param1";
		sp.param2 = "param2";
		sp.paramList = Lists.newArrayList();
		
		parameterFacility.addParameter("0001", sp);

		//单条取测试
		SampleParameter got = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat("经过串行化后取到的不该是原对象", got, is(not(equalTo(sp))));	//这里没有重写equals，所以是实例相等
		assertThat(got.param1, equalTo("param1"));
		assertThat(got.param2, equalTo("param2"));
		
		//确认缓存
		SampleParameter cached = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat("确认取到原对象", cached, is(equalTo(got)));	//这里没有重写equals，所以是实例相等
		assertThat(cached.param1, equalTo("param1"));
		assertThat(cached.param2, equalTo("param2"));

		//等待超时，比设置的多1纳秒
		ticker.setValue(parameterFacility.getExpireTimeUnit().toNanos(parameterFacility.getExpireDuration()) + 1);
		
		cached = parameterFacility.getParameter(SampleParameter.class, "0001");
		assertThat("确认没取到原对象", cached, is(not(equalTo(got))));	//这里没有重写equals，所以是实例相等
		assertThat(cached.param1, equalTo("param1"));
		assertThat(cached.param2, equalTo("param2"));
		
	}
	
	@Test(expected = ParameterNotFoundException.class)
	public void loadException()
	{
		parameterFacility.loadParameter(SampleParameter.class, "0000");
	}

	@Override
	public void initTestData() throws Exception {
		
	}

	@Override
	public void assertResult() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testProcess() throws Exception {
		
	}

	@Override
	public void end() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
