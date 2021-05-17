package com.deco.handler;

import com.alibaba.fastjson.JSON;
import com.deco.common.enums.ResultEnum;
import com.deco.common.vo.ResultVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: zhangjg
 * @date: 2019/4/30 16:12
 * @description: 无权访问
 */
@Component
public class ResultAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
    	response.setContentType("application/json;charset=utf-8");
    	response.setCharacterEncoding("utf-8");
    	response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.USER_NO_ACCESS,false)));
    	
    }
}
