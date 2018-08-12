package net.engining.pg.support.utils;

import java.lang.reflect.Array;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 数组操作工具类.
 *
 * @see org.apache.commons.lang3.ArrayUtils
 */
public abstract class ArrayUtilsExt extends ArrayUtils {

    /**
     * 创建指定类型和大小的数组.
     * <pre>
     *      int[] x = {length};
     *      Array.newInstance(componentType, x);
     * </pre>
     *
     * @param clazzType 数组类型
     * @param length    数组大小
     * @return the new array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newInstance(Class<T> clazzType, int length) {
        return (T[]) Array.newInstance(clazzType, length);
    }
}
