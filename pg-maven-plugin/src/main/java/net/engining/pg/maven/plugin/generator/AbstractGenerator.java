package net.engining.pg.maven.plugin.generator;

import java.util.List;

import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.maven.plugin.logging.Log;

import net.engining.pg.maven.plugin.interfaces.Generator;
import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Table;
import net.engining.pg.maven.plugin.utils.GeneralFileContent;

/**
 * 为实现类提供默认空实现
 * @author chenjun.li
 *
 */
public abstract class AbstractGenerator implements Generator
{
	protected Log logger;
	
	private String targetPackage;

	public List<CompilationUnit> generateAdditionalClasses(Table table, Database database)
	{
		return null;
	}

	public List<CompilationUnit> generateAdditionalClasses(Database database)
	{
		return null;
	}
	
	public void afterEntityGenerated(TopLevelClass entityClass, Table table)
	{
	}

	public void afterKeyGenerated(TopLevelClass keyClass)
	{
	}
	
	public List<GeneralFileContent> generateAdditionalFiles(Database database)
	{
		return null;
	}

	public List<GeneralFileContent> generateAdditionalFiles(Table table, Database database)
	{
		return null;
	}
	
	public void setLogger(Log logger) {
		this.logger = logger;
	}

	public String getTargetPackage() {
		return targetPackage;
	}

	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

}
