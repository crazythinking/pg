package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;

import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Table;

/**
 * 为Entity添加fillDefaultValues方法
 * @author Eric Lu
 *
 */
public class FillDefaultValuesGenerator extends AbstractGenerator {
	@Override
	public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
		Method method = new Method();
		method.setName("fillDefaultValues");
		method.setVisibility(JavaVisibility.PUBLIC);
		
		for (Column col : table.getColumns())
		{
			//主键字段不设默认值
			if(table.getPrimaryKeyColumns().contains(col))
				continue;
			
			//主键字段不设默认值
			if (col.isIdentity())
				continue;
			
			//非必填字段不设默认值
			if (!col.isMandatory())	
				continue;
			String type = col.getJavaType().getShortName();
			String value = "null";
			if (type.equals("String"))
				value = "\"\"";
			else if (type.equals("BigDecimal"))
				value = "BigDecimal.ZERO";
			else if (type.equals("Integer"))
				value = "0";
			else if (type.equals("Long"))
				value = "0l";
			else if (type.equals("Date"))
				value = "new Date()";
			else if (type.equals("Boolean"))
				value = "false";
			else if (col.getDomain() != null)
				value = MessageFormat.format("{0}.values()[0]", col.getDomain().getType().getShortName());
			//其它就不管了，出现问题再说
			method.addBodyLine(MessageFormat.format("if ({0} == null) {0} = {1};",
				col.getPropertyName(),
				value));
		}

		if (!method.getBodyLines().isEmpty())	//处理那些没有可以fill字段的情况
			entityClass.addMethod(method);
	}
}
