package net.engining.pg.support.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * 枚举工具类. 扩展commons-lang3的EnumUtils
 *
 * @see org.apache.commons.lang3.EnumUtils
 */
public final class EnumUtilsExt extends EnumUtils {

    private EnumUtilsExt() {
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }
    
    @SuppressWarnings("unchecked")
	public static <E extends Enum<E>> List<E> enumFilter(Class<E> enumClass, E... v){
		List<E> ls = Arrays.asList(v);
		
		List<E> lsv = getEnumList(enumClass);
		
		List<E> ems = lsv.stream().filter(x -> !ls.contains(x)).collect(Collectors.toList());
		
//		ems.stream().forEach(x ->{
//			System.out.println(x);
//		});;
		
		return ems;
	}
    
    /**
     * 根据枚举属性名称、属性的值获取枚举，忽略大小写.
     *
     * @param enumClass      枚举类
     * @param propertyName   属性名称
     * @param specifiedValue 属性值
     */
    public static <E extends Enum<?>, T> E getEnumByPropertyValueIgnoreCase(Class<E> enumClass, String propertyName, T specifiedValue) {
        return getEnumByPropertyValue(enumClass, propertyName, specifiedValue, true);
    }

    /**
     * 根据枚举属性名称、属性的值获取枚举，区分大小写.
     *
     * @param enumClass      枚举类
     * @param propertyName   属性名称
     * @param specifiedValue 属性值
     */
    public static <E extends Enum<?>, T> E getEnumByPropertyValue(Class<E> enumClass, String propertyName, T specifiedValue) {
        return getEnumByPropertyValue(enumClass, propertyName, specifiedValue, false);
    }

    /**
     * 根据枚举属性名称、属性的值获取枚举.
     * <pre>
     *     public enum HttpMethodType{
     *          GET("get"),
     *          POST("post");
     *
     *          private String method;
     *          private HttpMethodType(String method){
     *              this.method = method;
     *          }
     *
     *          public String getMethod(){
     *              return method;
     *          }
     *     }
     *
     *     要取HttpMethodType 里面的 method属性值是 "get"的枚举(区分大小写),调用方式:
     *     EnumUtil.getEnumByPropertyValue(HttpMethodType.class, "method", "get")
     * </pre>
     *
     * @param enumClass      枚举类
     * @param propertyName   属性名称
     * @param specifiedValue 属性值
     * @param ignoreCase     是否忽视大小写
     */
    private static <E extends Enum<?>, T> E getEnumByPropertyValue(Class<E> enumClass, String propertyName, T specifiedValue, boolean ignoreCase) {
        ValidateUtilExt.notNull(enumClass, "枚举类不能为null.");
        ValidateUtilExt.notBlank(propertyName, "propertyName can't be null/empty!");

        // 如果Class 对象不表示枚举类型,则返回枚举类的元素或 null.
        E[] enumConstants = enumClass.getEnumConstants();

        for (E e : enumConstants) {
            // 枚举的类型是class
            Object propertyValue = PropertyUtilsExt.getProperty(e, propertyName);
            if (isEquals(propertyValue, specifiedValue, ignoreCase)) {
                return e;
            }
        }

        return null;
    }

    /**
     * 检查枚举属性的值与指定的值是否相等.
     *
     * @param propertyValue  枚举属性的值
     * @param specifiedValue 指定的值
     * @param ignoreCase     忽略大小写
     */
    private static <T> boolean isEquals(Object propertyValue, T specifiedValue, boolean ignoreCase) {
        // 如果属性
        if (propertyValue == null || specifiedValue == null) {
            return propertyValue == specifiedValue;
        } else if (propertyValue == specifiedValue) {
            return true;
        }

        String propertyValueString = propertyValue.toString();
        String specifiedValueString = specifiedValue.toString();
        return ignoreCase ? StringUtils.equalsIgnoreCase(propertyValueString, specifiedValueString) : StringUtils.equals(propertyValueString, specifiedValueString);
    }
}
