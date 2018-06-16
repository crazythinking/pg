package net.engining.pg.parameter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

/**
 * 消息刷新广播器，用于转发本地刷新消息和网络消息。为防止递归，本地消息用this引用来标识自身，网络消息用随机生成的UUID。
 * 需要按AMQP listener配置，调用 {@link #messageReceived(ParameterChangedData)} 方法
 * @author binarier
 *
 */
public class AmqpParameterChangedBroadcaster implements ApplicationListener<ParameterChangedEvent> , ApplicationContextAware {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;
	
	@Autowired
	private AmqpTemplate amqpTemplate;
	
	private String amqpTopicExchange = "amq.topic";
	
	private String amqpTopicPattern = "parameter.changed";
	
	/**
	 * 用于在网络广播时标识自己
	 */
	private UUID uuid = UUID.randomUUID();
	
	@Override
	public void onApplicationEvent(ParameterChangedEvent event) {
		//收到本地参数刷新消息并且不是自己发出的
		if (event.getSource() != this)
		{
			ParameterChangedData data = new ParameterChangedData();
			data.setOrgId(event.getOrgId());
			data.setParamClass(event.getParamClass());
			data.setKey(event.getKey());
			data.setSource(uuid);	//标识自己
			//转播出去
			logger.info("发送参数刷新消息至AMQP[{}, {}, {}]", data.getOrgId(), data.getParamClass(), data.getKey());
			amqpTemplate.convertAndSend(amqpTopicExchange, amqpTopicPattern, data);
		}
	}
	
	public void messageReceived(ParameterChangedData data)
	{
		//收到广播参数刷新消息并且不是自己发出的
		if (!uuid.equals(data.getSource()))
		{
			logger.info("从AMQP收到参数刷新消息[{}, {}, {}]", data.getOrgId(), data.getParamClass(), data.getKey());
			applicationContext.publishEvent(new ParameterChangedEvent(this, data.getOrgId(), data.getParamClass(), data.getKey()));
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public AmqpTemplate getAmqpTemplate() {
		return amqpTemplate;
	}

	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public String getAmqpTopicExchange() {
		return amqpTopicExchange;
	}

	/**
	 * 用户消息广播的exchange，默认为rabbitmq的内置 amq.topic
	 * @param amqpTopicExchange
	 */
	public void setAmqpTopicExchange(String amqpTopicExchange) {
		this.amqpTopicExchange = amqpTopicExchange;
	}

	public String getAmqpTopicPattern() {
		return amqpTopicPattern;
	}

	/**
	 * 参数刷新消息的消息名，默认为parameter.changed
	 * @param amqpTopicPattern
	 */
	public void setAmqpTopicPattern(String amqpTopicPattern) {
		this.amqpTopicPattern = amqpTopicPattern;
	}

}
