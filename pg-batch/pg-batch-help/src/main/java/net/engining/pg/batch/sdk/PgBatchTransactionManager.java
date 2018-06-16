package net.engining.pg.batch.sdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.persistence.EntityManager;

import org.springframework.orm.jpa.JpaTransactionManager;

/**
 * 批量专用的事务管理器.
 * 这个是为了解决Spring的事务模型完全是针对联机服务设计的问题。
 * 默认情况下，每次事务提交，TM都会把当前线程绑定的EntityManager关闭，这对于批量程序来说是不可接受的。
 * 所以在这里，把初次获取的EntityManager存在ThreadLocal中，并且为之建立一个动态代理，把close方法的调用屏蔽，避免被关。
 * 通过这样处理，Reader中可以维持一个Hibernate Cursor，并且保持读出的对象为托管对象，以便于processor中的修改。
 * 同时，事务的管理还是按TransactionManager一样统一管理，并且也可以做到与JDBC协同事务。
 * 但需要注意的是，在线程结束时，需要有代码调用 {@link #closeCurrentEntityManager()}来实际关闭对象，需要配合使用 {@link PgBatchStepListener}。
 * deprecated:由于一级缓存问题，不再使用
 * @author licj
 *
 */
@Deprecated
public class PgBatchTransactionManager extends JpaTransactionManager {
	
	private static final long serialVersionUID = 1L;

	private ThreadLocal<EntityManager> emProxy = new ThreadLocal<EntityManager>();
	
	private ThreadLocal<EntityManager> emTarget = new ThreadLocal<EntityManager>();
	
	@Override
	protected EntityManager createEntityManagerForTransaction() {
		EntityManager em = emProxy.get();
		if (em == null)
		{
			final EntityManager target = super.createEntityManagerForTransaction();
		
			em = (EntityManager)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{EntityManager.class}, new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					try
					{
						if ("close".equals(method.getName()))
						{
							//把close屏蔽
							return null;
						}
						return method.invoke(target, args);
					}
					catch (InvocationTargetException ite)
					{
						//内部发生的异常会被包在这里面，所以需要在这里打开
						throw ite.getCause();
					}
				}
			});
			emProxy.set(em);
			emTarget.set(target);
		}
		return em;
	}
	
	public void closeCurrentEntityManager()
	{
		EntityManager em = emTarget.get();
		if (em != null)
		{
			em.close();
			emTarget.remove();
			emProxy.remove();
//			emTarget.set(null);
//			emProxy.set(null);
		}
	}
}
