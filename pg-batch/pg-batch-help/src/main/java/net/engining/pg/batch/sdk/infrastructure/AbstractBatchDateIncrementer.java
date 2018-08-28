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
	
	public abstract JobParameters setupJobParameters();

	@Override
	public JobParameters getNext(JobParameters parameters) {

		Preconditions.checkNotNull(batchSeqPrefix, "Batch seqence prefix cannot be null");
		JobParameters jobParam = setupJobParameters();

		log.info("当前系统时间{}, Batch Job Parameters: batchSeq={}", new Date(), jobParam.getString(BatchJobParameterKeys.BatchSeq));
		return jobParam;
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
