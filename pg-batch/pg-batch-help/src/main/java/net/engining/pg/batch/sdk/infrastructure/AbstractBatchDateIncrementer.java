package net.engining.pg.batch.sdk.infrastructure;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;

import com.google.common.base.Preconditions;

public abstract class AbstractBatchDateIncrementer implements JobParametersIncrementer {

	private static final Logger log = LoggerFactory.getLogger(AbstractBatchDateIncrementer.class);
	
	private String batchSeqPrefix = null;
	
	public abstract JobParameters setupJobParameters(JobParameters parameters);

	@Override
	public JobParameters getNext(JobParameters parameters) {

		Preconditions.checkNotNull(batchSeqPrefix, "Batch seqence prefix cannot be null");
		parameters = setupJobParameters(parameters);

		log.info("当前系统时间{}, Batch Job Parameters: batchSeq={}", new Date(), parameters.getString(BatchJobParameterKeys.BatchSeq));
		return parameters;
	}

	/**
	 * @return the batchSeqPrefix
	 */
	public String getBatchSeqPrefix() {
		return batchSeqPrefix;
	}

	/**
	 * @param batchSeqPrefix the batchSeqPrefix to set
	 */
	public void setBatchSeqPrefix(String batchSeqPrefix) {
		this.batchSeqPrefix = batchSeqPrefix;
	}
	
}
