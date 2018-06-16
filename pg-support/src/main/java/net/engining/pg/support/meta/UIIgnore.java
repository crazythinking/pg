package net.engining.pg.support.meta;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 指示插件不要生成U对象
 * @author binarier
 *
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface UIIgnore
{

}
