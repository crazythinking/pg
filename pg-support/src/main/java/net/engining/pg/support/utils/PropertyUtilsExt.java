package net.engining.pg.support.utils;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;

/**
 * 对 {@link PropertyUtilsExt}的再次封装.
 * <p/>
 * <h3>说明:</h3>
 * <blockquote>
 * <ol>
 * <li>目的是将原来的 checkedException 异常 转换成 {@link CoreException}</li>
 * </ol>
 * </blockquote>
 * <p/>
 * <h3>{@link PropertyUtilsExt}与 {@link BeanUtils}:</h3>
 * <p/>
 * <blockquote>
 * <p>
 * {@link PropertyUtilsExt}类和{@link BeanUtils}类很多的方法在参数上都是相同的,但返回值不同.<br>
 * {@link BeanUtils}着重于"Bean",返回值通常是{@link String},<br>
 * 而{@link PropertyUtilsExt}着重于属性,它的返回值通常是{@link Object}. 
 * </p>
 * </blockquote>
 */
public final class PropertyUtilsExt {

	private static final Logger logger = LoggerFactory.getLogger(PropertyUtilsExt.class);


    private PropertyUtilsExt() {
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    /**
     * 将 <code>fromObj</code> 中的全部或者一组属性的值,复制到 <code>toObj</code> 对象中.
     * <p/>
     * <h3>注意点:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <ol>
     * <li>如果 <code>toObj</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>fromObj</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>对于Date类型,<span style="color:red">不需要先注册converter</span></li>
     * <li>这种copy都是 <span style="color:red">浅拷贝</span>,复制后的2个Bean的同一个属性可能拥有同一个对象的ref,这个在使用时要小心,特别是对于属性为自定义类的情况 .</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>使用示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * User oldUser = new User();
     * oldUser.setId(5L);
     * oldUser.setMoney(new BigDecimal(500000));
     * oldUser.setDate(new Date());
     * oldUser.setNickName(ConvertUtil.toArray("feilong", "飞天奔月", "venusdrogon"));
     * <p/>
     * User newUser = new User();
     * PropertyUtil.copyProperties(newUser, oldUser, "date", "money", "nickName");
     * logger.debug(JsonUtil.format(newUser));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "date": "2015-09-06 13:27:43",
     * "id": 0,
     * "nickName":         [
     * "feilong",
     * "飞天奔月",
     * "venusdrogon"
     * ],
     * "age": 0,
     * "name": "feilong",
     * "money": 500000,
     * "userInfo": {"age": 0}
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>重构:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * 对于以下代码:
     * </p>
     * <p/>
     * <pre class="code">
     * <p/>
     * private ContactCommand toContactCommand(ShippingInfoSubForm shippingInfoSubForm){
     * ContactCommand contactCommand = new ContactCommand();
     * contactCommand.setCountryId(shippingInfoSubForm.getCountryId());
     * contactCommand.setProvinceId(shippingInfoSubForm.getProvinceId());
     * contactCommand.setCityId(shippingInfoSubForm.getCityId());
     * contactCommand.setAreaId(shippingInfoSubForm.getAreaId());
     * contactCommand.setTownId(shippingInfoSubForm.getTownId());
     * return contactCommand;
     * }
     * <p/>
     * </pre>
     * <p/>
     * <b>可以重构成:</b>
     * <p/>
     * <pre class="code">
     * <p/>
     * private ContactCommand toContactCommand(ShippingInfoSubForm shippingInfoSubForm){
     * ContactCommand contactCommand = new ContactCommand();
     * PropertyUtil.copyProperties(contactCommand, shippingInfoSubForm, "countryId", "provinceId", "cityId", "areaId", "townId");
     * return contactCommand;
     * }
     * </pre>
     * <p/>
     * <p>
     * 可以看出,代码更精简,目的性更明确
     * </p>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>{@link BeanUtils#copyProperties(Object, Object)}与 {@link PropertyUtilsExt#copyProperties(Object, Object)}区别</h3>
     * <p/>
     * <blockquote>
     * <ul>
     * <li>{@link BeanUtils#copyProperties(Object, Object) BeanUtils} 提供类型转换功能,即发现两个JavaBean的同名属性为不同类型时,在支持的数据类型范围内进行转换,<br>
     * 而 {@link PropertyUtilsExt#copyProperties(Object, Object) PropertyUtils}不支持这个功能,但是速度会更快一些.</li>
     * <li>commons-beanutils v1.9.0以前的版本 BeanUtils不允许对象的属性值为 null,PropertyUtils可以拷贝属性值 null的对象.<br>
     * (<b>注:</b>commons-beanutils v1.9.0+修复了这个情况,BeanUtilsBean.copyProperties() no longer throws a ConversionException for null properties
     * of certain data types),具体参阅commons-beanutils的
     * <a href="http://commons.apache.org/proper/commons-beanutils/javadocs/v1.9.2/RELEASE-NOTES.txt">RELEASE-NOTES.txt</a></li>
     * </ul>
     * </blockquote>
     * <p/>
     * <h3>相比较直接调用 {@link PropertyUtilsExt#copyProperties(Object, Object)}的优点:</h3>
     * <blockquote>
     * <ol>
     * <li>将 checkedException 异常转成了 {@link CoreException} RuntimeException,因为通常copy的时候出现了checkedException,也是普普通通记录下log,没有更好的处理方式
     * </li>
     * <li>支持 includePropertyNames 参数,允许针对性copy 个别属性</li>
     * <li>更多,更容易理解的的javadoc</li>
     * </ol>
     * </blockquote>
     *
     * @param toObj                目标对象
     * @param fromObj              原始对象
     * @param includePropertyNames 包含的属性数组名字数组,(can be nested/indexed/mapped/combo)<br>
     *                             如果是null或者empty,将会调用 {@link PropertyUtilsExt#copyProperties(Object, Object)}<br>
     *                             <ol>
     *                             <li>如果没有传入<code>includePropertyNames</code>参数,那么直接调用{@link PropertyUtilsExt#copyProperties(Object, Object)},否则循环调用
     *                             {@link #getProperty(Object, String)}再{@link #setProperty(Object, String, Object)}到<code>toObj</code>对象中</li>
     *                             <li>如果传入的<code>includePropertyNames</code>,含有 <code>fromObj</code>没有的属性名字,将会抛出异常</li>
     *                             <li>如果传入的<code>includePropertyNames</code>,含有 <code>fromObj</code>有,但是 <code>toObj</code>没有的属性名字,会抛出异常,see
     *                             {@link PropertyUtilsBean#setSimpleProperty(Object, String, Object) copyProperties} Line2078</li>
     *                             </ol>
     * @throws NullPointerException 如果 <code>toObj</code> 是null,或者 <code>fromObj</code> 是null
     * @throws CoreException        如果在copy的过程中,有任何的checkedException,将会被转成该异常返回
     * @see #setProperty(Object, String, Object)
     * @see PropertyUtilsBean#copyProperties(Object, Object)
     * @see <a href="http://www.cnblogs.com/kaka/archive/2013/03/06/2945514.html">Bean复制的几种框架性能比较(Apache BeanUtils、PropertyUtils,Spring
     * BeanUtils,Cglib BeanCopier)</a>
     * @since 1.4.1
     */
    public static void copyProperties(Object toObj, Object fromObj, String... includePropertyNames) {
        Validate.notNull(toObj, "toObj [destination bean] not specified!");
        Validate.notNull(fromObj, "fromObj [origin bean] not specified!");

        if (ValidateUtilExt.isNullOrEmpty(includePropertyNames)) {
            try {
                PropertyUtilsExt.copyProperties(toObj, fromObj);
                return;
            } catch (Exception e) {
                throw new ErrorMessageException(ErrorCode.SystemError, e.getMessage());
            }
        }
        for (String propertyName : includePropertyNames) {
            Object value = getProperty(fromObj, propertyName);
            setProperty(toObj, propertyName, value);
        }
    }

    /**
     * 返回一个 <code>bean</code>中指定属性 <code>propertyNames</code><span style="color:green">可读属性</span>,并将属性名/属性值放入一个
     * {@link java.util.LinkedHashMap LinkedHashMap} 中.
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * <b>场景:</b> 取到user bean里面所有的属性成map
     * </p>
     * <p/>
     * <pre class="code">
     * User user = new User();
     * user.setId(5L);
     * user.setDate(new Date());
     * <p/>
     * logger.debug(JsonUtil.format(PropertyUtil.describe(user));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "id": 5,
     * "name": "feilong",
     * "age": null,
     * "date": "2016-07-13 22:18:26"
     * }
     * </pre>
     * <p/>
     * <hr>
     * <p/>
     * <p>
     * <b>场景:</b> 提取user bean "date"和 "id"属性:
     * </p>
     * <p/>
     * <pre class="code">
     * User user = new User();
     * user.setId(5L);
     * user.setDate(new Date());
     * <p/>
     * logger.debug(JsonUtil.format(PropertyUtil.describe(user, "date", "id"));
     * </pre>
     * <p/>
     * 返回的结果,按照指定参数名称顺序:
     * <p/>
     * <pre class="code">
     * {
     * "date": "2016-07-13 22:21:24",
     * "id": 5
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>另外还有一个名为class的属性,属性值是Object的类名,事实上class是java.lang.Object的一个属性</li>
     * <li>如果 <code>propertyNames</code>是null或者 empty,那么获取所有属性的值</li>
     * <li>map的key按照 <code>propertyNames</code> 的顺序</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>原理:</h3>
     * <p/>
     * <blockquote>
     * <ol>
     * <li>取到bean class的 {@link java.beans.PropertyDescriptor}数组</li>
     * <li>循环,找到 {@link java.beans.PropertyDescriptor#getReadMethod()}</li>
     * <li>将 name and {@link PropertyUtilsBean#getProperty(Object, String)} 设置到map中</li>
     * </ol>
     * </blockquote>
     *
     * @param bean          Bean whose properties are to be extracted
     * @param propertyNames 属性名称 (can be nested/indexed/mapped/combo),参见 <a href="../BeanUtil.html#propertyName">propertyName</a>
     * @return 如果 <code>propertyNames</code> 是null或者empty,返回 {@link PropertyUtilsExt#describe(Object)}<br>
     * @throws NullPointerException     如果 <code>bean</code> 是null,或者<code>propertyNames</code> 包含 null的元素
     * @throws IllegalArgumentException 如果 <code>propertyNames</code> 包含 blank的元素
     * @see BeanUtils#describe(Object)
     * @see PropertyUtilsExt#describe(Object)
     * @since 1.8.0
     */
    public static Map<String, Object> describe(Object bean, String... propertyNames) {
        Validate.notNull(bean, "bean can't be null!");
        if (ValidateUtilExt.isNullOrEmpty(propertyNames)) {
            try {
                return PropertyUtilsExt.describe(bean);
            } catch (Exception e) {
                throw new ErrorMessageException(ErrorCode.SystemError, e.getMessage());
            }
        }
        Map<String, Object> map = MapUtilsExt.newLinkedHashMap(propertyNames.length);
        for (String propertyName : propertyNames) {
            map.put(propertyName, getProperty(bean, propertyName));
        }
        return map;
    }

    /**
     * 使用 {@link PropertyUtilsExt#setProperty(Object, String, Object)} 来设置指定bean对象中的指定属性的值.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>不会进行类型转换</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <pre class="code">
     * User newUser = new User();
     * PropertyUtil.setProperty(newUser, "name", "feilong");
     * logger.info(JsonUtil.format(newUser));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {
     * "age": 0,
     * "name": "feilong"
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <h3>注意点:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <ol>
     * <li>如果 <code>bean</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>propertyName</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>propertyName</code> 是blank,抛出 {@link IllegalArgumentException}</li>
     * <li>如果<code>bean</code>没有传入的 <code>propertyName</code>属性名字,会抛出异常,see
     * {@link PropertyUtilsBean#setSimpleProperty(Object, String, Object) setSimpleProperty} Line2078,转成 {@link CoreException}</li>
     * <li>对于Date类型,<span style="color:red">不需要先注册converter</span></li>
     * </ol>
     * </blockquote>
     *
     * @param bean         Bean whose property is to be modified
     * @param propertyName 属性名称 (can be nested/indexed/mapped/combo),参见 <a href="../BeanUtil.html#propertyName">propertyName</a>
     * @param value        Value to which this property is to be set
     * @see BeanUtils#setProperty(Object, String, Object)
     * @see PropertyUtilsExt#setProperty(Object, String, Object)
     * @see BeanUtil#setProperty(Object, String, Object)
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        Validate.notNull(bean, "bean can't be null!");
        Validate.notBlank(propertyName, "propertyName can't be null!");
        try {
            PropertyUtilsExt.setProperty(bean, propertyName, value);
        } catch (Exception e) {
            throw new ErrorMessageException(ErrorCode.SystemError, e.getMessage());
        }
    }

    /**
     * 如果 <code>value</code> isNotNullOrEmpty,那么才调用 {@link #setProperty(Object, String, Object)}.
     * <p/>
     * <h3>注意点:</h3>
     * <p/>
     * <blockquote>
     * <ol>
     * <li>如果 <code>bean</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>propertyName</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>propertyName</code> 是blank,抛出 {@link IllegalArgumentException}</li>
     * <li>如果<code>bean</code>没有传入的 <code>propertyName</code>属性名字,会抛出异常,see
     * {@link PropertyUtilsBean#setSimpleProperty(Object, String, Object)} Line2078</li>
     * <li>对于Date类型,<span style="color:red">不需要先注册converter</span></li>
     * </ol>
     * </blockquote>
     *
     * @param bean         Bean whose property is to be modified
     * @param propertyName 属性名称 (can be nested/indexed/mapped/combo),参见 <a href="../BeanUtil.html#propertyName">propertyName</a>
     * @param value        Value to which this property is to be set
     * @since 1.5.3
     */
    public static void setPropertyIfValueNotNullOrEmpty(Object bean, String propertyName, Object value) {
        if (ValidateUtilExt.isNotNullOrEmpty(value)) {
            setProperty(bean, propertyName, value);
        }
    }

    /**
     * 如果 <code>null != value</code>,那么才调用 {@link #setProperty(Object, String, Object)}.
     * <p/>
     * <h3>注意点:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <ol>
     * <li>如果 <code>bean</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>propertyName</code> 是null,抛出 {@link NullPointerException}</li>
     * <li>如果 <code>propertyName</code> 是blank,抛出 {@link IllegalArgumentException}</li>
     * <li>如果<code>bean</code>没有传入的 <code>propertyName</code>属性名字,会抛出异常,see
     * {@link PropertyUtilsBean#setSimpleProperty(Object, String, Object)} Line2078</li>
     * <li>对于Date类型,<span style="color:red">不需要先注册converter</span></li>
     * </ol>
     * </blockquote>
     *
     * @param bean         Bean whose property is to be modified
     * @param propertyName 属性名称 (can be nested/indexed/mapped/combo),参见 <a href="../BeanUtil.html#propertyName">propertyName</a>
     * @param value        Value to which this property is to be set
     * @see #setProperty(Object, String, Object)
     * @since 1.5.3
     */
    public static void setPropertyIfValueNotNull(Object bean, String propertyName, Object value) {
        if (null != value) {
            setProperty(bean, propertyName, value);
        }
    }

    /**
     * 使用 {@link PropertyUtilsExt#getProperty(Object, String)} 从指定bean对象中取得指定属性名称的值.
     * <p/>
     * <h3>说明:</h3>
     * <blockquote>
     * <ol>
     * <li>不会进行类型转换.</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <p/>
     * <blockquote>
     * <p/>
     * <p>
     * <b>场景:</b> 取list中第一个元素的id
     * </p>
     * <p/>
     * <pre class="code">
     * User user = new User();
     * user.setId(5L);
     * user.setDate(new Date());
     * <p/>
     * List{@code <User>} list = toList(user, user, user);
     * <p/>
     * Long id = PropertyUtil.getProperty(list, "[0].id");
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * 5
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <T>          the generic type
     * @param bean         Bean whose property is to be extracted
     * @param propertyName 属性名称 (can be nested/indexed/mapped/combo),参见 <a href="../BeanUtil.html#propertyName">propertyName</a>
     * @return 如果 <code>bean</code> 是null,抛出 {@link NullPointerException}<br>
     * 如果 <code>propertyName</code> 是null,抛出 {@link NullPointerException}<br>
     * 如果 <code>propertyName</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     * 否则 使用{@link PropertyUtilsExt#getProperty(Object, String)} 从对象中取得属性值
     * @see BeanUtil#getProperty(Object, String)
     * @see BeanUtils#getProperty(Object, String)
     * @see PropertyUtilsExt#getProperty(Object, String)
     * @see PropertyUtilsBean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(Object bean, String propertyName) {
        Validate.notNull(bean, "bean can't be null!");
        Validate.notBlank(propertyName, "propertyName can't be blank!");
        try {
            return (T) PropertyUtilsExt.getProperty(bean, propertyName);
        } catch (Exception e) {
            throw new ErrorMessageException(ErrorCode.SystemError, e.getMessage());
        }
    }

    // [end]

    /**
     * 从指定的 <code>obj</code>中,查找指定类型 <code>toBeFindedClassType</code> 的值.
     * <p/>
     * <h3>说明:</h3>
     * <p/>
     * <blockquote>
     * <ol>
     * <li>如果 <code>ClassUtil.isInstance(obj, toBeFindedClassType)</code> 直接返回 findValue</li>
     * <li>不支持obj是<code>isPrimitiveOrWrapper</code>,<code>CharSequence</code>,<code>Collection</code>,<code>Map</code>类型,自动过滤</li>
     * <li>调用 {@link PropertyUtilsExt#describe(Object, String...)} 再递归查找</li>
     * <li>目前暂不支持从集合里面找到指定类型的值,如果你有相关需求,可以调用 "org.springframework.util.CollectionUtils#findValueOfType(Collection, Class)"</li>
     * </ol>
     * </blockquote>
     * <p/>
     * <h3>示例:</h3>
     * <blockquote>
     * <p/>
     * <p>
     * <b>场景:</b> 从User中找到UserInfo类型的值
     * </p>
     * <p/>
     * <pre class="code">
     * User user = new User();
     * user.setId(5L);
     * user.setDate(new Date());
     * user.getUserInfo().setAge(28);
     * <p/>
     * logger.info(JsonUtil.format(PropertyUtil.findValueOfType(user, UserInfo.class)));
     * </pre>
     * <p/>
     * <b>返回:</b>
     * <p/>
     * <pre class="code">
     * {"age": 28}
     * </pre>
     * <p/>
     * </blockquote>
     *
     * @param <T>                 the generic type
     * @param obj                 要被查找的对象
     * @param toBeFindedClassType the to be finded class type
     * @return 如果 <code>obj</code> 是null或者是empty,返回null<br>
     * 如果 <code>toBeFindedClassType</code> 是null,抛出 {@link NullPointerException}<br>
     * 如果 <code>ClassUtil.isInstance(obj, toBeFindedClassType)</code>,直接返回 <code>obj</code><br>
     * 从对象中查找匹配的类型,如果找不到返回 <code>null</code><br>
     * @see "org.springframework.util.CollectionUtils#findValueOfType(Collection, Class)"
     * @since 1.4.1
     */
    @SuppressWarnings("unchecked")
    public static <T> T findValueOfType(Object obj, Class<T> toBeFindedClassType) {
        if (ValidateUtilExt.isNullOrEmpty(obj)) {
            return null;
        }

        Validate.notNull(toBeFindedClassType, "toBeFindedClassType can't be null/empty!");

        if (ClassUtilsExt.isInstance(obj, toBeFindedClassType)) {
            return (T) obj;
        }

        if (isDonotSupportFindType(obj)) {
            logger.trace("obj:[{}] not support find toBeFindedClassType:[{}]", obj.getClass().getName(), toBeFindedClassType.getName());
            return null;
        }

        //******************************************************************************
        Map<String, Object> describe = describe(obj);

        for (Map.Entry<String, Object> entry : describe.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (null != value && !"class".equals(key)) {
                //级联查询
                T t = findValueOfType(value, toBeFindedClassType);
                if (null != t) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * 一般自定义的command 里面 就是些 string int,list map等对象.
     * 这些我们过滤掉,只取类型是 findedClassType的
     * </p>
     *
     * @param obj the obj
     * @return true, if checks if is can find type
     */
    private static boolean isDonotSupportFindType(Object obj) {
        //一般自定义的command 里面 就是些 string int,list map等对象
        //这些我们过滤掉,只取类型是 findedClassType的
        return ClassUtils.isPrimitiveOrWrapper(obj.getClass())
            || ClassUtilsExt.isInstanceAnyClass(obj, CharSequence.class, Collection.class, Map.class);
    }
}
