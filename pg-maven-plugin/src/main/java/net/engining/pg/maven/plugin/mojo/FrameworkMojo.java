package net.engining.pg.maven.plugin.mojo;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.SimpleNamespaceContext;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@Mojo(name = "framework", threadSafe = true, requiresProject = false)
public class FrameworkMojo extends AbstractMojo {

	@Parameter(property = "groupId", required = true)
	private String groupId;

	/**
	 * 默认使用groupId的最后一个词
	 */
	@Parameter(property = "artifactPrefix")
	private String artifactPrefix;

	@Parameter(property = "version", defaultValue = "0.0.1-SNAPSHOT")
	private String version;

	@Parameter(property = "name", defaultValue = "Project")
	private String name;

	@Parameter(property = "template", defaultValue = "https://coding.net/u/binarier/p/powergear-demo/git/archive/master")
	private String template;

	private SimpleNamespaceContext namespace = new SimpleNamespaceContext(
			ImmutableMap.of("p", "http://maven.apache.org/POM/4.0.0"));

	@Override
	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {

		Log logger = getLog();

		if (StringUtils.isBlank(artifactPrefix)) {
			String words[] = StringUtils.split(groupId, ".");
			artifactPrefix = words[words.length - 1];
		}
		try {
			URL url = new URL(template);
			List<String> files = extractFiles(url);

			// 确定根目录
			File root = null;
			for (String file : files) {
				if (file.endsWith("/pom.xml")) {
					root = new File(file.substring(0, file.indexOf('/')));
					break;
				}
			}
			if (root == null)
				throw new RuntimeException("Zip包结构不对，没有根目录");

			logger.info("根目录为" + root);

			String targetDir = getDirName(groupId);
			FileUtils.moveDirectory(root, new File(targetDir));
			root = new File(targetDir);
			logger.info(String.format("修改根目录为[%s]", root));

			File rootPomFile = new File(root, "pom.xml");

			// 这里用dom4j来解析替换pom文件而不用maven-model，是因为maven-model不但会破坏文件风格，还会丢失注释。
			SAXReader reader = new SAXReader();
			Document dom = reader.read(rootPomFile);

			String originalGroupId = groupId(dom).getText();
			String originalArtifactPrefix = StringUtils.remove(artifactId(dom).getText(), "-parent");
			String originalNamePrefix = name(dom).getText();

			groupId(dom).setText(groupId);
			artifactId(dom).setText(artifactPrefix + "-parent");
			version(dom).setText(version);
			name(dom).setText(name);
			for (Node module : (List<Node>) xpath("/p:project/p:modules/*").selectNodes(dom)) {
				String oldModule = module.getText();
				String newModule = StringUtils.replace(oldModule, originalArtifactPrefix, artifactPrefix);
				module.setText(newModule);

				// 替换目录
				logger.info(String.format("修改[%s]为[%s]", oldModule, newModule));
				FileUtils.moveDirectory(new File(root, oldModule), new File(root, newModule));
				replaceModulePom(originalGroupId, originalArtifactPrefix, originalNamePrefix,
						new File(new File(root, newModule), "pom.xml"));

			}

			String originalVersionProperty = originalArtifactPrefix + ".version";
			Element vp = (Element) xpath("/p:project/p:properties/p:" + originalVersionProperty).selectSingleNode(dom);
			if (vp != null) {
				Element properties = (Element) xpath("/p:project/p:properties").selectSingleNode(dom);
				properties.remove(vp);

				String newProperty = artifactPrefix + ".version";
				properties.addElement(new QName(newProperty, properties.getNamespace())).setText(version);

				// 替换所有的引用
				for (Node dep : (List<Node>) xpath("/p:project/p:dependencyManagement/p:dependencies/p:dependency")
						.selectNodes(dom)) {
					if (originalGroupId.equals(xpath("p:groupId").selectSingleNode(dep).getText())) {
						xpath("p:groupId").selectSingleNode(dep).setText(groupId);
						Node nodeArtifactId = xpath("p:artifactId").selectSingleNode(dep);
						nodeArtifactId.setText(
								StringUtils.replace(nodeArtifactId.getText(), originalArtifactPrefix, artifactPrefix));
					}

					Node nodeVersion = xpath("p:version").selectSingleNode(dep);
					if (nodeVersion.getText().equals("${" + originalVersionProperty + "}")) {
						nodeVersion.setText("${" + newProperty + "}");
					}
				}
			}

			outputPomFile(rootPomFile, dom);

			// 移动包的目录结构
			String fromDir = StringUtils.replace(originalGroupId, ".", "/");
			String toDir = StringUtils.replace(groupId, ".", "/");
			for (File module : root.listFiles((FileFilter) FileFilterUtils.directoryFileFilter())) {
				logger.info(module.toString());
				for (String dir : new String[] { "src/main/java", "src/main/resources", "src/test/java",
						"src/test/resources" }) {
					File target = new File(module, dir + "/" + fromDir);
					logger.info("移动" + target);
					if (target.exists() && target.isDirectory()) {
						File to = new File(module, dir + "/" + toDir);
						logger.info(String.format("移动[%s]到[%s]", target, to));
						FileUtils.moveDirectory(target, to);
					}
				}
			}

			// 替换所有的包名
			for (File file : FileUtils.listFiles(root, new String[] { "xml", "java", "properties" }, true)) {
				logger.info("替换" + file);

				String content = FileUtils.readFileToString(file, Charsets.UTF_8);
				FileUtils.writeStringToFile(file, StringUtils.replace(content, originalGroupId, groupId),
						Charsets.UTF_8);
			}

		} catch (Exception e) {
			throw new MojoFailureException("运行出错", e);
		}
	}

