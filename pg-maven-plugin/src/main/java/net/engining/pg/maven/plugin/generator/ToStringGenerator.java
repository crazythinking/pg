package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;

import net.engining.pg.maven.plugin.meta.Table;

import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import com.google.common.base.MoreObjects;

/**
 * 为生成的Key类和Entity添加toString重载
 * 
 * @author chenjun.li
 *
 */
public class ToStringGenerator extends AbstractGenerator {
	
	@Override
	public void afterKeyGenerated(TopLevelClass keyClass) {
		createToString(keyClass);
	}
	
	@Override
	public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
		createToString(entityClass);
	}
	
	private void createToString(TopLevelClass clazz) {
		clazz.addImportedType(new FullyQualifiedJavaType(MoreObjects.class.getCanonicalName()));
//		   @Override  
//		   public String toString()  
//		   {  
//		      return com.google.common.base.Objects.toStringHelper(this)  
//		                .addValue(this.lastName)  
//		                .addValue(this.firstName)  
//		                .addValue(this.employerName)  
//		                .addValue(this.gender)  
//		                .toString();  
//		   }  
		Method m = new Method();
		m.setName("toString");
		m.setVisibility(JavaVisibility.PUBLIC);
		m.setReturnType(FullyQualifiedJavaType.getStringInstance());
		m.addAnnotation("@Override");
		m.addBodyLine("return MoreObjects.toStringHelper(this)");
		for (Field field : clazz.getFields())
		{
			if (!field.isStatic())
			{
				m.addBodyLine(MessageFormat.format("\t.addValue(this.{0})", field.getName()));
			}
		}
		m.addBodyLine("\t.toString();");
		clazz.addMethod(m);
	}

}
