package net.engining.pg.batch.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

/**
 * 使用临时文件为后台，实现 {@link Resource}接口。测试代码中可以使用 {@link #getFile()}来获取对应文件。
 * 
 * @author licj
 *
 */
public class ResourceMock extends AbstractResource implements BeanNameAware {
	private String beanName;

	private File file;

	private boolean deleteOnExit = true;

	@Override
	public String getDescription() {
		return beanName;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@PostConstruct
	public void init() throws IOException {
		// 建立临时文件，用bean name作前缀，以便行测试的调试
		file = File.createTempFile(beanName, null);
		if (deleteOnExit) {
			file.deleteOnExit();
		}
	}

	@Override
	public File getFile() {
		return file;
	}

	/**
	 * 指定是否在退出时删除，默认为true，指定为false可以用于调试目的，临时文件位于默认临时目录下，以bean name为前缀
	 */
	public void setDeleteOnExit(boolean deleteOnExit) {
		this.deleteOnExit = deleteOnExit;
	}

	@Override
	public URL getURL() throws IOException {
		return new URL("file://" + file.getAbsolutePath());
	}
}
