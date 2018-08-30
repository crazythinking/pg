package net.engining.pg.web;

/**
 * 通用的Web Request Builder
 * @author luxue
 *
 */
@Deprecated
public class WebCommonRequestBuilder<T> {
	
	public WebCommonRequest<T> build(){
		return new WebCommonRequest<T>();
	}
}
