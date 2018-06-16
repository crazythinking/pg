package net.engining.pg.support.cstruct;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CChar {
	int value() default 1;
	/**
	 * 在指定到整数数字字段的情况下是否要填充0
	 */
	boolean zeroPadding() default true;
	
	/**
	 * 在指定到数字字段的情况下的格式化字段串，使用 {@link MessageFormat}的格式。比{@link #zeroPadding()}优先级高。
	 */
	String formatPattern() default "";
	
	/**
	 * 向左填充空格。默认情况下为 false，即尾随追加空格。
	 */
	boolean leftPadding() default false;
	
	/**
	 * 对于字符串型的字段，在解析时自动执行trim()
	 */
	boolean autoTrim() default true;
	
	/**
	 * 对于 {@link BigDecimal}类型，解析时的小数位
	 */
	int precision() default 0;
	
	/**
	 * 在输出时如果有小数并且指定精度时的截取方式，默认为四舍五入。
	 */
	RoundingMode rounding() default RoundingMode.HALF_UP;
	
	/**
	 * 对于日期型的字段输入和解析的模版，如yyyyMMdd等，见 {@link SimpleDateFormat}
	 */
	String datePattern() default "";
	
	/**
	 * 必填字段
	 */
	boolean required() default false;
	
	/**
	 * 由于JDK规范中没有约定 class 的 getFields方法返回的顺序，而且不保证这个顺序，所以这里必须显式指定字段的顺序。
	 * 同一个类中带有 {@link CChar}或 {@link CBinaryInt}字段以order属性从小到大进行排序，以保证接口的稳定性。
	 */
	int order();
}
