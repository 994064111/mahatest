package com.deco.activityservice.pc;

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
import com.deco.entity.Page;
import com.deco.service.pc.ActivityAttachmentService;
import com.deco.service.pc.ActivityIncreasekeyService;
import com.deco.service.pc.ActivityLinkPlaceService;
import com.deco.service.pc.ActivityService;
import com.deco.service.pc.UserIncreasekeyService;
import com.deco.service.pc.UserService;
import com.deco.service.pc.MessageService;
import com.deco.service.pc.SchoolService;
import com.deco.service.pc.SignupFieldService;
import com.deco.service.pc.SignupService;
import com.deco.service.pc.SignupTemplateService;
import com.deco.utils.ActivityFileUtil;
import com.deco.utils.GetLinuxIpUtil;
import com.deco.utils.SerializeUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Component
public class ActivityConsumer {

	@Autowired
	private ActivityService activityService;
	@Autowired
	private SignupService signupService;
	@Autowired
	private SignupFieldService signupFieldService;
	@Autowired
	private ActivityAttachmentService activityAttachmentService;
	@Autowired
	private ActivityIncreasekeyService activityIncreasekeyService;
	@Autowired
	private SchoolService schoolService;
	@Autowired
	private SignupTemplateService signupTemplateService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private UserIncreasekeyService userIncreasekeyService;
	@Autowired
	private UserService userservice;
	@Autowired
	private ActivityLinkPlaceService activityLinkPlaceService;

	@Value("${file.web-path}")
	String url;
	@Value("${file.image}")
	String image;
	@Value("${file.audio}")
	String audio;
	@Value("${file.video}")
	String video;
	@Autowired
	ActivityFileUtil images;
	SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	GetLinuxIpUtil linuxip = new GetLinuxIpUtil();

	public Object showActivity(int CurrPageNo, int pageSize) throws ParseException {
		//SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
		Page page = new Page();
		int totalCount = Integer.parseInt(activityService.showActivityCount().get(0).get("count").toString());
		page.setTotalCount(totalCount);
		int a = page.getCurrPageNo();
		if (CurrPageNo == 0) {
			CurrPageNo = a;
		} else {
			page.setCurrPageNo(CurrPageNo);
		}
		if (pageSize == 0) {
			pageSize = page.getPageSize();
		} else {
			page.setPageSize(pageSize);
		}
		List<Map<String, Object>> list = new ArrayList<>();
		List<Map<String, Object>> activity = activityService.showActivity(CurrPageNo, pageSize);
		for (int i = 0; i < activity.size(); i++) {
			if (!activity.get(i).get("status").toString().equals("2")) {
				if (new Date().after(from.parse(activity.get(i).get("activityend").toString()))) {
					int value = activityService.updateActivityStatus(activity.get(i).get("id").toString());
					System.out.println("显示活动前是否过期：" + value);
				}
			}
		}
		Map<String, Object> allmap = new HashMap<String, Object>();
		List<Map<String, Object>> activitylist = activityService.showActivity(CurrPageNo, pageSize);
		for (int i = 0; i < activitylist.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", activitylist.get(i).get("id"));
			map.put("label", activitylist.get(i).get("label"));
			map.put("schoolid", activitylist.get(i).get("schoolid"));
			map.put("schoollabel", activitylist.get(i).get("schoollabel"));
			map.put("userid", activitylist.get(i).get("userid"));
			map.put("userlabel", activitylist.get(i).get("userlabel"));
			map.put("wechat", activitylist.get(i).get("wechat"));
			map.put("telephone", activitylist.get(i).get("telephone"));
			map.put("entrystart", from.format(activitylist.get(i).get("entrystart")));
			map.put("entryend", from.format(activitylist.get(i).get("entryend")));
			map.put("activitystart", from.format(activitylist.get(i).get("activitystart")));
			map.put("activityend", from.format(activitylist.get(i).get("activityend")));
			map.put("status", activitylist.get(i).get("status"));
			List<Map<String, Object>> Placelist = activityLinkPlaceService
					.showLinkPlace(activitylist.get(i).get("id").toString());
			if (Placelist.size() > 0) {
				map.put("placeaddress", Placelist.get(0).get("placeaddress"));
			} else {
				map.put("placeaddress", null);
			}
			list.add(map);
		}
		Map<String, Object> pagemap = new HashMap<String, Object>();

		pagemap.put("totalPageCount", page.getTotalPageCount());// 总页数
		pagemap.put("pageSize", page.getPageSize());// 每页显示条数
		pagemap.put("totalCount", page.getTotalCount());// 总记录数
		pagemap.put("currPageNo", page.getCurrPageNo());// 当前页数
		allmap.put("list", list);
		allmap.put("pagemap", pagemap);
		return JSON.toJSON(allmap);
	}

