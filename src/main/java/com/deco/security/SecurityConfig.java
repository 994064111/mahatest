package com.deco.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.deco.filter.JwtTokenFilter;
import com.deco.filter.MyAuthenticationFilter;
import com.deco.handler.ResultAccessDeniedHandler;
import com.deco.handler.ResultAuthenticationEntryPoint;
import com.deco.handler.ResultAuthenticationFailureHandler;
import com.deco.handler.ResultAuthenticationSuccessHandler;
import com.deco.handler.ResultLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	  @Autowired
	  private ResultAuthenticationEntryPoint authenticationEntryPoint;//未登陆时返回 JSON 格式的数据给前端（否则为 html）
	  @Autowired
	  private ResultAuthenticationSuccessHandler authenticationSuccessHandler; //登录成功返回的 JSON 格式数据给前端（否则为 html）
	  @Autowired
	  private ResultAuthenticationFailureHandler authenticationFailureHandler; //登录失败返回的 JSON 格式数据给前端（否则为 html）
	  @Autowired
	  private ResultLogoutSuccessHandler logoutSuccessHandler;//注销成功返回的 JSON 格式数据给前端（否则为 登录时的 html）
	  @Autowired
	  private ResultAccessDeniedHandler accessDeniedHandler;//无权访问返回的 JSON 格式数据给前端（否则为 403 html 页面）
	  /*@Autowired 
	  private UserService userService;*/
	  @Autowired
	  JwtTokenFilter jwtTokenFilter;
	  
	  @Bean
	  @Override 
	  public AuthenticationManager authenticationManagerBean() throws Exception { 
		  AuthenticationManager manager =super.authenticationManagerBean(); 
		  return manager; 
	  }
	  /**
	   * 自定义FilterBean
	   * @return
	   * @throws Exception
	   */
	  @Bean
	  public MyAuthenticationFilter myAuthenticationFilter() throws Exception {
		  MyAuthenticationFilter filter=new MyAuthenticationFilter();
		  filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);//登录成功处理
		  filter.setAuthenticationFailureHandler(authenticationFailureHandler);//登录失败处理
		  filter.setAuthenticationManager(authenticationManagerBean());//添加ManagerBean()
		  return filter;
	  }

	/*
	 * @Autowired(required = false) MyAuthenticationFilter myAuthenticationFilter;
	 */
	  /**
	   * 主要配置
	   */
	  @Override 
	  protected void configure(HttpSecurity http) throws Exception { 
		  http.csrf().disable();//关闭跨域保护
		  http.cors().and();//打开跨域
		  http
		  .authorizeRequests()//启动授权
		  .antMatchers(
				  //"/activity/**",
				  "/static/**",
				  "/image/**",
				  "/audio/**",
				  "/video/**",
				  "/wxactivity/**",
				  "/wxsignup/**",
				  "/wxactivitymessage/**").permitAll() //不需要权限就能访问的路径
		  .antMatchers("/**").authenticated()//需要权限才能访问的路径
		  .anyRequest().authenticated()//对http所有的请求必须通过授权认证才可以访问。
		  .and()
		  .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)//当用户请求了一个受保护的资源，但是用户没有通过认证，那么抛出异常，
		  .and()
		  .formLogin()//表单登录
		  .usernameParameter("name")//账号参数
          .passwordParameter("password")//密码
		  .loginPage("/login")//登录请求地址
		  //.successHandler(authenticationSuccessHandler)//登录成功请求处理（只限于默认的过滤器时定义 如果使用了自定义过滤器将无法执行  则在上方myAuthenticationFilter中定义）
		  //.failureHandler(authenticationFailureHandler)//登录失败请求处理（只限于默认的过滤器时定义 如果使用了自定义过滤器将无法执行  则在上方myAuthenticationFilter中定义）
		  .and()
		  .logout()//开启注销
		  .logoutUrl("/logout")//注销地址
		  .logoutSuccessHandler(logoutSuccessHandler)//注销成功处理
		  .and()
		  .sessionManagement()//定制我们自己的 session 策略
	      .sessionCreationPolicy(SessionCreationPolicy.STATELESS);// 调整为让 Spring Security 不创建和使用 session
		  http.exceptionHandling().accessDeniedHandler(accessDeniedHandler); //没有权限时请求处理
		  http.addFilterAt(myAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);//自定义的过滤器替换默认的过滤器，主要为登录过滤器
		  http.addFilterBefore(jwtTokenFilter,UsernamePasswordAuthenticationFilter.class); //在这之前添加过滤器（执行顺序问题）这个先执行 主要为token验证
		  
	  }
	  /**
	   * 配置用户登录的Service 及对密码的加密
	   */
	 /* @Override 
	  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		  System.out.println("密码加密");
		  auth.userDetailsService(userService).passwordEncoder(passwordEncoder()); 
	  }*/
	  
	  @Override 
	  public void configure(WebSecurity web) throws Exception { 
		  // TODOAuto-generated method stub 
		  web.ignoring().antMatchers("/v2/api-docs",//swagger api json
				  	"/webjars/**",
				  	"/distv2/**",
				  	"/swagger-dubbo/**",
	                "/swagger-resources/configuration/ui",//用来获取支持的动作
	                "/swagger-resources",//用来获取api-docs的URI
	                "/swagger-resources/configuration/security",//安全选项
	                "/swagger-ui.html");
		  super.configure(web); 
	  }
	  /**
	   * 自定义密码加密Bean
	   * @return
	   */
	  @Bean
	  public PasswordEncoder passwordEncoder() { 
		  //密码加密
		  return new BCryptPasswordEncoder(); 
	  }
	 


}
