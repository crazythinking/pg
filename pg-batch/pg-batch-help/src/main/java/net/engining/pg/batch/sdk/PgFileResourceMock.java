package net.engining.pg.batch.sdk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import net.engining.pg.support.cstruct.CStruct;

/**
 * 
 * @author luxue
 *
 * @param <H>
 * @param <D>
 */
public class PgFileResourceMock<H, D> extends ResourceMock {
	protected Class<H> fileHeaderClass;

	private Class<D> fileDetailClass;

	/**
	 * 为临时文件准备数据，相当于 {@link #prepare(H, D...)}中第一个参数指定为null
	 * 
	 * @param details
	 *            文件明细对象，可以指定多个
	 */
	public void prepare(D... details) {
		prepare(null, details);
	}

	/**
	 * 为临时文件准备数据
	 * 
	 * @param header
	 *            文件头对象，可以为null
	 * @param details
	 *            文件明细对象，可以指定多个
	 */
	public void prepare(H header, D... details) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(getFile());
			FileChannel channel = fos.getChannel();

			// 处理文件头，如果有的话
			if (fileHeaderClass != null) {
				if (header == null) {
					throw new IllegalArgumentException("指定了headerClass就必须指定header对象");
				}

				CStruct<H> structHeader = new CStruct<H>(fileHeaderClass, "utf-8");
				ByteBuffer buffer = ByteBuffer.wrap(new byte[structHeader.getByteLength() + 1]);
				structHeader.writeByteBuffer(header, buffer);
				buffer.put((byte) '\n');
				buffer.flip();
				channel.write(buffer);
			}

			CStruct<D> structDetail = new CStruct<D>(fileDetailClass, "utf-8");
			ByteBuffer buffer = ByteBuffer.wrap(new byte[details.length * (structDetail.getByteLength() + 1)]);
			for (D detail : details) {
				structDetail.writeByteBuffer(detail, buffer);
				buffer.put((byte) '\n');
			}
			buffer.flip();
			channel.write(buffer);
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		finally {
			IOUtils.closeQuietly(fos);
		}
	}

	public List<D> parseDetails() {
		List<D> result = new ArrayList<D>();

		FileChannel channel = null;
		try {
			channel = new FileInputStream(getFile()).getChannel();

			if (fileHeaderClass != null) {
				// 跳过文件头
				channel.position(new CStruct<H>(fileHeaderClass).getByteLength() + 1);
			}
			CStruct<D> struct = new CStruct<D>(fileDetailClass, "utf-8");
			int lines = (int) ((channel.size() - channel.position()) / (struct.getByteLength() + 1));
			ByteBuffer buffer = ByteBuffer.wrap(new byte[struct.getByteLength() + 1]);
			for (int i = 0; i < lines; i++) {
				buffer.clear();
				channel.read(buffer);
				buffer.flip();
				result.add(struct.parseByteBuffer(buffer));
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		finally {
			IOUtils.closeQuietly(channel);
		}

		return result;
	}

	public Class<H> getFileHeaderClass() {
		return fileHeaderClass;
	}

	public void setFileHeaderClass(Class<H> fileHeaderClass) {
		this.fileHeaderClass = fileHeaderClass;
	}

	public Class<D> getFileDetailClass() {
		return fileDetailClass;
	}

	public void setFileDetailClass(Class<D> fileDetailClass) {
		this.fileDetailClass = fileDetailClass;
	}

}
