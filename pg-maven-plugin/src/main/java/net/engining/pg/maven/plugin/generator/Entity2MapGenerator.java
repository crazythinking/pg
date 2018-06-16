package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Table;
import net.engining.pg.support.utils.DataTypeUtils;
import net.engining.pg.support.utils.HasMapping;

/**
 * 为Entity生成相应的convertToMap和updateFromMap方法
 * @author luxue
 *
 */
public class Entity2MapGenerator extends AbstractGenerator
{
	@Override
	public void afterEntityGenerated(TopLevelClass entityClass, Table table)
	{
		//建立 convertToMap方法
		FullyQualifiedJavaType fqjtSerializable = new FullyQualifiedJavaType("java.io.Serializable");
		entityClass.addImportedType(fqjtSerializable);
		FullyQualifiedJavaType fqjtMap = new FullyQualifiedJavaType("java.util.Map");
		fqjtMap.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		fqjtMap.addTypeArgument(fqjtSerializable);
		entityClass.addImportedType(fqjtMap);
		
		//添加接口
		FullyQualifiedJavaType fqjtMapping = new FullyQualifiedJavaType(HasMapping.class.getCanonicalName());
		entityClass.addImportedType(fqjtMapping);
		entityClass.addSuperInterface(fqjtMapping);
		
		Method to = new Method();
		to.setName("convertToMap");
		to.setVisibility(JavaVisibility.PUBLIC);
		to.setReturnType(fqjtMap);
		FullyQualifiedJavaType fqjtHashMap = new FullyQualifiedJavaType("java.util.HashMap");
		fqjtHashMap.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		fqjtHashMap.addTypeArgument(fqjtSerializable);
		entityClass.addImportedType(fqjtHashMap);
		
		to.addBodyLine("HashMap<String, Serializable> map = new HashMap<String, Serializable>();");
		for (Column col : table.getColumns())
		{
			if (col.isLob())
				continue;

			entityClass.addImportedType(col.getJavaType());
			if (col.getDomain() == null)
				//普通类型
				to.addBodyLine(MessageFormat.format("map.put(\"{0}\", {0});", col.getPropertyName()));
			else
				//枚举类型用字符串
				to.addBodyLine(MessageFormat.format("map.put(\"{0}\", {0} == null ? null : {0}.toString());", col.getPropertyName()));
		}
		to.addBodyLine("return map;");
		entityClass.addMethod(to);

		
		//updateFromMap
		Method uf = new Method();
		uf.setName("updateFromMap");
		uf.setVisibility(JavaVisibility.PUBLIC);
		uf.addParameter(new Parameter(fqjtMap, "map"));

		setupSetFields(entityClass, table, uf, "this");
		
		entityClass.addMethod(uf);
	}
	
	private void setupSetFields(TopLevelClass clazz, Table table, Method method, String instanceName)
	{
		clazz.addImportedType(new FullyQualifiedJavaType(DataTypeUtils.class.getCanonicalName()));
		for (Column col : table.getColumns())
		{
			if (col.isLob())
				continue;

			clazz.addImportedType(col.getJavaType());

			if (col.getDomain() != null)
			{
				//枚举值
				method.addBodyLine(MessageFormat.format("if (map.containsKey(\"{0}\")) {1}.set{2}(DataTypeUtils.getEnumValue(map.get(\"{0}\"), {3}.class));",
						col.getPropertyName(),
						instanceName,
						StringUtils.capitalize(col.getPropertyName()),
						col.getJavaType().getShortName()));
			}
			else
			{
				method.addBodyLine(MessageFormat.format("if (map.containsKey(\"{0}\")) {1}.set{2}(DataTypeUtils.get{3}Value(map.get(\"{0}\")));",
						col.getPropertyName(),
						instanceName,
						StringUtils.capitalize(col.getPropertyName()),
						col.getJavaType().getShortName()));
			}
		}
	}
}
