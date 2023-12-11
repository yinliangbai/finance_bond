package com.baiyinliang.finance.tools;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "wx")
@PropertySource("classpath:/wx.properties")
@Data
public class WxConfig {

    @Value("${wx.appid}")
    private String wxAppid;
    @Value("${wx.appsecret}")
    private String wxAppsecret;
    @Value("${wx.template_id}")
    private String wxTemplateId;
    @Value("${wx.open_id}")
    private String wxOpenId;

}
