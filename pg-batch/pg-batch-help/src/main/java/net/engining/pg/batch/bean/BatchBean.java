package net.engining.pg.batch.bean;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.engining.pg.batch.enums.ActionRunnerOption;
import net.engining.pg.batch.enums.AdditionalRunnerOption;
import net.engining.pg.web.BaseRequestBean;


/**
 * @author luxue
 *
 */
public class BatchBean extends BaseRequestBean{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 连续运行次数
	 */
	@Min(value=1)
	int count;

	/**
	 * 启动参数项
	 */
	@NotNull
	ActionRunnerOption actionRunnerOptions;

	/**
	 * 启动附加参数项
	 * 
	 */
	@NotNull
	AdditionalRunnerOption additionalRunnerOption;

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the actionRunnerOptions
	 */
	public ActionRunnerOption getActionRunnerOptions() {
		return actionRunnerOptions;
	}

	/**
	 * @param actionRunnerOptions the actionRunnerOptions to set
	 */
	public void setActionRunnerOptions(ActionRunnerOption actionRunnerOptions) {
		this.actionRunnerOptions = actionRunnerOptions;
	}

	/**
	 * @return the additionalRunnerOption
	 */
	public AdditionalRunnerOption getAdditionalRunnerOption() {
		return additionalRunnerOption;
	}

	/**
	 * @param additionalRunnerOption the additionalRunnerOption to set
	 */
	public void setAdditionalRunnerOption(AdditionalRunnerOption additionalRunnerOption) {
		this.additionalRunnerOption = additionalRunnerOption;
	}
	
}
