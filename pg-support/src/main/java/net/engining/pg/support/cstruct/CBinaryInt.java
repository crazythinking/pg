package net.engining.pg.support.cstruct;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 二进制字段，只取整数部分，最多64位
 * @author chenjun.li
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CBinaryInt {
	boolean bigEndian() default true;
	/**
	 * 字节数,最大值为8
	 * @return
	 */

	int length() default 4;
	/**
	 * 由于JDK规范中没有约定 class 的 getFields方法返回的顺序，而且不保证这个顺序，所以这里必须显式指定字段的顺序。
	 * 同一个类中带有 {@link CChar}或 {@link CBinaryInt}字段以order属性从小到大进行排序，以保证接口的稳定性。
	 */
	int order();
}
