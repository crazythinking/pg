package net.engining.pg.web;

import net.engining.pg.web.bean.NewWebCommonResponse;

/**
 * 重构，支持自定义ResponseHead
 * 通用的Web Response Builder
 * @author luxue
 *
 */
public class NewWebCommonResponseBuilder<H,T> {
	
	public NewWebCommonResponse<H,T> build(){
		return new NewWebCommonResponse<H,T>();
	}
}
