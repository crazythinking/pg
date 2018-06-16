package net.engining.pg.parameter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程上下文的工具类，可在此上下文中存取线程共享的对象，不可实例化
 * 
 * @author zhangkun
 * 
 */
public abstract class ThreadContextHolder {

	private static Map<String, ThreadLocal<Object>> context = new ConcurrentHashMap<String, ThreadLocal<Object>>();

	/**
	 * 取得上下文中的对象
	 * 
	 * @param name
	 *            对象名
	 * @return 输入对象名对应的上下文对象
	 */
	public static Object getObject(String name) {
		if (context.containsKey(name))
			return context.get(name).get();
		else
			return null;
	}

	/**
	 * 设置上下文对象
	 * 
	 * @param name
	 *            对象名
	 * @param obj
	 *            要保存在上下文中的对象
	 */
	public static synchronized void setObject(String name, Object obj) {
		if (context.get(name) == null) {
			ThreadLocal<Object> objHolder = new ThreadLocal<Object>();
			context.put(name, objHolder);
		}
		context.get(name).set(obj);
	}

}