	public Object showSearchActivity(String label, String activitystart, String status, int CurrPageNo, int pageSize) {
		SimpleDateFormat from1 = new SimpleDateFormat("yyyy-MM-dd");
		String activitystart1 = "";
		try {
			activitystart1 = from1.format(from1.parse(activitystart));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> allmap = new HashMap<String, Object>();
		Page page = new Page();
		int totalCount = Integer.parseInt(
				activityService.showSearchActivityCount(label, activitystart1, status).get(0).get("count").toString());
		page.setTotalCount(totalCount);
		int a = page.getCurrPageNo();
		if (CurrPageNo == 0) {
			CurrPageNo = a;
		} else {
			page.setCurrPageNo(CurrPageNo);
		}
		if (pageSize == 0) {
			pageSize = page.getPageSize();
		} else {
			page.setPageSize(pageSize);
		}
		List<Map<String, Object>> SearchActivitylist = activityService.showSearchActivity(label, activitystart1, status,
				CurrPageNo, pageSize);
		for (int i = 0; i < SearchActivitylist.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", SearchActivitylist.get(i).get("id"));
			map.put("label", SearchActivitylist.get(i).get("label"));
			map.put("schoolid", SearchActivitylist.get(i).get("schoolid"));
			map.put("schoollabel", SearchActivitylist.get(i).get("schoollabel"));
			map.put("userid", SearchActivitylist.get(i).get("userid"));
			map.put("userlabel", SearchActivitylist.get(i).get("userlabel"));
			map.put("wechat", SearchActivitylist.get(i).get("wechat"));
			map.put("telephone", SearchActivitylist.get(i).get("telephone"));
			map.put("entrystart", from.format(SearchActivitylist.get(i).get("entrystart")));
			map.put("entryend", from.format(SearchActivitylist.get(i).get("entryend")));
			map.put("activitystart", from.format(SearchActivitylist.get(i).get("activitystart")));
			map.put("activityend", from.format(SearchActivitylist.get(i).get("activityend")));
			map.put("status", SearchActivitylist.get(i).get("status"));
			List<Map<String, Object>> Placelist = activityLinkPlaceService
					.showLinkPlace(SearchActivitylist.get(i).get("id").toString());
			map.put("placeaddress", Placelist.get(0).get("placeaddress"));
			list.add(map);
		}
		Map<String, Object> pagemap = new HashMap<String, Object>();

		pagemap.put("totalPageCount", page.getTotalPageCount());// 总页数
		pagemap.put("pageSize", page.getPageSize());// 每页显示条数
		pagemap.put("totalCount", page.getTotalCount());// 总记录数
		pagemap.put("currPageNo", page.getCurrPageNo());// 当前页数
		allmap.put("list", list);
		allmap.put("pagemap", pagemap);
		return JSON.toJSON(allmap);
	}

	public int DeleteActivity(String id) {
		int value = 0;
		try {
			value = activityService.DeleteActivity(id);
		} catch (Exception e) {
			e.printStackTrace();
			value = -1;
		}
		return value;

	}

	public Object showActivityDetails(String id) throws SocketException {
		Map<String, Object> map = new HashMap<String, Object>();
		String ip = linuxip.getLinuxLocalIp();
		List<Map<String, Object>> activity = new ArrayList<>();
		List<Map<String, Object>> activitylist = activityService.showActivityDetails(id);
		for (int i = 0; i < activitylist.size(); i++) {
			Map<String, Object> activitymap = new HashMap<String, Object>();
			activitymap.put("id", activitylist.get(i).get("id"));
			activitymap.put("label", activitylist.get(i).get("label"));
			activitymap.put("schoolid", activitylist.get(i).get("schoolid"));
			activitymap.put("bgpathid", activitylist.get(i).get("bgpathid"));
			activitymap.put("schoollabel", activitylist.get(i).get("schoollabel"));
			activitymap.put("userid", activitylist.get(i).get("userid"));
			activitymap.put("userlabel", activitylist.get(i).get("userlabel"));
			activitymap.put("wechat", activitylist.get(i).get("wechat"));
			activitymap.put("telephone", activitylist.get(i).get("telephone"));
			activitymap.put("type", activitylist.get(i).get("type"));
			activitymap.put("createdate", activitylist.get(i).get("createdate"));
			activitymap.put("entrystart", from.format(activitylist.get(i).get("entrystart")));
			activitymap.put("entryend", from.format(activitylist.get(i).get("entryend")));
			activitymap.put("activitystart", from.format(activitylist.get(i).get("activitystart")));
			activitymap.put("activityend", from.format(activitylist.get(i).get("activityend")));
			activitymap.put("signupnumber", activitylist.get(i).get("signupnumber"));
			activitymap.put("signupcost", activitylist.get(i).get("signupcost"));
			activitymap.put("auditstatus", activitylist.get(i).get("auditstatus"));
			activitymap.put("info", activitylist.get(i).get("info"));
			activitymap.put("status", activitylist.get(i).get("status"));
			List<Map<String, Object>> Placelist = activityLinkPlaceService
					.showLinkPlace(activitylist.get(i).get("id").toString());
			map.put("placeid", Placelist.get(0).get("placeid"));
			map.put("placelabel", Placelist.get(0).get("placelabel"));
			map.put("placeaddress", Placelist.get(0).get("placeaddress"));
			activity.add(activitymap);
		}
		map.put("activitylist", activity);// 获取活动详情
		List<Map<String, Object>> signupTemplatelist = signupTemplateService.showSignupTemplate(id);
		List<Map<String, Object>> signupField = new ArrayList<>();
		for (int i = 0; i < signupTemplatelist.size(); i++) {
			String fieldid = signupTemplatelist.get(i).get("fieldid").toString();
			List<Map<String, Object>> signupFieldlist = signupFieldService.showSigmupField(fieldid);
			for (int j = 0; j < signupFieldlist.size(); j++) {
				Map<String, Object> signupFielmap = new HashMap<String, Object>();
				signupFielmap.put("label", signupFieldlist.get(j).get("label"));
				signupFielmap.put("name", signupFieldlist.get(j).get("name"));
				signupFielmap.put("id", signupFieldlist.get(j).get("id"));
				signupField.add(signupFielmap);
			}
		}
		map.put("signupFieldlist", signupField);// 获取报名项
		List<Map<String, Object>> signup = new ArrayList<>();
		List<Map<String, Object>> signupuserlist = signupService.selectSignup(id);
		List<Map<String, Object>> signuplist = signupService.showSignup(id, signupField);
		if (signuplist.size() > 0) {
			for (int i = 0; i < signuplist.size(); i++) {
				Map<String, Object> signupmap = new HashMap<String, Object>();
				for (int j = 0; j < signupField.size(); j++) {
					signupmap.put(signupField.get(j).get("name").toString(),
							signuplist.get(i).get(signupField.get(j).get("name").toString()));
				}
				signupmap.put("id", signuplist.get(i).get("id"));
				signupmap.put("status", signuplist.get(i).get("status"));
				signupmap.put("parentid", signuplist.get(i).get("parentid"));
				for (int j = 0; j < signupuserlist.size(); j++) {
					if (signuplist.get(i).get("parentid").toString()
							.equals(signupuserlist.get(j).get("id").toString())) {
						signupmap.put("userid", signupuserlist.get(j).get("userid"));
						List<Map<String, Object>> userlabellist = userservice
								.selectUserActivitySignUp(signupuserlist.get(j).get("userid").toString());
						if (userlabellist.size() > 0) {
							signupmap.put("nickname", userlabellist.get(0).get("label"));
						} else {
							signupmap.put("nickname", null);
						}
						signupmap.put("status", signupuserlist.get(j).get("status"));
						signup.add(signupmap);
					}
				}
			}
		}
		map.put("signuplist", signup);// 获取用户

		List<Map<String, Object>> activityAttachment = new ArrayList<>();
		List<Map<String, Object>> activityAttachmentlist = activityAttachmentService.showAttachment(id);
		for (int i = 0; i < activityAttachmentlist.size(); i++) {
			Map<String, Object> activityAttachmentmap = new HashMap<String, Object>();
			activityAttachmentmap.put("id", activityAttachmentlist.get(i).get("id"));
			activityAttachmentmap.put("label", activityAttachmentlist.get(i).get("label"));
			activityAttachmentmap.put("path", url + image + activityAttachmentlist.get(i).get("path"));
			activityAttachmentmap.put("size", activityAttachmentlist.get(i).get("size"));
			activityAttachment.add(activityAttachmentmap);
		}
		map.put("activityAttachment", activityAttachment);// 获取活动图片
		String bgpathid = "";
		if (activitylist.get(0).get("bgpathid") != null) {
			bgpathid = activitylist.get(0).get("bgpathid").toString();
		} else {
			bgpathid = "0";
		}
		List<Map<String, Object>> activityMainAttachmentlist = activityAttachmentService.showAttachmentBgpath(bgpathid);
		String bgpath = "";
		if (activityMainAttachmentlist.size()>0) {
			bgpath = activityMainAttachmentlist.get(0).get("path").toString();
			map.put("bgpath", url + image + bgpath);
		} else {
			map.put("bgpath", null);
		}

		return JSON.toJSON(map);

	}

	public Object showUpdateActivity(String id) throws SocketException {
		Map<String, Object> map = new HashMap<String, Object>();
		String ip = linuxip.getLinuxLocalIp();
		List<Map<String, Object>> activity = new ArrayList<>();
		List<Map<String, Object>> activitylist = activityService.showActivityDetails(id);
		for (int i = 0; i < activitylist.size(); i++) {
			Map<String, Object> activitymap = new HashMap<String, Object>();
			activitymap.put("id", activitylist.get(i).get("id"));
			activitymap.put("label", activitylist.get(i).get("label"));
			activitymap.put("schoolid", activitylist.get(i).get("schoolid"));
			activitymap.put("schoollabel", activitylist.get(i).get("schoollabel"));
			activitymap.put("bgpathid", activitylist.get(i).get("bgpathid"));
			activitymap.put("userid", activitylist.get(i).get("userid"));
			activitymap.put("userlabel", activitylist.get(i).get("userlabel"));
			activitymap.put("wechat", activitylist.get(i).get("wechat"));
			activitymap.put("telephone", activitylist.get(i).get("telephone"));
			activitymap.put("type", activitylist.get(i).get("type"));
			activitymap.put("createdate", activitylist.get(i).get("createdate"));
			activitymap.put("entrystart", from.format(activitylist.get(i).get("entrystart")));
			activitymap.put("entryend", from.format(activitylist.get(i).get("entryend")));
			activitymap.put("activitystart", from.format(activitylist.get(i).get("activitystart")));
			activitymap.put("activityend", from.format(activitylist.get(i).get("activityend")));
			activitymap.put("signupnumber", activitylist.get(i).get("signupnumber"));
			activitymap.put("signupcost", activitylist.get(i).get("signupcost"));
			activitymap.put("auditstatus", activitylist.get(i).get("auditstatus"));
			activitymap.put("info", activitylist.get(i).get("info"));
			activitymap.put("status", activitylist.get(i).get("status"));
			activitymap.put("signupcosttype", activitylist.get(i).get("signupcosttype"));
			List<Map<String, Object>> Placelist = activityLinkPlaceService
					.showLinkPlace(activitylist.get(i).get("id").toString());
			map.put("placeid", Placelist.get(0).get("placeid"));
			map.put("placelabel", Placelist.get(0).get("placelabel"));
			map.put("placeaddress", Placelist.get(0).get("placeaddress"));
			activity.add(activitymap);
		}
		map.put("activitylist", activity);
		List<Map<String, Object>> activityAttachment = new ArrayList<>();
		List<Map<String, Object>> activityAttachmentlist = activityAttachmentService.showAttachment(id);
		for (int i = 0; i < activityAttachmentlist.size(); i++) {
			Map<String, Object> activityAttachmentmap = new HashMap<String, Object>();
			activityAttachmentmap.put("id", activityAttachmentlist.get(i).get("id"));
			activityAttachmentmap.put("label", activityAttachmentlist.get(i).get("label"));
			activityAttachmentmap.put("path", url + image + activityAttachmentlist.get(i).get("path"));
			activityAttachmentmap.put("size", activityAttachmentlist.get(i).get("size"));
			activityAttachment.add(activityAttachmentmap);
		}
		map.put("activityAttachmentlist", activityAttachment);
		List<Map<String, Object>> signupTemplatelist = signupTemplateService.showSignupTemplate(id);
		List<Object> signupField = new ArrayList<>();
		for (int i = 0; i < signupTemplatelist.size(); i++) {
			String fieldid = signupTemplatelist.get(i).get("fieldid").toString();
			List<Map<String, Object>> signupFieldlist = signupFieldService.showSigmupField(fieldid);
			for (int j = 0; j < signupFieldlist.size(); j++) {
				signupField.add(signupFieldlist.get(j).get("id"));
			}
		}
		List<Map<String, Object>> activityMainAttachmentlist = activityAttachmentService
				.showAttachmentBgpath(activitylist.get(0).get("bgpathid").toString());
		String bgpath = activityMainAttachmentlist.get(0).get("path").toString();
		map.put("bgpath", url + image + bgpath);
		map.put("fieldid", signupField);
		return JSON.toJSON(map);
	}

	public int UpdateActivity(String JSONActivity) {
		int accountvalue = 0;
		DcActivity activity = new DcActivity();
		JSONObject json = JSONArray.parseObject(JSONActivity);
		activity.setId(json.getString("id"));
		activity.setLabel(json.getString("label"));
		activity.setName(json.getString("label"));
		activity.setSchoolid(json.getString("schoolid"));
		activity.setSchoollabel(json.getString("schoollabel"));
		activity.setUserid(json.getString("userid"));
		activity.setBgpathid(json.getString("bgpathid"));
		activity.setUserlabel(json.getString("userlabel"));
		activity.setWechat(json.getString("wechat"));
		activity.setTelephone(json.getString("telephone"));
		activity.setType(json.getString("type"));
		activity.setCreatedate(new Date());
		activity.setEntrystart(json.getDate("entrystart"));
		activity.setEntryend(json.getDate("entryend"));
		activity.setActivitystart(json.getDate("activitystart"));
		activity.setActivityend(json.getDate("activityend"));
		activity.setSignupnumber(json.getString("signupnumber"));
		activity.setSignupcost(json.getBigDecimal("signupcost"));
		activity.setAuditstatus(json.getString("auditstatus"));
		activity.setInfo(json.getString("info"));
		activity.setStatus(json.getString("status"));
		List<Object> itemlist = new ArrayList<>();
		String array1 = json.getString("item"); // 获取list的值
		JSONArray jsonArray1 = JSONArray.parseArray(array1); // 把list的值转为json数组对象
		Object[] strs1 = jsonArray1.toArray(); // json转为数组
		for (Object s1 : strs1) {
			String videoid1 = s1.toString();
			itemlist.add(videoid1);

		}
		String array = json.getString("imageid"); // 获取list的值
		JSONArray jsonArray = JSONArray.parseArray(array); // 把list的值转为json数组对象
		Object[] strs = jsonArray.toArray(); // json转为数组
		for (Object s : strs) {
			String videoid1 = s.toString();
			int a= activityAttachmentService.updateAttachment(activity.getId(), videoid1);
		}
		int delete = signupTemplateService.deleteSignupTemplate(activity.getId());
		String id = signupTemplateService.selectMaxId();
		if (delete > 0) {
			int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_signup_template",
					Integer.parseInt(id));
			if (increasekeyvalue > 0) {
				System.out.println("dc_activity_signup_template主键更新");
			}
		}
		for (int i = 0; i < itemlist.size(); i++) {
			int templateIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity_signup_template");
			if (templateIncreasekeyid <= 0) {
				String signupTemplateid = signupTemplateService.selectMaxId();
				int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_signup_template",
						Integer.parseInt(signupTemplateid));
				if (increasekeyvalue > 0) {
					templateIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity_signup_template");
				} else {
					return -1;
				}
			}
			DcActivitySignupTemplate activitySignupTemplate = new DcActivitySignupTemplate();
			activitySignupTemplate.setSid(templateIncreasekeyid + 1);
			activitySignupTemplate.setId(String.valueOf(templateIncreasekeyid + 1));
			activitySignupTemplate.setCode(String.valueOf(templateIncreasekeyid + 1));
			activitySignupTemplate.setActivityid(activity.getId());
			activitySignupTemplate.setFieldid(itemlist.get(i).toString()); 
			activitySignupTemplate.setStatus("0");
			activitySignupTemplate.setCreatedate(new Date());
			int add = signupTemplateService.addSignupTemplate(SerializeUtil.serialize(activitySignupTemplate));
			if (add < 0) {
				accountvalue = -1;
			} else {
				int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_signup_template",
						activitySignupTemplate.getSid());
				if (increasekeyvalue > 0) {
					System.out.println("dc_activity_signup_template主键更新");
				}
			}
		}
		
		
		//修改场所
		int o=activityService.updateActivityPlace(json.getString("id"), json.getString("placeid"), json.getString("placelabel"), json.getString("placeaddress"));
		
		int value = activityService.updateAcivity(SerializeUtil.serialize(activity));
		if (value > 0) {
			accountvalue = 1;

		} else {
			accountvalue = -1;
		}

		return accountvalue;

	}

	public int deleteimage(String id) {
		int value = 0;
		try {
			value = activityAttachmentService.deleteAttachment(id);
		} catch (Exception e) {
			e.printStackTrace();
			value = -1;
		}
		return value;

	}

	public Object updateimage(MultipartFile file, String activityid) throws SocketException {
		SimpleDateFormat from1 = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> mapUrl = new HashMap<String, Object>();
		DcActivityAttachment activityAttachment = new DcActivityAttachment();
		String ip = linuxip.getLinuxLocalIp();
		System.out.println("LinuxIp:" + ip);
		int value = 0;
		if (!file.isEmpty()) {
			int Financeincreasekeyid = activityIncreasekeyService.selectKey("dc_finance_attachment");
			if (Financeincreasekeyid <= 0) {
				String id = activityAttachmentService.selectMaxId();
				int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_finance_attachment",
						Integer.parseInt(id));
				if (increasekeyvalue > 0) {
					Financeincreasekeyid = activityIncreasekeyService.selectKey("dc_finance_attachment");
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
			activityAttachment.setParentid(activityid);
			System.out.println("url" + activityAttachment.getPath());
			if ((boolean) map.get("boolean")) {
				value = activityAttachmentService.addAttachment(SerializeUtil.serialize(activityAttachment));
				activityIncreasekeyService.addIncreasekey("dc_finance_attachment",
						Integer.parseInt(activityAttachment.getId()));
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

	@HystrixCommand(fallbackMethod = "reliableshowAllSchool")
	public Object showAllSchool() {
		List<Map<String, Object>> list = new ArrayList<>();
		List<Map<String, Object>> schoollist = schoolService.showallschool(0, 0);
		for (int i = 0; i < schoollist.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("schoolid", schoollist.get(i).get("id"));
			map.put("schoollabel", schoollist.get(i).get("label"));
			map.put("status", "200");
			list.add(map);
		}
		return JSON.toJSON(list);

	}

	public Object reliableshowAllSchool() {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolid", null);
		map.put("schoollabel", null);
		map.put("status", "500");
		map.put("value", "获取学堂失败，学堂服务器错误！！！");
		list.add(map);
		return JSON.toJSON(list);

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
	
	
	
	public Object images(MultipartFile file) throws SocketException {
		SimpleDateFormat from1 = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> mapUrl = new HashMap<String, Object>();
		DcActivityAttachment activityAttachment = new DcActivityAttachment();
		String ip = linuxip.getLinuxLocalIp();
		int value = 0;
		System.out.println("LinuxIp:" + ip);
			Map<String, Object> map = images.uploadingFile(file, 0);
			System.out.println(map.get("Route"));

			activityAttachment.setCreatedate(new Date());
			
			activityAttachment.setLabel(map.get("filename").toString());
			activityAttachment
					.setPath("/" + from1.format(activityAttachment.getCreatedate()) + "/" + map.get("name").toString());
			activityAttachment.setSize(String.valueOf(file.getSize() / 1024));
			activityAttachment.setStatus("0");
			System.out.println("url" + activityAttachment.getPath());
			if ((boolean) map.get("boolean")) {
				
				mapUrl.put("value", 1);
				mapUrl.put("id", activityAttachment.getId());
				mapUrl.put("url", url + image + activityAttachment.getPath());

					
			} else {
			value = -1;
			mapUrl.put("value", value);
		}
		
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

	public int addActivity(String JSONactivity) {
		List<Map<String, Object>> imagelist = new ArrayList<>();
		List<Map<String, Object>> itemlist = new ArrayList<>();
		int activityIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity");
		int accountvalue = 0;
		if (activityIncreasekeyid <= 0) {
			String id = activityService.selectMaxId();
			int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity", Integer.parseInt(id));
			if (increasekeyvalue > 0) {
				activityIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity");
			} else {
				return -1;
			}
		}
		DcActivity activity = new DcActivity();
		JSONObject json = JSONArray.parseObject(JSONactivity);
		activity.setSid(activityIncreasekeyid + 1);
		activity.setId(String.valueOf(activityIncreasekeyid + 1));
		//排序字段
		activity.setSort(activityIncreasekeyid + 1);
		activity.setCode(String.valueOf(activityIncreasekeyid + 1));
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
		activity.setEntrystart(json.getDate("entrystart"));
		activity.setEntryend(json.getDate("entryend"));
		activity.setActivitystart(json.getDate("activitystart"));
		activity.setActivityend(json.getDate("activityend"));
		activity.setSignupnumber(json.getString("signupnumber"));
		activity.setSignupcost(json.getBigDecimal("signupcost"));
		activity.setAuditstatus(json.getString("auditstatus"));
		activity.setInfo(json.getString("info"));
		activity.setParentid(json.getString("Parentid"));
		activity.setStatus(json.getString("status"));
		activity.setSignupcosttype(json.getString("signupcosttype"));
		activity.setClicks(0);
		/*
		 * List<Map<String, Object>> activitylist=
		 * activityService.showActivity(); for (int i = 0; i <
		 * activitylist.size(); i++) {
		 * if(json.getString("label").equals(activitylist.get(i).get("label"))){
		 * return -2; } }
		 */
		try {
			String array = json.getString("imageid"); // 获取list的值
			JSONArray jsonArray = JSONArray.parseArray(array); // 把list的值转为json数组对象
			Object[] strs = jsonArray.toArray(); // json转为数组
			for (Object s : strs) {
				Map<String, Object> imagemap = new HashMap<String, Object>();
				String videoid = s.toString();
				imagemap.put("imageid", videoid);
				imagelist.add(imagemap);
				// value +=
				// activityAttachmentService.updateAttachment(activity.getId(),
				// videoid);
			}

			String array1 = json.getString("item"); // 获取list的值
			JSONArray jsonArray1 = JSONArray.parseArray(array1); // 把list的值转为json数组对象
			Object[] strs1 = jsonArray1.toArray(); // json转为数组
			for (Object s1 : strs1) {
				Map<String, Object> itemmap = new HashMap<String, Object>();
				String videoid1 = s1.toString();
				itemmap.put("itemid", videoid1);
				itemlist.add(itemmap);

			}

			for (int i = 0; i < imagelist.size(); i++) {
				accountvalue += activityAttachmentService.updateAttachment(activity.getId(),
						imagelist.get(i).get("imageid").toString());
			}
			for (int i = 0; i < itemlist.size(); i++) {
				int templateIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity_signup_template");

				int templatevalue = 0;
				if (templateIncreasekeyid <= 0) {
					String id = signupTemplateService.selectMaxId();
					int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_signup_template",
							Integer.parseInt(id));
					if (increasekeyvalue > 0) {
						templateIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity_signup_template");
					} else {
						return -1;
					}
				}
				// 添加报名模板
				DcActivitySignupTemplate activitySignupTemplate = new DcActivitySignupTemplate();
				activitySignupTemplate.setSid(templateIncreasekeyid + 1);
				activitySignupTemplate.setId(String.valueOf(templateIncreasekeyid + 1));
				activitySignupTemplate.setCode(String.valueOf(templateIncreasekeyid + 1));
				activitySignupTemplate.setActivityid(activity.getId());
				activitySignupTemplate.setFieldid(itemlist.get(i).get("itemid").toString());
				activitySignupTemplate.setStatus("0");
				activitySignupTemplate.setCreatedate(new Date());
				templatevalue += signupTemplateService
						.addSignupTemplate(SerializeUtil.serialize(activitySignupTemplate));
				if (templatevalue > 0) {
					int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_signup_template",
							activitySignupTemplate.getSid());
					if (increasekeyvalue > 0) {
						accountvalue += 1;
					}
				} else {
					return -1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			accountvalue = -1;
		}
		int placeIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity_link_place");
		if (placeIncreasekeyid <= 0) {
			String id = activityLinkPlaceService.selectMaxId();
			int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_link_place",
					Integer.parseInt(id));
			if (increasekeyvalue > 0) {
				placeIncreasekeyid = activityIncreasekeyService.selectKey("dc_activity_link_place");
			} else {
				return -1;
			}
		}
		DcActivityLinkPlace activityLinkPlace = new DcActivityLinkPlace();
		activityLinkPlace.setSid(placeIncreasekeyid + 1);
		activityLinkPlace.setId(String.valueOf(placeIncreasekeyid + 1));
		activityLinkPlace.setCode(String.valueOf(placeIncreasekeyid + 1));
		activityLinkPlace.setLabel(json.getString("label"));
		activityLinkPlace.setActivityid(activity.getId());
		activityLinkPlace.setSchoolid(json.getString("schoolid"));
		activityLinkPlace.setUserid(json.getString("userid"));
		activityLinkPlace.setUserlabel(json.getString("userlabel"));
		activityLinkPlace.setCreatedate(new Date());
		activityLinkPlace.setPlaceid(json.getString("placeid"));
		activityLinkPlace.setPlacelabel(json.getString("placelabel"));
		activityLinkPlace.setPlaceaddress(json.getString("placeaddress"));
		activityLinkPlace.setStatus("0");
		
		//活动场所关联关系 
		List<Map<String, Object>> activityPlaceList = activityLinkPlaceService.showLinkPlace(activity.getId());
		int addPlace = 0;
		//判断活动场所是否已经存在 若存在则更新  若不存在则添加 
		if(activityPlaceList.size() > 0){
			addPlace = activityService.updateActivityPlace(activity.getId(), json.getString("placeid"), json.getString("placelabel"), json.getString("placeaddress"));
		}else{
			addPlace = activityLinkPlaceService.addActivityLinkPlace(SerializeUtil.serialize(activityLinkPlace));
		}

		
		if (addPlace > 0) {
			System.out.println("添加活动的场所成功。");
			int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity_link_place",
					activityLinkPlace.getSid());
			if (increasekeyvalue > 0) {
				accountvalue += 1;
			}
		}
		if (accountvalue > 1) {
			accountvalue += activityService.addActivity(SerializeUtil.serialize(activity));
			int increasekeyvalue = activityIncreasekeyService.addIncreasekey("dc_activity", activity.getSid());
			if (increasekeyvalue > 0) {
				System.out.println("dc_activity表主键更新");
			}
			accountvalue = 1;
		}
		return accountvalue;

	}

	@HystrixCommand(fallbackMethod = "reliableupdateuserSignup")
	public int updateuserSignup(String status, String Parentid, String schoolid, String info, String userid) {
		int value = 0;
		try {

			int messageIncreasekeyid = userIncreasekeyService.selectKey("dc_message");
			if (messageIncreasekeyid <= 0) {
				String id = messageService.selectMaxId();
				int increasekeyvalue = userIncreasekeyService.addIncreasekey("dc_message", Integer.parseInt(id));
				if (increasekeyvalue > 0) {
					messageIncreasekeyid = userIncreasekeyService.selectKey("dc_message");
				} else {
					return -1;
				}
			}
			DcMessage message = new DcMessage();
			message.setSid(messageIncreasekeyid + 1);
			message.setId(String.valueOf(messageIncreasekeyid + 1));
			message.setCode(String.valueOf(messageIncreasekeyid + 1));
			message.setInfo(info);
			message.setSchoolid(schoolid);
			message.setStatus("2");
			message.setType("2");
			message.setUserid(userid);
			System.out.println(userid);
			message.setOperate("0");
			message.setCreatedate(new Date());
			int value1 = messageService.addMessage(SerializeUtil.serialize(message));
			if (value1 > 0) {
				value = signupService.updateuserSignup(status, Parentid);
				userIncreasekeyService.addIncreasekey("dc_message", message.getSid());
			} else {
				value = -1;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			value = -1;
		}
		return value;

	}

	public int reliableupdateuserSignup(String status, String Parentid, String schoolid, String info, String userid) {
		return -2;

	}

	public int updateSignupstatus(String status, String Parentid) {
		int result = 0;
		result = signupService.updateuserSignup(status, Parentid);
		return result;

	}

	@HystrixCommand(fallbackMethod = "reliableshowActivityUser")
	@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000")
	public Object showActivityUser(String userid) {
		List<Map<String, Object>> userlist = userservice.selectUser(userid);
		Map<String, Object> map = new HashMap<String, Object>();
		if (userlist.size() > 0) {
			map.put("userid", userlist.get(0).get("id"));
			map.put("userlabel", userlist.get(0).get("label"));
			map.put("userphone", userlist.get(0).get("phone"));
		}
		return JSON.toJSON(map);
	}

	public Object reliableshowActivityUser(String userid) {
		return -1;
	}

	public Object selectSignupField() {
		List<Map<String, Object>> list = signupFieldService.selectSignupField();
		return JSON.toJSON(list);
	}
	//活动排序
	public int activitySort(String activityid,String sort) {
		int addvalue = 0;
		List<Map<String, Object>> activitylist = activityService.showActivitySort();
		Map<String, Object> activitySortmap = new HashMap<>();
		int initialsort = 0;// 排序前的排序号
		int currentsort = 0;// 要排的排序号
		for (int i = 0; i < activitylist.size(); i++) {

			if (activityid.equals(activitylist.get(i).get("id").toString())) {
				initialsort = Integer.parseInt(activitylist.get(i).get("sort").toString());
				currentsort = Integer.parseInt(activitylist.get( Integer.parseInt(sort) - 1).get("sort").toString());
				activitylist.remove(i);
			}

		}
		activitySortmap.put("id", activityid);
		activitySortmap.put("sort", currentsort);
		activitylist.add(activitySortmap);
		for (int i = 0; i < activitylist.size(); i++) {
			if (currentsort < initialsort) {
				if (Integer.parseInt(activitylist.get(i).get("sort").toString()) >= currentsort
						&& !activityid.equals(activitylist.get(i).get("id").toString())) {
					Map<String, Object> map = new HashMap<>();
					int a = Integer.parseInt(activitylist.get(i).get("sort").toString()) + 1;
					System.out.println(activitylist.get(i).get("id") + ":" + a);
					map.put("id", activitylist.get(i).get("id"));
					map.put("sort", a);

					activitylist.set(i, map);
				}
			} else {
				if (Integer.parseInt(activitylist.get(i).get("sort").toString()) <= currentsort
						&& !activityid.equals(activitylist.get(i).get("id").toString())) {
					Map<String, Object> map = new HashMap<>();
					int a = Integer.parseInt(activitylist.get(i).get("sort").toString()) - 1;
					System.out.println(activitylist.get(i).get("id") + ":" + a);
					map.put("id", activitylist.get(i).get("id"));
					map.put("sort", a);
					activitylist.set(i, map);
				}
			}

		}
		for (int i = 0; i < activitylist.size(); i++) {
			String id = activitylist.get(i).get("id").toString();
			int a = Integer.parseInt(activitylist.get(i).get("sort").toString());
			addvalue += activityService.updateActivitySort(activitylist.get(i).get("id").toString(),
					activitylist.get(i).get("sort").toString());
		}
		int value = 1;
		if (activitylist.size() == addvalue) {
			value = 0;
		}
		return value;
	}

}
