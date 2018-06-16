package net.engining.pg.support.db;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

/**
 * 用于增加autotrim支持，Hibernate-Core 3.6.0及以上适用
 * @author chenjun.li
 *
 */
public class AutoTrimStringTypeDescriptor extends StringTypeDescriptor {
	
	private static final long serialVersionUID = 1L;
	public static final AutoTrimStringTypeDescriptor INSTANCE = new AutoTrimStringTypeDescriptor();

	public <X> String wrap(X value, WrapperOptions options)
	{
		String result = super.wrap(value, options);
		return StringUtils.trim(result);
	};
	
}
