package net.engining.pg.mq.config;

public interface DestinationResolver {
	String getActualQueueName(String queueName);
}
