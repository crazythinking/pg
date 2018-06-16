package net.engining.pg.mq.test;

public class GreetingsImpl implements GreetingsApi
{

	@Override
	public String hello(String name)
	{
		return "hello " + name;
	}

}
