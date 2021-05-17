package com.deco.activityservice.wechatapplet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.deco.entity.DcMessage;
import com.deco.entity.DcSignup;
import com.deco.entity.DcSignupSub;
import com.deco.service.pc.SignupService;
import com.deco.service.wechatapplet.WXActivityService;
import com.deco.service.wechatapplet.WXMessageService;
import com.deco.service.wechatapplet.WXSignupService;
import com.deco.service.wechatapplet.WXUserService;
import com.deco.utils.ActivityFileUtil;
import com.deco.utils.GetLinuxIpUtil;


@Component
public class WXSignupServiceConsumer {

	@Autowired
	private WXSignupService signupService;
	
	@Autowired
	private SignupService PCsignupService;
	
	@Autowired
	private WXActivityService activityService;
	
	@Autowired
	private WXUserService userService;
	
	@Autowired
	private WXMessageService messageService;

	ActivityFileUtil images = new ActivityFileUtil();
	SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	GetLinuxIpUtil linuxip = new GetLinuxIpUtil();
	
	/**
	 * 获取活动报名人员列表
	 * @param activityid 活动ID
	 * @return
	 * @throws ParseException 
	 */
	public List<Map<String, Object>> selectSignupListByActivityid(String activityid, int CurrPageNo, int pageSize) throws ParseException{
		String picturesPathName = "";
		List<Map<String, Object>> list = signupService.selectSignupListByActivityid(activityid, CurrPageNo, pageSize);
		if(list.size()>0){
			for(int i=0; i<list.size(); i++){
				int sid = Integer.parseInt(list.get(i).get("userid").toString());
				Map<String, Object> userinfo = userService.selectUserById(sid, picturesPathName);
				if(userinfo.size() == 0){
					list.remove(i);
					continue;
				}
				list.get(i).put("headphoto", userinfo.get("headphoto"));
				
				String fieldvalue = "";
				if(!"".equals(userinfo.get("label")) && userinfo.get("label") != null){
					fieldvalue = userinfo.get("label").toString();
				}else
				if(!"".equals(userinfo.get("wechatname")) && userinfo.get("wechatname") != null){
					fieldvalue = userinfo.get("wechatname").toString();
				}else
				if(!"".equals(userinfo.get("wechatappname")) && userinfo.get("wechatappname") != null){
					fieldvalue = userinfo.get("wechatappname").toString();
				}else
				if(!"".equals(userinfo.get("qqname")) && userinfo.get("qqname") != null){
					fieldvalue = userinfo.get("qqname").toString();
				}else
				if(!"".equals(userinfo.get("wechath5name")) && userinfo.get("wechath5name") != null){
					fieldvalue = userinfo.get("wechath5name").toString();
				}
				
				list.get(i).put("fieldvalue", fieldvalue);
				list.get(i).put("createdate", from.format(list.get(i).get("sigunupcreatedate")));
				
				
//				list.get(i).put("qqname", userinfo.get("qqname"));
//				list.get(i).put("wechatname", userinfo.get("wechatname"));
//				list.get(i).put("userlabel", userinfo.get("label"));
//				list.get(i).put("username", userinfo.get("name"));
//				list.get(i).put("createdate",from.format(list.get(i).get("createdate")));
//				list.get(i).put("sigunupcreatedate",from.format(list.get(i).get("sigunupcreatedate")));
			}
		}
		
		
		return list;
	}
	
