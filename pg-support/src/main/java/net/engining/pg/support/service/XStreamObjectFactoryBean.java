package net.engining.pg.support.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 * 指定通过xml文件建立一个Spring的Bean
 * @author licj
 *
 */
public class XStreamObjectFactoryBean<T> implements FactoryBean<T>
{
	private Resource xml;
	
	private T object;
	
	/**
	 * @param xml
	 */
	public XStreamObjectFactoryBean(Resource xml) {
		super();
		this.xml = xml;
	}

	@Override
	public T getObject() throws Exception {
		
		return getTargetObject();
	}

	@Override
	public Class<?> getObjectType() {
		try
		{
			return getTargetObject().getClass();
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Resource getXml() {
		return xml;
	}
	
	@SuppressWarnings("unchecked")
	private T getTargetObject() throws IOException
	{
		if (object == null)
		{
			XStream xStream = new XStream();
			InputStream is = xml.getInputStream();
			object = (T)xStream.fromXML(is);
			is.close();
		}
		return object;
	}

	public void setXml(Resource xml) {
		this.xml = xml;
	}

}
