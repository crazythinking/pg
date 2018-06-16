package net.engining.pg.maven.plugin.mojo;

import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import net.engining.pg.maven.plugin.meta.Domain;
import net.engining.pg.maven.plugin.utils.GeneralFileContent;
import net.engining.pg.support.meta.EnumInfo;
import net.engining.pg.support.meta.PropertyInfo;

/**
 * 
 * 组件扫描代码 <a
 * href="http://www.blogjava.net/stone2083/archive/2010/07/23/326941.html"
 * />出处</a>
 * 
 */
@Mojo(name = "genRuleObject", threadSafe = true)
public class RuleObjectMojo extends AbstractMojo {
	/**
	 * @parameter
	 * @required
	 */
	private String basePackage;
	/**
	 * @parameter default-value="target/yak-generated"
	 */
	private String outputDirectory;
	/**
	 * @parameter
	 * @required
	 */
	private String ruleObjectPackages[];

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			MetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory();
			List<CompilationUnit> units = new ArrayList<CompilationUnit>();
			List<GeneralFileContent> allFiles = new ArrayList<GeneralFileContent>();
			Map<Class<?>, Domain> domainMap = new HashMap<Class<?>, Domain>();
			ArrayList<Resource> resources = new ArrayList<Resource>();
			for (String pkg : ruleObjectPackages) {
				String path = "classpath*:" + StringUtils.replace(pkg, ".", "/") + "/**/*.class";
				resources.addAll(Arrays.asList(resolver.getResources(path)));
			}
			for (Resource res : resources) {
				MetadataReader reader = readerFactory.getMetadataReader(res);

				ClassMetadata meta = reader.getClassMetadata();

				if (!meta.isIndependent())
					continue;

				Class<?> clazz = Class.forName(meta.getClassName());

				if (clazz.isEnum())
					continue;

				// ruleObjectHelper
				if (clazz.getFields().length > 0) {
					TopLevelClass ruleHelper = new TopLevelClass(new FullyQualifiedJavaType(basePackage + ".client.rule." + clazz.getSimpleName() + "RuleHelper"));
					ruleHelper.setVisibility(JavaVisibility.PUBLIC);

					FullyQualifiedJavaType fqjtDataSource = new FullyQualifiedJavaType("com.smartgwt.client.data.DataSource");
					FullyQualifiedJavaType fqjtDataSourceTextField = new FullyQualifiedJavaType("com.smartgwt.client.data.fields.DataSourceTextField");
					FullyQualifiedJavaType fqjtListGridRecord = new FullyQualifiedJavaType("com.smartgwt.client.widgets.grid.ListGridRecord");
					FullyQualifiedJavaType fqjtLinkedHashMap = new FullyQualifiedJavaType("java.util.LinkedHashMap");

					ruleHelper.addImportedType(fqjtDataSource);
					ruleHelper.addImportedType(fqjtDataSourceTextField);
					ruleHelper.addImportedType(fqjtListGridRecord);
					ruleHelper.addImportedType(fqjtLinkedHashMap);

					Method getDataSource = new Method();
					getDataSource.setStatic(true);
					getDataSource.setName("getDataSource");
					getDataSource.setVisibility(JavaVisibility.PUBLIC);
					getDataSource.setReturnType(fqjtDataSource);
					getDataSource.addBodyLine(MessageFormat.format("ListGridRecord fieldData[] = new ListGridRecord[{0}];", clazz.getFields().length));
					getDataSource.addBodyLine("DataSource dataSource = new DataSource();");
					getDataSource.addBodyLine("dataSource.setClientOnly(true);");
					getDataSource.addBodyLine("DataSourceTextField nameField = new DataSourceTextField(\"name\");");
					getDataSource.addBodyLine("DataSourceTextField titleField = new DataSourceTextField(\"title\");");
					getDataSource.addBodyLine("DataSourceTextField typeField = new DataSourceTextField(\"type\");");
					getDataSource.addBodyLine("DataSourceTextField lengthField = new DataSourceTextField(\"length\");");
					getDataSource.addBodyLine("dataSource.setFields(nameField, titleField, typeField, lengthField);");
					for (int i = 0; i < clazz.getFields().length; i++) {
						Field field = clazz.getFields()[i];

						PropertyInfo pi = field.getAnnotation(PropertyInfo.class);
						if (pi == null)
							continue;

						getDataSource.addBodyLine(MessageFormat.format("ListGridRecord {0} = new ListGridRecord();", field.getName()));
						getDataSource.addBodyLine(MessageFormat.format("{0}.setAttribute(\"name\", \"{0}\");", field.getName()));
						getDataSource.addBodyLine(MessageFormat.format("{0}.setAttribute(\"title\", \"{1}\");", field.getName(), pi.name()));
						getDataSource.addBodyLine(MessageFormat.format("{0}.setAttribute(\"type\", \"{1}\");", field.getName(), pi.ruleType()));
						if (pi.length() != 0) {
							getDataSource.addBodyLine(MessageFormat.format("{0}.setAttribute(\"length\", \"{1}\");", field.getName(), pi.length()));
						}
						// 为枚举类型增加valueMap
						if (field.getType().isEnum()) {
							Domain domain = domainMap.get(field.getType());
							if (domain == null) {
								domain = createDomain(domainMap, field.getType());
							}
							getDataSource.addBodyLine(MessageFormat.format("LinkedHashMap<String, String> {0}Map = new LinkedHashMap<String, String>();", field.getName()));
							for (Entry<String, String> entry: domain.getValueMap().entrySet()) {
								getDataSource.addBodyLine(MessageFormat.format("{0}Map.put(\"{1}\", \"{2}\");", field.getName(), entry.getKey(), entry.getValue()));
							}
							getDataSource.addBodyLine(MessageFormat.format("{0}.setAttribute(\"valueMap\", {0}Map);", field.getName()));
						}
						getDataSource.addBodyLine(MessageFormat.format("fieldData[{0}] = {1};", i, field.getName()));
					}
					getDataSource.addBodyLine("dataSource.setCacheData(fieldData);");
					getDataSource.addBodyLine("return dataSource;");
					ruleHelper.addMethod(getDataSource);

					units.add(ruleHelper);
				}
			}

