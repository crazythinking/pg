package net.engining.pg.maven.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.google.common.collect.Lists;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Domain;
import net.engining.pg.maven.plugin.meta.Table;
import net.engining.pg.support.db.DbType;

/**
 * 导入ERMaster的定义文件
 * @author chenjun.li
 *
 */
public class ERMImporter {
	
	private Log logger;
	
	public ERMImporter(Log logger)
	{
		this.logger = logger;
	}
	
	private FullyQualifiedJavaType fqjtInteger = new FullyQualifiedJavaType("java.lang.Integer");
	
	private FullyQualifiedJavaType fqjtBigDecimal = new FullyQualifiedJavaType("java.math.BigDecimal");
	
	private FullyQualifiedJavaType fqjtDate = new FullyQualifiedJavaType("java.util.Date");
	
	private FullyQualifiedJavaType fqjtBlob = new FullyQualifiedJavaType("java.sql.Blob");
	
	/**
	 * 表达式模式：[[xxxxx]]
	 */
	private static final Pattern hintPattern = Pattern.compile("\\[\\[.*\\]\\]");
	
	/**
	 * 通过description表示指定的java类型，表达式模式，例如：!!!java.lang.Boolean!!!
	 */
	private static final Pattern javaTypePattern = Pattern.compile("!!!.*!!!");
	
	/**
	 * 通过description表示指定的ID生成器类型，表达式模式。<br>
	 * 例如：###xxx.xxx.xxIdGenerator### 或者直接指定Hibernate支持的strategy，综合考虑目前只需要如下几个:<br>
	 * "uuid2","uuid.hex","net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator"
	 */
	private static final Pattern idGeneratorPattern = Pattern.compile("###.*###");
	
	/**
	 * 正则表达式$匹配输入字符串的结尾位置；要匹配 $ 字符本身，请使用 \$
	 * 通过description表示指定字段的审计支持，可以包含：@CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate
	 */
	private static final Pattern columnAuditedPattern = Pattern.compile("\\$\\$\\$.*\\$\\$\\$");

