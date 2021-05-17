package com.deco.activityservice.wechatapplet;

import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.deco.entity.DcActivity;
import com.deco.entity.DcActivityAttachment;
import com.deco.entity.DcActivityLinkPlace;
import com.deco.entity.DcActivitySignupTemplate;
import com.deco.entity.DcMessage;
import com.deco.service.pc.ActivityAttachmentService;
import com.deco.service.pc.ActivityIncreasekeyService;
import com.deco.service.pc.ActivityService;
import com.deco.service.pc.PlaceService;
import com.deco.service.pc.SchoolService;
import com.deco.service.wechatapplet.WXActivityService;
import com.deco.service.wechatapplet.WXSignupService;
import com.deco.service.wechatapplet.WXUserService;
import com.deco.utils.ActivityFileUtil;
import com.deco.utils.GetLinuxIpUtil;
import com.deco.utils.SerializeUtil;

@Component
public class WXActivityServiceConsumer {

	@Autowired
	private WXActivityService activityService;
	
	@Autowired
	private WXUserService userService;
	
	@Autowired
	private WXSignupService signupService;
	
	@Autowired
	private SchoolService schoolserive;
	@Autowired
	private PlaceService placeservie;
	
	
	@Autowired
	private ActivityService PCactivityService;
	@Autowired
	private ActivityIncreasekeyService activityIncreasekeyService;
	@Autowired
	private ActivityAttachmentService activityAttachmentService;
	

	ActivityFileUtil images = new ActivityFileUtil();
	SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	GetLinuxIpUtil linuxip = new GetLinuxIpUtil();
	
	@Value("${file.web-path}")
	String url;
	@Value("${file.static}")
	String staticPath;
	@Value("${file.image}")
	String image;
	@Value("${file.audio}")
	String audio;
	@Value("${file.video}")
	String video;

	/**
	 * 获取活动列表
	 * @param keywords 活动关键字
	 * @param schoolid 学堂ID
	 * @param placeid 场所ID
	 * @param placekeywords 场所关键字
	 * @param year 年份
	 * @param month 月份
	 * @param picturesPathName
	 * @param CurrPageNo 页数 
	 * @param pageSize 条数
	 */
	public List<Map<String, Object>> selectActivityList(String keywords,String schoolid, String placeid, String placekeywords, String year, String month, String picturesPathName, int CurrPageNo, int pageSize){
		SimpleDateFormat date = new SimpleDateFormat("MM-dd");
		List<Map<String, Object>> list =activityService.selectActivityList(keywords,schoolid, placeid, placekeywords, year, month, picturesPathName, CurrPageNo, pageSize);
		List<Map<String, Object>> newlist = new ArrayList<>();
		Date entryend=null;
		Date activitystart=null;
		for (int i = 0; i < list.size(); i++) {
			try {
				entryend=from.parse(list.get(i).get("entryend").toString());
				activitystart=from.parse(list.get(i).get("activitystart").toString());
			} catch (ParseException e) {
				entryend=new Date();
			}
			boolean value=new Date().before(entryend);
			int entrystatus =0;//报名状态（1 可报名，-1 已结束）
			if(value){
				entrystatus=1;
			}else{
				entrystatus=-1;
			}	
			list.get(i).put("entryend", date.format(entryend));
			list.get(i).put("activitystart", date.format(activitystart));
			Map<String, Object> map =list.get(i);
			map.put("entrystatus", entrystatus);
			newlist.add(map);
		}
		
		return newlist;
	}
	
	/**
	 * 获取活动详细信息
	 * @param activityid 活动ID
	 * @return
	 */
	/*@HystrixCommand(fallbackMethod="reliableSelectActivityInfoById")
	@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds" ,value="5000")*/
	public Map<String, Object> selectActivityInfoById(String activityid,String picturesPathName){
		Map<String, Object> info = activityService.selectActivityInfoById(activityid,picturesPathName);
		Date entryend=null;
		try {
			entryend=from.parse(info.get("entryend").toString());
		} catch (ParseException e) {
		}
		boolean value=new Date().before(entryend);
		int entrystatus =0;//报名状态（1 可报名，-1 已结束）
		if(value){
			entrystatus=1;
		}else{
			entrystatus=-1;
		}
		String userid = info.get("userid").toString();
		List<Map<String, Object>> list = activityService.selectActivityAttachmentById(activityid,picturesPathName);
		Map<String, Object> listHeadSculpture = userService.findUserHeadSculpture(userid);
		if(listHeadSculpture != null && listHeadSculpture.size() != 0){
			String url = listHeadSculpture.get("url").toString();
			String type = listHeadSculpture.get("TYPE").toString();
			if(type.equals("1")){
				info.put("url", url);
			}else if(type.equals("2")){
				info.put("url", staticPath + image + url);
			}
		}
		info.put("attachment", list);
		info.put("entrystatus", entrystatus);
		return info;
	}
	
