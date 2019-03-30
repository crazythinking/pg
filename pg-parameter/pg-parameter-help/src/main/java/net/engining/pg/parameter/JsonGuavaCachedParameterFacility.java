package net.engining.pg.parameter;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.TreeBasedTable;
import net.engining.pg.parameter.entity.enums.ParamOperationDef;
import net.engining.pg.parameter.entity.model.ParameterObject;
import net.engining.pg.support.core.context.Provider4Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 参数体系的本地缓存实现，使用Json结构持久化参数对象；使用google Guava Cache作为本地缓存
 */
public class JsonGuavaCachedParameterFacility extends ParameterFacility implements ApplicationListener<ParameterChangedEvent>, InitializingBean {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private Provider4Organization provider4Organization;

    //最小日期时间+1天，防止mysql5.7以上版本数据库出错，timestamp型数据的取值范围('1970-01-01 00:00:00', '2037-12-31 23:59:59']
    //private static Date minDate = DateUtils.addDays(new Date(0), 1);

    //默认5分钟过期
    private long expireDuration = 5;

    private TimeUnit expireTimeUnit = TimeUnit.MINUTES;

    private Ticker ticker = Ticker.systemTicker();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {

    }

    // (org|class_name)   -   map<key, value>
    private LoadingCache<String, TreeBasedTable<String, Date, Object>> cache;

    private Object convertParameterObject(String parameterStream, String paramClass) throws ClassNotFoundException {

        return JSON.parseObject(parameterStream, Class.forName(paramClass));
    }

    @Override
    public void afterPropertiesSet() {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireDuration, expireTimeUnit)
                .ticker(ticker)
                .build(new CacheLoader<String, TreeBasedTable<String, Date, Object>>() {

                    @Override
                    public TreeBasedTable<String, Date, Object> load(String key) throws Exception {
                        List<ParameterObject> results = fetchParamsByLocalCacheKey(key);

                        TreeBasedTable<String, Date, Object> ret = TreeBasedTable.create();
                        for (ParameterObject result : results) {
                            Object obj = convertParameterObject(result.getParamObject(), result.getParamClass());
                            ret.put(result.getParamKey(), result.getEffectiveDate(), obj);
                        }
                        logger.info("加载参数[{}]，计[{}]条", key, ret.size());
                        return ret;
                    }
                });
    }

    @Override
    public void onApplicationEvent(ParameterChangedEvent event) {
        applicationCacheEventAction(event, cache);
    }

    @Override
    public <T> T getParameter(Class<T> paramClass, String key, Date effectiveDate) {
        return defaultGetParameter(paramClass, key, effectiveDate);
    }

    public CacheStats getCurrentStats() {
        return cache.stats();
    }

    @Override
    public <T> TreeBasedTable<String, Date, T> getParameterTable(Class<T> paramClass) {
        return defaultGetParameterTable(paramClass, provider4Organization, cache);
    }

    //============   添加参数   ===========

    @Override
    @Transactional
    public <T> void addParameter(String key, T newParameter) {
        ParameterObject obj = defaultAddParameter(key, newParameter, provider4Organization, null);

        // 记录操作日志
        auditPrmModify(
                key,
                obj.getEffectiveDate(),
                obj.getParamClass(),
                ParamOperationDef.INSERT, newParameter,
                null,
                provider4Organization,
                null
        );

        //刷新
        ctx.publishEvent(new ParameterChangedEvent(this, obj.getOrgId(), obj.getParamClass(), obj.getParamKey()));
    }

    //============  更新参数   ===========

    @Override
    @Transactional
    public <T> void updateParameter(String key, T parameter, Date effectiveDate) {
        Map<String, Serializable> retMap = defaultUpdateParameter(key, parameter, effectiveDate, provider4Organization, null);
        String oldParamJson = (String) retMap.get("oldParamStr");
        ParameterObject obj = (ParameterObject) retMap.get("parameterObject");

        // 记录操作日志
        auditPrmModify(
                key,
                obj.getEffectiveDate(),
                obj.getParamClass(),
                ParamOperationDef.UPDATE,
                parameter,
                JSON.parseObject(oldParamJson, parameter.getClass()),
                provider4Organization,
                null
        );

        //刷新
        ctx.publishEvent(new ParameterChangedEvent(this, obj.getOrgId(), obj.getParamClass(), obj.getParamKey()));
    }

    //============ 删除参数   ===========

    @Override
    @Transactional
    public <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate) {
        boolean ret = defaultRemoveParameter(paramClass, key, effectiveDate, provider4Organization, null);

        ctx.publishEvent(
                new ParameterChangedEvent(this, provider4Organization.getCurrentOrganizationId(), paramClass.getCanonicalName(), key)
        );

        return ret;
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
