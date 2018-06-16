package net.engining.pg.maven.plugin.generator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.utils.GeneratorUtils;
import net.engining.pg.support.db.sequence.JPASequenceFactory;

/**
 * 生成Entity相关的Sequence类
 * @author luxue
 *
 */
public class SequenceHomeGenerator extends AbstractGenerator {
	
	private static final String NAME_PREFIX = "S";
	
	@Override
	public List<CompilationUnit> generateAdditionalClasses(Database database) {
		List<CompilationUnit> result = new ArrayList<CompilationUnit>();
		
		for (String s : database.getSequences())
		{
			TopLevelClass clazz = new TopLevelClass(new FullyQualifiedJavaType(this.getTargetPackage() + ".sequence." + NAME_PREFIX + GeneratorUtils.dbName2ClassName(s)));
			clazz.setVisibility(JavaVisibility.PUBLIC);
			clazz.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Component"));
			clazz.addAnnotation("@Component");
			
			FullyQualifiedJavaType fqjtEntityManager = new FullyQualifiedJavaType("javax.persistence.EntityManager");
			FullyQualifiedJavaType fqjtPersistenceContext = new FullyQualifiedJavaType("javax.persistence.PersistenceContext");
			clazz.addImportedType(fqjtEntityManager);
			clazz.addImportedType(fqjtPersistenceContext);
			Field emField = new Field();
			emField.setVisibility(JavaVisibility.PUBLIC);
			emField.setName("em");
			emField.setType(fqjtEntityManager);
			emField.addAnnotation("@PersistenceContext");
			clazz.addField(emField);
			
			
//			FullyQualifiedJavaType fqjtOperator = new FullyQualifiedJavaType(SequenceOperator.class.getCanonicalName());
//			clazz.addImportedType(fqjtOperator);
//			clazz.addSuperInterface(fqjtOperator);
			
			Method method = new Method();
			method.setName("getNextValue");
			method.setVisibility(JavaVisibility.PUBLIC);
			FullyQualifiedJavaType fqjtBigDecimal = new FullyQualifiedJavaType("java.math.BigDecimal");
			clazz.addImportedType(fqjtBigDecimal);
			method.setReturnType(fqjtBigDecimal);
			
			FullyQualifiedJavaType fqjtHelper = new FullyQualifiedJavaType(JPASequenceFactory.class.getCanonicalName());
			clazz.addImportedType(fqjtHelper);
			method.addBodyLine(MessageFormat.format("return JPASequenceFactory.getNextValue(em, \"{0}\", \"{1}\");", database.getDbType() ,s));
			clazz.addMethod(method);

			result.add(clazz);
		}
		
		return result;
	}
}
