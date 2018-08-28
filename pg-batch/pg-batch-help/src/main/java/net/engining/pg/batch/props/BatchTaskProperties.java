package net.engining.pg.batch.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 批量任务参数配置
 * @author luxue
 *
 */
@ConfigurationProperties(prefix = "pg.batch")
public class BatchTaskProperties {
	
	/**
	 * 批量任务分片默认网格Size
	 */
	private int defaultGridSize;
	
	/**
	 * 分布式批量下，Master获取Slave的心跳间隔毫秒数
	 */
	private long defaultPollMills;
	
	/**
	 * 默认批量任务分片执行的线程池core pool size
	 */
	private int defaultExecutorSize;
	
	/**
	 * 默认批量输出文件目录
	 */
	private String defaultBatchOutputDir;
	
	/**
	 * 默认批量输入文件目录
	 */
	private String defaultBatchInputDir;
	
	/**
	 * 默认批量输入备份文件目录
	 */
	private String defaultBackupInputDir;
	
	/**
	 * 是否使用远程分片策略
	 */
	private boolean enableRemotePartition = false;
	
	/**
	 * 默认事物提交间隔数量，每n条item批量框架触发一次事物提交
	 */
	private int defaultCommitInterval = 500;

	/**
	 * @return the defaultGridSize
	 */
	public int getDefaultGridSize() {
		return defaultGridSize;
	}

	/**
	 * @param defaultGridSize the defaultGridSize to set
	 */
	public void setDefaultGridSize(int defaultGridSize) {
		this.defaultGridSize = defaultGridSize;
	}

	/**
	 * @return the defaultPollMills
	 */
	public long getDefaultPollMills() {
		return defaultPollMills;
	}

	/**
	 * @param defaultPollMills the defaultPollMills to set
	 */
	public void setDefaultPollMills(long defaultPollMills) {
		this.defaultPollMills = defaultPollMills;
	}

	/**
	 * @return the defaultExecutorSize
	 */
	public int getDefaultExecutorSize() {
		return defaultExecutorSize;
	}

	/**
	 * @param defaultExecutorSize the defaultExecutorSize to set
	 */
	public void setDefaultExecutorSize(int defaultExecutorSize) {
		this.defaultExecutorSize = defaultExecutorSize;
	}

	/**
	 * @return the defaultBatchOutputDir
	 */
	public String getDefaultBatchOutputDir() {
		return defaultBatchOutputDir;
	}

	/**
	 * @param defaultBatchOutputDir the defaultBatchOutputDir to set
	 */
	public void setDefaultBatchOutputDir(String defaultBatchOutputDir) {
		this.defaultBatchOutputDir = defaultBatchOutputDir;
	}

	/**
	 * @return the defaultBatchInputDir
	 */
	public String getDefaultBatchInputDir() {
		return defaultBatchInputDir;
	}

	/**
	 * @param defaultBatchInputDir the defaultBatchInputDir to set
	 */
	public void setDefaultBatchInputDir(String defaultBatchInputDir) {
		this.defaultBatchInputDir = defaultBatchInputDir;
	}

	/**
	 * @return the defaultBackupInputDir
	 */
	public String getDefaultBackupInputDir() {
		return defaultBackupInputDir;
	}

	/**
	 * @param defaultBackupInputDir the defaultBackupInputDir to set
	 */
	public void setDefaultBackupInputDir(String defaultBackupInputDir) {
		this.defaultBackupInputDir = defaultBackupInputDir;
	}

	/**
	 * @return the enableRemotePartition
	 */
	public boolean isEnableRemotePartition() {
		return enableRemotePartition;
	}

	/**
	 * @param enableRemotePartition the enableRemotePartition to set
	 */
	public void setEnableRemotePartition(boolean enableRemotePartition) {
		this.enableRemotePartition = enableRemotePartition;
	}

	/**
	 * @return the defaultCommitInterval
	 */
	public int getDefaultCommitInterval() {
		return defaultCommitInterval;
	}

	/**
	 * @param defaultCommitInterval the defaultCommitInterval to set
	 */
	public void setDefaultCommitInterval(int defaultCommitInterval) {
		this.defaultCommitInterval = defaultCommitInterval;
	}
	
}
