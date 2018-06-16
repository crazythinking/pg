package net.engining.pg.maven.plugin.meta;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;

/**
 * 实体类中枚举类型的属性，对应枚举类型的表字段，用于准备生成Column时需要的内容
 * @author Eric Lu
 *
 */
public class Domain {
	/**
	 * 字段在实体类中对应属性的类名，如：Integer
	 */
	private String code;
	
	/**
	 * 属性名
	 */
	private String name;
	
	/**
	 * 对应的数据库字段类型
	 */
	private String dbType;
	
	/**
	 * 自身的全类型名，需由总控填写
	 */
	private FullyQualifiedJavaType type;
	
	//TODO 不生成U对象，可以考虑去掉
	private FullyQualifiedJavaType supportClientType;
	/**
	 * 可用值列表，key为取值，value为描述, 需要保持有序，所以使用 {@link LinkedHashMap}，这里不作类型处理
	 */
	private LinkedHashMap<String, String> valueMap;
	
	public boolean hasValueMap()
	{
		return valueMap != null && !valueMap.isEmpty();
	}
	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getDbType()
	{
		return dbType;
	}
	public void setDbType(String dbType)
	{
		this.dbType = dbType;
	}
	public LinkedHashMap<String, String> getValueMap()
	{
		return valueMap;
	}
	public void setValueMap(LinkedHashMap<String, String> valueMap)
	{
		this.valueMap = valueMap;
	}
	public FullyQualifiedJavaType getType()
	{
		return type;
	}
	public void setType(FullyQualifiedJavaType type)
	{
		this.type = type;
	}
	public FullyQualifiedJavaType getSupportClientType() {
		return supportClientType;
	}
	public void setSupportClientType(FullyQualifiedJavaType supportClientType) {
		this.supportClientType = supportClientType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		Domain rhs = (Domain) obj;
		return new EqualsBuilder()
				.append(supportClientType, rhs.supportClientType)
				.append(type, rhs.type)
				.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(type)
			.append(supportClientType)
			.toHashCode();
	}
}
