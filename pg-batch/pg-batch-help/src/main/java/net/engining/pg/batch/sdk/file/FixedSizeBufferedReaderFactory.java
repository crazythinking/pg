package net.engining.pg.batch.sdk.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.core.io.Resource;

/**
 * 固定缓冲区大小的BufferedReaderFactory
 * @author luxue
 *
 */
public class FixedSizeBufferedReaderFactory implements BufferedReaderFactory {
	
	//5M缓冲区
	private int bufferSize = 10 * 1024 * 1024;

	@Override
	public BufferedReader create(Resource resource, String encoding) throws UnsupportedEncodingException, IOException {
		return new BufferedReader(new InputStreamReader(resource.getInputStream(), encoding), this.bufferSize);
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

}
