package net.engining.pg.mq.exception;

/**
 * RPC服务可以通过抛出这个异常来指定当前消息处理失败，并且需要requeue重新处理。<br/>
 * 注意：container配置上如果指定了requeue-rejected="false"，那么该异常无效。
 * @author licj
 *
 */
public class RPCShouldRequeueException extends RuntimeException {

	private static final long serialVersionUID = -4244511655234909041L;

	public RPCShouldRequeueException()
	{
		super();
	}
	
	public RPCShouldRequeueException(String message)
	{
		super(message);
	}
	
	public RPCShouldRequeueException(Throwable t)
	{
		super(t);
	}
	
	public RPCShouldRequeueException(String message, Throwable t)
	{
		super(message, t);
	}
}
