package net.engining.pg.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * 针对Ajax异步调用，永远只返回401的entry point；
 * 【如供gwt rpc使用，并且需要 {@link RPCTemplate}配合】
 * 
 * @author binarier
 *
 */
public class Ajax401EntryPoint extends LoginUrlAuthenticationEntryPoint {

	public Ajax401EntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	public Ajax401EntryPoint() {
		super("/");
	}

	@Override
	public void commence(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		if ("XmlHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
			// 如果是ajax请求，则直接返回401
			response.sendError(HttpStatus.UNAUTHORIZED.value());
		} else {
			// 否则原样处理
			super.commence(request, response, authException);
		}
//		response.sendError(HttpStatus.UNAUTHORIZED.value());
	}
}