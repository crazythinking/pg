package net.engining.pg.support.db;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

/**
 * 扩展hibernate的类型，使字符串类型可以自动trim。在Hibernate 3.6之后，由于 Type体系的重写，取数据的关键函数 {@link #nullSafeGet(java.sql.ResultSet, String, org.hibernate.engine.SessionImplementor)}成为了final的。
 * 所以这里比以前需要一个额外的类来进行extract。这个类几乎是完全照抄 {@link StringType}，唯一不同处在于调父类构造函数时使用了 {@link AutoTrimStringTypeDescriptor}。
 * 每次Hibernate升级最好评估一下对本类的影响。
 * @author chenjun.li
 */
public class AutoTrimStringType extends AbstractSingleColumnStandardBasicType<String> implements DiscriminatorType<String>  
{
	private static final long serialVersionUID = 1L;

	public AutoTrimStringType() {
		super( VarcharTypeDescriptor.INSTANCE, /* 与 StringType唯一不同  --> */ AutoTrimStringTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "string";
	}

	@Override
	protected boolean registerUnderJavaType() {
		return true;
	}

	public String objectToSQLString(String value, Dialect dialect) throws Exception {
		return '\'' + value + '\'';
	}

	public String stringToObject(String xml) throws Exception {
		return xml;
	}

	public String toString(String value) {
		return value;
	}
}
