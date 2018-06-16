package net.engining.pg.batch.sdk.file;

import java.io.BufferedReader;
import java.io.IOException;
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
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.batch.item.file.DefaultBufferedReaderFactory;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.NonTransientFlatFileException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;

import net.engining.pg.batch.sdk.LineItem;
import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.core.exception.ErrorMessageException;

/**
 * 扩展FlatFileItemReader，基于pg内部自定义格式的reader: 通过换行标识表示一条记录
 * 
 * @author luxue
 *
 */
public class PgFlatFileItemReader<H extends FlatFileHeader, D> extends AbstractItemCountingItemStreamItemReader<LineItem<D>>
		implements ResourceAwareItemReaderItemStream<LineItem<D>>, Partitioner, BeanNameAware, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(PgFlatFileItemReader.class);

	/**
	 * default encoding for input files
	 */
	public static final String DEFAULT_CHARSET = "utf-8";

	private RecordSeparatorPolicy recordSeparatorPolicy = new SimpleRecordSeparatorPolicy();

	private Resource resource;

	private BufferedReader reader;

	/**
	 * 跟踪当前处理的行号
	 */
	private int lineCount = 0;

	private String[] comments = new String[] { "#" };

	private boolean noInput = false;

	private String encoding = DEFAULT_CHARSET;

	private LineMapper<D> lineMapper;

	/**
	 * 首部跳过的行，通常都是Header对应的行数
	 */
	private int linesToSkip = 0;

	private LineCallbackHandler skippedLinesCallback;

	private boolean strict = true;

	private BufferedReaderFactory bufferedReaderFactory = new DefaultBufferedReaderFactory();

	protected Class<H> fileHeaderClass;

	protected Class<D> fileDetailClass;

	protected H header;

	/**
	 * 最小分片size
	 */
	private int minPartitionSize = 1000;

	/**
	 * 最大分片size
	 */
	private int maxPartitionSize = Integer.MAX_VALUE;

	@PostConstruct
	public void init() {
		logger.debug("init PgFlatFileItemReader");
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);

		// 仅为打日志
		logger.info("已处理到文件的第{}行 (自第{}行开始处理)", lineCount, linesToSkip);
	}

	/**
	 * @return string corresponding to logical record according to
	 *         {@link #setRecordSeparatorPolicy(RecordSeparatorPolicy)} (might
	 *         span multiple lines in file).
	 */
	@Override
	protected LineItem<D> doRead() throws Exception {
		LineItem<D> lineItem = new LineItem<D>();

		if (noInput) {
			return null;
		}

		String line = readLine();

		if (line == null) {
			return null;
		}
		else {
			try {
				D detail = lineMapper.mapLine(line, lineCount);
				lineItem.setLineNumber(lineCount++);
				lineItem.setLineObject(detail);
				lineItem.setValid(true);

			}
			catch (Exception ex) {
				throw new FlatFileParseException(
						"Parsing error at line: " + lineCount + " in resource=[" + resource.getDescription() + "], input=[" + line + "]", ex, line,
						lineCount);
			}
		}

		return lineItem;
	}

	/**
	 * @return next line (skip comments).getCurrentResource
	 */
	private String readLine() {

		if (this.reader == null) {
			throw new ReaderNotOpenException("Reader must be open before it can be read.");
		}

		String line = null;

		try {
			line = this.reader.readLine();
			if (line == null) {
				return null;
			}
			lineCount++;
			while (isComment(line)) {
				line = this.reader.readLine();
				if (line == null) {
					return null;
				}
				lineCount++;
			}

			line = applyRecordSeparatorPolicy(line);
		}
		catch (IOException e) {
			// Prevent IOException from recurring indefinitely
			// if client keeps catching and re-calling
			noInput = true;
			throw new NonTransientFlatFileException("Unable to read from resource: [" + resource + "]", e, line, lineCount);
		}
		return line;
	}

	private String applyRecordSeparatorPolicy(String line) throws IOException {

		String record = line;
		while (line != null && !recordSeparatorPolicy.isEndOfRecord(record)) {
			line = this.reader.readLine();
			if (line == null) {
				if (StringUtils.hasText(record)) {
					// A record was partially complete since it hasn't ended but
					// the line is null
					throw new FlatFileParseException("Unexpected end of file before record complete", record, lineCount);
				}
				else {
					// Record has no text but it might still be post processed
					// to something (skipping preProcess since that was already
					// done)
					break;
				}
			}
			else {
				lineCount++;
			}
			record = recordSeparatorPolicy.preProcess(record) + line;
		}

		return recordSeparatorPolicy.postProcess(record);

	}

	private boolean isComment(String line) {
		for (String prefix : comments) {
			if (line.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 留给子类验证自定义文件头的扩展点
	 * 
	 * @param header
	 * @return
	 */
	protected boolean validate(H header) {
		return true;
	}

	@Override
	protected void doClose() throws Exception {
		lineCount = 0;
		if (this.reader != null) {
			IOUtils.closeQuietly(this.reader);
			// reader.close();
		}
	}

	@Override
	protected void doOpen() throws Exception {
		Assert.notNull(resource, "Input resource must be set");
		Assert.notNull(recordSeparatorPolicy, "RecordSeparatorPolicy must be set");

		noInput = true;
		if (!resource.exists()) {
			if (strict) {
				throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode): " + resource);
			}
			logger.warn("Input resource does not exist " + resource.getDescription());
			return;
		}

		if (!resource.isReadable()) {
			if (strict) {
				throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): " + resource);
			}
			logger.warn("Input resource is not readable " + resource.getDescription());
			return;
		}

		this.reader = bufferedReaderFactory.create(resource, encoding);

		// 对Header定义的每行轮询处理
		for (int i = 0; i < linesToSkip; i++) {
			String line = readLine();
			if (fileHeaderClass != null) {
				// 对于json类型的文件，Header的定义应该只存在于首行; 因此只对首行进行处理
				if (i == 0 && lineMapper instanceof FastJsonObjectLineMapper) {
					header = JSON.parseObject(line, fileHeaderClass);
				}
				else {
					// TODO 对其他类型lineMapper的支持
				}
				if (!validate(header)) {
					throw new ErrorMessageException(ErrorCode.SystemError, "File validated with Header is failed");
				}
			}

			// 对Header定义的每行额外处理
			if (skippedLinesCallback != null) {
				skippedLinesCallback.handleLine(line);
			}
		}

		// 记录跳过linesToSkip后的行号
		lineCount = linesToSkip;

		noInput = false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(lineMapper, "LineMapper is required");

	}

	@Override
	protected void jumpToItem(int itemIndex) throws Exception {

		logger.info("断点续批File Reader，跳转到第{}行。", itemIndex + linesToSkip);
		for (int i = 0; i < itemIndex; i++) {
			readLine();
		}
		// 记录跳过linesToSkip后的行号
		lineCount = itemIndex + linesToSkip;
	}

	@Override
	public void setBeanName(String name) {
		// 默认使用bean id作为name
		setName(name);
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		try {

			if (this.reader == null) {
				this.doOpen();
			}

			// 计算符合recordSeparatorPolicy的总行数
			int totalLines = 0;
			String line = "";
			while ((line = this.readLine()) != null) {
				totalLines++;
			}

			// 取文件大小计算网格规模; 这里加1是为了避免出现因为整除而产生非常小的片的情况
			int partitionSize = totalLines / gridSize + 1; 
			// 最小分片大小
			partitionSize = Math.max(partitionSize, minPartitionSize); 
			// 最大分片大小
			partitionSize = Math.min(partitionSize, maxPartitionSize); 

			logger.info("文件总记录数[{}]，网格数[{}]，网格大小[{}]", totalLines, gridSize, partitionSize);

			// 开始分partition，注意最后一个partition不要漏行
			// 为了排序，使用TreeMap
			Map<String, ExecutionContext> result = new TreeMap<String, ExecutionContext>(); 

			// 这里实际上已经不参考 gridSize，而是根据partitionSize来计算
			for (int i = 0, restLines = totalLines; restLines > 0; i++, restLines -= partitionSize) {
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
			throw new ErrorMessageException(ErrorCode.SystemError, e.getMessage());
		}
		finally {
			lineCount = 0;
			if (this.reader != null) {
				IOUtils.closeQuietly(this.reader);
			}
		}
	}

	@BeforeStep
	void beforeStep(StepExecution stepExecution) {
		ExecutionContext ec = stepExecution.getExecutionContext();
		if (ec.containsKey("skip")) {
			linesToSkip = ec.getInt("skip");
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

	public void setFileHeaderClass(Class<H> fileHeaderClass) {
		this.fileHeaderClass = fileHeaderClass;
	}

	public void setFileDetailClass(Class<D> fileDetailClass) {
		this.fileDetailClass = fileDetailClass;
	}

	/**
	 * In strict mode the reader will throw an exception on
	 * {@link #open(org.springframework.batch.item.ExecutionContext)} if the
	 * input resource does not exist.
	 * 
	 * @param strict
	 *            <code>true</code> by default
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * @param skippedLinesCallback
	 *            will be called for each one of the initial skipped lines
	 *            before any items are read.
	 */
	public void setSkippedLinesCallback(LineCallbackHandler skippedLinesCallback) {
		this.skippedLinesCallback = skippedLinesCallback;
	}

	/**
	 * Public setter for the number of lines to skip at the start of a file. Can
	 * be used if the file contains a header without useful (column name)
	 * information, and without a comment delimiter at the beginning of the
	 * lines.
	 * 
	 * @param linesToSkip
	 *            the number of lines to skip
	 */
	public void setLinesToSkip(int linesToSkip) {
		this.linesToSkip = linesToSkip;
	}

	/**
	 * Setter for line mapper. This property is required to be set.
	 * 
	 * @param lineMapper
	 *            maps line to item
	 */
	public void setLineMapper(LineMapper<D> lineMapper) {
		this.lineMapper = lineMapper;
	}

	/**
	 * Setter for the encoding for this input source. Default value is
	 * {@link #DEFAULT_CHARSET}.
	 * 
	 * @param encoding
	 *            a properties object which possibly contains the encoding for
	 *            this input file;
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Factory for the {@link BufferedReader} that will be used to extract lines
	 * from the file. The default is fine for plain text files, but this is a
	 * useful strategy for binary files where the standard BufferedReaader from
	 * java.io is limiting.
	 * 
	 * @param bufferedReaderFactory
	 *            the bufferedReaderFactory to set
	 */
	public void setBufferedReaderFactory(BufferedReaderFactory bufferedReaderFactory) {
		this.bufferedReaderFactory = bufferedReaderFactory;
	}

	/**
	 * Setter for comment prefixes. Can be used to ignore header lines as well
	 * by using e.g. the first couple of column names as a prefix.
	 * 
	 * @param comments
	 *            an array of comment line prefixes.
	 */
	public void setComments(String[] comments) {
		this.comments = new String[comments.length];
		System.arraycopy(comments, 0, this.comments, 0, comments.length);
	}

	/**
	 * Public setter for the input resource.
	 */
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Public setter for the recordSeparatorPolicy. Used to determine where the
	 * line endings are and do things like continue over a line ending if inside
	 * a quoted string.
	 * 
	 * @param recordSeparatorPolicy
	 *            the recordSeparatorPolicy to set
	 */
	public void setRecordSeparatorPolicy(RecordSeparatorPolicy recordSeparatorPolicy) {
		this.recordSeparatorPolicy = recordSeparatorPolicy;
	}
}
