package com.deco.filter;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.deco.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("=============================登录过滤器=============================");
	    String contentType=request.getContentType();
		if(contentType.toLowerCase().equals(MediaType.APPLICATION_JSON_UTF8_VALUE.toLowerCase())
	            ||contentType.toLowerCase().equals(MediaType.APPLICATION_JSON_VALUE.toLowerCase())){

	        ObjectMapper mapper = new ObjectMapper();//定义转换
	        UsernamePasswordAuthenticationToken authRequest = null;
	        try (InputStream is = request.getInputStream()){
	        	//将JSON数据转换为JAVA对象
	           UserEntity user = mapper.readValue(is,UserEntity.class);
	           //传入用户名和密码
	           authRequest = new UsernamePasswordAuthenticationToken(
	        		   user.getUsername(), user.getPassword());
	        }catch (IOException e) {
	            e.printStackTrace();
	            authRequest = new UsernamePasswordAuthenticationToken(
	                    "", "");
	        }
	        setDetails(request, authRequest);
	        SecurityContextHolder.getContext().setAuthentication(authRequest);
	        return this.getAuthenticationManager().authenticate(authRequest);
	    }else {
	        return super.attemptAuthentication(request, response);
	    }
	}

		

}