	/**
	 * 获取活动报名人员全部列表信息(数据库列转行的数据)
	 * @param activityid
	 * @return
	 */
	public Map<String, Object> selectSignupAllListByActivityid(String activityid){
		String picturesPathName = "";
		Map<String, Object> resultInfo = new HashMap<String, Object>();
		List<Map<String, Object>> signuplist = new ArrayList<>();
		List<Map<String, Object>> titlelist = new ArrayList<>();
		
		List<Map<String, Object>> templatelist = signupService.selectSignupTemplate(activityid);
		
		for(int i=0; i<templatelist.size(); i++){
			Map<String, Object> titleinfo = new HashMap<String, Object>();
			titleinfo.put("id", templatelist.get(i).get("fieldid"));
			titleinfo.put("label", templatelist.get(i).get("fieldLabel"));
			titleinfo.put("name", templatelist.get(i).get("fieldName"));
			titlelist.add(titleinfo);
		}
		signuplist = PCsignupService.showSignup(activityid, titlelist);
		for(int j=0; j<signuplist.size(); j++){
			String signupid = signuplist.get(j).get("parentid").toString();
			Map<String, Object> signupInfo = signupService.selectSignupUserBySignupId(signupid, activityid);
			int userid = Integer.parseInt(signupInfo.get("userid").toString());
			Map<String, Object> userInfo = userService.selectUserById(userid, picturesPathName);
			

			String nickname = "";
			if(!"".equals(userInfo.get("label")) && userInfo.get("label") != null){
				nickname = userInfo.get("label").toString();
			}else{
				if(!"".equals(userInfo.get("qqname")) && userInfo.get("qqname") != null){
					nickname = userInfo.get("qqname").toString();
				}else
				if(!"".equals(userInfo.get("wechatappname")) && userInfo.get("wechatappname") != null){
					nickname = userInfo.get("wechatappname").toString();
				}else
				if(!"".equals(userInfo.get("wechath5name")) && userInfo.get("wechath5name") != null){
					nickname = userInfo.get("wechath5name").toString();
				}else
				if(!"".equals(userInfo.get("wechatname")) && userInfo.get("wechatname") != null){
					nickname = userInfo.get("wechatname").toString();
				}
			}
			signuplist.get(j).put("nickname",nickname);
				
		}
		Map<String, Object> titleinfo = new HashMap<String, Object>();
		titleinfo.put("id", "0");
		titleinfo.put("label", "昵称");
		titleinfo.put("name", "nickname");
		titlelist.add(titleinfo);
		
		resultInfo.put("titlelist", titlelist);
		resultInfo.put("signuplist", signuplist);
//		List<Map<String, Object>> signup = new ArrayList<>();
//		List<Map<String, Object>> signupuserlist = PCsignupService.selectSignup(activityid);
//		if (signuplist.size() > 0) {
//			for (int i = 0; i < signuplist.size(); i++) {
//				Map<String, Object> signupmap = new HashMap<String, Object>();
//				for (int j = 0; j < titlelist.size(); j++) {
//					signupmap.put(titlelist.get(j).get("name").toString(),
//							signuplist.get(i).get(titlelist.get(j).get("name").toString()));
//				}
//				signupmap.put("id", signuplist.get(i).get("id"));
//				signupmap.put("status", signuplist.get(i).get("status"));
//				signupmap.put("parentid", signuplist.get(i).get("parentid"));
//				for (int j = 0; j < signupuserlist.size(); j++) {
//					if (signuplist.get(i).get("parentid").toString()
//							.equals(signupuserlist.get(j).get("id").toString())) {
//						signupmap.put("status", signupuserlist.get(j).get("status"));
//						signup.add(signupmap);
//					}
//				}
//			}
//		} 
		return resultInfo;
	}
	

