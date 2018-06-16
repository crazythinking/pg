package net.engining.pg.batch.sdk;


/**
 * 用于处理子游标的情况
 * @author licj
 *
 * @param <SUB> 子游标对应的实体
 * @param <INFO> 批量的传递结构体，用于在回调时传给子类
 */
public abstract class AbstractPgSubCursorHelper<SUB, INFO> extends AbstractPgCursorHelper<INFO>
{
	/**
	 * 子游标比较处理结果 
	 */
	public enum CompareResult { 
		/**
		 * 表明当前子游标所在的记录是与主游标匹配的，需要加入统一结构体
		 */
		PROCESS, 
		/**
		 * 表明当前子游标所在记录与主游标不匹配，并且当前记录落后于主游标，需要跳过。一般这出现在子游标包含主游标中不存在的key的情况
		 */
		SKIP,
		/**
		 * 表明当前子游标所在记录与主游标不匹配，并且当前记录超前于主游标，需要结束处理，等下一条再说。 
		 */
		OVER 
	};
	
	private SUB lastEntity;
	
	protected boolean ended = false;
	
	/**
	 * 取子游标的下一条记录，并且取到的数据通过调用 {@link #compareEntity(Object)}的回调返回结果来确定处理的行为
	 * @param info
	 */
	@SuppressWarnings("unchecked")
	public void fetchCursor(INFO info)
	{
		while (!ended)
		{
			if (lastEntity != null)
			{
				switch (compareEntity(lastEntity))
				{
				case PROCESS:
					processSubEntity(lastEntity, info);
					break;
				case OVER:
					//如果已经超过当前主表，则直接返回
					return;
				case SKIP:
					//如果有主表里没有的记录，则可以使用这个返回值，表示不处理直接跳过，取下一条
					break;
				default:
				}
			}
			
			if (cursor.next())
			{
				lastEntity = (SUB) cursor.get(0);
			}
			else
			{
				ended = true;
			}
		}
	}
	
	/**
	 * 判断传入的子游标实体中的key与主游标的关系，即是否应该包在同一个INFO对象中处理。
	 * @param subEntity
	 * @return 响应结果，参见 {@link CompareResult}
	 */
	protected abstract CompareResult compareEntity(SUB subEntity);
	
	/**
	 * 处理符合条件的子游标对象
	 * @param entity
	 * @param info
	 */
	protected abstract void processSubEntity(SUB entity, INFO info);
}
