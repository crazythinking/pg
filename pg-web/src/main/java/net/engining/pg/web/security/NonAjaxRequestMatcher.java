package net.engining.pg.web.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 针对ajax的request请求不进行Request缓存, 针对Spring-security对未login的request有缓存能力，直接重定向到登陆前的reques；但通常ajax不需要这种能力；
 * 
 * @author binarier 
 * @see http://distigme.wordpress.com/2012/11/01/ajax-and-spring-security-form-based-login
 */
public class NonAjaxRequestMatcher implements RequestMatcher {
    @Override
    public boolean matches(HttpServletRequest request) {
    	
        return !"XmlHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
}