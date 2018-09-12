package net.engining.pg.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import com.google.common.base.Optional;

import net.engining.pg.web.WebCommonUtils;
import net.engining.pg.web.bean.WebLoginUser;

/**
 * 存放在MDC中的数据，log4j可以直接引用并作为日志信息打印出来.
 * 
 * <pre>
 *  
 * 示例使用: 
 * log4j.appender.stdout.layout.conversionPattern=%d [%X{loginUserId}/%X{req.remoteAddr}/%X{req.id} - %X{req.requestURI}?%X{req.queryString}] %-5p %c{2} - %m%n
 * </pre>
 * 
 * @author
 */
public class Log4jMappedDiagnosticContextFilter extends AbstractRequestLoggingFilter {

	/**
	 * Logs the before-request message through Log4J and adds a message the
	 * Log4J MDC before the request is processed.
	 */
	@Override
	protected void beforeRequest(HttpServletRequest request, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
		setupMDC(request);

	}

	/**
	 * Removes the log message from the Log4J MDC after the request is processed
	 * and logs the after-request message through Log4J.
	 */
	@Override
	protected void afterRequest(HttpServletRequest request, String message) {
		ThreadContext.clearAll();
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
		
	}

	/**
	 * 创建MDC内的信息，便于日志跟踪
	 * 
	 * @param request
	 */
	private void setupMDC(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		if (Optional.fromNullable(session).isPresent()) {
			ThreadContext.put("req.sessionId", session.getId());
			WebLoginUser wusr = (WebLoginUser) session.getAttribute(WebCommonUtils.SE_CURRENT_USER);
			if (Optional.fromNullable(wusr).isPresent()) {
				ThreadContext.put("req.loginId", wusr.getLoginId());
			} else {
				ThreadContext.put("req.loginId", "anonymous");
			}
		}
		ThreadContext.put("req.hostname", request.getServerName());
		//请求的uri
		ThreadContext.put("requestUri", request.getRequestURI());
		//客户端的Ip
		ThreadContext.put("clientIp", WebCommonUtils.getIpAddress(request));
	}

}