	@SuppressWarnings("unchecked")
	public Database doImport(File ermSource, String tablePattern) throws Exception
	{
		Database result = new Database();
		result.setSource(ermSource);

		// 解析源文件
		SAXReader sar = new SAXReader();
		Document docSource = sar.read(ermSource);
		
		// 取数据库类型
		for (Node nodeSettingNode : docSource.selectNodes("/diagram/settings")){
			Element nodeSetting = (Element) nodeSettingNode;
			String dbType = nodeSetting.elementText("database");
			if(DbType.DB2.toString().equals(dbType)){
				result.setDbType(DbType.DB2);
			}
			else{
				
			}
		}
		
		//先取所有的word，组建Column
		Map<String, Element> words = new HashMap<String, Element>();
		Map<String, Domain> domains = new HashMap<String, Domain>();
		for (Node nodeWordNode : docSource.selectNodes("/diagram/dictionary/word"))
		{
			Element nodeWord = (Element) nodeWordNode;
			String id = nodeWord.elementText("id");
			words.put(id, nodeWord);
			
			//Domain,只用于处理description内有内容的字段，通常是枚举类型的字段
			Domain domain = parseDomain(nodeWord.elementText("physical_name"), nodeWord.elementText("description"));
			if (domain != null)
			{
				domains.put(id, domain);
				result.getDomains().add(domain);
			}
		}

		//取出table
		Map<String, Element> tables = new HashMap<String, Element>();
		for (Node nodeTableNode : docSource.selectNodes("/diagram/contents/table")){
			Element nodeTable = (Element) nodeTableNode;
			tables.put(nodeTable.elementText("id"), nodeTable);
		}
			
		//开始组建Database对象
		Map<String, Column> allColumns = new HashMap<String, Column>();	//全局column映射，以id为key
		for (Element nodeTable : tables.values())
		{
			Table table = new Table();
			table.setDbName(nodeTable.elementText("physical_name"));
			table.setTextName(nodeTable.elementText("logical_name"));
			logger.debug(table.getDbName());
			
			Set<String> columnNames = new HashSet<String>();	//防重复
			
			//处理字段
			for (Node nodeColumnNode : nodeTable.selectNodes("columns/*"))
			{
				Element nodeColumn = (Element) nodeColumnNode;
				Column column = new Column();
				logger.debug(nodeColumn.getName());
				
				String word_id = nodeColumn.elementText("word_id");
				if (word_id == null)
				{
					//没找到的话就找referenced_column
					Element node = nodeColumn;
					
					do
					{
						String refId = node.elementText("referenced_column");
						if (refId == null)
							throw new IllegalArgumentException("非法的表字段定义，没有找到referenced_column");
						node = (Element)docSource.selectSingleNode("//table/columns/*[id='" + refId + "']");
						word_id = node.elementText("word_id");
					}while (StringUtils.isEmpty(word_id));
				}
					
				Element nodeWord = words.get(word_id);

				//以本node的物理名优先
				String physicalName = nodeColumn.elementText("physical_name");
				if (StringUtils.isBlank(physicalName))
					physicalName = nodeWord.elementText("physical_name");
				physicalName = StringUtils.remove(physicalName, '\n');
				physicalName = StringUtils.remove(physicalName, '\r');
				column.setDbName(physicalName);
				column.setIdentity(Boolean.valueOf(nodeColumn.elementText("auto_increment")));
				
				//逻辑名也一样处理
				String logicalName = nodeColumn.elementText("logical_name");
				if (StringUtils.isBlank(logicalName))
					logicalName = nodeWord.elementText("logical_name");

				column.setTextName(logicalName);
				column.setDescription(nodeWord.elementText("description"));
				column.setId(nodeColumn.elementText("id"));
				//Id的生成策略
				column.setIdGenStrategy(extractIdGenerator(column.getDescription()));
				column.setMandatory(Boolean.parseBoolean(nodeColumn.elementText("not_null")));
				
				//从description解析hint
				column.setHint(extractHint(column.getDescription()));
				
				//从description解析字段审计类型
				column.setAuditedType(extractAduited(column.getDescription()));

				//解析类型
				String type = nodeWord.elementText("type");
				String length = nodeWord.elementText("length");
				String decimal = nodeWord.elementText("decimal");
				String javaType = extractJavaType(column.getDescription());
				if (javaType != null) {
					
					column.setJavaType(new FullyQualifiedJavaType(javaType));
					
					//特殊处理blob类型，因为Hibernate会将java.sql.Blob,java.lang.Byte[], byte[] 和 Serializable类型都持久化为Blob
					if (StringUtils.endsWithAny(type, "blob","blob(n)"))//mysql 里的各种blob
					{
//						column.setJavaType(new FullyQualifiedJavaType(javaType));
						column.setLob(true);
//						column.setLazy(true);
					}
					
					if(StringUtils.isNoneBlank(length) && !"null".equals(length))
						column.setLength(Integer.parseInt(length));
					
				}
				else if ("char".equals(type))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLength(1);
				}
				else if ("character(n)".equals(type) || "varchar(n)".equals(type))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLength(Integer.parseInt(length));
				}
				else if ("decimal".equals(type))
				{
					logger.warn(MessageFormat.format("decimal没有指定长度，按长度为1处理。[{0}], {1}, {2}", type, column.getDbName(), table.getDbName()));
					column.setJavaType(fqjtInteger);
					column.setLength(1);
				}
				else if ("decimal(p)".equals(type) || "numeric(p)".equals(type) || "float(p)".equals(type))
				{
					int l = Integer.parseInt(length);
					if (l <= 9 )
						column.setJavaType(fqjtInteger);
					else
						column.setJavaType(fqjtBigDecimal);
					column.setLength(l);
				}
				else if ("decimal(p,s)".equals(type) || "numeric(p,s)".equals(type) || "float(m,d)".equals(type) || "double(m,d)".equals(type))
				{
					int l = Integer.parseInt(length);
					int s = Integer.parseInt(decimal);
					if (s == 0 && l <= 9 )
						column.setJavaType(fqjtInteger);
					else
						column.setJavaType(fqjtBigDecimal);
					column.setLength(l);
					column.setScale(s);
				}
				else if ("integer".equals(type) || "int".equals(type) || "tinyint".equals(type) || "smallint".equals(type) || "mediumint".equals(type))
				{
					column.setJavaType(fqjtInteger);
					column.setLength(9);
				}
				else if ("bigint".equals(type) || "bigint(n)".equals(type))
				{
					column.setJavaType(new FullyQualifiedJavaType("java.lang.Long"));
					column.setLength(18);
				}
				else if ("date".equals(type))
				{
					column.setJavaType(fqjtDate);
					column.setTemporal("DATE");
				}
				else if ("time".equals(type))
				{
					column.setJavaType(fqjtDate);
					column.setTemporal("TIME");
				}
				else if ("timestamp".equals(type)||"datetime".equals(type))
				{
					column.setJavaType(fqjtDate);
					column.setTemporal("TIMESTAMP");
				}
				else if ("clob".equals(type) || "tinytext".equals(type) || "text".equals(type) || "mediumtext".equals(type) || "longtext".equals(type))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLob(true);
				}
				else if ("clob(n)".equals(type))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLob(true);
					column.setLength(Integer.parseInt(length));
				}
				else if (StringUtils.endsWithAny(type, "blob","blob(n)"))//mysql 里的各种blob
				{
					column.setJavaType(fqjtBlob);
					column.setLob(true);
					column.setLazy(true);
					if (StringUtils.isNotBlank(length) && !"null".equals(length))
					{
						column.setLength(Integer.parseInt(length));
					}
				}
				else if ("boolean".equals(type) || "bit".equals(type))
				{
					column.setJavaType(new FullyQualifiedJavaType(Boolean.class.getCanonicalName()));
				}
				else
				{
					logger.warn(MessageFormat.format("无法识别的类型[{0}]，跳过, {1}, {2}", type, column.getDbName(), table.getDbName()));
					continue;
				}
				
				
				if (type.startsWith("numeric"))
					logger.warn(MessageFormat.format("建议不要使用numeric，用decimal代替[{0}], {1}, {2}", type, column.getDbName(), table.getDbName()));
				if (type.startsWith("datetime"))
					logger.warn(MessageFormat.format("建议不要使用datetime，用timestamp代替[{0}], {1}, {2}", type, column.getDbName(), table.getDbName()));
				
				
				//JPA忽略该字段
				column.setTransient("DEL_FLAG".equalsIgnoreCase(column.getDbName()));
				
				//处理Version
				column.setVersion(
					"JPA_VERSION".equalsIgnoreCase(column.getDbName()) ||
					"JPA_TIMESTAMP".equalsIgnoreCase(column.getDbName()) ||
					"OL_VERSION".equalsIgnoreCase(column.getDbName())
					);
				
				//有unique_key按单列约束处理
				if ("true".equals(nodeColumn.elementText("unique_key")))
				{
					List<Column> unique = new ArrayList<Column>();
					unique.add(column);
					table.getUniques().add(unique);
				}
				
				if (columnNames.contains(column.getDbName()))	//字段重复
				{
					logger.warn(MessageFormat.format("字段重复，跳过 {0}, {1}", column.getDbName(), table.getDbName()));
					continue;
				}
				columnNames.add(column.getDbName());
				
				allColumns.put(column.getId(), column);
				table.getColumns().add(column);
				
				
				if (Boolean.parseBoolean(nodeColumn.elementText("primary_key")))
				{
					table.getPrimaryKeyColumns().add(column);
				}
				
				//domain
				if (domains.containsKey(word_id))
				{
					//如果有domain，设置之
					column.setDomain(domains.get(word_id));
					//临时补丁：在这里设置类型，需要重构得更优雅一点
//					if (column.getDomain().getJavaType() == null)
//						column.getDomain().setJavaType(column.getdo.getJavaType());
				}
			}
			if (table.getPrimaryKeyColumns().isEmpty())	//没有主键就跳过
			{
				logger.warn(table.getDbName() + " 没有主键，跳过");
				continue;
			}
			
			//处理索引
			for (Node nodeIndexNode : nodeTable.selectNodes("indexes/*"))	//源文件有拼写错误，所以这里用*，希望以后版本会改掉(1.0.0)
			{
				Element nodeIndex = (Element) nodeIndexNode;
				List<Column> index = new ArrayList<Column>();
				for (Node nodeColumnNode : nodeIndex.selectNodes("columns/column"))
				{
					Element nodeColumn = (Element) nodeColumnNode;
					index.add(allColumns.get(nodeColumn.elementText("id")));
				}
				table.getIndexes().add(index);
			}
			//唯一约束也按索引处理
			for (Node nodeIndexNode : nodeTable.selectNodes("complex_unique_key_list/complex_unique_key"))
			{
				Element nodeIndex = (Element) nodeIndexNode;
				List<Column> unique = new ArrayList<Column>();
				for (Node nodeColumnNode : nodeIndex.selectNodes("columns/column"))
				{
					Element nodeColumn = (Element) nodeColumnNode;
					unique.add(allColumns.get(nodeColumn.elementText("id")));
				}
				table.getUniques().add(unique);
			}
			
			result.getTables().add(table);
		}
		
		//处理Sequence
		for (Node nodeNameNode : docSource.selectNodes("/diagram/sequence_set/sequence/name")){
			Element nodeName = (Element) nodeNameNode;
			result.getSequences().add(nodeName.getText());
		}
			
		
		return result;
	}
	
	private Domain parseDomain(String code, String desc)
	{
		try
		{
			Domain domain = null;
			
			BufferedReader br = new BufferedReader(new StringReader(desc));
			String line = br.readLine();
			boolean started = false;
			while (line != null)
			{
				//跳过空行
				if (StringUtils.isNotBlank(line))
				{
					if (started)
					{
						if (line.startsWith("@"))
						{
							//以@打头为引用现有枚举类
							String type = StringUtils.remove(line.trim(), "@");
							domain.setType(new FullyQualifiedJavaType(type));
							//TODO 感觉有问题，code应该是字段名称，不一定与定义的枚举类型的名称相同
							domain.setCode(domain.getType().getShortName());
							//在pg2.0后，erm不直接生成U对象，所以这里不用解析外部枚举
							break;
						}
						
						String kv[] = line.split("\\|");
						if (kv.length != 2)
							throw new IllegalArgumentException("键值对语法错[" + code + "]:" + line);
						String key = kv[0];
						key = StringUtils.replace(key, ".", "_");
						domain.getValueMap().put(key, kv[1]);
					}
					else if ("///".equals(StringUtils.trim(line)))
					{
						started = true;
						domain = new Domain();
						domain.setCode(code);
						domain.setValueMap(new LinkedHashMap<String, String>());
					}
				}
				line = br.readLine();
			}
			return domain;
		}
		catch (Exception t)
		{
			throw new IllegalArgumentException(t);
		}
	}
	
	private String extractIdGenerator(String desc) throws Exception
	{
		Matcher m = idGeneratorPattern.matcher(desc);
		if (!m.find())
			return null;
		
		String ret = desc.substring(m.start() + 3, m.end() - 3);
		List<String> idgens= Lists.newArrayList("uuid2","uuid.hex","net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator");
		if(!idgens.contains(ret))
			throw new Exception("目前不支持该类型的ID生成器，只支持\"uuid2\",\"uuid.hex\",\"net.engining.pg.support.db.id.generator.SnowflakeSequenceIdGenerator\"");
		
		return desc.substring(m.start() + 3, m.end() - 3);
		
	}
	
	private String extractHint(String desc)
	{
		Matcher m = hintPattern.matcher(desc);
		if (!m.find())
			return null;
		
		return desc.substring(m.start() + 2, m.end() - 2);
		
	}
	
	private String extractJavaType(String desc){
		Matcher m = javaTypePattern.matcher(desc);
		if (!m.find())
			return null;
		
		return desc.substring(m.start() + 3, m.end() - 3);
	}
	
	private String extractAduited(String desc) throws Exception{
		Matcher m = columnAuditedPattern.matcher(desc);
		if (!m.find()){
//			logger.debug("########################################################## return null");
			return null;
		}
		else{
//			logger.debug("##########################################################"+m.toString());
			String ret = desc.substring(m.start() + 3, m.end() - 3);
			logger.debug("##########################################################"+ret);
			List<String> aduiteds= Lists.newArrayList("@CreatedBy", "@CreatedDate", "@LastModifiedBy", "@LastModifiedDate");
			if(!aduiteds.contains(ret))
				throw new Exception("指定的审计类型不正确，只支持\"@CreatedBy\", \"@CreatedDate\", \"@LastModifiedBy\", \"@LastModifiedDate\"");
		}
		
		return desc.substring(m.start() + 3, m.end() - 3);
	}
}
