package net.engining.pg.batch.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import net.engining.pg.batch.sdk.infrastructure.BatchJobParameterKeys;

public class BatchJobLoggedListener implements JobExecutionListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	@Override
	public void beforeJob(JobExecution jobExecution) {
		logger.info("{} 的批量开始执行, 于{}", jobExecution.getJobParameters().getString(BatchJobParameterKeys.BatchSeq), dateFormat.format(new Date()));
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info("{} 的批量执行结束, 于{}", jobExecution.getJobParameters().getDate(BatchJobParameterKeys.BatchSeq), "yyyy/MM/dd", dateFormat.format(new Date()));
		} else {
			logger.info("{} 的批量未成功执行结束, 批量状态={}, 于{}",
					jobExecution.getJobParameters().getDate(BatchJobParameterKeys.BatchSeq),
					jobExecution.getStatus(), dateFormat.format(new Date()));
		}
	}
}
