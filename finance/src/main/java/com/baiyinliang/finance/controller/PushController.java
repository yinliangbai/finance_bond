package com.baiyinliang.finance.controller;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Created by qcl on 2019-03-28
 * 微信：2501902696
 * desc: 模版消息推送模拟
 */
@RestController
@RequestMapping("/bond-msg")
public class PushController {


    /*
     * 微信测试账号推送
     * */
    @GetMapping("/push")
    public void push() {
        //1，配置
        WxMpDefaultConfigImpl wxStorage = new WxMpDefaultConfigImpl();
        wxStorage.setAppId("wx3a9a881cf4ff76d3");
        wxStorage.setSecret("729fb78d6ff441299bd13de4d4347f41");
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);

        //2,推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser("oy3VS55SZ4XRfFELK83mBSMRtvks")//要推送的用户openid
                .templateId("TpB8NkTl8mNDYi9yW1KJ7ohKcT4nkbhe64RsH0gUVMs")//模版id
//                .url("https://30paotui.com/")//点击模版消要访问的网址
                .build();

        WxMpTemplateData wxMpTemplateData = new WxMpTemplateData("bond_price_msg", "这里这里呀");

        //3,如果是正式版发送模版消息，这里需要配置你的信息
        //        templateMessage.addData(new WxMpTemplateData("name", "value", "#FF00FF"));
        //                templateMessage.addData(new WxMpTemplateData(name2, value2, color2));
        try {
            templateMessage.setData(Arrays.asList(wxMpTemplateData));
            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }

    }


}