	/**
	 * 容错方法：查询获取活动详细信息
	 * 
	 * @param schoolid
	 * @param roleid
	 * @return
	 */
	public Map<String, Object> reliableSelectActivityInfoById(String activityid,String picturesPathName) {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", "500");
		map.put("value", "查询活动详细信息失败，用户服务器错误！！！");
		list.add(map);
		return map;
	}
	
	
	/**
	 * 添加活动
	 * @param jsonInfo
	 * @return
	 */
	public Map<String,Object> addActivity(String jsonInfo){
		Map<String,Object> resultMap=new  HashMap<String,Object>();
		JSONObject json = JSONArray.parseObject(jsonInfo);
		//添加活动主表
		DcActivity activity = new DcActivity();
		activity.setName(json.getString("name"));
		activity.setLabel(json.getString("label"));
		activity.setBgpathid(json.getString("bgpathid"));
		activity.setSchoolid(json.getString("schoolid"));
		activity.setSchoollabel(json.getString("schoollabel"));
		activity.setUserid(json.getString("userid"));
		activity.setUserlabel(json.getString("userlabel"));
		activity.setWechat(json.getString("wechat"));
		activity.setTelephone(json.getString("telephone"));
		activity.setType(json.getString("type"));
		
		activity.setCreatedate(new Date());
		SimpleDateFormat  datepase= new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		String entrystartStr=json.getString("entrystart");
		String entryend=json.getString("entryend");
		String activitystart=json.getString("activitystart");
		String activityend=json.getString("activityend");
		
		try {
			activity.setEntrystart(datepase.parse(entrystartStr));
			activity.setEntryend(datepase.parse(entryend));
			activity.setActivitystart(datepase.parse(activitystart));
			activity.setActivityend(datepase.parse(activityend));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		activity.setSignupnumber(json.getString("signupnumber"));
		activity.setSignupcost(json.getBigDecimal("signupcost"));
		activity.setAuditstatus(json.getString("auditstatus"));
		activity.setInfo(json.getString("info"));
		activity.setParentid(json.getString("parentid"));
		activity.setStatus(json.getString("status"));
		activity.setSignupcosttype(json.getString("signupcosttype"));
		activity.setClicks(0);
		
		int activityId = activityService.addActivity(activity);
		if(activityId == 0){
			resultMap.put("status", false);
			resultMap.put("info", "添加活动失败");
			return resultMap;
		}
		
		//更新活动图片
		String arrayImage = json.getString("imageid"); // 获取list的值
		JSONArray jsonArrayImage = JSONArray.parseArray(arrayImage); // 把list的值转为json数组对象
		Object[] strImage = jsonArrayImage.toArray(); // json转为数组
		for (Object image : strImage) {
			String attachmentid = image.toString();
			int resultAttachment = activityService.updateActivityAttachmentByID(attachmentid, activityId+"");
			if(resultAttachment == 0){
				activityService.deleteActivityById(activityId+"");
				resultMap.put("status", false);
				resultMap.put("info", "更新活动图片失败");
				return resultMap;
			}
		}
		
		//添加活动报名模板
		String arrayItem = json.getString("item"); // 获取list的值
		JSONArray jsonArrayItem = JSONArray.parseArray(arrayItem); // 把list的值转为json数组对象
		Object[] strItem = jsonArrayItem.toArray(); // json转为数组
		for (Object item : strItem) {
			String fieldid = item.toString();
			DcActivitySignupTemplate activitySignupTemplate = new DcActivitySignupTemplate();
			activitySignupTemplate.setActivityid(activityId+"");
			activitySignupTemplate.setFieldid(fieldid);
			activitySignupTemplate.setStatus("0");
			activitySignupTemplate.setCreatedate(new Date());
			int resultTemplate = activityService.addActivitySignupTemplate(activitySignupTemplate);
			if(resultTemplate == 0){
				activityService.deleteActivityById(activityId+"");
				resultMap.put("status", false);
				resultMap.put("info", "添加活动报名模板失败");
				return resultMap;
			}
		}
		
		//添加活动关联场所
		DcActivityLinkPlace activityLinkPlace = new DcActivityLinkPlace();
		activityLinkPlace.setActivityid(activityId+"");
		activityLinkPlace.setLabel(json.getString("label"));
		activityLinkPlace.setSchoolid(json.getString("schoolid"));
		activityLinkPlace.setUserid(json.getString("userid"));
		activityLinkPlace.setUserlabel(json.getString("userlabel"));
		activityLinkPlace.setPlaceid(json.getString("placeid"));
		activityLinkPlace.setPlacelabel(json.getString("placelabel"));
		activityLinkPlace.setPlaceaddress(json.getString("placeaddress"));
		activityLinkPlace.setStatus("0");
		activityLinkPlace.setCreatedate(new Date());
		int resultPlace = activityService.addActivityLinkPlace(activityLinkPlace);
		if(resultPlace == 0){
			activityService.deleteActivityById(activityId+"");
			resultMap.put("status", false);
			resultMap.put("info", "添加活动关联场所失败");
			return resultMap;
		}
		
		resultMap.put("status", true);
		resultMap.put("info", "添加活动成功");
		return resultMap;
	}
	
	
	/**
	 * 删除活动
	 * @param id
	 * @return
	 */
	public int DeleteActivity(String id) {
		int value = 0;
		try {
			value = PCactivityService.DeleteActivity(id);
		} catch (Exception e) {
			e.printStackTrace();
			value = -1;
		}
		return value;

	}
	
	
	/**
	 * 活动访问量增加
	 * @param activityid:活动ID
	 * @return
	 */
	public int updateActivityClicks(int activityid){
		return activityService.updateActivityClicks(activityid);
	}
	
	/**
	 * 获取晒选项列表
	 * @param itemType ：1 年份  2 月份  3 学堂ID 4 场所ID
	 * @return
	 */
	public Map<String, Object> selectActivityQueryItem(){
		Map<String, Object> info = new HashMap<String, Object>();
		List<Map<String, Object>> yearList = activityService.selectActivityQueryItem(1);
		List<Map<String, Object>> monthList = activityService.selectActivityQueryItem(2);
		List<Map<String, Object>> schoolList = activityService.selectActivityQueryItem(3);
		List<Map<String,Object>>  schoolArray=new ArrayList<Map<String,Object>>();
		//学堂库现在存在的学堂
		List<Map<String,Object>> newSchoolList=schoolserive.showallschool(0,0);
		for (int i = 0; i < newSchoolList.size(); i++) {
			for(int j=0;j<schoolList.size();j++) {
				if(String.valueOf(newSchoolList.get(i).get("id")).equals(String.valueOf(schoolList.get(j).get("schoolid")))) {
					
					schoolArray.add(schoolList.get(j));
				}
				
			}
		}
		
		List<Map<String,Object>>  newplaceList=placeservie.showAllPlace(0, 0);
		List<Map<String, Object>> placeList = activityService.selectActivityQueryItem(4);
		List<Map<String,Object>> placeArray=new  ArrayList<Map<String,Object>>();
		//当前存在的场所
	
		for (int i = 0; i < newplaceList.size(); i++) {
			for(int j=0;j<placeList.size();j++) {
				if(String.valueOf(newplaceList.get(i).get("id")).equals(String.valueOf(placeList.get(j).get("placeid")))) {
					
					placeArray.add(placeList.get(j));
				}
				
			}
		}
		
		
		info.put("yearList", yearList);
		info.put("monthList", monthList);
		info.put("schoolList", schoolArray);
		info.put("placeList", placeArray);
		return info;
	}
	
	
	public Object SelectEstablishActivity(String userid,int CurrPageNo, int pageSize){
		List<Map<String, Object>> list = activityService.SelectEstablishActivity(userid, CurrPageNo, pageSize);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).put("path", staticPath+image+list.get(i).get("path"));
		}
		return JSON.toJSON(list);
		
	}
	
	/**
	 * 查询活动报名项
	 * @return
	 */
	public List<Map<String, Object>> selectActivitySignupField(){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list = activityService.selectActivitySignupField();
		return list;
	}
	
	public Object addimage(MultipartFile file) throws SocketException {
		SimpleDateFormat from1 = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> mapUrl = new HashMap<String, Object>();
		DcActivityAttachment activityAttachment = new DcActivityAttachment();
		String ip = linuxip.getLinuxLocalIp();
		System.out.println("LinuxIp:" + ip);
		int value = 0;
		if (!file.isEmpty()) {
			int Financeincreasekeyid = activityIncreasekeyService.selectKey("dc_activity_attachment");
			if (Financeincreasekeyid <= 0) {
				String id = activityAttachmentService.selectMaxId();
				int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_attachment",
						Integer.parseInt(id));
				if (increasekeyvalue > 0) {
					Financeincreasekeyid = activityIncreasekeyService.selectKey("dc_activity_attachment");
				} else {
					return -1;
				}
			}
			Map<String, Object> map = images.uploadingFile(file, 0);
			System.out.println(map.get("Route"));

			activityAttachment.setCreatedate(new Date());
			activityAttachment.setSid(Financeincreasekeyid + 1);
			activityAttachment.setId(String.valueOf(Financeincreasekeyid + 1));
			activityAttachment.setCode(String.valueOf(Financeincreasekeyid + 1));
			activityAttachment.setLabel(map.get("filename").toString());
			activityAttachment
					.setPath("/" + from1.format(activityAttachment.getCreatedate()) + "/" + map.get("name").toString());
			activityAttachment.setSize(String.valueOf(file.getSize() / 1024));
			activityAttachment.setStatus("0");
			System.out.println("url" + activityAttachment.getPath());
			if ((boolean) map.get("boolean")) {
				value = activityAttachmentService.addAttachment(SerializeUtil.serialize(activityAttachment));
				if (value > 0) {
					int add = activityIncreasekeyService.addIncreasekey("dc_activity_attachment",
							Integer.parseInt(activityAttachment.getId()));
				}
				mapUrl.put("value", value);
				mapUrl.put("id", activityAttachment.getId());
				mapUrl.put("url", url + image + activityAttachment.getPath());

			}

		} else {
			value = -1;
			mapUrl.put("value", value);
		}
		mapUrl.put("value", value);
		return JSON.toJSON(mapUrl);
	}
	
	
	public Object addMainimage(MultipartFile file) throws SocketException {
		SimpleDateFormat from1 = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> mapUrl = new HashMap<String, Object>();
		DcActivityAttachment activityAttachment = new DcActivityAttachment();
		String ip = linuxip.getLinuxLocalIp();
		System.out.println("LinuxIp:" + ip);
		int value = 0;
		if (!file.isEmpty()) {
			int Financeincreasekeyid = activityIncreasekeyService.selectKey("dc_activity_attachment");
			if (Financeincreasekeyid <= 0) {
				String id = activityAttachmentService.selectMaxId();
				int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_attachment",
						Integer.parseInt(id));
				if (increasekeyvalue > 0) {
					Financeincreasekeyid = activityIncreasekeyService.selectKey("dc_activity_attachment");
				} else {
					return -1;
				}
			}
			Map<String, Object> map = images.uploadingFile(file, 0);
			System.out.println(map.get("Route"));

			activityAttachment.setCreatedate(new Date());
			activityAttachment.setSid(Financeincreasekeyid + 1);
			activityAttachment.setId(String.valueOf(Financeincreasekeyid + 1));
			activityAttachment.setCode(String.valueOf(Financeincreasekeyid + 1));
			activityAttachment.setLabel(map.get("filename").toString());
			activityAttachment
					.setPath("/" + from1.format(activityAttachment.getCreatedate()) + "/" + map.get("name").toString());
			activityAttachment.setSize(String.valueOf(file.getSize() / 1024));
			activityAttachment.setStatus("0");
			System.out.println("url" + activityAttachment.getPath());
			if ((boolean) map.get("boolean")) {
				value = activityAttachmentService.addAttachment(SerializeUtil.serialize(activityAttachment));
				if (value > 0) {
					int add = activityIncreasekeyService.addIncreasekey("dc_activity_attachment",
							Integer.parseInt(activityAttachment.getId()));
				}
				mapUrl.put("value", value);
				mapUrl.put("bgpathid", activityAttachment.getId());
				mapUrl.put("url", url + image + activityAttachment.getPath());

			}

		} else {
			value = -1;
			mapUrl.put("value", value);
		}
		mapUrl.put("value", value);
		return JSON.toJSON(mapUrl);
	}
	
	
}
