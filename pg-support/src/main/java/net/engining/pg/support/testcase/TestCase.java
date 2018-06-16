package net.engining.pg.support.testcase;

/**
 * 测试案例接口, 定义一个测试案例的生命周期；<br>
 * 单元测试案例，基于每个独立的项目包；
 * @author luxue
 *
 */
public interface TestCase {
	
	/**
	 * 每个测试case用于init自己本身的测试数据
	 */
	public void initTestData() throws Exception;
	
	/**
	 * 每个测试case用于断言结果
	 */
	public void assertResult() throws Exception;
	
	/**
	 * 测试案例的具体逻辑; <br>
	 * 由具体的测试案例实现，该逻辑应该是一个完整的测试案例流程，从测试输入的数据到产生预期的结果；
	 */
	public void testProcess() throws Exception;
	
	/**
	 * 测试案例结束后的操作，通常用于清理资源
	 */
	public void end() throws Exception;
	
}
