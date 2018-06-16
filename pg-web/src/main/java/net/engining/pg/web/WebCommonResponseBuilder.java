package net.engining.pg.web;

/**
 * 通用的Web Response Builder
 * @author luxue
 *
 */
public class WebCommonResponseBuilder<T> {
	
	public WebCommonResponse<T> build(){
		return new WebCommonResponse<T>();
	}
}
