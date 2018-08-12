package net.engining.pg.support.utils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 类操作工具.
 */
public abstract class ClassUtilsExt {

    public static final String ARRAY_SUFFIX = "[]";

    private static final String INTERNAL_ARRAY_PREFIX = "[L";

    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(16);

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);

        Set<Class<?>> primitiveTypeNames = new HashSet<Class<?>>(16);
        primitiveTypeNames.addAll(primitiveWrapperTypeMap.values());
        primitiveTypeNames
            .addAll(Arrays.asList(new Class<?>[] {boolean[].class, byte[].class, char[].class, double[].class, float[].class, int[].class, long[].class, short[].class}));
        for (Iterator<Class<?>> it = primitiveTypeNames.iterator(); it.hasNext(); ) {
            Class<?> primitiveClass = (Class<?>) it.next();
            primitiveTypeNameMap.put(primitiveClass.getName(), primitiveClass);
        }
    }

    /**
     * 判断一个对象 obj 是不是某个类 clazz的实例.
     *
     * @param obj   实例
     * @param clazz 类
     * @return 如果 obj 是此类的实例,则返回 true
     * @see java.lang.Class#isInstance(Object)
     */
    public static boolean isInstance(Object obj, Class<?> clazz) {
        return null == clazz ? false : clazz.isInstance(obj);
    }

    /**
     * 判断 obj 是否是任意的一个clazzs的实例.
     *
     * @param obj    任意的对象
     * @param clazzs 类
     * @return 如果 <code>null == clazzs</code> ,返回 false<br>
     */
    public static boolean isInstanceAnyClass(Object obj, Class<?>... clazzs) {
        if (null == clazzs) {
            return false;
        }

        for (Class<?> klass : clazzs) {
            if (isInstance(obj, klass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> forNameWithThreadContextClassLoader(String name) throws ClassNotFoundException {
        return forName(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * @param name
     * @param caller
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> forNameWithCallerClassLoader(String name, Class<?> caller) throws ClassNotFoundException {
        return forName(name, caller.getClassLoader());
    }

    /**
     * @param caller
     * @return
     */
    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }

    /**
     *
     */
    public static ClassLoader getClassLoader() {
        return getClassLoader(ClassUtilsExt.class);
    }

    /**
     * 获取类加载器.
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
        }

        if (cl == null) {
            cl = clazz.getClassLoader();
        }
        return cl;
    }



    /**
     *
     */
    public static Class<?> forName(String name) throws ClassNotFoundException {
        return forName(name, getClassLoader());
    }

    /**
     * 根据类名获取类.
     * Replacement for <code>Class.forName()</code> that also returns Class
     * instances for primitives (like "int") and array class names (like
     * "String[]").
     */
    public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        int internalArrayMarker = name.indexOf(INTERNAL_ARRAY_PREFIX);
        if (internalArrayMarker != -1 && name.endsWith(";")) {
            String elementClassName = null;
            if (internalArrayMarker == 0) {
                elementClassName = name.substring(INTERNAL_ARRAY_PREFIX.length(), name.length() - 1);
            } else if (name.startsWith("[")) {
                elementClassName = name.substring(1);
            }
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = getClassLoader();
        }
        return classLoaderToUse.loadClass(name);
    }

    /**
     *
     */
    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = (Class<?>) primitiveTypeNameMap.get(name);
        }
        return result;
    }

    /**
     *
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        return org.springframework.util.ClassUtils.isAssignable(lhsType, rhsType);
    }

    public static String toShortString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
    }
}