	/**
	 * 获取活动报名人员详细信息
	 * @param userid 用户ID
	 * @param activityid 活动ID
	 * @return
	 */
	public List<Map<String, Object>> selectSignupByUserId(String userid, String activityid){
		List<Map<String, Object>> list = signupService.selectSignupByUserId(userid, activityid);
		List<Map<String, Object>> signupFieldSubList = activityService.selectActivitySignupFieldSub();
		if(list.size() > 0 && signupFieldSubList.size() > 0){
			for(int i = 0; i < list.size(); i++){
				Map<String, Object> info = new HashMap<String, Object>();
				info = list.get(i);
				for(int j = 0; j < signupFieldSubList.size(); j++){
					Map<String, Object> fieldSubInfo = new HashMap<String, Object>();
					fieldSubInfo = signupFieldSubList.get(j);
					if(info.get("fieldid").toString().equals(fieldSubInfo.get("fieldid").toString()) && info.get("fieldvalue").toString().equals(fieldSubInfo.get("value").toString())){
						info.put("fieldvalueid", fieldSubInfo.get("value"));
						info.put("fieldvalue", fieldSubInfo.get("label"));
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * 获取活动报名模板
	 * @param activityid 活动ID
	 * @return
	 */
	public List<Map<String, Object>> selectSignupTemplate(String activityid){
		List<Map<String, Object>> list = signupService.selectSignupTemplate(activityid);
		String fieldType = "";
		String fieldId = "";
		for(int i=0; i<list.size(); i++){
			fieldType = list.get(i).get("fieldType").toString();
			if(fieldType.equals("2")){
				fieldId = list.get(i).get("fieldid").toString();
				JSONArray jsonarr = new JSONArray();
				List<Map<String, Object>> sublist = signupService.selectSignupTemplateSub(fieldId);
				if(sublist.size()>0){
					for(int j=0; j<sublist.size(); j++){
						JSONObject jsonsub = new JSONObject();
						jsonsub.put("fieldsubid", sublist.get(j).get("id"));
						jsonsub.put("fieldsublabel", sublist.get(j).get("label"));
						jsonsub.put("fieldsubvalue", sublist.get(j).get("value"));
						jsonarr.set(j, jsonsub);
					}
				}
				list.get(i).put("fieldsub", jsonarr);
			}
		}
		
		return list;
	}
	
	/**
	 * 添加活动报名人员信息
	 * @param userid
	 * @param activityid
	 * @param listInfo
	 * @return
	 */
	public Map<String, Object> addSignup(String userid, String activityid ,List<Map<String,Object>> listInfo){
		Map<String,Object> resultMap=new  HashMap<String,Object>();
		//查询活动是否需要审核
		Map<String,Object> map = activityService.selectActivityAuditstatusById(activityid);
		if(map == null){
			resultMap.put("status", false);
			resultMap.put("info", "获取审核状态失败！");
			return resultMap;
		}
		String status = "1";
		String auditstatus = map.get("auditstatus").toString();
		if(auditstatus.equals("1")){//需要审核
			status = "1";//待审核
		}else if(auditstatus.equals("0")){//不需要审核
			status = "2";//待支付
		}
		Date date = new Date();
		//添加报名主表信息
		DcSignup signup = new DcSignup();
		signup.setActivityid(activityid);
		signup.setUserid(userid);
		signup.setStatus(status);
		signup.setCreatedate(date);
		int signupid = signupService.addSignup(signup);
		if(signupid == 0){
			resultMap.put("status", false);
			resultMap.put("info", "提交失败");
		}else{
			//添加报名从表信息
			Map<String,Object> mapInfo = new HashMap<String,Object>();
			for(int i=0; i<listInfo.size(); i++){
				mapInfo = listInfo.get(i);
				DcSignupSub signupsub = new DcSignupSub();
				signupsub.setFieldid(mapInfo.get("fieldid").toString());
				signupsub.setFieldlabel(mapInfo.get("fieldlabel").toString());
				signupsub.setFieldvalue(mapInfo.get("fieldcontent").toString());
				signupsub.setParentid(signupid + "");
				signupsub.setStatus("0");
				signupsub.setSort("0");
				signupsub.setCreatedate(date);
				int num = signupService.addSignupSub(signupsub);
				if(num <= 0){
					int selectSubNum = signupService.selectSignupSubBySignupid(signupid + "");
					if(selectSubNum <= 0 ){
						resultMap.put("status", false);
						resultMap.put("info", "提交信息失败");
					}else{
						int deleteSubNum = signupService.deleteSignupSubBySignupid(signupid + "");
						if(deleteSubNum > 0){
							int deleteNum = signupService.deleteSingnupById(signupid + "");
							if(deleteNum > 0){
								resultMap.put("status", false);
								resultMap.put("info", "提交信息失败");
							}else{
								resultMap.put("status", false);
								resultMap.put("info", "提交信息失败并且删除主要信息失败！");
							}
						}else{
							resultMap.put("status", false);
							resultMap.put("info", "提交信息失败并且删除从属信息失败！");
						}
					}
					return resultMap;
				}
			}
			resultMap.put("status", true);
			resultMap.put("info", "提交成功");
			resultMap.put("signupid", signupid);
		}
		
		
		return resultMap;
	}
	
	
	/**
	 * 添加审核活动报名人员信息
	 * @param status
	 * @param Parentid
	 * @param schoolid
	 * @param userid
	 * @param info
	 * @return
	 */
	public Map<String,Object> addSignupMessage(String status, String signupid, String schoolid, String userid, String info){
		Map<String,Object> resultMap = new  HashMap<String,Object>();
		DcMessage message = new DcMessage();
		message.setInfo(info);
		message.setSchoolid(schoolid);
		message.setUserid(userid);
		message.setStatus("2");
		message.setType("2");
		int resultMessage = messageService.addMessage(message);
		if(resultMessage <= 0){
			resultMap.put("status", false);
			resultMap.put("info", "添加审核信息失败");
			return resultMap;
		}
		int resultSignup = signupService.updateSignupStatusById(signupid, status);
		if(resultSignup <= 0){
			resultMap.put("status", false);
			resultMap.put("info", "更改报名人员状态信息失败");
			return resultMap;
		}
		resultMap.put("status", true);
		resultMap.put("info", "添加审核信息成功");
		return resultMap;
			
	}
	
	/**
	 * 支付成功后修改状态并发信息
	 * @param status 要修改的状态
	 * @param nickname 用户昵称
	 * @param Parentid 报名人ID
	 * @return
	 */
	public int updateSignupstatus(String status, String nickname, String Parentid) {
		int result = 0;
		result = signupService.updateSignupStatusById(Parentid, status);
		if(result == 1 && "4".equals(status)){
			String content = "";
			Map<String, Object> info = new HashMap<String, Object>();
			info = signupService.selectSignupActivityInfoById(Parentid);
			if(info.size() != 0){
				String activitylabel = info.get("activitylabel").toString();
				content = nickname + "您所参加的 \"" + activitylabel + "\" 活动，报名已成功！";
				String schoolid = info.get("schoolid").toString();
				String userid = info.get("userid").toString();
				DcMessage message = new DcMessage();
				message.setInfo(content);
				message.setSchoolid(schoolid);
				message.setUserid(userid);
				message.setStatus("2");
				message.setType("2");
				int resultMessage = messageService.addMessage(message);
			}
		}
		return result;
		
	}
	
	

	
}
