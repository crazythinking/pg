package net.engining.pg.maven.plugin.interfaces;

import java.util.List;

import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.maven.plugin.logging.Log;

import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Table;
import net.engining.pg.maven.plugin.utils.GeneralFileContent;

public interface Generator
{
	/**
	 * 生成额外的类
	 * @param table
	 * @param database
	 * @return key为文件名，value为文件内容
	 */
	List<CompilationUnit> generateAdditionalClasses(Table table, Database database);
	
	/**
	 * 生成额外的类
	 * @param database
	 * @return key为文件名，value为文件内容
	 */
	List<CompilationUnit> generateAdditionalClasses(Database database);

	/**
	 * 生成额外的文件
	 * @param database
	 * @return key为文件名，value为文件内容
	 */
	List<GeneralFileContent> generateAdditionalFiles(Database database);

	/**
	 * 生成额外的文件
	 * @param table
	 * @param database
	 * @return key为文件名，value为文件内容
	 */
	List<GeneralFileContent> generateAdditionalFiles(Table table, Database database);
	
	void afterEntityGenerated(TopLevelClass entityClass, Table table);

	void afterKeyGenerated(TopLevelClass keyClass);
	
	void setLogger(Log logger);
	
	void setTargetPackage(String targetPackage);
}
