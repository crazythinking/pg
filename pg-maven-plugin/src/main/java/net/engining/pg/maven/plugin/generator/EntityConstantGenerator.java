package net.engining.pg.maven.plugin.generator;

import org.apache.commons.text.WordUtils;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Table;

/**
 * 生成字段名的静态属性，以避免代码中出现字符串常量
 * @author luxue
 *
 */
public class EntityConstantGenerator extends AbstractGenerator {
	
	private String prefix = "P_";

	@Override
	public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
		for (Column col : table.getColumns())
		{
			Field field = new Field();
			field.setVisibility(JavaVisibility.PUBLIC);
			field.setStatic(true);
			field.setFinal(true);
			field.setType(FullyQualifiedJavaType.getStringInstance());
			field.setName(prefix + WordUtils.capitalize(col.getPropertyName()));
			field.setInitializationString('"' + col.getPropertyName() + '"');
			
			entityClass.addField(field);
		}
	}
}
