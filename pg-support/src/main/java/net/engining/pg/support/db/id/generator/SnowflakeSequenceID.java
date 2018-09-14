package net.engining.pg.support.db.id.generator;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engining.pg.support.utils.LocalMachineUtils;
import net.engining.pg.support.utils.SystemClockUtil;

/**
 * 分布式高效、有序ID生成
 * 
 * <pre>
 * 移植tweeter的snowflake:
 * (a) id构成: 42位的时间前缀 + 10位的节点标识 + 12位的sequence避免并发的数字(12位不够用时强制得到新的时间前缀)
 * 注意这里进行了小改动: snowkflake是5位的datacenter加5位的机器id; 这里变成使用10位的机器id
 * (b) 对系统时间的依赖性非常强，需关闭ntp的时间同步功能。当检测到ntp时间调整后，将会拒绝分配id
 *
 * 毫秒级时间41位+机器ID 10位+毫秒内序列12位。
 * 0 41 51 64 +-----------+------+------+ |time |pc |inc | +-----------+------+------+
 * 前41bits是以微秒为单位的timestamp,接着10bits是事先配置好的机器ID,最后12bits是累加计数器。
 * macheine id(10bits)标明最多只能有1024台机器同时产生ID，sequence number(12bits)也标明1台机器1ms中最多产生4096个ID， *
 * 注意点，因为使用到位移运算，所以需要64位操作系统，不然生成的ID会有可能不正确
 * </pre>
 */
public class SnowflakeSequenceID {

	private static final Logger logger = LoggerFactory.getLogger(SnowflakeSequenceID.class);

	private long workerId;
	private long datacenterId;

	// 0，并发控制
	private long sequence = 0L;

	// 时间起始标记点，作为基准，一般取系统的最近时间
	private long twepoch = 1514736000000L;

	// 机器标识位数
	private long workerIdBits = 10L;
	// 机器ID最大值: 1023
	private long maxWorkerId = -1L ^ (-1L << workerIdBits);

	// 数据中心标识位数
	private long datacenterIdBits = 10L;
	// 数据中心ID最大值: 1023
	private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

	// 毫秒内自增位; 序列在id中占的位数
	private long sequenceBits = 12L;

	// 机器ID偏左移12位
	private long workerIdShift = sequenceBits; // 12
	// 数据中心ID左移17位
	private long datacenterIdShift = sequenceBits + workerIdBits;
	// 时间毫秒左移22位
	private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
	// 序列号ID最大值
	private long sequenceMax = -1L ^ (-1L << sequenceBits);

	private long lastTimestamp = -1L;

	/**
	 * 构造函数.
	 *
	 * @param workerId
	 *            机器ID
	 * @param datacenterId
	 *            数据中心ID
	 */
	public SnowflakeSequenceID(long workerId, long datacenterId) {
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format("机器标示不能大于 %s 或者小于 0", maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(String.format("数据中心标示不能大于 %s 或者小于 0", maxDatacenterId));
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		logger.debug(
				"worker starting. timestamp left shift {}, datacenter id bits {}, worker id bits {}, sequence bits {}, workerid {}, datacenterId {}",
				timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId, datacenterId);
	}
	
	/**
	 * 默认构造函数通过MAC产生worker id
	 */
	public SnowflakeSequenceID(){
		logger.debug("将使用计算产生worker id，data center Id 默认为0");
		genWorkerIdAndDatacenterIdByMAC();
	}

	/**
	 * 由于使用MAC计算产生worker id，由于MAC的全球唯一性，故无需再设置data center Id，默认为0
	 * generate workerId and datacenterId by MAC
	 */
	private void genWorkerIdAndDatacenterIdByMAC() {
		this.datacenterId = 0L;
		StringBuilder sb = new StringBuilder();
		try {
			byte[] address = LocalMachineUtils.getLocalMac().getBytes();
			
			for (int i = 0; i < address.length; i++) {
				byte x = address[i];
				sb.append(String.format("%02X%s", x, (i < address.length - 1) ? "-" : ""));
				this.workerId = ((workerId * 131) + x) & maxWorkerId;
				this.workerId = ((workerId << 8) - Byte.MIN_VALUE + x) & maxWorkerId;
			}
		} catch (SocketException | UnknownHostException e) {
			logger.error("获取本机MAC地址异常：{}", e.getMessage());
		}
		
		logger.debug("获取本机的MAC为{}", sb.toString());
		Date minDate = new Date(0);
		workerId = (workerId * 31 + (minDate.getTime() >>> 4)) & maxWorkerId;
		logger.debug(
				"worker starting. timestamp left shift {}, datacenter id bits {}, worker id bits {}, sequence bits {}, workerid {}, datacenterId {}",
				timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId, datacenterId);

	}

	/**
	 * 获取下一ID
	 *
	 * @return
	 */
	public synchronized long nextIdLong() {
		long timestamp = timeGen();

		// 时间错误
		if (timestamp < lastTimestamp) {
			logger.error("Clock is moving backwards. Rejecting requests until %d.", lastTimestamp);
			throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds.", lastTimestamp - timestamp));
		}

		// 如果上一个timestamp与新产生的相等，则sequence加一(0-4095循环);
		// 对新的timestamp，sequence从0开始
		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & sequenceMax;
			if (sequence == 0) {
				// 重新生成timestamp
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}

		lastTimestamp = timestamp;
		// ID偏移组合生成最终的ID，并返回ID
		long id = ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
		return Math.abs(id);
	}

	public String nextIdString() {
		return nextIdLong() + "";
	}

	/**
	 * 等待下一个毫秒的到来, 保证返回的毫秒数在参数lastTimestamp之后
	 *
	 * @param lastTimestamp
	 * @return
	 */
	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * 获得系统当前毫秒数
	 *
	 * @return
	 */
	protected long timeGen() {
		return SystemClockUtil.now();
	}

}
