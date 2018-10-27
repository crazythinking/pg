package net.engining.pg.maven.plugin.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.Interface;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Table;

/**
 * 生成用于Spring Data 和 Querydsl组合支持的Repository
 * @author chenjun.li
 *
 */
public class SpringDataRepositoryGenerator extends AbstractGenerator
{
	private static final String NAME_PREFIX = "R";
	
	@Override
	public List<CompilationUnit> generateAdditionalClasses(Table table, Database database)
	{
//		import org.springframework.data.querydsl.QueryDslPredicateExecutor;
//		import org.springframework.data.repository.CrudRepository;
//
//		import com.aif.sandbox.model.Person;
//
//		public interface PersonRepos extends CrudRepository<Person, String>, QueryDslPredicateExecutor<Person> {
//			
//			List<Person> findByAgeLessThan(int age);
//		}
		Interface inter = new Interface(new FullyQualifiedJavaType(this.getTargetPackage() + ".repositroy." + NAME_PREFIX + table.getJavaClass().getShortName()));
		inter.setVisibility(JavaVisibility.PUBLIC);
		
		FullyQualifiedJavaType fqjtCrudRepository = new FullyQualifiedJavaType("org.springframework.data.jpa.repository.JpaRepository");
		inter.addImportedType(table.getJavaClass());
		fqjtCrudRepository.addTypeArgument(table.getJavaClass());
		inter.addImportedType(table.getJavaKeyClass());
		fqjtCrudRepository.addTypeArgument(table.getJavaKeyClass());
		inter.addImportedType(fqjtCrudRepository);
		inter.addSuperInterface(fqjtCrudRepository);
		
		FullyQualifiedJavaType fqjtQueryDsl = new FullyQualifiedJavaType("org.springframework.data.querydsl.QuerydslPredicateExecutor");
		fqjtQueryDsl.addTypeArgument(table.getJavaClass());
		inter.addImportedType(fqjtQueryDsl);
		inter.addSuperInterface(fqjtQueryDsl);
		
		//按索引添加findBy
		//返回值为列表 
		FullyQualifiedJavaType fqjtList = new FullyQualifiedJavaType(List.class.getCanonicalName());
		inter.addImportedType(fqjtList);
		fqjtList.addTypeArgument(table.getJavaClass());
		for (List<Column> cols : table.getIndexes())
			inter.addMethod(buildFindByMethod(fqjtList, cols, inter));
		
		//添加唯一约束对应索引，返回值为实例对象
		for (List<Column> cols : table.getUniques())
			inter.addMethod(buildFindByMethod(table.getJavaClass(), cols, inter));

		List<CompilationUnit> result = new ArrayList<CompilationUnit>();
		result.add(inter);
		return result;
	}
	
	private Method buildFindByMethod(FullyQualifiedJavaType returnType, List<Column> cols, CompilationUnit cu)
	{
		Method method = new Method();
		StringBuilder name = new StringBuilder();
		name.append("findBy");
		
		boolean first = true;
		for (Column col : cols)
		{
			if (!first)
				name.append("And");
			else
				first = false;
			name.append(StringUtils.capitalize(col.getPropertyName()));		
			
			cu.addImportedType(col.getJavaType());
			method.addParameter(new Parameter(col.getJavaType(), col.getPropertyName()));
		}

		method.setName(name.toString());
		method.setVisibility(JavaVisibility.PUBLIC);
		
		//返回值 
		method.setReturnType(returnType);
		return method;
	}
}
