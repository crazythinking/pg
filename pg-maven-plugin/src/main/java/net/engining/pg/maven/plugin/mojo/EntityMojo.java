package net.engining.pg.maven.plugin.mojo;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.ibatis.ibator.api.dom.java.TopLevelEnumeration;
import org.apache.ibatis.ibator.internal.util.JavaBeansUtil;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.engining.pg.maven.plugin.generator.Entity2MapGenerator;
import net.engining.pg.maven.plugin.generator.EntityConstantGenerator;
import net.engining.pg.maven.plugin.generator.EqualsHashCodeGenerator;
import net.engining.pg.maven.plugin.generator.FillDefaultValuesGenerator;
import net.engining.pg.maven.plugin.generator.SequenceHomeGenerator;
import net.engining.pg.maven.plugin.generator.SpringDataRepositoryGenerator;
import net.engining.pg.maven.plugin.generator.ToStringGenerator;
import net.engining.pg.maven.plugin.interfaces.Generator;
import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Domain;
import net.engining.pg.maven.plugin.meta.JoinColumn;
import net.engining.pg.maven.plugin.meta.Relationship;
import net.engining.pg.maven.plugin.meta.Table;
import net.engining.pg.maven.plugin.utils.ERMImporter;
import net.engining.pg.maven.plugin.utils.GeneralFileContent;
import net.engining.pg.maven.plugin.utils.GeneratorUtils;
import net.engining.pg.maven.plugin.utils.PDMImporter;
import net.engining.pg.support.meta.EnumInfo;
import net.engining.pg.support.meta.PropertyInfo;

