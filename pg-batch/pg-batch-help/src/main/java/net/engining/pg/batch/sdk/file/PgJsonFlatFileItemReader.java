package net.engining.pg.batch.sdk.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.batch.item.file.DefaultBufferedReaderFactory;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.NonTransientFlatFileException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.separator.JsonRecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;

import net.engining.pg.batch.sdk.LineItem;

/**
 * 基于pg内部自定义格式的reader: 
 * Json+换行, 首行可定义为JsonFileHeader或其子类；
 * 
 * @author luxue
 * 
 * @param <H>
 * @param <D>
 *
 */
public class PgJsonFlatFileItemReader<H extends FlatFileHeader, D>
		extends AbstractItemCountingItemStreamItemReader<LineItem<D>>
		implements ResourceAwareItemReaderItemStream<LineItem<D>>, Partitioner, BeanNameAware {

	private static final Logger logger = LoggerFactory.getLogger(PgJsonFlatFileItemReader.class);

	/**
	 * default encoding for input files
	 */
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

	private RecordSeparatorPolicy recordSeparatorPolicy = new JsonRecordSeparatorPolicy();

	private Resource resource;

	private BufferedReader reader;

	/**
	 * 跟踪当前处理的行号
	 */
	private int lineCount = 0;

	private String[] comments = new String[] { "#" };

	private boolean noInput = false;

	private String encoding = DEFAULT_CHARSET;

	/**
	 * 跳过Header所包含的Line数; 可以没有；
	 * Json类型的交互文件应该只有第一行为Header;
	 */
	private int linesToSkip = 0;

	private JsonFileHeaderLineCallbackHandler<H> skippedLinesCallback;

	private boolean strict = true;

	private BufferedReaderFactory bufferedReaderFactory = new DefaultBufferedReaderFactory();
	
	protected Class<H> fileHeaderClass;

	protected Class<D> fileDetailClass;

	protected String newline = "\n";
	protected byte[] newlineBuff;

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
		skippedLinesCallback = new JsonFileHeaderLineCallbackHandler<H>();
	}

	@Override
	public void setBeanName(String name) {
		// 默认使用bean id作为name
		setName(name);
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		// TODO Auto-generated method stub
		return null;
	}

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
				//line json string parse to object
				D detail = JSON.parseObject(line, fileDetailClass);
				lineItem.setLineNumber(lineCount++);
				lineItem.setLineObject(detail);
				lineItem.setValid(true);
			}
			catch (Exception ex) {
				throw new FlatFileParseException("Parsing error at line: " + lineCount + " in resource=["
						+ resource.getDescription() + "], input=[" + line + "]", ex, line, lineCount);
			}
		}
		
		return lineItem;
	}
	
	private String readLine() {

		if (reader == null) {
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
				line = reader.readLine();
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
			throw new NonTransientFlatFileException("Unable to read from resource: [" + resource + "]", e, line,
					lineCount);
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
				throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): "
						+ resource);
			}
			logger.warn("Input resource is not readable " + resource.getDescription());
			return;
		}

		reader = bufferedReaderFactory.create(resource, encoding);
		
		//读取Header，如果有Header也只处理第一行
		if (linesToSkip > 0) {
			logger.info("跳过文件Header，前{}项", linesToSkip);
			String line = readLine();
			if (skippedLinesCallback != null) {
				skippedLinesCallback.handleLine(line);
				header = skippedLinesCallback.getHeader();
			}
		}
		
		//记录跳过linesToSkip后的行号
		lineCount = linesToSkip;
		
		noInput = false;
	}

	@Override
	protected void doClose() throws Exception {
		lineCount = 0;
		if (reader != null) {
			reader.close();
		}
	}

	/**
	 * In strict mode the reader will throw an exception on
	 * {@link #open(org.springframework.batch.item.ExecutionContext)} if the input resource does not exist.
	 * @param strict <code>true</code> by default
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * Public setter for the number of lines to skip at the start of a file. Can be used if the file contains a header
	 * without useful (column name) information, and without a comment delimiter at the beginning of the lines.
	 * 
	 * @param linesToSkip the number of lines to skip
	 */
	public void setLinesToSkip(int linesToSkip) {
		this.linesToSkip = linesToSkip;
	}

	/**
	 * Setter for the encoding for this input source. Default value is {@link #DEFAULT_CHARSET}.
	 * 
	 * @param encoding a properties object which possibly contains the encoding for this input file;
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Factory for the {@link BufferedReader} that will be used to extract lines from the file. The default is fine for
	 * plain text files, but this is a useful strategy for binary files where the standard BufferedReaader from java.io
	 * is limiting.
	 * 
	 * @param bufferedReaderFactory the bufferedReaderFactory to set
	 */
	public void setBufferedReaderFactory(BufferedReaderFactory bufferedReaderFactory) {
		this.bufferedReaderFactory = bufferedReaderFactory;
	}

	/**
	 * Setter for comment prefixes. Can be used to ignore header lines as well by using e.g. the first couple of column
	 * names as a prefix.
	 * 
	 * @param comments an array of comment line prefixes.
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
	 * Public setter for the recordSeparatorPolicy. Used to determine where the line endings are and do things like
	 * continue over a line ending if inside a quoted string.
	 * 
	 * @param recordSeparatorPolicy the recordSeparatorPolicy to set
	 */
	public void setRecordSeparatorPolicy(RecordSeparatorPolicy recordSeparatorPolicy) {
		this.recordSeparatorPolicy = recordSeparatorPolicy;
	}
}
