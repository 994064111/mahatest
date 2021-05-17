package com.deco.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.deco.common.enums.ResultEnum;
import com.deco.common.vo.ResultVO;

/**
 * @author: zhangjg
 * @date: 2019/4/30 16:12
 * @description: 用户未登录时返回给前端的数据
 */
@Component
public class ResultAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		response.setContentType("application/json;charset=utf-8");
    	response.setCharacterEncoding("utf-8");
		response.getWriter().append(JSON.toJSONString(ResultVO.result(ResultEnum.USER_NEED_AUTHORITIES,false)));
		
	}

}
