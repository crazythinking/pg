package net.engining.pg.support.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取本地服务器相关信息工具类
 * @author luxue
 *
 */
public class LocalMachineUtils {
	
	private static final Logger log = LoggerFactory.getLogger(LocalMachineUtils.class);
	
	public static String getLocalMac() throws SocketException, UnknownHostException {
		InetAddress ia = getLocalIPAddress();
		// 获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		log.debug("mac数组长度：{}", mac.length);
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			// 字节转换为整数
			int temp = mac[i] & 0xff;
			String str = Integer.toHexString(temp);
			log.debug("每8位:{}", str);
			if (str.length() == 1) {
				sb.append("0" + str);
			} else {
				sb.append(str);
			}
		}
		log.debug("本机MAC地址:{}",sb.toString().toUpperCase());
		return sb.toString();
	}
	
	/**
	 * 来自com.alibaba.druid.support.monitor.MonitorClient
	 * @return
	 */
	public static InetAddress getLocalIPAddress() {
        try {
            Enumeration<?> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress inetAddress = null;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration<?> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    inetAddress = (InetAddress) e2.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception e) {
        	log.error("getLocalIP error", e);
        }

        return null;
    }
	
	public static void main(String[] args) throws SocketException, UnknownHostException{
//		InetAddress ia=InetAddress.getLocalHost();
//		
//		System.out.println(LocalMachineUtils.getLocalMac());
	}
}
