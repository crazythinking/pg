package net.engining.pg.maven.plugin.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import net.engining.pg.maven.plugin.meta.Column;
import net.engining.pg.maven.plugin.meta.Database;
import net.engining.pg.maven.plugin.meta.Table;

public class PDM2ERM {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws DocumentException, FileNotFoundException, IOException {
		String input = "apm.erm";
		
		String pdm_source = "database_new.pdm";
		
		Database pdm = new PDMImporter(new SystemStreamLog()).doImport(new File(pdm_source), ".*");
		
		SAXReader sar = new SAXReader();
		Document docSource = sar.read(new File(input));

		Map<String, Element> modules = new HashMap<String, Element>();
//		Element eCategory = (Element) docSource.selectSingleNode("//category_settings/categories");
		Element element;
		
		//建一个ORG字段
		element = (Element) docSource.selectSingleNode("/diagram/dictionary");
		element = element.addElement("word");
		element.addElement("id").setText("99999");
		element.addElement("length").setText("20");
		element.addElement("logical_name").setText("机构号");
		element.addElement("physical_name").setText("ORG_ID");
		element.addElement("type").setText("varchar(n)");
		
		int colid = 100000;	//用于ORG字段的编号
		
		//把表名填进去
		for (Table table : pdm.getTables())
		{
			String xpath = "/diagram/contents/table[physical_name='" + table.getDbName() + "']";
			System.out.println(xpath);
			Element elementTable = (Element) docSource.selectSingleNode(xpath);
			if (elementTable != null)
			{
				//记录module
				String module = StringUtils.mid(table.getDbName(), 4, 3);
				modules.put(module, null);
//				if (!modules.containsKey(module))
//				{
//					element = eCategory.addElement("category");
//					modules.put(module, element);
//					element.addElement("name").setText(module);
//					element.addElement("selected").setText("true");
//				}
//				element = modules.get(module);
//				element.addElement("node_element").setText(elementTable.elementText("id"));

				//清空schema
				elementTable.element("table_properties").element("schema").setText("");
				
				elementTable.element("logical_name").setText(table.getTextName());
				if (StringUtils.isNotBlank(table.getDescription()))
					elementTable.element("description").setText(table.getDescription());
				//把字段名和注释填进去
				HashMap<String, Column> cols = new HashMap<String, Column>();
				
				for (Column column : table.getColumns())
				{
					cols.put(column.getDbName(), column);
				}
				
				int insertPos = 0;	//找出第一个非主键字段
				for (Element e : (List<Element>)elementTable.selectNodes("columns/*"))
				{
					String word_id = e.elementText("word_id");
					xpath = MessageFormat.format("/diagram/dictionary/word[id=''{0}'']", word_id);
//					System.out.println(xpath);

					element = (Element) docSource.selectSingleNode(xpath);
					String dbname = element.elementText("physical_name");
					element.element("logical_name").setText(cols.get(dbname).getTextName());
					if (StringUtils.isNotEmpty(cols.get(dbname).getDescription()))
						element.element("description").setText(cols.get(dbname).getDescription());
					
					//填domain
					
					if (e.elementText("primary_key").equals("true"))
						insertPos++;
				}
				
				//插ORG字段
				element = (Element) elementTable.selectSingleNode("columns");
				Element col = DocumentHelper.createElement("normal_column");
				element.elements().add(insertPos, col);
				
				col.addElement("word_id").setText("99999");
				col.addElement("id").setText(String.valueOf(colid++));
				col.addElement("type").setText("varchar(n)");
				col.addElement("not_null").setText("true");
				col.addElement("sequence");				
			}
		}
		
		//清理sequence的schema
		for (Node n : (List<Node>)docSource.selectNodes("/diagram/sequence_set/sequence/schema"))
		{
			n.setText("");
		}
		
		//清理BTREE
		for (Node n : (List<Node>)docSource.selectNodes("//inidex/type"))
		{
			n.setText("");
		}
		
		//清理default_value里的单引号
		for (Node n : (List<Node>)docSource.selectNodes("//normal_column/default_value"))
		{
			String t = n.getText();
			if (StringUtils.isNotEmpty(t))
				n.setText(StringUtils.remove(t, "'"));
		}
//		FileOutputStream fos = new FileOutputStream("out.apm.erm");
//		XMLWriter xw = new XMLWriter(fos, OutputFormat.createPrettyPrint());
//		xw.write(docSource);
//		xw.close();
//		fos.close();
		
		//分文件
		for (String module : modules.keySet())
		{
			System.out.println("输出" + module);
			Document doc = (Document) docSource.clone();
			
			Map<String, Element> wordsUsed = new HashMap<String, Element>();
			//清理非该模块的表
			for (Element elementTable : (List<Element>)doc.selectNodes("/diagram/contents/table"))
			{
				if (!StringUtils.mid(elementTable.elementText("physical_name"), 4, 3).equals(module))
				{
					elementTable.detach();
				}
				else
				{
					//登记word的使用
					for (Element id : (List<Element>)elementTable.selectNodes("columns/*/word_id"))
						wordsUsed.put(id.getText(), id);
				}
			}
			//清理sequence
			for (Element elementSequence : (List<Element>)doc.selectNodes("/diagram/sequence_set/sequence"))
			{
				if (!StringUtils.mid(elementSequence.elementText("name"), 4, 3).equals(module))
				{
					elementSequence.detach();
				}
			}
			
			
			//清理未使用word
			for (Element elementWord : (List<Element>)doc.selectNodes("/diagram/dictionary/word"))
			{
				if (!wordsUsed.containsKey(elementWord.elementText("id")))
				{
					elementWord.detach();
				}
			}
		
			FileOutputStream fos = new FileOutputStream(StringUtils.lowerCase("out."+module+".erm"));
			XMLWriter xw = new XMLWriter(fos, OutputFormat.createPrettyPrint());
			xw.write(doc);
			xw.close();
			fos.close();
		}
	}
}
