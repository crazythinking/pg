package net.engining.pg.mq.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class AmqpInvokerClientInterceptor implements InvocationHandler {
	
	public final static String RPC_VERSION_HEADER = "yak.rpc.version";
	
	public final static String RPC_ASYNC_HEADER = "yak.async";
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private DestinationResolver destinationResolver;

	private String queueName;
	
	private AmqpTemplate amqpTemplate;
	
	private boolean async = false;
	
	private Set<String> asyncMethodNames = new HashSet<String>();
	
	private long rpcTimeout = -1;
	
	/**
	 * 随报文发送的接口版本信息
	 */
	private String rpcVersion;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RemoteInvocation remoteInvocation = new RemoteInvocation(method.getName(), method.getParameterTypes(), args);
		String actualQueueName;
		if (destinationResolver != null)
			actualQueueName = destinationResolver.getActualQueueName(queueName);
		else
			actualQueueName = queueName;

		if (isAsync() || asyncMethodNames.contains(method.getName()))
		{
			if (logger.isDebugEnabled())
				logger.debug("发送异步消息到[{}]，方法名为[{}]", actualQueueName, method.getName());
			//异步方式使用，同时要告知服务端不要发送响应
			amqpTemplate.convertAndSend(actualQueueName, remoteInvocation, new MessagePostProcessor() {

				@Override
				public Message postProcessMessage(Message message) throws AmqpException {
					//把版本加入消息头中
					message.getMessageProperties().setHeader(RPC_VERSION_HEADER, rpcVersion);
					//指定不要发响应
					message.getMessageProperties().setHeader(RPC_ASYNC_HEADER, "*");
					return message;
				}
			});
			return null;
		}
		else
		{
			//同步调用，要等响应的
			if (logger.isDebugEnabled())
				logger.debug("发送同步消息到[{}]，方法名为[{}]", actualQueueName, method.getName());
			Object response = amqpTemplate.convertSendAndReceive(actualQueueName, remoteInvocation, new MessagePostProcessor() {
				@Override
				public Message postProcessMessage(Message message) throws AmqpException {
					MessageProperties props = message.getMessageProperties();
					//把版本加入消息头中
					props.setHeader(RPC_VERSION_HEADER, rpcVersion);
					if (rpcTimeout != -1)
					{
						props.setExpiration(String.valueOf(rpcTimeout));
						logger.debug("设置RPC消息的TTL为{}", rpcTimeout);
					}
					return message;
				}
			});
			
			if (response == null)
			{
				logger.error("RPC服务响应超时[{}]", actualQueueName);
				throw new IllegalArgumentException("RPC服务响应超时:" + actualQueueName);
			}
			if (response instanceof RemoteInvocationResult)
				return ((RemoteInvocationResult)response).recreate();
			else
				throw new IllegalArgumentException("无效返回对象:" + response); 
		}
	}

	public AmqpTemplate getAmqpTemplate() {
		return amqpTemplate;
	}
	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public DestinationResolver getDestinationResolver() {
		return destinationResolver;
	}

	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	public String getRpcVersion() {
		return rpcVersion;
	}

	public void setRpcVersion(String rpcVersion) {
		this.rpcVersion = rpcVersion;
	}

	public boolean isAsync() {
		return async;
	}

	/**
	 * 异步方式使用，将会抛弃响应报文。
	 * @param async
	 */
	public void setAsync(boolean async) {
		this.async = async;
	}

	public Set<String> getAsyncMethodNames() {
		return asyncMethodNames;
	}

	public void setAsyncMethodNames(Set<String> asyncMethodNames) {
		this.asyncMethodNames = asyncMethodNames;
	}

	public long getRpcTimeout() {
		return rpcTimeout;
	}

	public void setRpcTimeout(long rpcTimeout) {
		this.rpcTimeout = rpcTimeout;
	}
}
