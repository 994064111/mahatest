package com.deco.handler;

import com.alibaba.fastjson.JSON;
import com.deco.common.enums.ResultEnum;
import com.deco.common.vo.ResultVO;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: zhangjg
 * @date: 2019/4/30 16:12
 * @description: 用户登录失败时返回给前端的数据
 */
@Component
public class ResultAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
    	response.setContentType("application/json;charset=utf-8");
    	response.setCharacterEncoding("utf-8");
    	response.getWriter().append(JSON.toJSONString(ResultVO.result(ResultEnum.USER_LOGIN_FAILED,false)));
    }

}
