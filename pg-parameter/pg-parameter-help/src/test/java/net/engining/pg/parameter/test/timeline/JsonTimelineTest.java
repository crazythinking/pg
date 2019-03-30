package net.engining.pg.parameter.test.timeline;

import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.ParameterNotFoundException;
import net.engining.pg.parameter.test.suport.AbstractTestCaseTemplate;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DirtiesContext(classMode= ClassMode.AFTER_CLASS)
public class JsonTimelineTest extends AbstractTestCaseTemplate {
	
	@Autowired
	private ParameterFacility jsonparameterFacility;
	
	private void validate(String key, int year, int month, int day, String expected)
	{
		TimelineParameter tp;
		tp = jsonparameterFacility.getParameter(TimelineParameter.class, key, new DateTime(year, month, day, 0, 0).toDate());
		if (expected == null)
		{
			assertThat(tp, is(nullValue()));
		}
		else
		{
			assertThat(tp, is(not(nullValue())));
			assertThat(tp.data, equalTo(expected));
		}
	}
	
	@Test
	public void normal()
	{
		TimelineParameter tp;
		//空的
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, null);
		validate("1", 2014, 1,31, null);
		validate("1", 2014, 2, 1, null);
		validate("1", 2014, 3, 1, null);
		validate("1", 2014, 3, 2, null);
		validate("2", 2014, 1, 1, null);

		//添加1/1
		jsonparameterFacility.addParameter("1", new TimelineParameter("tp20140101", new DateTime(2014, 1, 1, 0, 0)));
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, "tp20140101");
		validate("1", 2014, 1,31, "tp20140101");
		validate("1", 2014, 2, 1, "tp20140101");
		validate("1", 2014, 3, 1, "tp20140101");
		validate("1", 2014, 3, 2, "tp20140101");
		validate("2", 2014, 1, 1, null);
		
		//添加3/1
		jsonparameterFacility.addParameter("1", new TimelineParameter("tp20140301", new DateTime(2014, 3, 1, 0, 0)));
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, "tp20140101");
		validate("1", 2014, 1,31, "tp20140101");
		validate("1", 2014, 2, 1, "tp20140101");
		validate("1", 2014, 3, 1, "tp20140301");
		validate("1", 2014, 3, 2, "tp20140301");
		validate("2", 2014, 1, 1, null);

		//添加2/1，不按顺序添加测试排序
		jsonparameterFacility.addParameter("1", new TimelineParameter("tp20140201", new DateTime(2014, 2, 1, 0, 0)));
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, "tp20140101");
		validate("1", 2014, 1,31, "tp20140101");
		validate("1", 2014, 2, 1, "tp20140201");
		validate("1", 2014, 3, 1, "tp20140301");
		validate("1", 2014, 3, 2, "tp20140301");
		validate("2", 2014, 1, 1, null);

		//添加key为2的
		jsonparameterFacility.addParameter("2", new TimelineParameter("tp20130101_2", new DateTime(2013, 1, 1, 0, 0)));
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, "tp20140101");
		validate("1", 2014, 1,31, "tp20140101");
		validate("1", 2014, 2, 1, "tp20140201");
		validate("1", 2014, 3, 1, "tp20140301");
		validate("1", 2014, 3, 2, "tp20140301");
		validate("2", 2014, 1, 1, "tp20130101_2");
		
		//删除2/1
		jsonparameterFacility.removeParameter(TimelineParameter.class, "1", new DateTime(2014, 2, 1, 0, 0).toDate());
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, "tp20140101");
		validate("1", 2014, 1,31, "tp20140101");
		validate("1", 2014, 2, 1, "tp20140101");
		validate("1", 2014, 3, 1, "tp20140301");
		validate("1", 2014, 3, 2, "tp20140301");
		validate("2", 2014, 1, 1, "tp20130101_2");
		
		//更新1/1
		tp = new TimelineParameter("tp20140101_modified", new DateTime(2014, 1, 1, 0, 0));
		jsonparameterFacility.updateParameter("1", tp);
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, "tp20140101_modified");
		validate("1", 2014, 1,31, "tp20140101_modified");
		validate("1", 2014, 2, 1, "tp20140101_modified");
		validate("1", 2014, 3, 1, "tp20140301");
		validate("1", 2014, 3, 2, "tp20140301");
		validate("2", 2014, 1, 1, "tp20130101_2");
		
		//按兼容方式取最新
		tp = jsonparameterFacility.getParameter(TimelineParameter.class, "1");
		assertThat(tp.data, equalTo("tp20140301"));
		
		//取2/5的所有参数
		Map<String, TimelineParameter> map = jsonparameterFacility.getParameterMap(TimelineParameter.class, new DateTime(2014, 2, 5, 0, 0).toDate());
		assertThat(map.get("1").data, equalTo("tp20140101_modified"));
		assertThat(map.get("2").data, equalTo("tp20130101_2"));
		
		//取最新参数
		map = jsonparameterFacility.getParameterMap(TimelineParameter.class);
		assertThat(map.get("1").data, equalTo("tp20140301"));
		assertThat(map.get("2").data, equalTo("tp20130101_2"));
		
		//兼容方式删除
		assertThat(
				jsonparameterFacility.removeParameter(TimelineParameter.class, "1"),
				equalTo(true)
		);
		validate("1", 2013, 1, 1, null);
		validate("1", 2014, 1, 1, null);
		validate("1", 2014, 1,31, null);
		validate("1", 2014, 2, 1, null);
		validate("1", 2014, 3, 1, null);
		validate("1", 2014, 3, 2, null);
		validate("2", 2014, 1, 1, "tp20130101_2");
	}
	
	/**
	 * 指定了 HasEffectiveDate必须指定effectiveDate
	 */
	@Test(expected = RuntimeException.class)
	public void dateExists()
	{
		TimelineParameter tp = new TimelineParameter("1+2", null);
		jsonparameterFacility.addParameter("dateExists", tp);
	}

	/**
	 * 更新不存在的日期
	 */
	@Test(expected = ParameterNotFoundException.class)
	public void updateNonExists()
	{
		TimelineParameter tp = new TimelineParameter("updateNonExists", new DateTime(2014, 1, 1, 0, 0));
		jsonparameterFacility.addParameter("updateNonExists", tp);
		
		tp = new TimelineParameter("updateNonExists_modified", new DateTime(2014, 1, 2, 0, 0));
		jsonparameterFacility.updateParameter("updateNonExists", tp);
	}
	
	/**
	 * 删除不存在的日期
	 */
	@Test
	public void removeNonExists()
	{
		TimelineParameter tp = new TimelineParameter("removeNonExists", new DateTime(2014, 1, 1, 0, 0));
		jsonparameterFacility.addParameter("removeNonExists", tp);
		
		boolean ret = jsonparameterFacility.removeParameter(TimelineParameter.class, "removeNonExists", new DateTime(2014, 1, 2, 0, 0).toDate());
		assertThat(ret, equalTo(false));
	}

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
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.testcase.TestCase#testProcess()
	 */
	@Override
	public void testProcess() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.engining.pg.support.testcase.TestCase#end()
	 */
	@Override
	public void end() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
