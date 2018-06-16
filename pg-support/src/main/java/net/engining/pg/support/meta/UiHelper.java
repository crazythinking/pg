package net.engining.pg.support.meta;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Deprecated
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface UiHelper {

}
