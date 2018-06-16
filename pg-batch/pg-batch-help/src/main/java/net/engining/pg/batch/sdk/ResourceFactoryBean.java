package net.engining.pg.batch.sdk;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * 用于把资源做成独立的bean
 * @author licj
 *
 */
public class ResourceFactoryBean implements FactoryBean<Resource> {
	
	private Resource resource;

	@Override
	public Resource getObject() throws Exception {
		return resource;
	}

	@Override
	public Class<?> getObjectType() {
		return Resource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
