package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;

import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import com.google.common.base.Objects;

/**
 * 为生成的Key类添加equals和hashCode重载;
 * 只对Entity的复合主键Key，生成相应的equals和hashCode方法，因为主键相同既考虑为同一条记录；
 * 对于其他非主键字段，通过生成的toString方法进行比较即可；
 * 
 * @author chenjun.li
 *
 */
public class EqualsHashCodeGenerator extends AbstractGenerator {

	@Override
	public void afterKeyGenerated(TopLevelClass keyClass) {
		boolean first;
		keyClass.addImportedType(new FullyQualifiedJavaType(Objects.class.getCanonicalName()));

		// public boolean equals(Object obj) {
		// if (obj == null) { return false; }
		// if (obj == this) { return true; }
		// if (obj.getClass() != getClass()) {
		// return false;
		// }
		// final MyClass rhs = (MyClass) obj;
		// return
		// Objects.equal(this.field1, rhs.rifle1)
		// && Objects.equals(field2, rhs.field2);
		// }
		Method equals = new Method();
		equals.setName("equals");
		equals.setVisibility(JavaVisibility.PUBLIC);
		equals.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
		equals.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "obj"));
		equals.addAnnotation("@Override");
		equals.addBodyLine("if (obj == null) { return false; }");
		equals.addBodyLine("if (obj == this) { return true; }");
		equals.addBodyLine("if (obj.getClass() != getClass()) {return false;}");
		equals.addBodyLine(MessageFormat.format("final {0} rhs = ({0}) obj;", keyClass.getType().getShortName()));
		equals.addBodyLine("return");
		first = true;
		for (Field field : keyClass.getFields()) {
			equals.addBodyLine(MessageFormat.format("\t{0} Objects.equal(this.{1}, rhs.{1})", (!first ? "&&" : ""),
					field.getName()));
			first = false;
		}
		equals.addBodyLine(";");
		keyClass.addMethod(equals);

		// @Override
		// public int hashCode()
		// {
		// return Objects.hashCode(
		// this.lastName,
		// this.firstName,
		// this.employerName,
		// this.gender);
		// }
		Method hashCode = new Method();
		keyClass.addMethod(hashCode);
		hashCode.setName("hashCode");
		hashCode.setVisibility(JavaVisibility.PUBLIC);
		hashCode.setReturnType(FullyQualifiedJavaType.getIntInstance());
		hashCode.addAnnotation("@Override");
		hashCode.addBodyLine("return Objects.hashCode(");

		first = true;
		for (Field field : keyClass.getFields()) {
			hashCode.addBodyLine(MessageFormat.format("\t{0}this.{1}", (!first ? "," : ""), field.getName()));
			first = false;
		}

		hashCode.addBodyLine("\t);");
	}

}
