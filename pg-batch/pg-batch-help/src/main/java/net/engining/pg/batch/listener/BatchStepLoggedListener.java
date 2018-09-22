package net.engining.pg.batch.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import net.engining.pg.support.core.exception.ErrorCode;
import net.engining.pg.support.utils.ExceptionUtilsExt;

public class BatchStepLoggedListener implements StepExecutionListener {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		logger.info(stepExecution.getStepName() + " 开始执行," + dateFormat.format(new Date()));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info(stepExecution.getStepName() + " 执行结束" + dateFormat.format(new Date()));
		}
		else {
			logger.error("TRADE_TYPE:[{}], 执行出错(TRADE_STATUS:F), AT {}", stepExecution.getStepName(), dateFormat.format(new Date()));
			logger.error("ERROR_CODE:{}, have exceptions as flowing: ",ErrorCode.SystemError.getValue());
			for(Throwable t : stepExecution.getFailureExceptions()){
				ExceptionUtilsExt.dump(t);
			}
		}
		
		return stepExecution.getExitStatus();
	}
}
