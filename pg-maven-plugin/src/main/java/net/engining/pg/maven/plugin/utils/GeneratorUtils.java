package net.engining.pg.maven.plugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.ImmutableSet;

import net.engining.pg.maven.plugin.meta.Column;

public class GeneratorUtils
{
	private static Set<String> javaKeywords = ImmutableSet.copyOf(
		new String[]{"abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while"}
	);
	
	public static boolean isJavaKeyword(String word)
	{
		return javaKeywords.contains(word);
	}
	
	public static String dbName2ClassName(String dbName)
	{
		String s = dbName;
		
		boolean allUpperCaseOrNumeric = true;
		for (char c : s.toCharArray())
		{
			if (c != '_' && !CharUtils.isAsciiNumeric(c) && !CharUtils.isAsciiAlphaUpper(c))
			{
				allUpperCaseOrNumeric = false;
				break;
			}
		}
		
		if (allUpperCaseOrNumeric)
		{
			//为应对Java类定义的情况，只有在全大写时才需要定义
			//TODO 这是临时方案
			s = s.toLowerCase();
			s = WordUtils.capitalizeFully(s, new char[]{ '_' });
			s = StringUtils.remove(s, "_");
		}
		
		if (!StringUtils.isAlpha(StringUtils.left(s, 1)))	//避免首个不是字母的情况
			s = "_" + s;
		return s;
	}

	public static String dbName2PropertyName(String dbName)
	{
		return WordUtils.uncapitalize(dbName2ClassName(dbName));
	}

	public static FullyQualifiedJavaType forType(TopLevelClass topLevelClass, String type)
	{
		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(type);
		topLevelClass.addImportedType(fqjt);
		return fqjt;
	}

	/**
	 * 生成类的属性，包括生成属性字段，及其getter、setter
	 * @param clazz		属性所在类
	 * @param fqjt		属性的Java类型
	 * @param property	属性名
	 * @param javadoc	属性的javadoc
	 * @param trimStrings	是否为setter方法赋值时自动trim，只对String的属性字段有效
	 * @return
	 */
	public static Field generateProperty(TopLevelClass clazz, FullyQualifiedJavaType fqjt, String property, List<String> javadoc, boolean trimStrings)
	{
		clazz.addImportedType(fqjt);

		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(fqjt);
		field.setName(property);
		
		clazz.addField(field);
		
		//getter
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(fqjt);
		method.setName(getGetterMethodName(field.getName(), field.getType()));
		StringBuilder sb = new StringBuilder();
		sb.append("return ");
		sb.append(property);
		sb.append(';');
		method.addBodyLine(sb.toString());

		createJavadoc(method, javadoc);

		clazz.addMethod(method);

		//setter
		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName(getSetterMethodName(property));
		method.addParameter(new Parameter(fqjt, property));
		createJavadoc(method, javadoc);

		if (trimStrings && fqjt.equals(FullyQualifiedJavaType.getStringInstance()))
		{
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(property);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(property);
			sb.append(" == null ? null : "); //$NON-NLS-1$
			sb.append(property);
			sb.append(".trim();"); //$NON-NLS-1$
			method.addBodyLine(sb.toString());
		}
		else
		{
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(property);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(property);
			sb.append(';');
			method.addBodyLine(sb.toString());
		}

		clazz.addMethod(method);

		return field;
	}

	private static void createJavadoc(Method method, List<String> javadoc) {
		if (javadoc != null)
		{
			method.addJavaDocLine("/**");
			for (String line : javadoc)
			{
				method.addJavaDocLine(" * <p>" + line + "</p>");
			}
			method.addJavaDocLine(" */");
		}
	}
	
	/**
	 * @param col
	 * @return
	 */
	public static List<String> generatePropertyJavadoc(Column col)
	{
		try
		{
			List<String> result = new ArrayList<String>();
			result.add(col.getTextName());
			
			String desc = col.getDescription();
			if (StringUtils.isNotBlank(desc))
			{
				BufferedReader br = new BufferedReader(new StringReader(desc));
				String line = br.readLine();
				while (line != null)
				{
					if (line.equals("///"))
						break;
					line = StringUtils.remove(line, "[[");
					line = StringUtils.remove(line, "]]");
					result.add(line);
					line = br.readLine();
				}
			}
		
			return result;
		} 
		catch (IOException e) {
			//不会出错
			throw new IllegalArgumentException(e);
		}
	}
	
	public static String getGetterMethodName(String property, FullyQualifiedJavaType fullyQualifiedJavaType)
	{
		String name = StringUtils.capitalize(property);

		if (fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getBooleanPrimitiveInstance()))
			name = "is" + name;
		else
			name = "get" + name;
		return name;
	}

    public static String getSetterMethodName(String property)
    {
		String name = StringUtils.capitalize(property);
		return "set" + name;
    }
    
    /**
     * @param outputStream 输出流
     * @param targetModule 生成模块名
     * @param inheritModules 需要继承的模块列表
     * @param units 准备生成的文件，用于决定source标签
     * @return
     * @throws IOException
     */
    public static void createGwtModule(OutputStream outputStream, String targetBasePackage, Collection<String> inheritModules, Collection<CompilationUnit> units) throws IOException
    {
		Set<String> sources = new HashSet<String>();
		for (CompilationUnit unit : units)
		{
			String packageName = unit.getType().getPackageName();
			if (!packageName.startsWith(targetBasePackage))
			{
				throw new RuntimeException("生成包[" + packageName + "]与模块前缀[" + targetBasePackage + "]不一致，无法生成GWT模块");
			}
			String source = StringUtils.remove(packageName, targetBasePackage).substring(1).replace('.', '/');
			sources.add(source);
		}
		Document doc = DocumentHelper.createDocument();
		Element root = DocumentHelper.createElement("module");
		doc.add(root);
//		doc.addDocType("module", "-//Google Inc.//DTD Google Web Toolkit 2.5.1//EN", "http://google-web-toolkit.googlecode.com/svn/tags/2.5.1/distro-source/core/src/gwt-module.dtd");
		
		for (String inherit : inheritModules)
			root.addElement("inherits").addAttribute("name", inherit);
		
		for (String source : sources)
			root.addElement("source").addAttribute("path", source);
		
		
		XMLWriter xw = new XMLWriter(outputStream, OutputFormat.createPrettyPrint());
		xw.write(doc);
		xw.close();
    }
    
}
