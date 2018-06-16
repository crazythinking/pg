package net.engining.pg.batch.sdk;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.transaction.annotation.Transactional;

import net.engining.pg.batch.entity.model.PgKeyContext;

/**
 * 基于主键模式的读取reader，注意，这里不限于数据库表，理论上可以是任何数据实体，Key与实体也不一定有数据库的关系，支持断点续批；
 * 另外根据AbstractItemCountingItemStreamItemReader的说明，需要确保主键（值，数量，顺序）在执行完成前保持不变；并且Reader必须是单线程的；
 * 
 * @author licj
 *
 * @param <KEY>
 *            主键对象类型
 * @param <INFO>
 *            处理实体类型
 */
public abstract class KeyBasedStreamReader<KEY, INFO> extends AbstractItemCountingItemStreamItemReader<INFO>
		implements ItemStreamReader<INFO>, Partitioner, BeanNameAware {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private List<KEY> allKeys;

	private Iterator<KEY> keyIterator;
	
	private KEY key;

	private final String KEY_CONTEXT_KEY = "keyContextId";

	@PersistenceContext
	private EntityManager em;

	private int minPartitionSize = 1000;

	/**
	 * 默认不限制分片大小
	 */
	private int maxPartitionSize = Integer.MAX_VALUE;

	/**
	 * 加载要处理记录的Keys
	 * @return
	 */
	protected abstract List<KEY> loadKeys();

	/**
	 * 根据记录的Key，加载记录数据
	 * @param key
	 * @return
	 */
	protected abstract INFO loadItemByKey(KEY key);

	@Override
	protected INFO doRead() throws Exception {
		if (!keyIterator.hasNext()) {
			return null;
		}
		
		//并发时可能会造成多个线程抢同一个keyIterator.next()，造成上面保护逻辑失效；这里只能暂时先通过try-catch保护
		try{
			return loadItemByKey(keyIterator.next());
		}
		catch(NoSuchElementException ex){
			logger.warn("已经没有下一个key了，已经被其他线程取走");
			return null;
		}
		
//		synchronized (key){
//			key = keyIterator.next();
//		}
//		
//		return loadItemByKey(key);
	}

	@Override
	protected void doOpen() throws Exception {
		// allKeys应该在beforeStep里加载或直接从ExecutionContext读取

		keyIterator = allKeys.iterator();

	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);

		// 仅为打日志
		logger.info("已处理到游标的第{}项", getCurrentItemCount());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void jumpToItem(int itemIndex) throws Exception {
		// 在特殊情况下indexIndex会超过allKeys的大小， 参见
		// net.engining.pg.batch.sdk.test.laststop.LastStopTest
		if (itemIndex > allKeys.size()) {
			keyIterator = Collections.EMPTY_LIST.iterator();
		}
		else {
			keyIterator = allKeys.subList(itemIndex, allKeys.size()).iterator();
		}
	}

	@Override
	protected void doClose() throws Exception {
		// 清空缓存，以防万一
		keyIterator = null;
		allKeys = null;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		// 用于支持分区
		List<KEY> keys = loadKeys();

		// 取文件大小计算网格规模
		int total = keys.size();

		// 这里加1是为了避免出现因为整除而漏项的情况
		int partitionSize = total / gridSize + 1; 
		partitionSize = Math.max(partitionSize, minPartitionSize);
		partitionSize = Math.min(partitionSize, maxPartitionSize);

		logger.info("总记录数[{}]，网格数[{}]，分片大小[{}]", total, gridSize, partitionSize);

		// 开始分partition，注意最后一个partition不要漏行；为了排序，使用TreeMap
		Map<String, ExecutionContext> result = new TreeMap<String, ExecutionContext>(); 
		int rest = total;
		int i = 0;
		while (rest > 0) {
			ExecutionContext ec = new ExecutionContext();
			// 动态subList不可串行化
			ArrayList<KEY> subList = new ArrayList<KEY>(keys.subList(i * partitionSize, Math.min((i + 1) * partitionSize, total))); 

			PgKeyContext context = createNewKeyContext(subList);

			ec.putLong("keyContextId", context.getContextId());

			result.put(MessageFormat.format("part{0,number,000}", i), ec);
			rest -= partitionSize;
			i++;
		}
		logger.info("实际网格数量[{}]", result.size());

		return result;
	}

	@SuppressWarnings("unchecked")
	@BeforeStep
	void beforeStep(StepExecution stepExecution) {
		// 由于继承了
		// AbstractItemCountingItemStreamReader，doOpen里拿不到ExecutionContext，所以只能在这里处理
		// 这里的处理在事务之外
		ExecutionContext ec = stepExecution.getExecutionContext();
		if (ec.containsKey(KEY_CONTEXT_KEY)) {
			// 如果有Partitioner或断点续批
			long contextId = ec.getLong(KEY_CONTEXT_KEY);
			PgKeyContext context = em.find(PgKeyContext.class, contextId);
			allKeys = (List<KEY>) context.getKeyList();
			logger.info("加载已有的ContextId:{}，共{}条主键信息。", contextId, allKeys.size());
		}
		else {
			// 如果没有Partitioner，就在这里把所有主键加载，并且写入ExecutionContext
			// 但这个ExecutionContext会在第一次update时写入数据库，这个操作是在chunk的事务之外的
			allKeys = loadKeys();
			long contextId = createNewKeyContext(new ArrayList<KEY>(allKeys)).getContextId();
			ec.putLong(KEY_CONTEXT_KEY, contextId);
			logger.info("加载新建的ContextId:{}，共{}条主键信息。", contextId, allKeys.size());
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private PgKeyContext createNewKeyContext(ArrayList<KEY> subList) {
		PgKeyContext context = new PgKeyContext();
		context.setKeyList(subList);
		em.persist(context);
		return context;
	}

	@Override
	public void setBeanName(String name) {
		// 默认使用bean id作为name
		setName(name);
	}

	public int getMinPartitionSize() {
		return minPartitionSize;
	}

	public void setMinPartitionSize(int minPartitionSize) {
		this.minPartitionSize = minPartitionSize;
	}

	public int getMaxPartitionSize() {
		return maxPartitionSize;
	}

	public void setMaxPartitionSize(int maxPartitionSize) {
		this.maxPartitionSize = maxPartitionSize;
	}

}
