package net.engining.pg.batch.sdk;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;

import net.engining.pg.support.cstruct.CStruct;

/**
 * 基于pg内部自定义格式的reader:定长+换行
 * 
 * @author licj
 *
 * @param <H>
 * @param <D>
 */
public class PgFileItemReader<H extends FileHeader, D> extends AbstractItemCountingItemStreamItemReader<LineItem<D>>
		implements ResourceAwareItemReaderItemStream<LineItem<D>>, Partitioner, BeanNameAware {

	private Logger logger = LoggerFactory.getLogger(getClass());

	protected Resource resource;

	/**
	 * 可以为空，表明没有文件头
	 */
	protected Class<H> fileHeaderClass;

	protected Class<D> fileDetailClass;

	/**
	 * 使用CStruct解析时的编码
	 */
	protected String charset = "utf-8";

	protected String newline = "\n";
	protected byte[] newlineBuff;

	protected FileChannel fileChannel;
	protected FileInputStream fileInputStream;

	protected H header;

	protected CStruct<D> detailStruct;
	protected CStruct<H> headerStruct;

	protected int detailBytesLength;
	protected int headerBytesLength;

	private ByteBuffer detailBuffer;

	private int minPartitionSize = 1000;

	private int maxPartitionSize = Integer.MAX_VALUE;

	/**
	 * 跟踪当前处理的行号，仅用于显示，且首行数据为0行
	 */
	private int currentLineNo;

	private int skip = 0;

	@Override
	protected LineItem<D> doRead() throws Exception {
		LineItem<D> lineItem = new LineItem<D>();

		detailBuffer.clear();

		int read = fileChannel.read(detailBuffer);
		if (read == -1) {
			return null;
		} // 文件读完
		if (read != detailBytesLength) {
			logger.warn("文件最后一行不完整，忽略。");
			return null;
		}

		detailBuffer.flip();
		D detail = detailStruct.parseByteBuffer(detailBuffer);

		// 验证换行
		detailBuffer.get(newlineBuff);
		if (!newline.equals(new String(newlineBuff))) {
			throw new IllegalArgumentException("换行符不正确:" + resource);
		}

		lineItem.setLineNumber(currentLineNo++);
		lineItem.setLineObject(detail);
		lineItem.setValid(true);
		return lineItem;
	}

	/**
	 * 留给子类验证自定义文件头的扩展点
	 * 
	 * @param header
	 * @return
	 */
	protected boolean validate(H header) {
		// if (header.detailSize != detailStruct.getByteLength() + 1) {
		// logger.error("文件头中记录大小与数据结构不同{}， 期待[{}]，实际[{}]。", new Object[] {
		// resource.getDescription(), detailBytesLength, header.detailSize });
		// // throw new IllegalArgumentException(MessageFormat.format(
		// // "文件头中记录大小与数据结构不同{0}， 期待[{1}]，实际[{2}]。", resource.getURI(),
		// // detailStruct.getByteLength() + 1, header.detailSize));// TODO 改规范
		// return false;
		// } else {
		// return true;
		// }
		return true;
	}

	@Override
	protected void doOpen() throws Exception {
		fileInputStream = new FileInputStream(resource.getFile());
		fileChannel = fileInputStream.getChannel();

		// 读文件头
		if (headerStruct != null) {

			ByteBuffer buffer = ByteBuffer.allocate(headerBytesLength);

			if (fileChannel.read(buffer) != headerBytesLength) {
				throw new IllegalArgumentException("没有正确的文件头:" + resource.getDescription());
			} // TODO 改规范

			buffer.flip();
			header = headerStruct.parseByteBuffer(buffer);

			buffer.get(newlineBuff);
			if (!newline.equals(new String(newlineBuff))) {
				throw new IllegalArgumentException("文件头没有换行:" + resource.getDescription());
			} // TODO 改规范

			if (!validate(header)) {
				throw new IllegalArgumentException("文件正确性验证失败");
			}
		}

		if (skip > 0) {
			logger.info("跳过前{}项", skip);
			// 防超过2G文件
			fileChannel.position(headerBytesLength + (long) detailBytesLength * skip); 
		}

		currentLineNo = skip;
	}

	@Override
	protected void jumpToItem(int itemIndex) throws Exception {

		// 防超过2G文件
		long position = headerBytesLength + (long) detailBytesLength * (itemIndex + skip); 

		logger.info("断点续批，跳转到第{}字节，第{}项。", position, itemIndex + skip);

		// 直接跳过字节
		fileChannel.position(position);

		currentLineNo = itemIndex + skip;
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);

		// 仅为打日志
		logger.info("已处理到文件的第{}项(自第{}项开始处理)", currentLineNo, skip);
	}

	@Override
	protected void doClose() throws Exception {
		IOUtils.closeQuietly(fileInputStream);
	}

	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@PostConstruct
	public void init() {
		detailStruct = new CStruct<D>(fileDetailClass, charset);
		if (fileHeaderClass != null) {
			headerStruct = new CStruct<H>(fileHeaderClass, charset);
			headerBytesLength = headerStruct.getByteLength() + newline.length();
		}
		detailBytesLength = detailStruct.getByteLength() + newline.length();
		detailBuffer = ByteBuffer.allocate(detailBytesLength);
		newlineBuff = new byte[newline.length()];
	}

	public void setFileHeaderClass(Class<H> fileHeaderClass) {
		this.fileHeaderClass = fileHeaderClass;
	}

	public void setFileDetailClass(Class<D> fileDetailClass) {
		this.fileDetailClass = fileDetailClass;
	}

	public void setNewline(String newline) {
		this.newline = newline;
	}

	@Override
	public void setBeanName(String name) {
		// 默认使用bean id作为name
		setName(name);
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {

		// 用于支持分区

		FileInputStream fis = null;
		try {
			// 取文件大小计算网格规模
			fis = new FileInputStream(resource.getFile());
			FileChannel channel = fis.getChannel();
			long size = channel.size();
			int total = (int) ((size - headerBytesLength) / detailBytesLength);
			// 这里加1是为了避免出现因为整除而产生非常小的片的情况
			int partitionSize = total / gridSize + 1; 
			// 最小分片大小
			partitionSize = Math.max(partitionSize, minPartitionSize); 
			// 最大分片大小
			partitionSize = Math.min(partitionSize, maxPartitionSize); 

			logger.info("文件大小[{}]字节，总记录数[{}]，网格数[{}]，网格大小[{}]", size, total, gridSize, partitionSize);

			// 开始分partition，注意最后一个partition不要漏行; 为了排序，使用TreeMap
			Map<String, ExecutionContext> result = new TreeMap<String, ExecutionContext>(); 

			// 这里实际上已经不参考 gridSize，而是根据partitionSize来计算
			for (int i = 0, restLines = total; restLines > 0; i++, restLines -= partitionSize) {
				String partName = MessageFormat.format("part{0,number,000}", i);
				ExecutionContext ec = new ExecutionContext();
				ec.putInt("skip", i * partitionSize);
				ec.putInt("limit", partitionSize);
				ec.putString("name", partName);
				result.put(partName, ec);
			}
			logger.info("实际网格数量[{}]", result.size());

			return result;
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		finally {
			IOUtils.closeQuietly(fis);
		}
	}

	@BeforeStep
	void beforeStep(StepExecution stepExecution) {
		ExecutionContext ec = stepExecution.getExecutionContext();
		if (ec.containsKey("skip")) {
			skip = ec.getInt("skip");
		}
		if (ec.containsKey("limit")) {
			setMaxItemCount(ec.getInt("limit"));
		}
	}

	public int getMinPartitionSize() {
		return minPartitionSize;
	}

	/**
	 * 最小分区大小，避免因为数据集太小而分了过多的区，默认值为1000
	 * 
	 * @param minPartitionSize
	 */
	public void setMinPartitionSize(int minPartitionSize) {
		this.minPartitionSize = minPartitionSize;
	}

	public int getMaxPartitionSize() {
		return maxPartitionSize;
	}

	/**
	 * 最大分区大小，与线程池配合使用，避免分片过大导致单个步骤时间过长，在产生断批时浪费时间的问题。 默认值为
	 * Integer.MAX_VALUE，即不限制
	 * 
	 * @param maxPartitionSize
	 */
	public void setMaxPartitionSize(int maxPartitionSize) {
		this.maxPartitionSize = maxPartitionSize;
	}
}
