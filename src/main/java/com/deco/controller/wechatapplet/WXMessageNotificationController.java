package com.deco.controller.wechatapplet;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deco.activityservice.wechatapplet.WXMessageNotificationConsumer;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

/**
 * 小程序消息通知
 * 
 * @author admin
 *
 */
@RestController
@RequestMapping("/wxactivitymessage")
public class WXMessageNotificationController {

	@Autowired
	private WXMessageNotificationConsumer MessageNotificationConsumer;

	/**
	 *活动报名通知
	 * paytype 类型：1 QQ 2 微信小程序 3 微信第三方APP 4 活动小程序 5 H5  
	 * paystatus 状态0 正式 1 测试
	 * @return
	 */
	// @PreAuthorize("hasAuthority('wxactivitymessage/activitiesenlistRelease')")
	@PostMapping("activitiesenlistRelease")
	public int ActivitieSenlistRelease(@RequestBody String params) {
		int value = MessageNotificationConsumer.ActivitieSenlistRelease(params);
		return value;

	}
	
	/**
	 * 给前台消息模板ID
	 */
	// @PreAuthorize("hasAuthority('wxactivitymessage/showmessagetemplateid')")
	@PostMapping("showmessagetemplateid")
	public String showActivityEnrollMessagetemplateID(){
		return MessageNotificationConsumer.showActivityEnrollMessagetemplateID();
		
	}

}
