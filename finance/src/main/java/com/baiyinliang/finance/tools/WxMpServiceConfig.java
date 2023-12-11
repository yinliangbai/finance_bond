package com.baiyinliang.finance.tools;


import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxMpServiceConfig {


    @Autowired
    private WxConfig wxConfig;


    @Bean("wxMpService")
    public WxMpService initWxMpService(){
        WxMpDefaultConfigImpl wxStorage = new WxMpDefaultConfigImpl();
        wxStorage.setAppId(wxConfig.getWxAppid());
        wxStorage.setSecret(wxConfig.getWxAppsecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        return wxMpService;
    }

}
