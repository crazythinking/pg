package net.engining.pg.batch.sdk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import net.engining.pg.support.cstruct.CStruct;

/**
 * 用于批量任务的文件导出处理
 * @author luxue
 *
 */
public class PgFileFacilityExporter implements Tasklet {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private String[] filenames;
	
	@PersistenceContext
	private EntityManager em;
	
	private int chunkSize = 10000;
	
	private String charset = "utf-8";
	
	private String batchNumber;
	
	private String outputDir;

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		for (String filename : filenames)
		{
			String outputName = outputDir + "/" + filename;
			File finalFile = new File(outputName);
			File tempFile = new File(outputName + ".pg");
			OutputStream os = null;

			logger.info("输出文件到[{}]", outputName);

			try
			{
				// 4M
				os = new BufferedOutputStream(new FileOutputStream(tempFile), 4 * 1024 * 1024);
				
				writeFileHeader(filename, os);
				
				//这里就直接用JPQL了
				Query query = em.createQuery("select t.line from PgFileItem t where t.batchNumber = :batchNumber and t.filename = :filename order by t.itemId");
				
				query.setParameter("batchNumber", batchNumber);
				query.setParameter("filename", filename);
				
				int start = 0;
				List<String> result;
				do
				{
					logger.info("处理第{}条开始的数据", start);
	
					query.setFirstResult(start);
					query.setMaxResults(chunkSize);
					result = query.getResultList();
					
					for (String line : result)
					{
						os.write(line.getBytes(charset));
						os.write('\n');
					}
					os.flush();
					start += result.size();
				}while(!result.isEmpty());
				
				os.close();
				
				//完成后改名
				//成功处理，改文件名为最终文件
				if (finalFile.exists())
				{
					logger.warn("输出文件[{}]已存在，将被删除。", finalFile);
					finalFile.delete();
				}
				tempFile.renameTo(finalFile);
			}
			finally
			{
				IOUtils.closeQuietly(os);
			}
		}
		return RepeatStatus.FINISHED;
	}
	
	/**
	 * 可供子类重写，自定义文件头
	 * @throws IOException 
	 */
	protected void writeFileHeader(String filename, OutputStream os) throws IOException
	{
		CStruct<FileHeader> cfh = new CStruct<FileHeader>(FileHeader.class);
		ByteBuffer buffer = ByteBuffer.allocate(cfh.getByteLength());
		FileHeader fh = new FileHeader();
		fh.filename = filename;
		cfh.writeByteBuffer(fh, buffer);
		os.write(buffer.array());
		os.write('\n');
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String[] getFilenames() {
		return filenames;
	}

	@Required
	public void setFilenames(String[] filenames) {
		this.filenames = filenames;
	}

}
