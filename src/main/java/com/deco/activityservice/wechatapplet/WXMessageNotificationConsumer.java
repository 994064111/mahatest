package com.deco.activityservice.wechatapplet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.deco.MessageURL;
import com.deco.service.pc.ApplyMchService;
import com.deco.service.pc.UserService;
import com.deco.utils.RequestUtils;

@Component
public class WXMessageNotificationConsumer {

	@Autowired
	private ApplyMchService applyMchService;
	@Resource(name = "request")
	private RequestUtils request;
	@Autowired
	private UserService userService;

	SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * 活动报名通知
	 * @return
	 */
	public int ActivitieSenlistRelease (String params){
		JSONObject json = JSONArray.parseObject(params);
		String paytype=json.getString("paytype");//paytype 类型：1 QQ 2 微信小程序 3 微信第三方APP 4 活动小程序 5 H5  
		String paystatus=json.getString("paystatus");// paystatus 状态0 正式 1 测试
		String userid = json.getString("userid");//用户id
		String label=json.getString("label");//活动名称
		String activitystart=json.getString("activitystart");//活动开始时间
		String address=json.getString("address");//活动地址
		String enrollname=json.getString("enrollname");//报名人姓名
		String info="如有其他需求，请提前联系发起人";//备注
		int value=0;
		List<Map<String, Object>> applyMchlist = applyMchService.selectInformation(paytype, paystatus);
		if(applyMchlist.size()>0){
		JSONObject ob = request.httpsRequest(MessageURL.access_token_url+"&appid="+applyMchlist.get(0).get("apply_id")+"&secret="+applyMchlist.get(0).get("apply_secret"),"POST",null);
		String access_token=ob.getString("access_token");
		List<Map<String, Object>> UserThirdaccountlist=  userService.selectUserThirdaccount(userid);
		String thirdaccount="";
		if(UserThirdaccountlist.size() > 0){
			for(int i=0; i < UserThirdaccountlist.size(); i++){
				String usertype = "";
				try {
					usertype = UserThirdaccountlist.get(i).get("type").toString();
				} catch (Exception e) {
					// TODO: handle exception
				}
				if(paytype.equals(usertype)){
					try {
						thirdaccount= UserThirdaccountlist.get(i).get("thirdaccount").toString();//获取用户openid
					} catch (Exception e) {
						System.out.println("小程序的用户openid未获取到。");
						value=-1;//消息发送失败
					}
				}
			}	
		}
		
		StringBuffer a= new StringBuffer();
		a.append("{\"touser\":\""+thirdaccount+"\",\"data\":{\"thing1\":{\"value\":\""+label+"\"},\"date5\":{\"value\":\""+activitystart+"\"},\"thing3\":{\"value\":\""+address+"\"},\"name4\":{\"value\":\""+enrollname+"\"},\"thing6\":{\"value\":\""+info+"\"}},\"template_id\":\""+MessageURL.activity_enroll_MessagetemplateID+"\"}");
		
		String q= a.toString();
		String jsonString= JSON.toJSONString(q);
		System.out.println(jsonString);
		String content =StringEscapeUtils.unescapeJava(q);
		JSONObject jsonObject1 =JSONObject.parseObject(content);
		String ob1 = request.post(jsonObject1, MessageURL.message_url+"access_token="+access_token);
		JSONObject a1= JSON.parseObject(ob1);
		String errmsg=a1.getString("errmsg");
		if(errmsg.equals("ok")){
			value=1;//消息发送成功
			System.out.println("消息发送成功！");
		}else{
			value=-1;//消息发送失败
			System.out.println("消息发送失败！问题为："+ob1);
		}
		}else{
			value=-2;//数据库微信配置问题
			System.out.println("消息发送失败！数据库微信配置问题。");
		}
		return value;
		
	}
	
	public String showActivityEnrollMessagetemplateID(){
		return MessageURL.activity_enroll_MessagetemplateID;
		
	}

}
