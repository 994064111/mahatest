package com.deco.controller.wechatapplet;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/wxactivity")
public class WXActivityController {

	@Autowired
	private WXActivityServiceConsumer activityServiceConsumer;
	
	@Autowired
	private WXSignupServiceConsumer signupServiceConsumer;
	
	
//	private FileUtil images;
	
	
	@Value("${file.static}")
	String staticPath;
	@Value("${file.image}")
	String image;
	@Value("${file.audio}")
	String audio;
	@Value("${file.video}")
	String video;

	/**
	 * 获取活动信息列表
	 * @param keywords 活动关键字
	 * @param schoolid 学堂ID
	 * @param placeid 场所ID
	 * @param placekeywords 场所关键字
	 * @param year 年份
	 * @param month 月份
	 * @param picturesPathName
	 * @param CurrPageNo 页数 
	 * @param pageSize 条数 
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/showActivityList')")
	@ResponseBody
	@RequestMapping("/showActivityList")
	public Object selectActivityList(HttpServletRequest request){
		String picturesPathName = staticPath+image;
		int CurrPageNo = Integer.parseInt(request.getParameter("CurrPageNo") != null ? request.getParameter("CurrPageNo") : "0");
		int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "0");
//		String roleid = request.getParameter("roleid");
		String schoolid = request.getParameter("schoolid");
		String year = request.getParameter("year");
		String month = request.getParameter("month");
		String placeid = request.getParameter("placeid");
		String placekeywords = request.getParameter("placekeywords");
		String keywords = request.getParameter("keywords");
		List<Map<String, Object>> list = activityServiceConsumer.selectActivityList(keywords,schoolid, placeid, placekeywords, year, month, picturesPathName, CurrPageNo, pageSize);
		Object object=JSON.toJSON(list);
		return object;
	}	
	
	/**
	 * 获取活动详细信息
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/showActivityInfo')")
	@ResponseBody
	@RequestMapping("/showActivityInfo")
	public Object selectActivityInfoById(HttpServletRequest request){
		String picturesPathName = staticPath+image;
		String activityid = request.getParameter("id");
		Map<String, Object> info = activityServiceConsumer.selectActivityInfoById(activityid,picturesPathName);
		Object object=JSON.toJSON(info);
		return object;
	}
	
	/**
	 * 添加活动
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/addActivityInfo')")
	@ResponseBody
	@RequestMapping("/addActivityInfo")
	public Object addActivityInfo(HttpServletRequest request){
		String jsonInfo = request.getParameter("JSONactivity");
		Map<String, Object> info = activityServiceConsumer.addActivity(jsonInfo);
		Object object=JSON.toJSON(info);
		return object;
	}
	
	
	/**
	 * 活动访问量增加
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/updateActivityClicks')")
	@ResponseBody
	@RequestMapping("/updateActivityClicks")
	public Object updateActivityClicksById(HttpServletRequest request){
		String id = request.getParameter("id");
		int activityid = Integer.parseInt(id);
		int result = activityServiceConsumer.updateActivityClicks(activityid);
		Object object=JSON.toJSON(result);
		return object;
		
	}
	
	/**
	 * 获取活动筛选项
	 * @param itemType 筛选项类型 ：1 年份  2 月份  3 学堂ID 4 场所ID
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/showActivityQueryItem')")
	@ResponseBody
	@RequestMapping("/showActivityQueryItem")
	public Object selectActivityQueryItem(HttpServletRequest request){
		Map<String, Object> info = new HashMap<String, Object>();
		info = activityServiceConsumer.selectActivityQueryItem();
		Object object=JSON.toJSON(info);
		return object;
	}
	
	/**
	 * 支付成功后修改状态并发信息
	 * @param status 
	 * @param Parentid
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/updatesignupstatus')")
	@RequestMapping("/updatesignupstatus")
	public int updateSignupstatus(String status, String nickname, String parentid) {
		return signupServiceConsumer.updateSignupstatus(status, nickname, parentid);
		
	}
	
	/***
	 * 查询我创建的活动列表
	 * @param userid 用户id
	 * @param CurrPageNo 当前页数
	 * @param pageSize 显示条数
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/selectestablishactivity')")
	@RequestMapping("/selectestablishactivity")
	public Object SelectEstablishActivity(String userid,int CurrPageNo, int pageSize){
		return activityServiceConsumer.SelectEstablishActivity(userid, CurrPageNo, pageSize);
	}
	
	
	/**
	 * 获取活动报名项
	 * @param request
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/showActivitySignup')")
	@ResponseBody
	@RequestMapping("/showActivitySignup")
	public Object selectActivitySignupField(HttpServletRequest request){
		List<Map<String, Object>> list = activityServiceConsumer.selectActivitySignupField();
		Object object=JSON.toJSON(list);
		return object;
	}
	
	/*****PC端移植功能**开始***/
	
	/**
	 * 删除活动信息
	 * @param id
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivity/deleteactivity')")
	@PostMapping("/deleteactivity")
	public int DeleteActivity(String id){
		return activityServiceConsumer.DeleteActivity(id);
		
	}
	
	
	/**
	 * 添加图片
	 * @param file
	 * @return
	 * @throws SocketException
	 */
	// @PreAuthorize("hasAuthority('wxactivity/addimage')")
	@PostMapping("addimage")
	public Object addimage(@RequestParam("file")  MultipartFile file) throws SocketException{
		return activityServiceConsumer.addimage(file);
		
	}
	
	/**
	 * 添加主图片
	 * @param file
	 * @return
	 * @throws SocketException
	 */
	// @PreAuthorize("hasAuthority('wxactivity/addmainimage')")
	@PostMapping("addmainimage")
	public Object addMainimage(@RequestParam("file")  MultipartFile file) throws SocketException{
		return activityServiceConsumer.addMainimage(file);
		
	}
	
	
	/*****PC端移植功能**结束***/
	
}