			// 先把内容生成出来，统一放到allFiles里
			for (CompilationUnit unit : units) {
				GeneratedJavaFile gjf = new GeneratedJavaFile(unit, outputDirectory);
				String filename = MessageFormat.format("{0}/{1}", StringUtils.replace(gjf.getTargetPackage(), ".", "/"), gjf.getFileName());
				allFiles.add(new GeneralFileContent(filename, gjf.getFormattedContent()));
			}

			// 再生成文件
			for (GeneralFileContent file : allFiles) {
				File targetFile = new File(FilenameUtils.concat(outputDirectory, file.getFilename()));
				FileUtils.writeStringToFile(targetFile, file.getContent(), file.getEncoding());
			}
		} catch (Exception e) {
			throw new MojoExecutionException("生成出错", e);
		}
	}
	
	private Domain createDomain(Map<Class<?>, Domain> domainMap, Class<?> javaType) {
		Domain domain = null;
		EnumInfo enumInfo = javaType.getAnnotation(EnumInfo.class);
		if (enumInfo == null) {
			getLog().warn(MessageFormat.format("枚举 {0} 没有指定EnumInfo", javaType.getCanonicalName()));
		} else {
			domain = new Domain();
			domainMap.put(javaType, domain);
			domain.setCode(javaType.getSimpleName());
			LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
			for (String value : enumInfo.value()) {
				String kv[] = value.split("\\|");
				if (kv.length != 2)
					throw new IllegalArgumentException("键值对语法错[" + javaType.getCanonicalName() + "]:" + value);
				String key = kv[0];
				key = StringUtils.replace(key, ".", "_");
				valueMap.put(key, kv[1]);
			}
			domain.setValueMap(valueMap);
			domain.setType(new FullyQualifiedJavaType(javaType.getCanonicalName()));
		}
		return domain;
	}
	
	public String[] getRuleObjectPackages() {
		return ruleObjectPackages;
	}

	public void setRuleObjectPackages(String[] ruleObjectPackages) {
		this.ruleObjectPackages = ruleObjectPackages;
	}

}
