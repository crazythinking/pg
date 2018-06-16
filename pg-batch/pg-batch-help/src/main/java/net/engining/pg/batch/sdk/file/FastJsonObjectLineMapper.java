package net.engining.pg.batch.sdk.file;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;

/**
 * 代替JsonLineMapper，其只能转为Map<String, Object>，且对带有map或list的复杂json不支持；
 * 这里利用FastJson转换为具体领域对象
 * @author luxue
 *
 */
public class FastJsonObjectLineMapper<D> implements LineMapper<D>, InitializingBean {
	
	private Class<D> detailClass;
	
	private D detailObject;

	@Override
	public D mapLine(String line, int lineNumber) throws Exception {
		
		detailObject = JSON.parseObject(line, detailClass);
		return detailObject;
	}

	public void setDetailClass(Class<D> detailClass) {
		this.detailClass = detailClass;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(detailClass, "The detailClass must be set");
		
	}

}
