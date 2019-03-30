package net.engining.pg.parameter.test.audit;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.entity.enums.ParamOperationDef;
import net.engining.pg.parameter.entity.model.ParameterAudit;
import net.engining.pg.parameter.entity.model.QParameterAudit;
import net.engining.pg.parameter.test.cache.InnerParameter;
import net.engining.pg.parameter.test.cache.SampleParameter;
import net.engining.pg.parameter.test.suport.AbstractTestCaseTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles(profiles={
		"jsonp"

})
@DirtiesContext(classMode= ClassMode.AFTER_CLASS)
public class JsonParameterLogTest extends AbstractTestCaseTemplate {
	
	@Autowired
	private ParameterFacility jsonparameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Test
	public void insertLogTest()
	{
		SampleParameter sp = new SampleParameter();
		sp.param1 = "param1";
		sp.param2 = "param2";
		sp.paramList = Lists.newArrayList();
		
		jsonparameterFacility.addParameter("0001", sp);

		// 取日志
		QParameterAudit qParameterAudit = QParameterAudit.parameterAudit;
		JPAQuery<ParameterAudit> query = new JPAQueryFactory(em).select(qParameterAudit);
		query.from(qParameterAudit).where(qParameterAudit.paramKey.eq("0001")
				.and(qParameterAudit.paramClass.eq(SampleParameter.class.getCanonicalName()))
//				.and(qParameterAudit.effectiveDate.isNull())
				.and(qParameterAudit.paramOperation.eq(ParamOperationDef.INSERT)));
		List<ParameterAudit> logs = query.fetch();
		
		assertThat(logs.size(), equalTo(1));
		assertThat(logs.get(0).getNewObject(), equalTo(JSON.toJSONString(sp)));
		assertThat(logs.get(0).getOldObject(), equalTo(""));
		assertThat(logs.get(0).getMtnUser(), notNullValue());
		assertThat(logs.get(0).getMtnTimestamp(), notNullValue());
	}
	
	@Test
	public void deleteLogTest()
	{
		SampleParameter sp = new SampleParameter();
		sp.param1 = "param1";
		sp.param2 = "param2";
		sp.paramList = Lists.newArrayList();
		
		jsonparameterFacility.addParameter("0002", sp);

		// 取日志
		QParameterAudit qParameterAudit = QParameterAudit.parameterAudit;
		JPAQuery<ParameterAudit> query = new JPAQueryFactory(em).select(qParameterAudit);
		query.from(qParameterAudit).where(qParameterAudit.paramKey.eq("0002")
				.and(qParameterAudit.paramClass.eq(SampleParameter.class.getCanonicalName()))
//				.and(qParameterAudit.effectiveDate.isNull())
				.and(qParameterAudit.paramOperation.eq(ParamOperationDef.INSERT)));
		List<ParameterAudit> logs = query.fetch();
		
		assertThat(logs.size(), equalTo(1));
		assertThat(logs.get(0).getNewObject(), equalTo(JSON.toJSONString(sp)));
		assertThat(logs.get(0).getOldObject(), equalTo(""));
		assertThat(logs.get(0).getMtnUser(), notNullValue());
		assertThat(logs.get(0).getMtnTimestamp(), notNullValue());
		
		jsonparameterFacility.removeParameter(SampleParameter.class, "0002");
		JPAQuery<ParameterAudit> query2 = new JPAQueryFactory(em).select(qParameterAudit);
		query2.from(qParameterAudit).where(qParameterAudit.paramKey.eq("0002")
				.and(qParameterAudit.paramClass.eq(SampleParameter.class.getCanonicalName()))
//				.and(qParameterAudit.effectiveDate.isNull())
				.and(qParameterAudit.paramOperation.eq(ParamOperationDef.DELETE)));
		List<ParameterAudit> logs2 = query2.fetch();
		
		assertThat(logs2.size(), equalTo(1));
		assertThat(logs2.get(0).getNewObject(), equalTo(""));
		assertThat(logs2.get(0).getOldObject(), equalTo(JSON.toJSONString(sp)));
		assertThat(logs2.get(0).getMtnUser(), notNullValue());
		assertThat(logs2.get(0).getMtnTimestamp(), notNullValue());
	}
	
	@Test
	public void updateLogTest()
	{
		SampleParameter sp = new SampleParameter();
		sp.param1 = "param1";
		sp.param2 = "param2";
		sp.paramList = Lists.newArrayList();
		InnerParameter innerParameter = new InnerParameter();
		innerParameter.innerParameter = "innerParameter1";
		sp.paramList.add(innerParameter);
		
		jsonparameterFacility.addParameter("0003", sp);
		
		// 取日志
		QParameterAudit qParameterAudit = QParameterAudit.parameterAudit;
		JPAQuery<ParameterAudit> query = new JPAQueryFactory(em).select(qParameterAudit);
		query.from(qParameterAudit).where(qParameterAudit.paramKey.eq("0003")
				.and(qParameterAudit.paramClass.eq(SampleParameter.class.getCanonicalName()))
//				.and(qParameterAudit.effectiveDate.isNull())
				.and(qParameterAudit.paramOperation.eq(ParamOperationDef.INSERT)));
		List<ParameterAudit> logs = query.fetch();
		
		assertThat(logs.size(), equalTo(1));
		assertThat(logs.get(0).getNewObject(), equalTo(JSON.toJSONString(sp)));
		assertThat(logs.get(0).getOldObject(), equalTo(""));
		assertThat(logs.get(0).getMtnUser(), notNullValue());
		assertThat(logs.get(0).getMtnTimestamp(), notNullValue());
		
		SampleParameter sp2 = new SampleParameter();
		sp2.param1 = "param1-update";
		sp2.param2 = "param2-update";
		sp2.paramList = Lists.newArrayList();
		InnerParameter innerParameter2 = new InnerParameter();
		innerParameter2.innerParameter = "innerParameter1";
		sp2.paramList.add(innerParameter2);
		innerParameter2 = new InnerParameter();
		innerParameter2.innerParameter = "innerParameter2";
		sp2.paramList.add(innerParameter2);
		
		jsonparameterFacility.updateParameter("0003", sp2);
		
		JPAQuery<ParameterAudit> query2 = new JPAQueryFactory(em).select(qParameterAudit);
		query2.from(qParameterAudit).where(qParameterAudit.paramKey.eq("0003")
				.and(qParameterAudit.paramClass.eq(SampleParameter.class.getCanonicalName()))
//				.and(qParameterAudit.effectiveDate.isNull())
				.and(qParameterAudit.paramOperation.eq(ParamOperationDef.UPDATE)));
		List<ParameterAudit> logs2 = query2.fetch();
		
		assertThat(logs2.size(), equalTo(1));
		assertThat(logs2.get(0).getNewObject(), equalTo(JSON.toJSONString(sp2)));
		assertThat(logs2.get(0).getOldObject(), equalTo(JSON.toJSONString(sp)));
		assertThat(logs2.get(0).getMtnUser(), notNullValue());
		assertThat(logs2.get(0).getMtnTimestamp(), notNullValue());
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
