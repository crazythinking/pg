package net.engining.pg.web;

import net.engining.pg.web.bean.NewWebCommonRequest;

/**
 * 重构，支持自定义RequestHead
 * 通用的Web Request Builder
 * @author luxue
 *
 */
public class NewWebCommonRequestBuilder<H,T> {
	
	public NewWebCommonRequest<H,T> build(){
		return new NewWebCommonRequest<H,T>();
	}
}