@Mojo(name = "entity", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
public class EntityMojo extends AbstractMojo {

	@org.apache.maven.plugins.annotations.Parameter(required = true)
	public String designs[];

	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project.build.directory}/pg-generated")
	public String outputDirectory;

	@org.apache.maven.plugins.annotations.Parameter(defaultValue = ".*")
	public String tablePattern;

	@org.apache.maven.plugins.annotations.Parameter(required = true)
	public String targetModule;

	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "0")
	public Integer maxSymbolLength;

	/**
	 * 使用新的基于SequenceStyleGenerator的id支持，同时要求Hibernate版本4.1以上。如果为false，则使用nativeGenerator，向下兼容。
	 */
	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "false")
	public boolean useEnhancedSequenceGenerator;

	/**
	 * 是否在Entity中加入常量 TABLE_NAME,以便于在客户端之类的环境下得到对应的表名
	 */
	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "false")
	public boolean tableNameConstant;

	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Component
	public BuildContext buildContext;

	private List<Generator> generators = new ArrayList<Generator>();

	public EntityMojo() {
		
		generators.add(new EqualsHashCodeGenerator());
		generators.add(new FillDefaultValuesGenerator());
		generators.add(new ToStringGenerator());
		generators.add(new Entity2MapGenerator());
		generators.add(new EntityConstantGenerator());
		generators.add(new SequenceHomeGenerator());
		generators.add(new SpringDataRepositoryGenerator());
//		generators.add(new AllFieldMappingsGenerator());
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		//TODO 该变量的赋值本要放到构造函数，作为属性定义，但不知为何无法赋值，很奇怪；暂时在执行阶段赋值；
		String targetBasePackage = StringUtils.left(targetModule, StringUtils.lastIndexOf(targetModule, "."));
		
		getLog().debug("插件变量：designs="+Arrays.toString(designs));
		getLog().debug("插件变量：outputDirectory="+outputDirectory);
		getLog().debug("插件变量：tablePattern="+tablePattern);
		getLog().debug("插件变量：targetModule="+targetModule);
		getLog().debug("插件变量：maxSymbolLength="+maxSymbolLength);
		getLog().debug("插件变量：useEnhancedSequenceGenerator="+useEnhancedSequenceGenerator);
		getLog().debug("插件变量：tableNameConstant="+tableNameConstant);
		getLog().debug("插件变量：project="+project);
		getLog().debug("插件变量：targetBasePackage="+targetBasePackage);
		getLog().debug("插件变量：generators size="+generators.size());
		

		if (buildContext.isIncremental()) {
			getLog().warn("增量编译");
		}
		try {
			project.addCompileSourceRoot(outputDirectory);

			// 解析数据库定义
			List<Database> databases = new ArrayList<Database>();
			ERMImporter ermImporter = new ERMImporter(getLog());
			PDMImporter pdmImporter = new PDMImporter(getLog());

			for (String design : designs) {
				File target = new File(project.getBasedir(), design);
				getLog().info("处理源文件:" + target.getCanonicalPath());
				Database db;
				if (target.getName().endsWith("pdm")) {
					db = pdmImporter.doImport(target, tablePattern);
				} else {
					db = ermImporter.doImport(target, tablePattern);
				}
				databases.add(db);
			}

			// 调各插件
			for (Generator generator : generators) {
				generator.setLogger(getLog());
				generator.setTargetPackage(targetBasePackage);
			}

			List<CompilationUnit> allUnits = new ArrayList<CompilationUnit>();
			Resource r = new Resource();
			r.setDirectory(outputDirectory);

			for (Database db : databases) {
				List<CompilationUnit> units = Lists.newArrayList();
				List<GeneralFileContent> files = Lists.newArrayList();

				// 待生成的Entities编译单元
				List<CompilationUnit> entities = generateEntity(db, targetBasePackage);
				units.addAll(entities);

				// 为了DomainGenerator和ClientHome的需要将generateAdditionalClasses放在前面，待改进
				// 生成附加的java文件和文本文件，这里可能要增加新的generator
				// 生成Database相关的附加文件; 比如Sequence;
				for (Generator gen : generators) {
					List<CompilationUnit> result = gen.generateAdditionalClasses(db);
					if (result != null)
						units.addAll(result);
					List<GeneralFileContent> f = gen.generateAdditionalFiles(db);
					if (f != null)
						files.addAll(f);
				}

				// 生成Table相关的附加文件
				for (Table table : db.getTables()) {
					for (Generator gen : generators) {
						List<CompilationUnit> result = gen.generateAdditionalClasses(table, db);
						if (result != null)
							units.addAll(result);
						List<GeneralFileContent> f = gen.generateAdditionalFiles(table, db);
						if (f != null)
							files.addAll(f);
					}
				}

				// 把需要编译的单元生成文件对象，也统一放到files里
				for (CompilationUnit unit : units) {
					GeneratedJavaFile gjf = new GeneratedJavaFile(unit, outputDirectory);
					String filename = MessageFormat.format("{0}/{1}",
							StringUtils.replace(gjf.getTargetPackage(), ".", "/"), gjf.getFileName());

					files.add(new GeneralFileContent(filename, gjf.getFormattedContent()));
				}

				// 生成文件，写入目录
				Set<String> filenames = Sets.newHashSet(); // 记录所有生成的文件名
				for (GeneralFileContent file : files) {
					File targetFile = new File(FilenameUtils.concat(outputDirectory, file.getFilename()));
					FileUtils.forceMkdir(targetFile.getParentFile());
					OutputStream os = buildContext.newFileOutputStream(targetFile);
					os.write(file.getContent().getBytes(file.getEncoding()));
					os.close();

					// 这里生成的所有文件都要加入Resource
					r.addInclude(file.getFilename());

					filenames.add(file.getFilename());
				}

				// 这段代码的目的，是存在多数据库设计文件时，如果有相同文件要生成，则先删除旧的文件，由新的文件替代；这种情况会造成后加载的数据库设计会覆盖之前加载的相同部分；
				String dbFilename = db.getSource().getName();
				Object value = buildContext.getValue(dbFilename);
				if (value != null) {
					@SuppressWarnings("unchecked")
					Set<String> olds = (Set<String>) value;
					olds.removeAll(filenames);
					for (String old : olds) {
						getLog().info("删除" + old);
						File fileOld = new File(new File(outputDirectory), old);
						FileUtils.deleteQuietly(fileOld);
						buildContext.refresh(fileOld);
					}
				}

				// 在buildContext中加入记录
				buildContext.setValue(db.getSource().getName(), filenames);

				allUnits.addAll(units);
			}

			// gwt.xml文件
			// String moduleFileName = StringUtils.replace(targetModule, ".",
			// "/") + ".gwt.xml";
			// File moduleFile = new File(new File(outputDirectory),
			// moduleFileName);
			// FileUtils.forceMkdir(moduleFile.getParentFile());
			// GeneratorUtils.createGwtModule(buildContext.newFileOutputStream(moduleFile),
			// targetBasePackage,
			// ImmutableList.of("net.engining.pg.gwt.GWT"), allUnits);
			// r.addInclude(moduleFileName);

			project.addResource(r);

		} catch (Exception e) {
			throw new MojoFailureException("生成过程出错", e);
		}
	}

	private TopLevelClass generateKeyClass(Table table) {
		// 复合主键
		TopLevelClass keyClass = new TopLevelClass(
				new FullyQualifiedJavaType(table.getJavaClass().getFullyQualifiedName() + "Key"));
		keyClass.setVisibility(JavaVisibility.PUBLIC);

		// 添加两个构造函数，默认和全属性的
		Method cm = new Method();
		cm.setConstructor(true);
		cm.setVisibility(JavaVisibility.PUBLIC);
		cm.setName(keyClass.getType().getShortName());
		cm.addBodyLine(""); // 不加空行生成的就会是abstract的
		keyClass.addMethod(cm);

		cm = new Method();
		cm.setConstructor(true);
		cm.setVisibility(JavaVisibility.PUBLIC);
		cm.setName(keyClass.getType().getShortName());
		for (Column pc : table.getPrimaryKeyColumns()) {
			cm.addParameter(new Parameter(pc.getJavaType(), pc.getPropertyName()));
			cm.addBodyLine(MessageFormat.format("this.{0} = {0};", pc.getPropertyName()));
		}
		keyClass.addMethod(cm);

		// 添加属性
		for (Column pc : table.getPrimaryKeyColumns()) {
			GeneratorUtils.generateProperty(keyClass, pc.getJavaType(), pc.getPropertyName(),
					GeneratorUtils.generatePropertyJavadoc(pc), false);
		}

		// 序列化
		keyClass.addSuperInterface(GeneratorUtils.forType(keyClass, Serializable.class.getCanonicalName()));
		keyClass.addAnnotation("@SuppressWarnings(\"serial\")");

		return keyClass;
	}

	/**
	 * 生成domain和entity
	 * 
	 * @return
	 */
	private List<CompilationUnit> generateEntity(Database db, String basePackage) {
		List<CompilationUnit> generatedFiles = new ArrayList<CompilationUnit>();

		Map<Table, TopLevelClass> generatingMap = new HashMap<Table, TopLevelClass>();

		// 先生成domain，并确定domain类型
		for (Domain domain : db.getDomains()) {
			if (domain.getType() != null) {
				// 如果已经写入自身类型，则表明是一个预定义枚举，不作处理
				continue;
			}

			// 对于非预定义枚举，需要生成枚举类
			TopLevelEnumeration clazz = new TopLevelEnumeration(new FullyQualifiedJavaType(
					basePackage + ".enums." + GeneratorUtils.dbName2ClassName(domain.getCode()) + "Def"));
			domain.setType(clazz.getType());
			clazz.setVisibility(JavaVisibility.PUBLIC);

			for (Entry<String, String> entry : domain.getValueMap().entrySet()) {
				String value = entry.getKey();
				if (!CharUtils.isAsciiAlpha(value.charAt(0))) {
					getLog().warn(MessageFormat.format("常量值不以字母打头，跳过：[{0}]-[{1}]", domain.getCode(), value));
					continue;
				}

				clazz.addEnumConstant(MessageFormat.format("/** {0} */\t{1}", entry.getValue(), entry.getKey()));
			}

			// EnumInfo注释
			clazz.addImportedType(new FullyQualifiedJavaType(EnumInfo.class.getCanonicalName()));
			StringBuilder anno = new StringBuilder();
			anno.append("@EnumInfo({\n");
			boolean first = true;
			for (Entry<String, String> entry : domain.getValueMap().entrySet()) {
				if (!first) {
					anno.append(",");
				}
				first = false;

				anno.append(MessageFormat.format("	\"{0}|{1}\"\n", entry.getKey(),
						StringEscapeUtils.escapeJava(entry.getValue())));
			}

			anno.append("})");

			clazz.addAnnotation(anno.toString());

			generatedFiles.add(clazz);
		}

		// 再生成entity
		for (Table table : db.getTables()) {
			// 计算表名及列名
			table.setJavaClass(new FullyQualifiedJavaType(
					basePackage + ".model." + JavaBeansUtil.getCamelCaseString(table.getDbName(), true)));

			for (Column col : table.getColumns())
				col.setPropertyName(JavaBeansUtil.getCamelCaseString(col.getDbName(), false));

			// 建立Entity类
			TopLevelClass entityClass = new TopLevelClass(table.getJavaClass());
			entityClass.setVisibility(JavaVisibility.PUBLIC);
			if (StringUtils.isNotBlank(table.getTextName())) {
				entityClass.addJavaDocLine("/**");
				entityClass.addJavaDocLine(" * " + table.getTextName());
				entityClass.addJavaDocLine(" * @author pg-maven-plugin");
				entityClass.addJavaDocLine(" */");
			}

			if (tableNameConstant) {
				// 为了在代码中引用表名，加上一个静态变量
				// public static final String TABLE_NAME = "xxxx"
				Field tableNameField = new Field();
				tableNameField.setVisibility(JavaVisibility.PUBLIC);
				tableNameField.setStatic(true);
				tableNameField.setFinal(true);
				tableNameField.setType(FullyQualifiedJavaType.getStringInstance());
				tableNameField.setName("TABLE_NAME");
				tableNameField.setInitializationString('"' + table.getDbName() + '"');
				entityClass.addField(tableNameField);
			}

			// 序列化
			// 加入serialVersionUID属性
			entityClass.addSuperInterface(GeneratorUtils.forType(entityClass, Serializable.class.getCanonicalName()));
			Field serialVersionUIField = new Field();
			serialVersionUIField.setVisibility(JavaVisibility.PRIVATE);
			serialVersionUIField.setStatic(true);
			serialVersionUIField.setFinal(true);
			serialVersionUIField.setType(new FullyQualifiedJavaType("long"));
			serialVersionUIField.setName("serialVersionUID");
			serialVersionUIField.setInitializationString("1L");
			entityClass.addField(serialVersionUIField);
			// entityClass.addAnnotation("@SuppressWarnings(\"serial\")");

			GeneratorUtils.forType(entityClass, "javax.persistence.Entity");
			entityClass.addAnnotation("@Entity");
			GeneratorUtils.forType(entityClass, "javax.persistence.Table");
			String tableName = table.getDbName();
			if (maxSymbolLength > 0 && tableName.length() > maxSymbolLength) {
				getLog().warn(String.format("表名[%s]超过设定的最大长度[%d]，将被截取", table.getDbName(), maxSymbolLength));
				tableName = StringUtils.left(tableName, maxSymbolLength);
			}
			entityClass.addAnnotation("@Table(name=\"" + tableName + "\")");
			GeneratorUtils.forType(entityClass, "org.hibernate.annotations.DynamicInsert");
			entityClass.addAnnotation("@DynamicInsert(true)");
			GeneratorUtils.forType(entityClass, "org.hibernate.annotations.DynamicUpdate");
			entityClass.addAnnotation("@DynamicUpdate(true)");
			GeneratorUtils.forType(entityClass, "javax.persistence.EntityListeners");
			GeneratorUtils.forType(entityClass, "org.springframework.data.jpa.domain.support.AuditingEntityListener");
			entityClass.addAnnotation("@EntityListeners(AuditingEntityListener.class)");
			

			// 在生成entity前先修正枚举列类型
			for (Column col : table.getColumns()) {
				if (col.getDomain() != null)
					col.setJavaType(col.getDomain().getType());
			}

			List<Column> pks = table.getPrimaryKeyColumns();
			if (pks.size() > 1) {
				TopLevelClass keyClass = generateKeyClass(table);
				entityClass.addImportedType(keyClass.getType());
				entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.IdClass"));
				entityClass
						.addAnnotation(MessageFormat.format("@IdClass({0}.class)", keyClass.getType().getShortName()));
				table.setJavaKeyClass(keyClass.getType());

				for (Generator generator : generators)
					generator.afterKeyGenerated(keyClass);

				generatedFiles.add(keyClass);
			} else if (pks.size() == 1) {
				table.setJavaKeyClass(table.getPrimaryKeyColumns().get(0).getJavaType());
			} else {
				throw new IllegalArgumentException("主键数量不对：" + table.getDbName());
			}

			// 开始生成Entity的属性
			// import, net.engining.pg.support.meta.PropertyInfo
			entityClass.addImportedType(new FullyQualifiedJavaType(PropertyInfo.class.getCanonicalName()));
			for (Column col : table.getColumns()) {
				// 建立Entity类里的属性
				checkArgument(col.getJavaType() != null, "属性[%s]没有分配类型，数据库[%s.%s]", col.getPropertyName(),
						table.getDbName(), col.getDbName());

				if (GeneratorUtils.isJavaKeyword(col.getPropertyName())) {
					getLog().warn(String.format("[%s.%s]是java关键字，跳过。", table.getDbName(), col.getDbName()));
					continue;
				}
				//处理Entity的属性字段
				Field f = GeneratorUtils.generateProperty(entityClass, col.getJavaType(), col.getPropertyName(),
						GeneratorUtils.generatePropertyJavadoc(col), false);

				// 放上PropertyInfo
				String piAnnotation = MessageFormat.format("@PropertyInfo(name=\"{0}\"",
						StringEscapeUtils.escapeJava(col.getTextName()));
				if (col.getJavaType().equals(FullyQualifiedJavaType.getStringInstance())) {
					piAnnotation += ", length=" + col.getLength();
				}
				piAnnotation += ")";
				f.addAnnotation(piAnnotation);

				if (table.getPrimaryKeyColumns().contains(col)) {
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Id"));
					f.addAnnotation("@Id");

					// 指定identity，如果有的话
					if (col.isIdentity()) {
						entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.GeneratedValue"));
						entityClass.addImportedType(
								new FullyQualifiedJavaType("org.hibernate.annotations.GenericGenerator"));
						String genName = "GEN_" + table.getDbName();
						if (maxSymbolLength > 0 && genName.length() > maxSymbolLength) {
							getLog().warn(String.format("Sequence名[%s]超过设定的最大长度[%d]，将被截取", genName, maxSymbolLength));
							genName = StringUtils.left(genName, maxSymbolLength);
						}
						f.addAnnotation("@GeneratedValue(generator=\"" + genName + "\")");

						if (!useEnhancedSequenceGenerator) {
							f.addAnnotation("@GenericGenerator(name=\"" + genName + "\", strategy=\"native\")");
						} else {
							// 使用新的SequenceStyleGenerator
							entityClass
									.addImportedType(new FullyQualifiedJavaType("org.hibernate.annotations.Parameter"));
							f.addAnnotation("@GenericGenerator(name=\"" + genName
									+ "\", strategy=\"org.hibernate.id.enhanced.SequenceStyleGenerator\",\n"
									+ "parameters = {@Parameter( name = \"prefer_sequence_per_entity\", value = \"true\")})");
						}
					} else if (StringUtils.isNotBlank(col.getIdGenStrategy())) {
						String strategy = col.getIdGenStrategy();
						getLog().warn(
								String.format("[%s]表的主键[%s]生成策略：%s", table.getDbName(), col.getDbName(), strategy));

						entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.GeneratedValue"));
						entityClass.addImportedType(
								new FullyQualifiedJavaType("org.hibernate.annotations.GenericGenerator"));
						String genName = "GEN_" + table.getDbName();
						if (maxSymbolLength > 0 && genName.length() > maxSymbolLength) {
							getLog().warn(String.format("Sequence名[%s]超过设定的最大长度[%d]，将被截取", genName, maxSymbolLength));
							genName = StringUtils.left(genName, maxSymbolLength);
						}
						f.addAnnotation("@GeneratedValue(generator=\"" + genName + "\")");
						
						getLog().debug("strategy of id genterator:"+strategy);
						switch (strategy) {
						case "uuid2":
							f.addAnnotation("@GenericGenerator(name=\"" + genName + "\", strategy=\"uuid2\")");
							break;
						case "uuid.hex":
							f.addAnnotation("@GenericGenerator(name=\"" + genName + "\", strategy=\"uuid.hex\")");
							break;
						case "net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator":
							f.addAnnotation("@GenericGenerator(name=\"" + genName + "\", strategy=\"net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator\")");
							break;
						default:
							break;
						}
					}
				}

				if (col.getDomain() != null) {
					f.addAnnotation("@Enumerated(EnumType.STRING)");
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Enumerated"));
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.EnumType"));
				}
				if (col.getJavaType().getShortName().equals("Date")) {
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Temporal"));
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.TemporalType"));
					f.addAnnotation("@Temporal(value=TemporalType." + col.getTemporal() + ")");
				}
				if (col.isLob()) {
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Lob"));
					f.addAnnotation("@Lob");
				}
				if (col.isLazy()) {
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Basic"));
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.FetchType"));
					f.addAnnotation("@Basic(fetch=FetchType.LAZY)");
				}
				entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Column"));

				String annoColumn = "@Column(";
				annoColumn += "name=\"" + col.getDbName() + "\"";
				annoColumn += ", nullable=" + !col.isMandatory();
				// 布尔类型指定数据库类型
				// if (col.getJavaType().getShortName().equals("Boolean")) {
				// annoColumn += ", columnDefinition=\"CHAR(1)\"";
				// }
				// 数字的长度
				if (col.getJavaType().getShortName().equals("BigDecimal")) {
					annoColumn += ", precision=" + col.getLength();
					annoColumn += ", scale=" + col.getScale();
				}
				// 字符串长度
				if (col.getDomain() != null || col.getJavaType().equals(FullyQualifiedJavaType.getStringInstance())) {
					annoColumn += ", length=" + col.getLength();
				}
				annoColumn += ")";
				f.addAnnotation(annoColumn);
				
				if(StringUtils.isNoneBlank(col.getAuditedType())){
					if("@CreatedBy".equals(col.getAuditedType())){
						entityClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.annotation.CreatedBy"));
						f.addAnnotation("@CreatedBy");
					}
					else if("@CreatedDate".equals(col.getAuditedType())){
						entityClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.annotation.CreatedDate"));
						f.addAnnotation("@CreatedDate");
					}
					else if("@LastModifiedBy".equals(col.getAuditedType())){
						entityClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.annotation.LastModifiedBy"));
						f.addAnnotation("@LastModifiedBy");
					}
					else if("@LastModifiedDate".equals(col.getAuditedType())){
						entityClass.addImportedType(new FullyQualifiedJavaType("org.springframework.data.annotation.LastModifiedDate"));
						f.addAnnotation("@LastModifiedDate");
					}
				}
				
				//del_flag
				if(col.isTransient()){
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Transient"));
					f.addAnnotation("@Transient");
				}

				// Version
				if (col.isVersion()) {
					entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Version"));
					f.addAnnotation("@Version");
					//配置json忽略
					entityClass.addImportedType(new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonIgnore"));
					f.addAnnotation("@JsonIgnore");
				}
			}

			for (Generator generator : generators) {
				generator.afterEntityGenerated(entityClass, table);
			}
			generatedFiles.add(entityClass);
			generatingMap.put(table, entityClass);
		}

		// 建立ManyToOne
		// @ManyToOne
		// @JoinColumn(name = "cdhd_usr_id", referencedColumnName =
		// "cdhd_usr_id")
		// 或
		// @JoinColumns({
		// @JoinColumn(name="a", referencedColumnName = "d"),
		// @JoinColumn(name="e", referencedColumnName = "f")
		// })
		// private TblUmsvcCdhdBasInf tblUmsvcCdhdBasInf;
		for (Relationship rel : db.getRelationships()) {
			TopLevelClass parentClass = generatingMap.get(rel.getParent());
			TopLevelClass childClass = generatingMap.get(rel.getChild());
			Field f;

			// ManyToOne
			f = GeneratorUtils.generateProperty(childClass, parentClass.getType(),
					WordUtils.uncapitalize(parentClass.getType().getShortName()), null, false);
			if (rel.isOne2One()) {
				childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.OneToOne"));
				f.addAnnotation("@OneToOne");
			} else {
				childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.ManyToOne"));
				f.addAnnotation("@ManyToOne");
			}
			childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.JoinColumn"));
			if (rel.getJoinColumns().size() > 1) {
				childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.JoinColumns"));
				f.addAnnotation("@JoinColumns({");

				Iterator<JoinColumn> iter = rel.getJoinColumns().iterator();
				while (iter.hasNext()) {
					JoinColumn jc = iter.next();
					f.addAnnotation(MessageFormat.format(
							"	@JoinColumn(name=\"{0}\", referencedColumnName = \"{1}\", updatable=false, insertable=false){2}",
							jc.getFk().getDbName(), jc.getPk().getDbName(), iter.hasNext() ? "," : ""));
				}
				f.addAnnotation("})");
			} else {
				JoinColumn jc = rel.getJoinColumns().get(0);
				f.addAnnotation(MessageFormat.format(
						"@JoinColumn(name=\"{0}\", referencedColumnName = \"{1}\", updatable=false, insertable=false)",
						jc.getFk().getDbName(), jc.getPk().getDbName()));
			}

			// 建立OneToMany
			// @OneToMany(mappedBy = "xxx", cascade = CascadeType.ALL)
			// private List<> xxx = new ArrayList<>();
			// 添加属性

			if (rel.isOne2One()) {
				parentClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.OneToOne"));
				f = GeneratorUtils.generateProperty(parentClass, childClass.getType(),
						WordUtils.uncapitalize(childClass.getType().getShortName()), null, false);
				f.addAnnotation(MessageFormat.format(
						"@OneToOne(mappedBy = \"{0}\", optional = true, cascade = CascadeType.ALL)",
						WordUtils.uncapitalize(parentClass.getType().getShortName())));
			} else {
				parentClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.OneToMany"));
				FullyQualifiedJavaType fqjtSet = GeneratorUtils.forType(parentClass, "java.util.Set");
				fqjtSet.addTypeArgument(childClass.getType());
				f = GeneratorUtils.generateProperty(parentClass, fqjtSet,
						WordUtils.uncapitalize(childClass.getType().getShortName() + "s"), null, false);
				// f.setInitializationString("new ArrayList<" +
				// childClass.getType().getShortName() + ">()");
				parentClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.CascadeType"));
				f.addAnnotation(MessageFormat.format("@OneToMany(mappedBy = \"{0}\", cascade = CascadeType.ALL)",
						WordUtils.uncapitalize(parentClass.getType().getShortName())));
			}
		}
		return generatedFiles;
	}

}
