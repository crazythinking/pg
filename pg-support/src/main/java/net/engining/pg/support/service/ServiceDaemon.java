package net.engining.pg.support.service;

import java.io.FileNotFoundException;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 基础的服务Daemon.
 * 可以带一个参数，指定在classpath中加载的spring配置文件名，程序会自动加上 "-context.xml"后缀
 * @author licj
 *
 */
public class ServiceDaemon implements Daemon {
	protected static final Logger logger = LoggerFactory.getLogger(ServiceDaemon.class);
	
	private ConfigurableApplicationContext ctx;

	@Override
	public void init(DaemonContext context) throws DaemonInitException,	Exception
	{
		logger.info("系统后台轮询服务正在启动#####################################################################");
//		initLogging();
		
		ctx = new ClassPathXmlApplicationContext(getContextFilename(context.getArguments()));
		
		ctx.registerShutdownHook();
	}

	/*private static void initLogging() throws FileNotFoundException {
		String logLocation = System.getProperty("log.config");
		if (StringUtils.isNotBlank(logLocation))
			Log4jConfigurer.initLogging(logLocation, 1000 * 60);//刷新间隔一分钟
	}*/

	@Override
	public void start() throws Exception
	{
	}

	@Override
	public void stop() throws Exception
	{
	}

	@Override
	public void destroy()
	{
		ctx.close();
	}

	/**
	 * 这样可以直接跑，要关闭就杀掉进程
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		logger.info("系统后台轮询服务正在启动#####################################################################");
		ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(getContextFilename(args));
		ctx.registerShutdownHook();
	}
	
	public static String getContextFilename(String args[])
	{
		String filename = "/service-context.xml";
		if (args.length > 1 )
			filename = "/" + args[0] + "-context.xml";
		return filename;
	}
}
