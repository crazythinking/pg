package net.engining.pg.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


/**
 * web端通用工具类
 * @author Eric Lu
 *
 */
public class WebCommonUtils {
	
	/**
	 * 系统当前用户标识，通常作为session的建
	 */
	public final static String SE_CURRENT_USER	= "CURRENT_USER";
	
	public final static String SE_JWT_SIGNKEY	= "Jwt@SecrEtKey123!@#";
	
	/**
	 * 获取客户端的IP
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request){    
        String ip = request.getHeader("x-forwarded-for");    
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {    
            ip = request.getHeader("Proxy-Client-IP");    
        }    
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {    
            ip = request.getHeader("WL-Proxy-Client-IP");    
        }    
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {    
            ip = request.getHeader("HTTP_CLIENT_IP");    
        }    
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {    
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");    
        }    
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {    
            ip = request.getRemoteAddr();    
        }    
        return ip;    
    }
	
	
}
