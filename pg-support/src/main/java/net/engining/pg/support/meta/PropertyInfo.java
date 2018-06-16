package net.engining.pg.support.meta;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
public @interface PropertyInfo
{
	String name() default "";
	String ruleType() default "text";
	int precision() default 0;
	int length() default 0;
	String hint() default "";
}
