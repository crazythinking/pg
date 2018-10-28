package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;

import org.apache.commons.text.WordUtils;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Table;

public class PrimaryKeyGenerator extends AbstractGenerator {
	@Override
	public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
		FullyQualifiedJavaType inter = new FullyQualifiedJavaType(PrimaryKeyGenerator.class.getCanonicalName());
		entityClass.addImportedType(table.getJavaKeyClass());
		inter.addTypeArgument(table.getJavaKeyClass());
		entityClass.addImportedType(inter);
		entityClass.addSuperInterface(inter);
		
		Method method = new Method();
		method.setName("pk");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(table.getJavaKeyClass());
		
		if (table.getPrimaryKeyColumns().size() > 1)
		{
			method.addBodyLine(MessageFormat.format("{0} key = new {0}();", table.getJavaKeyClass().getShortName()));
			for (Column col : table.getPrimaryKeyColumns())
			{
				method.addBodyLine(MessageFormat.format("key.set{0}({1});", WordUtils.capitalize(col.getPropertyName()), col.getPropertyName()));
			}
			method.addBodyLine("return key;");
		}
		else
		{
			method.addBodyLine(MessageFormat.format("return {0};", table.getPrimaryKeyColumns().get(0).getPropertyName()));
		}
		entityClass.addMethod(method);
	}
}
