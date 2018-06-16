package net.engining.pg.mq.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/new-rpc-context.xml") 
public class NewRPCTest
{
	@Resource
	private GreetingsApi serviceClient;
	
	@Test
	public void abc()
	{
		assertThat(serviceClient, is(not(instanceOf(GreetingsImpl.class))));
		assertThat(serviceClient.hello("world"), equalTo("hello world"));
	}
}