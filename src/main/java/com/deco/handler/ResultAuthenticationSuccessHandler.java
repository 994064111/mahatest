package com.deco.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.deco.common.enums.ResultEnum;
import com.deco.common.vo.ResultVO;
import com.deco.entity.UserEntity;
import com.deco.utils.AccessAddressUtil;
import com.deco.utils.JwtTokenUtil;
import com.deco.utils.RedisUtil;
import com.deco.utils.SerializableUtil;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author: zhangjg
 * @date: 2019/4/30 16:12
 * @description: 鐢ㄦ埛鐧诲綍鎴愬姛鏃惰繑鍥炵粰鍓嶇鐨勬暟鎹�
 */
@Slf4j
@Component
public class ResultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private Logger log=LoggerFactory.getLogger(ResultAuthenticationSuccessHandler.class);
	@Value("${token.expirationSeconds}")
    private int expirationSeconds;
 
    @Value("${token.validTime}")
    private int validTime;
    
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String ip = AccessAddressUtil.getIpAddress(request);
        Map<String,Object> map = new HashMap<>();
        map.put("ip",ip);
 
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
 
        String jwtToken = JwtTokenUtil.generateToken(userEntity.getUsername(), expirationSeconds, map);
 
        //鍒锋柊鏃堕棿
        Integer expire = validTime*24*60*60*1000;
        //鑾峰彇璇锋眰鐨刬p鍦板潃
        String currentIp = AccessAddressUtil.getIpAddress(request);
        System.out.println(SerializableUtil.serialize(userEntity));
        redisUtil.setTokenRefresh(jwtToken,userEntity,currentIp);
        log.info("鐢ㄦ埛{}鐧诲綍鎴愬姛锛屼俊鎭凡淇濆瓨鑷硆edis",userEntity.getUsername());
        response.setContentType("application/json;charset=utf-8");
    	response.setCharacterEncoding("utf-8");
    	Map<String, Object> json=ResultVO.result(ResultEnum.USER_LOGIN_SUCCESS,jwtToken,true);
    	List<String> list=new ArrayList<String>();
    	for(Object obj:userEntity.getAuthorities()) {
    		list.add(obj.toString());
    	}
    	json.put("permissions",JSONArray.toJSON(list));
    	json.put("username",userEntity.getUsername());
        response.getWriter().append(JSON.toJSONString(json));
       
    }
}
