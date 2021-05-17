package com.deco.handler;

import com.alibaba.fastjson.JSON;
import com.deco.common.enums.ResultEnum;
import com.deco.common.vo.ResultVO;
import com.deco.utils.DateUtil;
import com.deco.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: zhangjg
 * @date: 2019/4/29 
 * @description: 鐧诲嚭鎴愬姛
 */
@Slf4j
@Component
public class ResultLogoutSuccessHandler implements LogoutSuccessHandler {
	
	private Logger log=LoggerFactory.getLogger(ResultLogoutSuccessHandler.class);
	
	@Autowired
	RedisUtil redisUtil;
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    	String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String authToken = authHeader.substring("Bearer ".length());
            //灏唗oken鏀惧叆榛戝悕鍗曚腑
            redisUtil.hset("blacklist", authToken, DateUtil.getTime());
            log.info("token锛歿}宸插姞鍏edis榛戝悕鍗�",authToken);
        }
    	response.setContentType("application/json;charset=utf-8");
    	response.setCharacterEncoding("utf-8");
    	response.getWriter().append(JSON.toJSONString(ResultVO.result(ResultEnum.USER_LOGOUT_SUCCESS,true)));
    }

}
