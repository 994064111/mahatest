package com.deco.controller.wechatapplet;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.deco.activityservice.wechatapplet.WXActivityServiceConsumer;
import com.deco.activityservice.wechatapplet.WXSignupServiceConsumer;
import com.deco.entity.UserEntity;
import com.deco.entity.UserEntity;
import com.deco.service.pc.UserService;
import com.deco.utils.AllCharacterCodeUtil;
import com.deco.utils.FileUtil;
import com.deco.utils.SerializeUtil;

@RestController
@RequestMapping("/wxsignup")
public class WXSignupController {

	@Autowired
	private WXSignupServiceConsumer signupServiceConsumer;
	
	
	private FileUtil images;
	
//	@Autowired
//	FilepathPropertie path;

	/**
	 * 获取活动报名人员列表
	 * @param activityid 活动ID
	 * @throws ParseException 
	 */
	// @PreAuthorize("hasAuthority('wxsignup/showSignupList')")
	@ResponseBody
	@RequestMapping("/showSignupList")
	public Object selectSignupListByActivityid(HttpServletRequest request) throws ParseException{
		int CurrPageNo = Integer.parseInt(request.getParameter("CurrPageNo") != null ? request.getParameter("CurrPageNo") : "0");
		int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "0");
		String activityid = request.getParameter("id");
		List<Map<String, Object>> list = signupServiceConsumer.selectSignupListByActivityid(activityid, CurrPageNo, pageSize);
		Object object=JSON.toJSON(list);
		return object;
	}
	
	/**
	 * 获取活动报名人员全部列表信息(数据库列转行的数据)
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxsignup/showSignupAllList')")
	@ResponseBody
	@RequestMapping("/showSignupAllList")
	public Object selectSignupAllListByActivityid(HttpServletRequest request){
		String activityid = request.getParameter("id");
		Map<String, Object> info = signupServiceConsumer.selectSignupAllListByActivityid(activityid);
		Object object=JSON.toJSON(info);
		return object;
	}
	
	
	/**
	 * 获取活动报名人员详细信息
	 * @param userid 用户ID
	 * @param activityid 活动ID
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxsignup/showSignupInfo')")
	@ResponseBody
	@RequestMapping("/showSignupInfo")
	public Object selectSignupByUserId(HttpServletRequest request){
		String activityid = request.getParameter("id");
		String userid = request.getParameter("userid");
		List<Map<String, Object>> list = signupServiceConsumer.selectSignupByUserId(userid, activityid);
		Object object=JSON.toJSON(list);
		return object;
	}
	
	
	/**
	 * 获取活动报名模板
	 * @param activityid 活动ID
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxsignup/showSignupTemplate')")
	@ResponseBody
	@RequestMapping("/showSignupTemplate")
	public Object selectSignupTemplate(HttpServletRequest request){
		String activityid = request.getParameter("id");
		List<Map<String, Object>> list = signupServiceConsumer.selectSignupTemplate(activityid);
		Object object=JSON.toJSON(list);
		return object;
	}
	
	
	/**
	 * 添加活动报名
	 * @param activityid 活动ID
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxsignup/addActivitySignup')")
	@ResponseBody
	@RequestMapping("/addActivitySignup")
	public Object addSignupInfo(HttpServletRequest request){
//		String jsonstr = (String) list;
		String list = request.getParameter("list");
		System.out.println("活动报名List:" + list);
		JSONObject json = JSONArray.parseObject(list);
		System.out.println("活动报名Json:" + json);
		String listArray = json.getString("list");
		String activityid = json.getString("id");
		String userid = json.getString("userid");
		List<Map<String,Object>> listInfo = (List<Map<String,Object>>) JSONArray.parse(listArray);
		System.out.println("活动报名ListInfo:" + listInfo);
//		String field = listInfo.get(0).get("fieldlabel").toString();
		Map<String,Object> resultMap = signupServiceConsumer.addSignup(userid, activityid, listInfo);
		Object object=JSON.toJSON(resultMap);
		return object;
	}
	
	
	/**
	 * 添加审核活动报名人员信息
	 * @param activityid 活动ID
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxsignup/addSignupMessage')")
	@ResponseBody
	@RequestMapping("/addSignupMessage")
	public Object addSignupMessage(HttpServletRequest request){
		String status = request.getParameter("status");
		String signupid = request.getParameter("signupid");
		String schoolid = request.getParameter("schoolid");
		String userid = request.getParameter("userid");
		String info = request.getParameter("info");
		Map<String, Object> resultinfo = signupServiceConsumer.addSignupMessage(status, signupid, schoolid, userid, info);
		Object object=JSON.toJSON(resultinfo);
		return object;
	}
	
	
	
}
