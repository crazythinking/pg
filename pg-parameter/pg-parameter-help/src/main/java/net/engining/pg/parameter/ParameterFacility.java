package net.engining.pg.parameter;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeBasedTable;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thoughtworks.xstream.XStream;
import net.engining.pg.parameter.entity.enums.ParamOperationDef;
import net.engining.pg.parameter.entity.model.ParameterAudit;
import net.engining.pg.parameter.entity.model.ParameterObject;
import net.engining.pg.parameter.entity.model.ParameterObjectKey;
import net.engining.pg.parameter.entity.model.QParameterObject;
import net.engining.pg.parameter.utils.ParamObjDiffUtils;
import net.engining.pg.support.core.context.Provider4Organization;
import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;
import net.engining.pg.support.utils.ValidateUtilExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ParameterFacility implements ParameterInter{

	Logger logger = LoggerFactory.getLogger(ParameterFacility.class);

	@PersistenceContext
	private EntityManager em;

	// 下面是参数默认操作逻辑，由子类调用

	<T> T defaultGetParameter(Class<T> paramClass, String key, Date effectiveDate){
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

	@SuppressWarnings("unchecked")
	<T> TreeBasedTable<String, Date, T>  defaultGetParameterTable(Class<T> paramClass,
                                                                  Provider4Organization provider4Organization,
                                                                  LoadingCache<String, TreeBasedTable<String, Date, Object>> cache){
		String org = provider4Organization.getCurrentOrganizationId();
		String classname = paramClass.getCanonicalName();
		try
		{
			return (TreeBasedTable<String, Date, T>) cache.get(createCacheKey(org, classname));
		}
		catch (Exception e)
		{
			throw new ErrorMessageException(ErrorCode.UnknowFail, ErrorCode.UnknowFail.getLabel(), e);
		}
	}

    @Transactional
    <T> ParameterObject defaultAddParameter(String key, T newParameter, Provider4Organization provider4Organization, XStream xstream){
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

        ParameterObjectKey pok = createKey(key, effectiveDate, newParameter.getClass(), provider4Organization);
        if (em.find(ParameterObject.class, pok) != null)
        {
            throw new ParameterExistsException(pok.getOrgId(), pok.getParamClass(), pok.getParamKey(), pok.getEffectiveDate());
        }

        ParameterObject obj = new ParameterObject();
        obj.setOrgId(pok.getOrgId());
        obj.setParamKey(pok.getParamKey());
        obj.setParamClass(pok.getParamClass());
        obj.setEffectiveDate(effectiveDate);
        setupParameterObjectValueAndAduitFd(newParameter, xstream, obj);

        if (newParameter instanceof HasVersion)
        {
            ((HasVersion) newParameter).setVersion(0);
        }

        em.persist(obj);

        return obj;
    }

    private <T> void setupParameterObjectValueAndAduitFd(T newParameter, XStream xstream, ParameterObject obj) {
        if(xstream != null){
            obj.setParamObject(xstream.toXML(newParameter));
        }
        else {
            obj.setParamObject(JSON.toJSONString(newParameter));
        }
        obj.setMtnTimestamp(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null)
            obj.setMtnUser(auth.getName());
    }

	@Transactional
    <T> Map<String, Serializable> defaultUpdateParameter(String key, T parameter, Date effectiveDate, Provider4Organization provider4Organization, XStream xstream){
	    Map<String, Serializable> retMap = Maps.newHashMap();
        checkNotNull(parameter, "更新时对象不能为null");
        checkNotNull(key, "更新时对象key不能为null");

        if(ValidateUtilExt.isNullOrEmpty(effectiveDate)) {
            effectiveDate = minDate;	//默认为史前+1d '1970-01-02 00:00:00'
        }

        if (parameter instanceof HasEffectiveDate) {
            //如果支持effectiveDate，则使用参数内的数据
            effectiveDate = ((HasEffectiveDate) parameter).getEffectiveDate();
        }

        ParameterObjectKey pok = createKey(key, effectiveDate, parameter.getClass(), provider4Organization);
        ParameterObject obj = em.find(ParameterObject.class, pok);

        if (obj == null)
        {
            throw new ParameterNotFoundException(pok.getOrgId(), pok.getParamClass(), pok.getParamKey(), effectiveDate);
        }

        String oldParamStr = obj.getParamObject();
        setupParameterObjectValueAndAduitFd(parameter, xstream, obj);

        //避免竞争条件，所以提前flush
        em.flush();

        retMap.put("oldParamStr",oldParamStr);
        retMap.put("parameterObject", obj);
        return retMap;
    }

	/**
	 * 删除指定参数。
	 * @param paramClass 参数类
	 * @param key 主键
	 * @param effectiveDate 生效日期。如果为null，则表示删除所有的生效日期的参数。
	 * @return 是否确实删除参数
	 */
	//public abstract <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate);

    @Transactional
    <T> boolean defaultRemoveParameter(Class<T> paramClass, String key, Date effectiveDate, Provider4Organization provider4Organization, XStream xstream) {

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

        List<ParameterObject> paramObjects = query.fetch();

        if (ValidateUtilExt.isNullOrEmpty(paramObjects))
        {
            logger.warn("希望删的参数不存在[{}/{}/{}/{}]", orgId, classname, key, effectiveDate);
            return false;
        } else {
            for (ParameterObject paramObject : paramObjects) {
                em.remove(paramObject);

                if (xstream != null){
                    // 记录操作日志
                    auditPrmModify(
                            key,
                            paramObject.getEffectiveDate(),
                            paramClass.getCanonicalName(),
                            ParamOperationDef.DELETE,
                            null,
                            xstream.fromXML(paramObject.getParamObject()),
                            provider4Organization,
							xstream
                    );
                }
                else {
                    // 记录操作日志
                    auditPrmModify(
                            key,
                            paramObject.getEffectiveDate(),
                            paramClass.getCanonicalName(),
                            ParamOperationDef.DELETE,
                            null,
                            JSON.parseObject(paramObject.getParamObject(), paramClass),
                            provider4Organization,
                            null
                    );
                }


            }
        }
        return true;
    }

	List<ParameterObject> fetchParamsByLocalCacheKey(String key){
		//cache中的key是 (org|class_name)，切分org和classname
		int pos = key.indexOf('|');

		QParameterObject q = QParameterObject.parameterObject;

		return new JPAQueryFactory(em)
				.select(q)
				.from(q)
				.where(q.orgId.eq(key.substring(0, pos)), q.paramClass.eq(key.substring(pos + 1)))
				.fetch();
	}

	void applicationCacheEventAction(ParameterChangedEvent event, LoadingCache<String, TreeBasedTable<String, Date, Object>> cache){
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
	 */
    private String createCacheKey(String orgId, String classname)
	{
		return orgId + "|" + classname;
	}

    /**
     * 构建参数Table的组合主键对象
     */
    private ParameterObjectKey createKey(String key, Date effectiveDate, Class<?> clazz, Provider4Organization provider4Organization)
    {
        ParameterObjectKey pok = new ParameterObjectKey();
        pok.setOrgId(provider4Organization.getCurrentOrganizationId());
        pok.setParamKey(key);
        pok.setParamClass(clazz.getCanonicalName());
        pok.setEffectiveDate(effectiveDate);
        return pok;
    }

    /**
     * 记录参数操作审计日志
     *
     */
    @Transactional
    public <T> void auditPrmModify(String key, Date effectiveDate, String paramClass, ParamOperationDef operation, T newObj,
                                   T oldObj, Provider4Organization provider4Organization, XStream xstream) {
        String org = provider4Organization.getCurrentOrganizationId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = auth == null ? "" : auth.getName();

        ParameterAudit prmAudit = new ParameterAudit();
        prmAudit.setOrgId(org);
        prmAudit.setParamKey(key);
        prmAudit.setParamClass(paramClass);
        prmAudit.setEffectiveDate(effectiveDate);
        prmAudit.setParamOperation(operation);
        if(xstream != null){
            prmAudit.setOldObject(oldObj == null ? "" : xstream.toXML(oldObj));
            prmAudit.setNewObject(newObj == null ? "" : xstream.toXML(newObj));
        }
        else{
            prmAudit.setOldObject(oldObj == null ? "" : JSON.toJSONString(oldObj));
            prmAudit.setNewObject(newObj == null ? "" : JSON.toJSONString(newObj));
        }
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

}
