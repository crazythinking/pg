package net.engining.pg.maven.plugin.mojo;

import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.engining.pg.maven.plugin.utils.GeneralFileContent;
import net.engining.pg.maven.plugin.utils.GeneratorUtils;
import net.engining.pg.support.cstruct.CChar;

/**
 * 用于生成接口
 * TODO 这个可以以后作为例子参考
 * 
 * @author chenjun.li
 *
 */
@Mojo(name = "genInterface", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class InterfaceMojo extends AbstractMojo {

	/**
	 * @parameter
	 * @required
	 */
	private DirectoryScanner interfaceFiles;

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
	 * <i>Maven Internal</i>: Project to interact with.
	 *
	 * @parameter property="project"
	 * @required
	 * @readonly
	 * @noinspection UnusedDeclaration
	 */
	private MavenProject project;

	private Log logger = getLog();

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		project.addCompileSourceRoot(outputDirectory);

		try {
			interfaceFiles.scan();

			List<TopLevelClass> generatedClasses = new ArrayList<TopLevelClass>();

			for (String file : interfaceFiles.getIncludedFiles()) {
				SAXReader sar = new SAXReader();
				Document docSource = sar.read(new File(interfaceFiles.getBasedir(), file));

				Element eLayout = (Element) docSource.selectSingleNode("//layout");

				String layoutName = eLayout.attributeValue("report");
				String layoutModule = eLayout.attributeValue("module");

				for (Element eRecord : (List<Element>) eLayout.selectNodes("record")) {
					String className = GeneratorUtils
							.dbName2ClassName(layoutName + "_" + eRecord.attributeValue("type"));
					TopLevelClass clazz = new TopLevelClass(
							new FullyQualifiedJavaType(basePackage + ".inter." + layoutModule + "." + className));

					clazz.setVisibility(JavaVisibility.PUBLIC);
					clazz.addImportedType(new FullyQualifiedJavaType(CChar.class.getCanonicalName()));

					for (Element eField : (List<Element>) eRecord.selectNodes("field")) {
						String fieldType = eField.attributeValue("type");
						int length = Integer.parseInt(eField.attributeValue("length"));
						Field field = new Field();
						field.setVisibility(JavaVisibility.PUBLIC);
						field.setName(GeneratorUtils.dbName2PropertyName(eField.attributeValue("name")));

						if ("C".equals(fieldType)) {
							field.addAnnotation(MessageFormat.format("@CChar({0,number,0})", length));
							if ("true".equalsIgnoreCase(eField.attributeValue("number"))) {
								if (length < 8)
									field.setType(FullyQualifiedJavaType.getIntInstance());
								else {
									FullyQualifiedJavaType fqjtBigDecimal = new FullyQualifiedJavaType(
											BigDecimal.class.getCanonicalName());
									clazz.addImportedType(fqjtBigDecimal);
									field.setType(fqjtBigDecimal);
								}
							} else
								field.setType(FullyQualifiedJavaType.getStringInstance());
						} else
							logger.warn(MessageFormat.format("无法处理的字段类型[{0}]", fieldType));

						clazz.addField(field);
					}
					generatedClasses.add(clazz);
				}
			}

			for (TopLevelClass clazz : generatedClasses) {
				GeneratedJavaFile gjf = new GeneratedJavaFile(clazz, outputDirectory);
				String filename = MessageFormat.format("{0}/{1}", StringUtils.replace(gjf.getTargetPackage(), ".", "/"),
						gjf.getFileName());

				FileUtils.writeStringToFile(new File(outputDirectory + "/" + filename), gjf.getFormattedContent(),
						GeneralFileContent.DEFAULT_ENCODING);
			}

		} catch (Exception e) {
			throw new MojoFailureException("执行出错", e);
		}
	}

	public DirectoryScanner getInterfaceFiles() {
		return interfaceFiles;
	}

	public void setInterfaceFiles(DirectoryScanner interfaceFiles) {
		this.interfaceFiles = interfaceFiles;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

}