	private void outputPomFile(File rootPomFile, Document dom)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		XMLWriter xw = new XMLWriter(new FileOutputStream(rootPomFile), OutputFormat.createPrettyPrint());
		xw.write(dom);
		xw.close();
	}

	private XPath xpath(String xpath) {
		XPath x = DocumentHelper.createXPath(xpath);
		x.setNamespaceContext(namespace);
		return x;
	}

	private Node name(Document dom) {
		return xpath("/p:project/p:name").selectSingleNode(dom);
	}

	private Node artifactId(Document dom) {
		return xpath("/p:project/p:artifactId").selectSingleNode(dom);
	}

	private Node groupId(Document dom) {
		return xpath("/p:project/p:groupId").selectSingleNode(dom);
	}

	private Node version(Document dom) {
		return xpath("/p:project/p:version").selectSingleNode(dom);
	}

	@SuppressWarnings("unchecked")
	private void replaceModulePom(String originalGroupId, String originalArtifactPrefix, String originalNamePrefix,
			File pomFile) throws IOException, XmlPullParserException, FileNotFoundException, DocumentException {
		SAXReader reader = new SAXReader();
		Document dom = reader.read(pomFile);

		Node nodeParent = xpath("/p:project/p:parent").selectSingleNode(dom);
		xpath("p:groupId").selectSingleNode(nodeParent).setText(groupId);
		xpath("p:artifactId").selectSingleNode(nodeParent).setText(artifactPrefix + "-parent");
		xpath("p:version").selectSingleNode(nodeParent).setText(version);
		artifactId(dom).setText(StringUtils.replace(artifactId(dom).getText(), originalArtifactPrefix, artifactPrefix));
		name(dom).setText(StringUtils.replace(name(dom).getText(), originalNamePrefix, name));

		for (Node dep : (List<Node>) xpath("/p:project/p:dependencies/p:dependency").selectNodes(dom)) {
			Node nodeGroupId = xpath("p:groupId").selectSingleNode(dep);
			if (nodeGroupId.getText().equals(originalGroupId)) {
				nodeGroupId.setText(groupId);
				Node nodeArtifactId = xpath("p:artifactId").selectSingleNode(dep);
				nodeArtifactId
						.setText(StringUtils.replace(nodeArtifactId.getText(), originalArtifactPrefix, artifactPrefix));
			}
		}
		outputPomFile(pomFile, dom);
	}

	private String getDirName(String packageName) {
		int i = packageName.lastIndexOf('.');
		if (i == -1) {
			return packageName;
		} else {
			return packageName.substring(i + 1);
		}
	}

	private List<String> extractFiles(URL url) throws IOException, FileNotFoundException {
		List<String> files = Lists.newArrayList();
		InputStream is = url.openStream();
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			getLog().debug(entry.getName());
			files.add(entry.getName());
			File file = new File(entry.getName());
			if (entry.getName().endsWith("/")) {
				file.mkdirs();
			} else {
				if (file.getParentFile() != null)
					file.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(file);
				IOUtils.copy(zis, fos);
				fos.close();
			}
			zis.closeEntry();
		}
		zis.close();
		return files;
	}
}
