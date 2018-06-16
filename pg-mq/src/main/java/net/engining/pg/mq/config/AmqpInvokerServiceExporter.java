package net.engining.pg.mq.config;

import net.engining.pg.mq.exception.RPCShouldRequeueException;
import net.engining.pg.support.meta.RPCVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * 按最简单的方式实现的基于AMQP的RPC实现
 * @author licj
 *
 */
public class AmqpInvokerServiceExporter extends RemoteInvocationBasedExporter implements MessageListener, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Object proxy;
	
	private MessageConverter messageConverter = new SimpleMessageConverter();
	
	private AmqpTemplate amqpTemplate;
	
	private String rpcVersion;
	
	@Override
	public void onMessage(Message message) 
	{
		Object content = messageConverter.fromMessage(message);
		
		//验证接口版本
		String clientVersion = (String)message.getMessageProperties().getHeaders().get(AmqpInvokerClientInterceptor.RPC_VERSION_HEADER);
		
		//TODO 需要按版本号规则来校验，而不是简单的比较
		if (clientVersion != null && rpcVersion != null && clientVersion.compareTo(rpcVersion) != 0)
			logger.warn("接口[{}]的服务端版本({})与客户端版本({})不一致", getServiceInterface().getCanonicalName(), rpcVersion, clientVersion);
		
		if (content instanceof RemoteInvocation)
		{
			RemoteInvocation request = (RemoteInvocation)content;
			//调用实际服务
			RemoteInvocationResult result = invokeAndCreateResult(request, proxy);
			
			if (result.hasException() && result.getException().getCause() instanceof RPCShouldRequeueException)
			{
				//应用指定需要requeue，则抛出该异常。如果container上没有指定requeue-rejected="false"，则容器会reject消息并且requeue
				logger.warn("RPC服务要求requeue操作", result.getException());
				throw (RPCShouldRequeueException)result.getException();
			}
			
			if (message.getMessageProperties().getHeaders().containsKey(AmqpInvokerClientInterceptor.RPC_ASYNC_HEADER))
			{
				//异步调用，不发送响应，但是要检查响应是不是有异常
				if (result.getException() != null)
					logger.warn("异步调用发生异常", result.getException());
			}
			else
			{
				//发送同步调用的响应
				String replyTo = message.getMessageProperties().getReplyTo();
	
				MessageProperties mp = new MessageProperties();
				//响应消息的header以请求为基础，以支持如 spring_reply_correlation之类的header
				mp.getHeaders().putAll(message.getMessageProperties().getHeaders());
				
				Message response = messageConverter.toMessage(result, mp);
	
				amqpTemplate.send(replyTo, response);
			}
		}
		else
		{
			throw new IllegalArgumentException("无效请求对象" + content);
		}
	}

	public MessageConverter getMessageConverter() {
		return messageConverter;
	}

	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public AmqpTemplate getAmqpTemplate() {
		return amqpTemplate;
	}

	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		proxy = getProxyForService();
		Class<?> serviceInterface = getServiceInterface();
		RPCVersion rpcVersionAnno = serviceInterface.getAnnotation(RPCVersion.class);
		if (rpcVersionAnno != null)
			rpcVersion = rpcVersionAnno.value();
		else
			logger.warn("服务接口[{}]没有指定@RPCVersion。", serviceInterface.getCanonicalName());
	}

}
