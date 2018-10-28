package net.engining.pg.batch.sdk;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Required;

/**
 * 与 {@link PgBatchTransactionManager}配合，在每个批量步骤后关闭EntityManager。
 * 由于Spring Batch会在afterStep之后再调用close，所以这个过程要放在beforeStep中。
 * @author licj
 *
 */
@Deprecated
public class PgBatchStepListener implements StepExecutionListener {
	
	private PgBatchTransactionManager transactionManager;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		transactionManager.closeCurrentEntityManager();
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		//不修改批量结果
		return stepExecution.getExitStatus();	
	}

	@Required
	public void setTransactionManager(PgBatchTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

}
