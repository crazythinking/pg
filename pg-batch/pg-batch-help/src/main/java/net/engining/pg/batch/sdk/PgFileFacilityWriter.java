package net.engining.pg.batch.sdk;

import java.nio.ByteBuffer;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Required;

import net.engining.pg.batch.entity.model.PgFileItem;
import net.engining.pg.support.cstruct.CStruct;

/**
 * 
 * @author luxue
 *
 * @param <T>
 */
public class PgFileFacilityWriter<T> implements ItemWriter<T>
{
	@PersistenceContext
	private EntityManager em;
	
	private String filename;
	
	private CStruct<T> detailStruct;
	
	private ByteBuffer detailBuffer;
	
	private Class<T> fileDetailClass;

	/**
	 * 转换目标代码
	 */
	private String charset = "utf-8";	
	
	private String batchNumber;

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T item : items)
		{
			PgFileItem entity = new PgFileItem();
			entity.setBatchNumber(batchNumber);
			entity.setFilename(filename);
			
			detailBuffer.clear();
			detailStruct.writeByteBuffer(item, detailBuffer);
			
			entity.setLine(new String(detailBuffer.array(), charset));

			em.persist(entity);
		}
	}
	
	@PostConstruct
	public void init()
	{
		detailStruct = new CStruct<T>(fileDetailClass, charset);
		detailBuffer = ByteBuffer.allocate(detailStruct.getByteLength());
	}

	public String getFilename() {
		return filename;
	}

	@Required
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Class<T> getFileDetailClass() {
		return fileDetailClass;
	}
	@Required
	public void setFileDetailClass(Class<T> fileDetailClass) {
		this.fileDetailClass = fileDetailClass;
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
}
