package net.engining.pg.batch.sdk.test.suport;

public interface TestCase {
	
	/**
	 * 用于装配某个TestCase执行前的输入数据
	 */
	public void setupTestData();
	
	/**
	 * 单元测试逻辑
	 */
	public void testLogic();
	
	/**
	 * 单元测试完成后，关闭或清理相关资源
	 */
	public void testClose();
	
}
