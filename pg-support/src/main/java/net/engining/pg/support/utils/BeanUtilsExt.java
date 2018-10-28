package net.engining.pg.support.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * Bean操作工具类, 扩展 commons-beantutils
 * <p>
 * <pre>
 *     依赖：commons-beanutils
 * </pre>
 *
 * @see org.apache.commons.beanutils.PropertyUtilsBean
 */
public class BeanUtilsExt extends PropertyUtilsBean {

    public BeanUtilsExt() {
        super();
    }

    /**
     * 对象拷贝, 忽略空值.
     */
    public static void copyBeanNotNull2Bean(Object target, Object source) throws Exception {
        PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(source);
        for (int i = 0; i < origDescriptors.length; i++) {
            String name = origDescriptors[i].getName();
            //String type = origDescriptors[i].getPropertyType().toString();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (PropertyUtils.isReadable(source, name) && PropertyUtils.isWriteable(target, name)) {
                try {
                    Object value = PropertyUtils.getSimpleProperty(source, name);
                    if (value != null) {
                        getInstance().setSimpleProperty(target, name, value);
                    }
                } catch (IllegalArgumentException ie) {
                } catch (Exception e) {
                }

            }
        }
    }

    /**
     * 对象拷贝.
     *
     * @param target 目标对象
     * @param source 源对象
     */
    public static void copyBean2Bean(Object target, Object source) {

        ValidateUtilExt.notNull(target, "No destination bean specified.");
        ValidateUtilExt.notNull(source, "No origin bean specified.");

        if (source instanceof DynaBean) { // DynaBean
            DynaProperty origDescriptors[] = ((DynaBean) source).getDynaClass().getDynaProperties();
            for (int i = 0; i < origDescriptors.length; i++) {
                String name = origDescriptors[i].getName();
                if (PropertyUtils.isWriteable(target, name)) {
                    Object value = ((DynaBean) source).get(name);
                    try {
                        getInstance().setSimpleProperty(target, name, value);
                    } catch (Exception e) {
                    }

                }
            }
        } else if (source instanceof Map) { // Map
            Iterator<?> names = ((Map<?, ?>) source).keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (PropertyUtils.isWriteable(target, name)) {
                    Object value = ((Map<?, ?>) source).get(name);
                    try {
                        getInstance().setSimpleProperty(target, name, value);
                    } catch (Exception e) {
                    }

                }
            }
        } else { // 普通bean
            PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(source);
            for (int i = 0; i < origDescriptors.length; i++) {
                String name = origDescriptors[i].getName();
                if ("class".equals(name)) {
                    continue;
                }
                if (PropertyUtils.isReadable(source, name) && PropertyUtils.isWriteable(target, name)) {
                    try {
                        Object value = PropertyUtils.getSimpleProperty(source, name);
                        getInstance().setSimpleProperty(target, name, value);
                    } catch (IllegalArgumentException ie) {
                    } catch (Exception e) {
                    }

                }
            }
        }
    }

    /**
     * 将Bean拷贝到Map中,
     *
     * @param target 目标Map
     * @param source 源对象
     */
    public static void copyBean2Map(Map<String, Object> target, Object source) {
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(source);
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];
            String propname = pd.getName();
            try {
                Object propvalue = PropertyUtils.getSimpleProperty(source, propname);
                target.put(propname, propvalue);
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
        }
    }

    /**
     * 将Map内的key与Bean中属性相同的内容复制到Bean中
     *
     * @param target 目标Bean
     * @param source 源Map
     */
    public static void copyMap2Bean(Object target, Map<?, ?> source) {
        if ((target == null) || (source == null)) {
            return;
        }
        Iterator<?> names = source.keySet().iterator();
        while (names.hasNext()) {
            String name = (String) names.next();
            if (name == null) {
                continue;
            }
            Object value = source.get(name);
            try {
                if (value == null) {
                    continue;
                }

                Class<?> clazz = PropertyUtils.getPropertyType(target, name);
                // 属性在Bean中不存在.
                if (null == clazz) {
                    continue;
                }
                String className = clazz.getName();
                if (className.equalsIgnoreCase("java.sql.Timestamp")) {
                    if (value.equals("")) {
                        continue;
                    }
                }
                if (className.equalsIgnoreCase("java.util.Date")) {
                    value = new java.util.Date(((java.sql.Timestamp) value).getTime());
                }

                getInstance().setSimpleProperty(target, name, value);
            } catch (NoSuchMethodException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            } catch (InvocationTargetException e) {
                continue;
            }
        }
    }

    /**
     * Map内的key与Bean中属性相同的内容复制到BEAN中
     * 对于存在空值的取默认值
     *
     * @param target       目标Bean
     * @param source       源Map
     * @param defaultValue 默认值
     */
    public static void copyMap2Bean(Object target, Map<?, ?> source, String defaultValue) {
        if ((target == null) || (source == null)) {
            return;
        }
        Iterator<?> names = source.keySet().iterator();
        while (names.hasNext()) {
            String name = (String) names.next();
            if (name == null) {
                continue;
            }
            Object value = source.get(name);
            try {
                Class<?> clazz = PropertyUtils.getPropertyType(target, name);
                if (null == clazz) {
                    continue;
                }
                String className = clazz.getName();
                if (className.equalsIgnoreCase("java.sql.Timestamp")) {
                    if (value == null || value.equals("")) {
                        continue;
                    }
                }
                if (className.equalsIgnoreCase("java.lang.String")) {
                    if (value == null) {
                        value = defaultValue;
                    }
                }
                getInstance().setSimpleProperty(target, name, value);
            } catch (NoSuchMethodException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            } catch (InvocationTargetException e) {
                continue;
            }
        }
    }
}
