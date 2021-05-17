package com.deco.controller.pc;

import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.deco.activityservice.pc.ActivityConsumer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RequestMapping("/activity")
public class ActivityController {

	@Autowired
	private ActivityConsumer activityConsumer;
	
	/**
	 * 显示活动信息
	 * @return
	 * @throws ParseException 
	 */
	@PreAuthorize("hasAuthority('activity/showactivity')")
	@PostMapping("showactivity")
	public Object showActivity(int CurrPageNo,int pageSize) throws ParseException {
		return activityConsumer.showActivity(CurrPageNo, pageSize);
	}
	
	/**
	 * 搜索活动信息
	 * @param label 活动名或举办方
	 * @param activitystart 时间 （0000-00-00）
	 * @param status 状态 （0 未发布，1 进行中，2已结束）
	 * @param CurrPageNo 当前页数
	 * @param pageSize 显示行数
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/showsearchactivity')")
	@PostMapping("showsearchactivity")
	public  Object showSearchActivity(String label,String activitystart,String status,int CurrPageNo,int pageSize){
		return activityConsumer.showSearchActivity(label, activitystart, status,CurrPageNo,pageSize);
		
	}
	
	/**
	 * 删除活动信息
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/deleteactivity')")
	@PostMapping("deleteactivity")
	public int DeleteActivity(String id){
		return activityConsumer.DeleteActivity(id);
		
	}
	
	/**
	 * 查看活动信息详情
	 * @param id
	 * @return
	 * @throws SocketException 
	 */
	@PreAuthorize("hasAuthority('activity/showactivitydetails')")
	@PostMapping("showactivitydetails")
	public Object showActivityDetails(String id) throws SocketException{
		return activityConsumer.showActivityDetails(id);
		
	}
	
	/**
	 * 修改活动回显
	 * @param id
	 * @return
	 * @throws SocketException
	 */
	@PreAuthorize("hasAuthority('activity/showupdateactivity')")
	@PostMapping("showupdateactivity")
	public Object showUpdateActivity(String id) throws SocketException{
		return activityConsumer.showUpdateActivity(id);
		
		
	}
	
	/**
	 * 修改活动信息
	 * @param JSONActivity
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/updateactivity')")
	@PostMapping("updateactivity")
	public int UpdateActivity(@RequestBody String JSONActivity){
		return activityConsumer.UpdateActivity(JSONActivity);
		
	}
	
	/**
	 * 删除活动图片
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/deleteimage')")
	@PostMapping("deleteimage")
	public int deleteimage(String id){
		return activityConsumer.deleteimage(id);
		
	}
	
	/**
	 * 修改活动信息图片
	 * @param file
	 * @param activityid
	 * @return
	 * @throws SocketException
	 */
	@PreAuthorize("hasAuthority('activity/updateimage')")
	@PostMapping("updateimage")
	public Object updateimage(@RequestParam("file") MultipartFile file, @RequestParam("activityid") String activityid)
			throws SocketException{
		return activityConsumer.updateimage(file, activityid);
		
	}
	
	/**
	 * 添加活动时选择学堂
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/showallschool')")
	@PostMapping("showallschool")
	public Object showAllSchool(){
		return activityConsumer.showAllSchool();
		
	}
	
	
	
	/**
	 * 添加图片
	 * @param file
	 * @return
	 * @throws SocketException
	 */
	@PreAuthorize("hasAuthority('activity/addimage')")
	@PostMapping("addimage")
	public Object addimage(@RequestParam("file")  MultipartFile file) throws SocketException{
		return activityConsumer.addimage(file);
		
	}
	
	/**
	 * 添加多图片
	 * @param file
	 * @return
	 * @throws SocketException
	 */
	@PreAuthorize("hasAuthority('activity/images')")
	@PostMapping("images")
	public Object images(@RequestParam("file")  MultipartFile file) throws SocketException{
		return activityConsumer.images(file);
		
	}
	
	/**
	 * 添加主图片
	 * @param file
	 * @return
	 * @throws SocketException
	 */
	@PreAuthorize("hasAuthority('activity/addmainimage')")
	@PostMapping("addmainimage")
	public Object addMainimage(@RequestParam("file")  MultipartFile file) throws SocketException{
		return activityConsumer.addMainimage(file);
		
	}
	
	/**
	 * 添加活动信息
	 * @param JSONactivity
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/addactivity')")
	@PostMapping("addactivity")
	public int addActivity(@RequestBody String JSONactivity){
		return activityConsumer.addActivity(JSONactivity);
		
	}
	
	/**
	 * 审核活动报名人员信息
	 * @param status 状态
	 * @param Parentid 人员id
	 * @param schoolid 学校id
	 * @param info 不通过理由
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/updateuserSignup')")
	@PostMapping("updateuserSignup")
	public int updateuserSignup(String status,String Parentid,String schoolid,String info,String userid){
		return activityConsumer.updateuserSignup(status, Parentid, schoolid, info,userid);
		
	}
	
	/**
	 * 支付成功后修改状态
	 * @param status 
	 * @param Parentid
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/updatesignupstatus')")
	@RequestMapping("updatesignupstatus")
	public int updateSignupstatus(String status, String parentid) {
		return activityConsumer.updateSignupstatus(status, parentid);
		
	}
	/**
	 * 获取发布活动的用户信息
	 * @param userid
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/showactivityuser')")
	@PostMapping("showactivityuser")
	public Object showActivityUser(String userid){
		return activityConsumer.showActivityUser(userid);
		
	}
	
	/***
	 * 查询报名项信息
	 */
	@PreAuthorize("hasAuthority('activity/selectsignupfield')")
	@PostMapping("selectsignupfield")
	public Object selectSignupField(){
		return activityConsumer.selectSignupField();
		
	}
	/**
	 * 活动排序
	 * @param activityid 
	 * @param sort 
	 * @return
	 */
	@PreAuthorize("hasAuthority('activity/activitysort')")
	@PostMapping("activitysort")
	public int activitySort(String activityid,String sort){
		//String schoolid = request.getParameter("schoolid");
		//String sort = request.getParameter("sort");
		System.out.println(activityid+"********activityid*********");
		System.out.println(sort+"*********sort********");
		return activityConsumer.activitySort(activityid, sort);
		
		//return schoolServiceConsumer.SchoolSort(schoolid, sort);
	}

}
