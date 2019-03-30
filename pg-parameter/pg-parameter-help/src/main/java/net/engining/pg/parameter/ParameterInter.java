package net.engining.pg.parameter;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeBasedTable;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public interface ParameterInter {

    /**
     * 参数如果是全局的，那么机构号用此常量代替
     */
    String GLOBAL_ORGANIZATION_ID = "*";

    /**
     * 参数如果是唯一的，那么参数Key用此常量代替
     */
	String UNIQUE_PARAM_KEY = "*";

    Date maxDate = new Date(Long.MAX_VALUE);

    //最小日期时间+1天，防止mysql5.7以上版本数据库出错，timestamp型数据的取值范围('1970-01-01 00:00:00', '2037-12-31 23:59:59']
    Date minDate = DateUtils.addDays(new Date(0), 1);

    static Date getMinDate() {
        return minDate;
    }

    /**
     * 根据参数类型、参数主键，取在指定日期有效的参数。
     */
    <T> T getParameter(Class<T> paramClass, String key, Date effectiveDate);

    /**
     * 取参数表格
     * @param paramClass 参数类
     */
    <T> TreeBasedTable<String, Date, T> getParameterTable(Class<T> paramClass);

    /**
     * 添加新参数。生效日期由 {@link HasEffectiveDate}指定
     * @param key	参数key
     * @param newParameter	参数对象实例
     */
    <T> void addParameter(String key, T newParameter);

    /**
     * 更新指定参数，生效日期由 {@link HasEffectiveDate}指定。
     * @param key	参数key
     * @param parameter	参数对象实例
     * @param effectiveDate	参数生效日期
     * @param <T>	参数类型泛化
     */
    <T> void updateParameter(String key, T parameter, Date effectiveDate);

    /**
     * 删除指定参数。
     * @param paramClass 参数类
     * @param key 主键
     * @param effectiveDate 生效日期。如果为null，则表示删除所有的生效日期的参数。
     * @return 是否确实删除参数
     */
    <T> boolean removeParameter(Class<T> paramClass, String key, Date effectiveDate);

    default <T> void addUniqueParameter(T newParameter)
    {
        addParameter(UNIQUE_PARAM_KEY, newParameter);
    }

    /**
     * 不指定有效期，更新指定参数
     * @param key	参数key
     * @param parameter	参数对象实例
     * @param <T>	参数类型泛化
     */
    default <T> void updateParameter(String key, T parameter)
    {
        updateParameter(key, parameter, minDate);
    }

    /**
     * 不指定有效期，更新指定全局唯一参数
     */
    default <T> void updateUniqueParameter(T parameter)
    {
        updateParameter(UNIQUE_PARAM_KEY, parameter);
    }

    /**
     * 取唯一、不设有效日期的参数
     */
    default <T> T loadUniqueParameter(Class<T> paramClass)
    {
        return loadParameter(paramClass, UNIQUE_PARAM_KEY);
    }

    /**
     * 返回一个可能为空的参数实例
     */
    default <T> Optional<T> getUniqueParameter(Class<T> paramClass)
    {
        return Optional.ofNullable(getParameter(paramClass, UNIQUE_PARAM_KEY));
    }

    /**
     * 当使用此方法取参数时，如参数不存在，抛出异常
     *
     * @param paramClass 参数类型
     * @param key 参数对象唯一标识
     */
    default <T> T loadParameter(Class<T> paramClass, String key, Date effectiveDate)
    {
        T param = getParameter(paramClass, key, effectiveDate);
        if (param == null)
        {
            throw new ParameterNotFoundException(paramClass.getCanonicalName(), key);
        }
        return param;
    }

    /**
     * 当使用此方法取参数时，如参数不存在，抛出异常
     *
     * @param paramClass 参数类型
     * @param key 参数对象唯一标识
     */
    default <T> T loadParameter(Class<T> paramClass, String key)
    {
        T param = getParameter(paramClass, key);
        if (param == null)
        {
            throw new ParameterNotFoundException(paramClass.getCanonicalName(), key);
        }
        return param;
    }

    /**
     * 不指定有效期，取最新的版本(如果参数启用effectiveDate机制，慎用）
     */
    default <T> T getParameter(Class<T> paramClass, String key)
    {
        return getParameter(paramClass, key, maxDate);
    }

    /**
     * 取指定参数的各key的最新值。（供不启用effectiveDate参数使用，向下兼容）
     */
    default <T> Map<String, T> getParameterMap(Class<T> paramClass)
    {
        return getParameterMap(paramClass, maxDate);
    }

    /**
     * 取给定时间点的有效参数Map
     */
    default <T> Map<String, T> getParameterMap(Class<T> paramClass, Date effectiveDate)
    {
        checkNotNull(paramClass, "需要指定参数类 paramClass");
        checkNotNull(effectiveDate, "需要指定参数生效日期 effectiveDate");

        Map<String, T> map = Maps.newLinkedHashMap();
        TreeBasedTable<String, Date, T> table = getParameterTable(paramClass);
        for (String key : table.rowKeySet())
        {
            T param = getParameter(paramClass, key, effectiveDate);
            if (param != null)
            {
                map.put(key, param);
            }
        }
        return map;
    }

    /**
     * 添加支持 {@link HasKey}的参数
     */
    default void addParameter(HasKey newParameter)
    {
        addParameter(newParameter.getKey(), newParameter);
    }

    /**
     * 更新支持 {@link HasKey}的参数
     */
    default void updateParameter(HasKey parameter)
    {
        updateParameter(parameter.getKey(), parameter);
    }

    /**
     * 不管effectiveDate，指定key全删(如果参数启用effectiveDate机制，慎用）
     */
    default <T> boolean removeParameter(Class<T> paramClass, String key)
    {
        return removeParameter(paramClass, key, null);
    }

}
