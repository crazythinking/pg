package net.engining.pg.parameter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.TreeBasedTable;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pg.parameter.entity.enums.ParamOperationDef;
import net.engining.pg.parameter.entity.model.ParameterAudit;
import net.engining.pg.parameter.entity.model.ParameterObject;
import net.engining.pg.parameter.entity.model.ParameterObjectKey;
import net.engining.pg.parameter.entity.model.QParameterObject;
import net.engining.pg.parameter.utils.ParamObjDiffUtils;
import net.engining.pg.support.utils.ValidateUtilExt;


public class JsonLocalCachedParameterFacility extends ParameterFacility implements ApplicationListener<ParameterChangedEvent>, InitializingBean
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private Provider4Organization provider4Organization;
	
	//最小日期时间+1天，防止mysql5.7以上版本数据库出错，timestamp型数据的取值范围('1970-01-01 00:00:00', '2037-12-31 23:59:59']
	private static Date minDate = DateUtils.addDays(new Date(0), 1);

	//默认5分钟过期
	private long expireDuration = 5;
	
	private TimeUnit expireTimeUnit = TimeUnit.MINUTES;
	
	private Ticker ticker = Ticker.systemTicker();
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PostConstruct
	public void init()
	{
		
	}
	
	// (org|class_name)   -   map<key, value>
	private LoadingCache<String, TreeBasedTable<String, Date, Object>> cache;
	
	public Object convertParameterObject(String parameterStream, String paramClass) throws ClassNotFoundException{
		
		return JSON.parseObject(parameterStream, Class.forName(paramClass));
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		cache = CacheBuilder.newBuilder()
				.expireAfterWrite(expireDuration, expireTimeUnit)
				.ticker(ticker)
				.build(new CacheLoader<String, TreeBasedTable<String, Date, Object>>(){

					@Override
					public TreeBasedTable<String, Date, Object> load(String key) throws Exception {
						//cache中的key是 (org|class_name)，切分org和classname
						int pos = key.indexOf('|');
						
						QParameterObject q = QParameterObject.parameterObject;
						List<ParameterObject> results = new JPAQueryFactory(em)
															.select(q)
															.from(q)
															.where(q.orgId.eq(key.substring(0, pos)), q.paramClass.eq(key.substring(pos + 1)))
															.fetch();
						TreeBasedTable<String, Date, Object> ret = TreeBasedTable.create();
						for (ParameterObject result : results)
						{
							Object obj = convertParameterObject(result.getParamObject(), result.getParamClass());
							//TODO 处理Version之类的属性
							ret.put(result.getParamKey(), result.getEffectiveDate(), obj);
						}
						logger.info("加载参数[{}]，计[{}]条", key, ret.size());
						return ret;
					}
				});
	}

	@Override
	public void onApplicationEvent(ParameterChangedEvent event) {
		//刷新参数
		String cacheKey = createCacheKey(event.getOrgId(), event.getParamClass());
		if (cache.getIfPresent(cacheKey) != null)
		{
			logger.info("刷新参数缓存[{}]", cacheKey);
		}
		//避免竞争条件，总是invalidate
		cache.invalidate(cacheKey);
	}
	
	/**
	 * 参数在cache中的key：org|class_name
	 * @param orgId
	 * @param classname
	 * @return
	 */
	private String createCacheKey(String orgId, String classname)
	{
		return orgId + "|" + classname;
	}

	@Override
	public <T> T getParameter(Class<T> paramClass, String key, Date effectiveDate) {
		
		checkNotNull(paramClass, "需要指定参数类 paramClass");
		checkNotNull(key, "需要指定参数key");
		checkNotNull(effectiveDate, "需要指定参数生效日期 effectiveDate");

		TreeBasedTable<String, Date, T> table = getParameterTable(paramClass);
		SortedMap<Date, T> timeline = table.row(key);
		
		if (timeline.isEmpty())
		{
			//没有
			return null;
		}
		
		//先看是不是有正好起效的，因为日期是闭区间
		if (timeline.containsKey(effectiveDate))
		{
			return timeline.get(effectiveDate);
		}
		else
		{
			//这里是按date从小到大排序
			SortedMap<Date, T> map = timeline.headMap(effectiveDate);
			if (map.isEmpty())
			{
				//没有范围内的
				return null;
			}
			Date lastKey = map.lastKey();
			return timeline.get(lastKey);
		}
	}
	
	public CacheStats getCurrentStats()
	{
		return cache.stats();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> TreeBasedTable<String, Date, T> getParameterTable(Class<T> paramClass) {
		String org = provider4Organization.getCurrentOrganizationId();
		String classname = paramClass.getCanonicalName();
		try
		{
			return (TreeBasedTable<String, Date, T>) cache.get(createCacheKey(org, classname));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 构建参数表的主键
	 * @param key
	 * @param effectiveDate
	 * @param clazz
	 * @return
	 */
	private ParameterObjectKey createKey(String key, Date effectiveDate, Class<?> clazz)
	{
		ParameterObjectKey pok = new ParameterObjectKey();
		pok.setOrgId(provider4Organization.getCurrentOrganizationId());
		pok.setParamKey(key);
		pok.setParamClass(clazz.getCanonicalName());
		pok.setEffectiveDate(effectiveDate);
		return pok;
	}
	
	//============   添加参数   ===========

	@Override
	@Transactional
	public <T> void addParameter(String key, T newParameter) {
		checkNotNull(newParameter, "添加时对象不能为null");
		checkNotNull(key, "添加时对象key不能为null");

		Date effectiveDate = minDate;	//默认为史前

		if (newParameter instanceof HasEffectiveDate)
		{
			//如果支持effectiveDate，则使用参数内的数据
			Date eff = ((HasEffectiveDate) newParameter).getEffectiveDate();
			if (eff != null)
			{
				effectiveDate = eff;
			}
			else
			{
				logger.warn("参数[{}]/[{}]支持effectiveDate特性，但没有指定生效日期，使用默认值new Date(0l)", newParameter.getClass(), key);
				((HasEffectiveDate) newParameter).setEffectiveDate(effectiveDate);
			}
		}
		
		ParameterObjectKey pok = createKey(key, effectiveDate, newParameter.getClass());
		if (em.find(ParameterObject.class, pok) != null)
		{
			throw new ParameterExistsException(pok.getOrgId(), pok.getParamClass(), pok.getParamKey(), pok.getEffectiveDate());
		}

		ParameterObject obj = new ParameterObject();
		obj.setOrgId(pok.getOrgId());
		obj.setParamKey(pok.getParamKey());
		obj.setParamClass(pok.getParamClass());
		obj.setEffectiveDate(effectiveDate);
		obj.setParamObject(JSON.toJSONString(newParameter));
		obj.setMtnTimestamp(new Date());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null)
			obj.setMtnUser(auth.getName());
		
		if (newParameter instanceof HasVersion)
		{
			((HasVersion) newParameter).setVersion(0);
		}
		
		em.persist(obj);
		
		// 记录操作日志
		auditPrmModify(key, pok.getEffectiveDate(), pok.getParamClass(), ParamOperationDef.INSERT, newParameter, null);
				
		//TODO 更新version等字段
		
		//刷新
		ctx.publishEvent(new ParameterChangedEvent(this, obj.getOrgId(), obj.getParamClass(), obj.getParamKey()));
	}

	//============  更新参数   ===========

	@Override
	@Transactional
	public <T> void updateParameter(String key, T parameter, Date effectiveDate) {
		checkNotNull(parameter, "更新时对象不能为null");
		checkNotNull(key, "更新时对象key不能为null");
		
		if(ValidateUtilExt.isNullOrEmpty(effectiveDate)) {
			effectiveDate = minDate;	//默认为史前+1d '1970-01-02 00:00:00'
		}

		if (parameter instanceof HasEffectiveDate) {
			//如果支持effectiveDate，则使用参数内的数据
			effectiveDate = ((HasEffectiveDate) parameter).getEffectiveDate();
		}

		ParameterObjectKey pok = createKey(key, effectiveDate, parameter.getClass());
		ParameterObject obj = em.find(ParameterObject.class, pok);

		if (obj == null)
		{
			throw new ParameterNotFoundException(pok.getOrgId(), pok.getParamClass(), pok.getParamKey(), effectiveDate);
		}

		//TODO hasversion支持
		String oldParamXml = obj.getParamObject();
		obj.setParamObject(JSON.toJSONString(parameter));
		obj.setMtnTimestamp(new Date());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null)
			obj.setMtnUser(auth.getName());
		
		//避免竞争条件，所以提前flush
		em.flush();
		
		// 记录操作日志
		auditPrmModify(key, pok.getEffectiveDate(), pok.getParamClass(), ParamOperationDef.UPDATE, parameter, JSON.parseObject(oldParamXml, parameter.getClass()));
		
		//刷新
		ctx.publishEvent(new ParameterChangedEvent(this, pok.getOrgId(), pok.getParamClass(), pok.getParamKey()));
	}
	
	//============ 删除参数   ===========

	@Override
	@Transactional
	public <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate) {
		
		QParameterObject q = QParameterObject.parameterObject;
		String orgId = provider4Organization.getCurrentOrganizationId();
		String classname = paramClass.getCanonicalName();
		
		JPAQuery<ParameterObject> query = new JPAQueryFactory(em).select(q);
		query.from(q).where(
				q.orgId.eq(orgId),
				q.paramClass.eq(classname),
				q.paramKey.eq(key)
				);
		if (effectiveDate != null)
		{
			query.where(q.effectiveDate.eq(effectiveDate));
		}
		
		ctx.publishEvent(new ParameterChangedEvent(this, orgId, classname, key));
		
		List<ParameterObject> paramObjects = query.fetch();
		
		if (paramObjects == null || paramObjects.size() == 0)
		{
			logger.warn("希望删的参数不存在[{}/{}/{}/{}]", orgId, classname, key, effectiveDate);
			return false;
		} else {
			for (ParameterObject paramObject : paramObjects) {
				em.remove(paramObject);
				
				// 记录操作日志
				auditPrmModify(key, paramObject.getEffectiveDate(), paramClass.getCanonicalName(), ParamOperationDef.DELETE, null, JSON.parseObject(paramObject.getParamObject(), paramClass));
			}
		}
		return true;
	}

	/**
	 * 记录参数操作审计日志
	 * 
	 * @param org
	 * @param key
	 * @param paramClass
	 * @param operation
	 * @param newObj
	 * @param oldObj
	 * @param user
	 */
	@Transactional
	private <T> void auditPrmModify(String key, Date effectiveDate, String paramClass, ParamOperationDef operation, T newObj, T oldObj) {
		String org = provider4Organization.getCurrentOrganizationId();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = auth == null ? "" : auth.getName();
		
		ParameterAudit prmAudit = new ParameterAudit();
		prmAudit.setOrgId(org);
		prmAudit.setParamKey(key);
		prmAudit.setParamClass(paramClass);
		prmAudit.setEffectiveDate(effectiveDate);
		prmAudit.setParamOperation(operation);
		prmAudit.setOldObject(oldObj == null ? "" : JSON.toJSONString(oldObj));
		prmAudit.setNewObject(newObj == null ? "" : JSON.toJSONString(newObj));
		prmAudit.setMtnUser(user);
		prmAudit.setMtnTimestamp(new Date());
		switch (operation) {
		case INSERT:
			prmAudit.setUpdateLog("新增参数，详细记录参看xml数据");
			break;
		case DELETE:
			prmAudit.setUpdateLog("删除参数，原记录参看xml数据");
			break;
		case UPDATE:
			try {
				prmAudit.setUpdateLog(ParamObjDiffUtils.diff(newObj, oldObj, "", "", 0, 0));
			} catch (Exception e) {
				logger.error("对象对比时异常", e);
				prmAudit.setUpdateLog("参数对象无法比对");
			} 
			break;
		default:
			break;
		}
		
		em.persist(prmAudit);
	}
	
	public long getExpireDuration() {
		return expireDuration;
	}

	public void setExpireDuration(long expireDuration) {
		this.expireDuration = expireDuration;
	}

	public TimeUnit getExpireTimeUnit() {
		return expireTimeUnit;
	}

	public void setExpireTimeUnit(TimeUnit expireTimeUnit) {
		this.expireTimeUnit = expireTimeUnit;
	}

	public Ticker getTicker() {
		return ticker;
	}

	public void setTicker(Ticker ticker) {
		this.ticker = ticker;
	}

}
