package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Table;

/**
 * 为Entity生成相应的Mapping类，其内getAllMappings方法，为每个字段生成对应的(column name | com.querydsl.core.types.Expression)
 * @author luxue
 *
 */
public class AllFieldMappingsGenerator extends AbstractGenerator
{
	
	private static final String NAME_PREFIX = "M";
	
	@Override
	public List<CompilationUnit> generateAdditionalClasses(Table table,	Database database)
	{
		FullyQualifiedJavaType fqjtEntity = table.getJavaClass();

		TopLevelClass clazz = new TopLevelClass(new FullyQualifiedJavaType(this.getTargetPackage() + ".mapping." + NAME_PREFIX + fqjtEntity.getShortName()));
		clazz.setVisibility(JavaVisibility.PUBLIC);
		
		FullyQualifiedJavaType fqjtQ = new FullyQualifiedJavaType(fqjtEntity.getPackageName() + ".Q" + fqjtEntity.getShortName());
		
		clazz.addImportedType(fqjtQ);
		
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setStatic(true);
		method.addAnnotation("@SuppressWarnings(\"rawtypes\")");
		method.setName("getAllMappings");
		
		FullyQualifiedJavaType fqjtMap = new FullyQualifiedJavaType("java.util.Map");
		FullyQualifiedJavaType fqjtExpression = new FullyQualifiedJavaType("com.querydsl.core.types.Expression");
		fqjtMap.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		fqjtMap.addTypeArgument(fqjtExpression);
		clazz.addImportedType(fqjtMap);
		clazz.addImportedType(fqjtExpression);
		method.setReturnType(fqjtMap);
		
		clazz.addImportedType(new FullyQualifiedJavaType("java.util.HashMap"));
		
		method.addBodyLine("HashMap<String, Expression> result = new HashMap<String, Expression>();");
		method.addBodyLine(MessageFormat.format("{0} q = {0}.{1};", fqjtQ.getShortName(), StringUtils.uncapitalize(fqjtEntity.getShortName())));
		
		for (Column col : table.getColumns())
			method.addBodyLine(MessageFormat.format("result.put(\"{0}\", q.{0});", col.getPropertyName()));

		method.addBodyLine("return result;");
		
		clazz.addMethod(method);

		List<CompilationUnit> result = new ArrayList<CompilationUnit>();
		result.add(clazz);
		return result;
	}
}
