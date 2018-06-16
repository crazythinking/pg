package net.engining.pg.mq.config;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import net.engining.pg.support.meta.RPCAsync;
import net.engining.pg.support.meta.RPCQueueName;
import net.engining.pg.support.meta.RPCVersion;

/**
 * 建立RPC代理Bean
 * @author licj
 */
public class AmqpInvokerClientFactoryBean<T> extends AmqpInvokerClientInterceptor implements FactoryBean<T>, InitializingBean
{
	private Logger logger = LoggerFactory.getLogger(getClass());

	private Class<?> serviceInterface;
	
	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		
		//初始化
		if (getQueueName() != null)
		{
			logger.info("指定的目标列队名[{}]，覆盖接口定义。", getQueueName());
		}
		else
		{
			RPCQueueName name = serviceInterface.getAnnotation(RPCQueueName.class);
			if (name == null)
				throw new IllegalArgumentException("接口" + serviceInterface.getCanonicalName() + "没有指定@RPCQueueName");
			setQueueName(name.value());
		}
		
		RPCVersion rpcVersion = serviceInterface.getAnnotation(RPCVersion.class);
		if (rpcVersion == null)
			logger.warn("接口[{}]没有指定@RPCVersion", serviceInterface.getCanonicalName());
		else
			setRpcVersion(rpcVersion.value());
		
		if (serviceInterface.getAnnotation(RPCAsync.class) != null)
		{
			setAsync(true);
		}
		
		//检查所有方法是否有@RPCAsync
		Set<String> names = getAsyncMethodNames();
		
		for (Method method : serviceInterface.getMethods())
		{
			boolean asyncMethod = (method.getAnnotation(RPCAsync.class) != null);
			if (asyncMethod)
			{
				names.add(method.getName());
				logger.debug("异步方法{}", method.getName());
			}
			if (!method.getReturnType().equals(void.class) &&
				(isAsync() || asyncMethod))
			{
				logger.warn("方法{}被标记为异步，但其返回值不为void。({})", method.getName(), method.getReturnType().getCanonicalName());
			}
		}
		
		if (getRpcTimeout() == -1 && getAmqpTemplate() instanceof RabbitTemplate)
		{
			//从RabbitTemplate里取RPC超时时间
			//实在不愿意这么干，因为又取了private成员，同时还增加了对spring-rabbit的依赖，做法很不好。
			//但在AMQPTempalte或RabbitTemplate不加入getTimeout()方法的前提下，这是唯一的办法。
			//TODO 换一个更优雅的办法
			RabbitTemplate rt = (RabbitTemplate) getAmqpTemplate();
			Long timeout = (Long) FieldUtils.readField(rt, "replyTimeout", true);
			timeout = Math.max(timeout - 5000, 1000);	//留5秒处理时间，最短1秒
			logger.debug("配置RPC消息的TTL为{}", timeout);
			setRpcTimeout(timeout);
		}
		logger.info("建立RPC客户端代理接口[{}]，版本[{}]，目标队列[{}]。", serviceInterface.getCanonicalName(), getRpcVersion(), getQueueName());
		
		return (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{serviceInterface}, this);
	}

	@Override
	public Class<?> getObjectType() {
		return serviceInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Class<?> getServiceInterface() {
		return serviceInterface;
	}
	@Required
	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//由于有子类调用了该父类，所以还必须保留该方法，等以后gmp都更新到1.0.6以后才可以拿掉，完全由getObject代替
	}
}
