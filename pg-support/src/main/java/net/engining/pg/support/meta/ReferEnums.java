package net.engining.pg.support.meta;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 用于指定参数字段中涉及的枚举类型，以便于Domain的生成，如
 * <pre>@ReferEnums({AuthReason.class, AuthAction.class})
 *   public Map<AuthReason, AuthAction> reasonActions;
 * </pre>
 * 这样就会把这些不适用于 {@link PropertyInfo}的字段加入Domain的生成列表，以便于界面的编写。
 * @author licj
 *
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface ReferEnums 
{
	Class<? extends Enum<?>>[] value();
}
