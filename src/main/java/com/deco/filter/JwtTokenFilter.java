package com.deco.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.deco.common.enums.ResultEnum;
import com.deco.common.vo.ResultVO;
import com.deco.entity.MyGrantedAuthority;
import com.deco.entity.UserEntity;
import com.deco.utils.AccessAddressUtil;
import com.deco.utils.CollectionUtil;
import com.deco.utils.DateUtil;
import com.deco.utils.JwtTokenUtil;
import com.deco.utils.RedisUtil;
import com.deco.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
	
	private Logger log=LoggerFactory.getLogger(JwtTokenFilter.class);
	/*@Autowired
	UserService userService;*/
	@Value("${token.expirationSeconds}")
    private int expirationSeconds;
 
    @Value("${token.validTime}")
    private int validTime;
 
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("=============================Token过滤器=============================");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Authorization", "Invalid");
        System.out.println();
        String authHeader = request.getHeader("Authorization");
        //获取请求的ip地址
        String currentIp = AccessAddressUtil.getIpAddress(request);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        	String authToken = authHeader.substring("Bearer ".length());
 
            String username = JwtTokenUtil.parseToken(authToken, "_secret");
            String ip = CollectionUtil.getMapValue(JwtTokenUtil.getClaims(authToken), "ip");
			/*
			 * if(username==null&&ip==null) { log.info("此Token：{}在系统中不存在，拒绝访问",authToken);
			 * response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.
			 * TOKEN_INVALID, false))); return; }
			 */
            //进入黑名单验证
            if (redisUtil.isBlackList(authToken)) {
                log.info("用户：{}的token：{}在黑名单之中，拒绝访问",username,authToken);
                response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.TOKEN_IS_BLACKLIST, false)));
            }
 
            //判断token是否过期
            /*
             * 过期的话，从redis中读取有效时间（比如七天登录有效），再refreshToken（根据以后业务加入，现在直接refresh）
             * 同时，已过期的token加入黑名单
             */
            if (redisUtil.hasKey(authToken)) {//判断redis是否有保存
                String expirationTime = redisUtil.hget(authToken,"expirationTime").toString();
                if (JwtTokenUtil.isExpiration(expirationTime)) {
                    //获得redis中用户的token刷新时效
                    String tokenValidTime = (String) redisUtil.getTokenValidTimeByToken(authToken);
                    String currentTime = DateUtil.getTime();
                    //这个token已作废，加入黑名单
                    log.info("{}已作废，加入黑名单",authToken);
                    redisUtil.hset("blacklist", authToken, DateUtil.getTime());
                    if (DateUtil.compareDate(currentTime, tokenValidTime)) {
                        //超过有效期，不予刷新
                        log.info("{}已超过有效期，不予刷新",authToken);
                        response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.LOGIN_IS_OVERDUE, false)));
                        return;
                    } else {//仍在刷新时间内，则刷新token，放入请求头中
                        String usernameByToken = (String) redisUtil.getUsernameByToken(authToken);
                        username = usernameByToken;//更新username
                        UserEntity userEntity=(UserEntity) redisUtil.getModel(authToken);
                        ip = (String) redisUtil.getIPByToken(authToken);//更新ip
 
                        //获取请求的ip地址
                        Map<String, Object> map = new HashMap<>();
                        map.put("ip", ip);
                        String jwtToken = JwtTokenUtil.generateToken(usernameByToken, expirationSeconds, map);
 
 
                        //更新redis
                        Integer expire = validTime * 24 * 60 * 60 * 1000;//刷新时间
                        //删除旧的token保存的redis
                        redisUtil.deleteKey(authToken);
                        //新的token保存到redis中
                        redisUtil.setTokenRefresh(jwtToken,userEntity,ip);
 
                        log.info("redis已删除旧token：{},新token：{}已更新redis",authToken,jwtToken);
                        authToken = jwtToken;//更新token，为了后面
                        
                        response.setHeader("Authorization", jwtToken);
                    }
                }
 
            }else {
            	log.info("此Token：{}在系统中不存在，拒绝访问",authToken);
   			 	response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.TOKEN_INVALID, false)));
   			 	return;
            }
 
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
 
                /*
                 * 加入对ip的验证
                 * 如果ip不正确，进入黑名单验证
                 */
                if (!StringUtil.equals(ip, currentIp)) {//地址不正确
                    log.info("用户：{}的ip地址变动，进入黑名单校验",username);
                    //进入黑名单验证
                    if (redisUtil.isBlackList(authToken)) {
                        log.info("用户：{}的token：{}在黑名单之中，拒绝访问",username,authToken);
                        response.getWriter().write(JSON.toJSONString(ResultVO.result(ResultEnum.TOKEN_IS_BLACKLIST, false)));
                        return;
                    }
                    //黑名单没有则继续，如果黑名单存在就退出后面
                }
 
 
              //从redis获取user对象
                UserEntity userDetails = (UserEntity) redisUtil.getModel(authToken);
                //由于redis中无法存储集合 ，序列号后再次转集合后出现问题，需要重新赋值
                List<GrantedAuthority> privilege=new ArrayList<GrantedAuthority>();
                for (Object obj : userDetails.getAuthorities().toArray()) {
					JSONObject json=JSONObject.parseObject(obj.toString());
					privilege.add(new MyGrantedAuthority(json.getString("authority")));
				}
                //将转纠正后的集合给User权限赋值
                userDetails.setAuthorities(new HashSet<GrantedAuthority>(privilege));
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
 
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        filterChain.doFilter(request, response);
        
    }
}
